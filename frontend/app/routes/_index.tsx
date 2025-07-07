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
    <div className="h-full bg-gray-50">
      <div className="p-6">
        <div className="mb-6">
          <h1 className="text-2xl font-bold text-gray-900">공지사항</h1>
          <p className="text-gray-600 mt-2">최신 공지사항을 확인하세요</p>
          <SignedIn>
            {user && (
              <div className="mt-2 text-sm text-gray-500">
                환영합니다, {user.email}
              </div>
            )}
            {error && (
              <div className="mt-2 text-sm text-red-600">
                오류: {error}
              </div>
            )}
          </SignedIn>
          <SignedOut>
            <div className="mt-2 text-sm text-blue-600">
              베타 서비스 - 로그인 없이도 이용 가능합니다
            </div>
          </SignedOut>
        </div>
        <AnnouncementBoard />
      </div>
    </div>
  )
}
