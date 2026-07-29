import type { ScreenKind } from "../types";
import { Button, Field } from "./ui";

const fieldsByKind: Record<ScreenKind, string[]> = {
  dashboard: ["상태"],
  users: ["교번", "성명", "소속", "직급", "재직상태", "역할", "사용여부"],
  organizations: ["기준일", "조직코드", "조직유형"],
  roles: ["사용여부"],
  userRoles: ["교번", "성명", "activeOnly"],
  menuPermissions: ["targetType", "targetId"],
  menuStructure: ["includeInactive", "level"],
  menuInfo: ["메뉴 선택"],
  codeGroups: ["groupId", "groupName", "useYn"],
  detailCodes: ["parentDetailCodeId", "useYn"],
};

const primaryActions: Record<ScreenKind, string> = {
  dashboard: "상태 새로고침",
  users: "저장",
  organizations: "관계 저장",
  roles: "저장",
  userRoles: "역할 부여",
  menuPermissions: "matrix 저장",
  menuStructure: "구조 저장",
  menuInfo: "실행정보 저장",
  codeGroups: "등록/수정",
  detailCodes: "등록/수정",
};

export function ScreenToolbar({
  kind,
  onRefresh,
}: {
  kind: ScreenKind;
  onRefresh: () => void;
}) {
  return (
    <div className="rounded-xl border border-slate-200 bg-slate-50/80 p-4">
      <div className="grid gap-3 md:grid-cols-2 xl:grid-cols-4">
        {fieldsByKind[kind].map((label) => (
          <Field key={label} label={label} />
        ))}
      </div>
      <div className="mt-4 flex flex-wrap items-center gap-2">
        <Button
          className="border border-slate-200 bg-white text-slate-900 hover:bg-slate-100"
          onClick={onRefresh}
        >
          조회
        </Button>
        <Button className="bg-slate-950 text-white hover:bg-slate-800">
          {primaryActions[kind]}
        </Button>
        {kind !== "dashboard" && (
          <Button className="border border-rose-200 bg-rose-50 text-rose-700 hover:bg-rose-100">
            비활성화 확인
          </Button>
        )}
      </div>
    </div>
  );
}
