import { FormEvent, useEffect, useMemo, useState } from "react";
import { api, ApiResponse, PageResult } from "../api/client";
import { FieldContract, ScreenContract } from "../featureCatalog";

type Row = Record<string, unknown>;
type LoadStatus = "loading" | "success" | "empty" | "error" | "permission";
type WriteMethod = "POST" | "PATCH" | "PUT" | "DELETE";

type ModalMode = "view" | "create" | "edit";

export function resolveMutationMethod(
  screen: ScreenContract,
  mode: ModalMode,
): ScreenContract["mutationMethod"] {
  if (screen.readOnly) return "GET";
  if (mode === "edit" && screen.mutationMethod === "POST") return "PATCH";
  return screen.mutationMethod;
}

export function buildMutationPayload(
  screen: ScreenContract,
  form: Row,
  _mode: ModalMode,
) {
  if (screen.readOnly) return {};

  const payload: Row = {};
  const editableKeys = new Set(
    screen.modalFields
      .filter((field) => !field.readOnly)
      .map((field) => field.key),
  );

  for (const key of editableKeys) {
    if (form[key] !== undefined) payload[key] = form[key];
  }

  const reason = String(form.reason ?? "").trim();
  if (reason) payload.reason = reason;

  return payload;
}

export function buildSearchKeyword(
  keyword: string,
  filterValues: Record<string, string>,
) {
  const directKeyword = keyword.trim();
  if (directKeyword) return directKeyword;
  return Object.values(filterValues)
    .map((value) => value.trim())
    .filter(Boolean)
    .join(" ");
}

const statusLabels: Record<string, string> = {
  ACTIVE: "활성",
  INACTIVE: "비활성",
  DELETED: "삭제",
  COMPLETED: "완료",
  FAILED: "실패",
  RUNNING: "실행중",
  REVOKED: "회수",
};

