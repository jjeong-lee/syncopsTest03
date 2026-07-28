export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  error?: {
    code: string;
    message: string;
    fieldErrors?: { field: string; message: string }[];
  } | null;
  requestId: string;
}

export async function apiRequest<T>(
  path: `/api/${string}`,
  options: RequestInit = {},
): Promise<T> {
  if (!path.startsWith("/api/")) {
    throw new Error("API path must be relative and start with /api/");
  }
  const response = await fetch(path, {
    credentials: "include",
    headers: { "Content-Type": "application/json", ...(options.headers ?? {}) },
    ...options,
  });
  const envelope = (await response.json()) as ApiResponse<T>;
  if (!response.ok || !envelope.success) {
    const message =
      envelope.error?.message ??
      envelope.message ??
      "요청 처리 중 오류가 발생했습니다.";
    throw new Error(`${message} (${envelope.requestId ?? "no-request-id"})`);
  }
  return envelope.data;
}

export const api = {
  health: () => apiRequest<{ status: "UP" }>("/api/health"),
  login: (loginId: string, password: string) =>
    apiRequest<CurrentUser>("/api/auth/login", {
      method: "POST",
      body: JSON.stringify({ loginId, password }),
    }),
  me: () => apiRequest<CurrentUser>("/api/auth/me"),
  logout: () => apiRequest<null>("/api/auth/logout", { method: "POST" }),
  list: <T>(path: `/api/${string}`) => apiRequest<T>(path),
  mutate: <T>(
    path: `/api/${string}`,
    method: "POST" | "PATCH" | "PUT",
    body: unknown,
  ) => apiRequest<T>(path, { method, body: JSON.stringify(body) }),
  listRoute: <T>(screenId: string, fallbackPath: `/api/${string}`) => {
    switch (screenId) {
      case "USR-001":
        return apiRequest<T>("/api/users");
      case "ORG-001":
        return apiRequest<T>("/api/organizations");
      case "ROLE-001":
        return apiRequest<T>("/api/roles");
      case "UROLE-001":
        return apiRequest<T>("/api/user-roles");
      case "MPERM-001":
        return apiRequest<T>(
          "/api/menu-permissions?targetType=ROLE&targetId=R09",
        );
      case "MSTRUCT-001":
        return apiRequest<T>("/api/menus/tree");
      case "MINFO-001":
        return apiRequest<T>("/api/menus");
      case "CGRP-001":
        return apiRequest<T>("/api/code-groups");
      case "DCODE-001":
        return apiRequest<T>("/api/detail-codes?groupId=ROLE_SOURCE");
      default:
        return apiRequest<T>(fallbackPath);
    }
  },
};

export const adminApi = {
  updateUserUsage: (userId: string, body: unknown) =>
    apiRequest(`/api/users/${userId}/usage`, {
      method: "PATCH",
      body: JSON.stringify(body),
    }),
  updateOrganizationRelation: (organizationCode: string, body: unknown) =>
    apiRequest(`/api/organizations/${organizationCode}/relation`, {
      method: "PUT",
      body: JSON.stringify(body),
    }),
  createRole: (body: unknown) =>
    apiRequest("/api/roles", { method: "POST", body: JSON.stringify(body) }),
  updateRole: (roleCode: string, body: unknown) =>
    apiRequest(`/api/roles/${roleCode}`, {
      method: "PATCH",
      body: JSON.stringify(body),
    }),
  assignUserRole: (body: unknown) =>
    apiRequest("/api/user-roles", {
      method: "POST",
      body: JSON.stringify(body),
    }),
  revokeUserRole: (userRoleId: string, body: unknown) =>
    apiRequest(`/api/user-roles/${userRoleId}/revoke`, {
      method: "PATCH",
      body: JSON.stringify(body),
    }),
  createMenu: (body: unknown) =>
    apiRequest("/api/menus", { method: "POST", body: JSON.stringify(body) }),
  updateMenu: (menuId: string, body: unknown) =>
    apiRequest(`/api/menus/${menuId}`, {
      method: "PATCH",
      body: JSON.stringify(body),
    }),
  reorderMenus: (body: unknown) =>
    apiRequest("/api/menus/reorder", {
      method: "PATCH",
      body: JSON.stringify(body),
    }),
  saveMenuPermissions: (body: unknown) =>
    apiRequest("/api/menu-permissions", {
      method: "PUT",
      body: JSON.stringify(body),
    }),
  createCodeGroup: (body: unknown) =>
    apiRequest("/api/code-groups", {
      method: "POST",
      body: JSON.stringify(body),
    }),
  updateCodeGroup: (groupId: string, body: unknown) =>
    apiRequest(`/api/code-groups/${groupId}`, {
      method: "PATCH",
      body: JSON.stringify(body),
    }),
  createDetailCode: (body: unknown) =>
    apiRequest("/api/detail-codes", {
      method: "POST",
      body: JSON.stringify(body),
    }),
  updateDetailCode: (detailCodeId: string, body: unknown) =>
    apiRequest(`/api/detail-codes/${detailCodeId}`, {
      method: "PATCH",
      body: JSON.stringify(body),
    }),
};

export interface CurrentUser {
  userId: string;
  loginId: string;
  displayName: string;
  roleCodes: string[];
}
