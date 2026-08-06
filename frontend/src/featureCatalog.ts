export type FieldContract = {
  key: string;
  label: string;
  readOnly?: boolean;
};

export type MutationMethod = "GET" | "POST" | "PATCH" | "PUT" | "DELETE";

export type ScreenContract = {
  route: string;
  screenId: string;
  menuPath: string;
  role: string;
  primaryEntity: string;
  operationId: string;
  apiPath: string;
  requirements: string[];
  archetype: string;
  goal: string;
  filters: FieldContract[];
  columns: FieldContract[];
  modalFields: FieldContract[];
  primaryActions: string[];
  constraint: string;
  readOnly: boolean;
  mutationMethod: MutationMethod;
};

export const screens = [
  {
    route: "/admin/cmn/fr/001",
    screenId: "SCR-CMN-FR-001",
    menuPath: "시스템 관리 > 사용자·조직 관리 > 사용자 관리",
    role: "R09 시스템관리자",
    primaryEntity: "user_account",
    operationId: "cmn_fr_001_search",
    apiPath: "/api/admin/cmn/fr/001",
    requirements: ["REQ-001", "REQ-002", "REQ-003"],
    archetype: "시스템 관리 > 사용자·조직 관리 > 사용자 관리",
    goal: "사용자 관리에서 source-backed 조회/명령/검증 결과를 확인한다.",
    filters: [
      {
        key: "employeeNo",
        label: "교번",
        readOnly: false,
      },
      {
        key: "userName",
        label: "성명",
        readOnly: false,
      },
      {
        key: "departmentName",
        label: "소속",
        readOnly: false,
      },
      {
        key: "positionName",
        label: "직급",
        readOnly: false,
      },
      {
        key: "employmentStatus",
        label: "재직상태",
        readOnly: false,
      },
      {
        key: "roleCode",
        label: "역할",
        readOnly: false,
      },
      {
        key: "useYn",
        label: "사용여부",
        readOnly: false,
      },
    ],
    columns: [
      {
        key: "employeeNo",
        label: "교번",
        readOnly: true,
      },
      {
        key: "userName",
        label: "성명",
        readOnly: true,
      },
      {
        key: "departmentName",
        label: "소속",
        readOnly: true,
      },
      {
        key: "positionName",
        label: "직급",
        readOnly: true,
      },
      {
        key: "employmentStatus",
        label: "재직상태",
        readOnly: true,
      },
      {
        key: "roleCode",
        label: "역할",
        readOnly: true,
      },
      {
        key: "useYn",
        label: "사용여부",
        readOnly: true,
      },
      {
        key: "appointmentName",
        label: "보직",
        readOnly: true,
      },
      {
        key: "retiredAt",
        label: "퇴직일자",
        readOnly: true,
      },
      {
        key: "lastSyncedAt",
        label: "최종 동기화일시",
        readOnly: true,
      },
    ],
    modalFields: [
      {
        key: "systemUseYn",
        label: "시스템 사용여부",
        readOnly: false,
      },
      {
        key: "businessRole",
        label: "업무 역할",
        readOnly: false,
      },
    ],
    primaryActions: ["검색", "행 선택", "시스템 사용여부·업무 역할 저장"],
    constraint:
      "KORUS 원천 인사정보는 읽기 전용이며 수정 항목은 시스템 사용여부와 업무 역할로 제한한다.",
    readOnly: false,
    mutationMethod: "POST",
  },
  {
    route: "/admin/cmn/fr/002",
    screenId: "SCR-CMN-FR-002",
    menuPath: "시스템 관리 > 사용자·조직 관리 > 조직 관리",
    role: "R09 시스템관리자",
    primaryEntity: "organization",
    operationId: "cmn_fr_002_search",
    apiPath: "/api/admin/cmn/fr/002",
    requirements: ["REQ-008", "REQ-009", "REQ-010", "REQ-011"],
    archetype: "시스템 관리 > 사용자·조직 관리 > 조직 관리",
    goal: "조직 관리에서 source-backed 조회/명령/검증 결과를 확인한다.",
    filters: [
      {
        key: "organizationCode",
        label: "조직코드",
        readOnly: false,
      },
      {
        key: "field85771",
        label: "조직구분(대학",
        readOnly: false,
      },
      {
        key: "field70685",
        label: "대학원",
        readOnly: false,
      },
      {
        key: "field51223",
        label: "단과대학",
        readOnly: false,
      },
      {
        key: "field21459",
        label: "학과",
        readOnly: false,
      },
      {
        key: "field3321",
        label: "부서)",
        readOnly: false,
      },
      {
        key: "effectiveDate",
        label: "적용일",
        readOnly: false,
      },
    ],
    columns: [
      {
        key: "organizationCode",
        label: "조직코드",
        readOnly: true,
      },
      {
        key: "organizationName",
        label: "조직명",
        readOnly: true,
      },
      {
        key: "organizationType",
        label: "조직구분",
        readOnly: true,
      },
      {
        key: "parentOrganizationCode",
        label: "상위조직",
        readOnly: true,
      },
      {
        key: "effectiveStartDate",
        label: "적용 시작일",
        readOnly: true,
      },
      {
        key: "effectiveEndDate",
        label: "적용 종료일",
        readOnly: true,
      },
      {
        key: "hasChangeHistory",
        label: "변경 이력 여부",
        readOnly: true,
      },
    ],
    modalFields: [
      {
        key: "parentOrganizationCode",
        label: "상위조직",
        readOnly: false,
      },
      {
        key: "effectiveStartDate",
        label: "적용 시작일",
        readOnly: false,
      },
      {
        key: "effectiveEndDate",
        label: "적용 종료일",
        readOnly: false,
      },
    ],
    primaryActions: ["검색", "계층 행 선택", "조직 관계·적용기간 저장"],
    constraint:
      "조직개편은 기존 관계를 덮어쓰지 않고 적용기간 이력으로 보존한다.",
    readOnly: false,
    mutationMethod: "POST",
  },
  {
    route: "/admin/cmn/fr/003",
    screenId: "SCR-CMN-FR-003",
    menuPath: "시스템 관리 > 사용자·조직 관리 > 보직 관리",
    role: "R09 시스템관리자",
    primaryEntity: "appointment",
    operationId: "cmn_fr_003_search",
    apiPath: "/api/admin/cmn/fr/003",
    requirements: ["REQ-016", "REQ-017", "REQ-018"],
    archetype: "시스템 관리 > 사용자·조직 관리 > 보직 관리",
    goal: "보직 관리에서 source-backed 조회/명령/검증 결과를 확인한다.",
    filters: [
      {
        key: "appointmentCode",
        label: "보직코드",
        readOnly: false,
      },
      {
        key: "targetUserId",
        label: "대상 사용자",
        readOnly: false,
      },
      {
        key: "organizationCode",
        label: "소속조직",
        readOnly: false,
      },
      {
        key: "baseDate",
        label: "기준일",
        readOnly: false,
      },
    ],
    columns: [
      {
        key: "appointmentCode",
        label: "보직코드",
        readOnly: true,
      },
      {
        key: "targetUserId",
        label: "대상 사용자",
        readOnly: true,
      },
      {
        key: "organizationCode",
        label: "소속조직",
        readOnly: true,
      },
      {
        key: "validFrom",
        label: "유효 시작일",
        readOnly: true,
      },
      {
        key: "validTo",
        label: "유효 종료일",
        readOnly: true,
      },
    ],
    modalFields: [
      {
        key: "appointmentCode",
        label: "보직코드",
        readOnly: false,
      },
      {
        key: "targetUserId",
        label: "대상 사용자",
        readOnly: false,
      },
      {
        key: "organizationCode",
        label: "소속조직",
        readOnly: false,
      },
      {
        key: "validFrom",
        label: "유효 시작일",
        readOnly: false,
      },
      {
        key: "validTo",
        label: "유효 종료일",
        readOnly: false,
      },
    ],
    primaryActions: ["검색", "신규 등록", "보직 수정"],
    constraint:
      "보직은 사용자 인사정보나 조직구조를 변경하지 않고 지정 조직·유효기간에만 연결한다.",
    readOnly: false,
    mutationMethod: "POST",
  },
  {
    route: "/admin/cmn/fr/005",
    screenId: "SCR-CMN-FR-005",
    menuPath: "시스템 관리 > 역할·권한 관리 > 역할 관리",
    role: "R09 시스템관리자",
    primaryEntity: "role",
    operationId: "cmn_fr_005_search",
    apiPath: "/api/admin/cmn/fr/005",
    requirements: ["REQ-024", "REQ-025", "REQ-026"],
    archetype: "시스템 관리 > 역할·권한 관리 > 역할 관리",
    goal: "역할 관리에서 source-backed 조회/명령/검증 결과를 확인한다.",
    filters: [
      {
        key: "roleCode",
        label: "역할코드",
        readOnly: false,
      },
      {
        key: "roleName",
        label: "역할명",
        readOnly: false,
      },
      {
        key: "useYn",
        label: "사용여부",
        readOnly: false,
      },
    ],
    columns: [
      {
        key: "roleCode",
        label: "역할코드",
        readOnly: true,
      },
      {
        key: "roleName",
        label: "역할명",
        readOnly: true,
      },
      {
        key: "rolePurpose",
        label: "목적",
        readOnly: true,
      },
      {
        key: "grantCriteria",
        label: "부여 기준",
        readOnly: true,
      },
      {
        key: "defaultDataScope",
        label: "데이터 범위 기본값",
        readOnly: true,
      },
      {
        key: "useYn",
        label: "사용여부",
        readOnly: true,
      },
    ],
    modalFields: [
      {
        key: "roleName",
        label: "역할명",
        readOnly: false,
      },
      {
        key: "rolePurpose",
        label: "목적",
        readOnly: false,
      },
      {
        key: "grantCriteria",
        label: "부여 기준",
        readOnly: false,
      },
      {
        key: "defaultDataScope",
        label: "데이터 범위 기본값",
        readOnly: false,
      },
    ],
    primaryActions: ["검색", "역할 등록", "역할 수정"],
    constraint: "R01~R09 역할코드는 삭제하거나 다른 의미로 재사용하지 않는다.",
    readOnly: false,
    mutationMethod: "POST",
  },
  {
    route: "/admin/cmn/fr/006",
    screenId: "SCR-CMN-FR-006",
    menuPath: "시스템 관리 > 역할·권한 관리 > 사용자 역할 관리",
    role: "R09 시스템관리자",
    primaryEntity: "user_role",
    operationId: "cmn_fr_006_search",
    apiPath: "/api/admin/cmn/fr/006",
    requirements: ["REQ-031", "REQ-032", "REQ-033", "REQ-034"],
    archetype: "시스템 관리 > 역할·권한 관리 > 사용자 역할 관리",
    goal: "사용자 역할 관리에서 source-backed 조회/명령/검증 결과를 확인한다.",
    filters: [
      {
        key: "userId",
        label: "사용자",
        readOnly: false,
      },
      {
        key: "roleCode",
        label: "역할",
        readOnly: false,
      },
      {
        key: "validPeriod",
        label: "유효기간",
        readOnly: false,
      },
      {
        key: "assignmentType",
        label: "역할 부여 방식",
        readOnly: false,
      },
    ],
    columns: [
      {
        key: "userId",
        label: "사용자",
        readOnly: true,
      },
      {
        key: "currentRole",
        label: "현재 역할",
        readOnly: true,
      },
      {
        key: "validFrom",
        label: "유효 시작일",
        readOnly: true,
      },
      {
        key: "validTo",
        label: "유효 종료일",
        readOnly: true,
      },
      {
        key: "approverId",
        label: "승인자",
        readOnly: true,
      },
      {
        key: "appointmentBasedYn",
        label: "보직 기반 여부",
        readOnly: true,
      },
    ],
    modalFields: [
      {
        key: "roleCode",
        label: "역할",
        readOnly: false,
      },
      {
        key: "validFrom",
        label: "유효 시작일",
        readOnly: false,
      },
      {
        key: "validTo",
        label: "유효 종료일",
        readOnly: false,
      },
      {
        key: "approverId",
        label: "승인자",
        readOnly: false,
      },
      {
        key: "grantReason",
        label: "부여",
        readOnly: false,
      },
      {
        key: "changeReason",
        label: "변경",
        readOnly: false,
      },
      {
        key: "revokeReason",
        label: "회수 사유",
        readOnly: false,
      },
    ],
    primaryActions: ["검색", "역할 부여", "역할 변경", "역할 회수"],
    constraint: "유효기간이 종료된 역할은 현재 적용 역할로 판정하지 않는다.",
    readOnly: false,
    mutationMethod: "POST",
  },
  {
    route: "/admin/cmn/fr/007",
    screenId: "SCR-CMN-FR-007",
    menuPath: "시스템 관리 > 역할·권한 관리 > 메뉴 권한 관리",
    role: "R09 시스템관리자",
    primaryEntity: "menu_permission",
    operationId: "cmn_fr_007_search",
    apiPath: "/api/admin/cmn/fr/007",
    requirements: ["REQ-040", "REQ-041", "REQ-042"],
    archetype: "시스템 관리 > 역할·권한 관리 > 메뉴 권한 관리",
    goal: "메뉴 권한 관리에서 source-backed 조회/명령/검증 결과를 확인한다.",
    filters: [
      {
        key: "field93331",
        label: "대상 유형(역할",
        readOnly: false,
      },
      {
        key: "field24118",
        label: "조직",
        readOnly: false,
      },
      {
        key: "field16779",
        label: "사용자)",
        readOnly: false,
      },
      {
        key: "targetId",
        label: "대상",
        readOnly: false,
      },
      {
        key: "menuId",
        label: "메뉴",
        readOnly: false,
      },
    ],
    columns: [
      {
        key: "targetType",
        label: "대상 유형",
        readOnly: true,
      },
      {
        key: "targetName",
        label: "대상명",
        readOnly: true,
      },
      {
        key: "mainMenuName",
        label: "대메뉴",
        readOnly: true,
      },
      {
        key: "middleMenuName",
        label: "중메뉴",
        readOnly: true,
      },
      {
        key: "screenName",
        label: "화면",
        readOnly: true,
      },
      {
        key: "accessAllowedYn",
        label: "접근 허용 여부",
        readOnly: true,
      },
    ],
    modalFields: [
      {
        key: "accessAllowedYn",
        label: "접근 허용 여부",
        readOnly: false,
      },
    ],
    primaryActions: ["검색", "접근 허용 여부 저장"],
    constraint: "화면 미노출과 서버 접근통제는 같은 권한값을 사용한다.",
    readOnly: false,
    mutationMethod: "PUT",
  },
  {
    route: "/admin/cmn/fr/008",
    screenId: "SCR-CMN-FR-008",
    menuPath: "시스템 관리 > 역할·권한 관리 > 기능 권한 관리",
    role: "R09 시스템관리자",
    primaryEntity: "function_permission",
    operationId: "cmn_fr_008_search",
    apiPath: "/api/admin/cmn/fr/008",
    requirements: ["REQ-047", "REQ-048", "REQ-049"],
    archetype: "시스템 관리 > 역할·권한 관리 > 기능 권한 관리",
    goal: "기능 권한 관리에서 source-backed 조회/명령/검증 결과를 확인한다.",
    filters: [
      {
        key: "roleCode",
        label: "역할",
        readOnly: false,
      },
      {
        key: "screenId",
        label: "화면ID",
        readOnly: false,
      },
      {
        key: "actionCode",
        label: "기능구분",
        readOnly: false,
      },
    ],
    columns: [
      {
        key: "roleCode",
        label: "역할",
        readOnly: true,
      },
      {
        key: "screenId",
        label: "화면ID",
        readOnly: true,
      },
      {
        key: "canRead",
        label: "조회",
        readOnly: true,
      },
      {
        key: "canCreate",
        label: "등록",
        readOnly: true,
      },
      {
        key: "canUpdate",
        label: "수정",
        readOnly: true,
      },
      {
        key: "canDelete",
        label: "삭제",
        readOnly: true,
      },
      {
        key: "canConfirm",
        label: "확인",
        readOnly: true,
      },
      {
        key: "canAuthenticate",
        label: "인증",
        readOnly: true,
      },
      {
        key: "canApprove",
        label: "승인",
        readOnly: true,
      },
      {
        key: "canCancelApproval",
        label: "승인취소",
        readOnly: true,
      },
      {
        key: "canPrint",
        label: "출력",
        readOnly: true,
      },
      {
        key: "canExcel",
        label: "엑셀",
        readOnly: true,
      },
      {
        key: "canBatch",
        label: "일괄처리",
        readOnly: true,
      },
    ],
    modalFields: [
      {
        key: "allowedYn",
        label: "기능구분별 허용 여부",
        readOnly: false,
      },
    ],
    primaryActions: ["검색", "기능권한 저장"],
    constraint: "기능권한은 화면 표시와 서버 처리에 모두 적용한다.",
    readOnly: false,
    mutationMethod: "PUT",
  },
  {
    route: "/admin/cmn/fr/009",
    screenId: "SCR-CMN-FR-009",
    menuPath: "시스템 관리 > 역할·권한 관리 > 데이터 범위 권한",
    role: "R09 시스템관리자",
    primaryEntity: "data_scope_permission",
    operationId: "cmn_fr_009_search",
    apiPath: "/api/admin/cmn/fr/009",
    requirements: ["REQ-054", "REQ-055", "REQ-056"],
    archetype: "시스템 관리 > 역할·권한 관리 > 데이터 범위 권한",
    goal: "데이터 범위 권한에서 source-backed 조회/명령/검증 결과를 확인한다.",
    filters: [
      {
        key: "roleCode",
        label: "역할",
        readOnly: false,
      },
      {
        key: "scopeType",
        label: "데이터 범위 유형",
        readOnly: false,
      },
      {
        key: "organizationCode",
        label: "조직코드",
        readOnly: false,
      },
      {
        key: "dutyArea",
        label: "업무영역",
        readOnly: false,
      },
    ],
    columns: [
      {
        key: "roleCode",
        label: "역할",
        readOnly: true,
      },
      {
        key: "field52633",
        label: "범위 유형(본인",
        readOnly: true,
      },
      {
        key: "field53879",
        label: "소속학과",
        readOnly: true,
      },
      {
        key: "field51223",
        label: "단과대학",
        readOnly: true,
      },
      {
        key: "field52245",
        label: "담당업무",
        readOnly: true,
      },
      {
        key: "field95624",
        label: "전체)",
        readOnly: true,
      },
      {
        key: "organizationCode",
        label: "조직코드",
        readOnly: true,
      },
      {
        key: "dutyArea",
        label: "업무영역",
        readOnly: true,
      },
    ],
    modalFields: [
      {
        key: "scopeType",
        label: "범위 유형",
        readOnly: false,
      },
      {
        key: "organizationCode",
        label: "조직코드",
        readOnly: false,
      },
      {
        key: "dutyArea",
        label: "업무영역",
        readOnly: false,
      },
    ],
    primaryActions: ["검색", "데이터 범위 저장"],
    constraint: "데이터 범위는 UI 필터가 아니라 서버 조회조건에 강제 적용한다.",
    readOnly: false,
    mutationMethod: "PUT",
  },
  {
    route: "/admin/cmn/fr/013/014",
    screenId: "SCR-CMN-FR-013-014",
    menuPath: "시스템 관리 > 메뉴 관리 > 메뉴 관리",
    role: "R09 시스템관리자",
    primaryEntity: "menu",
    operationId: "cmn_fr_013_014_search",
    apiPath: "/api/admin/cmn/fr/013/014",
    requirements: ["REQ-061", "REQ-062", "REQ-063", "REQ-064", "REQ-065"],
    archetype: "시스템 관리 > 메뉴 관리 > 메뉴 관리",
    goal: "메뉴 관리에서 source-backed 조회/명령/검증 결과를 확인한다.",
    filters: [
      {
        key: "menuName",
        label: "메뉴명",
        readOnly: false,
      },
      {
        key: "screenId",
        label: "화면ID",
        readOnly: false,
      },
      {
        key: "url",
        label: "URL",
        readOnly: false,
      },
      {
        key: "businessCategory",
        label: "업무구분",
        readOnly: false,
      },
    ],
    columns: [
      {
        key: "mainMenuName",
        label: "대메뉴",
        readOnly: true,
      },
      {
        key: "middleMenuName",
        label: "중메뉴",
        readOnly: true,
      },
      {
        key: "leafMenuName",
        label: "소메뉴",
        readOnly: true,
      },
      {
        key: "displayOrder",
        label: "표시순서",
        readOnly: true,
      },
      {
        key: "screenId",
        label: "화면ID",
        readOnly: true,
      },
      {
        key: "url",
        label: "URL",
        readOnly: true,
      },
      {
        key: "icon",
        label: "아이콘",
        readOnly: true,
      },
      {
        key: "businessCategory",
        label: "업무구분",
        readOnly: true,
      },
      {
        key: "description",
        label: "설명",
        readOnly: true,
      },
    ],
    modalFields: [
      {
        key: "parentMenuId",
        label: "부모메뉴",
        readOnly: false,
      },
      {
        key: "displayOrder",
        label: "표시순서",
        readOnly: false,
      },
      {
        key: "menuName",
        label: "메뉴명",
        readOnly: false,
      },
      {
        key: "screenId",
        label: "화면ID",
        readOnly: false,
      },
      {
        key: "url",
        label: "URL",
        readOnly: false,
      },
      {
        key: "icon",
        label: "아이콘",
        readOnly: false,
      },
      {
        key: "businessCategory",
        label: "업무구분",
        readOnly: false,
      },
      {
        key: "description",
        label: "설명",
        readOnly: false,
      },
    ],
    primaryActions: ["검색", "메뉴 등록", "메뉴 수정", "표시순서 재정렬"],
    constraint:
      "표시순서는 동일 부모·동일 계층 안에서 관리하고 화면ID와 URL로 실행 화면을 연결한다.",
    readOnly: false,
    mutationMethod: "POST",
  },
  {
    route: "/admin/cmn/fr/016",
    screenId: "SCR-CMN-FR-016",
    menuPath: "시스템 관리 > 공통코드 관리 > 코드그룹 관리",
    role: "R09 시스템관리자",
    primaryEntity: "code_group",
    operationId: "cmn_fr_016_search",
    apiPath: "/api/admin/cmn/fr/016",
    requirements: ["REQ-071", "REQ-072", "REQ-073"],
    archetype: "시스템 관리 > 공통코드 관리 > 코드그룹 관리",
    goal: "코드그룹 관리에서 source-backed 조회/명령/검증 결과를 확인한다.",
    filters: [
      {
        key: "groupId",
        label: "그룹ID",
        readOnly: false,
      },
      {
        key: "name",
        label: "명칭",
        readOnly: false,
      },
      {
        key: "managingDepartment",
        label: "관리부서",
        readOnly: false,
      },
      {
        key: "useYn",
        label: "사용여부",
        readOnly: false,
      },
    ],
    columns: [
      {
        key: "groupId",
        label: "그룹ID",
        readOnly: true,
      },
      {
        key: "name",
        label: "명칭",
        readOnly: true,
      },
      {
        key: "description",
        label: "설명",
        readOnly: true,
      },
      {
        key: "managingDepartment",
        label: "관리부서",
        readOnly: true,
      },
      {
        key: "useYn",
        label: "사용여부",
        readOnly: true,
      },
    ],
    modalFields: [
      {
        key: "groupId",
        label: "그룹ID",
        readOnly: false,
      },
      {
        key: "name",
        label: "명칭",
        readOnly: false,
      },
      {
        key: "description",
        label: "설명",
        readOnly: false,
      },
      {
        key: "managingDepartment",
        label: "관리부서",
        readOnly: false,
      },
    ],
    primaryActions: [
      "검색",
      "코드그룹 등록",
      "코드그룹 수정",
      "상세코드 목록 이동",
    ],
    constraint:
      "그룹ID는 코드그룹 식별값이며 코드값·정렬순서는 상세코드 관리에서 변경한다.",
    readOnly: false,
    mutationMethod: "POST",
  },
  {
    route: "/admin/cmn/fr/017",
    screenId: "SCR-CMN-FR-017",
    menuPath: "시스템 관리 > 공통코드 관리 > 상세코드 관리",
    role: "R09 시스템관리자",
    primaryEntity: "detail_code",
    operationId: "cmn_fr_017_search",
    apiPath: "/api/admin/cmn/fr/017",
    requirements: ["REQ-078", "REQ-079", "REQ-080"],
    archetype: "시스템 관리 > 공통코드 관리 > 상세코드 관리",
    goal: "상세코드 관리에서 source-backed 조회/명령/검증 결과를 확인한다.",
    filters: [
      {
        key: "groupId",
        label: "그룹ID",
        readOnly: false,
      },
      {
        key: "codeValue",
        label: "코드값",
        readOnly: false,
      },
      {
        key: "codeName",
        label: "코드명",
        readOnly: false,
      },
      {
        key: "parentCode",
        label: "상위코드",
        readOnly: false,
      },
      {
        key: "useYn",
        label: "사용여부",
        readOnly: false,
      },
    ],
    columns: [
      {
        key: "groupId",
        label: "그룹ID",
        readOnly: true,
      },
      {
        key: "codeValue",
        label: "코드값",
        readOnly: true,
      },
      {
        key: "codeName",
        label: "코드명",
        readOnly: true,
      },
      {
        key: "parentCode",
        label: "상위코드",
        readOnly: true,
      },
      {
        key: "sortOrder",
        label: "정렬순서",
        readOnly: true,
      },
      {
        key: "extraAttributes",
        label: "추가속성",
        readOnly: true,
      },
      {
        key: "useYn",
        label: "사용여부",
        readOnly: true,
      },
      {
        key: "validPeriod",
        label: "유효기간",
        readOnly: true,
      },
    ],
    modalFields: [
      {
        key: "codeValue",
        label: "코드값",
        readOnly: false,
      },
      {
        key: "codeName",
        label: "코드명",
        readOnly: false,
      },
      {
        key: "parentCode",
        label: "상위코드",
        readOnly: false,
      },
      {
        key: "sortOrder",
        label: "정렬순서",
        readOnly: false,
      },
      {
        key: "extraAttributes",
        label: "추가속성",
        readOnly: false,
      },
      {
        key: "useYn",
        label: "사용여부",
        readOnly: false,
      },
      {
        key: "validPeriod",
        label: "유효기간",
        readOnly: false,
      },
    ],
    primaryActions: ["검색", "상세코드 등록", "상세코드 수정", "사용중지"],
    constraint:
      "상세코드는 하나의 코드그룹에 속하고 사용 중인 코드는 삭제 대신 사용중지 처리한다.",
    readOnly: false,
    mutationMethod: "POST",
  },
  {
    route: "/admin/cmn/fr/019",
    screenId: "SCR-CMN-FR-019",
    menuPath: "시스템 관리 > 시스템 환경설정 > 공통 환경설정",
    role: "R09 시스템관리자",
    primaryEntity: "system_setting",
    operationId: "cmn_fr_019_search",
    apiPath: "/api/admin/cmn/fr/019",
    requirements: ["REQ-085", "REQ-086"],
    archetype: "시스템 관리 > 시스템 환경설정 > 공통 환경설정",
    goal: "공통 환경설정에서 source-backed 조회/명령/검증 결과를 확인한다.",
    filters: [
      {
        key: "settingName",
        label: "설정명",
        readOnly: false,
      },
      {
        key: "settingType",
        label: "설정구분",
        readOnly: false,
      },
      {
        key: "useYn",
        label: "사용여부",
        readOnly: false,
      },
    ],
    columns: [
      {
        key: "settingName",
        label: "설정명",
        readOnly: true,
      },
      {
        key: "settingValue",
        label: "현재값",
        readOnly: true,
      },
      {
        key: "unit",
        label: "단위",
        readOnly: true,
      },
      {
        key: "description",
        label: "설명",
        readOnly: true,
      },
      {
        key: "useYn",
        label: "사용여부",
        readOnly: true,
      },
    ],
    modalFields: [
      {
        key: "sessionIdleMinutes",
        label: "세션 유휴시간",
        readOnly: false,
      },
      {
        key: "defaultPageSize",
        label: "페이지당 조회건수",
        readOnly: false,
      },
      {
        key: "defaultSearchPeriod",
        label: "기본 검색기간",
        readOnly: false,
      },
      {
        key: "bulkSearchLimit",
        label: "대량조회 기준건수",
        readOnly: false,
      },
      {
        key: "longJobNoticeSeconds",
        label: "장시간작업 안내 기준",
        readOnly: false,
      },
    ],
    primaryActions: ["검색", "설정값 저장"],
    constraint:
      "설정값은 항목별 의미와 단위에 맞게 관리하고 사용자별 개별 환경값은 만들지 않는다.",
    readOnly: false,
    mutationMethod: "PUT",
  },
  {
    route: "/admin/cmn/fr/020",
    screenId: "SCR-CMN-FR-020",
    menuPath: "시스템 관리 > 시스템 환경설정 > 기준연도 관리",
    role: "R09 시스템관리자",
    primaryEntity: "base_year",
    operationId: "cmn_fr_020_search",
    apiPath: "/api/admin/cmn/fr/020",
    requirements: ["REQ-091", "REQ-092", "REQ-093"],
    archetype: "시스템 관리 > 시스템 환경설정 > 기준연도 관리",
    goal: "기준연도 관리에서 source-backed 조회/명령/검증 결과를 확인한다.",
    filters: [
      {
        key: "evaluationYear",
        label: "평가연도",
        readOnly: false,
      },
      {
        key: "defaultSearchYearYn",
        label: "기본 조회연도 여부",
        readOnly: false,
      },
      {
        key: "field31432",
        label: "복사",
        readOnly: false,
      },
      {
        key: "initializeYn",
        label: "초기화 여부",
        readOnly: false,
      },
    ],
    columns: [
      {
        key: "evaluationYear",
        label: "평가연도",
        readOnly: true,
      },
      {
        key: "currentEvaluationYearYn",
        label: "현재 평가연도 여부",
        readOnly: true,
      },
      {
        key: "defaultSearchYearYn",
        label: "기본 조회연도 여부",
        readOnly: true,
      },
      {
        key: "copyBaseDataYn",
        label: "기준정보 복사 여부",
        readOnly: true,
      },
      {
        key: "initializeYn",
        label: "초기화 여부",
        readOnly: true,
      },
    ],
    modalFields: [
      {
        key: "evaluationYear",
        label: "평가연도",
        readOnly: false,
      },
      {
        key: "currentEvaluationYearYn",
        label: "현재 평가연도",
        readOnly: false,
      },
      {
        key: "defaultSearchYearYn",
        label: "기본 조회연도",
        readOnly: false,
      },
      {
        key: "field31432",
        label: "복사",
        readOnly: false,
      },
      {
        key: "field56400",
        label: "초기화 선택",
        readOnly: false,
      },
    ],
    primaryActions: ["검색", "기준연도 등록", "기준연도 수정"],
    constraint:
      "기준정보 복사·초기화는 기존 연도 평가자료를 삭제하거나 변경하지 않는다.",
    readOnly: false,
    mutationMethod: "POST",
  },
  {
    route: "/admin/cmn/fr/021",
    screenId: "SCR-CMN-FR-021",
    menuPath: "시스템 관리 > 시스템 환경설정 > 파일정책 관리",
    role: "R09 시스템관리자",
    primaryEntity: "file_policy",
    operationId: "cmn_fr_021_search",
    apiPath: "/api/admin/cmn/fr/021",
    requirements: ["REQ-099", "REQ-100", "REQ-101"],
    archetype: "시스템 관리 > 시스템 환경설정 > 파일정책 관리",
    goal: "파일정책 관리에서 source-backed 조회/명령/검증 결과를 확인한다.",
    filters: [
      {
        key: "businessCategory",
        label: "업무구분",
        readOnly: false,
      },
      {
        key: "allowedExtensions",
        label: "허용 확장자",
        readOnly: false,
      },
      {
        key: "malwareScanYn",
        label: "악성파일 검사 적용여부",
        readOnly: false,
      },
    ],
    columns: [
      {
        key: "businessCategory",
        label: "업무구분",
        readOnly: true,
      },
      {
        key: "allowedExtensions",
        label: "허용 확장자",
        readOnly: true,
      },
      {
        key: "maxFileSizeMb",
        label: "단일 파일 최대용량",
        readOnly: true,
      },
      {
        key: "maxFilesPerRecord",
        label: "건당 첨부개수",
        readOnly: true,
      },
      {
        key: "totalSizeMb",
        label: "전체용량",
        readOnly: true,
      },
      {
        key: "fileNameLength",
        label: "파일명 길이",
        readOnly: true,
      },
      {
        key: "malwareScanYn",
        label: "악성검사 적용여부",
        readOnly: true,
      },
    ],
    modalFields: [
      {
        key: "allowedExtensions",
        label: "허용 확장자",
        readOnly: false,
      },
      {
        key: "maxFileSizeMb",
        label: "단일 파일 최대용량",
        readOnly: false,
      },
      {
        key: "maxFilesPerRecord",
        label: "건당 첨부개수",
        readOnly: false,
      },
      {
        key: "totalSizeMb",
        label: "전체용량",
        readOnly: false,
      },
      {
        key: "fileNameLength",
        label: "파일명 길이",
        readOnly: false,
      },
      {
        key: "malwareScanYn",
        label: "악성검사 적용여부",
        readOnly: false,
      },
    ],
    primaryActions: ["검색", "파일정책 등록", "파일정책 수정"],
    constraint:
      "파일정책 관리 화면은 실제 파일 업로드·조회·삭제를 수행하지 않는다.",
    readOnly: false,
    mutationMethod: "POST",
  },
  {
    route: "/admin/cmn/fr/023",
    screenId: "SCR-CMN-FR-023",
    menuPath: "시스템 관리 > 공지·도움말 관리 > 공지사항 관리",
    role: "R09 시스템관리자",
    primaryEntity: "notice",
    operationId: "cmn_fr_023_search",
    apiPath: "/api/admin/cmn/fr/023",
    requirements: ["REQ-106", "REQ-107", "REQ-108"],
    archetype: "시스템 관리 > 공지·도움말 관리 > 공지사항 관리",
    goal: "공지사항 관리에서 source-backed 조회/명령/검증 결과를 확인한다.",
    filters: [
      {
        key: "title",
        label: "제목",
        readOnly: false,
      },
      {
        key: "publishPeriod",
        label: "게시기간",
        readOnly: false,
      },
      {
        key: "targetRole",
        label: "대상 역할",
        readOnly: false,
      },
      {
        key: "targetOrganization",
        label: "대상 조직",
        readOnly: false,
      },
      {
        key: "importantYn",
        label: "중요여부",
        readOnly: false,
      },
    ],
    columns: [
      {
        key: "title",
        label: "제목",
        readOnly: true,
      },
      {
        key: "publishStartDate",
        label: "게시 시작일",
        readOnly: true,
      },
      {
        key: "publishEndDate",
        label: "게시 종료일",
        readOnly: true,
      },
      {
        key: "targetRole",
        label: "대상 역할",
        readOnly: true,
      },
      {
        key: "targetOrganization",
        label: "대상 조직",
        readOnly: true,
      },
      {
        key: "importantYn",
        label: "중요여부",
        readOnly: true,
      },
      {
        key: "hasAttachment",
        label: "첨부파일 여부",
        readOnly: true,
      },
    ],
    modalFields: [
      {
        key: "title",
        label: "제목",
        readOnly: false,
      },
      {
        key: "publishPeriod",
        label: "게시기간",
        readOnly: false,
      },
      {
        key: "targetRole",
        label: "대상 역할",
        readOnly: false,
      },
      {
        key: "field24118",
        label: "조직",
        readOnly: false,
      },
      {
        key: "importantYn",
        label: "중요여부",
        readOnly: false,
      },
      {
        key: "attachmentIds",
        label: "첨부파일",
        readOnly: false,
      },
    ],
    primaryActions: ["검색", "공지 등록", "공지 수정", "첨부파일 확인"],
    constraint:
      "지정 대상과 게시기간에만 노출하며 공지 열람은 업무 승인이나 확인처리가 아니다.",
    readOnly: false,
    mutationMethod: "POST",
  },
  {
    route: "/admin/cmn/fr/052/053/054",
    screenId: "SCR-CMN-FR-052-053-054",
    menuPath: "파일·데이터 관리 > 첨부파일 관리 > 첨부파일 관리",
    role: "R09 시스템관리자",
    primaryEntity: "attachment_metadata",
    operationId: "cmn_fr_052_053_054_search",
    apiPath: "/api/admin/cmn/fr/052/053/054",
    requirements: ["REQ-114", "REQ-115", "REQ-116", "REQ-117"],
    archetype: "파일·데이터 관리 > 첨부파일 관리 > 첨부파일 관리",
    goal: "첨부파일 관리에서 source-backed 조회/명령/검증 결과를 확인한다.",
    filters: [
      {
        key: "businessKey",
        label: "업무자료",
        readOnly: false,
      },
      {
        key: "fileId",
        label: "파일ID",
        readOnly: false,
      },
      {
        key: "originalFileName",
        label: "원본명",
        readOnly: false,
      },
      {
        key: "extension",
        label: "확장자",
        readOnly: false,
      },
      {
        key: "malwareScanResult",
        label: "악성검사결과",
        readOnly: false,
      },
      {
        key: "deleteYn",
        label: "삭제여부",
        readOnly: false,
      },
    ],
    columns: [
      {
        key: "fileId",
        label: "파일ID",
        readOnly: true,
      },
      {
        key: "originalFileName",
        label: "원본명",
        readOnly: true,
      },
      {
        key: "storedFileName",
        label: "저장명",
        readOnly: true,
      },
      {
        key: "extension",
        label: "확장자",
        readOnly: true,
      },
      {
        key: "sizeBytes",
        label: "크기",
        readOnly: true,
      },
      {
        key: "createdBy",
        label: "등록자",
        readOnly: true,
      },
      {
        key: "createdAt",
        label: "등록일시",
        readOnly: true,
      },
      {
        key: "malwareScanResult",
        label: "악성검사결과",
        readOnly: true,
      },
      {
        key: "deleteMark",
        label: "삭제표시",
        readOnly: true,
      },
    ],
    modalFields: [
      {
        key: "deleteTargetConfirm",
        label: "삭제대상 확인",
        readOnly: false,
      },
      {
        key: "reason",
        label: "삭제사유",
        readOnly: false,
      },
      {
        key: "integrityCheckYn",
        label: "정합성 점검 실행",
        readOnly: false,
      },
    ],
    primaryActions: [
      "검색",
      "파일 상세",
      "논리삭제",
      "정합성 점검",
      "점검 결과 상세",
    ],
    constraint:
      "논리삭제만 허용하고 평가확정 자료는 논리삭제도 차단하며 점검만으로 자동 삭제하지 않는다.",
    readOnly: false,
    mutationMethod: "DELETE",
  },
  {
    route: "/admin/cmn/fr/055",
    screenId: "SCR-CMN-FR-055",
    menuPath: "파일·데이터 관리 > 엑셀 관리 > 업로드 양식 관리",
    role: "R09 시스템관리자",
    primaryEntity: "excel_upload_template",
    operationId: "cmn_fr_055_search",
    apiPath: "/api/admin/cmn/fr/055",
    requirements: ["REQ-126", "REQ-127", "REQ-128"],
    archetype: "파일·데이터 관리 > 엑셀 관리 > 업로드 양식 관리",
    goal: "업로드 양식 관리에서 source-backed 조회/명령/검증 결과를 확인한다.",
    filters: [
      {
        key: "businessCategory",
        label: "업무구분",
        readOnly: false,
      },
      {
        key: "templateVersion",
        label: "버전",
        readOnly: false,
      },
      {
        key: "effectiveDate",
        label: "시행일",
        readOnly: false,
      },
      {
        key: "useYn",
        label: "사용여부",
        readOnly: false,
      },
    ],
    columns: [
      {
        key: "businessCategory",
        label: "업무구분",
        readOnly: true,
      },
      {
        key: "templateVersion",
        label: "버전",
        readOnly: true,
      },
      {
        key: "requiredColumns",
        label: "필수 열",
        readOnly: true,
      },
      {
        key: "columnOrder",
        label: "열 순서",
        readOnly: true,
      },
      {
        key: "codeValueRules",
        label: "코드값 규칙",
        readOnly: true,
      },
      {
        key: "effectiveDate",
        label: "시행일",
        readOnly: true,
      },
      {
        key: "downloadFileId",
        label: "다운로드 파일",
        readOnly: true,
      },
    ],
    modalFields: [
      {
        key: "businessCategory",
        label: "업무구분",
        readOnly: false,
      },
      {
        key: "templateVersion",
        label: "버전",
        readOnly: false,
      },
      {
        key: "requiredColumns",
        label: "필수 열",
        readOnly: false,
      },
      {
        key: "columnOrder",
        label: "열 순서",
        readOnly: false,
      },
      {
        key: "codeValueRules",
        label: "코드값 규칙",
        readOnly: false,
      },
      {
        key: "effectiveDate",
        label: "시행일",
        readOnly: false,
      },
      {
        key: "downloadFileId",
        label: "다운로드 파일",
        readOnly: false,
      },
    ],
    primaryActions: ["검색", "양식 등록", "양식 수정", "버전 파일 다운로드"],
    constraint:
      "양식은 업무·버전·시행일별 검증규칙과 연결하며 실제 업무자료 업로드는 수행하지 않는다.",
    readOnly: false,
    mutationMethod: "POST",
  },
  {
    route: "/admin/cmn/fr/056",
    screenId: "SCR-CMN-FR-056",
    menuPath: "파일·데이터 관리 > 엑셀 관리 > 엑셀 업로드",
    role: "R09 시스템관리자",
    primaryEntity: "excel_upload_history",
    operationId: "cmn_fr_056_search",
    apiPath: "/api/admin/cmn/fr/056",
    requirements: ["REQ-133", "REQ-134", "REQ-135", "REQ-136", "REQ-137"],
    archetype: "파일·데이터 관리 > 엑셀 관리 > 엑셀 업로드",
    goal: "엑셀 업로드에서 source-backed 조회/명령/검증 결과를 확인한다.",
    filters: [
      {
        key: "businessCategory",
        label: "업무구분",
        readOnly: false,
      },
      {
        key: "templateVersion",
        label: "양식 버전",
        readOnly: false,
      },
      {
        key: "uploadedAt",
        label: "업로드 일시",
        readOnly: false,
      },
      {
        key: "resultStatus",
        label: "처리결과",
        readOnly: false,
      },
    ],
    columns: [
      {
        key: "uploadedBy",
        label: "업로더",
        readOnly: true,
      },
      {
        key: "field29988",
        label: "일시",
        readOnly: true,
      },
      {
        key: "fileName",
        label: "파일명",
        readOnly: true,
      },
      {
        key: "totalCount",
        label: "총건수",
        readOnly: true,
      },
      {
        key: "successCount",
        label: "정상",
        readOnly: true,
      },
      {
        key: "errorCount",
        label: "오류",
        readOnly: true,
      },
      {
        key: "excludedCount",
        label: "제외",
        readOnly: true,
      },
      {
        key: "savedCount",
        label: "저장건수",
        readOnly: true,
      },
      {
        key: "processingTime",
        label: "처리시간",
        readOnly: true,
      },
    ],
    modalFields: [
      {
        key: "businessCategory",
        label: "업무구분",
        readOnly: false,
      },
      {
        key: "templateVersion",
        label: "양식 버전",
        readOnly: false,
      },
      {
        key: "xlsxFileName",
        label: "XLSX 파일",
        readOnly: false,
      },
      {
        key: "field90341",
        label: "업로드 실행 확인",
        readOnly: false,
      },
    ],
    primaryActions: [
      "파일 선택",
      "사전 검증",
      "일괄 등록",
      "결과 상세",
      "오류목록 다운로드",
      "이력 상세",
    ],
    constraint:
      "오류가 하나라도 있으면 전체를 반영하지 않고 모든 정상 행만 하나의 transaction으로 등록한다.",
    readOnly: false,
    mutationMethod: "POST",
  },
  {
    route: "/admin/cmn/fr/059",
    screenId: "SCR-CMN-FR-059",
    menuPath: "파일·데이터 관리 > 엑셀 관리 > 엑셀 다운로드",
    role: "R09 시스템관리자",
    primaryEntity: "excel_download_request",
    operationId: "cmn_fr_059_search",
    apiPath: "/api/admin/cmn/fr/059",
    requirements: ["REQ-144", "REQ-145", "REQ-146"],
    archetype: "파일·데이터 관리 > 엑셀 관리 > 엑셀 다운로드",
    goal: "엑셀 다운로드에서 source-backed 조회/명령/검증 결과를 확인한다.",
    filters: [
      {
        key: "targetScreen",
        label: "대상 화면",
        readOnly: false,
      },
      {
        key: "searchCondition",
        label: "조회조건",
        readOnly: false,
      },
      {
        key: "dataScope",
        label: "데이터 범위",
        readOnly: false,
      },
    ],
    columns: [
      {
        key: "targetScreen",
        label: "대상 화면",
        readOnly: true,
      },
      {
        key: "field14983",
        label: "조회조건 요약",
        readOnly: true,
      },
      {
        key: "field97829",
        label: "데이터 범위 적용 여부",
        readOnly: true,
      },
      {
        key: "field96839",
        label: "생성일시",
        readOnly: true,
      },
      {
        key: "field17967",
        label: "다운로드 상태",
        readOnly: true,
      },
    ],
    modalFields: [
      {
        key: "currentSearchCondition",
        label: "현재 조회조건",
        readOnly: false,
      },
      {
        key: "targetScreen",
        label: "대상 화면",
        readOnly: false,
      },
      {
        key: "downloadConfirmYn",
        label: "다운로드 실행 확인",
        readOnly: false,
      },
    ],
    primaryActions: ["검색", "엑셀 생성", "다운로드"],
    constraint:
      "현재 조회조건과 서버 데이터 범위 권한을 함께 적용하고 원천 업무자료는 변경하지 않는다.",
    readOnly: false,
    mutationMethod: "POST",
  },
  {
    route: "/admin/cmn/fr/071/072/073",
    screenId: "SCR-CMN-FR-071-072-073",
    menuPath: "보안·감사 관리 > 개인정보 관리 > 개인정보 관리",
    role: "R09 시스템관리자",
    primaryEntity: "privacy_field_policy",
    operationId: "cmn_fr_071_072_073_search",
    apiPath: "/api/admin/cmn/fr/071/072/073",
    requirements: ["REQ-150", "REQ-151", "REQ-152"],
    archetype: "보안·감사 관리 > 개인정보 관리 > 개인정보 관리",
    goal: "개인정보 관리에서 source-backed 조회/명령/검증 결과를 확인한다.",
    filters: [
      {
        key: "privacyFieldName",
        label: "개인정보 항목",
        readOnly: false,
      },
      {
        key: "privacyGrade",
        label: "등급",
        readOnly: false,
      },
      {
        key: "encryptedYn",
        label: "암호화 여부",
        readOnly: false,
      },
      {
        key: "maskedYn",
        label: "마스킹 여부",
        readOnly: false,
      },
      {
        key: "roleCode",
        label: "역할",
        readOnly: false,
      },
    ],
    columns: [
      {
        key: "fieldName",
        label: "필드명",
        readOnly: true,
      },
      {
        key: "field41830",
        label: "개인정보 등급",
        readOnly: true,
      },
      {
        key: "field34962",
        label: "암호화",
        readOnly: true,
      },
      {
        key: "field67702",
        label: "마스킹",
        readOnly: true,
      },
      {
        key: "field94717",
        label: "로그 제외",
        readOnly: true,
      },
      {
        key: "field62779",
        label: "원문 권한",
        readOnly: true,
      },
      {
        key: "field3588",
        label: "출력 권한",
        readOnly: true,
      },
      {
        key: "field37293",
        label: "계좌정보 권한",
        readOnly: true,
      },
      {
        key: "field26429",
        label: "처리이력",
        readOnly: true,
      },
    ],
    modalFields: [
      {
        key: "privacyGrade",
        label: "등급",
        readOnly: false,
      },
      {
        key: "field34962",
        label: "암호화",
        readOnly: false,
      },
      {
        key: "field67702",
        label: "마스킹",
        readOnly: false,
      },
      {
        key: "field87430",
        label: "로그 제외 정책",
        readOnly: false,
      },
      {
        key: "field38523",
        label: "역할별 조회",
        readOnly: false,
      },
      {
        key: "field3588",
        label: "출력 권한",
        readOnly: false,
      },
      {
        key: "accessPurpose",
        label: "조회 목적",
        readOnly: false,
      },
    ],
    primaryActions: [
      "검색",
      "보호정책 저장",
      "역할별 권한 저장",
      "처리이력 상세",
    ],
    constraint:
      "실제 개인정보 원문값은 이 화면에서 조회·수정하지 않고 처리이력은 수정·삭제할 수 없다.",
    readOnly: false,
    mutationMethod: "PUT",
  },
  {
    route: "/admin/cmn/fr/074",
    screenId: "SCR-CMN-FR-074",
    menuPath: "보안·감사 관리 > 접속기록 관리 > 접속현황 관리",
    role: "R09 시스템관리자",
    primaryEntity: "session",
    operationId: "cmn_fr_074_search",
    apiPath: "/api/admin/cmn/fr/074",
    requirements: ["REQ-161", "REQ-162", "REQ-163"],
    archetype: "보안·감사 관리 > 접속기록 관리 > 접속현황 관리",
    goal: "접속현황 관리에서 source-backed 조회/명령/검증 결과를 확인한다.",
    filters: [
      {
        key: "userId",
        label: "사용자",
        readOnly: false,
      },
      {
        key: "period",
        label: "기간",
        readOnly: false,
      },
      {
        key: "sessionStatus",
        label: "세션상태",
        readOnly: false,
      },
      {
        key: "ipAddress",
        label: "IP",
        readOnly: false,
      },
    ],
    columns: [
      {
        key: "userId",
        label: "사용자",
        readOnly: true,
      },
      {
        key: "loginAt",
        label: "로그인시각",
        readOnly: true,
      },
      {
        key: "lastActivityAt",
        label: "최종활동시각",
        readOnly: true,
      },
      {
        key: "ipAddress",
        label: "IP",
        readOnly: true,
      },
      {
        key: "sessionStatus",
        label: "세션상태",
        readOnly: true,
      },
      {
        key: "field1456",
        label: "종료유형",
        readOnly: true,
      },
      {
        key: "field20190",
        label: "종료일시",
        readOnly: true,
      },
    ],
    modalFields: [
      {
        key: "sessionId",
        label: "강제종료 대상 세션",
        readOnly: false,
      },
      {
        key: "reason",
        label: "사유",
        readOnly: false,
      },
      {
        key: "userId",
        label: "사용자",
        readOnly: false,
      },
      {
        key: "field85460",
        label: "기간 종료이력 검색조건",
        readOnly: false,
      },
    ],
    primaryActions: [
      "활성 세션 검색",
      "세션 상세",
      "강제종료",
      "종료이력 조회",
    ],
    constraint:
      "강제종료는 권한 있는 운영자에게만 허용하고 즉시 세션 무효화 및 감사로그 기록을 수행한다.",
    readOnly: false,
    mutationMethod: "POST",
  },
  {
    route: "/admin/cmn/fr/076/077/078",
    screenId: "SCR-CMN-FR-076-077-078",
    menuPath: "보안·감사 관리 > 감사로그 관리 > 감사 로그 관리",
    role: "R09 시스템관리자",
    primaryEntity: "audit_log",
    operationId: "cmn_fr_076_077_078_search",
    apiPath: "/api/admin/cmn/fr/076/077/078",
    requirements: ["REQ-170", "REQ-171", "REQ-172"],
    archetype: "보안·감사 관리 > 감사로그 관리 > 감사 로그 관리",
    goal: "감사 로그 관리에서 source-backed 조회/명령/검증 결과를 확인한다.",
    filters: [
      {
        key: "logType",
        label: "로그유형",
        readOnly: false,
      },
      {
        key: "actionType",
        label: "행위유형",
        readOnly: false,
      },
      {
        key: "informationType",
        label: "정보유형",
        readOnly: false,
      },
      {
        key: "authorityType",
        label: "권한유형",
        readOnly: false,
      },
      {
        key: "targetKey",
        label: "대상키",
        readOnly: false,
      },
      {
        key: "actorId",
        label: "처리자",
        readOnly: false,
      },
      {
        key: "period",
        label: "기간",
        readOnly: false,
      },
    ],
    columns: [
      {
        key: "logType",
        label: "로그유형",
        readOnly: true,
      },
      {
        key: "targetKey",
        label: "대상키",
        readOnly: true,
      },
      {
        key: "beforeAfterState",
        label: "전후상태",
        readOnly: true,
      },
      {
        key: "actorId",
        label: "처리자",
        readOnly: true,
      },
      {
        key: "resultStatus",
        label: "처리결과",
        readOnly: true,
      },
      {
        key: "viewerId",
        label: "조회자",
        readOnly: true,
      },
      {
        key: "targetScope",
        label: "대상범위",
        readOnly: true,
      },
      {
        key: "accessPurpose",
        label: "조회목적",
        readOnly: true,
      },
      {
        key: "approverId",
        label: "승인자",
        readOnly: true,
      },
      {
        key: "reason",
        label: "사유",
        readOnly: true,
      },
      {
        key: "field77981",
        label: "변경일시",
        readOnly: true,
      },
    ],
    modalFields: [
      {
        key: "field56611",
        label: "검색조건만 입력",
        readOnly: true,
      },
      {
        key: "field17406",
        label: "로그 수정",
        readOnly: true,
      },
      {
        key: "field80777",
        label: "삭제 입력 없음",
        readOnly: true,
      },
    ],
    primaryActions: [
      "통합 검색",
      "업무처리 로그 상세",
      "중요정보 조회로그 상세",
      "권한변경 로그 상세",
    ],
    constraint:
      "감사로그는 변경 불가 이력이며 원업무 재실행·취소를 제공하지 않는다.",
    readOnly: true,
    mutationMethod: "GET",
  },
  {
    route: "/admin/cmn/fr/079",
    screenId: "SCR-CMN-FR-079",
    menuPath: "시스템 운영 관리 > 배치작업 관리 > 배치 정의 관리",
    role: "R09 시스템관리자",
    primaryEntity: "batch_definition",
    operationId: "cmn_fr_079_search",
    apiPath: "/api/admin/cmn/fr/079",
    requirements: ["REQ-178", "REQ-179"],
    archetype: "시스템 운영 관리 > 배치작업 관리 > 배치 정의 관리",
    goal: "배치 정의 관리에서 source-backed 조회/명령/검증 결과를 확인한다.",
    filters: [
      {
        key: "batchId",
        label: "배치ID",
        readOnly: false,
      },
      {
        key: "scheduleExpression",
        label: "실행주기",
        readOnly: false,
      },
      {
        key: "ownerId",
        label: "담당자",
        readOnly: false,
      },
      {
        key: "useYn",
        label: "사용여부",
        readOnly: false,
      },
    ],
    columns: [
      {
        key: "batchId",
        label: "배치ID",
        readOnly: true,
      },
      {
        key: "scheduleExpression",
        label: "실행주기",
        readOnly: true,
      },
      {
        key: "dependencyBatchId",
        label: "선후행",
        readOnly: true,
      },
      {
        key: "parameters",
        label: "파라미터",
        readOnly: true,
      },
      {
        key: "maxRuntimeMinutes",
        label: "최대실행시간",
        readOnly: true,
      },
      {
        key: "ownerId",
        label: "담당자",
        readOnly: true,
      },
      {
        key: "useYn",
        label: "사용여부",
        readOnly: true,
      },
    ],
    modalFields: [
      {
        key: "batchId",
        label: "배치ID",
        readOnly: false,
      },
      {
        key: "scheduleExpression",
        label: "실행주기",
        readOnly: false,
      },
      {
        key: "dependencyBatchId",
        label: "선후행",
        readOnly: false,
      },
      {
        key: "parameters",
        label: "파라미터",
        readOnly: false,
      },
      {
        key: "maxRuntimeMinutes",
        label: "최대실행시간",
        readOnly: false,
      },
      {
        key: "ownerId",
        label: "담당자",
        readOnly: false,
      },
    ],
    primaryActions: ["검색", "배치 정의 등록", "배치 정의 수정"],
    constraint: "배치 정의 화면은 즉시 실행·중지·재실행을 제공하지 않는다.",
    readOnly: false,
    mutationMethod: "POST",
  },
  {
    route: "/admin/cmn/fr/080",
    screenId: "SCR-CMN-FR-080",
    menuPath: "시스템 운영 관리 > 배치작업 관리 > 배치 실행 관리",
    role: "R09 시스템관리자",
    primaryEntity: "batch_execution_history",
    operationId: "cmn_fr_080_search",
    apiPath: "/api/admin/cmn/fr/080",
    requirements: ["REQ-185", "REQ-186", "REQ-187"],
    archetype: "시스템 운영 관리 > 배치작업 관리 > 배치 실행 관리",
    goal: "배치 실행 관리에서 source-backed 조회/명령/검증 결과를 확인한다.",
    filters: [
      {
        key: "batchId",
        label: "배치ID",
        readOnly: false,
      },
      {
        key: "executionStatus",
        label: "실행상태",
        readOnly: false,
      },
      {
        key: "ownerId",
        label: "담당자",
        readOnly: false,
      },
    ],
    columns: [
      {
        key: "batchId",
        label: "배치ID",
        readOnly: true,
      },
      {
        key: "executionStatus",
        label: "실행상태",
        readOnly: true,
      },
      {
        key: "parameters",
        label: "파라미터",
        readOnly: true,
      },
      {
        key: "field92220",
        label: "최근 실행시각",
        readOnly: true,
      },
      {
        key: "field50677",
        label: "운영자",
        readOnly: true,
      },
      {
        key: "field43861",
        label: "처리유형",
        readOnly: true,
      },
      {
        key: "reason",
        label: "사유",
        readOnly: true,
      },
    ],
    modalFields: [
      {
        key: "executionParameters",
        label: "실행 파라미터",
        readOnly: false,
      },
      {
        key: "reason",
        label: "수동 실행 사유",
        readOnly: false,
      },
      {
        key: "reason",
        label: "중지 사유",
        readOnly: false,
      },
      {
        key: "reason",
        label: "재실행 사유",
        readOnly: false,
      },
    ],
    primaryActions: ["실행 가능 배치 조회", "수동 실행", "중지", "재실행"],
    constraint:
      "권한 있는 운영자만 실행·중지·재실행할 수 있고 배치 정의·원천 업무자료는 변경하지 않는다.",
    readOnly: false,
    mutationMethod: "POST",
  },
  {
    route: "/admin/cmn/fr/081",
    screenId: "SCR-CMN-FR-081",
    menuPath: "시스템 운영 관리 > 배치작업 관리 > 배치 결과 조회",
    role: "R09 시스템관리자",
    primaryEntity: "batch_execution_result",
    operationId: "cmn_fr_081_search",
    apiPath: "/api/admin/cmn/fr/081",
    requirements: ["REQ-193", "REQ-194", "REQ-195"],
    archetype: "시스템 운영 관리 > 배치작업 관리 > 배치 결과 조회",
    goal: "배치 결과 조회에서 source-backed 조회/명령/검증 결과를 확인한다.",
    filters: [
      {
        key: "batchExecutionId",
        label: "배치 실행ID",
        readOnly: false,
      },
      {
        key: "batchId",
        label: "배치ID",
        readOnly: false,
      },
      {
        key: "period",
        label: "기간",
        readOnly: false,
      },
      {
        key: "field74772",
        label: "성공",
        readOnly: false,
      },
      {
        key: "field39609",
        label: "실패 여부",
        readOnly: false,
      },
    ],
    columns: [
      {
        key: "batchExecutionId",
        label: "배치 실행ID",
        readOnly: true,
      },
      {
        key: "startedAt",
        label: "시작시간",
        readOnly: true,
      },
      {
        key: "endedAt",
        label: "종료시간",
        readOnly: true,
      },
      {
        key: "processedCount",
        label: "처리건수",
        readOnly: true,
      },
      {
        key: "successCount",
        label: "성공건수",
        readOnly: true,
      },
      {
        key: "failureCount",
        label: "실패건수",
        readOnly: true,
      },
      {
        key: "excludedCount",
        label: "제외건수",
        readOnly: true,
      },
      {
        key: "duration",
        label: "소요시간",
        readOnly: true,
      },
      {
        key: "logFilePath",
        label: "로그파일",
        readOnly: true,
      },
    ],
    modalFields: [
      {
        key: "field56611",
        label: "검색조건만 입력",
        readOnly: true,
      },
      {
        key: "field24549",
        label: "재실행",
        readOnly: true,
      },
      {
        key: "field70765",
        label: "실패자료 변경 입력 없음",
        readOnly: true,
      },
    ],
    primaryActions: ["결과 검색", "결과 상세", "로그파일 상세"],
    constraint:
      "결과 조회 화면은 배치를 재실행하거나 실패자료·로그파일을 변경하지 않는다.",
    readOnly: true,
    mutationMethod: "GET",
  },
] satisfies ScreenContract[];

export const screenByRoute = (route: string) =>
  screens.find((screen) => screen.route === route);
