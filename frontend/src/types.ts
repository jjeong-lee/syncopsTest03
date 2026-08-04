export type UseYn = "Y" | "N";
export type RoleCode =
  | "R01"
  | "R02"
  | "R03"
  | "R04"
  | "R05"
  | "R06"
  | "R07"
  | "R08"
  | "R09";

export interface ApiErrorField {
  field: string;
  message: string;
}
export interface ApiError {
  code: string;
  message: string;
  fields?: ApiErrorField[];
}
export interface ApiResponse<T> {
  success: boolean;
  data?: T;
  error?: ApiError;
  timestamp?: string;
}
export interface Page<T> {
  items: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}
export interface Menu {
  menuId: string;
  parentMenuId?: string;
  menuLevel: number;
  displayOrder: number;
  menuName: string;
  screenId?: string;
  url?: string;
  icon?: string;
  businessType?: string;
  description?: string;
  activeYn: UseYn;
}
export interface CurrentUser {
  userId: string;
  username: string;
  roles: RoleCode[];
  menus: Menu[];
}
export interface UserSummary {
  userId: string;
  staffNo: string;
  staffName: string;
  organizationCode: string;
  organizationName?: string;
  positionName?: string;
  employmentStatus: string;
  dutyName?: string;
  retirementDate?: string;
  lastSyncedAt: string;
  systemUseYn: UseYn;
  roles: RoleCode[];
}
export interface Organization {
  organizationCode: string;
  organizationName: string;
  organizationType: string;
  parentOrganizationCode?: string;
  effectiveStartDate: string;
  effectiveEndDate?: string;
  relationChangeReason?: string;
  useYn: UseYn;
}
export interface Role {
  roleCode: RoleCode;
  roleName: string;
  purpose: string;
  grantCriteria: string;
  dataScopeDefault: string;
  useYn: UseYn;
}
export interface UserRole {
  userRoleId: number;
  userId: string;
  roleCode: RoleCode;
  assignmentType: string;
  validFrom: string;
  validTo?: string;
  approvedBy: string;
  useYn: UseYn;
}
export interface MenuPermission {
  permissionId?: number;
  targetType: string;
  targetId: string;
  menuId: string;
  accessAllowedYn: UseYn;
  explicitDenyYn: UseYn;
}
export interface CodeGroup {
  groupId: string;
  groupName: string;
  description?: string;
  managementDepartment: string;
  useYn: UseYn;
}
export interface DetailCode {
  groupId: string;
  codeValue: string;
  codeName: string;
  parentCodeValue?: string;
  sortOrder: number;
  extraAttributes?: Record<string, unknown>;
  useYn: UseYn;
  validFrom?: string;
  validTo?: string;
}
export type LoadState =
  | "idle"
  | "loading"
  | "empty"
  | "error"
  | "permission"
  | "success";
