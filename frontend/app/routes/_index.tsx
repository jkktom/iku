import { UserButton } from '@clerk/remix'
import { getAuth } from '@clerk/remix/ssr.server'
import { LoaderFunction, redirect } from '@remix-run/node'

export const loader: LoaderFunction = async (args) => {
  const { userId } = await getAuth(args)
  if (!userId) {
    return redirect('/sign-in')
  }
  return {}
}

export default function Index() {
  return (
    <div>
      <h1>Index route</h1>
      <p>You are signed in!</p>
      <UserButton />
    </div>
  )
}