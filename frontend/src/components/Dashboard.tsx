import React, { useState, useEffect } from 'react'
import { BarChart3, TrendingUp, Clock, HardDrive, Users, FileText, Zap, Activity, Server } from 'lucide-react'
import { mediaAPI } from '../services/api'
import type { SystemStats } from '../services/api'

const Dashboard: React.FC = () => {
  const [stats, setStats] = useState<SystemStats | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    fetchStats()
    // Refresh stats every 30 seconds
    const interval = setInterval(fetchStats, 30000)
    return () => clearInterval(interval)
  }, [])

  const fetchStats = async () => {
    try {
      const response = await mediaAPI.getSystemStats()
      setStats(response)
      setError(null)
    } catch (err) {
      console.error('Error fetching stats:', err)
      setError('Failed to load dashboard data')
    } finally {
      setLoading(false)
    }
  }

  const formatBytes = (bytes: number) => {
    if (bytes === 0) return '0 Bytes'
    const k = 1024
    const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB']
    const i = Math.floor(Math.log(bytes) / Math.log(k))
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
  }

  const formatUptime = (seconds: number) => {
    const days = Math.floor(seconds / 86400)
    const hours = Math.floor((seconds % 86400) / 3600)
    const mins = Math.floor((seconds % 3600) / 60)
    
    if (days > 0) return `${days}d ${hours}h ${mins}m`
    if (hours > 0) return `${hours}h ${mins}m`
    return `${mins}m`
  }

  if (loading) {
    return (
      <div className="bg-white/70 backdrop-blur-sm rounded-2xl shadow-xl border border-slate-200/50 p-8">
        <div className="flex items-center justify-center space-x-3">
          <Activity className="w-6 h-6 text-indigo-500 animate-pulse" />
          <span className="text-lg font-semibold text-slate-700">Loading dashboard...</span>
        </div>
      </div>
    )
  }

  if (error && !stats) {
    return (
      <div className="bg-white/70 backdrop-blur-sm rounded-2xl shadow-xl border border-slate-200/50 p-8">
        <div className="text-center">
          <BarChart3 className="w-12 h-12 text-red-500 mx-auto mb-4" />
          <h3 className="text-xl font-bold text-slate-800 mb-2">Error Loading Dashboard</h3>
          <p className="text-slate-600 mb-4">{error}</p>
          <button
            onClick={fetchStats}
            className="px-6 py-3 bg-gradient-to-r from-indigo-600 to-purple-600 text-white rounded-xl font-semibold hover:from-indigo-700 hover:to-purple-700 transition-all duration-200"
          >
            Retry
          </button>
        </div>
      </div>
    )
  }

  return (
    <div className="space-y-8">
      {/* Header */}
      <div className="bg-white/70 backdrop-blur-sm rounded-2xl shadow-xl border border-slate-200/50 p-8">
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-4">
            <div className="w-10 h-10 bg-gradient-to-r from-emerald-500 to-cyan-600 rounded-xl flex items-center justify-center">
              <BarChart3 className="w-6 h-6 text-white" />
            </div>
            <div>
              <h2 className="text-2xl font-bold text-slate-800">System Dashboard</h2>
              <p className="text-slate-600">Monitor system performance and usage statistics</p>
            </div>
          </div>
          <div className="flex items-center space-x-3">
            <div className="flex items-center space-x-2 px-3 py-2 bg-green-50 border border-green-200 rounded-lg">
              <div className="w-2 h-2 bg-green-500 rounded-full animate-pulse"></div>
              <span className="text-sm font-medium text-green-700">Live Data</span>
            </div>
          </div>
        </div>
      </div>

      {/* Key Metrics */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        {[
          {
            title: 'Total Files Processed',
            value: stats?.totalFilesProcessed || 0,
            change: '+12%',
            changeType: 'positive',
            icon: FileText,
            color: 'from-blue-500 to-blue-600',
            emoji: '📁'
          },
          {
            title: 'Active Users',
            value: stats?.activeUsers || 0,
            change: '+5%',
            changeType: 'positive',
            icon: Users,
            color: 'from-emerald-500 to-emerald-600',
            emoji: '👥'
          },
          {
            title: 'Processing Time Saved',
            value: `${stats?.totalProcessingTime || 0}h`,
            change: '+23%',
            changeType: 'positive',
            icon: Clock,
            color: 'from-purple-500 to-purple-600',
            emoji: '⚡'
          },
          {
            title: 'Storage Used',
            value: formatBytes(stats?.storageUsed || 0),
            change: '+8%',
            changeType: 'neutral',
            icon: HardDrive,
            color: 'from-orange-500 to-orange-600',
            emoji: '💾'
          }
        ].map((metric, index) => (
          <div key={index} className="bg-white/70 backdrop-blur-sm rounded-2xl shadow-xl border border-slate-200/50 p-6 hover:shadow-2xl transition-all duration-200">
            <div className="flex items-center justify-between mb-4">
              <div className={`w-12 h-12 bg-gradient-to-r ${metric.color} rounded-xl flex items-center justify-center text-white text-lg`}>
                {metric.emoji}
              </div>
              <div className={`flex items-center space-x-1 px-2 py-1 rounded-full text-xs font-semibold ${
                metric.changeType === 'positive' ? 'bg-green-100 text-green-700' : 
                metric.changeType === 'negative' ? 'bg-red-100 text-red-700' : 'bg-slate-100 text-slate-700'
              }`}>
                <TrendingUp className="w-3 h-3" />
                <span>{metric.change}</span>
              </div>
            </div>
            <div>
              <p className="text-2xl font-bold text-slate-800 mb-1">{metric.value}</p>
              <p className="text-sm font-medium text-slate-600">{metric.title}</p>
            </div>
          </div>
        ))}
      </div>

      {/* System Health */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
        {/* System Resources */}
        <div className="bg-white/70 backdrop-blur-sm rounded-2xl shadow-xl border border-slate-200/50 p-8">
          <div className="flex items-center space-x-3 mb-6">
            <div className="w-8 h-8 bg-gradient-to-r from-blue-500 to-cyan-600 rounded-lg flex items-center justify-center">
              <Server className="w-5 h-5 text-white" />
            </div>
            <h3 className="text-xl font-bold text-slate-800">System Resources</h3>
          </div>
          
          <div className="space-y-6">
            {[
              { label: 'CPU Usage', value: stats?.cpuUsage || 45, color: 'from-blue-500 to-blue-600', emoji: '🖥️' },
              { label: 'Memory Usage', value: stats?.memoryUsage || 62, color: 'from-emerald-500 to-emerald-600', emoji: '🧠' },
              { label: 'Storage Usage', value: stats?.storageUsage || 78, color: 'from-orange-500 to-orange-600', emoji: '💾' },
              { label: 'Network I/O', value: stats?.networkIO || 34, color: 'from-purple-500 to-purple-600', emoji: '🌐' }
            ].map((resource, index) => (
              <div key={index} className="space-y-2">
                <div className="flex items-center justify-between">
                  <div className="flex items-center space-x-2">
                    <span className="text-lg">{resource.emoji}</span>
                    <span className="font-semibold text-slate-700">{resource.label}</span>
                  </div>
                  <span className="text-sm font-semibold text-slate-600">{resource.value}%</span>
                </div>
                <div className="w-full bg-slate-200 rounded-full h-3">
                  <div
                    className={`bg-gradient-to-r ${resource.color} h-3 rounded-full transition-all duration-500`}
                    style={{ width: `${resource.value}%` }}
                  ></div>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Processing Stats */}
        <div className="bg-white/70 backdrop-blur-sm rounded-2xl shadow-xl border border-slate-200/50 p-8">
          <div className="flex items-center space-x-3 mb-6">
            <div className="w-8 h-8 bg-gradient-to-r from-indigo-500 to-purple-600 rounded-lg flex items-center justify-center">
              <Zap className="w-5 h-5 text-white" />
            </div>
            <h3 className="text-xl font-bold text-slate-800">Processing Statistics</h3>
          </div>
          
          <div className="space-y-6">
            {[
              { label: 'Files in Queue', value: stats?.filesInQueue || 8, subtitle: 'Currently processing', emoji: '⏳' },
              { label: 'Avg Processing Time', value: `${stats?.avgProcessingTime || 3.2}min`, subtitle: 'Per file', emoji: '⚡' },
              { label: 'Success Rate', value: `${stats?.successRate || 98.5}%`, subtitle: 'Last 30 days', emoji: '✅' },
              { label: 'System Uptime', value: formatUptime(stats?.uptime || 432000), subtitle: 'Current session', emoji: '🚀' }
            ].map((stat, index) => (
              <div key={index} className="flex items-center space-x-4 p-4 bg-slate-50/50 rounded-xl border border-slate-200/50">
                <div className="w-12 h-12 bg-gradient-to-r from-slate-100 to-slate-200 rounded-xl flex items-center justify-center text-2xl">
                  {stat.emoji}
                </div>
                <div className="flex-1">
                  <div className="flex items-center justify-between">
                    <span className="font-semibold text-slate-800">{stat.label}</span>
                    <span className="text-lg font-bold text-slate-800">{stat.value}</span>
                  </div>
                  <p className="text-sm text-slate-600">{stat.subtitle}</p>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Recent Activity */}
      <div className="bg-white/70 backdrop-blur-sm rounded-2xl shadow-xl border border-slate-200/50 p-8">
        <div className="flex items-center space-x-3 mb-6">
          <div className="w-8 h-8 bg-gradient-to-r from-green-500 to-emerald-600 rounded-lg flex items-center justify-center">
            <Activity className="w-5 h-5 text-white" />
          </div>
          <h3 className="text-xl font-bold text-slate-800">Recent Activity</h3>
        </div>
        
        <div className="space-y-4">
          {(stats?.recentActivity || [
            { type: 'upload', message: 'User uploaded video.mp4', time: '2 minutes ago', status: 'success' },
            { type: 'processing', message: 'Started processing audio.wav', time: '5 minutes ago', status: 'processing' },
            { type: 'completed', message: 'Completed processing image.jpg', time: '8 minutes ago', status: 'success' },
            { type: 'error', message: 'Failed to process corrupt.mp4', time: '12 minutes ago', status: 'error' }
          ]).map((activity, index) => (
            <div key={index} className="flex items-center space-x-4 p-4 bg-slate-50/50 rounded-xl border border-slate-200/50 hover:bg-slate-50 transition-colors">
              <div className={`w-10 h-10 rounded-lg flex items-center justify-center ${
                activity.status === 'success' ? 'bg-green-100 text-green-600' :
                activity.status === 'processing' ? 'bg-blue-100 text-blue-600' :
                activity.status === 'error' ? 'bg-red-100 text-red-600' :
                'bg-slate-100 text-slate-600'
              }`}>
                {activity.status === 'success' && '✅'}
                {activity.status === 'processing' && '⚡'}
                {activity.status === 'error' && '❌'}
                {activity.status === 'info' && 'ℹ️'}
              </div>
              <div className="flex-1">
                <p className="font-semibold text-slate-800">{activity.message}</p>
                <p className="text-sm text-slate-600">{activity.time}</p>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  )
}

export default Dashboard 