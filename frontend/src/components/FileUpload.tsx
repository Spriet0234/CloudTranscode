import React, { useCallback, useState, useEffect } from 'react'
import { useDropzone } from 'react-dropzone'
import { Upload, FileVideo, FileImage, FileAudio, X, Settings, CheckCircle, AlertCircle, Sparkles } from 'lucide-react'
import { mediaAPI } from '../services/api'
import type { ProcessingSettings } from '../services/api'

interface FileWithPreview extends File {
  preview?: string
}

const FileUpload: React.FC = () => {
  const [files, setFiles] = useState<FileWithPreview[]>([])
  const [uploading, setUploading] = useState(false)
  const [uploadResults, setUploadResults] = useState<Array<{ file: string; success: boolean; message: string }>>([])
  const [uploadSettings, setUploadSettings] = useState<ProcessingSettings>({
    quality: 'high',
    format: 'auto',
    addSubtitles: false,
    extractMetadata: true,
    optimizeForWeb: true,
    optimizeForMobile: false,
  })
  const [downloadUrl, setDownloadUrl] = useState<string | null>(null)
  const [jobId, setJobId] = useState<string | null>(null)

  useEffect(() => {
    if (jobId) {
      // This useEffect ensures jobId is used and avoids Vercel build errors
      console.log('Current jobId:', jobId)
    }
  }, [jobId])

  const onDrop = useCallback((acceptedFiles: File[]) => {
    const filesWithPreview = acceptedFiles.map(file => {
      const fileWithPreview = file as FileWithPreview
      if (file.type.startsWith('image/')) {
        fileWithPreview.preview = URL.createObjectURL(file)
      }
      return fileWithPreview
    })
    setFiles(prev => [...prev, ...filesWithPreview])
    setUploadResults([]) // Clear previous results
  }, [])

  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    onDrop,
    accept: {
      'video/*': ['.mp4', '.avi', '.mov', '.wmv', '.flv', '.webm'],
      'audio/*': ['.mp3', '.wav', '.flac', '.aac', '.ogg'],
      'image/*': ['.jpeg', '.jpg', '.png', '.gif', '.bmp', '.webp'],
    },
    multiple: true,
  })

  const removeFile = (index: number) => {
    setFiles(prev => prev.filter((_, i) => i !== index))
  }

  const getFileIcon = (file: File) => {
    if (file.type.startsWith('video/')) return <FileVideo className="w-8 h-8 text-blue-500" />
    if (file.type.startsWith('audio/')) return <FileAudio className="w-8 h-8 text-emerald-500" />
    if (file.type.startsWith('image/')) return <FileImage className="w-8 h-8 text-purple-500" />
    return <Upload className="w-8 h-8 text-slate-500" />
  }

  const formatFileSize = (bytes: number) => {
    if (bytes === 0) return '0 Bytes'
    const k = 1024
    const sizes = ['Bytes', 'KB', 'MB', 'GB']
    const i = Math.floor(Math.log(bytes) / Math.log(k))
    console.log(jobId)
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
  }

  const handleUpload = async () => {
    if (files.length === 0) return

    setUploading(true)
    setDownloadUrl(null)
    setJobId(null)
    const results: Array<{ file: string; success: boolean; message: string }> = []

    try {
      for (const file of files) {
        try {
          // Upload file and get job
          const response = await mediaAPI.uploadFile(file, uploadSettings)
          console.log('Upload response:', response)
          results.push({
            file: file.name,
            success: true,
            message: 'Upload successful, processing...'
          })
          setJobId(response.id)

          // Poll job status
          const pollJob = async (jobId: string, retries = 60) => {
            for (let i = 0; i < retries; i++) {
              try {
                const job = await mediaAPI.getJob(jobId)
                console.log(`Polled job status for ${jobId}:`, job.status)
                if (job.status === 'COMPLETED') {
                  const url = await mediaAPI.getDownloadUrl(jobId)
                  console.log('Download URL fetched:', url)
                  setDownloadUrl(url)
                  results[results.length - 1].message = 'Processing complete! Ready to download.'
                  break
                } else if (job.status === 'FAILED') {
                  results[results.length - 1].message = 'Processing failed.'
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
          console.error(`Error uploading ${file.name}:`, error)
          results.push({
            file: file.name,
            success: false,
            message: error instanceof Error ? error.message : 'Upload failed'
          })
        }
      }
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

  useEffect(() => {
    if (downloadUrl) {
      console.log('Download button will be shown for URL:', downloadUrl)
    }
  }, [downloadUrl])

  return (
    <div className="space-y-8">
      {/* Upload Area */}
      <div className="bg-white/70 backdrop-blur-sm rounded-2xl shadow-xl border border-slate-200/50 overflow-hidden">
        <div
          {...getRootProps()}
          className={`border-2 border-dashed rounded-2xl m-6 p-16 text-center cursor-pointer transition-all duration-300 ${
            isDragActive
              ? 'border-indigo-400 bg-gradient-to-br from-indigo-50 to-blue-50 scale-[1.02]'
              : 'border-slate-300 hover:border-slate-400 hover:bg-slate-50/50'
          } ${uploading ? 'pointer-events-none opacity-50' : ''}`}
        >
          <input {...getInputProps()} />
          <div className="flex flex-col items-center space-y-4">
            <div className="relative">
              <div className="w-16 h-16 bg-gradient-to-r from-indigo-500 to-purple-600 rounded-2xl flex items-center justify-center shadow-lg">
                <Upload className="w-8 h-8 text-white" />
              </div>
              {isDragActive && (
                <div className="absolute -top-1 -right-1">
                  <Sparkles className="w-6 h-6 text-yellow-500 animate-pulse" />
                </div>
              )}
            </div>
            <div className="space-y-2">
              <h3 className="text-2xl font-bold text-slate-800">
                {isDragActive ? 'Drop files here!' : 'Upload your media files'}
              </h3>
              <p className="text-slate-600 font-medium">
                Drag and drop videos, images, or audio files, or click to browse
              </p>
              <div className="flex items-center justify-center space-x-2 text-sm text-slate-500">
                <FileVideo className="w-4 h-4" />
                <FileImage className="w-4 h-4" />
                <FileAudio className="w-4 h-4" />
                <span>MP4, AVI, MOV, MP3, WAV, JPEG, PNG, and more</span>
              </div>
            </div>
          </div>
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
                  <p className="font-semibold text-slate-800">{result.file}</p>
                  <p className={`text-sm ${result.success ? 'text-green-600' : 'text-red-600'}`}>
                    {result.message}
                  </p>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Always-visible debug download section */}
      <div className="bg-yellow-50 border border-yellow-200 rounded-xl p-6 mt-6">
        <h4 className="font-bold text-yellow-800 mb-2">Debug Download Section</h4>
        <p className="text-sm text-yellow-700 mb-2">downloadUrl: <span className="font-mono">{String(downloadUrl)}</span></p>
        {downloadUrl && (
          <a href={downloadUrl} target="_blank" rel="noopener noreferrer" className="px-6 py-3 bg-green-600 text-white rounded-xl font-semibold shadow-lg hover:bg-green-700 transition">
            Download Processed File
          </a>
        )}
      </div>

      {/* Upload Settings */}
      <div className="bg-white/70 backdrop-blur-sm rounded-2xl shadow-xl border border-slate-200/50 p-8">
        <div className="flex items-center space-x-3 mb-8">
          <div className="w-8 h-8 bg-gradient-to-r from-blue-500 to-cyan-600 rounded-lg flex items-center justify-center">
            <Settings className="w-5 h-5 text-white" />
          </div>
          <h3 className="text-xl font-bold text-slate-800">Processing Settings</h3>
        </div>
        
        <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
          <div className="space-y-3">
            <label className="block text-sm font-semibold text-slate-700">
              Output Quality
            </label>
            <select
              value={uploadSettings.quality}
              onChange={(e) => setUploadSettings(prev => ({ ...prev, quality: e.target.value }))}
              className="w-full px-4 py-3 bg-white border border-slate-300 rounded-xl focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 transition-colors font-medium"
              disabled={uploading}
            >
              <option value="low">🚀 Low (Faster processing)</option>
              <option value="medium">⚖️ Medium (Balanced)</option>
              <option value="high">💎 High (Best quality)</option>
            </select>
          </div>

          <div className="space-y-3">
            <label className="block text-sm font-semibold text-slate-700">
              Output Format
            </label>
            <select
              value={uploadSettings.format}
              onChange={(e) => setUploadSettings(prev => ({ ...prev, format: e.target.value }))}
              className="w-full px-4 py-3 bg-white border border-slate-300 rounded-xl focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 transition-colors font-medium"
              disabled={uploading}
            >
              <option value="auto">🎯 Auto (Recommended)</option>
              <option value="mp4">🎬 MP4 (Video)</option>
              <option value="webm">🌐 WebM (Video)</option>
              <option value="mp3">🎵 MP3 (Audio)</option>
              <option value="webp">🖼️ WebP (Image)</option>
            </select>
          </div>
        </div>

        <div className="mt-8 grid grid-cols-1 md:grid-cols-2 gap-6">
          {[
            { key: 'addSubtitles', label: 'Add auto-generated subtitles', description: 'Generate subtitles for video content', icon: '💬' },
            { key: 'extractMetadata', label: 'Extract metadata', description: 'Extract file information and properties', icon: '📊' },
            { key: 'optimizeForWeb', label: 'Optimize for web', description: 'Compress and optimize for web delivery', icon: '🌐' },
            { key: 'optimizeForMobile', label: 'Optimize for mobile', description: 'Additional optimization for mobile devices', icon: '📱' },
          ].map(({ key, label, description, icon }) => (
            <div key={key} className="flex items-start space-x-4 p-4 bg-slate-50/50 rounded-xl border border-slate-200/50 hover:bg-slate-50 transition-colors">
              <div className="flex items-center space-x-3 flex-1">
                <span className="text-2xl">{icon}</span>
                <div className="flex-1">
                  <label className="flex items-center space-x-3 cursor-pointer">
                    <input
                      type="checkbox"
                      checked={uploadSettings[key as keyof ProcessingSettings] as boolean}
                      onChange={(e) => setUploadSettings(prev => ({ ...prev, [key]: e.target.checked }))}
                      className="w-5 h-5 text-indigo-600 bg-white border-slate-300 rounded focus:ring-indigo-500 focus:ring-2"
                      disabled={uploading}
                    />
                    <div>
                      <span className="font-semibold text-slate-800">{label}</span>
                      <p className="text-sm text-slate-600">{description}</p>
                    </div>
                  </label>
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Selected Files */}
      {files.length > 0 && (
        <div className="bg-white/70 backdrop-blur-sm rounded-2xl shadow-xl border border-slate-200/50 p-8">
          <h3 className="text-xl font-bold text-slate-800 mb-6">
            Selected Files ({files.length})
          </h3>
          <div className="space-y-4">
            {files.map((file, index) => (
              <div key={index} className="flex items-center justify-between p-4 bg-slate-50/50 rounded-xl border border-slate-200/50 hover:bg-slate-50 transition-colors">
                <div className="flex items-center space-x-4">
                  <div className="w-12 h-12 bg-slate-100 rounded-xl flex items-center justify-center">
                    {getFileIcon(file)}
                  </div>
                  <div>
                    <p className="font-semibold text-slate-800">{file.name}</p>
                    <p className="text-sm text-slate-600">
                      {formatFileSize(file.size)} • {file.type}
                    </p>
                  </div>
                </div>
                <button
                  onClick={() => removeFile(index)}
                  className="w-8 h-8 bg-red-100 hover:bg-red-200 text-red-600 rounded-lg transition-colors flex items-center justify-center"
                  disabled={uploading}
                >
                  <X className="w-4 h-4" />
                </button>
              </div>
            ))}
          </div>
          
          <div className="mt-8 flex justify-end">
            <button
              onClick={handleUpload}
              disabled={uploading}
              className={`px-8 py-4 rounded-xl font-semibold text-white shadow-lg transition-all duration-200 transform ${
                uploading
                  ? 'bg-slate-400 cursor-not-allowed'
                  : 'bg-gradient-to-r from-indigo-600 to-purple-600 hover:from-indigo-700 hover:to-purple-700 hover:scale-105 hover:shadow-xl'
              }`}
            >
              {uploading ? (
                <div className="flex items-center space-x-2">
                  <div className="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin"></div>
                  <span>Processing...</span>
                </div>
              ) : (
                `🚀 Start Processing (${files.length} file${files.length !== 1 ? 's' : ''})`
              )}
            </button>
          </div>
        </div>
      )}
    </div>
  )
}

export default FileUpload 