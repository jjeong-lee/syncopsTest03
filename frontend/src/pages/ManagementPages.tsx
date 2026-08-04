import { useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  codeGroupApi,
  detailCodeApi,
  menuApi,
  menuPermissionApi,
  organizationApi,
  roleApi,
  userApi,
  userRoleApi,
} from "../api/domain";
import type {
  CodeGroup,
  DetailCode,
  Menu,
  MenuPermission,
  Organization,
  Role,
  UserRole,
  UserSummary,
} from "../types";
import {
  CheckboxCell,
  ConfirmModal,
  DataTable,
  DetailShell,
  Input,
  PageFrame,
  roles,
  SearchPanel,
  Select,
  StatePanel,
  Textarea,
  Toast,
  usePage,
  useYn,
} from "../ui";

const emptyRole: Role = {
  roleCode: "R01",
  roleName: "",
  purpose: "",
  grantCriteria: "",
  dataScopeDefault: "",
  useYn: "Y",
};
const emptyMenu: Menu = {
  menuId: "",
  menuLevel: 3,
  displayOrder: 99,
  menuName: "",
  screenId: "",
  url: "/system/",
  icon: "",
  businessType: "",
  description: "",
  activeYn: "Y",
};
const emptyGroup: CodeGroup = {
  groupId: "",
  groupName: "",
  description: "",
  managementDepartment: "",
  useYn: "Y",
};
const emptyDetail = (groupId: string): DetailCode => ({
  groupId,
  codeValue: "",
  codeName: "",
  parentCodeValue: "",
  sortOrder: 1,
  extraAttributes: {},
  useYn: "Y",
  validFrom: "",
  validTo: "",
});
const today = () => new Date().toISOString().slice(0, 10);

export function UserManagementPage() {
  const [filters, setFilters] = useState<Record<string, string>>({});
  const [selected, setSelected] = useState<UserSummary | null>(null);
  const [toast, setToast] = useState("");
  const [error, setError] = useState("");
  const page = usePage<UserSummary>(() => userApi.search(filters), [filters]);
  const save = (kind: "usage" | "roles") => {
    if (!selected) return;
    setError("");
    const request =
      kind === "usage"
        ? userApi.updateUsage(
            selected.userId,
            selected.systemUseYn,
            "화면 저장",
          )
        : userApi.updateRoles(
            selected.userId,
            selected.roles,
            selected.userId,
            today(),
            "화면 저장",
          );
    request
      .then((row) => {
        setSelected(row);
        setToast(
          kind === "usage"
            ? "사용여부가 저장되었습니다."
            : "업무 역할이 저장되었습니다.",
        );
        void page.load();
      })
      .catch((err) => setError(err.message));
  };
  return (
    <PageFrame title="사용자 관리">
      <Toast message={toast} />
      <SearchPanel onSearch={page.load}>
        <Input
          label="교번"
          onChange={(v) => setFilters({ ...filters, staffNo: v })}
        />
        <Input
          label="성명"
          onChange={(v) => setFilters({ ...filters, staffName: v })}
        />
        <Input
          label="소속코드"
          onChange={(v) => setFilters({ ...filters, organizationCode: v })}
        />
        <Input
          label="직급"
          onChange={(v) => setFilters({ ...filters, positionName: v })}
        />
        <Select
          label="재직상태"
          values={["ACTIVE", "LEAVE", "RETIRED"]}
          onChange={(v) => setFilters({ ...filters, employmentStatus: v })}
        />
        <Select
          label="역할"
          values={roles}
          onChange={(v) => setFilters({ ...filters, roleCode: v })}
        />
        <Select
          label="사용여부"
          values={useYn}
          onChange={(v) => setFilters({ ...filters, systemUseYn: v })}
        />
      </SearchPanel>
      <StatePanel state={page.state} title="사용자 목록" message={page.error} />
      <DataTable
        rows={page.rows as unknown as Record<string, unknown>[]}
        selectedKey={selected?.staffNo}
        columns={[
          ["staffNo", "교번"],
          ["staffName", "성명"],
          ["organizationName", "소속"],
          ["positionName", "직급"],
          ["employmentStatus", "재직상태"],
          ["dutyName", "보직"],
          ["retirementDate", "퇴직일자"],
          ["lastSyncedAt", "최종 동기화"],
          ["systemUseYn", "사용"],
          ["roles", "역할"],
        ]}
        onSelect={(r) => setSelected(r as unknown as UserSummary)}
      />
      {selected && (
        <DetailShell title="선택 사용자 상세/편집">
          <Input label="교번" value={selected.staffNo} readOnly />
          <Input label="성명" value={selected.staffName} readOnly />
          <Input
            label="소속"
            value={selected.organizationName ?? selected.organizationCode}
            readOnly
          />
          <Input label="직급" value={selected.positionName ?? ""} readOnly />
          <Select
            label="시스템 사용여부"
            values={useYn}
            includeAll={false}
            value={selected.systemUseYn}
            onChange={(v) =>
              setSelected({ ...selected, systemUseYn: v as never })
            }
          />
          <Select
            label="업무 역할"
            values={roles}
            value={selected.roles[0]}
            onChange={(v) =>
              setSelected({ ...selected, roles: v ? [v as never] : [] })
            }
          />
          {error && (
            <div className="inline-error" role="alert">
              {error}
            </div>
          )}
          <div className="button-row">
            <button onClick={() => save("usage")}>사용여부 저장</button>
            <button onClick={() => save("roles")}>역할 저장</button>
            <button className="secondary" onClick={() => setSelected(null)}>
              취소
            </button>
          </div>
        </DetailShell>
      )}
    </PageFrame>
  );
}

