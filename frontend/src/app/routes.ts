export interface AppRoute {
  screenId: string;
  path: string;
  title: string;
  group: string;
  apiPath: `/api/${string}`;
  archetype:
    | "SEARCH_LIST_DETAIL"
    | "TREE_EDITOR"
    | "PERMISSION_MATRIX"
    | "EFFECTIVE_PERIOD_FORM";
  fields: string[];
}

export const adminRoutes: AppRoute[] = [
  {
    screenId: "USR-001",
    path: "/system/users",
    title: "사용자 관리",
    group: "사용자·조직 관리",
    apiPath: "/api/users",
    archetype: "SEARCH_LIST_DETAIL",
    fields: ["교번", "성명", "소속", "직급", "재직상태", "역할", "사용여부"],
  },
  {
    screenId: "ORG-001",
    path: "/system/organizations",
    title: "조직 관리",
    group: "사용자·조직 관리",
    apiPath: "/api/organizations",
    archetype: "TREE_EDITOR",
    fields: ["조직코드", "조직명", "상위조직", "적용 시작일", "종료일"],
  },
  {
    screenId: "ROLE-001",
    path: "/system/roles",
    title: "역할 관리",
    group: "역할·권한 관리",
    apiPath: "/api/roles",
    archetype: "SEARCH_LIST_DETAIL",
    fields: ["역할코드", "역할명", "목적", "부여기준", "데이터 범위"],
  },
  {
    screenId: "UROLE-001",
    path: "/system/user-roles",
    title: "사용자 역할 관리",
    group: "역할·권한 관리",
    apiPath: "/api/user-roles",
    archetype: "EFFECTIVE_PERIOD_FORM",
    fields: ["교번", "역할코드", "role_source", "유효 시작", "승인자"],
  },
  {
    screenId: "MPERM-001",
    path: "/system/menu-permissions",
    title: "메뉴 권한 관리",
    group: "역할·권한 관리",
    apiPath: "/api/menu-permissions?targetType=ROLE&targetId=R09",
    archetype: "PERMISSION_MATRIX",
    fields: ["대상 유형", "대상ID", "메뉴", "접근허용"],
  },
  {
    screenId: "MSTRUCT-001",
    path: "/system/menu-structure",
    title: "메뉴 구조 관리",
    group: "메뉴 관리",
    apiPath: "/api/menus/tree",
    archetype: "TREE_EDITOR",
    fields: ["메뉴명", "상위메뉴", "표시순서", "사용여부"],
  },
  {
    screenId: "MINFO-001",
    path: "/system/menu-info",
    title: "메뉴 정보 관리",
    group: "메뉴 관리",
    apiPath: "/api/menus",
    archetype: "SEARCH_LIST_DETAIL",
    fields: ["메뉴명", "화면ID", "URL", "아이콘", "업무구분"],
  },
  {
    screenId: "CGRP-001",
    path: "/system/code-groups",
    title: "코드그룹 관리",
    group: "공통코드 관리",
    apiPath: "/api/code-groups",
    archetype: "SEARCH_LIST_DETAIL",
    fields: ["groupID", "명칭", "설명", "관리부서", "사용여부"],
  },
  {
    screenId: "DCODE-001",
    path: "/system/codes",
    title: "상세코드 관리",
    group: "공통코드 관리",
    apiPath: "/api/detail-codes?groupId=ROLE_SOURCE",
    archetype: "EFFECTIVE_PERIOD_FORM",
    fields: [
      "groupID",
      "code_value",
      "code_name",
      "상위코드",
      "정렬순서",
      "유효기간",
    ],
  },
];

export function currentRoute(pathname = window.location.pathname): AppRoute {
  return adminRoutes.find((route) => route.path === pathname) ?? adminRoutes[0];
}

export function routeByScreenId(screenId: AppRoute["screenId"]): AppRoute {
  return (
    adminRoutes.find((route) => route.screenId === screenId) ?? adminRoutes[0]
  );
}
