import { apiRequest, toQuery } from "./client";
import type {
  CodeGroup,
  CurrentUser,
  DetailCode,
  Menu,
  MenuPermission,
  Organization,
  Page,
  Role,
  UserRole,
  UserSummary,
} from "../types";

export const authApi = {
  login: (username: string, password: string) =>
    apiRequest<CurrentUser>("/api/auth/login", {
      method: "POST",
      body: JSON.stringify({ username, password }),
    }),
  logout: () => apiRequest<void>("/api/auth/logout", { method: "POST" }),
  me: () => apiRequest<CurrentUser>("/api/auth/me"),
};

export const userApi = {
  search: (params: Record<string, string>) =>
    apiRequest<Page<UserSummary>>(`/api/users${toQuery(params)}`),
  updateUsage: (userId: string, systemUseYn: string, changeReason: string) =>
    apiRequest<UserSummary>(`/api/users/${userId}/usage`, {
      method: "PATCH",
      body: JSON.stringify({ systemUseYn, changeReason }),
    }),
  updateRoles: (
    userId: string,
    roleCodes: string[],
    approvedBy: string,
    validFrom: string,
    changeReason: string,
  ) =>
    apiRequest<UserSummary>(`/api/users/${userId}/roles`, {
      method: "PUT",
      body: JSON.stringify({ roleCodes, approvedBy, validFrom, changeReason }),
    }),
};
export const organizationApi = {
  search: (params: Record<string, string>) =>
    apiRequest<Page<Organization>>(`/api/organizations${toQuery(params)}`),
  tree: () => apiRequest<Organization[]>("/api/organizations/tree"),
  updateRelation: (organizationCode: string, body: Record<string, string>) =>
    apiRequest<Organization>(
      `/api/organizations/${organizationCode}/relation`,
      { method: "PUT", body: JSON.stringify(body) },
    ),
};
export const roleApi = {
  list: (filter = "") => apiRequest<Role[]>(`/api/roles${toQuery({ filter })}`),
  create: (body: Role) =>
    apiRequest<Role>(`/api/roles/${body.roleCode}`, {
      method: "PUT",
      body: JSON.stringify(body),
    }),
  update: (roleCode: string, body: Role) =>
    apiRequest<Role>(`/api/roles/${roleCode}`, {
      method: "PUT",
      body: JSON.stringify(body),
    }),
};
export const userRoleApi = {
  list: (params: Record<string, string>) =>
    apiRequest<Page<UserRole>>(`/api/user-roles${toQuery(params)}`),
  grant: (body: Record<string, string>) =>
    apiRequest<UserRole>("/api/user-roles/grants", {
      method: "POST",
      body: JSON.stringify(body),
    }),
  revoke: (body: Record<string, string | number>) =>
    apiRequest<UserRole>("/api/user-roles/revocations", {
      method: "POST",
      body: JSON.stringify(body),
    }),
};
export const menuApi = {
  tree: () => apiRequest<Menu[]>("/api/menus/tree"),
  updateHierarchy: (
    items: Array<{
      menuId: string;
      parentMenuId?: string;
      displayOrder: number;
    }>,
  ) =>
    apiRequest<Menu[]>("/api/menus/hierarchy", {
      method: "PUT",
      body: JSON.stringify({ items }),
    }),
  list: (params: Record<string, string>) =>
    apiRequest<Page<Menu>>(`/api/menus${toQuery(params)}`),
  create: (body: Partial<Menu>) =>
    apiRequest<Menu>(`/api/menus/${body.menuId}`, {
      method: "PUT",
      body: JSON.stringify(body),
    }),
  update: (menuId: string, body: Partial<Menu>) =>
    apiRequest<Menu>(`/api/menus/${menuId}`, {
      method: "PUT",
      body: JSON.stringify(body),
    }),
};
export const menuPermissionApi = {
  list: (params: Record<string, string>) =>
    apiRequest<Page<MenuPermission>>(`/api/menu-permissions${toQuery(params)}`),
  save: (targetType: string, targetId: string, permissions: MenuPermission[]) =>
    apiRequest<{ savedCount: number }>("/api/menu-permissions", {
      method: "PUT",
      body: JSON.stringify({ targetType, targetId, permissions }),
    }),
};
export const codeGroupApi = {
  list: (params: Record<string, string>) =>
    apiRequest<Page<CodeGroup>>(`/api/code-groups${toQuery(params)}`),
  create: (body: CodeGroup) =>
    apiRequest<CodeGroup>(`/api/code-groups/${body.groupId}`, {
      method: "PUT",
      body: JSON.stringify(body),
    }),
  update: (groupId: string, body: CodeGroup) =>
    apiRequest<CodeGroup>(`/api/code-groups/${groupId}`, {
      method: "PUT",
      body: JSON.stringify(body),
    }),
};
export const detailCodeApi = {
  list: (groupId: string, params: Record<string, string>) =>
    apiRequest<Page<DetailCode>>(
      `/api/code-groups/${groupId}/codes${toQuery(params)}`,
    ),
  create: (groupId: string, body: DetailCode) =>
    apiRequest<DetailCode>(
      `/api/code-groups/${groupId}/codes/${body.codeValue}`,
      {
        method: "PUT",
        body: JSON.stringify(body),
      },
    ),
  update: (groupId: string, codeValue: string, body: DetailCode) =>
    apiRequest<DetailCode>(`/api/code-groups/${groupId}/codes/${codeValue}`, {
      method: "PUT",
      body: JSON.stringify(body),
    }),
};
