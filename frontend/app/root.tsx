import type { MetaFunction, LinksFunction } from '@remix-run/node'
import { Links, Meta, Outlet, Scripts, ScrollRestoration } from '@remix-run/react'

// ✅ Tailwind CSS import
import tailwindStylesheet from './tailwind.css?url'

export const meta: MetaFunction = () => [
  {
    charset: 'utf-8',
    title: 'New Remix App',
    viewport: 'width=device-width,initial-scale=1',
  },
]

export const links: LinksFunction = () => [
  {
    rel: "icon",
    href: "/favicon.ico",
    type: "image/x-icon",
  },
  // ✅ Tailwind CSS 로드
  { rel: "stylesheet", href: tailwindStylesheet },
]

export function Layout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en">
      <head>
        <Meta />
        <Links />
      </head>
      <body>
        {children}
        <ScrollRestoration />
        <Scripts />
      </body>
    </html>
  )
}

function App() {
  return <Outlet />
}

export default App