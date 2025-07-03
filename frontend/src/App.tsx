import { useState, useEffect } from 'react'
import { FileVideo, FileImage, BarChart3 } from 'lucide-react'
import VideoUpload from './components/VideoUpload'
import PictureUploader from './components/PictureUploader'
import ProcessingQueue from './components/ProcessingQueue'
import Dashboard from './components/Dashboard'
import './App.css'

function App() {
  // On mount, check localStorage for last tab, else default to 'pictures'
  const getInitialTab = () => {
    const savedTab = localStorage.getItem('activeTab')
    return savedTab === 'videos' || savedTab === 'pictures' || savedTab === 'queue' || savedTab === 'dashboard' ? savedTab : 'pictures'
  }
  const [activeTab, setActiveTab] = useState<'videos' | 'pictures' | 'queue' | 'dashboard'>(getInitialTab())

  // Save to localStorage whenever activeTab changes
  useEffect(() => {
    localStorage.setItem('activeTab', activeTab)
  }, [activeTab])

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 via-blue-50 to-indigo-100">
      {/* Header */}
      <header className="bg-white/80 backdrop-blur-sm shadow-lg border-b border-slate-200/50 sticky top-0 z-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center py-6">
            <div className="flex items-center space-x-4">
              <div className="flex items-center justify-center w-12 h-12 bg-gradient-to-r from-indigo-600 to-purple-600 rounded-xl shadow-lg">
                <FileVideo className="w-7 h-7 text-white" />
              </div>
              <div>
                <h1 className="text-3xl font-bold bg-gradient-to-r from-slate-800 to-slate-600 bg-clip-text text-transparent">
                  MediaTranscode
                </h1>
                <p className="text-sm text-slate-500 font-medium">Cloud-based media optimization</p>
              </div>
            </div>
            <div className="flex items-center space-x-3">
              <div className="flex items-center space-x-2 px-3 py-2 bg-green-50 border border-green-200 rounded-lg">
                <div className="w-2 h-2 bg-green-500 rounded-full animate-pulse"></div>
                {/* <span className="text-sm font-medium text-green-700">System Online</span> */}
              </div>
            </div>
          </div>
        </div>
      </header>

      {/* Navigation */}
      <nav className="bg-white/60 backdrop-blur-sm border-b border-slate-200/50 sticky top-[88px] z-40">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex space-x-8">
            {[{ id: 'pictures', label: 'Pictures', icon: FileImage, color: 'text-purple-600' },

              { id: 'videos', label: 'Videos', icon: FileVideo, color: 'text-blue-600' },
              
              // { id: 'queue', label: 'Processing Queue', icon: Clock, color: 'text-amber-600' },
              { id: 'dashboard', label: 'Dashboard', icon: BarChart3, color: 'text-emerald-600' },
            ].map(({ id, label, icon: Icon, color }) => (
              <button
                key={id}
                onClick={() => setActiveTab(id as any)}
                className={`flex items-center space-x-2 px-4 py-4 border-b-3 font-semibold text-sm transition-all duration-200 ${
                  activeTab === id
                    ? `border-indigo-500 ${color} bg-indigo-50/50`
                    : 'border-transparent text-slate-500 hover:text-slate-700 hover:border-slate-300 hover:bg-slate-50/50'
                }`}
              >
                <Icon className="w-5 h-5" />
                <span>{label}</span>
                {activeTab === id && (
                  <div className="absolute bottom-0 left-1/2 transform -translate-x-1/2 w-2 h-2 bg-indigo-500 rounded-full"></div>
                )}
              </button>
            ))}
          </div>
        </div>
      </nav>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="animate-fadeIn">
          
          {activeTab === 'pictures' && <PictureUploader />}
          {activeTab === 'videos' && <VideoUpload />}
          {activeTab === 'queue' && <ProcessingQueue />}
          {activeTab === 'dashboard' && <Dashboard />}
        </div>
      </main>

      {/* Background decorations */}
      <div className="fixed inset-0 overflow-hidden pointer-events-none">
        <div className="absolute top-1/4 left-1/4 w-64 h-64 bg-gradient-to-r from-blue-400/10 to-purple-400/10 rounded-full blur-3xl"></div>
        <div className="absolute bottom-1/4 right-1/4 w-80 h-80 bg-gradient-to-r from-indigo-400/10 to-cyan-400/10 rounded-full blur-3xl"></div>
      </div>
    </div>
  )
}

export default App
