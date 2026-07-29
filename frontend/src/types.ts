export type Menu = {
  menuId: string;
  parentMenuId?: string;
  menuName: string;
  screenId?: string;
  url?: string;
  level: string;
};

export type Page<T> = { content: T[]; totalElements: number };

export type Role = {
  roleCode: string;
  roleName: string;
  purpose?: string;
  grantCriteria?: string;
  defaultDataScope?: string;
  useYn: string;
};

export type User = {
  userId: string;
  staffNo: string;
  staffName: string;
  organizationName: string;
  rankName: string;
  employmentStatus: string;
  positionName: string;
  retirementDate?: string;
  lastSyncedAt: string;
  systemUseYn: string;
  roleCodes?: string[];
};

export type UiState = "loading" | "success" | "empty" | "error" | "permission";

export type ScreenKind =
  | "dashboard"
  | "users"
  | "organizations"
  | "roles"
  | "userRoles"
  | "menuPermissions"
  | "menuStructure"
  | "menuInfo"
  | "codeGroups"
  | "detailCodes";

export type ScreenConfig = {
  id: string;
  route: string;
  title: string;
  description: string;
  endpoint: string;
  kind: ScreenKind;
  archetype: string;
  menuPath: string;
  primaryEntity: string;
};
