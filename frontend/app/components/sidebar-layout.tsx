import { SignedIn, SignedOut, UserButton, useAuth } from '@clerk/remix'
import { Link, useLocation } from '@remix-run/react'
import { useState } from 'react'
import { 
  Home, 
  Megaphone, 
  Sparkles, 
  Menu,
  X,
  Plus,
  History,
  ChevronDown,
  ChevronRight,
  FileText,
  BarChart3
} from 'lucide-react'

interface SidebarLayoutProps {
  children: React.ReactNode
}

export function SidebarLayout({ children }: SidebarLayoutProps) {
  const [sidebarOpen, setSidebarOpen] = useState(false)
  const [manualHistoryExpanded, setManualHistoryExpanded] = useState(false)
  const location = useLocation()
  const { isSignedIn } = useAuth()
  
  // Auto-expand history section if user is on history pages or manually expanded
  const historyExpanded = location.pathname.includes('history') || manualHistoryExpanded

  const navigation = [
    { name: '공지사항', href: '/', icon: Megaphone },
    { name: '새로운 분석 요청하기', href: '/ai-analysis', icon: Sparkles },
  ]
  
  const historyNavigation = [
    { name: '단일 경기 분석 결과', href: '/single-match-history', icon: FileText },
    { name: '다수 경기 분석 결과', href: '/multi-match-history', icon: BarChart3 },
  ]

  const isActive = (href: string) => {
    if (href === '/') {
      return location.pathname === '/' || location.pathname.startsWith('/announcement')
    }
    return location.pathname === href || location.pathname.startsWith(href + '/')
  }

  return (
    <div className="flex h-screen bg-gray-50">
      {/* Mobile sidebar overlay */}
      {sidebarOpen && (
        <div 
          className="fixed inset-0 z-40 bg-black bg-opacity-50 lg:hidden"
          onClick={() => setSidebarOpen(false)}
        />
      )}

      {/* Sidebar - Always open on large screens, hamburger menu on mobile */}
      <div className={`
        fixed inset-y-0 left-0 z-50 w-64 bg-white shadow-lg transform transition-transform duration-300 ease-in-out
        lg:relative lg:translate-x-0 lg:z-auto
        ${sidebarOpen ? 'translate-x-0' : '-translate-x-full lg:translate-x-0'}
      `}>
        <div className="flex flex-col h-full">
          {/* Logo/Brand */}
          <div className="flex items-center justify-between h-16 px-4 border-b border-gray-200">
            <h1 className="text-xl font-bold text-gray-900">IKU</h1>
            <button
              onClick={() => setSidebarOpen(false)}
              className="lg:hidden p-2 rounded-md hover:bg-gray-100"
            >
              <X className="h-5 w-5" />
            </button>
          </div>

          {/* Navigation */}
          <nav className="flex-1 px-4 py-6 space-y-2">
            {navigation.map((item) => {
              const Icon = item.icon
              return (
                <Link
                  key={item.name}
                  to={item.href}
                  onClick={() => setSidebarOpen(false)}
                  className={`
                    flex items-center px-3 py-2 rounded-md text-sm font-medium transition-colors
                    ${isActive(item.href) 
                      ? 'bg-blue-50 text-blue-700 border-r-2 border-blue-700' 
                      : 'text-gray-700 hover:bg-gray-50 hover:text-gray-900'
                    }
                  `}
                >
                  <Icon className="h-5 w-5 mr-3 flex-shrink-0" />
                  {item.name}
                </Link>
              )
            })}
            
            {/* 분석 기록 섹션 - 확장 가능한 메뉴 */}
            <div className="space-y-1">
              <button
                onClick={() => setManualHistoryExpanded(!manualHistoryExpanded)}
                className={`w-full flex items-center px-3 py-2 rounded-md text-sm font-medium transition-colors ${
                  historyExpanded ? 'bg-blue-50 text-blue-700' : 'text-gray-700 hover:bg-gray-50 hover:text-gray-900'
                }`}
              >
                <History className="h-5 w-5 mr-3 flex-shrink-0" />
                <span className="flex-1 text-left">분석 기록</span>
                {historyExpanded ? (
                  <ChevronDown className="h-4 w-4" />
                ) : (
                  <ChevronRight className="h-4 w-4" />
                )}
              </button>
              
              {historyExpanded && (
                <div className="ml-6 space-y-1">
                  {historyNavigation.map((item) => {
                    const Icon = item.icon
                    return (
                      <Link
                        key={item.name}
                        to={item.href}
                        onClick={() => setSidebarOpen(false)}
                        className={`
                          flex items-center px-3 py-2 rounded-md text-sm font-medium transition-colors
                          ${isActive(item.href) 
                            ? 'bg-blue-50 text-blue-700 border-r-2 border-blue-700' 
                            : 'text-gray-700 hover:bg-gray-50 hover:text-gray-900'
                          }
                        `}
                      >
                        <Icon className="h-4 w-4 mr-3 flex-shrink-0" />
                        {item.name}
                      </Link>
                    )
                  })}
                </div>
              )}
            </div>
          </nav>
        </div>
      </div>

      {/* Main content */}
      <div className="flex-1 flex flex-col overflow-hidden">
        {/* Header with hamburger menu and auth */}
        <header className="bg-white shadow-sm border-b">
          <div className="px-4 py-3 flex items-center justify-between">
            <button
              onClick={() => setSidebarOpen(true)}
              className="lg:hidden p-2 rounded-md hover:bg-gray-100"
            >
              <Menu className="h-6 w-6" />
            </button>
            <h1 className="text-lg font-semibold text-gray-900 lg:ml-0">IKU</h1>
            
            {/* Auth buttons in top right */}
            <div className="flex items-center gap-3">
              <SignedOut>
                <Link 
                  to="/sign-in" 
                  className="text-gray-600 hover:text-gray-900 px-3 py-1 rounded-md text-sm font-medium"
                >
                  Sign In
                </Link>
                <Link 
                  to="/sign-up" 
                  className="bg-blue-600 hover:bg-blue-700 text-white px-3 py-1 rounded-md text-sm font-medium"
                >
                  Sign Up
                </Link>
              </SignedOut>
              <SignedIn>
                <UserButton />
              </SignedIn>
            </div>
          </div>
        </header>

        {/* Main content area */}
        <main className="flex-1 overflow-y-auto">
          <div className="h-full">
            {children}
          </div>
        </main>
      </div>
    </div>
  )
}