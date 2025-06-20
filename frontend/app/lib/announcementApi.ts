import type { 
  AnnouncementRequest, 
  AnnouncementResponse, 
  PaginatedAnnouncements,
  PaginatedAnnouncementResponse
} from "../../types/notice";

const BASE_URL = 'http://localhost:8080';
const API_ENDPOINT = '/api/announcements';

// 개발 환경에서 인증 우회 (실제 운영에서는 false로 설정)
const BYPASS_AUTH = true;

class AnnouncementApi {
  private getAuthToken(): string | null {
    // 로컬 스토리지나 쿠키에서 JWT 토큰 가져오기
    if (typeof window !== 'undefined') {
      return localStorage.getItem('auth_token');
    }
    return null;
  }

  private getHeaders(includeAuth: boolean = false): HeadersInit {
    const headers: HeadersInit = {
      'Content-Type': 'application/json',
    };

    if (includeAuth) {
      if (BYPASS_AUTH) {
        // 개발 환경에서 임시 토큰 사용
        headers['Authorization'] = 'Bearer dev-token';
      } else {
        const token = this.getAuthToken();
        if (token) {
          headers['Authorization'] = `Bearer ${token}`;
        } else {
          console.warn('인증 토큰이 없습니다. localStorage에 "auth_token"을 설정해주세요.');
        }
      }
    }

    return headers;
  }

  // 페이지네이션된 공지사항 조회 (새로운 API)
  async getAnnouncementsWithPagination(page: number = 0, size: number = 10, sortBy: string = 'id', sortDir: string = 'desc'): Promise<PaginatedAnnouncementResponse> {
    try {
      const params = new URLSearchParams({
        page: page.toString(),
        size: size.toString(),
        sortBy: sortBy,
        sortDir: sortDir
      });

      const response = await fetch(`${BASE_URL}${API_ENDPOINT}?${params}`, {
        method: 'GET',
        headers: this.getHeaders(),
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      return await response.json();
    } catch (error) {
      console.error('Error fetching announcements with pagination:', error);
      throw error;
    }
  }

  // 모든 공지사항 조회 (배열 반환) - 기존 호환성 유지
  async getAllAnnouncements(): Promise<AnnouncementResponse[]> {
    const response = await fetch(`${BASE_URL}${API_ENDPOINT}`, {
      method: 'GET',
      headers: this.getHeaders(),
    });
    if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
    return await response.json();
  }

  // 페이지네이션된 공지사항 조회 (인증 불필요) - 기존 호환성 유지
  async getAnnouncementsWithPaginationOld(page: number = 1, limit: number = 10): Promise<PaginatedAnnouncements> {
    try {
      const response = await fetch(`${BASE_URL}${API_ENDPOINT}?page=${page}&limit=${limit}`, {
        method: 'GET',
        headers: this.getHeaders(),
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      return await response.json();
    } catch (error) {
      console.error('Error fetching announcements with pagination:', error);
      throw error;
    }
  }

  // 단일 공지사항 조회
  async getAnnouncementById(id: number): Promise<AnnouncementResponse> {
    const response = await fetch(`${BASE_URL}${API_ENDPOINT}/${id}`, {
      method: 'GET',
      headers: this.getHeaders(),
    });
    if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
    return await response.json();
  }

  // 공지사항 생성
  async createAnnouncement(data: AnnouncementRequest): Promise<AnnouncementResponse> {
    const response = await fetch(`${BASE_URL}${API_ENDPOINT}`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify(data),
    });
    if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
    return await response.json();
  }

  // 공지사항 수정
  async updateAnnouncement(id: number, data: AnnouncementRequest): Promise<AnnouncementResponse> {
    const response = await fetch(`${BASE_URL}${API_ENDPOINT}/${id}`, {
      method: 'PUT',
      headers: this.getHeaders(),
      body: JSON.stringify(data),
    });
    if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
    return await response.json();
  }

  // 공지사항 삭제
  async deleteAnnouncement(id: number): Promise<void> {
    const response = await fetch(`${BASE_URL}${API_ENDPOINT}/${id}`, {
      method: 'DELETE',
      headers: this.getHeaders(),
    });
    if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
  }
}

export const announcementApi = new AnnouncementApi(); 