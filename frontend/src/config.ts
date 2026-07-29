import { endpoints } from "./services/apiClient";
import type { ScreenConfig } from "./types";

export const screens: ScreenConfig[] = [
  {
    id: "SYSTEM-SMOKE-DASHBOARD",
    route: "/system",
    title: "시스템 관리 대시보드",
    description:
      "백엔드 health, 메뉴 seed, 인증 상태를 한 화면에서 확인합니다.",
    endpoint: endpoints.health,
    kind: "dashboard",
    archetype: "DASHBOARD",
    menuPath: "시스템 관리 > 대시보드 > 검증 대시보드",
    primaryEntity: "service_health",
  },
  {
    id: "CMN-USER-MGMT",
    route: "/system/users",
    title: "사용자 관리",
    description:
      "KORUS 원천 사용자 정보를 조회하고 시스템 사용여부와 업무 역할을 관리합니다.",
    endpoint: endpoints.users,
    kind: "users",
    archetype: "SEARCH_LIST_DETAIL",
    menuPath: "시스템 관리 > 사용자·조직 관리 > 사용자 관리",
    primaryEntity: "users",
  },
  {
    id: "CMN-ORG-MGMT",
    route: "/system/organizations",
    title: "조직 관리",
    description: "기준일별 조직 계층을 조회하고 관계와 적용기간을 저장합니다.",
    endpoint: endpoints.organizationTree,
    kind: "organizations",
    archetype: "TREE_EDITOR",
    menuPath: "시스템 관리 > 사용자·조직 관리 > 조직 관리",
    primaryEntity: "organizations",
  },
  {
    id: "CMN-ROLE-MGMT",
    route: "/system/roles",
    title: "역할 관리",
    description: "R01~R09 역할 기준정보와 사용여부를 관리합니다.",
    endpoint: endpoints.roles,
    kind: "roles",
    archetype: "SEARCH_LIST_DETAIL",
    menuPath: "시스템 관리 > 역할·권한 관리 > 역할 관리",
    primaryEntity: "roles",
  },
  {
    id: "CMN-USER-ROLE-MGMT",
    route: "/system/user-roles",
    title: "사용자 역할 관리",
    description:
      "사용자별 현재 역할과 유효기간 기반 역할 부여·회수 상태를 확인합니다.",
    endpoint: endpoints.userRoles,
    kind: "userRoles",
    archetype: "EFFECTIVE_PERIOD_FORM",
    menuPath: "시스템 관리 > 역할·권한 관리 > 사용자 역할 관리",
    primaryEntity: "user_roles",
  },
  {
    id: "CMN-MENU-AUTH-MGMT",
    route: "/system/menu-permissions",
    title: "메뉴 권한 관리",
    description:
      "역할·조직·사용자 대상 메뉴 접근 matrix와 최종 판정 preview를 관리합니다.",
    endpoint: endpoints.menuPermissions,
    kind: "menuPermissions",
    archetype: "PERMISSION_MATRIX",
    menuPath: "시스템 관리 > 역할·권한 관리 > 메뉴 권한 관리",
    primaryEntity: "menu_permissions",
  },
  {
    id: "CMN-MENU-STRUCT-MGMT",
    route: "/system/menu-structure",
    title: "메뉴 구조 관리",
    description:
      "메뉴 tree의 parent, sortOrder, status를 기준정보로 관리합니다.",
    endpoint: endpoints.menuTree,
    kind: "menuStructure",
    archetype: "TREE_EDITOR",
    menuPath: "시스템 관리 > 메뉴 관리 > 메뉴 구조 관리",
    primaryEntity: "menus",
  },
  {
    id: "CMN-MENU-INFO-MGMT",
    route: "/system/menu-info",
    title: "메뉴 정보 관리",
    description:
      "메뉴명, screenId, frontend route, icon, 업무구분 실행정보를 연결합니다.",
    endpoint: endpoints.menuTree,
    kind: "menuInfo",
    archetype: "CONTENT_EDITOR",
    menuPath: "시스템 관리 > 메뉴 관리 > 메뉴 정보 관리",
    primaryEntity: "menus",
  },
  {
    id: "CMN-CODE-GROUP-MGMT",
    route: "/system/code-groups",
    title: "코드그룹 관리",
    description: "공통코드 group master를 등록·수정하고 상세코드로 이동합니다.",
    endpoint: endpoints.codeGroups,
    kind: "codeGroups",
    archetype: "SEARCH_LIST_DETAIL",
    menuPath: "시스템 관리 > 공통코드 관리 > 코드그룹 관리",
    primaryEntity: "code_groups",
  },
  {
    id: "CMN-DETAIL-CODE-MGMT",
    route: "/system/code-groups/:groupId/detail-codes",
    title: "상세코드 관리",
    description:
      "선택된 코드그룹의 상세코드 계층, 유효기간, 추가속성을 관리합니다.",
    endpoint: "/api/code-groups/:groupId/detail-codes",
    kind: "detailCodes",
    archetype: "TREE_EDITOR",
    menuPath: "시스템 관리 > 공통코드 관리 > 상세코드 관리",
    primaryEntity: "detail_codes",
  },
];

export const navGroups = [
  {
    label: "대시보드",
    items: [screens[0]],
  },
  {
    label: "사용자·조직 관리",
    items: [screens[1], screens[2]],
  },
  {
    label: "역할·권한 관리",
    items: [screens[3], screens[4], screens[5]],
  },
  {
    label: "메뉴 관리",
    items: [screens[6], screens[7]],
  },
  {
    label: "공통코드 관리",
    items: [screens[8], screens[9]],
  },
];

export function getDefaultDetailCodeRoute() {
  return "/system/code-groups/USER_STATUS/detail-codes";
}
