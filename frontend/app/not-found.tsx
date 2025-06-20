import Link from "next/link"
import { Button } from "~/components/ui/button"
import { ArrowLeft } from "lucide-react"
import Image from "next/image"

export default function NotFound() {
  return (
    <div className="min-h-screen bg-slate-900 flex items-center justify-center">
      <div className="text-center">
        <Image src="/logo.png" alt="IKU Logo" width={80} height={80} className="mx-auto mb-6 object-contain" />
        <h1 className="text-4xl font-bold text-white mb-4">404</h1>
        <p className="text-slate-400 mb-8">요청하신 페이지를 찾을 수 없습니다.</p>
        <Link href="/">
          <Button className="bg-blue-600 hover:bg-blue-700">
            <ArrowLeft className="h-4 w-4 mr-2" />
            홈으로 돌아가기
          </Button>
        </Link>
      </div>
    </div>
  )
}
