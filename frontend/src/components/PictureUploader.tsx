import React, { useCallback, useState, useEffect } from 'react'
import { useDropzone } from 'react-dropzone'
import { FileImage, X, Settings, CheckCircle, AlertCircle, Sparkles } from 'lucide-react'
import { mediaAPI } from '../services/api'
import type { ProcessingSettings } from '../services/api'
// @ts-ignore
import JSZip from 'jszip' // If you get a module error, run: npm install jszip

interface FileWithPreview extends File {
  preview?: string
}

interface PictureProcessingSettings extends ProcessingSettings {
  resize: boolean
  width: string
  height: string
}

const ProcessedFileName: React.FC<{ originalName: string; jobId: string }> = ({ originalName, jobId }) => {
  const [processedName, setProcessedName] = React.useState(originalName);

  React.useEffect(() => {
    (async () => {
      const job = await mediaAPI.getJob(jobId);
      const extension = job.outputFormat ? job.outputFormat.toLowerCase() : 'jpg';
      let baseName = originalName;
      if (baseName.includes('.')) {
        baseName = baseName.substring(0, baseName.lastIndexOf('.'));
      }
      setProcessedName(`${baseName}.${extension}`);
    })();
  }, [originalName, jobId]);

  return <span className="font-semibold text-slate-800">{processedName}</span>;
};