export function OrganizationManagementPage() {
  const [filters, setFilters] = useState<Record<string, string>>({});
  const [selected, setSelected] = useState<Organization | null>(null);
  const [confirm, setConfirm] = useState(false);
  const [toast, setToast] = useState("");
  const [error, setError] = useState("");
  const page = usePage<Organization>(
    () => organizationApi.search(filters),
    [filters],
  );
  const tree = usePage<Organization>(() => organizationApi.tree(), []);
  const save = () =>
    selected &&
    organizationApi
      .updateRelation(selected.organizationCode, {
        parentOrganizationCode: selected.parentOrganizationCode ?? "",
        effectiveStartDate: selected.effectiveStartDate,
        effectiveEndDate: selected.effectiveEndDate ?? "",
        relationChangeReason: selected.relationChangeReason ?? "화면 저장",
      })
      .then((row) => {
        setSelected(row);
        setConfirm(false);
        setToast("조직 관계가 저장되었습니다.");
        void page.load();
        void tree.load();
      })
      .catch((err) => setError(err.message));
  return (
    <PageFrame title="조직 관리">
      <Toast message={toast} />
      <SearchPanel onSearch={page.load}>
        <Input
          label="조직코드"
          onChange={(v) => setFilters({ ...filters, organizationCode: v })}
        />
        <Input
          label="조직명"
          onChange={(v) => setFilters({ ...filters, organizationName: v })}
        />
        <Select
          label="조직유형"
          values={[
            "UNIVERSITY",
            "GRADUATE_SCHOOL",
            "COLLEGE",
            "DEPARTMENT",
            "OFFICE",
          ]}
          onChange={(v) => setFilters({ ...filters, organizationType: v })}
        />
        <Select
          label="사용여부"
          values={useYn}
          onChange={(v) => setFilters({ ...filters, useYn: v })}
        />
      </SearchPanel>
      <div className="split">
        <section className="card">
          <StatePanel
            state={page.state}
            title="조직 목록"
            message={page.error}
          />
          <DataTable
            rows={page.rows as unknown as Record<string, unknown>[]}
            columns={[
              ["organizationCode", "조직코드"],
              ["organizationName", "조직명"],
              ["organizationType", "유형"],
              ["effectiveStartDate", "적용시작"],
              ["effectiveEndDate", "적용종료"],
              ["useYn", "사용"],
            ]}
            onSelect={(r) => setSelected(r as unknown as Organization)}
          />
        </section>
        <section className="card tree-card">
          <StatePanel
            state={tree.state}
            title="조직 계층"
            message={tree.error}
          />
          <ul className="tree-list">
            {tree.rows.map((o) => (
              <li
                key={o.organizationCode}
                style={{
                  paddingLeft: `${o.parentOrganizationCode ? 18 : 0}px`,
                }}
              >
                <strong>{o.organizationName}</strong>
                <span>
                  {o.organizationCode} · {o.organizationType}
                </span>
              </li>
            ))}
          </ul>
        </section>
      </div>
      {selected && (
        <DetailShell title="조직 관계 편집">
          <Input label="조직코드" value={selected.organizationCode} readOnly />
          <Input
            label="상위조직코드"
            value={selected.parentOrganizationCode ?? ""}
            onChange={(v) =>
              setSelected({ ...selected, parentOrganizationCode: v })
            }
          />
          <Input
            label="적용 시작일"
            type="date"
            value={selected.effectiveStartDate}
            onChange={(v) =>
              setSelected({ ...selected, effectiveStartDate: v })
            }
          />
          <Input
            label="적용 종료일"
            type="date"
            value={selected.effectiveEndDate ?? ""}
            onChange={(v) => setSelected({ ...selected, effectiveEndDate: v })}
          />
          <Textarea
            label="변경 사유"
            value={selected.relationChangeReason ?? ""}
            onChange={(v) =>
              setSelected({ ...selected, relationChangeReason: v })
            }
          />
          {error && (
            <div className="inline-error" role="alert">
              {error}
            </div>
          )}
          <div className="button-row">
            <button onClick={() => setConfirm(true)}>관계 저장</button>
            <button className="secondary" onClick={() => setSelected(null)}>
              취소
            </button>
          </div>
        </DetailShell>
      )}
      <ConfirmModal
        open={confirm}
        title="조직 관계 저장 확인"
        description="상위조직과 적용기간을 변경합니다. 기존 이력은 DB에 보존됩니다."
        onConfirm={save}
        onClose={() => setConfirm(false)}
      />
    </PageFrame>
  );
}

