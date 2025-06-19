// app/routes/_index.tsx 또는 index.tsx

import { LoaderFunction, redirect, MetaFunction } from "@remix-run/node";
import { getAuth } from "@clerk/remix/ssr.server";
import { UserButton } from "@clerk/remix";
import { AnnouncementBoard } from "~/components/announcement-board";

// 로그인 체크용 loader
export const loader: LoaderFunction = async (args) => {
  const { userId } = await getAuth(args);
  if (!userId) {
    return redirect("/sign-in");
  }
  return {};
};

// 메타 정보 설정
export const meta: MetaFunction = () => {
  return [
    { title: "IKU 공지사항 시스템" },
    { name: "description", content: "공지사항 시스템" },
  ];
};

// UI 구성
export default function Index() {
  return (
      <div className="min-h-screen bg-gray-100 dark:bg-gray-900 text-gray-800 dark:text-white px-4 py-6">
        {/* 상단 로고 및 유저 버튼 */}
        <header className="flex justify-between items-center mb-6">
          <div className="text-xl font-bold text-blue-600">iku</div>
          <UserButton />
        </header>

        {/* 공지사항 컴포넌트 */}
        <main>
          <AnnouncementBoard />
        </main>
      </div>
  );
}
