import axios from 'axios'

const API_BASE_URL = 'http://localhost:8080/api'

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
})

export interface JobResponse {
  id: string
  filename: string
  originalFilename?: string
  mimeType?: string
  status: string
  progress: number
  fileSize: number
  processedSize?: number
  compressionRatio?: number
  processingTime?: number
  errorMessage?: string
  message?: string
  createdAt?: string
  updatedAt?: string
  startedAt?: string
  completedAt?: string
  downloadUrl?: string
}

export interface ProcessingJob {
  id: string
  fileName: string
  status: 'queued' | 'processing' | 'completed' | 'failed'
  progress?: number
  fileSize: number
  duration?: number
  createdAt: string
  completedAt?: string
}

export interface ProcessingJobsResponse {
  jobs: ProcessingJob[]
  total: number
  page: number
  size: number
}

export interface SystemStats {
  totalFilesProcessed: number
  activeUsers: number
  totalProcessingTime: number
  storageUsed: number
  cpuUsage: number
  memoryUsage: number
  storageUsage: number
  networkIO: number
  filesInQueue: number
  avgProcessingTime: number
  successRate: number
  uptime: number
  recentActivity?: Array<{
    type: string
    message: string
    time: string
    status: 'success' | 'processing' | 'error' | 'info'
  }>
}

export interface ProcessingSettings {
  quality: string
  format: string
  addSubtitles: boolean
  extractMetadata: boolean
  optimizeForWeb: boolean
  optimizeForMobile: boolean
}

export interface UploadResponse {
  id: string
  filename: string
  status: string
  progress: number
  fileSize: number
  message: string
}

export interface StatsResponse {
  totalJobs: number
  queuedJobs: number
  processingJobs: number
  completedJobs: number
  failedJobs: number
  totalDataProcessed: string
  averageCompressionRatio: number
}

class MediaAPI {
  async uploadFile(file: File, settings: ProcessingSettings): Promise<UploadResponse> {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('settings', JSON.stringify(settings))

    const response = await api.post('/media/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    })

    return response.data
  }

  async getJobs(status: string = 'all', page: number = 0, size: number = 10): Promise<JobResponse[]> {
    const response = await api.get('/media/jobs', {
      params: { status, page, size },
    })
    return response.data
  }

  async getProcessingJobs(): Promise<ProcessingJobsResponse> {
    const response = await api.get('/media/processing-jobs')
    return response.data
  }

  async deleteProcessingJob(jobId: string): Promise<{ message: string }> {
    const response = await api.delete(`/media/processing-jobs/${jobId}`)
    return response.data
  }

  async getSystemStats(): Promise<SystemStats> {
    const response = await api.get('/media/system-stats')
    return response.data
  }

  async getJob(jobId: string): Promise<JobResponse> {
    const response = await api.get(`/media/jobs/${jobId}`)
    return response.data
  }

  async cancelJob(jobId: string): Promise<{ message: string; jobId: string }> {
    const response = await api.delete(`/media/jobs/${jobId}`)
    return response.data
  }

  async getStats(): Promise<StatsResponse> {
    const response = await api.get('/media/stats')
    return response.data
  }

  async healthCheck(): Promise<{ status: string; service: string; timestamp: string }> {
    const response = await api.get('/media/health')
    return response.data
  }
}

export const mediaAPI = new MediaAPI()
export default mediaAPI 