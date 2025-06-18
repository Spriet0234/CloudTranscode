import React, { useState, useEffect } from 'react'
import { Clock, CheckCircle, AlertCircle, FileVideo, FileImage, FileAudio, Eye, Download, Trash2, RefreshCw, Play } from 'lucide-react'
import { mediaAPI } from '../services/api'
import type { ProcessingJob } from '../services/api'

const ProcessingQueue: React.FC = () => {
  const [jobs, setJobs] = useState<ProcessingJob[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    fetchJobs()
    // Set up polling to refresh jobs every 5 seconds
    const interval = setInterval(fetchJobs, 5000)
    return () => clearInterval(interval)
  }, [])

  const fetchJobs = async () => {
    try {
      const response = await mediaAPI.getProcessingJobs()
      setJobs(response.jobs || [])
      setError(null)
    } catch (err) {
      console.error('Error fetching jobs:', err)
      setError('Failed to load processing jobs')
    } finally {
      setLoading(false)
    }
  }

  const handleDeleteJob = async (jobId: string) => {
    try {
      await mediaAPI.deleteProcessingJob(jobId)
      setJobs(prev => prev.filter(job => job.id !== jobId))
    } catch (err) {
      console.error('Error deleting job:', err)
    }
  }

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'completed':
        return <CheckCircle className="w-5 h-5 text-green-500" />
      case 'failed':
        return <AlertCircle className="w-5 h-5 text-red-500" />
      case 'processing':
        return <RefreshCw className="w-5 h-5 text-blue-500 animate-spin" />
      default:
        return <Clock className="w-5 h-5 text-yellow-500" />
    }
  }

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'completed':
        return 'bg-green-100 text-green-800 border-green-200'
      case 'failed':
        return 'bg-red-100 text-red-800 border-red-200'
      case 'processing':
        return 'bg-blue-100 text-blue-800 border-blue-200'
      default:
        return 'bg-yellow-100 text-yellow-800 border-yellow-200'
    }
  }

  const getFileIcon = (fileName: string) => {
    const ext = fileName.split('.').pop()?.toLowerCase()
    if (['mp4', 'avi', 'mov', 'wmv', 'flv', 'webm'].includes(ext || '')) {
      return <FileVideo className="w-6 h-6 text-blue-500" />
    }
    if (['mp3', 'wav', 'flac', 'aac', 'ogg'].includes(ext || '')) {
      return <FileAudio className="w-6 h-6 text-emerald-500" />
    }
    if (['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp'].includes(ext || '')) {
      return <FileImage className="w-6 h-6 text-purple-500" />
    }
    return <FileVideo className="w-6 h-6 text-slate-500" />
  }

  const formatDuration = (seconds: number) => {
    const mins = Math.floor(seconds / 60)
    const secs = Math.floor(seconds % 60)
    return `${mins}:${secs.toString().padStart(2, '0')}`
  }

  const formatFileSize = (bytes: number) => {
    if (bytes === 0) return '0 Bytes'
    const k = 1024
    const sizes = ['Bytes', 'KB', 'MB', 'GB']
    const i = Math.floor(Math.log(bytes) / Math.log(k))
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
  }

  if (loading) {
    return (
      <div className="bg-white/70 backdrop-blur-sm rounded-2xl shadow-xl border border-slate-200/50 p-8">
        <div className="flex items-center justify-center space-x-3">
          <RefreshCw className="w-6 h-6 text-indigo-500 animate-spin" />
          <span className="text-lg font-semibold text-slate-700">Loading processing queue...</span>
        </div>
      </div>
    )
  }

  if (error && jobs.length === 0) {
    return (
      <div className="bg-white/70 backdrop-blur-sm rounded-2xl shadow-xl border border-slate-200/50 p-8">
        <div className="text-center">
          <AlertCircle className="w-12 h-12 text-red-500 mx-auto mb-4" />
          <h3 className="text-xl font-bold text-slate-800 mb-2">Error Loading Jobs</h3>
          <p className="text-slate-600 mb-4">{error}</p>
          <button
            onClick={fetchJobs}
            className="px-6 py-3 bg-gradient-to-r from-indigo-600 to-purple-600 text-white rounded-xl font-semibold hover:from-indigo-700 hover:to-purple-700 transition-all duration-200"
          >
            Try Again
          </button>
        </div>
      </div>
    )
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="bg-white/70 backdrop-blur-sm rounded-2xl shadow-xl border border-slate-200/50 p-8">
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-4">
            <div className="w-10 h-10 bg-gradient-to-r from-blue-500 to-indigo-600 rounded-xl flex items-center justify-center">
              <Clock className="w-6 h-6 text-white" />
            </div>
            <div>
              <h2 className="text-2xl font-bold text-slate-800">Processing Queue</h2>
              <p className="text-slate-600">Monitor your media processing jobs</p>
            </div>
          </div>
          <button
            onClick={fetchJobs}
            className="px-4 py-2 bg-slate-100 hover:bg-slate-200 text-slate-700 rounded-lg font-medium transition-colors flex items-center space-x-2"
          >
            <RefreshCw className="w-4 h-4" />
            <span>Refresh</span>
          </button>
        </div>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
        {[
          { label: 'Total Jobs', value: jobs.length, color: 'from-slate-500 to-slate-600', icon: '📊' },
          { label: 'Processing', value: jobs.filter(j => j.status === 'processing').length, color: 'from-blue-500 to-blue-600', icon: '⚡' },
          { label: 'Completed', value: jobs.filter(j => j.status === 'completed').length, color: 'from-green-500 to-green-600', icon: '✅' },
          { label: 'Failed', value: jobs.filter(j => j.status === 'failed').length, color: 'from-red-500 to-red-600', icon: '❌' },
        ].map((stat, index) => (
          <div key={index} className="bg-white/70 backdrop-blur-sm rounded-2xl shadow-xl border border-slate-200/50 p-6">
            <div className="flex items-center space-x-4">
              <div className={`w-12 h-12 bg-gradient-to-r ${stat.color} rounded-xl flex items-center justify-center text-white text-lg`}>
                {stat.icon}
              </div>
              <div>
                <p className="text-sm font-medium text-slate-600">{stat.label}</p>
                <p className="text-2xl font-bold text-slate-800">{stat.value}</p>
              </div>
            </div>
          </div>
        ))}
      </div>

      {/* Jobs List */}
      {jobs.length === 0 ? (
        <div className="bg-white/70 backdrop-blur-sm rounded-2xl shadow-xl border border-slate-200/50 p-12 text-center">
          <div className="w-20 h-20 bg-slate-100 rounded-2xl flex items-center justify-center mx-auto mb-6">
            <Clock className="w-10 h-10 text-slate-400" />
          </div>
          <h3 className="text-xl font-bold text-slate-800 mb-2">No Processing Jobs</h3>
          <p className="text-slate-600">Upload some files to see processing jobs here</p>
        </div>
      ) : (
        <div className="space-y-4">
          {jobs.map((job) => (
            <div key={job.id} className="bg-white/70 backdrop-blur-sm rounded-2xl shadow-xl border border-slate-200/50 p-6 hover:shadow-2xl transition-all duration-200">
              <div className="flex items-center justify-between">
                <div className="flex items-center space-x-4 flex-1">
                  <div className="w-12 h-12 bg-slate-100 rounded-xl flex items-center justify-center">
                    {getFileIcon(job.fileName)}
                  </div>
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center space-x-3">
                      <h3 className="font-semibold text-slate-800 truncate">{job.fileName}</h3>
                      <span className={`px-3 py-1 rounded-full text-xs font-semibold border ${getStatusColor(job.status)}`}>
                        {job.status.charAt(0).toUpperCase() + job.status.slice(1)}
                      </span>
                    </div>
                    <div className="flex items-center space-x-4 mt-2 text-sm text-slate-600">
                      <span>Size: {formatFileSize(job.fileSize)}</span>
                      {job.duration && <span>Duration: {formatDuration(job.duration)}</span>}
                      <span>Started: {new Date(job.createdAt).toLocaleString()}</span>
                      {job.completedAt && <span>Completed: {new Date(job.completedAt).toLocaleString()}</span>}
                    </div>
                    {job.progress !== undefined && job.status === 'processing' && (
                      <div className="mt-3">
                        <div className="flex items-center justify-between mb-1">
                          <span className="text-sm font-medium text-slate-700">Progress</span>
                          <span className="text-sm text-slate-600">{job.progress}%</span>
                        </div>
                        <div className="w-full bg-slate-200 rounded-full h-2">
                          <div
                            className="bg-gradient-to-r from-blue-500 to-indigo-600 h-2 rounded-full transition-all duration-300"
                            style={{ width: `${job.progress}%` }}
                          ></div>
                        </div>
                      </div>
                    )}
                  </div>
                </div>
                
                <div className="flex items-center space-x-2">
                  {getStatusIcon(job.status)}
                  <div className="flex items-center space-x-2 ml-4">
                    {job.status === 'completed' && (
                      <>
                        <button
                          className="p-2 bg-blue-100 hover:bg-blue-200 text-blue-600 rounded-lg transition-colors"
                          title="Preview"
                        >
                          <Eye className="w-4 h-4" />
                        </button>
                        <button
                          className="p-2 bg-green-100 hover:bg-green-200 text-green-600 rounded-lg transition-colors"
                          title="Download"
                        >
                          <Download className="w-4 h-4" />
                        </button>
                      </>
                    )}
                    {job.status === 'failed' && (
                      <button
                        className="p-2 bg-yellow-100 hover:bg-yellow-200 text-yellow-600 rounded-lg transition-colors"
                        title="Retry"
                      >
                        <Play className="w-4 h-4" />
                      </button>
                    )}
                    <button
                      onClick={() => handleDeleteJob(job.id)}
                      className="p-2 bg-red-100 hover:bg-red-200 text-red-600 rounded-lg transition-colors"
                      title="Delete"
                    >
                      <Trash2 className="w-4 h-4" />
                    </button>
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}

export default ProcessingQueue 