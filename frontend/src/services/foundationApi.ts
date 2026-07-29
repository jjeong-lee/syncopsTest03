import { apiRequest } from "./apiClient";

export function login(loginId: string, password: string) {
  return apiRequest("/api/auth/login", {
    method: "POST",
    body: JSON.stringify({ loginId, password }),
  });
}

export function getCurrentUser() {
  return apiRequest("/api/auth/me");
}

export function logout() {
  return apiRequest("/api/auth/logout", { method: "POST" });
}

export function listCurrentMenus(query = "") {
  return apiRequest(`/api/menus/current${query}`);
}

export function searchUsers(query = "") {
  return apiRequest(`/api/users${query}`);
}

export function updateUserAdministration(
  userId: string,
  body: unknown,
  changeReason = "",
) {
  return apiRequest(
    `/api/users/${encodeURIComponent(userId)}/administration${changeReason}`,
    {
      method: "PATCH",
      body: JSON.stringify(body),
    },
  );
}

export function listOrganizations(query = "") {
  return apiRequest(`/api/organizations${query}`);
}

export function getOrganizationTree(query = "") {
  return apiRequest(`/api/organizations/tree${query}`);
}

export function saveOrganizationRelationship(
  organizationId: string,
  body: unknown,
  changeReason = "",
) {
  return apiRequest(
    `/api/organizations/${encodeURIComponent(organizationId)}/relationship${changeReason}`,
    {
      method: "PUT",
      body: JSON.stringify(body),
    },
  );
}

export function listRoles(query = "") {
  return apiRequest(`/api/roles${query}`);
}

export function updateRole(roleCode: string, body: unknown, changeReason = "") {
  return apiRequest(
    `/api/roles/${encodeURIComponent(roleCode)}${changeReason}`,
    {
      method: "PUT",
      body: JSON.stringify(body),
    },
  );
}

export function listUserRoles(query = "") {
  return apiRequest(`/api/user-roles${query}`);
}

export function assignUserRoles(body: unknown, changeReason = "") {
  return apiRequest(`/api/user-roles/assignments${changeReason}`, {
    method: "POST",
    body: JSON.stringify(body),
  });
}

export function revokeUserRole(
  userRoleId: string,
  body: unknown,
  changeReason = "",
) {
  return apiRequest(
    `/api/user-roles/${encodeURIComponent(userRoleId)}/revoke${changeReason}`,
    {
      method: "POST",
      body: JSON.stringify(body),
    },
  );
}

export function listMenuPermissions(query = "") {
  return apiRequest(`/api/menu-permissions${query}`);
}

export function saveMenuPermissions(body: unknown, changeReason = "") {
  return apiRequest(`/api/menu-permissions/bulk${changeReason}`, {
    method: "PUT",
    body: JSON.stringify(body),
  });
}

export function getMenuTree(query = "") {
  return apiRequest(`/api/menus/tree${query}`);
}

export function saveMenuStructure(
  menuId: string,
  body: unknown,
  changeReason = "",
) {
  return apiRequest(
    `/api/menus/${encodeURIComponent(menuId)}/structure${changeReason}`,
    {
      method: "PUT",
      body: JSON.stringify(body),
    },
  );
}

export function reorderMenus(body: unknown, changeReason = "") {
  return apiRequest(`/api/menus/reorder${changeReason}`, {
    method: "PUT",
    body: JSON.stringify(body),
  });
}

export function saveMenuInfo(menuId: string, body: unknown, changeReason = "") {
  return apiRequest(
    `/api/menus/${encodeURIComponent(menuId)}/info${changeReason}`,
    {
      method: "PUT",
      body: JSON.stringify(body),
    },
  );
}

export function listCodeGroups(query = "") {
  return apiRequest(`/api/code-groups${query}`);
}

export function createCodeGroup(body: unknown, changeReason = "") {
  return apiRequest(`/api/code-groups${changeReason}`, {
    method: "POST",
    body: JSON.stringify(body),
  });
}

export function updateCodeGroup(
  groupId: string,
  body: unknown,
  changeReason = "",
) {
  return apiRequest(
    `/api/code-groups/${encodeURIComponent(groupId)}${changeReason}`,
    {
      method: "PUT",
      body: JSON.stringify(body),
    },
  );
}

export function listDetailCodes(groupId: string, query = "") {
  return apiRequest(
    `/api/code-groups/${encodeURIComponent(groupId)}/detail-codes${query}`,
  );
}

export function createDetailCode(
  groupId: string,
  body: unknown,
  changeReason = "",
) {
  return apiRequest(
    `/api/code-groups/${encodeURIComponent(groupId)}/detail-codes${changeReason}`,
    {
      method: "POST",
      body: JSON.stringify(body),
    },
  );
}

export function updateDetailCode(
  detailCodeId: string,
  body: unknown,
  changeReason = "",
) {
  return apiRequest(
    `/api/detail-codes/${encodeURIComponent(detailCodeId)}${changeReason}`,
    {
      method: "PUT",
      body: JSON.stringify(body),
    },
  );
}
