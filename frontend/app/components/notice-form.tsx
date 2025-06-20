import type React from "react"

import { useState } from "react"
import { Button } from "~/components/ui/button"
import { Input } from "~/components/ui/input"
import { Textarea } from "~/components/ui/textarea"
import { Checkbox } from "~/components/ui/checkbox"
import { Card, CardContent, CardHeader, CardTitle } from "~/components/ui/card"
import { Label } from "~/components/ui/label"
import { createNotice, updateNotice } from "~/lib/actions"
import type { Notice } from "~/types/notice"
import { useRouter } from "next/navigation"

interface NoticeFormProps {
  notice?: Notice
  onCancel: () => void
}

export function NoticeForm({ notice, onCancel }: NoticeFormProps) {
  const [title, setTitle] = useState(notice?.title || "")
  const [content, setContent] = useState(notice?.content || "")
  const [isImportant, setIsImportant] = useState(notice?.isImportant || false)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const router = useRouter()

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!title.trim() || !content.trim()) return

    setIsSubmitting(true)
    try {
      if (notice) {
        await updateNotice(notice.id, { title, content, isImportant })
      } else {
        await createNotice({ title, content, isImportant })
      }
      onCancel()
      router.refresh()
    } catch (error) {
      console.error("Error saving notice:", error)
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <Card className="bg-slate-800 border-slate-700">
      <CardHeader>
        <CardTitle className="text-white">{notice ? "공지사항 수정" : "새 공지사항 작성"}</CardTitle>
      </CardHeader>
      <CardContent>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <Label htmlFor="title" className="text-slate-300">
              제목
            </Label>
            <Input
              id="title"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="공지사항 제목을 입력하세요"
              className="bg-slate-700 border-slate-600 text-white placeholder:text-slate-400"
              required
            />
          </div>

          <div>
            <Label htmlFor="content" className="text-slate-300">
              내용
            </Label>
            <Textarea
              id="content"
              value={content}
              onChange={(e) => setContent(e.target.value)}
              placeholder="공지사항 내용을 입력하세요"
              className="bg-slate-700 border-slate-600 text-white placeholder:text-slate-400 min-h-[120px]"
              required
            />
          </div>

          <div className="flex items-center space-x-2">
            <Checkbox
              id="important"
              checked={isImportant}
              onCheckedChange={(checked) => setIsImportant(checked as boolean)}
              className="border-slate-600"
            />
            <Label htmlFor="important" className="text-slate-300">
              중요 공지
            </Label>
          </div>

          <div className="flex gap-2">
            <Button
              type="submit"
              disabled={isSubmitting || !title.trim() || !content.trim()}
              className="bg-blue-600 hover:bg-blue-700"
            >
              {isSubmitting ? "저장 중..." : notice ? "수정" : "작성"}
            </Button>
            <Button
              type="button"
              variant="outline"
              onClick={onCancel}
              className="border-slate-600 text-slate-300 hover:bg-slate-700"
            >
              취소
            </Button>
          </div>
        </form>
      </CardContent>
    </Card>
  )
}
