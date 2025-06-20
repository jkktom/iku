import { SignedIn, SignedOut, UserButton, useAuth } from '@clerk/remix'
import { Link } from '@remix-run/react'
import { useApi } from '~/utils/api'
import { useEffect, useState } from 'react'

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
    <div>
      <h1>Index route</h1>
      <SignedOut>
        <p>Welcome! Please sign in or sign up.</p>
        <Link to="/sign-in">Sign In</Link>
        <br />
        <Link to="/sign-up">Sign Up</Link>
      </SignedOut>
      <SignedIn>
        <p>You are signed in!</p>
        {user && <p>Welcome, {user.email}</p>}
        {error && <p style={{ color: 'red' }}>Error: {error}</p>}
        <UserButton />
      </SignedIn>
    </div>
  )
}