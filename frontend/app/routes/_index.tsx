import { SignedIn, SignedOut, UserButton } from '@clerk/remix'
import { Link } from '@remix-run/react'
import { useApi } from '~/utils/api'
import { useEffect, useState } from 'react'

export default function Index() {
  const apiFetch = useApi()
  const [user, setUser] = useState<any>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    const fetchUser = async () => {
      try {
        const userData = await apiFetch('/api/users/me')
        setUser(userData)
      } catch (err) {
        if (err instanceof Error) {
          setError(err.message)
        } else {
          setError('An unknown error occurred')
        }
      }
    }

    fetchUser()
  }, [apiFetch])

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