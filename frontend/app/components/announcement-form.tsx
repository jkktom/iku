import { useState, useEffect } from "react"
import { Button } from "./ui/button"
import { Card, CardContent, CardHeader } from "./ui/card"
import { Input } from "./ui/input"
import { Label } from "./ui/label"
import { Textarea } from "./ui/textarea"
import { Checkbox } from "./ui/checkbox"
import type { Announcement, AnnouncementRequest } from "../../types/notice"
import { useApi } from "~/utils/api"
import { ArrowLeft, Save, Loader2 } from "lucide-react"

interface AnnouncementFormProps {
  announcement?: Announcement
  onCancel: () => void
  onSuccess: (formData?: AnnouncementRequest) => void
  saving?: boolean
  isEditMode?: boolean
}

export function AnnouncementForm({ 
  announcement, 
  onCancel, 
  onSuccess, 
  saving = false,
  isEditMode = false 
}: AnnouncementFormProps) {
  const [formData, setFormData] = useState<AnnouncementRequest>({
    title: '',
    content: '',
    important: false
  })
  const [errors, setErrors] = useState<Record<string, string>>({})
  const apiFetch = useApi()
  const isEditing = !!announcement || isEditMode

  useEffect(() => {
    if (announcement) {
      console.log('Form - Setting announcement data:', announcement);
      setFormData({
        title: announcement.title,
        content: announcement.content,
        important: announcement.important
      })
    }
  }, [announcement])

  const validateForm = (): boolean => {
    const newErrors: Record<string, string> = {}

    if (!formData.title.trim()) {
      newErrors.title = '제목을 입력해주세요.'
    }

    if (!formData.content.trim()) {
      newErrors.content = '내용을 입력해주세요.'
    }

    setErrors(newErrors)
    return Object.keys(newErrors).length === 0
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()

    if (!validateForm()) {
      return
    }

    console.log('Form submit - isEditMode:', isEditMode, 'announcement:', announcement);

    try {
      if (isEditing && announcement && !isEditMode) {
        console.log('Using old edit mode');
        await apiFetch(`/api/announcements/${announcement.id}`, {
          method: 'PUT',
          body: JSON.stringify(formData)
        })
        onSuccess()
      } else if (isEditMode) {
        console.log('Using new edit mode, formData:', formData);
        onSuccess(formData)
      } else {
        console.log('Creating new announcement');
        await apiFetch(`/api/announcements`, {
          method: 'POST',
          body: JSON.stringify(formData)
        })
        onSuccess()
      }
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : '저장 중 오류가 발생했습니다.'
      alert(errorMessage)
      console.error('Error saving announcement:', err)
    }
  }

  const handleInputChange = (field: keyof AnnouncementRequest, value: string | boolean) => {
    setFormData(prev => ({ ...prev, [field]: value }))
    
    // 에러 메시지 제거
    if (errors[field]) {
      setErrors(prev => ({ ...prev, [field]: '' }))
    }
  }

  return (
    <div className="min-h-screen bg-slate-900 p-6">
      <div className="max-w-4xl mx-auto">
        {/* 헤더 */}
        <div className="flex items-center justify-between mb-8">
          <Button onClick={onCancel} variant="outline" className="border-slate-600 text-slate-300 hover:bg-slate-700">
            <ArrowLeft className="w-4 h-4 mr-2" />
            목록으로
          </Button>
          <h1 className="text-3xl font-bold text-white">
            {isEditing ? '공지사항 수정' : '새 공지사항 작성'}
          </h1>
        </div>

        {/* 폼 */}
        <Card className="bg-slate-800 border-slate-700">
          <CardHeader>
            <h2 className="text-xl font-semibold text-white">
              {isEditing ? '공지사항 수정' : '새 공지사항 작성'}
            </h2>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleSubmit} className="space-y-6">
              {/* 제목 */}
              <div className="space-y-2">
                <Label htmlFor="title" className="text-slate-300">
                  제목 *
                </Label>
                <Input
                  id="title"
                  type="text"
                  value={formData.title}
                  onChange={(e) => handleInputChange('title', e.target.value)}
                  className={`bg-slate-700 border-slate-600 text-white ${
                    errors.title ? 'border-red-500' : ''
                  }`}
                  placeholder="공지사항 제목을 입력하세요"
                />
                {errors.title && (
                  <p className="text-red-400 text-sm">{errors.title}</p>
                )}
              </div>

              {/* 내용 */}
              <div className="space-y-2">
                <Label htmlFor="content" className="text-slate-300">
                  내용 *
                </Label>
                <Textarea
                  id="content"
                  value={formData.content}
                  onChange={(e) => handleInputChange('content', e.target.value)}
                  className={`bg-slate-700 border-slate-600 text-white min-h-[200px] ${
                    errors.content ? 'border-red-500' : ''
                  }`}
                  placeholder="공지사항 내용을 입력하세요"
                />
                {errors.content && (
                  <p className="text-red-400 text-sm">{errors.content}</p>
                )}
              </div>

              {/* 중요 공지 여부 */}
              <div className="flex items-center space-x-2">
                <Checkbox
                  id="important"
                  checked={formData.important}
                  onCheckedChange={(checked) => handleInputChange('important', checked as boolean)}
                  className="border-slate-600 data-[state=checked]:bg-blue-600"
                />
                <Label htmlFor="important" className="text-slate-300 cursor-pointer">
                  중요 공지로 설정
                </Label>
              </div>

              {/* 버튼 */}
              <div className="flex justify-end gap-3 pt-6">
                <Button
                  type="button"
                  onClick={onCancel}
                  variant="outline"
                  className="border-slate-600 text-slate-300 hover:bg-slate-700"
                >
                  취소
                </Button>
                <Button
                  type="submit"
                  disabled={saving}
                  className="bg-blue-600 hover:bg-blue-700"
                >
                  {saving ? (
                    <>
                      <Loader2 className="w-4 h-4 mr-2 animate-spin" />
                      저장 중...
                    </>
                  ) : (
                    <>
                      <Save className="w-4 h-4 mr-2" />
                      {isEditing ? '수정' : '작성'}
                    </>
                  )}
                </Button>
              </div>
            </form>
          </CardContent>
        </Card>
      </div>
    </div>
  )
} 