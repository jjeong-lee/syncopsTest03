export type ApiResponse<T> = {
  success: boolean;
  data?: T;
  error?: {
    code: string;
    message: string;
    fieldErrors?: { field: string; message: string }[];
  };
  timestamp: string;
};

export async function apiRequest<T>(
  path: string,
  options: RequestInit = {},
): Promise<T> {
  if (!path.startsWith("/api/"))
    throw new Error("API path must be relative /api/...");
  if (
    /^https?:\/\//.test(path) ||
    path.includes("localhost") ||
    path.includes("backend:")
  )
    throw new Error("Absolute API URL is not allowed");
  const response = await fetch(path, {
    credentials: "include",
    headers: { "Content-Type": "application/json", ...(options.headers || {}) },
    ...options,
  });
  const body = (await response.json()) as ApiResponse<T>;
  if (!response.ok || !body.success)
    throw Object.assign(new Error(body.error?.message || "요청 실패"), {
      status: response.status,
      error: body.error,
    });
  return body.data as T;
}

export const api = {
  login: (username: string, password: string) =>
    apiRequest<CurrentUser>("/api/auth/login", {
      method: "POST",
      body: JSON.stringify({ username, password }),
    }),
  logout: () => apiRequest<void>("/api/auth/logout", { method: "POST" }),
  me: () => apiRequest<CurrentUser>("/api/auth/me"),
  get: <T>(path: string) => apiRequest<T>(path),
  post: <T>(path: string, body: unknown) =>
    apiRequest<T>(path, { method: "POST", body: JSON.stringify(body) }),
  put: <T>(path: string, body: unknown) =>
    apiRequest<T>(path, { method: "PUT", body: JSON.stringify(body) }),
  patch: <T>(path: string, body: unknown) =>
    apiRequest<T>(path, { method: "PATCH", body: JSON.stringify(body) }),
  delete: <T>(path: string, query?: URLSearchParams) =>
    apiRequest<T>(
      query && query.toString() ? `${path}?${query.toString()}` : path,
      {
        method: "DELETE",
      },
    ),
};

export type CurrentUser = {
  userId: string;
  username: string;
  roles: string[];
  systemEnabled: boolean;
};
export type Page<T> = {
  items: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};
export type Menu = {
  menuId: number;
  parentMenuId?: number | null;
  menuLevel: string;
  menuName: string;
  screenId?: string;
  url?: string;
  icon?: string;
  businessCategory?: string;
  description?: string;
  displayOrder: number;
  active: boolean;
};
