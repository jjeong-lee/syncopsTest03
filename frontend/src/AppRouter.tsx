import React, { useEffect, useMemo, useState } from "react";
import {
  Navigate,
  NavLink,
  Outlet,
  Route,
  Routes,
  useLocation,
  useNavigate,
  useSearchParams,
} from "react-router-dom";
import {
  api,
  ManagedRecord,
  readRecordValue,
  SessionUser,
} from "./services/api";
import {
  navigationGroups,
  ScreenConfig,
  ScreenField,
  screens,
} from "./screens";

type AuthState = {
  user: SessionUser | null;
  loading: boolean;
  setUser: (user: SessionUser | null) => void;
};

const icons: Record<string, string> = {
  "시스템 관리": "▦",
  "파일·데이터 관리": "⇪",
  "보안·감사 관리": "◈",
  "시스템 운영 관리": "⚙",
};

function ErrorText({ error }: { error: string }) {
  return <div className="alert alert-error">{error}</div>;
}

function FieldControl({
  field,
  value,
  disabled,
  onChange,
}: {
  field: ScreenField;
  value: string;
  disabled?: boolean;
  onChange: (value: string) => void;
}) {
  if (field.type === "textarea") {
    return (
      <textarea
        value={value}
        disabled={disabled || field.readonly}
        placeholder={field.placeholder}
        onChange={(event) => onChange(event.target.value)}
      />
    );
  }
  if (field.type === "select") {
    return (
      <select
        value={value}
        disabled={disabled || field.readonly}
        onChange={(event) => onChange(event.target.value)}
      >
        {(field.options ?? []).map((option) => (
          <option key={option} value={option}>
            {option || "전체"}
          </option>
        ))}
      </select>
    );
  }
  return (
    <input
      type={
        field.type === "number"
          ? "number"
          : field.type === "date"
            ? "date"
            : "text"
      }
      value={value}
      disabled={disabled || field.readonly}
      placeholder={field.placeholder}
      onChange={(event) => onChange(event.target.value)}
    />
  );
}

function StatePanel({
  state,
  title,
  description,
  action,
}: {
  state: "loading" | "empty" | "error" | "permission" | "success";
  title: string;
  description: string;
  action?: React.ReactNode;
}) {
  return (
    <div className={`state-card state-${state}`}>
      <div className="state-icon">
        {state === "loading"
          ? "◌"
          : state === "empty"
            ? "∅"
            : state === "permission"
              ? "!"
              : state === "success"
                ? "✓"
                : "×"}
      </div>
      <div>
        <strong>{title}</strong>
        <p>{description}</p>
      </div>
      {action}
    </div>
  );
}

function StatusBadge({ value }: { value: string }) {
  const normalized = value.toUpperCase();
  const tone =
    normalized.includes("FAIL") ||
    normalized.includes("ERROR") ||
    normalized.includes("N")
      ? "danger"
      : normalized.includes("PENDING") || normalized.includes("RUNNING")
        ? "warning"
        : normalized.includes("Y") ||
            normalized.includes("ACTIVE") ||
            normalized.includes("SUCCESS") ||
            normalized.includes("COMPLETED")
          ? "success"
          : "neutral";
  return <span className={`status-badge ${tone}`}>{value || "-"}</span>;
}

function Login({ onLogin }: { onLogin: (user: SessionUser) => void }) {
  const [loginId, setLoginId] = useState("admin");
  const [password, setPassword] = useState("admin");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  return (
    <main className="login-shell">
      <section className="login-hero" aria-hidden="true">
        <span className="brand-mark">KN</span>
        <p>Common Function Console</p>
        <h1>평가 운영의 기준정보, 권한, 감사 흐름을 한 번에 정렬합니다.</h1>
        <div className="hero-mosaic">
          <span />
          <span />
          <span />
          <span />
        </div>
      </section>
      <form
        className="login-card"
        onSubmit={async (event) => {
          event.preventDefault();
          setError("");
          setLoading(true);
          try {
            onLogin(await api.login(loginId, password));
          } catch (err) {
            setError((err as Error).message);
          } finally {
            setLoading(false);
          }
        }}
      >
        <span className="eyebrow">R09 시스템관리자 콘솔</span>
        <h2>교수업적평가 공통기능</h2>
        <p>
          인증 후 `/admin/*` 보호 route에서 메뉴 권한과 기능 권한을 기준으로
          관리 화면에 접근합니다.
        </p>
        <label>
          아이디
          <FieldControl
            field={{ key: "loginId", label: "아이디" }}
            value={loginId}
            onChange={setLoginId}
          />
        </label>
        <label>
          비밀번호
          <input
            type="password"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
          />
        </label>
        {error && <ErrorText error={error} />}
        <button disabled={loading}>
          {loading ? "로그인 중..." : "로그인"}
        </button>
      </form>
    </main>
  );
}