export function RoleManagementPage() {
  const [filter, setFilter] = useState("");
  const [selected, setSelected] = useState<Role>({ ...emptyRole });
  const [mode, setMode] = useState<"create" | "edit">("create");
  const [toast, setToast] = useState("");
  const [error, setError] = useState("");
  const page = usePage<Role>(() => roleApi.list(filter), [filter]);
  const save = () => {
    setError("");
    (mode === "create"
      ? roleApi.create(selected)
      : roleApi.update(selected.roleCode, selected)
    )
      .then((r) => {
        setSelected(r);
        setMode("edit");
        setToast("역할 정보가 저장되었습니다.");
        void page.load();
      })
      .catch((err) => setError(err.message));
  };
  return (
    <PageFrame title="역할 관리">
      <Toast message={toast} />
      <SearchPanel onSearch={page.load}>
        <Input label="역할코드/명" value={filter} onChange={setFilter} />
      </SearchPanel>
      <StatePanel state={page.state} title="역할 목록" message={page.error} />
      <DataTable
        rows={page.rows as unknown as Record<string, unknown>[]}
        columns={[
          ["roleCode", "role_code"],
          ["roleName", "role_name"],
          ["purpose", "purpose"],
          ["grantCriteria", "grant_criteria"],
          ["dataScopeDefault", "data_scope_default"],
          ["useYn", "use_yn"],
        ]}
        onSelect={(r) => {
          setSelected(r as unknown as Role);
          setMode("edit");
        }}
      />
      <DetailShell title={mode === "create" ? "신규 역할 등록" : "역할 수정"}>
        <Select
          label="role_code"
          values={roles}
          includeAll={false}
          value={selected.roleCode}
          onChange={(v) => setSelected({ ...selected, roleCode: v as never })}
        />
        <Input
          label="role_name"
          value={selected.roleName}
          onChange={(v) => setSelected({ ...selected, roleName: v })}
        />
        <Input
          label="purpose"
          value={selected.purpose}
          onChange={(v) => setSelected({ ...selected, purpose: v })}
        />
        <Input
          label="grant_criteria"
          value={selected.grantCriteria}
          onChange={(v) => setSelected({ ...selected, grantCriteria: v })}
        />
        <Input
          label="data_scope_default"
          value={selected.dataScopeDefault}
          onChange={(v) => setSelected({ ...selected, dataScopeDefault: v })}
        />
        <Select
          label="use_yn"
          values={useYn}
          includeAll={false}
          value={selected.useYn}
          onChange={(v) => setSelected({ ...selected, useYn: v as never })}
        />
        {error && (
          <div className="inline-error" role="alert">
            {error}
          </div>
        )}
        <div className="button-row">
          <button onClick={save}>
            {mode === "create" ? "신규 저장" : "수정 저장"}
          </button>
          <button
            className="secondary"
            onClick={() => {
              setSelected({ ...emptyRole });
              setMode("create");
            }}
          >
            취소
          </button>
        </div>
      </DetailShell>
    </PageFrame>
  );
}