const PictureUploader: React.FC = () => {
  const [files, setFiles] = useState<FileWithPreview[]>([])
  const [uploading, setUploading] = useState(false)
  const [uploadResults, setUploadResults] = useState<Array<{ file: string; success: boolean; message: string; jobId?: string }>>([])
  const [uploadSettings, setUploadSettings] = useState<PictureProcessingSettings>({
    quality: 'high',
    format: 'jpg',
    addSubtitles: false,
    extractMetadata: true,
    optimizeForWeb: true,
    optimizeForMobile: false,
    resize: false,
    width: '',
    height: '',
  })
  const [downloadUrls, setDownloadUrls] = useState<{ [jobId: string]: string }>({})
  const [jobIds, setJobIds] = useState<string[]>([])

  const onDrop = useCallback((acceptedFiles: File[]) => {
    const filesWithPreview = acceptedFiles.map(file => {
      const fileWithPreview = file as FileWithPreview
      fileWithPreview.preview = URL.createObjectURL(file)
      return fileWithPreview
    })
    setFiles(prev => [...prev, ...filesWithPreview])
    setUploadResults([])
  }, [])

  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    onDrop,
    accept: {
      'image/*': ['.jpeg', '.jpg', '.png', '.gif', '.bmp', '.webp']
    },
    multiple: true
  })

  const removeFile = (index: number) => {
    setFiles(prev => prev.filter((_, i) => i !== index))
  }

  const handleUpload = async () => {
    if (files.length === 0) return
    setUploading(true)
    setDownloadUrls({})
    setJobIds([])
    const results: Array<{ file: string; success: boolean; message: string; jobId?: string }> = []
    const newJobIds: string[] = []
    try {
      for (const file of files) {
        try {
          const response = await mediaAPI.uploadFile(file, uploadSettings)
          results.push({
            file: file.name,
            success: true,
            message: response.message || 'Upload successful, processing...',
            jobId: response.id
          })
          newJobIds.push(response.id)
          // Poll job status
          const pollJob = async (jobId: string, retries = 60) => {
            for (let i = 0; i < retries; i++) {
              try {
                const job = await mediaAPI.getJob(jobId)
                console.log('Polling job:', jobId, 'status:', job.status, 'outputFormat:', job.outputFormat);
                if (job.status === 'COMPLETED') {
                  const url = `https://cloudtranscode.onrender.com/api/v1/jobs/${jobId}/download`;
                  setDownloadUrls(prev => ({ ...prev, [jobId]: url }))
                  results[results.length - 1].message = 'Processing complete! Ready to download.'
                  setUploadResults([...results])
                  break
                } else if (job.status === 'FAILED') {
                  results[results.length - 1].message = 'Processing failed.'
                  setUploadResults([...results])
                  break
                }
              } catch (err) {
                console.error('Error polling job status:', err)
              }
              await new Promise(res => setTimeout(res, 2000)) // wait 2s
            }
          }
          pollJob(response.id)
        } catch (error) {
          results.push({
            file: file.name,
            success: false,
            message: error instanceof Error ? error.message : 'Upload failed'
          })
        }
      }
      setJobIds(newJobIds)
      setUploadResults(results)
      if (results.every(r => r.success)) {
        setFiles([])
      }
    } catch (error) {
      console.error('Upload error:', error)
    } finally {
      setUploading(false)
    }
  }

  // Download a single file as blob
  const handleDownload = async (url: string, filename: string, jobId: string) => {
    try {
      console.log('Starting download for:', filename);
      const response = await fetch(`https://cloudtranscode.onrender.com/api/v1/jobs/${jobId}/download`, {
        method: 'GET',
      });
      console.log('Download response status:', response.status);
      if (!response.ok) {
        throw new Error(`Failed to download file: ${response.status}`);
      }
      const blob = await response.blob();
      console.log('Blob created:', blob.type, blob.size);
      const link = document.createElement('a');
      link.href = window.URL.createObjectURL(blob);
      link.download = filename;
      link.style.display = 'none';  // Hide the link
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(link.href); // Clean up the object URL
    } catch (err: unknown) {
      console.error('Download error:', err);
      if (err instanceof Error) {
        console.error('Error details:', {
          name: err.name,
          message: err.message,
          stack: err.stack
        });
        alert('Failed to download file: ' + err.message);
      } else {
        console.error('Unknown error type:', err);
        alert('Failed to download file: Unknown error');
      }
    }
  }

  // Download all as zip
  const handleDownloadAll = async () => {
    if (typeof JSZip === 'undefined') {
      // fallback: download individually
      jobIds.forEach(jobId => {
        if (downloadUrls[jobId]) {
          const filename = uploadResults.find(r => r.jobId === jobId)?.file || `processed-image.${uploadSettings.format}`
          handleDownload(downloadUrls[jobId], filename, jobId)
        }
      })
      return
    }
    const zip = new JSZip()
    await Promise.all(jobIds.map(async jobId => {
      if (downloadUrls[jobId]) {
        const response = await fetch(downloadUrls[jobId])
        const blob = await response.blob()
        const filename = uploadResults.find(r => r.jobId === jobId)?.file || `processed-image.${uploadSettings.format}`
        zip.file(filename, blob)
      }
    }))
    const content = await zip.generateAsync({ type: 'blob' })
    const link = document.createElement('a')
    link.href = window.URL.createObjectURL(content)
    link.download = 'processed-images.zip'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
  }

  useEffect(() => {
    if (Object.keys(downloadUrls).length > 0) {
      console.log('Download buttons will be shown for URLs:', Object.values(downloadUrls))
    }
  }, [downloadUrls])

  return (
    <div className="space-y-8">
      {/* Upload Area and Settings Side by Side  */}
      <div className="flex flex-col md:flex-row md:space-x-8 space-y-8 md:space-y-0">
        {/* Upload Area */}
        <div className="flex-1 bg-white/70 backdrop-blur-sm rounded-2xl shadow-xl border border-slate-200/50 overflow-hidden">
          <div
            {...getRootProps()}
            className={`border-2 border-dashed rounded-2xl m-6 p-16 text-center cursor-pointer transition-all duration-300 ${
              isDragActive
                ? 'border-purple-400 bg-gradient-to-br from-purple-50 to-pink-50 scale-[1.02]'
                : 'border-slate-300 hover:border-slate-400 hover:bg-slate-50/50'
            } ${uploading ? 'pointer-events-none opacity-50' : ''}`}
          >
            <input {...getInputProps()} />
            <div className="flex flex-col items-center space-y-4">
              <div className="relative">
                <div className="w-16 h-16 bg-gradient-to-r from-purple-500 to-pink-600 rounded-2xl flex items-center justify-center shadow-lg">
                  <FileImage className="w-8 h-8 text-white" />
                </div>
                {isDragActive && (
                  <div className="absolute -top-1 -right-1">
                    <Sparkles className="w-6 h-6 text-yellow-500 animate-pulse" />
                  </div>
                )}
              </div>
              <div className="space-y-2">
                <h3 className="text-2xl font-bold text-slate-800">
                  {isDragActive ? 'Drop images here!' : 'Upload your image files'}
                </h3>
                <p className="text-slate-600 font-medium">
                  Drag and drop images, or click to browse
                </p>
                <div className="flex items-center justify-center space-x-2 text-sm text-slate-500">
                  <FileImage className="w-4 h-4" />
                  <span>JPG, PNG, GIF, BMP, WEBP, TIFF</span>
                </div>
              </div>
            </div>
          </div>
        </div>
        {/* Image Processing Settings */}
        <div className="flex-1 bg-white/70 backdrop-blur-sm rounded-2xl shadow-xl border border-slate-200/50 p-8">
          <div className="flex items-center space-x-3 mb-8">
            <div className="w-8 h-8 bg-gradient-to-r from-purple-500 to-pink-600 rounded-lg flex items-center justify-center">
              <Settings className="w-5 h-5 text-white" />
            </div>
            <h3 className="text-xl font-bold text-slate-800">Image Processing Settings</h3>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
            <div className="space-y-3">
              <label className="block text-sm font-semibold text-slate-700">Output Quality</label>
              <select
                value={uploadSettings.quality}
                onChange={e => setUploadSettings(prev => ({ ...prev, quality: e.target.value }))}
                className="w-full px-4 py-3 bg-white border border-slate-300 rounded-xl focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-purple-500 transition-colors font-medium"
                disabled={uploading}
              >
                <option value="high">High</option>
                <option value="medium">Medium</option>
                <option value="low">Low</option>
              </select>
            </div>
            <div className="space-y-3">
              <label className="block text-sm font-semibold text-slate-700">Output Format</label>
              <select
                value={uploadSettings.format}
                onChange={e => setUploadSettings(prev => ({ ...prev, format: e.target.value }))}
                className="w-full px-4 py-3 bg-white border border-slate-300 rounded-xl focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-purple-500 transition-colors font-medium"
                disabled={uploading} 
              >
                <option value="jpg">JPG</option>
                <option value="png">PNG</option>
                <option value="webp">WEBP</option>
                <option value="gif">GIF</option>
                <option value="tiff">TIFF</option> 
                <option value="bmp">BMP</option>
                <option value="avif">AVIF</option>
                <option value="heif">HEIF</option>
                <option value="ico">ICO</option>
                <option value="tga">TGA</option>
              </select>
            </div>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-8 mt-8">
            <div className="flex items-center space-x-2">
              <input
                type="checkbox"
                checked={uploadSettings.resize}
                onChange={e => setUploadSettings(prev => ({ ...prev, resize: e.target.checked }))}
                disabled={uploading}
                className="w-5 h-5 text-purple-600 border-gray-300 rounded focus:ring-purple-500"
              />
              <span className="text-sm text-slate-700">Resize Image</span>
            </div>
            {uploadSettings.resize && (
              <div className="flex space-x-2">
                <input
                  type="number"
                  placeholder="Width"
                  value={uploadSettings.width}
                  onChange={e => setUploadSettings(prev => ({ ...prev, width: e.target.value }))}
                  className="w-24 px-3 py-2 border border-slate-300 rounded-xl focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-purple-500"
                  disabled={uploading}
                />
                <input
                  type="number"
                  placeholder="Height"
                  value={uploadSettings.height}
                  onChange={e => setUploadSettings(prev => ({ ...prev, height: e.target.value }))}
                  className="w-24 px-3 py-2 border border-slate-300 rounded-xl focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-purple-500"
                  disabled={uploading}
                />
              </div>
            )}
          </div>
          <button
            onClick={handleUpload}
            className="mt-8 w-full py-3 px-6 bg-gradient-to-r from-purple-500 to-pink-600 text-white font-bold rounded-xl shadow-lg hover:from-purple-600 hover:to-pink-700 transition-all duration-200 disabled:opacity-50"
            disabled={uploading || files.length === 0}
          >
            {uploading ? 'Uploading...' : 'Start Image Processing'}
          </button>
        </div>
      </div>

      {/* Upload Results */}
      {uploadResults.length > 0 && (
        <div className="bg-white/70 backdrop-blur-sm rounded-2xl shadow-xl border border-slate-200/50 p-8">
          <div className="flex items-center space-x-3 mb-6">
            <div className="w-8 h-8 bg-gradient-to-r from-green-500 to-emerald-600 rounded-lg flex items-center justify-center">
              <CheckCircle className="w-5 h-5 text-white" />
            </div>
            <h3 className="text-xl font-bold text-slate-800">Upload Results</h3>
          </div>
          <div className="space-y-3">
            {uploadResults.map((result, index) => (
              <div key={index} className="flex items-center space-x-4 p-4 bg-slate-50/50 rounded-xl border border-slate-200/50">
                <div className={`w-10 h-10 rounded-lg flex items-center justify-center ${
                  result.success ? 'bg-green-100' : 'bg-red-100'
                }`}>
                  {result.success ? (
                    <CheckCircle className="w-5 h-5 text-green-600" />
                  ) : (
                    <AlertCircle className="w-5 h-5 text-red-600" />
                  )}
                </div>
                <div className="flex-1">
                  {result.success && result.jobId ? (
                    // Show processed filename with new extension
                    <ProcessedFileName originalName={result.file} jobId={result.jobId} />
                  ) : (
                    // Show original filename for failed jobs
                    <span className="font-semibold text-slate-800">{result.file}</span>
                  )}
                  <p className={`text-sm ${result.success ? 'text-green-600' : 'text-red-600'}`}>{result.message}</p>
                </div>
                {/* Download button for each file */}
                {result.success && typeof result.jobId === 'string' && result.jobId && downloadUrls[result.jobId] && (
                  <button
                    className="ml-4 px-4 py-2 bg-green-600 text-white rounded-lg font-semibold shadow hover:bg-green-700 transition"
                    onClick={async () => {
                      if (!result.jobId) return;
                      const job = await mediaAPI.getJob(result.jobId);
                      const extension = job.outputFormat ? job.outputFormat.toLowerCase() : 'jpg';
                      let baseName = result.file;
                      if (baseName.includes('.')) {
                        baseName = baseName.substring(0, baseName.lastIndexOf('.'));
                      }
                      const downloadFileName = `${baseName}.${extension}`;
                      console.log('Download filename used:', downloadFileName);
                      handleDownload(downloadUrls[result.jobId ?? ''], downloadFileName, result.jobId ?? '');
                    }}
                  >
                    Download
                  </button>
                )}
              </div>
            ))}
          </div>
          {/* Download All button if more than one file is ready */}
          {jobIds.length > 1 && jobIds.filter(j => typeof j === 'string' && j).every(jobId => downloadUrls[jobId ?? '']) && (
            <div className="flex justify-end mt-6">
              <button
                className="px-6 py-3 bg-blue-600 text-white rounded-xl font-semibold shadow-lg hover:bg-blue-700 transition"
                onClick={handleDownloadAll}
              >
                Download All
              </button>
            </div>
          )}
        </div>
      )}

      {/* File List */}
      {files.length > 0 && (
        <div className="bg-white/70 backdrop-blur-sm rounded-2xl shadow-xl border border-slate-200/50 p-8">
          <div className="flex items-center space-x-3 mb-6">
            <div className="w-8 h-8 bg-gradient-to-r from-purple-500 to-pink-600 rounded-lg flex items-center justify-center">
              <FileImage className="w-5 h-5 text-white" />
            </div>
            <h3 className="text-xl font-bold text-slate-800">Files to Upload</h3>
          </div>
          <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
            {files.map((file, index) => (
              <div key={index} className="relative group">
                <img
                  src={file.preview}
                  alt={file.name}
                  className="w-full h-32 object-cover rounded-xl"
                />
                <button
                  onClick={() => removeFile(index)}
                  className="absolute top-2 right-2 p-1 bg-red-500 text-white rounded-full opacity-0 group-hover:opacity-100 transition-opacity"
                >
                  <X className="w-4 h-4" />
                </button>
                <div className="mt-2">
                  <p className="text-sm font-medium text-slate-800 truncate">{file.name}</p>
                  <p className="text-xs text-slate-500">{(file.size / 1024).toFixed(1)} KB</p>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  )
}

export default PictureUploader 

