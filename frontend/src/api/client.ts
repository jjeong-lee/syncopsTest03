import type { ApiResponse } from "../types";

export class ApiClientError extends Error {
  constructor(
    public status: number,
    public payload?: ApiResponse<unknown>,
  ) {
    super(payload?.error?.message ?? `HTTP ${status}`);
  }
}

export async function apiRequest<T>(
  path: string,
  init: RequestInit = {},
): Promise<T> {
  if (!path.startsWith("/api/")) {
    throw new Error("API path must be relative and start with /api/");
  }
  if (path.includes("localhost") || path.includes("backend:")) {
    throw new Error("Absolute host names are not allowed in browser API calls");
  }
  const response = await fetch(path, {
    credentials: "include",
    headers: { "Content-Type": "application/json", ...(init.headers ?? {}) },
    ...init,
  });
  const payload = (await response.json()) as ApiResponse<T>;
  if (!response.ok || !payload.success) {
    throw new ApiClientError(response.status, payload as ApiResponse<unknown>);
  }
  return payload.data as T;
}

export function toQuery(params: Record<string, string | undefined>): string {
  const search = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== "") search.set(key, value);
  });
  const query = search.toString();
  return query ? `?${query}` : "";
}
