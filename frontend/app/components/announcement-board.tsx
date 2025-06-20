import { useState, useEffect } from "react"
import { useNavigate } from "@remix-run/react"
import { Button } from "./ui/button"
import { Card, CardContent, CardHeader } from "./ui/card"
import { Badge } from "./ui/badge"
import type { Announcement, PaginatedAnnouncementResponse } from "../../types/notice"
import { announcementApi } from "../lib/announcementApi"
import { Pagination } from "./pagination"
import { Plus, Loader2 } from "lucide-react"

// 안전한 날짜 포맷팅 함수
const formatDate = (dateString: string | null): string => {
  if (!dateString) return '날짜 없음'
  try {
    return new Date(dateString).toLocaleDateString('ko-KR')
  } catch (error) {
    console.error('날짜 파싱 오류:', error)
    return '날짜 오류'
  }
}

export function AnnouncementBoard() {
  const [announcements, setAnnouncements] = useState<Announcement[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [currentPage, setCurrentPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)
  const navigate = useNavigate()

  const fetchAnnouncements = async (page: number = 0) => {
    setLoading(true)
    try {
      console.log('Fetching announcements with pagination, page:', page);
      const result = await announcementApi.getAnnouncementsWithPagination(page, 10, 'id', 'desc')
      console.log('Pagination API result:', result);
      setAnnouncements(result.content || [])
      setTotalPages(result.totalPages)
      setTotalElements(result.totalElements)
      setCurrentPage(result.number)
    } catch (err) {
      console.error('Pagination API failed, trying fallback:', err);
      // 페이지네이션 API가 실패하면 기존 API로 폴백
      try {
        const fallbackResult = await announcementApi.getAllAnnouncements()
        console.log('Fallback API result:', fallbackResult);
        setAnnouncements(fallbackResult || [])
        setTotalPages(1)
        setTotalElements(fallbackResult?.length || 0)
        setCurrentPage(0)
      } catch (fallbackErr) {
        console.error('Fallback API also failed:', fallbackErr);
        setError("공지사항을 불러오는 중 오류가 발생했습니다.")
        setAnnouncements([])
      }
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchAnnouncements(0)
  }, [])

  const handlePageChange = (page: number) => {
    fetchAnnouncements(page)
  }

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

  if (error) {
    return (
      <div className="min-h-screen bg-slate-900 p-6">
        <div className="max-w-4xl mx-auto">
          <div className="text-center py-12">
            <div className="text-red-400 mb-4">{error}</div>
            <Button onClick={() => fetchAnnouncements(0)} className="bg-blue-600 hover:bg-blue-700">
              다시 시도
            </Button>
          </div>
        </div>
      </div>
    )
  }

  const safeAnnouncements = announcements || []

  return (
    <div className="min-h-screen bg-slate-900 p-6">
      <div className="max-w-4xl mx-auto">
       
        {/* 헤더 */}
        <div className="flex items-center justify-between mb-8 w-full max-w-4xl mx-auto">
          {/* 왼쪽: 공지사항 텍스트 */}
          <div className="flex flex-col items-start">
            <h1 className="text-3xl font-bold text-white">공지사항</h1>
            <Badge className="bg-blue-600 text-white w-fit mt-2">
              총 {totalElements}개
            </Badge>
          </div>
          {/* 가운데: 로고 */}
          <div className="flex-1 flex justify-center">
            <div className="w-32 h-32 flex items-center justify-center">
              <img 
                src="/iku-logo.png" 
                alt="IKU Logo" 
                className="w-full h-full object-contain"
              />
            </div>
          </div>
          {/* 오른쪽: 작성하기 버튼 */}
          <div className="flex items-center">
            <Button asChild className="bg-slate-700 hover:bg-slate-600 text-white">
              <a href="/announcement/new">
                <Plus className="w-4 h-4 mr-2" />새 글 작성
              </a>
            </Button>
          </div>
        </div>

        {/* 게시판 */}
        <Card className="bg-slate-800 border-slate-700">
          <CardHeader>
            <div className="grid grid-cols-12 gap-4 text-slate-300 font-medium border-b border-slate-700 pb-3">
              <div className="col-span-1 text-center">번호</div>
              <div className="col-span-6">제목</div>
              <div className="col-span-2 text-center">작성자</div>
              <div className="col-span-3 text-center">작성일</div>
            </div>
          </CardHeader>
          <CardContent className="p-0">
            {safeAnnouncements.length === 0 ? (
              <div className="text-center py-12 text-slate-400">등록된 공지사항이 없습니다.</div>
            ) : (
              <div className="divide-y divide-slate-700">
                {safeAnnouncements.map((announcement, index) => (
                  <div
                    key={announcement.id}
                    className="grid grid-cols-12 gap-4 p-4 hover:bg-slate-700/50 cursor-pointer transition-colors"
                  >
                    <div className="col-span-1 text-center text-slate-300">
                      {currentPage * 10 + index + 1}
                    </div>
                    <div className="col-span-6 flex items-center gap-2">
                      {announcement.important && (
                        <Badge className="bg-red-600 hover:bg-red-700 text-xs">중요</Badge>
                      )}
                      <span
                        className="text-white hover:text-blue-400 transition-colors underline cursor-pointer"
                        onClick={() => navigate(`/announcement/${announcement.id}`)}
                      >
                        {announcement.title}
                      </span>
                    </div>
                    <div className="col-span-2 text-center text-slate-400">
                      {announcement.authorName || '작성자 없음'}
                    </div>
                    <div className="col-span-3 text-center text-slate-400">
                      {formatDate(announcement.createdAt)}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </CardContent>
        </Card>

        {/* 페이지네이션 */}
        <Pagination
          currentPage={currentPage}
          totalPages={totalPages}
          onPageChange={handlePageChange}
        />
      </div>
    </div>
  )
} 