function ProtectedShell({ auth }: { auth: AuthState }) {
  if (auth.loading)
    return (
      <StatePanel
        state="loading"
        title="세션 확인 중"
        description="/api/auth/me로 현재 사용자를 확인하고 있습니다."
      />
    );
  if (!auth.user) return <Login onLogin={auth.setUser} />;
  return <AdminLayout user={auth.user} onLogout={() => auth.setUser(null)} />;
}

function AdminLayout({
  user,
  onLogout,
}: {
  user: SessionUser;
  onLogout: () => void;
}) {
  const location = useLocation();
  const navigate = useNavigate();
  const current =
    screens.find((screen) => screen.route === location.pathname) ?? screens[0];
  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="sidebar-brand">
          <span className="brand-mark small">25</span>
          <div>
            <strong>공통기능 관리</strong>
            <p>shadcn-admin 스타일 운영 콘솔</p>
          </div>
        </div>
        <nav aria-label="관리 메뉴">
          {navigationGroups.map((group) => {
            const groupActive = group.middleMenus.some((middle) =>
              middle.items.some((item) => item.route === current.route),
            );
            return (
              <details key={group.topMenu} open={groupActive}>
                <summary>
                  <span>{icons[group.topMenu] ?? "•"}</span>
                  {group.topMenu}
                </summary>
                {group.middleMenus.map((middle) => {
                  const middleActive = middle.items.some(
                    (item) => item.route === current.route,
                  );
                  return (
                    <details
                      key={`${group.topMenu}-${middle.middleMenu}`}
                      open={middleActive}
                      className="middle-menu"
                    >
                      <summary>{middle.middleMenu}</summary>
                      <div className="leaf-list">
                        {middle.items.map((item) => (
                          <NavLink
                            key={item.route}
                            to={item.route}
                            className={({ isActive }) =>
                              isActive ? "active" : undefined
                            }
                          >
                            {item.title}
                          </NavLink>
                        ))}
                      </div>
                    </details>
                  );
                })}
              </details>
            );
          })}
        </nav>
      </aside>
      <div className="content-shell">
        <header className="topbar">
          <div>
            <span className="breadcrumb">{current.menuPath}</span>
            <strong>{current.title}</strong>
          </div>
          <div className="user-chip">
            <span>{user.userName}</span>
            <b>{user.roleCodes.join(", ")}</b>
            <button
              className="ghost compact"
              onClick={async () => {
                await api.logout().catch(() => undefined);
                onLogout();
                navigate("/admin/users");
              }}
            >
              로그아웃
            </button>
          </div>
        </header>
        <main className="main-panel" id="main-content">
          <Outlet />
        </main>
      </div>
    </div>
  );
}

function makeEmptyRecord(screen: ScreenConfig): ManagedRecord {
  return screen.editableFields.reduce<ManagedRecord>(
    (acc, field) => {
      if (field.key === "id") acc.id = "";
      else if (field.key === "title") acc.title = "";
      else if (field.key === "status") acc.status = "ACTIVE";
      else if (field.key === "useYn") acc.useYn = "Y";
      else acc[field.key] = field.options?.includes("Y") ? "Y" : "";
      return acc;
    },
    { id: "", title: "", status: "ACTIVE", useYn: "Y" },
  );
}