export function AdminScreenPage({ screen }: { screen: ScreenContract }) {
  const [keyword, setKeyword] = useState("");
  const [filterValues, setFilterValues] = useState<Record<string, string>>({});
  const [size, setSize] = useState(20);
  const [page, setPage] = useState(0);
  const [data, setData] = useState<PageResult<Row> | null>(null);
  const [status, setStatus] = useState<LoadStatus>("loading");
  const [error, setError] = useState<ApiResponse<unknown>["error"] | null>(
    null,
  );
  const [modal, setModal] = useState<{
    mode: ModalMode;
    row: Row | null;
  } | null>(null);
  const [toast, setToast] = useState("");

  const title = screen.menuPath.split(" > ").slice(-1)[0];
  const category = screen.menuPath.split(" > ")[0];
  const canMutate = !screen.readOnly && screen.mutationMethod !== "GET";

  async function load(nextPage = page) {
    setStatus("loading");
    setError(null);
    const response = await api.search<Row>(
      screen.apiPath,
      buildSearchKeyword(keyword, filterValues),
      size,
      nextPage,
    );
    if (!response.success) {
      setError(response.error ?? null);
      setStatus(
        response.error?.code === "FORBIDDEN" || response.error?.code === "403"
          ? "permission"
          : "error",
      );
      return;
    }
    const result = response.data ?? {
      items: [],
      page: { page: nextPage, size, totalElements: 0 },
    };
    setData(result);
    setStatus(result.items.length > 0 ? "success" : "empty");
  }

  useEffect(() => {
    setPage(0);
    setKeyword("");
    setFilterValues({});
    setToast("");
    void load(0);
    // screen changes must re-query the representative GET operation.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [screen.route, size]);

  function submitSearch(event: FormEvent) {
    event.preventDefault();
    setPage(0);
    void load(0);
  }

  async function mutate(
    payload: Row,
    mode: ModalMode,
    recordId?: string | null,
  ) {
    const method = resolveMutationMethod(screen, mode) as WriteMethod;
    const result = await api.mutate<Row>(
      screen.apiPath,
      method,
      payload,
      mode === "edit" ? recordId : null,
    );
    if (!result.success) {
      return result;
    }
    setToast(`${title} 처리 후 목록을 다시 조회했습니다.`);
    setModal(null);
    await load();
    return result;
  }

  return (
    <main className="page-main">
      <header className="page-hero">
        <div>
          <span className="eyebrow">
            {category} · {screen.archetype}
          </span>
          <h1>{title}</h1>
          <p>{screen.goal}</p>
        </div>
        <div className="hero-actions">
          <span className="chip">{screen.role}</span>
          {canMutate && (
            <button
              className="primary-button"
              onClick={() => setModal({ mode: "create", row: null })}
              type="button"
            >
              신규/실행
            </button>
          )}
        </div>
      </header>

      <section className="contract-strip" aria-label="화면 계약 체크리스트">
        <ContractChip label="route" value={screen.route} />
        <ContractChip label="operation" value={screen.operationId} />
        <ContractChip label="entity" value={screen.primaryEntity} />
        <ContractChip label="success" value="toast 후 대표 GET 재조회" />
      </section>

      <form className="search-card" onSubmit={submitSearch}>
        <div className="search-fields">
          <label className="field wide">
            통합 검색
            <input
              value={keyword}
              onChange={(event) => setKeyword(event.target.value)}
              placeholder={
                screen.filters
                  .map((field) => field.label)
                  .slice(0, 4)
                  .join(", ") || "제목, 상태, payload 검색"
              }
            />
          </label>
          {screen.filters.map((field) => (
            <label className="field" key={field.key}>
              {field.label}
              <input
                value={filterValues[field.key] ?? ""}
                onChange={(event) =>
                  setFilterValues((current) => ({
                    ...current,
                    [field.key]: event.target.value,
                  }))
                }
              />
            </label>
          ))}
          <label className="field compact">
            표시 건수
            <select
              value={size}
              onChange={(event) => setSize(Number(event.target.value))}
            >
              <option value={20}>20</option>
              <option value={50}>50</option>
              <option value={100}>100</option>
            </select>
          </label>
        </div>
        <div className="filter-hints">
          {screen.filters.map((field) => (
            <span key={field.key}>{field.label}</span>
          ))}
        </div>
        <button
          className="primary-button"
          disabled={status === "loading"}
          type="submit"
        >
          {status === "loading" ? "조회 중" : "검색"}
        </button>
      </form>

      {toast && (
        <div className="toast" role="status">
          {toast}
        </div>
      )}
      {error && <ErrorAlert error={error} />}

      <section className="content-card">
        <div className="table-toolbar">
          <div>
            <strong>조회 결과</strong>
            <span>
              {data?.page.totalElements ?? 0}건 · 기본 {size}건/page
            </span>
          </div>
          <button className="ghost-button" onClick={() => load()} type="button">
            새로고침
          </button>
        </div>
        <StatePanel
          status={status}
          canCreate={canMutate}
          onCreate={() => setModal({ mode: "create", row: null })}
        />
        <ResultTable
          rows={data?.items ?? []}
          columns={screen.columns}
          screen={screen}
          loading={status === "loading"}
          onSelect={(row) =>
            setModal({ mode: screen.readOnly ? "view" : "edit", row })
          }
        />
        <Pagination
          page={page}
          size={size}
          total={data?.page.totalElements ?? 0}
          onPage={(next) => {
            setPage(next);
            void load(next);
          }}
        />
      </section>

      {modal && (
        <DetailModal
          mode={modal.mode}
          row={modal.row}
          screen={screen}
          onClose={() => setModal(null)}
          onSubmit={mutate}
        />
      )}
    </main>
  );
}

function ContractChip({ label, value }: { label: string; value: string }) {
  return (
    <span className="contract-chip">
      <small>{label}</small>
      {value}
    </span>
  );
}

function ResultTable({
  rows,
  columns,
  screen,
  loading,
  onSelect,
}: {
  rows: Row[];
  columns: FieldContract[];
  screen: ScreenContract;
  loading: boolean;
  onSelect: (row: Row) => void;
}) {
  const visibleColumns = useMemo(() => columns.slice(0, 9), [columns]);

  if (loading) {
    return (
      <div className="skeleton-table" aria-label="loading table">
        {Array.from({ length: 7 }).map((_, index) => (
          <span key={index} />
        ))}
      </div>
    );
  }

  return (
    <div className="table-scroll">
      <table>
        <thead>
          <tr>
            {visibleColumns.map((field) => (
              <th key={field.key}>{field.label}</th>
            ))}
            <th>상태</th>
            <th>수정일시</th>
          </tr>
        </thead>
        <tbody>
          {rows.map((row) => (
            <tr
              key={String(row.recordId)}
              onClick={() => onSelect(row)}
              tabIndex={0}
            >
              {visibleColumns.map((field, index) => (
                <td key={field.key}>
                  <CellValue
                    field={field}
                    row={row}
                    fallback={index === 0 ? row.title : undefined}
                  />
                </td>
              ))}
              <td>
                <StatusBadge value={String(row.status ?? "ACTIVE")} />
              </td>
              <td>{formatValue(row.updatedAt ?? row.createdAt)}</td>
            </tr>
          ))}
          {rows.length === 0 && (
            <tr>
              <td colSpan={visibleColumns.length + 2}>
                <div className="empty-inline">
                  {screen.menuPath} 조건에 맞는 데이터가 없습니다.
                </div>
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  );
}

function CellValue({
  field,
  row,
  fallback,
}: {
  field: FieldContract;
  row: Row;
  fallback?: unknown;
}) {
  const value = row[field.key] ?? row[toSnake(field.key)] ?? fallback ?? "-";
  if (field.key.toLowerCase().includes("yn")) {
    return (
      <span className="soft-badge">
        {String(value) === "Y" || value === true
          ? "예"
          : String(value) === "N" || value === false
            ? "아니오"
            : String(value)}
      </span>
    );
  }
  return <>{formatValue(value)}</>;
}

function StatusBadge({ value }: { value: string }) {
  const tone =
    value === "FAILED" || value === "DELETED"
      ? "danger"
      : value === "RUNNING"
        ? "info"
        : value === "COMPLETED" || value === "ACTIVE"
          ? "success"
          : "neutral";
  return (
    <span className={`status-badge ${tone}`}>
      {statusLabels[value] ?? value}
    </span>
  );
}

function StatePanel({
  status,
  canCreate,
  onCreate,
}: {
  status: LoadStatus;
  canCreate: boolean;
  onCreate: () => void;
}) {
  if (status === "success") return null;
  const content: Record<
    LoadStatus,
    { title: string; body: string; icon: string }
  > = {
    loading: {
      title: "데이터를 불러오는 중",
      body: "중복 제출을 막고 API 응답을 기다립니다.",
      icon: "◌",
    },
    empty: {
      title: "조회 결과가 없습니다",
      body: "검색 조건을 줄이거나 기본 20건/page 조건으로 다시 조회하세요.",
      icon: "⌕",
    },
    error: {
      title: "조회 중 오류가 발생했습니다",
      body: "상단 오류 내용을 확인한 뒤 다시 조회하세요.",
      icon: "!",
    },
    permission: {
      title: "권한이 없습니다",
      body: "직접 route 접근은 허용되지 않는 경우 permission-denied 상태로 유지됩니다.",
      icon: "×",
    },
    success: { title: "", body: "", icon: "" },
  };
  const selected = content[status];
  return (
    <div className={`state-card ${status}`}>
      <span>{selected.icon}</span>
      <div>
        <strong>{selected.title}</strong>
        <p>{selected.body}</p>
      </div>
      {status === "empty" && canCreate && (
        <button className="ghost-button" onClick={onCreate} type="button">
          신규 등록
        </button>
      )}
    </div>
  );
}

function DetailModal({
  mode,
  row,
  screen,
  onClose,
  onSubmit,
}: {
  mode: ModalMode;
  row: Row | null;
  screen: ScreenContract;
  onClose: () => void;
  onSubmit: (
    payload: Row,
    mode: ModalMode,
    recordId?: string | null,
  ) => Promise<ApiResponse<Row>>;
}) {
  const [form, setForm] = useState<Row>(() => ({
    ...(row ?? {}),
    reason: row?.reason ?? "",
  }));
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<ApiResponse<unknown>["error"] | null>(
    null,
  );
  const title = screen.menuPath.split(" > ").slice(-1)[0];
  const editableFields = screen.readOnly
    ? []
    : screen.modalFields.filter((field) => !field.readOnly);
  const readonlyPairs = row
    ? Object.entries(row)
        .filter(([key]) => !["payloadJson"].includes(key))
        .slice(0, 14)
    : [];

  async function submit(event: FormEvent) {
    event.preventDefault();
    if (screen.readOnly) {
      onClose();
      return;
    }
    if (!String(form.reason ?? "").trim()) {
      setError({
        code: "VALIDATION",
        message: "변경 사유는 필수입니다.",
        fieldErrors: { reason: "변경 사유는 필수입니다." },
      });
      return;
    }
    setSaving(true);
    const result = await onSubmit(
      buildMutationPayload(screen, form, mode),
      mode,
      row ? String(row.recordId ?? "") : null,
    ).finally(() => setSaving(false));
    if (!result.success) setError(result.error ?? null);
  }

  return (
    <div className="modal-backdrop" role="presentation">
      <form className="modal" onSubmit={submit}>
        <header className="modal-header">
          <div>
            <span className="eyebrow">{screen.screenId}</span>
            <h2>
              {title} {mode === "create" ? "등록·실행 확인" : "상세·변경 확인"}
            </h2>
          </div>
          <button
            className="icon-button"
            onClick={onClose}
            type="button"
            aria-label="닫기"
          >
            ×
          </button>
        </header>
        <p className="readonly-note">{screen.constraint}</p>
        {error && <ErrorAlert error={error} />}

        {readonlyPairs.length > 0 && (
          <section className="detail-grid" aria-label="선택 행 조회 데이터">
            {readonlyPairs.map(([key, value]) => (
              <div key={key}>
                <small>{key}</small>
                <span>{formatValue(value)}</span>
              </div>
            ))}
          </section>
        )}

        {!screen.readOnly && (
          <section className="form-grid">
            {editableFields.map((field) => (
              <FieldInput
                key={field.key}
                field={field}
                value={form[field.key] ?? form[toSnake(field.key)] ?? ""}
                fieldError={error?.fieldErrors?.[field.key]}
                onChange={(value) =>
                  setForm((current) => ({ ...current, [field.key]: value }))
                }
              />
            ))}
            <label className="field wide">
              변경 사유
              <textarea
                value={String(form.reason ?? "")}
                onChange={(event) =>
                  setForm((current) => ({
                    ...current,
                    reason: event.target.value,
                  }))
                }
              />
              {error?.fieldErrors?.reason && (
                <em>{error.fieldErrors.reason}</em>
              )}
            </label>
          </section>
        )}

        <footer className="modal-actions">
          <button className="ghost-button" onClick={onClose} type="button">
            취소/닫기
          </button>
          <button className="primary-button" disabled={saving} type="submit">
            {screen.readOnly ? "닫기" : saving ? "처리 중" : "확인·실행"}
          </button>
        </footer>
      </form>
    </div>
  );
}

function FieldInput({
  field,
  value,
  fieldError,
  onChange,
}: {
  field: FieldContract;
  value: unknown;
  fieldError?: string;
  onChange: (value: string) => void;
}) {
  const label = field.label;
  const lowerKey = field.key.toLowerCase();
  const stringValue = String(value ?? "");
  if (lowerKey.includes("yn")) {
    return (
      <label className="field compact">
        {label}
        <select
          value={stringValue || "Y"}
          onChange={(event) => onChange(event.target.value)}
        >
          <option value="Y">예</option>
          <option value="N">아니오</option>
        </select>
        {fieldError && <em>{fieldError}</em>}
      </label>
    );
  }
  if (
    lowerKey.includes("date") ||
    lowerKey.includes("from") ||
    lowerKey.includes("to")
  ) {
    return (
      <label className="field compact">
        {label}
        <input
          type="date"
          value={stringValue.slice(0, 10)}
          onChange={(event) => onChange(event.target.value)}
        />
        {fieldError && <em>{fieldError}</em>}
      </label>
    );
  }
  return (
    <label className="field">
      {label}
      <input
        value={stringValue}
        onChange={(event) => onChange(event.target.value)}
      />
      {fieldError && <em>{fieldError}</em>}
    </label>
  );
}

function ErrorAlert({
  error,
}: {
  error: NonNullable<ApiResponse<unknown>["error"]>;
}) {
  return (
    <div className="alert error" role="alert">
      <strong>{error.code}</strong>
      <span>{error.message}</span>
      {error.fieldErrors && (
        <ul>
          {Object.entries(error.fieldErrors).map(([field, message]) => (
            <li key={field}>
              {field}: {message}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}

function Pagination({
  page,
  size,
  total,
  onPage,
}: {
  page: number;
  size: number;
  total: number;
  onPage: (page: number) => void;
}) {
  const lastPage = Math.max(Math.ceil(total / size) - 1, 0);
  return (
    <div className="pagination">
      <span>
        {page + 1} / {lastPage + 1} page
      </span>
      <div>
        <button
          className="ghost-button"
          disabled={page <= 0}
          onClick={() => onPage(page - 1)}
          type="button"
        >
          이전
        </button>
        <button
          className="ghost-button"
          disabled={page >= lastPage}
          onClick={() => onPage(page + 1)}
          type="button"
        >
          다음
        </button>
      </div>
    </div>
  );
}

function formatValue(value: unknown) {
  if (value == null || value === "") return "-";
  if (typeof value === "boolean") return value ? "예" : "아니오";
  return String(value);
}

function toSnake(key: string) {
  return key.replace(/[A-Z]/g, (letter) => `_${letter.toLowerCase()}`);
}
