import type { ApiResponse } from "./types";

const API_PREFIX = "/api";

export async function apiRequest<T>(
  path: string,
  init: RequestInit = {},
): Promise<T> {
  if (!path.startsWith("/"))
    throw new Error("API path must be relative and start with /");
  const response = await fetch(`${API_PREFIX}${path}`, {
    credentials: "include",
    headers: { "Content-Type": "application/json", ...(init.headers ?? {}) },
    ...init,
  });
  const envelope = (await response.json()) as ApiResponse<T>;
  if (!response.ok || !envelope.success) {
    const message = envelope.error?.message ?? "요청 처리에 실패했습니다.";
    const error = new Error(message) as Error & {
      fieldErrors?: Record<string, string>;
      status?: number;
    };
    error.fieldErrors = envelope.error?.fieldErrors;
    error.status = response.status;
    throw error;
  }
  return envelope.data;
}

export const api = {
  health: () => apiRequest<{ status: string }>("/health"),
  login: (loginId: string, password: string) =>
    apiRequest("/auth/login", {
      method: "POST",
      body: JSON.stringify({ loginId, password }),
    }),
  me: () => apiRequest("/auth/me"),
  logout: () => apiRequest("/auth/logout", { method: "POST" }),
  list: (path: string, query: Record<string, string> = {}) =>
    apiRequest(`${path}?${new URLSearchParams(query)}`),
  mutate: (path: string, method: string, body: unknown) =>
    apiRequest(path, { method, body: JSON.stringify(body) }),
};
