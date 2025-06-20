import { MetaFunction } from "@remix-run/node";
import { AnnouncementBoard } from "~/components/announcement-board";

export const meta: MetaFunction = () => {
  return [
    { title: "IKU 공지사항 시스템" },
    { name: "description", content: "공지사항 시스템" },
  ];
};

export default function Index() {
  return (
      <div className="min-h-screen bg-gray-100 dark:bg-gray-900 text-gray-800 dark:text-white px-4 py-6">
        {/* 상단 로고 */}
        <header className="flex justify-between items-center mb-6">
          <div className="text-xl font-bold text-blue-600">iku</div>
          {/* ✅ Clerk UserButton 제거, 임시 사용자 정보 표시 */}
          <div className="text-sm text-gray-500">
            임시 사용자 (Clerk 비활성화)
          </div>
        </header>

        {/* 공지사항 컴포넌트 */}
        <main>
          <AnnouncementBoard />
        </main>
      </div>
  );
}