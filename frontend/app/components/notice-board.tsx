import { useState } from "react"
import { Button } from "~/components/ui/button"
import { Card, CardContent, CardHeader } from "~/components/ui/card"
import { Badge } from "~/components/ui/badge"
import type { Notice } from "~/types/notice"
import { NoticeForm } from "./notice-form"
import { NoticeDetail } from "./notice-detail"
import { Plus } from "lucide-react"
import Image from "next/image"

interface NoticeBoardProps {
  notices: Notice[]
}

export function NoticeBoard({ notices }: NoticeBoardProps) {
  const [selectedNotice, setSelectedNotice] = useState<Notice | null>(null)
  const [isCreating, setIsCreating] = useState(false)

  if (isCreating) {
    return (
      <div className="min-h-screen bg-slate-900 p-6">
        <div className="max-w-4xl mx-auto">
          <NoticeForm onCancel={() => setIsCreating(false)} />
        </div>
      </div>
    )
  }

  if (selectedNotice) {
    return (
      <div className="min-h-screen bg-slate-900 p-6">
        <div className="max-w-4xl mx-auto">
          <NoticeDetail notice={selectedNotice} onBack={() => setSelectedNotice(null)} />
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-slate-900 p-6">
      <div className="max-w-4xl mx-auto">
        {/* 헤더 */}
        <div className="flex items-center justify-between mb-8">
          <div className="flex items-center gap-4">
            <Image src="/logo.png" alt="IKU Logo" width={60} height={60} className="object-contain" />
            <h1 className="text-3xl font-bold text-white">공지 사항</h1>
          </div>
          <Button onClick={() => setIsCreating(true)} className="bg-slate-700 hover:bg-slate-600 text-white">
            <Plus className="w-4 h-4 mr-2" />새 글 작성
          </Button>
        </div>

        {/* 게시판 */}
        <Card className="bg-slate-800 border-slate-700">
          <CardHeader>
            <div className="grid grid-cols-12 gap-4 text-slate-300 font-medium border-b border-slate-700 pb-3">
              <div className="col-span-1 text-center">번호</div>
              <div className="col-span-7">제목</div>
              <div className="col-span-4 text-center">작성일</div>
            </div>
          </CardHeader>
          <CardContent className="p-0">
            {notices.length === 0 ? (
              <div className="text-center py-12 text-slate-400">등록된 공지사항이 없습니다.</div>
            ) : (
              <div className="divide-y divide-slate-700">
                {notices.map((notice) => (
                  <div
                    key={notice.id}
                    className="grid grid-cols-12 gap-4 p-4 hover:bg-slate-700/50 cursor-pointer transition-colors"
                    onClick={() => setSelectedNotice(notice)}
                  >
                    <div className="col-span-1 text-center text-slate-300">{notice.id}</div>
                    <div className="col-span-7 flex items-center gap-2">
                      {notice.isImportant && <Badge className="bg-red-600 hover:bg-red-700 text-xs">공지</Badge>}
                      <span className="text-white hover:text-blue-400 transition-colors">{notice.title}</span>
                    </div>
                    <div className="col-span-4 text-center text-slate-400">{notice.createdAt}</div>
                  </div>
                ))}
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
