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

    return response.json();
  }, [getToken]);

  return apiFetch;
}; 