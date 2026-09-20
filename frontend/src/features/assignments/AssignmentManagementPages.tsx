import { useState } from "react";
import { ApiRequestError, apiRequest } from "../../shared/api/client";
import "./assignment-management.css";

type AssignmentKind = "position" | "work" | "scope";
type AssignmentRow = Record<string, string | null>;
type State = "idle" | "loading" | "empty" | "error" | "permission" | "success";

const scopes = ["본인", "소속학과", "단과대학", "담당업무", "전체"];

function AssignmentPage({ kind }: { kind: AssignmentKind }) {
  const position = kind === "position";
  const work = kind === "work";
  const title = position
    ? "보직 관리"
    : work
      ? "업무담당자 관리"
      : "데이터 범위 권한";
  const endpoint = position
    ? "position-assignments"
    : work
      ? "work-assignments"
      : "role-data-scopes";
  const [rows, setRows] = useState<AssignmentRow[]>([]);
  const [selected, setSelected] = useState<AssignmentRow | null>(null);
  const [state, setState] = useState<State>("idle");
  const [message, setMessage] = useState("");
  const [modalOpen, setModalOpen] = useState(false);
  const [confirming, setConfirming] = useState(false);
  const [search, setSearch] = useState({
    first: "",
    second: "",
    referenceDate: "",
    size: "20",
  });
  const [form, setForm] = useState<Record<string, string>>({
    dataScopeType: "전체",
    processPermission: "Y",
  });

  const query = (includePage = true) => {
    const params = new URLSearchParams();
    if (position) {
      if (search.first) params.set("positionCode", search.first);
      if (search.second) params.set("userId", search.second);
    }
    if (work) {
      if (search.first) params.set("organizationId", search.first);
      if (search.second) params.set("userId", search.second);
    }
    if (!position && !work) {
      if (search.first) params.set("roleCode", search.first);
      if (search.second) params.set("dataScopeType", search.second);
    }
    if (search.referenceDate && kind !== "scope")
      params.set("referenceDate", search.referenceDate);
    if (includePage) {
      params.set("page", "0");
      params.set("size", search.size);
    }
    return `/api/${endpoint}${params.toString() ? `?${params}` : ""}` as `/api/${string}`;
  };

  const load = async (showSuccess = false) => {
    setState("loading");
    setMessage("");
    try {
      const response = await apiRequest<AssignmentRow[]>(query());
      setRows(response.data);
      setState(response.data.length ? "success" : "empty");
      if (showSuccess) setMessage("저장 후 현재 조건으로 다시 조회했습니다.");
    } catch (error) {
      const denied =
        error instanceof ApiRequestError &&
        (error.status === 401 || error.status === 403);
      setState(denied ? "permission" : "error");
      setMessage(denied ? "권한이 없습니다." : "조회에 실패했습니다.");
    }
  };

  const download = async () => {
    try {
      const response = await fetch(
        query(false).replace(`/api/${endpoint}`, `/api/${endpoint}/export`),
        { credentials: "include" },
      );
      if (!response.ok) throw new Error();
      const url = URL.createObjectURL(await response.blob());
      const link = document.createElement("a");
      link.href = url;
      link.download = `${endpoint}.xlsx`;
      link.click();
      URL.revokeObjectURL(url);
    } catch {
      setState("error");
      setMessage("Excel 다운로드에 실패했습니다.");
    }
  };

  const open = (row?: AssignmentRow) => {
    setForm(
      row
        ? Object.fromEntries(
            Object.entries(row).map(([key, value]) => [key, value ?? ""]),
          )
        : { dataScopeType: "전체", processPermission: "Y" },
    );
    setConfirming(false);
    setMessage("");
    setModalOpen(true);
  };
  const required = position
    ? ["positionCode", "userId", "organizationId", "effectiveStartDate"]
    : work
      ? [
          "organizationId",
          "userId",
          "workArea",
          "effectiveStartDate",
          "dataScopeType",
          "processPermission",
        ]
      : ["roleCode", "dataScopeType"];
  const save = async () => {
    if (required.some((field) => !form[field])) {
      setMessage("필수 입력 항목을 확인하세요.");
      return;
    }
    if (work && !confirming) {
      setConfirming(true);
      return;
    }
    try {
      await apiRequest<null>(`/api/${endpoint}` as `/api/${string}`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(form),
      });
      setModalOpen(false);
      await load(true);
    } catch (error) {
      setMessage(
        error instanceof ApiRequestError
          ? error.message
          : "저장에 실패했습니다.",
      );
    }
  };
  const label = (field: string, text: string, type = "text") => (
    <label>
      {text}
      <input
        aria-label={text}
        data-testid={`${kind}-${field}-input`}
        type={type}
        value={form[field] ?? ""}
        onChange={(event) => setForm({ ...form, [field]: event.target.value })}
      />
    </label>
  );
  const columns = position
    ? [
        ["positionCode", "보직코드"],
        ["userId", "대상 사용자"],
        ["organizationId", "소속조직"],
        ["effectiveStartDate", "유효 시작일"],
        ["effectiveEndDate", "유효 종료일"],
      ]
    : work
      ? [
          ["organizationId", "업무조직"],
          ["userId", "담당자"],
          ["workArea", "담당 업무영역"],
          ["effectiveStartDate", "지정 시작일"],
          ["effectiveEndDate", "지정 종료일"],
          ["dataScopeType", "데이터 범위"],
          ["processPermission", "처리 권한"],
        ]
      : [
          ["roleCode", "역할"],
          ["dataScopeType", "데이터 범위 유형"],
          ["organizationCode", "조직코드"],
          ["workArea", "업무영역"],
        ];
  if (state === "permission")
    return (
      <section
        className="assignment-management"
        data-testid={`${kind}-assignment-page`}
      >
        <h1>{title}</h1>
        <p>권한이 없습니다.</p>
      </section>
    );
  return (
    <section
      className="assignment-management"
      data-testid={`${kind}-assignment-page`}
    >
      <p className="breadcrumb">
        시스템 관리 &gt;{" "}
        {position || work ? "사용자·조직 관리" : "역할·권한 관리"} &gt; {title}
      </p>
      <h1>{title}</h1>
      <section className="assignment-search" aria-label={`${title} 조회`}>
        <label>
          {position ? "보직코드" : work ? "업무조직" : "역할"}
          <input
            data-testid={`${kind}-search-first-input`}
            value={search.first}
            onChange={(event) =>
              setSearch({ ...search, first: event.target.value })
            }
          />
        </label>
        <label>
          {position ? "대상 사용자" : work ? "담당자" : "데이터 범위 유형"}
          <input
            data-testid={`${kind}-search-second-input`}
            value={search.second}
            onChange={(event) =>
              setSearch({ ...search, second: event.target.value })
            }
          />
        </label>
        {kind !== "scope" && (
          <label>
            기준일
            <input
              data-testid={`${kind}-reference-date-input`}
              type="date"
              value={search.referenceDate}
              onChange={(event) =>
                setSearch({ ...search, referenceDate: event.target.value })
              }
            />
          </label>
        )}
        <label>
          목록 건수
          <select
            aria-label="목록 건수"
            data-testid={`${kind}-page-size-select`}
            value={search.size}
            onChange={(event) =>
              setSearch({ ...search, size: event.target.value })
            }
          >
            <option value="20">20</option>
            <option value="50">50</option>
            <option value="100">100</option>
          </select>
        </label>
        <div className="form-actions">
          <button
            className="text-action"
            type="button"
            data-testid={`${kind}-export-button`}
            onClick={() => void download()}
          >
            Excel 다운로드
          </button>
          <button
            className="primary-action"
            type="button"
            data-testid={`${kind}-search-button`}
            onClick={() => void load()}
          >
            조회
          </button>
        </div>
      </section>
      {state === "loading" && (
        <p className="loading-message">목록을 조회 중입니다.</p>
      )}
      {state === "empty" && (
        <p className="empty-message">조건에 맞는 정보가 없습니다.</p>
      )}
      {state === "error" && (
        <p className="error-message">
          {message}{" "}
          <button
            className="text-action"
            type="button"
            data-testid={`${kind}-retry-button`}
            onClick={() => void load()}
          >
            다시 시도
          </button>
        </p>
      )}
      {message && state === "success" && (
        <p className="success-message">{message}</p>
      )}
      {(state === "success" || state === "empty" || state === "idle") && (
        <section className="assignment-results">
          <div className="section-title">
            <h2>{title} 목록</h2>
            <button
              className="primary-action"
              type="button"
              data-testid={`${kind}-save-open-button`}
              onClick={() => open()}
            >
              {position ? "보직 등록·변경" : work ? "담당자 지정" : "등록·변경"}
            </button>
          </div>
          {state === "success" && (
            <div className="table-scroll">
              <table>
                <thead>
                  <tr>
                    {columns.map(([, text]) => (
                      <th key={text}>{text}</th>
                    ))}
                    <th>작업</th>
                  </tr>
                </thead>
                <tbody>
                  {rows.map((row, index) => (
                    <tr
                      data-testid={`${kind}-assignment-row-${index}`}
                      key={
                        row[`${kind}AssignmentId`] ??
                        row.roleDataScopeId ??
                        index
                      }
                      onClick={() => setSelected(row)}
                    >
                      {columns.map(([field]) => (
                        <td key={field}>{row[field] ?? "-"}</td>
                      ))}
                      <td>
                        <button
                          className="text-action"
                          type="button"
                          onClick={() => open(row)}
                        >
                          변경
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </section>
      )}
      {selected && (
        <section className="assignment-detail">
          <h2>선택 상세</h2>
          {columns.map(([field, text]) => (
            <p key={field}>
              <strong>{text}</strong> {selected[field] ?? "-"}
            </p>
          ))}
        </section>
      )}
      {modalOpen && (
        <div className="modal-backdrop">
          <section
            className="assignment-modal"
            role="dialog"
            aria-modal="true"
            data-testid={`${kind}-assignment-modal`}
          >
            <div className="section-title">
              <h2>{title} 등록·변경</h2>
              <button
                className="text-action"
                type="button"
                data-testid={`${kind}-modal-close-button`}
                onClick={() => setModalOpen(false)}
              >
                닫기
              </button>
            </div>
            <div className="assignment-form">
              {columns.map(([field, text]) => (
                <div key={field}>
                  {label(field, text, field.includes("Date") ? "date" : "text")}
                </div>
              ))}
              {(work || kind === "scope") && (
                <label>
                  데이터 범위
                  <select
                    aria-label="데이터 범위"
                    data-testid={`${kind}-data-scope-select`}
                    value={form.dataScopeType ?? "전체"}
                    onChange={(event) =>
                      setForm({ ...form, dataScopeType: event.target.value })
                    }
                  >
                    {scopes.map((scope) => (
                      <option key={scope}>{scope}</option>
                    ))}
                  </select>
                </label>
              )}
            </div>
            {confirming && (
              <p role="alert">입력한 업무담당자 지정을 저장하시겠습니까?</p>
            )}
            {message && <p className="error-message">{message}</p>}
            <div className="form-actions">
              <button
                className="text-action"
                type="button"
                data-testid={`${kind}-modal-cancel-button`}
                onClick={() => setModalOpen(false)}
              >
                취소
              </button>
              <button
                className="primary-action"
                type="button"
                data-testid={`${kind}-modal-save-button`}
                onClick={() => void save()}
              >
                저장
              </button>
            </div>
          </section>
        </div>
      )}
    </section>
  );
}
export const PositionAssignmentManagementPage = () => (
  <AssignmentPage kind="position" />
);
export const WorkAssignmentManagementPage = () => (
  <AssignmentPage kind="work" />
);
export const RoleDataScopeManagementPage = () => (
  <AssignmentPage kind="scope" />
);
