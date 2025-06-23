import { useState, useEffect } from "react"
import { Button } from "./ui/button"
import { Card, CardContent, CardHeader } from "./ui/card"
import { Badge } from "./ui/badge"
import type { Announcement } from "../../types/notice"
import { useApi } from "~/utils/api"
import { ArrowLeft, Edit, Trash2, Loader2, Clock, User, Calendar } from "lucide-react"

// 안전한 날짜 포맷팅 함수
const formatDateTime = (dateString: string | null): string => {
  if (!dateString) return '날짜 없음'
  try {
    return new Date(dateString).toLocaleString('ko-KR')
  } catch (error) {
    console.error('날짜 파싱 오류:', error)
    return '날짜 오류'
  }
}

// 날짜 비교 함수
const isSameDate = (date1: string | null, date2: string | null): boolean => {
  if (!date1 || !date2) return false
  try {
    return new Date(date1).getTime() === new Date(date2).getTime()
  } catch (error) {
    return false
  }
}

interface AnnouncementDetailProps {
  id: number
  onBack: () => void
  onEdit?: () => void
  onDelete?: (id: number) => void
}

export function AnnouncementDetail({ id, onBack, onEdit, onDelete }: AnnouncementDetailProps) {
  const [announcement, setAnnouncement] = useState<Announcement | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [deleting, setDeleting] = useState(false)
  const apiFetch = useApi()
  useEffect(() => {
    const fetchAnnouncement = async () => {
      try {
        setLoading(true)
        setError(null)
        const data = await apiFetch(`/api/announcements/${id}`)
        console.log('Fetched announcement data:', data)
        setAnnouncement(data)
      } catch (err) {
        setError(err instanceof Error ? err.message : '공지사항을 불러오는 중 오류가 발생했습니다.')
        console.error('Error fetching announcement:', err)
      } finally {
        setLoading(false)
      }
    }

    fetchAnnouncement()
  }, [id])

  const handleDelete = async () => {
    if (!announcement || !onDelete) return

    if (!confirm('정말로 이 공지사항을 삭제하시겠습니까?')) {
      return
    }

    try {
      setDeleting(true)
      await apiFetch(`/api/announcements/${id}`, {
        method: 'DELETE',
      })
      onDelete(id)
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : '삭제 중 오류가 발생했습니다.'
      alert(errorMessage)
      console.error('Error deleting announcement:', err)
    } finally {
      setDeleting(false)
    }
  }

  const handleEdit = () => {
    console.log('Edit button clicked, navigating to edit page...'); // 디버깅용 로그
    if (!announcement) return;
    // 수정 페이지로 이동 - props로 전달받은 onEdit 함수 사용
    if (onEdit) {
      onEdit();
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-slate-900 p-6">
        <div className="max-w-4xl mx-auto">
          <div className="flex items-center justify-center py-12">
            <Loader2 className="w-8 h-8 animate-spin text-blue-500" />
            <span className="ml-2 text-white">공지사항을 불러오는 중...</span>
          </div>
        </div>
      </div>
    )
  }

  if (error || !announcement) {
    return (
      <div className="min-h-screen bg-slate-900 p-6">
        <div className="max-w-4xl mx-auto">
          <div className="text-center py-12">
            <div className="text-red-400 mb-4">{error || '공지사항을 찾을 수 없습니다.'}</div>
            <Button onClick={onBack} className="bg-blue-600 hover:bg-blue-700">
              목록으로 돌아가기
            </Button>
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-slate-900 p-6">
      <div className="max-w-4xl mx-auto">
        {/* 헤더 */}
        <div className="flex items-center justify-between mb-8">
          <Button onClick={onBack} variant="outline" className="border-slate-600 text-slate-300 hover:bg-slate-700">
            <ArrowLeft className="w-4 h-4 mr-2" />
            목록으로
          </Button>
          <div className="flex gap-2">
            {onEdit && (
              <Button 
                onClick={handleEdit}
                variant="outline" 
                className="border-slate-600 text-slate-300 hover:bg-slate-700"
              >
                <Edit className="w-4 h-4 mr-2" />
                수정
              </Button>
            )}
            {onDelete && (
              <Button 
                onClick={handleDelete}
                variant="outline" 
                disabled={deleting}
                className="border-red-600 text-red-400 hover:bg-red-900/20"
              >
                {deleting ? (
                  <Loader2 className="w-4 h-4 mr-2 animate-spin" />
                ) : (
                  <Trash2 className="w-4 h-4 mr-2" />
                )}
                삭제
              </Button>
            )}
          </div>
        </div>

        {/* 공지사항 내용 */}
        <Card className="bg-slate-800 border-slate-700">
          <CardHeader>
            <div className="flex items-start justify-between">
              <div className="flex-1">
                <div className="flex items-center gap-3 mb-2">
                  {announcement.important && (
                    <Badge className="bg-red-600 hover:bg-red-700">중요</Badge>
                  )}
                  {(announcement.isActive === false) && (
                    <Badge className="bg-gray-600 hover:bg-gray-700">비활성</Badge>
                  )}
                  <h1 className="text-2xl font-bold text-white">{announcement.title}</h1>
                </div>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-slate-400 text-sm">
                  <div className="flex items-center gap-2">
                    <User className="w-4 h-4" />
                    <span>작성자: {announcement.authorName || '작성자 없음'}</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <Calendar className="w-4 h-4" />
                    <span>작성일: {formatDateTime(announcement.createdAt)}</span>
                  </div>
                  {announcement.updatedAt && !isSameDate(announcement.updatedAt, announcement.createdAt) && (
                    <div className="flex items-center gap-2">
                      <Clock className="w-4 h-4" />
                      <span>수정일: {formatDateTime(announcement.updatedAt)}</span>
                    </div>
                  )}
                  {announcement.deletedAt && (
                    <div className="flex items-center gap-2">
                      <Trash2 className="w-4 h-4" />
                      <span>삭제일: {formatDateTime(announcement.deletedAt)}</span>
                    </div>
                  )}
                </div>
              </div>
            </div>
          </CardHeader>
          <CardContent>
            <div className="prose prose-invert max-w-none">
              <div className="text-slate-300 whitespace-pre-wrap leading-relaxed">
                {announcement.content}
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  )
} 