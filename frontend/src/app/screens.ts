export type FieldKind =
  | "text"
  | "number"
  | "date"
  | "boolean"
  | "select"
  | "json"
  | "roles";

export type FieldConfig = {
  key: string;
  label: string;
  kind?: FieldKind;
  readonly?: boolean;
  options?: string[];
  placeholder?: string;
  required?: boolean;
};

export type ScreenKind =
  | "users"
  | "organizations"
  | "roles"
  | "userRoles"
  | "permissions"
  | "menuStructure"
  | "menuInfo"
  | "codeGroups"
  | "detailCodes";

export type ScreenConfig = {
  id: string;
  title: string;
  route: string;
  menuPath: string;
  group: string;
  listPath: string;
  kind: ScreenKind;
  operationLabel: string;
  primaryEntity: string;
  filters: FieldConfig[];
  columns: FieldConfig[];
  formFields: FieldConfig[];
};

export const roleCodes = [
  "R01",
  "R02",
  "R03",
  "R04",
  "R05",
  "R06",
  "R07",
  "R08",
  "R09",
];
export const activeOptions = ["true", "false"];
export const screens: ScreenConfig[] = [
  {
    id: "USER_MANAGEMENT",
    title: "사용자 관리",
    route: "/system/users",
    menuPath: "시스템 관리 > 사용자·조직 관리 > 사용자 관리",
    group: "사용자·조직 관리",
    listPath: "/api/admin/users",
    kind: "users",
    operationLabel: "listUsers · updateUserUsage · replaceUserRoles",
    primaryEntity: "user_accounts, korus_staff_snapshots, user_roles",
    filters: [
      { key: "staffNo", label: "교번" },
      { key: "staffName", label: "성명" },
      { key: "organizationCode", label: "소속코드" },
      { key: "positionName", label: "직급/보직" },
      { key: "employmentStatus", label: "재직상태" },
      { key: "roleCode", label: "역할", kind: "select", options: roleCodes },
      {
        key: "systemEnabled",
        label: "사용여부",
        kind: "select",
        options: activeOptions,
      },
    ],
    columns: [
      { key: "staffNo", label: "교번" },
      { key: "staffName", label: "성명" },
      { key: "organizationName", label: "소속" },
      { key: "positionName", label: "직급/보직" },
      { key: "employmentStatus", label: "재직상태" },
      { key: "roleCodes", label: "역할" },
      { key: "systemEnabled", label: "사용여부", kind: "boolean" },
      { key: "retirementDate", label: "퇴직일자" },
      { key: "lastSyncedAt", label: "최종 동기화" },
    ],
    formFields: [
      { key: "staffNo", label: "교번", readonly: true },
      { key: "staffName", label: "성명", readonly: true },
      { key: "organizationName", label: "소속", readonly: true },
      { key: "positionName", label: "직급/보직", readonly: true },
      { key: "employmentStatus", label: "재직상태", readonly: true },
      { key: "systemEnabled", label: "시스템 사용여부", kind: "boolean" },
      { key: "roleCodes", label: "업무 역할", kind: "roles" },
      { key: "retirementDate", label: "퇴직일자", readonly: true },
      { key: "lastSyncedAt", label: "최종 동기화", readonly: true },
    ],
  },
  {
    id: "ORGANIZATION_MANAGEMENT",
    title: "조직 관리",
    route: "/system/organizations",
    menuPath: "시스템 관리 > 사용자·조직 관리 > 조직 관리",
    group: "사용자·조직 관리",
    listPath: "/api/admin/organizations",
    kind: "organizations",
    operationLabel: "listOrganizations · updateOrganizationRelationships",
    primaryEntity: "organizations",
    filters: [
      { key: "organizationCode", label: "조직코드" },
      { key: "organizationName", label: "조직명" },
      { key: "organizationType", label: "조직유형" },
      {
        key: "active",
        label: "사용여부",
        kind: "select",
        options: activeOptions,
      },
    ],
    columns: [
      { key: "organizationCode", label: "조직코드" },
      { key: "organizationName", label: "조직명" },
      { key: "organizationType", label: "조직유형" },
      { key: "parentOrganizationCode", label: "상위조직" },
      { key: "effectiveStartDate", label: "적용 시작" },
      { key: "effectiveEndDate", label: "적용 종료" },
      { key: "active", label: "사용여부", kind: "boolean" },
    ],
    formFields: [
      { key: "organizationCode", label: "조직코드", readonly: true },
      { key: "organizationName", label: "조직명", readonly: true },
      { key: "parentOrganizationCode", label: "상위조직코드" },
      {
        key: "effectiveStartDate",
        label: "적용 시작일",
        kind: "date",
        required: true,
      },
      { key: "effectiveEndDate", label: "적용 종료일", kind: "date" },
    ],
  },
  {
    id: "ROLE_MANAGEMENT",
    title: "역할 관리",
    route: "/system/roles",
    menuPath: "시스템 관리 > 역할·권한 관리 > 역할 관리",
    group: "역할·권한 관리",
    listPath: "/api/admin/roles",
    kind: "roles",
    operationLabel: "listRoles · createRole · updateRole",
    primaryEntity: "roles",
    filters: [
      {
        key: "roleCode",
        label: "역할코드",
        kind: "select",
        options: roleCodes,
      },
      { key: "roleName", label: "역할명" },
      {
        key: "active",
        label: "사용여부",
        kind: "select",
        options: activeOptions,
      },
    ],
    columns: [
      { key: "roleCode", label: "역할코드" },
      { key: "roleName", label: "역할명" },
      { key: "purpose", label: "목적" },
      { key: "grantCriteria", label: "부여 기준" },
      { key: "defaultDataScope", label: "데이터 범위" },
      { key: "active", label: "사용여부", kind: "boolean" },
    ],
    formFields: [
      {
        key: "roleCode",
        label: "역할코드",
        kind: "select",
        options: roleCodes,
        required: true,
      },
      { key: "roleName", label: "역할명", required: true },
      { key: "purpose", label: "목적", required: true },
      { key: "grantCriteria", label: "부여 기준" },
      { key: "defaultDataScope", label: "데이터 범위 기본값" },
    ],
  },
  {
    id: "USER_ROLE_MANAGEMENT",
    title: "사용자 역할 관리",
    route: "/system/user-roles",
    menuPath: "시스템 관리 > 역할·권한 관리 > 사용자 역할 관리",
    group: "역할·권한 관리",
    listPath: "/api/admin/user-roles",
    kind: "userRoles",
    operationLabel: "listUserRoleAssignments · grantUserRole · revokeUserRole",
    primaryEntity: "user_roles",
    filters: [
      { key: "userId", label: "사용자ID" },
      { key: "roleCode", label: "역할", kind: "select", options: roleCodes },
      {
        key: "status",
        label: "상태",
        kind: "select",
        options: ["ACTIVE", "REVOKED", "EXPIRED"],
      },
    ],
    columns: [
      { key: "assignmentId", label: "assignment_id" },
      { key: "userId", label: "사용자ID" },
      { key: "roleCode", label: "역할코드" },
      { key: "assignmentSource", label: "부여구분" },
      { key: "effectiveStartDate", label: "시작일" },
      { key: "effectiveEndDate", label: "종료일" },
      { key: "approvedBy", label: "승인자" },
      { key: "status", label: "상태" },
    ],
    formFields: [
      { key: "userId", label: "사용자ID", required: true },
      {
        key: "roleCode",
        label: "역할코드",
        kind: "select",
        options: roleCodes,
        required: true,
      },
      {
        key: "assignmentSource",
        label: "부여구분",
        kind: "select",
        options: ["POSITION_BASED", "MANUAL"],
        required: true,
      },
      {
        key: "effectiveStartDate",
        label: "유효 시작일",
        kind: "date",
        required: true,
      },
      { key: "effectiveEndDate", label: "유효 종료일", kind: "date" },
      { key: "approvedBy", label: "승인자", required: true },
      { key: "status", label: "상태", readonly: true },
    ],
  },
  {
    id: "MENU_PERMISSION_MANAGEMENT",
    title: "메뉴 권한 관리",
    route: "/system/menu-permissions",
    menuPath: "시스템 관리 > 역할·권한 관리 > 메뉴 권한 관리",
    group: "역할·권한 관리",
    listPath: "/api/admin/menu-permissions",
    kind: "permissions",
    operationLabel: "listMenuPermissions · saveMenuPermissions",
    primaryEntity: "menu_permissions, menus",
    filters: [
      {
        key: "principalType",
        label: "principal_type",
        kind: "select",
        options: ["ROLE", "ORGANIZATION", "USER"],
      },
      { key: "principalId", label: "principal_id", placeholder: "R09" },
      { key: "menuId", label: "menu_id", kind: "number" },
    ],
    columns: [
      { key: "permissionId", label: "permission_id" },
      { key: "principalType", label: "대상 유형" },
      { key: "principalId", label: "대상 ID" },
      { key: "menuId", label: "menu_id" },
      { key: "permissionEffect", label: "접근 효과" },
      { key: "active", label: "사용여부", kind: "boolean" },
    ],
    formFields: [
      {
        key: "principalType",
        label: "대상 유형",
        kind: "select",
        options: ["ROLE", "ORGANIZATION", "USER"],
        required: true,
      },
      { key: "principalId", label: "대상 ID", required: true },
      { key: "menuId", label: "menu_id", kind: "number", required: true },
      {
        key: "permissionEffect",
        label: "접근 효과",
        kind: "select",
        options: ["ALLOW", "DENY"],
        required: true,
      },
    ],
  },
  {
    id: "MENU_STRUCTURE_MANAGEMENT",
    title: "메뉴 구조 관리",
    route: "/system/menu-structure",
    menuPath: "시스템 관리 > 메뉴 관리 > 메뉴 구조 관리",
    group: "메뉴 관리",
    listPath: "/api/admin/menus/tree",
    kind: "menuStructure",
    operationLabel: "getMenuTree · updateMenuStructure",
    primaryEntity: "menus",
    filters: [],
    columns: [
      { key: "menuId", label: "menu_id" },
      { key: "parentMenuId", label: "parent_menu_id" },
      { key: "menuLevel", label: "레벨" },
      { key: "menuName", label: "메뉴명" },
      { key: "displayOrder", label: "순서", kind: "number" },
      { key: "active", label: "사용여부", kind: "boolean" },
    ],
    formFields: [
      { key: "menuId", label: "menu_id", readonly: true },
      { key: "parentMenuId", label: "parent_menu_id", kind: "number" },
      { key: "menuLevel", label: "레벨", readonly: true },
      { key: "menuName", label: "메뉴명", readonly: true },
      {
        key: "displayOrder",
        label: "표시순서",
        kind: "number",
        required: true,
      },
    ],
  },
  {
    id: "MENU_INFO_MANAGEMENT",
    title: "메뉴 정보 관리",
    route: "/system/menu-info",
    menuPath: "시스템 관리 > 메뉴 관리 > 메뉴 정보 관리",
    group: "메뉴 관리",
    listPath: "/api/admin/menus",
    kind: "menuInfo",
    operationLabel: "listMenus · createMenu · updateMenu",
    primaryEntity: "menus",
    filters: [
      { key: "menuName", label: "메뉴명" },
      { key: "screenId", label: "화면ID" },
      { key: "url", label: "URL" },
      {
        key: "active",
        label: "사용여부",
        kind: "select",
        options: activeOptions,
      },
    ],
    columns: [
      { key: "menuId", label: "menu_id" },
      { key: "menuName", label: "메뉴명" },
      { key: "screenId", label: "화면ID" },
      { key: "url", label: "URL" },
      { key: "icon", label: "아이콘" },
      { key: "businessCategory", label: "업무구분" },
      { key: "active", label: "사용여부", kind: "boolean" },
    ],
    formFields: [
      { key: "menuId", label: "menu_id", readonly: true },
      { key: "parentMenuId", label: "parent_menu_id", kind: "number" },
      {
        key: "menuLevel",
        label: "레벨",
        kind: "select",
        options: ["TOP", "MIDDLE", "SCREEN"],
        required: true,
      },
      { key: "menuName", label: "메뉴명", required: true },
      { key: "screenId", label: "화면ID" },
      { key: "url", label: "URL" },
      { key: "icon", label: "아이콘" },
      { key: "businessCategory", label: "업무구분" },
      { key: "description", label: "설명" },
      {
        key: "displayOrder",
        label: "표시순서",
        kind: "number",
        required: true,
      },
    ],
  },
  {
    id: "CODE_GROUP_MANAGEMENT",
    title: "코드그룹 관리",
    route: "/system/code-groups",
    menuPath: "시스템 관리 > 공통코드 관리 > 코드그룹 관리",
    group: "공통코드 관리",
    listPath: "/api/admin/code-groups",
    kind: "codeGroups",
    operationLabel: "listCodeGroups · createCodeGroup · updateCodeGroup",
    primaryEntity: "code_groups",
    filters: [
      { key: "groupId", label: "그룹ID" },
      { key: "groupName", label: "명칭" },
      { key: "managingDepartment", label: "관리부서" },
      {
        key: "active",
        label: "사용여부",
        kind: "select",
        options: activeOptions,
      },
    ],
    columns: [
      { key: "groupId", label: "그룹ID" },
      { key: "groupName", label: "명칭" },
      { key: "description", label: "설명" },
      { key: "managingDepartment", label: "관리부서" },
      { key: "active", label: "사용여부", kind: "boolean" },
    ],
    formFields: [
      { key: "groupId", label: "그룹ID", required: true },
      { key: "groupName", label: "명칭", required: true },
      { key: "description", label: "설명" },
      { key: "managingDepartment", label: "관리부서" },
    ],
  },
  {
    id: "DETAIL_CODE_MANAGEMENT",
    title: "상세코드 관리",
    route: "/system/code-groups/:groupId/codes",
    menuPath: "시스템 관리 > 공통코드 관리 > 상세코드 관리",
    group: "공통코드 관리",
    listPath: "/api/admin/code-groups/{groupId}/codes",
    kind: "detailCodes",
    operationLabel: "listDetailCodes · createDetailCode · updateDetailCode",
    primaryEntity: "detail_codes, code_groups",
    filters: [
      { key: "codeValue", label: "코드값" },
      { key: "codeName", label: "코드명" },
      {
        key: "active",
        label: "사용여부",
        kind: "select",
        options: activeOptions,
      },
    ],
    columns: [
      { key: "codeValue", label: "코드값" },
      { key: "codeName", label: "코드명" },
      { key: "parentCodeValue", label: "상위코드" },
      { key: "sortOrder", label: "정렬순서", kind: "number" },
      { key: "additionalAttributes", label: "추가속성", kind: "json" },
      { key: "validFrom", label: "유효시작" },
      { key: "validTo", label: "유효종료" },
      { key: "active", label: "사용여부", kind: "boolean" },
    ],
    formFields: [
      { key: "codeValue", label: "코드값", required: true },
      { key: "codeName", label: "코드명", required: true },
      { key: "parentCodeValue", label: "상위코드" },
      { key: "sortOrder", label: "정렬순서", kind: "number", required: true },
      { key: "additionalAttributes", label: "추가속성 JSON", kind: "json" },
      { key: "validFrom", label: "유효 시작일", kind: "date" },
      { key: "validTo", label: "유효 종료일", kind: "date" },
    ],
  },
];

export function screenById(id: string) {
  const screen = screens.find((item) => item.id === id);
  if (!screen) throw new Error(`Unknown screen ${id}`);
  return screen;
}