export function allowedPayload(
  screen: ScreenConfig,
  draft: ManagedRecord,
  original: ManagedRecord | null,
): ManagedRecord {
  const allowedKeys = new Set(screen.editableFields.map((field) => field.key));
  const next: ManagedRecord = {
    id: draft.id || original?.id || "",
    title: draft.title ?? original?.title ?? draft.id,
    status: draft.status,
    useYn: draft.useYn,
    payload: {},
  };
  screen.editableFields.forEach((field) => {
    const value = readRecordValue(draft, field.key);
    if (field.key === "id") next.id = value;
    else if (field.key === "title") next.title = value;
    else if (field.key === "status") next.status = value;
    else if (field.key === "useYn") next.useYn = value;
    else if (allowedKeys.has(field.key)) next[field.key] = value;
  });
  if (screen.id === "SCR-001-users") {
    next.title = original?.title ?? draft.title ?? next.id;
  }
  if (
    screen.id === "SCR-016-attachments" &&
    readRecordValue(draft, "deleteYn") === "Y"
  ) {
    next.useYn = "Y";
  }
  if (screen.id === "SCR-021-active-sessions") {
    next.status = "TERMINATED";
  }
  return next;
}

function RecordDialog({
  screen,
  record,
  mode,
  onClose,
  onSaved,
}: {
  screen: ScreenConfig;
  record: ManagedRecord;
  mode: "create" | "edit" | "readonly";
  onClose: () => void;
  onSaved: (record: ManagedRecord) => void;
}) {
  const [draft, setDraft] = useState<ManagedRecord>({
    ...record,
    payload: { ...(record.payload ?? {}) },
  });
  const [error, setError] = useState("");
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [progress, setProgress] = useState(false);
  const readonly = mode === "readonly" || screen.noMutation;
  const update = (key: string, value: string) =>
    setDraft((prev) => ({ ...prev, [key]: value }));
  const save = async () => {
    setError("");
    setFieldErrors({});
    if (readonly) return;
    if (!readRecordValue(draft, "id")) {
      setError("식별자는 필수입니다.");
      return;
    }
    if (
      screen.id === "SCR-016-attachments" &&
      readRecordValue(draft, "deleteYn") === "Y" &&
      !readRecordValue(draft, "deleteReason")
    ) {
      setError("논리삭제 사유는 필수입니다.");
      return;
    }
    if (
      screen.id === "SCR-021-active-sessions" &&
      !readRecordValue(draft, "reason")
    ) {
      setError("강제종료 사유는 필수입니다.");
      return;
    }
    setProgress(true);
    try {
      onSaved(
        await api.save(
          screen.apiPath,
          allowedPayload(screen, draft, mode === "create" ? null : record),
        ),
      );
    } catch (err) {
      const details = (err as Error & { details?: unknown }).details;
      if (details && typeof details === "object") {
        setFieldErrors(details as Record<string, string>);
      }
      setError((err as Error).message);
    } finally {
      setProgress(false);
    }
  };
  const fields = readonly
    ? [...(screen.readonlyFields ?? []), ...screen.columns]
    : screen.editableFields;
  return (
    <div className="modal-backdrop" role="dialog" aria-modal="true">
      <div className="modal">
        <div className="modal-head">
          <div>
            <span className="eyebrow">{screen.id}</span>
            <h3>
              {screen.title}{" "}
              {mode === "create" ? "신규 등록" : readonly ? "상세" : "수정"}
            </h3>
          </div>
          <button
            className="ghost compact"
            onClick={onClose}
            disabled={progress}
          >
            닫기
          </button>
        </div>
        {screen.readonlyNotice && (
          <div className="contract-note">{screen.readonlyNotice}</div>
        )}
        <div className="form-grid">
          {fields.map((field) => {
            const fieldReadonly =
              readonly ||
              field.readonly ||
              (field.key === "id" && mode !== "create");
            const value = readRecordValue(draft, field.key);
            return (
              <label key={`${field.key}-${field.label}`}>
                {field.label}
                <FieldControl
                  field={field}
                  value={value}
                  disabled={fieldReadonly || progress}
                  onChange={(next) => update(field.key, next)}
                />
                {field.helper && <small>{field.helper}</small>}
                {fieldErrors[field.key] && (
                  <small className="field-error">
                    {fieldErrors[field.key]}
                  </small>
                )}
              </label>
            );
          })}
        </div>
        {screen.oqNotice && <div className="oq-note">{screen.oqNotice}</div>}
        {progress && (
          <StatePanel
            state="loading"
            title="저장 중"
            description="서버 API 처리와 후속 재조회가 끝날 때까지 기다려 주세요."
          />
        )}
        {error && <ErrorText error={error} />}
        <div className="modal-actions">
          <button className="ghost" onClick={onClose} disabled={progress}>
            취소
          </button>
          {!readonly && (
            <button onClick={save} disabled={progress}>
              {screen.primaryCta}
            </button>
          )}
        </div>
      </div>
    </div>
  );
}

