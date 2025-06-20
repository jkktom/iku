import type { MetaFunction } from "@remix-run/node";
import { AnnouncementForm } from "~/components/announcement-form";
import { useNavigate, useParams } from "@remix-run/react";
import { useState, useEffect } from "react";
import { announcementApi } from "../lib/announcementApi";
import type { Announcement, AnnouncementRequest } from "../../types/notice";
import { Loader2 } from "lucide-react";

export const meta: MetaFunction = () => {
  return [
    { title: "공지사항 수정 - IKU 공지사항 시스템" },
    { name: "description", content: "공지사항을 수정합니다." },
  ];
};

export default function EditAnnouncement() {
  const navigate = useNavigate();
  const params = useParams();
  const id = parseInt(params.id || "0", 10);
  
  const [announcement, setAnnouncement] = useState<Announcement | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    const fetchAnnouncement = async () => {
      try {
        setLoading(true);
        const data = await announcementApi.getAnnouncementById(id);
        console.log('Edit page - Fetched announcement data:', data);
        setAnnouncement(data);
      } catch (err) {
        setError(err instanceof Error ? err.message : '공지사항을 불러오는 중 오류가 발생했습니다.');
        console.error('Error fetching announcement:', err);
      } finally {
        setLoading(false);
      }
    };

    if (id > 0) {
      fetchAnnouncement();
    }
  }, [id]);

  const handleSuccess = async (formData?: AnnouncementRequest) => {
    if (!formData) {
      console.error('No form data provided');
      return;
    }
    
    try {
      setSaving(true);
      await announcementApi.updateAnnouncement(id, formData);
      navigate(`/announcement/${id}`);
    } catch (err) {
      alert('수정 중 오류가 발생했습니다.');
    } finally {
      setSaving(false);
    }
  };

  const handleCancel = () => {
    navigate(`/announcement/${id}`);
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
    );
  }

  if (error || !announcement) {
    return (
      <div className="min-h-screen bg-slate-900 p-6">
        <div className="max-w-4xl mx-auto">
          <div className="text-center py-12">
            <div className="text-red-400 mb-4">{error || '공지사항을 찾을 수 없습니다.'}</div>
            <button 
              onClick={() => navigate('/')}
              className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded"
            >
              목록으로 돌아가기
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <AnnouncementForm
      announcement={announcement}
      onCancel={handleCancel}
      onSuccess={handleSuccess}
      saving={saving}
      isEditMode={true}
    />
  );
} 