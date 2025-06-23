"use client"

import { useState } from "react"
import { Button } from "~/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "~/components/ui/card"
import { Badge } from "~/components/ui/badge"
import type { Notice } from "~/types/notice"
import { NoticeForm } from "./notice-form"
import { deleteNotice } from "~/lib/actions"
import { useRouter } from "next/navigation"
import { Pencil, Trash2, ArrowLeft } from "lucide-react"

interface NoticeDetailProps {
  notice: Notice
  onBack: () => void
}

export function NoticeDetail({ notice, onBack }: NoticeDetailProps) {
  const [isEditing, setIsEditing] = useState(false)
  const [isDeleting, setIsDeleting] = useState(false)
  const router = useRouter()

  const handleDelete = async () => {
    if (!confirm("정말로 이 공지사항을 삭제하시겠습니까?")) return

    setIsDeleting(true)
    try {
      await deleteNotice(notice.id)
      onBack()
      router.refresh()
    } catch (error) {
      console.error("Error deleting notice:", error)
    } finally {
      setIsDeleting(false)
    }
  }

  if (isEditing) {
    return <NoticeForm notice={notice} onCancel={() => setIsEditing(false)} />
  }

  return (
    <Card className="bg-slate-800 border-slate-700">
      <CardHeader>
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-3">
            <Button variant="ghost" size="sm" onClick={onBack} className="text-slate-300 hover:bg-slate-700">
              <ArrowLeft className="w-4 h-4" />
            </Button>
            <div>
              <CardTitle className="text-white flex items-center gap-2">
                {notice.title}
                {notice.isImportant && <Badge className="bg-red-600 hover:bg-red-700">공지</Badge>}
              </CardTitle>
              <p className="text-slate-400 text-sm mt-1">{notice.createdAt}</p>
            </div>
          </div>
          <div className="flex gap-2">
            <Button
              variant="outline"
              size="sm"
              onClick={() => setIsEditing(true)}
              className="border-slate-600 text-slate-300 hover:bg-slate-700"
            >
              <Pencil className="w-4 h-4" />
            </Button>
            <Button
              variant="outline"
              size="sm"
              onClick={handleDelete}
              disabled={isDeleting}
              className="border-red-600 text-red-400 hover:bg-red-900/20"
            >
              <Trash2 className="w-4 h-4" />
            </Button>
          </div>
        </div>
      </CardHeader>
      <CardContent>
        <div className="text-slate-300 whitespace-pre-wrap">{notice.content}</div>
      </CardContent>
    </Card>
  )
}
