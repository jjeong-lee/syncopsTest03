import type { ReactNode } from "react";
import type { LoadState, Page } from "./types";
import { useCallback, useEffect, useState } from "react";

export const roles = [
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
export const useYn = ["Y", "N"];

export const routeMeta: Record<
  string,
  { title: string; screen: string; path: string; entity: string }
> = {
  "/system/users": {
    title: "사용자 관리",
    screen: "CMN-FR-001",
    path: "시스템 관리 > 사용자·조직 관리",
    entity: "user_account / korus_staff_snapshot",
  },
  "/system/organizations": {
    title: "조직 관리",
    screen: "CMN-FR-002",
    path: "시스템 관리 > 사용자·조직 관리",
    entity: "organization",
  },
  "/system/roles": {
    title: "역할 관리",
    screen: "CMN-FR-005",
    path: "시스템 관리 > 역할·권한 관리",
    entity: "role",
  },
  "/system/user-roles": {
    title: "사용자 역할 관리",
    screen: "CMN-FR-006",
    path: "시스템 관리 > 역할·권한 관리",
    entity: "user_role",
  },
  "/system/menu-permissions": {
    title: "메뉴 권한 관리",
    screen: "CMN-FR-007",
    path: "시스템 관리 > 역할·권한 관리",
    entity: "menu_permission",
  },
  "/system/menu-structure": {
    title: "메뉴 구조 관리",
    screen: "CMN-FR-013",
    path: "시스템 관리 > 메뉴 관리",
    entity: "menu",
  },
  "/system/menu-info": {
    title: "메뉴 정보 관리",
    screen: "CMN-FR-014",
    path: "시스템 관리 > 메뉴 관리",
    entity: "menu",
  },
  "/system/code-groups": {
    title: "코드그룹 관리",
    screen: "CMN-FR-016",
    path: "시스템 관리 > 공통코드 관리",
    entity: "code_group",
  },
  "/system/detail-codes": {
    title: "상세코드 관리",
    screen: "CMN-FR-017",
    path: "시스템 관리 > 공통코드 관리",
    entity: "detail_code",
  },
};

export const navGroups = [
  {
    label: "사용자·조직 관리",
    routes: ["/system/users", "/system/organizations"],
  },
  {
    label: "역할·권한 관리",
    routes: ["/system/roles", "/system/user-roles", "/system/menu-permissions"],
  },
  {
    label: "메뉴 관리",
    routes: ["/system/menu-structure", "/system/menu-info"],
  },
  {
    label: "공통코드 관리",
    routes: ["/system/code-groups", "/system/detail-codes"],
  },
];

export function usePage<T>(
  loader: () => Promise<Page<T> | T[]>,
  deps: unknown[] = [],
) {
  const [rows, setRows] = useState<T[]>([]);
  const [state, setState] = useState<LoadState>("idle");
  const [error, setError] = useState("");
  const load = useCallback(() => {
    setState("loading");
    setError("");
    return loader()
      .then((data) => {
        const items = Array.isArray(data) ? data : data.items;
        setRows(items);
        setState(items.length ? "success" : "empty");
        return items;
      })
      .catch((err: { message?: string; status?: number }) => {
        setRows([]);
        setError(err.message ?? "요청 처리 중 오류가 발생했습니다.");
        setState(err.status === 403 ? "permission" : "error");
        return [] as T[];
      });
  }, deps);
  useEffect(() => {
    void load();
  }, [load]);
  return { rows, setRows, state, error, load };
}

export function StatePanel({
  state,
  title,
  message,
}: {
  state: LoadState;
  title: string;
  message?: string;
}) {
  const label: Record<LoadState, string> = {
    idle: "필터를 입력하거나 행을 선택해 작업을 시작하세요.",
    loading: "데이터를 불러오는 중입니다.",
    empty: "검색 결과가 없습니다. 조건을 조정한 뒤 다시 조회하세요.",
    error: "요청 처리 중 오류가 발생했습니다.",
    permission: "권한이 없습니다.",
    success: "API 조회 결과가 화면에 반영되었습니다.",
  };
  return (
    <div
      className={`state-card ${state}`}
      role={state === "error" || state === "permission" ? "alert" : "status"}
    >
      <span className="state-dot" />
      <div>
        <strong>{title}</strong>
        <p>{message || label[state]}</p>
      </div>
      {state === "loading" && <span className="spinner" aria-hidden="true" />}
    </div>
  );
}

export function Toast({
  message,
  tone = "success",
}: {
  message: string;
  tone?: "success" | "error";
}) {
  return message ? (
    <div className={`toast ${tone}`} role="status">
      {message}
    </div>
  ) : null;
}

export function PageFrame({
  title,
  state,
  error,
  children,
  actions,
}: {
  title: string;
  state?: LoadState;
  error?: string;
  children: ReactNode;
  actions?: ReactNode;
}) {
  const meta = Object.values(routeMeta).find((m) => m.title === title);
  return (
    <main className="page-main">
      <section className="page-heading">
        <div>
          <p className="eyebrow">
            {meta?.screen} · {meta?.entity}
          </p>
          <h1>{title}</h1>
          <p>{meta?.path}</p>
        </div>
        <div className="heading-actions">{actions}</div>
      </section>
      {state && (
        <StatePanel state={state} title={`${title} 상태`} message={error} />
      )}
      {children}
    </main>
  );
}

export function SearchPanel({
  children,
  onSearch,
}: {
  children: ReactNode;
  onSearch: () => void;
}) {
  return (
    <section className="card search-panel">
      <div className="section-title">
        <strong>조회 조건</strong>
        <span>값이 있는 조건만 API query로 전송합니다.</span>
      </div>
      <div className="form-grid">{children}</div>
      <button type="button" onClick={onSearch}>
        검색
      </button>
    </section>
  );
}

export function DetailShell({
  title = "상세/편집",
  children,
}: {
  title?: string;
  children: ReactNode;
}) {
  return (
    <section className="card detail-shell">
      <div className="section-title">
        <strong>{title}</strong>
        <span>저장 후 목록을 다시 조회합니다.</span>
      </div>
      <div className="form-grid">{children}</div>
    </section>
  );
}

export function ConfirmModal({
  open,
  title,
  description,
  confirmText = "저장",
  onConfirm,
  onClose,
}: {
  open: boolean;
  title: string;
  description: string;
  confirmText?: string;
  onConfirm: () => void;
  onClose: () => void;
}) {
  return open ? (
    <div className="modal" role="dialog" aria-modal="true">
      <div className="modal-card">
        <h2>{title}</h2>
        <p>{description}</p>
        <div className="button-row">
          <button type="button" onClick={onConfirm}>
            {confirmText}
          </button>
          <button type="button" className="secondary" onClick={onClose}>
            취소
          </button>
        </div>
      </div>
    </div>
  ) : null;
}

export function DataTable<T extends Record<string, unknown>>({
  rows,
  columns,
  onSelect,
  selectedKey,
}: {
  rows: T[];
  columns: Array<[keyof T, string]>;
  onSelect?: (row: T) => void;
  selectedKey?: string;
}) {
  return (
    <div className="table-wrap">
      <table>
        <thead>
          <tr>
            {columns.map(([key, label]) => (
              <th key={String(key)}>{label}</th>
            ))}
          </tr>
        </thead>
        <tbody>
          {rows.length === 0 ? (
            <tr>
              <td colSpan={columns.length} className="empty-cell">
                표시할 데이터가 없습니다.
              </td>
            </tr>
          ) : (
            rows.map((row, idx) => {
              const rowKey = String(row[columns[0][0]] ?? idx);
              return (
                <tr
                  key={rowKey}
                  className={selectedKey === rowKey ? "selected" : ""}
                  onClick={() => onSelect?.(row)}
                >
                  {columns.map(([key]) => (
                    <td key={String(key)}>{formatCell(row[key])}</td>
                  ))}
                </tr>
              );
            })
          )}
        </tbody>
      </table>
    </div>
  );
}

export function formatCell(value: unknown): string {
  if (Array.isArray(value)) return value.join(", ");
  if (typeof value === "object" && value !== null) return JSON.stringify(value);
  return value == null ? "" : String(value);
}

export function Input({
  label,
  value,
  onChange,
  type = "text",
  readOnly = false,
  placeholder,
}: {
  label: string;
  value?: string;
  onChange?: (value: string) => void;
  type?: string;
  readOnly?: boolean;
  placeholder?: string;
}) {
  return (
    <label>
      {label}
      <input
        type={type}
        value={value ?? ""}
        readOnly={readOnly}
        disabled={readOnly}
        placeholder={placeholder}
        onChange={(e) => onChange?.(e.target.value)}
      />
    </label>
  );
}

export function Select({
  label,
  values,
  value,
  onChange,
  includeAll = true,
}: {
  label: string;
  values: string[];
  value?: string;
  onChange: (value: string) => void;
  includeAll?: boolean;
}) {
  return (
    <label>
      {label}
      <select value={value ?? ""} onChange={(e) => onChange(e.target.value)}>
        {includeAll && <option value="">전체</option>}
        {values.map((v) => (
          <option key={v} value={v}>
            {v}
          </option>
        ))}
      </select>
    </label>
  );
}

export function Textarea({
  label,
  value,
  onChange,
  placeholder,
}: {
  label: string;
  value?: string;
  onChange: (value: string) => void;
  placeholder?: string;
}) {
  return (
    <label>
      {label}
      <textarea
        value={value ?? ""}
        placeholder={placeholder}
        onChange={(e) => onChange(e.target.value)}
      />
    </label>
  );
}

export function CheckboxCell({
  value,
  onChange,
}: {
  value: "Y" | "N";
  onChange: (value: "Y" | "N") => void;
}) {
  return (
    <button
      type="button"
      className={`toggle ${value === "Y" ? "on" : "off"}`}
      onClick={() => onChange(value === "Y" ? "N" : "Y")}
    >
      {value}
    </button>
  );
}
