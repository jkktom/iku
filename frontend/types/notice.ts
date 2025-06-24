export interface Notice {
  id: number
  title: string
  content: string
  createdAt: string
  isImportant: boolean
}

export interface Announcement {
  id: number
  title: string
  content: string
  authorName: string | null
  important: boolean
  createdAt: string | null
  updatedAt: string | null
  deletedAt: string | null
  isActive?: boolean
}

export interface AnnouncementRequest {
  title: string
  content: string
  important: boolean
}

export interface AnnouncementResponse {
  id: number
  title: string
  content: string
  authorName: string | null
  important: boolean
  createdAt: string | null
  updatedAt: string | null
  deletedAt: string | null
  isActive?: boolean
}

// 페이지네이션 응답 타입
export interface Pageable {
  pageNumber: number
  pageSize: number
  sort: {
    empty: boolean
    sorted: boolean
    unsorted: boolean
  }
  offset: number
  paged: boolean
  unpaged: boolean
}

export interface PaginatedAnnouncementResponse {
  content: AnnouncementResponse[]
  pageable: Pageable
  totalElements: number
  totalPages: number
  last: boolean
  first: boolean
  numberOfElements: number
  size: number
  number: number
}

export interface CreateNoticeData {
  title: string
  content: string
  isImportant: boolean
}

export interface PaginatedNotices {
  notices: Notice[]
  totalCount: number
  currentPage: number
  totalPages: number
  hasNext: boolean
  hasPrev: boolean
}

export interface PaginatedAnnouncements {
  announcements: Announcement[]
  totalCount: number
  currentPage: number
  totalPages: number
  hasNext: boolean
  hasPrev: boolean
}