export function ManagementScreen({ screen }: { screen: ScreenConfig }) {
  const [params, setParams] = useSearchParams();
  const [keyword, setKeyword] = useState(params.get("keyword") ?? "");
  const [size, setSize] = useState(Number(params.get("size") ?? 20));
  const initialFilters = () =>
    Object.fromEntries(
      screen.searchFields
        .filter((field) => field.key !== "keyword")
        .map((field) => [field.key, params.get(field.key) ?? ""]),
    );
  const emptyFilters = () =>
    Object.fromEntries(
      screen.searchFields
        .filter((field) => field.key !== "keyword")
        .map((field) => [field.key, ""]),
    );
  const [filters, setFilters] =
    useState<Record<string, string>>(initialFilters);
  const [rows, setRows] = useState<ManagedRecord[]>([]);
  const [selected, setSelected] = useState<ManagedRecord | null>(null);
  const [dialogMode, setDialogMode] = useState<"create" | "edit" | "readonly">(
    "readonly",
  );
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [permissionDenied, setPermissionDenied] = useState(false);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const supportsCreate = !screen.noMutation && screen.supportsCreate !== false;
  const load = async (
    nextKeyword = keyword,
    nextSize = size,
    nextFilters = filters,
  ) => {
    setLoading(true);
    setError("");
    setPermissionDenied(false);
    setMessage("");
    setParams({
      keyword: nextKeyword,
      size: String(nextSize),
      ...Object.fromEntries(
        Object.entries(nextFilters).filter(([, value]) => Boolean(value)),
      ),
    });
    try {
      setRows(
        await api.list(screen.apiPath, nextKeyword, nextSize, 0, nextFilters),
      );
    } catch (err) {
      const code = (err as Error & { code?: string }).code;
      if (code === "UNAUTHORIZED" || code === "FORBIDDEN")
        setPermissionDenied(true);
      setError((err as Error).message);
    } finally {
      setLoading(false);
    }
  };
  useEffect(() => {
    const nextFilters = initialFilters();
    setFilters(nextFilters);
    void load(
      params.get("keyword") ?? "",
      Number(params.get("size") ?? 20),
      nextFilters,
    );
  }, [screen.route]);
  const contractChecklist = useMemo(
    () => [
      `route ${screen.route}`,
      `${screen.apiPath} GET/POST`,
      screen.archetype,
      screen.primaryCta,
      "loading/empty/error/permission/success",
    ],
    [screen],
  );
  if (permissionDenied) {
    return (
      <StatePanel
        state="permission"
        title="접근 권한이 없습니다"
        description={`${screen.menuPath} 메뉴 또는 조회 기능 권한이 필요합니다.`}
      />
    );
  }
  return (
    <section className="page-card">
      <div className="page-hero">
        <div>
          <span className="eyebrow">
            {screen.id} · {screen.archetype}
          </span>
          <h2>{screen.title}</h2>
          <p>{screen.goal}</p>
        </div>
        <div className="hero-actions">
          {!supportsCreate ? (
            <span className="readonly-pill">
              {screen.noMutation ? "조회 전용" : "신규 없음"}
            </span>
          ) : (
            <button
              onClick={() => {
                setDialogMode("create");
                setSelected(makeEmptyRecord(screen));
              }}
            >
              신규 등록
            </button>
          )}
        </div>
      </div>
      <div className="contract-strip">
        {contractChecklist.map((item) => (
          <span key={item}>{item}</span>
        ))}
      </div>
      <div className="search-panel">
        {screen.searchFields.map((field) => (
          <label key={field.key}>
            {field.label}
            <FieldControl
              field={field}
              value={
                field.key === "keyword" ? keyword : (filters[field.key] ?? "")
              }
              onChange={(value) =>
                field.key === "keyword"
                  ? setKeyword(value)
                  : setFilters((prev) => ({ ...prev, [field.key]: value }))
              }
            />
          </label>
        ))}
        <label>
          표시 건수
          <select
            value={size}
            onChange={(event) => setSize(Number(event.target.value))}
          >
            <option value="20">20</option>
            <option value="50">50</option>
            <option value="100">100</option>
          </select>
        </label>
        <button onClick={() => void load(keyword, size, filters)}>검색</button>
        <button
          className="ghost"
          onClick={() => {
            setKeyword("");
            setSize(20);
            setFilters(emptyFilters());
            setParams({});
          }}
        >
          초기화
        </button>
      </div>
      {screen.secondaryApis && (
        <div className="secondary-api-row">
          {screen.secondaryApis.map((apiInfo) => (
            <span key={apiInfo.path}>
              {apiInfo.label}: {apiInfo.path}
            </span>
          ))}
        </div>
      )}
      {screen.readonlyNotice && (
        <div className="contract-note">{screen.readonlyNotice}</div>
      )}
      {screen.oqNotice && <div className="oq-note">{screen.oqNotice}</div>}
      {message && (
        <StatePanel state="success" title="처리 완료" description={message} />
      )}
      {error && !permissionDenied && <ErrorText error={error} />}
      <div className="table-card">
        <div className="table-headline">
          <div>
            <strong>{screen.title} 목록</strong>
            <p>
              행 선택 시 상세 모달로 이동합니다. 저장 후 같은 조건으로
              재조회합니다.
            </p>
          </div>
          <span>{rows.length}건 표시</span>
        </div>
        {loading ? (
          <div className="skeleton-list">
            <span />
            <span />
            <span />
            <span />
          </div>
        ) : rows.length === 0 ? (
          <StatePanel
            state="empty"
            title="조회 결과가 없습니다"
            description="검색조건을 변경하거나 권한 및 API 데이터를 확인해 주세요."
            action={
              supportsCreate && (
                <button
                  className="ghost compact"
                  onClick={() => {
                    setDialogMode("create");
                    setSelected(makeEmptyRecord(screen));
                  }}
                >
                  신규 등록
                </button>
              )
            }
          />
        ) : (
          <div className="table-scroll">
            <table>
              <thead>
                <tr>
                  {screen.columns.map((column) => (
                    <th key={`${screen.id}-${column.key}`}>{column.label}</th>
                  ))}
                  <th>동작</th>
                </tr>
              </thead>
              <tbody>
                {rows.map((row) => (
                  <tr
                    key={row.id}
                    onClick={() => {
                      setDialogMode(screen.noMutation ? "readonly" : "edit");
                      setSelected(row);
                    }}
                  >
                    {screen.columns.map((column) => {
                      const value = readRecordValue(row, column.key);
                      const badgeLike =
                        column.key.toLowerCase().includes("status") ||
                        column.key.toLowerCase().endsWith("yn") ||
                        column.key === "useYn";
                      return (
                        <td key={`${row.id}-${column.key}`}>
                          {badgeLike ? (
                            <StatusBadge value={value} />
                          ) : (
                            <span
                              className={
                                column.key === "id" ||
                                column.key.endsWith("Id") ||
                                column.key.endsWith("Code")
                                  ? "mono"
                                  : undefined
                              }
                            >
                              {value || "-"}
                            </span>
                          )}
                        </td>
                      );
                    })}
                    <td>
                      <button
                        className="ghost compact"
                        onClick={(event) => {
                          event.stopPropagation();
                          setDialogMode(
                            screen.noMutation ? "readonly" : "edit",
                          );
                          setSelected(row);
                        }}
                      >
                        {screen.noMutation ? "상세" : "수정"}
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
      <div className="action-map">
        {screen.actions.map((action) => (
          <button
            key={action.label}
            className="ghost action-chip"
            onClick={() => {
              if (action.kind === "navigate") {
                const groupId = selected ? readRecordValue(selected, "id") : "";
                if (groupId)
                  navigate(
                    `/admin/codes?keyword=${encodeURIComponent(groupId)}`,
                  );
                else setMessage("코드그룹을 먼저 선택하세요.");
              } else if (action.kind === "logicalDelete" && selected) {
                setDialogMode("edit");
                setSelected({ ...selected, deleteYn: "Y" });
              } else if (action.kind === "terminate" && selected) {
                setDialogMode("edit");
                setSelected({ ...selected, status: "TERMINATED" });
              } else if (action.kind === "readonly" && selected) {
                setDialogMode("readonly");
                setSelected(selected);
              } else if (action.kind === "local") {
                setMessage(action.description);
              }
            }}
            title={action.description}
          >
            {action.label}
          </button>
        ))}
      </div>
      {selected && (
        <RecordDialog
          screen={screen}
          record={selected}
          mode={dialogMode}
          onClose={() => setSelected(null)}
          onSaved={(saved) => {
            setSelected(null);
            setMessage(
              `${readRecordValue(saved, "id")} 저장 완료. 같은 조건으로 목록을 재조회했습니다.`,
            );
            void load(keyword, size);
          }}
        />
      )}
    </section>
  );
}

function makePage(screen: ScreenConfig) {
  return function ScreenPage() {
    return <ManagementScreen screen={screen} />;
  };
}

export const UsersPage = makePage(screens[0]);
export const OrganizationsPage = makePage(screens[1]);
export const PositionsPage = makePage(screens[2]);
export const RolesPage = makePage(screens[3]);
export const UserRolesPage = makePage(screens[4]);
export const MenuPermissionsPage = makePage(screens[5]);
export const FunctionPermissionsPage = makePage(screens[6]);
export const DataScopePermissionsPage = makePage(screens[7]);
export const MenusPage = makePage(screens[8]);
export const CodeGroupsPage = makePage(screens[9]);
export const CodesPage = makePage(screens[10]);
export const SystemSettingsPage = makePage(screens[11]);
export const BaseYearsPage = makePage(screens[12]);
export const FilePoliciesPage = makePage(screens[13]);
export const NoticesPage = makePage(screens[14]);
export const AttachmentsPage = makePage(screens[15]);
export const UploadTemplatesPage = makePage(screens[16]);
export const ExcelUploadsPage = makePage(screens[17]);
export const ExcelDownloadsPage = makePage(screens[18]);
export const PersonalInformationPage = makePage(screens[19]);
export const ActiveSessionsPage = makePage(screens[20]);
export const AuditLogsPage = makePage(screens[21]);
export const BatchDefinitionsPage = makePage(screens[22]);
export const BatchExecutionsPage = makePage(screens[23]);
export const BatchResultsPage = makePage(screens[24]);

const routeComponents = [
  UsersPage,
  OrganizationsPage,
  PositionsPage,
  RolesPage,
  UserRolesPage,
  MenuPermissionsPage,
  FunctionPermissionsPage,
  DataScopePermissionsPage,
  MenusPage,
  CodeGroupsPage,
  CodesPage,
  SystemSettingsPage,
  BaseYearsPage,
  FilePoliciesPage,
  NoticesPage,
  AttachmentsPage,
  UploadTemplatesPage,
  ExcelUploadsPage,
  ExcelDownloadsPage,
  PersonalInformationPage,
  ActiveSessionsPage,
  AuditLogsPage,
  BatchDefinitionsPage,
  BatchExecutionsPage,
  BatchResultsPage,
];

export function AppRouter() {
  const [user, setUser] = useState<SessionUser | null>(null);
  const [loading, setLoading] = useState(true);
  useEffect(() => {
    api
      .me()
      .then(setUser)
      .catch(() => undefined)
      .finally(() => setLoading(false));
  }, []);
  const auth = { user, loading, setUser };
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/admin/users" replace />} />
      <Route path="/admin" element={<Navigate to="/admin/users" replace />} />
      <Route path="/admin/*" element={<ProtectedShell auth={auth} />}>
        {screens.map((screen, index) => {
          const Page = routeComponents[index];
          return (
            <Route
              key={screen.route}
              path={screen.route.replace("/admin/", "")}
              element={<Page />}
            />
          );
        })}
      </Route>
      <Route
        path="*"
        element={
          <StatePanel
            state="error"
            title="페이지를 찾을 수 없습니다"
            description="정의된 AppRouter route 목록에 없는 주소입니다."
          />
        }
      />
    </Routes>
  );
}
