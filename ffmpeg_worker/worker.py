import os
import subprocess
import requests
import pika
import json
import firebase_admin
from firebase_admin import credentials, storage
import time

RABBITMQ_HOST = os.environ.get("RABBITMQ_HOST", "rabbitmq")
RABBITMQ_USER = os.environ.get("RABBITMQ_USER", "guest")
RABBITMQ_PASS = os.environ.get("RABBITMQ_PASS", "guest")
QUEUE_NAME = os.environ.get("QUEUE_NAME", "media_jobs")
BACKEND_UPDATE_URL = os.environ.get("BACKEND_UPDATE_URL", "http://backend:8080/api/v1/jobs/worker-callback")
FIREBASE_CREDENTIALS = os.environ.get("FIREBASE_CREDENTIALS", "firebase-service-account.json")
FIREBASE_BUCKET = os.environ.get("FIREBASE_BUCKET")  # Set this to your bucket name

# Initialize Firebase
print(f"[WORKER] Initializing Firebase with bucket: {FIREBASE_BUCKET}")
print(f"[WORKER] Firebase credentials path: {FIREBASE_CREDENTIALS}")
try:
    if not firebase_admin._apps:
        cred = credentials.Certificate(FIREBASE_CREDENTIALS)
        firebase_admin.initialize_app(cred, {
            'storageBucket': FIREBASE_BUCKET
        })
        print(f"[WORKER] Firebase initialized successfully")
    else:
        print(f"[WORKER] Firebase already initialized")
except Exception as e:
    print(f"[WORKER] Failed to initialize Firebase: {e}")
    # Don't exit here, let the worker continue and fail gracefully when trying to use Firebase


def upload_to_firebase(local_path, dest_path):
    bucket = storage.bucket()
    blob = bucket.blob(dest_path)
    blob.upload_from_filename(local_path)
    return blob.name


def process_job(job):
    print(f"[WORKER] Processing job: {job['id']}")
    print(f"[WORKER] Full job data: {json.dumps(job, indent=2)}")
    
    input_url = job['input_url']
    output_format = job['output_format']
    output_quality = job.get('output_quality', 'high')
    settings = job.get('settings', {})
    
    print(f"[WORKER] Settings received: {settings}")
    print(f"[WORKER] Resize enabled: {settings.get('resize')}")
    print(f"[WORKER] Width: {settings.get('width')}")
    print(f"[WORKER] Height: {settings.get('height')}")
    
    output_file = f"/tmp/output.{output_format}"
    input_file = "/tmp/input"

    # Download input file
    print(f"[WORKER] Downloading from: {input_url}")
    r = requests.get(input_url, stream=True)
    with open(input_file, "wb") as f:
        for chunk in r.iter_content(chunk_size=8192):
            f.write(chunk)

    # Build FFmpeg command
    ffmpeg_cmd = ["ffmpeg", "-y", "-i", input_file]
    
    # Add resize filter if requested
    if settings.get('resize') == 'true':
        width = settings.get('width')
        height = settings.get('height')
        if width and height:
            print(f"[WORKER] Resizing to {width}x{height}")
            ffmpeg_cmd.extend(["-vf", f"scale={width}:{height}"])
        else:
            print(f"[WORKER] Resize enabled but missing width/height: width={width}, height={height}")
    else:
        print(f"[WORKER] Resize not enabled or not 'true'")
    
    # Add quality settings based on format
    print(f"[WORKER] Output quality requested: {output_quality}")
    
    if output_format.lower() in ['jpg', 'jpeg']:
        quality_map = {'low': '25', 'medium': '15', 'high': '3'}
        quality = quality_map.get(output_quality, '15')
        ffmpeg_cmd.extend(["-q:v", quality])
        print(f"[WORKER] JPEG quality set to: {quality} (scale: 1-31, lower=better quality)")
    elif output_format.lower() == 'webp':
        quality_map = {'low': '30', 'medium': '60', 'high': '95'}
        quality = quality_map.get(output_quality, '60')
        ffmpeg_cmd.extend(["-quality", quality])
        print(f"[WORKER] WebP quality set to: {quality} (scale: 0-100, higher=better quality)")
    elif output_format.lower() == 'png':
        # PNG is lossless, but we can control compression
        compression_map = {'low': '0', 'medium': '6', 'high': '9'}
        compression = compression_map.get(output_quality, '6')
        ffmpeg_cmd.extend(["-compression_level", compression])
        print(f"[WORKER] PNG compression set to: {compression} (scale: 0-9, higher=more compression)")
    elif output_format.lower() in ['gif', 'bmp', 'tiff']:
        # For other formats, use general quality parameter
        quality_map = {'low': '25', 'medium': '15', 'high': '5'}
        quality = quality_map.get(output_quality, '15')
        ffmpeg_cmd.extend(["-q:v", quality])
        print(f"[WORKER] {output_format.upper()} quality set to: {quality}")
    else:
        print(f"[WORKER] No specific quality settings for format: {output_format}")
    
    ffmpeg_cmd.append(output_file)
    
    print(f"[WORKER] Final FFmpeg command: {' '.join(ffmpeg_cmd)}")
    result = subprocess.run(ffmpeg_cmd, capture_output=True, text=True)
    
    if result.returncode != 0:
        print(f"[WORKER] FFmpeg failed: {result.stderr}")
        notify_backend(job['id'], "failed", error_message=result.stderr)
        return

    # Upload output file to Firebase
    processed_file_key = f"processed/{job['id']}/output.{output_format}"
    try:
        uploaded_key = upload_to_firebase(output_file, processed_file_key)
        print(f"[WORKER] Uploaded to Firebase: {uploaded_key}")
        notify_backend(job['id'], "completed", processed_file_key=uploaded_key)
    except Exception as e:
        print(f"[WORKER] Failed to upload to Firebase: {e}")
        notify_backend(job['id'], "failed", error_message=str(e))


