// useApi.ts

import { useAuth } from "@clerk/remix";
import { useCallback } from "react";

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

export const useApi = () => {
  const { getToken } = useAuth();

  const apiFetch = useCallback(async (url: string, options: RequestInit = {}) => {
    const token = await getToken();

    // 전달된 url과 baseUrl을 합쳐서 전체 URL을 만듭니다.
    const fullUrl = new URL(url, API_BASE_URL).toString();

    const headers = new Headers(options.headers);
    if (token) {
      headers.set("Authorization", `Bearer ${token}`);
    }

    if (options.body && typeof options.body === 'string' && !headers.has('Content-Type')) {
      headers.set('Content-Type', 'application/json');
    }

    // fetch 호출 시 fullUrl을 사용합니다.
    const response = await fetch(fullUrl, {
      ...options,
      headers,
    });

    if (!response.ok) {
      throw new Error(`API call failed with status: ${response.status}`);
    }

    if (response.status === 204 || response.headers.get('content-length') === '0') {
      return null;
    }

    const contentType = response.headers.get('content-type');
    if (contentType && contentType.includes('application/json')) {
      return response.json();
    }

    return response.text();
  }, [getToken]);

  return apiFetch;
};