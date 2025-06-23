import { useAuth } from "@clerk/remix";
import { useCallback } from "react";

export const useApi = () => {
  const { getToken } = useAuth();

  const apiFetch = useCallback(async (url: string, options: RequestInit = {}) => {
    const token = await getToken();

    const headers = new Headers(options.headers);
    if (token) {
      headers.set("Authorization", `Bearer ${token}`);
    }

    // Automatically set Content-Type for JSON requests
    if (options.body && typeof options.body === 'string' && !headers.has('Content-Type')) {
      headers.set('Content-Type', 'application/json');
    }

    const response = await fetch(url, {
      ...options,
      headers,
    });

    if (!response.ok) {
      // Handle HTTP errors
      throw new Error(`API call failed with status: ${response.status}`);
    }

    // Handle empty responses (like 204 No Content)
    if (response.status === 204 || response.headers.get('content-length') === '0') {
      return null;
    }

    // Check if response has content to parse
    const contentType = response.headers.get('content-type');
    if (contentType && contentType.includes('application/json')) {
      return response.json();
    }

    // For non-JSON responses, return text
    return response.text();
  }, [getToken]);

  return apiFetch;
}; 