def notify_backend(job_id, status, processed_file_key=None, error_message=None):
    data = {
        "job_id": job_id,
        "status": status,
        "processed_file_key": processed_file_key,
        "error_message": error_message
    }
    try:
        print(f"[WORKER] Notifying backend: {data}")
        response = requests.post(BACKEND_UPDATE_URL, json=data)
        print(f"[WORKER] Backend response: {response.status_code}")
    except Exception as e:
        print(f"[WORKER] Failed to notify backend: {e}")


def main():
    print(f"[WORKER] Connecting to RabbitMQ at {RABBITMQ_HOST}")
    connection = None
    max_retries = 10
    for attempt in range(1, max_retries + 1):
        try:
            connection = pika.BlockingConnection(pika.ConnectionParameters(host=RABBITMQ_HOST, credentials=pika.PlainCredentials(RABBITMQ_USER, RABBITMQ_PASS)))
            print(f"[WORKER] Connected to RabbitMQ on attempt {attempt}")
            break
        except pika.exceptions.AMQPConnectionError as e:
            print(f"[WORKER] Failed to connect to RabbitMQ (attempt {attempt}/{max_retries}): {e}")
            time.sleep(5)
    if not connection:
        print(f"[WORKER] Could not connect to RabbitMQ after {max_retries} attempts. Exiting.")
        return
    channel = connection.channel()
    channel.queue_declare(queue=QUEUE_NAME)

    def callback(ch, method, properties, body):
        try:
            job = json.loads(body)
            print(f"[WORKER] Received job: {job}")
            process_job(job)
        except Exception as e:
            print(f"[WORKER] Error processing job: {e}")
        finally:
            ch.basic_ack(delivery_tag=method.delivery_tag)

    channel.basic_qos(prefetch_count=1)
    channel.basic_consume(queue=QUEUE_NAME, on_message_callback=callback)
    print(f"[WORKER] Waiting for jobs on queue: {QUEUE_NAME}")
    channel.start_consuming()


if __name__ == "__main__":
    main() 