export type ApiResponse<T> = { success: boolean; data: T };
export type ApiError = {
  code: string;
  message: string;
  fieldErrors?: Record<string, string>;
};

export async function apiRequest<T>(
  path: string,
  init?: RequestInit,
): Promise<T> {
  if (!path.startsWith("/api/"))
    throw new Error("API 경로는 /api/... 상대경로여야 합니다.");
  const response = await fetch(path, {
    credentials: "include",
    headers: { "Content-Type": "application/json", ...(init?.headers ?? {}) },
    ...init,
  });
  const envelope = (await response.json()) as ApiResponse<T | ApiError>;
  if (!response.ok || !envelope.success) {
    const error = envelope.data as ApiError;
    throw Object.assign(new Error(error.message), {
      status: response.status,
      apiError: error,
    });
  }
  return envelope.data as T;
}

export const endpoints = {
  login: "/api/auth/login",
  me: "/api/auth/me",
  logout: "/api/auth/logout",
  menus: "/api/menus/current",
  users: "/api/users",
  organizations: "/api/organizations",
  organizationTree: "/api/organizations/tree",
  roles: "/api/roles",
  userRoles: "/api/user-roles",
  menuPermissions: "/api/menu-permissions",
  menuTree: "/api/menus/tree",
  codeGroups: "/api/code-groups",
  health: "/api/health",
} as const;
