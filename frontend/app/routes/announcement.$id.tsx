import type { MetaFunction } from "@remix-run/node";
import { AnnouncementDetail } from "~/components/announcement-detail";
import { useNavigate, useParams } from "@remix-run/react";

export const meta: MetaFunction = () => {
  return [
    { title: "공지사항 상세 - IKU 공지사항 시스템" },
    { name: "description", content: "공지사항 상세 내용을 확인합니다." },
  ];
};

export default function AnnouncementDetailPage() {
  const navigate = useNavigate();
  const params = useParams();
  const id = parseInt(params.id || "0", 10);

  const handleBack = () => {
    navigate("/");
  };

  const handleEdit = () => {
    console.log('Route handleEdit called, navigating to:', `/announcement/edit/${id}`);
    navigate(`/announcement/edit/${id}`);
  };

  const handleDelete = (id: number) => {
    navigate("/");
  };

  return (
    <AnnouncementDetail
      id={id}
      onBack={handleBack}
      onEdit={handleEdit}
      onDelete={handleDelete}
    />
  );
} 