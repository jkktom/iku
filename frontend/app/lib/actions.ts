"use server"

import type { Notice, PaginatedNotices } from "~/types/notice"
import { revalidatePath } from "next/cache"
import { redirect } from "next/navigation"

// 임시 데이터 저장소 (실제로는 데이터베이스를 사용해야 합니다)
const notices: Notice[] = [
  {
    id: 15,
    title: "시스템 업데이트 완료 안내",
    content: "시스템 업데이트가 성공적으로 완료되었습니다. 새로운 기능들을 확인해보세요.",
    createdAt: "2024.04.25",
    isImportant: true,
  },
  {
    id: 14,
    title: "개인정보 처리방침 변경 안내",
    content: "개인정보 처리방침이 변경되었습니다. 변경된 내용을 확인해주세요.",
    createdAt: "2024.04.25",
    isImportant: false,
  },
  {
    id: 13,
    title: "서비스 이용약관 개정 안내",
    content: "서비스 이용약관이 개정되었습니다.",
    createdAt: "2024.04.24",
    isImportant: true,
  },
  {
    id: 12,
    title: "정기 점검 완료 안내",
    content: "정기 점검이 완료되었습니다.",
    createdAt: "2024.04.24",
    isImportant: false,
  },
  {
    id: 11,
    title: "새로운 기능 출시 안내",
    content: "새로운 기능이 출시되었습니다.",
    createdAt: "2024.04.24",
    isImportant: true,
  },
  {
    id: 10,
    title: "보안 업데이트 안내",
    content: "보안 업데이트가 적용되었습니다.",
    createdAt: "2024.04.23",
    isImportant: false,
  },
  {
    id: 9,
    title: "이벤트 종료 안내",
    content: "진행 중이던 이벤트가 종료되었습니다.",
    createdAt: "2024.04.23",
    isImportant: false,
  },
  {
    id: 8,
    title: "서버 증설 완료 안내",
    content: "서버 증설이 완료되어 더욱 안정적인 서비스를 제공합니다.",
    createdAt: "2024.04.22",
    isImportant: true,
  },
  {
    id: 7,
    title: "고객센터 운영시간 변경",
    content: "고객센터 운영시간이 변경되었습니다.",
    createdAt: "2024.04.22",
    isImportant: false,
  },
  {
    id: 6,
    title: "모바일 앱 업데이트",
    content: "모바일 앱이 업데이트되었습니다.",
    createdAt: "2024.04.21",
    isImportant: false,
  },
  {
    id: 5,
    title: "이용 약관 변경 안내",
    content: "이용 약관이 변경되었습니다. 자세한 내용을 확인해주세요.",
    createdAt: "2024.04.21",
    isImportant: false,
  },
  {
    id: 4,
    title: "서비스 점검 예정 안내",
    content: "서비스 점검이 예정되어 있습니다.",
    createdAt: "2024.04.20",
    isImportant: true,
  },
  {
    id: 3,
    title: "업데이트 안내",
    content: "새로운 기능이 업데이트되었습니다.",
    createdAt: "2024.04.20",
    isImportant: true,
  },
  {
    id: 2,
    title: "새로운 기능 안내",
    content: "새로운 기능이 추가되었습니다.",
    createdAt: "2024.04.19",
    isImportant: true,
  },
  {
    id: 1,
    title: "이벤트 안내",
    content: "특별 이벤트가 진행됩니다.",
    createdAt: "2024.04.19",
    isImportant: false,
  },
]

let nextId = 16
const ITEMS_PER_PAGE = 10

export async function getNotices(page = 1): Promise<PaginatedNotices> {
  const sortedNotices = [...notices].sort((a, b) => {
    // 중요 공지를 먼저 정렬, 그 다음 ID 순
    if (a.isImportant && !b.isImportant) return -1
    if (!a.isImportant && b.isImportant) return 1
    return b.id - a.id
  })

  const totalCount = sortedNotices.length
  const totalPages = Math.ceil(totalCount / ITEMS_PER_PAGE)
  const startIndex = (page - 1) * ITEMS_PER_PAGE
  const endIndex = startIndex + ITEMS_PER_PAGE
  const paginatedNotices = sortedNotices.slice(startIndex, endIndex)

  return {
    notices: paginatedNotices,
    totalCount,
    currentPage: page,
    totalPages,
    hasNext: page < totalPages,
    hasPrev: page > 1,
  }
}

export async function getNotice(id: number): Promise<Notice | null> {
  return notices.find((notice) => notice.id === id) || null
}

export async function createNotice(formData: FormData) {
  const title = formData.get("title") as string
  const content = formData.get("content") as string
  const isImportant = formData.get("isImportant") === "on"

  if (!title.trim() || !content.trim()) {
    throw new Error("제목과 내용을 모두 입력해주세요.")
  }

  const newNotice: Notice = {
    id: nextId++,
    title: title.trim(),
    content: content.trim(),
    createdAt: new Date()
      .toLocaleDateString("ko-KR", {
        year: "numeric",
        month: "2-digit",
        day: "2-digit",
      })
      .replace(/\./g, ".")
      .replace(/ /g, ""),
    isImportant,
  }

  notices.push(newNotice)
  revalidatePath("/")
  redirect("/")
}

export async function updateNotice(formData: FormData) {
  const id = Number.parseInt(formData.get("id") as string)
  const title = formData.get("title") as string
  const content = formData.get("content") as string
  const isImportant = formData.get("isImportant") === "on"

  if (!title.trim() || !content.trim()) {
    throw new Error("제목과 내용을 모두 입력해주세요.")
  }

  const index = notices.findIndex((notice) => notice.id === id)
  if (index === -1) {
    throw new Error("공지사항을 찾을 수 없습니다.")
  }

  notices[index] = {
    ...notices[index],
    title: title.trim(),
    content: content.trim(),
    isImportant,
  }

  revalidatePath("/")
  revalidatePath(`/notice/${id}`)
  redirect(`/notice/${id}`)
}

export async function deleteNotice(formData: FormData) {
  const id = Number.parseInt(formData.get("id") as string)

  const index = notices.findIndex((notice) => notice.id === id)
  if (index === -1) {
    throw new Error("공지사항을 찾을 수 없습니다.")
  }

  notices.splice(index, 1)
  revalidatePath("/")
  redirect("/")
}
