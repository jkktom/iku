import { SignedIn, SignedOut, UserButton, useAuth } from '@clerk/remix'
import { Link } from '@remix-run/react'
import type { MetaFunction } from "@remix-run/node";
import { AnnouncementBoard } from "~/components/announcement-board";
import { useApi } from '~/utils/api'
import { useEffect, useState } from 'react'

export const meta: MetaFunction = () => {
  return [
    { title: "IKU 공지사항 시스템" },
    { name: "description", content: "공지사항 시스템" },
  ];
};

export default function Index() {
  const apiFetch = useApi()
  const { isSignedIn } = useAuth()
  const [user, setUser] = useState<any>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    // Only fetch user data if the user is actually signed in
    if (!isSignedIn) {
      setUser(null)
      setError(null)
      return
    }

    const fetchUser = async () => {
      try {
        const userData = await apiFetch('/api/users/me')
        setUser(userData)
        setError(null)
      } catch (err) {
        if (err instanceof Error) {
          setError(err.message)
        } else {
          setError('An unknown error occurred')
        }
      }
    }

    fetchUser()
  }, [apiFetch, isSignedIn])

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header with authentication */}
      <header className="bg-white shadow-sm border-b">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-16">
            <div className="flex items-center">
              <h1 className="text-xl font-semibold text-gray-900">IKU 공지사항 시스템</h1>
            </div>
            <div className="flex items-center gap-4">
              <SignedIn>
                <Link 
                  to="/ai-analysis" 
                  className="bg-purple-600 hover:bg-purple-700 text-white px-4 py-2 rounded-md text-sm font-medium"
                >
                  AI 분석
                </Link>
              </SignedIn>
              <SignedOut>
                <Link 
                  to="/sign-in" 
                  className="text-gray-600 hover:text-gray-900 px-3 py-2 rounded-md text-sm font-medium"
                >
                  Sign In
                </Link>
                <Link 
                  to="/sign-up" 
                  className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-md text-sm font-medium"
                >
                  Sign Up
                </Link>
              </SignedOut>
              <SignedIn>
                {user && (
                  <span className="text-sm text-gray-600">
                    Welcome, {user.email}
                  </span>
                )}
                {error && (
                  <span className="text-sm text-red-600">
                    Error: {error}
                  </span>
                )}
                <UserButton />
              </SignedIn>
            </div>
          </div>
        </div>
      </header>

      {/* Main content */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <SignedOut>
          <div className="text-center py-12">
            <h2 className="text-2xl font-bold text-gray-900 mb-4">
              공지사항을 확인하려면 로그인해 주세요
            </h2>
            <p className="text-gray-600 mb-8">
              IKU 공지사항 시스템에 오신 것을 환영합니다. 
              로그인하여 최신 공지사항을 확인하세요.
            </p>
            <div className="space-x-4">
              <Link 
                to="/sign-in" 
                className="bg-blue-600 hover:bg-blue-700 text-white px-6 py-3 rounded-md font-medium"
              >
                로그인
              </Link>
              <Link 
                to="/sign-up" 
                className="bg-gray-200 hover:bg-gray-300 text-gray-800 px-6 py-3 rounded-md font-medium"
              >
                회원가입
              </Link>
            </div>
          </div>
        </SignedOut>
        
        <SignedIn>
          <AnnouncementBoard />
        </SignedIn>
      </main>
    </div>
  )
}
