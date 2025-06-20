import type { MetaFunction } from "@remix-run/node";
import { AnnouncementForm } from "~/components/announcement-form";
import { useNavigate } from "@remix-run/react";

export const meta: MetaFunction = () => {
  return [
    { title: "새 공지사항 작성 - IKU 공지사항 시스템" },
    { name: "description", content: "새 공지사항을 작성합니다." },
  ];
};

export default function NewAnnouncement() {
  const navigate = useNavigate();

  const handleSuccess = () => {
    navigate("/");
  };

  const handleCancel = () => {
    navigate("/");
  };

  return (
    <AnnouncementForm
      onCancel={handleCancel}
      onSuccess={handleSuccess}
    />
  );
} 