export function UserRoleManagementPage() {
  const [filters, setFilters] = useState<Record<string, string>>({});
  const [selected, setSelected] = useState<UserRole | null>(null);
  const [confirm, setConfirm] = useState(false);
  const [form, setForm] = useState({
    userId: "",
    roleCode: "",
    assignmentType: "MANUAL",
    validFrom: today(),
    validTo: "",
    approvedBy: "",
    changeReason: "",
  });
  const [toast, setToast] = useState("");
  const [error, setError] = useState("");
  const page = usePage<UserRole>(() => userRoleApi.list(filters), [filters]);
  const grant = () =>
    userRoleApi
      .grant(form)
      .then(() => {
        setToast("역할이 부여되었습니다.");
        void page.load();
      })
      .catch((err) => setError(err.message));
  const revoke = () =>
    selected &&
    userRoleApi
      .revoke({
        userRoleId: selected.userRoleId,
        validTo: today(),
        changeReason: form.changeReason || "화면 회수",
      })
      .then(() => {
        setConfirm(false);
        setToast("역할이 회수되었습니다.");
        void page.load();
      })
      .catch((err) => setError(err.message));
  return (
    <PageFrame title="사용자 역할 관리">
      <Toast message={toast} />
      <SearchPanel onSearch={page.load}>
        <Input
          label="사용자ID"
          onChange={(v) => setFilters({ ...filters, userId: v })}
        />
        <Select
          label="역할"
          values={roles}
          onChange={(v) => setFilters({ ...filters, roleCode: v })}
        />
        <Select
          label="assignment_type"
          values={["POSITION_BASED", "MANUAL"]}
          onChange={(v) => setFilters({ ...filters, assignmentType: v })}
        />
        <Input
          label="유효일"
          type="date"
          onChange={(v) => setFilters({ ...filters, validOn: v })}
        />
      </SearchPanel>
      <StatePanel
        state={page.state}
        title="사용자 역할 목록"
        message={page.error}
      />
      <DataTable
        rows={page.rows as unknown as Record<string, unknown>[]}
        columns={[
          ["userId", "사용자"],
          ["roleCode", "현재 역할"],
          ["assignmentType", "assignment_type"],
          ["validFrom", "valid_from"],
          ["validTo", "valid_to"],
          ["approvedBy", "approved_by"],
          ["useYn", "use_yn"],
        ]}
        onSelect={(r) => setSelected(r as unknown as UserRole)}
      />
      <DetailShell title="역할 부여/회수">
        <Input
          label="사용자ID"
          value={form.userId}
          onChange={(v) => setForm({ ...form, userId: v })}
        />
        <Select
          label="역할"
          values={roles}
          value={form.roleCode}
          onChange={(v) => setForm({ ...form, roleCode: v })}
        />
        <Select
          label="assignment_type"
          values={["MANUAL", "POSITION_BASED"]}
          includeAll={false}
          value={form.assignmentType}
          onChange={(v) => setForm({ ...form, assignmentType: v })}
        />
        <Input
          label="valid_from"
          type="date"
          value={form.validFrom}
          onChange={(v) => setForm({ ...form, validFrom: v })}
        />
        <Input
          label="valid_to"
          type="date"
          value={form.validTo}
          onChange={(v) => setForm({ ...form, validTo: v })}
        />
        <Input
          label="approved_by"
          value={form.approvedBy}
          onChange={(v) => setForm({ ...form, approvedBy: v })}
        />
        <Textarea
          label="변경/회수 사유"
          value={form.changeReason}
          onChange={(v) => setForm({ ...form, changeReason: v })}
        />
        {error && (
          <div className="inline-error" role="alert">
            {error}
          </div>
        )}
        <div className="button-row">
          <button
            onClick={grant}
            disabled={!form.userId || !form.roleCode || !form.approvedBy}
          >
            역할 부여
          </button>
          <button
            className="danger"
            onClick={() => setConfirm(true)}
            disabled={!selected}
          >
            회수
          </button>
          <button className="secondary" onClick={() => setSelected(null)}>
            취소
          </button>
        </div>
      </DetailShell>
      <ConfirmModal
        open={confirm}
        title="역할 회수 확인"
        description="선택한 사용자 역할을 종료하고 이력을 남깁니다."
        confirmText="회수"
        onConfirm={revoke}
        onClose={() => setConfirm(false)}
      />
    </PageFrame>
  );
}

