import React, { useCallback, useState } from 'react'
import { useDropzone } from 'react-dropzone'
import { FileVideo, X, Settings, CheckCircle, AlertCircle, Sparkles } from 'lucide-react'
import { mediaAPI } from '../services/api'
import type { ProcessingSettings } from '../services/api'

interface FileWithPreview extends File {
  preview?: string
}

const VideoUpload: React.FC = () => {
  const [files, setFiles] = useState<FileWithPreview[]>([])
  const [uploading, setUploading] = useState(false)
  const [uploadResults, setUploadResults] = useState<Array<{ file: string; success: boolean; message: string }>>([])
  const [uploadSettings, setUploadSettings] = useState<ProcessingSettings>({
    quality: 'high',
    format: 'mp4',
    addSubtitles: false,
    extractMetadata: true,
    optimizeForWeb: true,
    optimizeForMobile: false,
  })
  const [downloadUrl, setDownloadUrl] = useState<string | null>(null)

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
      'video/*': ['.mp4', '.avi', '.mov', '.wmv', '.flv', '.webm'],
    },
    multiple: true,
  })

  const removeFile = (index: number) => {
    setFiles(prev => prev.filter((_, i) => i !== index))
  }

  const formatFileSize = (bytes: number) => {
    if (bytes === 0) return '0 Bytes'
    const k = 1024
    const sizes = ['Bytes', 'KB', 'MB', 'GB']
    const i = Math.floor(Math.log(bytes) / Math.log(k))
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
  }

  const handleUpload = async () => {
    if (files.length === 0) return
    setUploading(true)
    const results: Array<{ file: string; success: boolean; message: string }> = []
    try {
      for (const file of files) {
        try {
          const response = await mediaAPI.uploadFile(file, uploadSettings)
          results.push({
            file: file.name,
            success: true,
            message: response.message || 'Upload successful'
          })
        } catch (error) {
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
      // eslint-disable-next-line no-console
      console.error('Upload error:', error)
    } finally {
      setUploading(false)
    }
  }

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
                <FileVideo className="w-8 h-8 text-white" />
              </div>
              {isDragActive && (
                <div className="absolute -top-1 -right-1">
                  <Sparkles className="w-6 h-6 text-yellow-500 animate-pulse" />
                </div>
              )}
            </div>
            <div className="space-y-2">
              <h3 className="text-2xl font-bold text-slate-800">
                {isDragActive ? 'Drop videos here!' : 'Upload your video files'}
              </h3>
              <p className="text-slate-600 font-medium">
                Drag and drop videos, or click to browse
              </p>
              <div className="flex items-center justify-center space-x-2 text-sm text-slate-500">
                <FileVideo className="w-4 h-4" />
                <span>MP4, AVI, MOV, WMV, FLV, WEBM</span>
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
                  <p className={`text-sm ${result.success ? 'text-green-600' : 'text-red-600'}`}>{result.message}</p>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Upload Settings */}
      <div className="bg-white/70 backdrop-blur-sm rounded-2xl shadow-xl border border-slate-200/50 p-8">
        <div className="flex items-center space-x-3 mb-8">
          <div className="w-8 h-8 bg-gradient-to-r from-blue-500 to-cyan-600 rounded-lg flex items-center justify-center">
            <Settings className="w-5 h-5 text-white" />
          </div>
          <h3 className="text-xl font-bold text-slate-800">Video Processing Settings</h3>
        </div>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
          <div className="space-y-3">
            <label className="block text-sm font-semibold text-slate-700">Output Quality</label>
            <select
              value={uploadSettings.quality}
              onChange={e => setUploadSettings(prev => ({ ...prev, quality: e.target.value }))}
              className="w-full px-4 py-3 bg-white border border-slate-300 rounded-xl focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 transition-colors font-medium"
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
              className="w-full px-4 py-3 bg-white border border-slate-300 rounded-xl focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 transition-colors font-medium"
              disabled={uploading}
            >
              <option value="mp4">MP4</option>
              <option value="webm">WEBM</option>
              <option value="mov">MOV</option>
            </select>
          </div>
        </div>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-8 mt-8">
          <div className="flex items-center space-x-2">
            <input
              type="checkbox"
              checked={uploadSettings.addSubtitles}
              onChange={e => setUploadSettings(prev => ({ ...prev, addSubtitles: e.target.checked }))}
              disabled={uploading}
              className="w-5 h-5 text-indigo-600 border-gray-300 rounded focus:ring-indigo-500"
            />
            <span className="text-sm text-slate-700">Add Subtitles (future)</span>
          </div>
          <div className="flex items-center space-x-2">
            <input
              type="checkbox"
              checked={uploadSettings.optimizeForWeb}
              onChange={e => setUploadSettings(prev => ({ ...prev, optimizeForWeb: e.target.checked }))}
              disabled={uploading}
              className="w-5 h-5 text-indigo-600 border-gray-300 rounded focus:ring-indigo-500"
            />
            <span className="text-sm text-slate-700">Optimize for Web</span>
          </div>
        </div>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-8 mt-8">
          <div className="flex items-center space-x-2">
            <input
              type="checkbox"
              checked={uploadSettings.optimizeForMobile}
              onChange={e => setUploadSettings(prev => ({ ...prev, optimizeForMobile: e.target.checked }))}
              disabled={uploading}
              className="w-5 h-5 text-indigo-600 border-gray-300 rounded focus:ring-indigo-500"
            />
            <span className="text-sm text-slate-700">Optimize for Mobile</span>
          </div>
        </div>
        <button
          onClick={handleUpload}
          className="mt-8 w-full py-3 px-6 bg-gradient-to-r from-indigo-500 to-blue-600 text-white font-bold rounded-xl shadow-lg hover:from-indigo-600 hover:to-blue-700 transition-all duration-200 disabled:opacity-50"
          disabled={uploading || files.length === 0}
        >
          {uploading ? 'Uploading...' : 'Start Video Processing'}
        </button>
      </div>

      {/* File List */}
      {files.length > 0 && (
        <div className="bg-white/70 backdrop-blur-sm rounded-2xl shadow-xl border border-slate-200/50 p-8">
          <div className="flex items-center space-x-3 mb-6">
            <div className="w-8 h-8 bg-gradient-to-r from-indigo-500 to-purple-600 rounded-lg flex items-center justify-center">
              <FileVideo className="w-5 h-5 text-white" />
            </div>
            <h3 className="text-xl font-bold text-slate-800">Files to Upload</h3>
          </div>
          <ul className="space-y-3">
            {files.map((file, index) => (
              <li key={index} className="flex items-center space-x-4 p-4 bg-slate-50/50 rounded-xl border border-slate-200/50">
                <FileVideo className="w-8 h-8 text-blue-500" />
                <div className="flex-1">
                  <p className="font-semibold text-slate-800">{file.name}</p>
                  <p className="text-sm text-slate-500">{formatFileSize(file.size)}</p>
                </div>
                <button
                  onClick={() => removeFile(index)}
                  className="ml-2 p-2 rounded-full hover:bg-red-100 transition-colors"
                  disabled={uploading}
                >
                  <X className="w-5 h-5 text-red-500" />
                </button>
              </li>
            ))}
          </ul>
        </div>
      )}

      {downloadUrl && (
        <div>
          <p>Debug: downloadUrl = {String(downloadUrl)}</p>
          <button onClick={() => alert(downloadUrl)}>Show Download URL</button>
        </div>
      )}
    </div>
  )
}

export default VideoUpload 