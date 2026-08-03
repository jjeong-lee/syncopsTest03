import { useCallback, useEffect, useMemo, useState } from "react";
import { Link, useParams } from "react-router-dom";
import {
  AlertTriangle,
  CheckCircle2,
  Loader2,
  Plus,
  Save,
  Search,
  Trash2,
  Undo2,
} from "lucide-react";
import { api, Page } from "../api/client";
import { FieldConfig, ScreenConfig } from "./screens";

type Row = Record<string, unknown>;
type StatusType = "success" | "error" | "permission" | "empty" | "loading";
type Status = { type: StatusType; text: string } | null;

export function ManagementPage({
  screen,
  currentUserId,
}: {
  screen: ScreenConfig;
  currentUserId: string;
}) {
  const { groupId } = useParams();
  const [rows, setRows] = useState<Row[]>([]);
  const [selected, setSelected] = useState<Row | null>(null);
  const [draft, setDraft] = useState<Row>({});
  const [filters, setFilters] = useState<Row>(() =>
    defaultFilters(screen.kind),
  );
  const [reason, setReason] = useState("");
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [status, setStatus] = useState<Status>(null);

  const detailCodeNeedsGroup = screen.kind === "detailCodes" && !groupId;
  const listPath = useMemo(
    () => resolveListPath(screen, groupId, filters),
    [screen, groupId, filters],
  );

  const load = useCallback(async () => {
    if (detailCodeNeedsGroup) {
      setRows([]);
      setSelected(null);
      setDraft({});
      setStatus({
        type: "empty",
        text: "코드그룹 관리 화면에서 코드그룹을 선택하면 상세코드 route로 이동합니다.",
      });
      return;
    }
    setLoading(true);
    setStatus({
      type: "loading",
      text: `${screen.title} 데이터를 조회하는 중입니다.`,
    });
    try {
      const page = await api.get<Page<Row>>(listPath);
      setRows(page.items);
      const first = page.items[0] ?? null;
      setSelected(first);
      setDraft(
        first ? normalizeDraft(first, screen) : initialDraft(screen, groupId),
      );
      setStatus(
        page.items.length === 0
          ? {
              type: "empty",
              text: "검색 결과가 없습니다. 조건을 바꾸거나 신규 등록을 시작하세요.",
            }
          : null,
      );
    } catch (e) {
      setStatus({ type: errorType(e), text: errorText(e, "조회 실패") });
    } finally {
      setLoading(false);
    }
  }, [detailCodeNeedsGroup, groupId, listPath, screen]);

  useEffect(() => {
    setFilters(defaultFilters(screen.kind));
    setReason("");
  }, [screen.id, screen.kind]);

  useEffect(() => {
    void load();
  }, [load]);

  const selectRow = (row: Row) => {
    setSelected(row);
    setDraft(normalizeDraft(row, screen));
    setStatus(null);
  };

  const startCreate = () => {
    setSelected(null);
    setDraft(initialDraft(screen, groupId));
    setReason("");
    setStatus({
      type: "empty",
      text: "신규 등록 값을 입력한 뒤 등록 버튼을 누르세요.",
    });
  };

  const save = async (
    mode: "create" | "update" | "usage" | "roles" | "revoke" = selected
      ? "update"
      : "create",
  ) => {
    setSaving(true);
    setStatus(null);
    try {
      await mutate(screen, draft, reason, mode, groupId, currentUserId);
      setStatus({ type: "success", text: successMessage(screen, mode) });
      await load();
    } catch (e) {
      setStatus({ type: errorType(e), text: errorText(e, "저장 실패") });
    } finally {
      setSaving(false);
    }
  };

  return (
    <article className="page-stack">
      <section className="page-header card-panel">
        <div>
          <p className="breadcrumb">{screen.menuPath}</p>
          <h1>{screen.title}</h1>
          <p className="page-description">
            {screen.operationLabel} · {screen.primaryEntity}
          </p>
        </div>
        <div className="header-actions">
          {canCreate(screen.kind) && (
            <button
              className="outline-button"
              onClick={startCreate}
              type="button"
            >
              <Plus size={16} /> 신규 등록
            </button>
          )}
          <button className="primary-button" onClick={load} type="button">
            <Search size={16} /> {screen.filters.length ? "조회" : "트리 조회"}
          </button>
        </div>
      </section>

      {screen.kind === "detailCodes" && groupId && (
        <div className="notice">
          현재 코드그룹: <strong>{groupId}</strong>
        </div>
      )}

      <section className="card-panel toolbar" aria-label="검색 조건">
        {screen.filters.length === 0 ? (
          <span className="muted">이 화면은 메뉴 tree 전체를 조회합니다.</span>
        ) : (
          screen.filters.map((field) => (
            <FieldInput
              key={field.key}
              field={field}
              value={filters[field.key]}
              onChange={(value) =>
                setFilters((prev) => ({ ...prev, [field.key]: value }))
              }
              compact
            />
          ))
        )}
        <button
          className="ghost-button"
          onClick={() => setFilters(defaultFilters(screen.kind))}
          type="button"
        >
          <Undo2 size={15} /> 조건 초기화
        </button>
      </section>

      {status && <StateBlock status={status} />}

      <section className="data-grid">
        <div className="table-card">
          <div className="table-title">
            <strong>{screen.title} 목록</strong>
            <span>{rows.length}건</span>
          </div>
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  {screen.columns.map((column) => (
                    <th key={column.key}>{column.label}</th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {loading ? (
                  <SkeletonRows colSpan={screen.columns.length} />
                ) : rows.length === 0 ? (
                  <tr>
                    <td className="empty-cell" colSpan={screen.columns.length}>
                      표시할 데이터가 없습니다.
                    </td>
                  </tr>
                ) : (
                  rows.map((row, index) => (
                    <tr
                      key={rowKey(row, index)}
                      className={row === selected ? "selected" : ""}
                      onClick={() => selectRow(row)}
                    >
                      {screen.columns.map((column) => (
                        <td key={column.key}>
                          {formatValue(row[column.key], column)}
                        </td>
                      ))}
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>

        <aside className="detail-card card-panel">
          <div className="detail-title">
            <div>
              <strong>{selected ? "선택 행 상세/편집" : "신규 등록"}</strong>
              <p>
                {screen.kind === "users"
                  ? "KORUS 원천 필드는 읽기 전용입니다."
                  : "저장 후 목록을 다시 조회합니다."}
              </p>
            </div>
          </div>
          {detailCodeNeedsGroup ? (
            <div className="empty-detail">
              <Link to="/system/code-groups">코드그룹 관리에서 그룹 선택</Link>
            </div>
          ) : (
            <>
              <div className="form-grid">
                {screen.formFields.map((field) => (
                  <FieldInput
                    key={field.key}
                    field={field}
                    value={draft[field.key]}
                    onChange={(value) =>
                      setDraft((prev) => ({ ...prev, [field.key]: value }))
                    }
                  />
                ))}
                <label className="field wide">
                  <span>변경 사유</span>
                  <input
                    value={reason}
                    onChange={(event) => setReason(event.target.value)}
                    placeholder="변경 이력에 남길 사유"
                  />
                </label>
              </div>
              <div className="actions">
                {actionButtons(screen, selected).map((action) => (
                  <button
                    className={
                      action.variant === "danger"
                        ? "danger-button"
                        : action.variant === "outline"
                          ? "outline-button"
                          : "primary-button"
                    }
                    disabled={saving}
                    key={action.mode}
                    onClick={() => save(action.mode)}
                    type="button"
                  >
                    {action.icon === "trash" ? (
                      <Trash2 size={16} />
                    ) : (
                      <Save size={16} />
                    )}
                    {saving ? "처리 중..." : action.label}
                  </button>
                ))}
                <button
                  className="ghost-button"
                  onClick={() =>
                    selected ? selectRow(selected) : startCreate()
                  }
                  type="button"
                >
                  <Undo2 size={15} /> 취소
                </button>
                {screen.kind === "codeGroups" && draft.groupId ? (
                  <Link
                    className="link-button"
                    to={`/system/code-groups/${encodeURIComponent(String(draft.groupId))}/codes`}
                  >
                    상세코드 이동
                  </Link>
                ) : null}
              </div>
            </>
          )}
        </aside>
      </section>
    </article>
  );
}

function FieldInput({
  field,
  value,
  onChange,
  compact = false,
}: {
  field: FieldConfig;
  value: unknown;
  onChange: (value: unknown) => void;
  compact?: boolean;
}) {
  const inputValue =
    value == null
      ? ""
      : Array.isArray(value)
        ? value.join(",")
        : typeof value === "object"
          ? JSON.stringify(value)
          : String(value);
  if (field.kind === "select" || field.kind === "boolean") {
    const options =
      field.kind === "boolean" ? ["true", "false"] : (field.options ?? []);
    return (
      <label className={`field ${compact ? "compact-field" : ""}`}>
        <span>{field.label}</span>
        <select
          disabled={field.readonly}
          value={inputValue}
          onChange={(event) =>
            onChange(
              field.kind === "boolean"
                ? event.target.value === "true"
                : event.target.value,
            )
          }
        >
          <option value="">전체/미지정</option>
          {options.map((option) => (
            <option key={option} value={option}>
              {labelOption(option)}
            </option>
          ))}
        </select>
      </label>
    );
  }
  if (field.kind === "roles") {
    return (
      <label className="field wide">
        <span>{field.label}</span>
        <input
          readOnly={field.readonly}
          value={inputValue}
          onChange={(event) =>
            onChange(
              event.target.value
                .split(",")
                .map((item) => item.trim())
                .filter(Boolean),
            )
          }
          placeholder="R01,R09"
        />
      </label>
    );
  }
  return (
    <label
      className={`field ${compact ? "compact-field" : ""} ${field.kind === "json" ? "wide" : ""}`}
    >
      <span>
        {field.label}
        {field.required ? " *" : ""}
      </span>
      <input
        readOnly={field.readonly}
        type={
          field.kind === "date"
            ? "date"
            : field.kind === "number"
              ? "number"
              : "text"
        }
        value={inputValue}
        onChange={(event) =>
          onChange(
            field.kind === "number"
              ? toNumber(event.target.value)
              : field.kind === "json"
                ? event.target.value
                : event.target.value,
          )
        }
        placeholder={field.placeholder}
      />
    </label>
  );
}

function StateBlock({ status }: { status: NonNullable<Status> }) {
  const Icon =
    status.type === "loading"
      ? Loader2
      : status.type === "success"
        ? CheckCircle2
        : AlertTriangle;
  return (
    <div
      className={`state ${status.type}`}
      role={status.type === "error" ? "alert" : "status"}
    >
      <Icon size={18} className={status.type === "loading" ? "spin" : ""} />
      <span>{status.text}</span>
    </div>
  );
}

function SkeletonRows({ colSpan }: { colSpan: number }) {
  return (
    <>
      {[0, 1, 2, 3].map((row) => (
        <tr key={row}>
          <td colSpan={colSpan}>
            <div className="skeleton" />
          </td>
        </tr>
      ))}
    </>
  );
}

function actionButtons(
  screen: ScreenConfig,
  selected: Row | null,
): {
  mode: "create" | "update" | "usage" | "roles" | "revoke";
  label: string;
  variant?: "danger" | "outline";
  icon?: "trash";
}[] {
  if (screen.kind === "users")
    return [
      { mode: "usage", label: "사용여부 저장" },
      { mode: "roles", label: "업무 역할 저장", variant: "outline" },
    ];
  if (screen.kind === "userRoles")
    return selected
      ? [
          { mode: "create", label: "역할 부여" },
          {
            mode: "revoke",
            label: "선택 assignment 회수",
            variant: "danger",
            icon: "trash",
          },
        ]
      : [{ mode: "create", label: "역할 부여" }];
  return [
    { mode: selected ? "update" : "create", label: selected ? "저장" : "등록" },
  ];
}

function canCreate(kind: ScreenConfig["kind"]) {
  return [
    "roles",
    "userRoles",
    "menuInfo",
    "codeGroups",
    "detailCodes",
  ].includes(kind);
}

async function mutate(
  screen: ScreenConfig,
  row: Row,
  reason: string,
  mode: "create" | "update" | "usage" | "roles" | "revoke",
  groupId?: string,
  currentUserId?: string,
) {
  const clean = cleanRow(row);
  switch (screen.kind) {
    case "users":
      if (mode === "roles") {
        return api.put(
          `/api/admin/users/${encodeURIComponent(String(clean.userId))}/roles`,
          {
            roles: (Array.isArray(clean.roleCodes)
              ? clean.roleCodes
              : String(clean.roleCodes ?? "").split(",")
            )
              .filter(Boolean)
              .map((roleCode) => ({
                roleCode,
                assignmentSource: "MANUAL",
                effectiveStartDate: today(),
                effectiveEndDate: null,
                approvedBy: currentUserId,
              })),
            reason,
          },
        );
      }
      return api.patch(
        `/api/admin/users/${encodeURIComponent(String(clean.userId))}/usage`,
        { systemEnabled: Boolean(clean.systemEnabled), reason },
      );
    case "organizations":
      return api.put(
        `/api/admin/organizations/${encodeURIComponent(String(clean.organizationCode))}/relationships`,
        {
          parentOrganizationCode: clean.parentOrganizationCode || null,
          effectiveStartDate: clean.effectiveStartDate,
          effectiveEndDate: clean.effectiveEndDate || null,
          reason,
        },
      );
    case "roles": {
      const body = {
        roleCode: clean.roleCode,
        roleName: clean.roleName,
        purpose: clean.purpose,
        grantCriteria: clean.grantCriteria,
        defaultDataScope: clean.defaultDataScope,
        reason,
      };
      return mode === "create"
        ? api.post("/api/admin/roles", body)
        : api.put(
            `/api/admin/roles/${encodeURIComponent(String(clean.roleCode))}`,
            body,
          );
    }
    case "userRoles":
      if (mode === "revoke") {
        const assignmentId = encodeURIComponent(String(clean.assignmentId));
        return api.delete(
          `/api/admin/user-roles/${assignmentId}`,
          new URLSearchParams({ reason }),
        );
      }
      return api.post("/api/admin/user-roles", {
        userId: clean.userId,
        roleCode: clean.roleCode,
        assignmentSource: clean.assignmentSource || "MANUAL",
        effectiveStartDate: clean.effectiveStartDate,
        effectiveEndDate: clean.effectiveEndDate || null,
        approvedBy: clean.approvedBy || currentUserId,
        reason,
      });
    case "permissions":
      return api.put("/api/admin/menu-permissions", {
        principalType: clean.principalType || "ROLE",
        principalId: clean.principalId || "R09",
        permissions: [
          {
            menuId: clean.menuId,
            permissionEffect: clean.permissionEffect || "ALLOW",
          },
        ],
        reason,
      });
    case "menuStructure":
      return api.put(`/api/admin/menus/${clean.menuId}/structure`, {
        parentMenuId: clean.parentMenuId || null,
        displayOrder: clean.displayOrder,
        reason,
      });
    case "menuInfo": {
      const body = {
        parentMenuId: clean.parentMenuId || null,
        menuLevel: clean.menuLevel,
        menuName: clean.menuName,
        screenId: clean.screenId || null,
        url: clean.url || null,
        icon: clean.icon || null,
        businessCategory: clean.businessCategory || null,
        description: clean.description || null,
        displayOrder: clean.displayOrder,
        reason,
      };
      return mode === "create"
        ? api.post("/api/admin/menus", body)
        : api.put(`/api/admin/menus/${clean.menuId}`, body);
    }
    case "codeGroups": {
      const body = {
        groupId: clean.groupId,
        groupName: clean.groupName,
        description: clean.description || null,
        managingDepartment: clean.managingDepartment || null,
        reason,
      };
      return mode === "create"
        ? api.post("/api/admin/code-groups", body)
        : api.put(
            `/api/admin/code-groups/${encodeURIComponent(String(clean.groupId))}`,
            body,
          );
    }
    case "detailCodes": {
      const pathGroupId = groupId || String(clean.groupId);
      const body = {
        codeValue: clean.codeValue,
        codeName: clean.codeName,
        parentCodeValue: clean.parentCodeValue || null,
        sortOrder: clean.sortOrder,
        additionalAttributes: parseJson(clean.additionalAttributes),
        validFrom: clean.validFrom || null,
        validTo: clean.validTo || null,
        reason,
      };
      return mode === "create"
        ? api.post(
            `/api/admin/code-groups/${encodeURIComponent(pathGroupId)}/codes`,
            body,
          )
        : api.put(
            `/api/admin/code-groups/${encodeURIComponent(pathGroupId)}/codes/${encodeURIComponent(String(clean.codeValue))}`,
            body,
          );
    }
  }
}

function resolveListPath(
  screen: ScreenConfig,
  groupId: string | undefined,
  filters: Row,
) {
  const params = new URLSearchParams();
  Object.entries(filters).forEach(([key, value]) => {
    if (value !== undefined && value !== null && String(value).trim() !== "")
      params.set(key, String(value));
  });
  const path = screen.listPath.replace(
    "{groupId}",
    encodeURIComponent(groupId ?? ""),
  );
  const query = params.toString();
  return query ? `${path}?${query}` : path;
}

function normalizeDraft(row: Row, screen: ScreenConfig) {
  const next: Row = { ...row };
  if (screen.kind === "permissions") {
    next.principalType ??= "ROLE";
    next.principalId ??= "R09";
    next.permissionEffect ??= "ALLOW";
  }
  if (screen.kind === "userRoles") next.assignmentSource ??= "MANUAL";
  return next;
}

function initialDraft(screen: ScreenConfig, groupId?: string) {
  const draft: Row = {};
  screen.formFields.forEach((field) => {
    if (field.kind === "boolean") draft[field.key] = true;
    else if (field.kind === "json") draft[field.key] = "{}";
    else if (field.kind === "select")
      draft[field.key] = field.options?.[0] ?? "";
    else draft[field.key] = "";
  });
  if (screen.kind === "detailCodes") draft.groupId = groupId;
  if (screen.kind === "userRoles") {
    draft.assignmentSource = "MANUAL";
    draft.effectiveStartDate = today();
  }
  return draft;
}

function defaultFilters(kind: ScreenConfig["kind"]) {
  return kind === "permissions"
    ? { principalType: "ROLE", principalId: "R09" }
    : {};
}

function cleanRow(row: Row) {
  const clean: Row = {};
  Object.entries(row).forEach(([key, value]) => {
    if (value === "") clean[key] = null;
    else clean[key] = value;
  });
  return clean;
}

function parseJson(value: unknown) {
  if (!value) return {};
  if (typeof value === "object") return value;
  try {
    return JSON.parse(String(value));
  } catch {
    throw new Error("추가속성 JSON 형식이 올바르지 않습니다.");
  }
}

function formatValue(value: unknown, field: FieldConfig) {
  if (value == null || value === "") return <span className="muted">-</span>;
  if (field.kind === "boolean" || typeof value === "boolean")
    return (
      <span className={`badge ${value ? "green" : "gray"}`}>
        {value ? "사용" : "미사용"}
      </span>
    );
  if (Array.isArray(value))
    return (
      <span className="chip-row">
        {value.map((item) => (
          <span className="chip" key={String(item)}>
            {String(item)}
          </span>
        ))}
      </span>
    );
  if (typeof value === "object") return <code>{JSON.stringify(value)}</code>;
  return String(value);
}

function rowKey(row: Row, index: number) {
  return String(
    row.userId ??
      row.organizationCode ??
      row.roleCode ??
      row.assignmentId ??
      row.permissionId ??
      row.menuId ??
      row.groupId ??
      row.codeValue ??
      index,
  );
}

function labelOption(option: string) {
  if (option === "true") return "사용";
  if (option === "false") return "미사용";
  return option;
}

function toNumber(value: string) {
  return value === "" ? "" : Number(value);
}

function today() {
  return new Date().toISOString().slice(0, 10);
}

function successMessage(screen: ScreenConfig, mode: string) {
  if (mode === "revoke") return "회수 후 목록을 재조회했습니다.";
  if (mode === "usage")
    return "사용여부 저장 후 사용자 목록과 상세를 재조회했습니다.";
  if (mode === "roles")
    return "업무 역할 저장 후 사용자 목록과 상세를 재조회했습니다.";
  return `${screen.title} ${mode === "create" ? "등록" : "저장"} 후 재조회했습니다.`;
}

function errorType(error: unknown): StatusType {
  return typeof error === "object" &&
    error !== null &&
    "status" in error &&
    ((error as { status?: number }).status === 401 ||
      (error as { status?: number }).status === 403)
    ? "permission"
    : "error";
}

function errorText(error: unknown, fallback: string) {
  if (error instanceof Error) return error.message;
  return fallback;
}