export function MenuPermissionManagementPage() {
  const [targetType, setTargetType] = useState("ROLE");
  const [targetId, setTargetId] = useState("R09");
  const [toast, setToast] = useState("");
  const [error, setError] = useState("");
  const page = usePage<MenuPermission>(
    () =>
      targetId
        ? menuPermissionApi.list({ targetType, targetId })
        : Promise.resolve({
            items: [],
            page: 0,
            size: 0,
            totalElements: 0,
            totalPages: 0,
          }),
    [targetType, targetId],
  );
  const update = (
    idx: number,
    key: "accessAllowedYn" | "explicitDenyYn",
    value: "Y" | "N",
  ) =>
    page.setRows(
      page.rows.map((row, i) => (i === idx ? { ...row, [key]: value } : row)),
    );
  const save = () =>
    menuPermissionApi
      .save(targetType, targetId, page.rows)
      .then((r) => {
        setToast(`${r.savedCount}개 권한이 저장되었습니다.`);
        void page.load();
      })
      .catch((err) => setError(err.message));
  return (
    <PageFrame title="메뉴 권한 관리">
      <Toast message={toast} />
      <SearchPanel onSearch={page.load}>
        <Select
          label="대상유형"
          values={["ROLE", "ORGANIZATION", "USER"]}
          includeAll={false}
          value={targetType}
          onChange={setTargetType}
        />
        <Input label="대상ID" value={targetId} onChange={setTargetId} />
      </SearchPanel>
      <StatePanel state={page.state} title="권한 grid" message={page.error} />
      <section className="card">
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>메뉴</th>
                <th>대상</th>
                <th>접근 허용</th>
                <th>명시 차단</th>
              </tr>
            </thead>
            <tbody>
              {page.rows.length === 0 ? (
                <tr>
                  <td colSpan={4} className="empty-cell">
                    대상 권한을 조회하세요.
                  </td>
                </tr>
              ) : (
                page.rows.map((p, idx) => (
                  <tr key={`${p.targetType}-${p.targetId}-${p.menuId}`}>
                    <td>{p.menuId}</td>
                    <td>
                      {p.targetType}:{p.targetId}
                    </td>
                    <td>
                      <CheckboxCell
                        value={p.accessAllowedYn}
                        onChange={(v) => update(idx, "accessAllowedYn", v)}
                      />
                    </td>
                    <td>
                      <CheckboxCell
                        value={p.explicitDenyYn}
                        onChange={(v) => update(idx, "explicitDenyYn", v)}
                      />
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </section>
      {error && (
        <div className="inline-error" role="alert">
          {error}
        </div>
      )}
      <div className="button-row">
        <button onClick={save} disabled={!targetId || page.rows.length === 0}>
          권한 저장
        </button>
        <button className="secondary" onClick={page.load}>
          취소
        </button>
      </div>
    </PageFrame>
  );
}

export function MenuStructureManagementPage() {
  const page = usePage<Menu>(() => menuApi.tree(), []);
  const [toast, setToast] = useState("");
  const [error, setError] = useState("");
  const update = (
    idx: number,
    key: "parentMenuId" | "displayOrder",
    value: string,
  ) =>
    page.setRows(
      page.rows.map((m, i) =>
        i === idx
          ? {
              ...m,
              [key]:
                key === "displayOrder" ? Number(value) : value || undefined,
            }
          : m,
      ),
    );
  const save = () =>
    menuApi
      .updateHierarchy(
        page.rows.map((m) => ({
          menuId: m.menuId,
          parentMenuId: m.parentMenuId,
          displayOrder: m.displayOrder,
        })),
      )
      .then(() => {
        setToast("메뉴 계층이 저장되었습니다.");
        void page.load();
      })
      .catch((err) => setError(err.message));
  return (
    <PageFrame
      title="메뉴 구조 관리"
      actions={<button onClick={page.load}>메뉴 계층 새로고침</button>}
    >
      <Toast message={toast} />
      <StatePanel state={page.state} title="Tree editor" message={page.error} />
      <section className="card structure-list">
        {page.rows.map((m, idx) => (
          <div
            className="structure-row"
            key={m.menuId}
            style={{ marginLeft: `${(m.menuLevel - 1) * 14}px` }}
          >
            <strong>{m.menuName}</strong>
            <span>
              {m.menuId} · {m.url || "폴더"}
            </span>
            <Input
              label="parent_menu_id"
              value={m.parentMenuId ?? ""}
              onChange={(v) => update(idx, "parentMenuId", v)}
            />
            <Input
              label="display_order"
              type="number"
              value={String(m.displayOrder)}
              onChange={(v) => update(idx, "displayOrder", v)}
            />
          </div>
        ))}
      </section>
      {error && (
        <div className="inline-error" role="alert">
          {error}
        </div>
      )}
      <div className="button-row">
        <button onClick={save}>재정렬 저장</button>
        <button className="secondary" onClick={page.load}>
          취소
        </button>
      </div>
    </PageFrame>
  );
}

export function MenuInfoManagementPage() {
  const [filters, setFilters] = useState<Record<string, string>>({});
  const [selected, setSelected] = useState<Menu>({ ...emptyMenu });
  const [mode, setMode] = useState<"create" | "edit">("create");
  const [toast, setToast] = useState("");
  const [error, setError] = useState("");
  const page = usePage<Menu>(() => menuApi.list(filters), [filters]);
  const save = () =>
    (mode === "create"
      ? menuApi.create(selected)
      : menuApi.update(selected.menuId, selected)
    )
      .then((m) => {
        setSelected(m);
        setMode("edit");
        setToast("메뉴 실행정보가 저장되었습니다.");
        void page.load();
      })
      .catch((err) => setError(err.message));
  return (
    <PageFrame title="메뉴 정보 관리">
      <Toast message={toast} />
      <SearchPanel onSearch={page.load}>
        <Input
          label="메뉴명"
          onChange={(v) => setFilters({ ...filters, menuName: v })}
        />
        <Input
          label="screen_id"
          onChange={(v) => setFilters({ ...filters, screenId: v })}
        />
        <Input
          label="URL"
          onChange={(v) => setFilters({ ...filters, url: v })}
        />
        <Select
          label="active_yn"
          values={useYn}
          onChange={(v) => setFilters({ ...filters, activeYn: v })}
        />
      </SearchPanel>
      <StatePanel
        state={page.state}
        title="메뉴 실행정보 목록"
        message={page.error}
      />
      <DataTable
        rows={page.rows as unknown as Record<string, unknown>[]}
        columns={[
          ["menuId", "menu_id"],
          ["menuName", "menu_name"],
          ["screenId", "screen_id"],
          ["url", "url"],
          ["icon", "icon"],
          ["businessType", "business_type"],
          ["activeYn", "active_yn"],
        ]}
        onSelect={(r) => {
          setSelected(r as unknown as Menu);
          setMode("edit");
        }}
      />
      <DetailShell title={mode === "create" ? "메뉴 등록" : "메뉴 수정"}>
        <Input
          label="menu_id"
          value={selected.menuId}
          readOnly={mode === "edit"}
          onChange={(v) => setSelected({ ...selected, menuId: v })}
        />
        <Input
          label="menu_name"
          value={selected.menuName}
          onChange={(v) => setSelected({ ...selected, menuName: v })}
        />
        <Input
          label="screen_id"
          value={selected.screenId ?? ""}
          onChange={(v) => setSelected({ ...selected, screenId: v })}
        />
        <Input
          label="url"
          value={selected.url ?? ""}
          onChange={(v) => setSelected({ ...selected, url: v })}
        />
        <Input
          label="icon"
          value={selected.icon ?? ""}
          onChange={(v) => setSelected({ ...selected, icon: v })}
        />
        <Input
          label="business_type"
          value={selected.businessType ?? ""}
          onChange={(v) => setSelected({ ...selected, businessType: v })}
        />
        <Textarea
          label="description"
          value={selected.description ?? ""}
          onChange={(v) => setSelected({ ...selected, description: v })}
        />
        <Select
          label="active_yn"
          values={useYn}
          includeAll={false}
          value={selected.activeYn}
          onChange={(v) => setSelected({ ...selected, activeYn: v as never })}
        />
        {error && (
          <div className="inline-error" role="alert">
            {error}
          </div>
        )}
        <div className="button-row">
          <button onClick={save}>
            {mode === "create" ? "신규 저장" : "수정 저장"}
          </button>
          <button
            className="secondary"
            onClick={() => {
              setSelected({ ...emptyMenu });
              setMode("create");
            }}
          >
            취소
          </button>
        </div>
      </DetailShell>
    </PageFrame>
  );
}

export function CodeGroupManagementPage() {
  const [filters, setFilters] = useState<Record<string, string>>({});
  const [selected, setSelected] = useState<CodeGroup>({ ...emptyGroup });
  const [mode, setMode] = useState<"create" | "edit">("create");
  const [toast, setToast] = useState("");
  const [error, setError] = useState("");
  const navigate = useNavigate();
  const page = usePage<CodeGroup>(() => codeGroupApi.list(filters), [filters]);
  const save = () =>
    (mode === "create"
      ? codeGroupApi.create(selected)
      : codeGroupApi.update(selected.groupId, selected)
    )
      .then((g) => {
        setSelected(g);
        setMode("edit");
        setToast("코드그룹이 저장되었습니다.");
        void page.load();
      })
      .catch((err) => setError(err.message));
  return (
    <PageFrame title="코드그룹 관리">
      <Toast message={toast} />
      <SearchPanel onSearch={page.load}>
        <Input
          label="group_id"
          onChange={(v) => setFilters({ ...filters, groupId: v })}
        />
        <Input
          label="group_name"
          onChange={(v) => setFilters({ ...filters, groupName: v })}
        />
        <Input
          label="관리부서"
          onChange={(v) => setFilters({ ...filters, managementDepartment: v })}
        />
        <Select
          label="use_yn"
          values={useYn}
          onChange={(v) => setFilters({ ...filters, useYn: v })}
        />
      </SearchPanel>
      <StatePanel
        state={page.state}
        title="코드그룹 목록"
        message={page.error}
      />
      <DataTable
        rows={page.rows as unknown as Record<string, unknown>[]}
        columns={[
          ["groupId", "group_id"],
          ["groupName", "group_name"],
          ["description", "description"],
          ["managementDepartment", "management_department"],
          ["useYn", "use_yn"],
        ]}
        onSelect={(r) => {
          setSelected(r as unknown as CodeGroup);
          setMode("edit");
        }}
      />
      <DetailShell
        title={mode === "create" ? "코드그룹 등록" : "코드그룹 수정"}
      >
        <Input
          label="group_id"
          value={selected.groupId}
          readOnly={mode === "edit"}
          onChange={(v) => setSelected({ ...selected, groupId: v })}
        />
        <Input
          label="group_name"
          value={selected.groupName}
          onChange={(v) => setSelected({ ...selected, groupName: v })}
        />
        <Textarea
          label="description"
          value={selected.description ?? ""}
          onChange={(v) => setSelected({ ...selected, description: v })}
        />
        <Input
          label="management_department"
          value={selected.managementDepartment}
          onChange={(v) =>
            setSelected({ ...selected, managementDepartment: v })
          }
        />
        <Select
          label="use_yn"
          values={useYn}
          includeAll={false}
          value={selected.useYn}
          onChange={(v) => setSelected({ ...selected, useYn: v as never })}
        />
        {error && (
          <div className="inline-error" role="alert">
            {error}
          </div>
        )}
        <div className="button-row">
          <button onClick={save}>{mode === "create" ? "등록" : "수정"}</button>
          <button
            className="secondary"
            onClick={() => {
              setSelected({ ...emptyGroup });
              setMode("create");
            }}
          >
            취소
          </button>
          <button
            className="secondary"
            onClick={() =>
              navigate(`/system/detail-codes?groupId=${selected.groupId}`)
            }
            disabled={!selected.groupId}
          >
            상세코드로 이동
          </button>
        </div>
      </DetailShell>
    </PageFrame>
  );
}

export function DetailCodeManagementPage() {
  const initialGroup =
    new URLSearchParams(window.location.search).get("groupId") ?? "";
  const [groupId, setGroupId] = useState(initialGroup);
  const [filters, setFilters] = useState<Record<string, string>>({});
  const [selected, setSelected] = useState<DetailCode>(
    emptyDetail(initialGroup),
  );
  const [mode, setMode] = useState<"create" | "edit">("create");
  const [toast, setToast] = useState("");
  const [error, setError] = useState("");
  const page = usePage<DetailCode>(
    () =>
      groupId
        ? detailCodeApi.list(groupId, filters)
        : Promise.resolve({
            items: [],
            page: 0,
            size: 0,
            totalElements: 0,
            totalPages: 0,
          }),
    [groupId, filters],
  );
  const save = () => {
    const body = { ...selected, groupId };
    return (
      mode === "create"
        ? detailCodeApi.create(groupId, body)
        : detailCodeApi.update(groupId, selected.codeValue, body)
    )
      .then((d) => {
        setSelected(d);
        setMode("edit");
        setToast("상세코드가 저장되었습니다.");
        void page.load();
      })
      .catch((err) => setError(err.message));
  };
  return (
    <PageFrame title="상세코드 관리">
      <Toast message={toast} />
      <SearchPanel onSearch={page.load}>
        <Input
          label="코드그룹"
          value={groupId}
          onChange={(v) => {
            setGroupId(v);
            setSelected(emptyDetail(v));
          }}
        />
        <Input
          label="code_value"
          onChange={(v) => setFilters({ ...filters, codeValue: v })}
        />
        <Input
          label="code_name"
          onChange={(v) => setFilters({ ...filters, codeName: v })}
        />
        <Select
          label="use_yn"
          values={useYn}
          onChange={(v) => setFilters({ ...filters, useYn: v })}
        />
      </SearchPanel>
      {!groupId && (
        <StatePanel
          state="empty"
          title="코드그룹 선택"
          message="코드그룹 관리에서 확인한 group_id를 입력하세요."
        />
      )}
      <StatePanel
        state={groupId ? page.state : "idle"}
        title="상세코드 목록"
        message={page.error}
      />
      <DataTable
        rows={page.rows as unknown as Record<string, unknown>[]}
        columns={[
          ["codeValue", "code_value"],
          ["codeName", "code_name"],
          ["parentCodeValue", "parent_code_value"],
          ["sortOrder", "sort_order"],
          ["useYn", "use_yn"],
          ["validFrom", "valid_from"],
          ["validTo", "valid_to"],
        ]}
        onSelect={(r) => {
          setSelected(r as unknown as DetailCode);
          setMode("edit");
        }}
      />
      <DetailShell
        title={mode === "create" ? "상세코드 등록" : "상세코드 수정"}
      >
        <Input label="group_id" value={groupId} readOnly />
        <Input
          label="code_value"
          value={selected.codeValue}
          readOnly={mode === "edit"}
          onChange={(v) => setSelected({ ...selected, codeValue: v })}
        />
        <Input
          label="code_name"
          value={selected.codeName}
          onChange={(v) => setSelected({ ...selected, codeName: v })}
        />
        <Input
          label="parent_code_value"
          value={selected.parentCodeValue ?? ""}
          onChange={(v) => setSelected({ ...selected, parentCodeValue: v })}
        />
        <Input
          label="sort_order"
          type="number"
          value={String(selected.sortOrder)}
          onChange={(v) => setSelected({ ...selected, sortOrder: Number(v) })}
        />
        <Textarea
          label="extra_attributes JSON"
          value={JSON.stringify(selected.extraAttributes ?? {})}
          onChange={(v) => {
            try {
              setSelected({
                ...selected,
                extraAttributes: JSON.parse(v || "{}"),
              });
              setError("");
            } catch {
              setError("extra_attributes는 JSON 형식이어야 합니다.");
            }
          }}
        />
        <Select
          label="use_yn"
          values={useYn}
          includeAll={false}
          value={selected.useYn}
          onChange={(v) => setSelected({ ...selected, useYn: v as never })}
        />
        <Input
          label="valid_from"
          type="date"
          value={selected.validFrom ?? ""}
          onChange={(v) => setSelected({ ...selected, validFrom: v })}
        />
        <Input
          label="valid_to"
          type="date"
          value={selected.validTo ?? ""}
          onChange={(v) => setSelected({ ...selected, validTo: v })}
        />
        {error && (
          <div className="inline-error" role="alert">
            {error}
          </div>
        )}
        <div className="button-row">
          <button
            onClick={save}
            disabled={!groupId || !selected.codeValue || !selected.codeName}
          >
            {mode === "create" ? "등록" : "수정"}
          </button>
          <button
            className="secondary"
            onClick={() => {
              setSelected(emptyDetail(groupId));
              setMode("create");
            }}
          >
            취소
          </button>
        </div>
      </DetailShell>
    </PageFrame>
  );
}
