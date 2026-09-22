import { useState } from "react";
import { ApiRequestError } from "../../shared/api/client";
import {
  listDetailCodeUsageSettings,
  saveDetailCodeUsageSettings,
  type DetailCodeUsageSearch,
  type DetailCodeUsageSetting,
} from "./Api";

type EditForm = DetailCodeUsageSetting;

const initialSearch: DetailCodeUsageSearch = {
  groupId: "",
  codeValue: "",
  useYn: "",
  size: "20",
};

function formatUseYn(value: "Y" | "N") {
  return value === "Y" ? "사용" : "중지";
}

export function DetailCodeUsagePage() {
  const [search, setSearch] = useState<DetailCodeUsageSearch>(initialSearch);
  const [rows, setRows] = useState<DetailCodeUsageSetting[]>([]);
  const [form, setForm] = useState<EditForm | null>(null);
  const [state, setState] = useState<
    "idle" | "loading" | "empty" | "error" | "permission" | "success"
  >("idle");
  const [message, setMessage] = useState("");
  const [fieldError, setFieldError] = useState("");

  const isPermissionError = (error: unknown) =>
    error instanceof ApiRequestError &&
    (error.status === 401 || error.status === 403);

  const loadSettings = async (showSuccess = false) => {
    setState("loading");
    setMessage("");
    try {
      const response = await listDetailCodeUsageSettings(search);
      setRows(response.data);
      setState(response.data.length ? "success" : "empty");
      if (showSuccess) {
        setMessage("저장 후 코드 사용여부와 적용기간을 다시 조회했습니다.");
      }
    } catch (error) {
      const permissionDenied = isPermissionError(error);
      setState(permissionDenied ? "permission" : "error");
      setMessage(
        permissionDenied
          ? "권한이 없어 코드 사용 설정을 조회하거나 변경할 수 없습니다."
          : "코드 사용 설정을 조회하지 못했습니다.",
      );
    }
  };

  const openEdit = (setting: DetailCodeUsageSetting) => {
    setFieldError("");
    setForm({ ...setting });
  };

  const save = async () => {
    if (!form) return;
    if (!form.effectiveStartDate) {
      setFieldError("적용 시작일은 필수입니다.");
      return;
    }
    if (!window.confirm("코드 사용여부와 적용기간을 저장하시겠습니까?")) return;

    setState("loading");
    setMessage("");
    setFieldError("");
    try {
      await saveDetailCodeUsageSettings(form);
      setForm(null);
      await loadSettings(true);
    } catch (error) {
      if (isPermissionError(error)) {
        setForm(null);
        setState("permission");
        setMessage(
          "권한이 없어 코드 사용 설정을 조회하거나 변경할 수 없습니다.",
        );
        return;
      }
      setState("success");
      setFieldError(
        error instanceof ApiRequestError
          ? error.message
          : "코드 사용 설정을 저장하지 못했습니다.",
      );
    }
  };

  if (state === "permission") {
    return (
      <section
        className="menu-information-management menu-information-state"
        data-testid="detail-code-usage-page"
      >
        <p className="breadcrumb">
          시스템 관리 &gt; 공통코드 관리 &gt; 코드 사용 관리
        </p>
        <h1>코드 사용 관리</h1>
        <p>{message}</p>
      </section>
    );
  }

  return (
    <section
      className="menu-information-management"
      data-testid="detail-code-usage-page"
      aria-labelledby="detail-code-usage-title"
    >
      <p className="breadcrumb">
        시스템 관리 &gt; 공통코드 관리 &gt; 코드 사용 관리
      </p>
      <h1 id="detail-code-usage-title">코드 사용 관리</h1>
      <section
        className="menu-information-search"
        aria-label="코드 사용 설정 조회"
      >
        <label>
          코드그룹
          <input
            data-testid="detail-code-usage-group-id-input"
            value={search.groupId}
            onChange={(event) =>
              setSearch({ ...search, groupId: event.target.value })
            }
          />
        </label>
        <label>
          코드값
          <input
            data-testid="detail-code-usage-code-value-input"
            value={search.codeValue}
            onChange={(event) =>
              setSearch({ ...search, codeValue: event.target.value })
            }
          />
        </label>
        <label>
          사용여부
          <select
            data-testid="detail-code-usage-search-use-yn-select"
            aria-label="검색 사용여부"
            value={search.useYn}
            onChange={(event) =>
              setSearch({ ...search, useYn: event.target.value })
            }
          >
            <option value="">전체</option>
            <option value="Y">사용</option>
            <option value="N">중지</option>
          </select>
        </label>
        <label>
          목록 건수
          <select
            data-testid="detail-code-usage-size-select"
            value={search.size}
            onChange={(event) =>
              setSearch({
                ...search,
                size: event.target.value as DetailCodeUsageSearch["size"],
              })
            }
          >
            <option value="20">20</option>
            <option value="50">50</option>
            <option value="100">100</option>
          </select>
        </label>
        <div className="form-actions">
          <button
            data-testid="detail-code-usage-query-button"
            type="button"
            className="primary-action"
            onClick={() => void loadSettings()}
          >
            조회
          </button>
        </div>
      </section>

      {state === "loading" && (
        <p className="loading-message">코드 사용 설정을 조회하고 있습니다.</p>
      )}
      {state === "empty" && (
        <p className="empty-message">조건에 맞는 코드 사용 설정이 없습니다.</p>
      )}
      {state === "error" && (
        <p className="error-message">
          {message}{" "}
          <button
            data-testid="detail-code-usage-retry-button"
            type="button"
            className="text-action"
            onClick={() => void loadSettings()}
          >
            다시 시도
          </button>
        </p>
      )}
      {message && state === "success" && (
        <p className="success-message" role="status">
          {message}
        </p>
      )}

      {state === "success" && (
        <section
          className="menu-information-results"
          aria-label="코드 사용 설정 목록"
        >
          <div className="section-title">
            <h2>코드 사용 설정</h2>
          </div>
          <div className="table-scroll">
            <table>
              <thead>
                <tr>
                  <th>코드그룹</th>
                  <th>코드값</th>
                  <th>사용여부</th>
                  <th>적용 시작일</th>
                  <th>적용 종료일</th>
                  <th>작업</th>
                </tr>
              </thead>
              <tbody>
                {rows.map((row) => (
                  <tr
                    key={`${row.groupId}-${row.codeValue}`}
                    data-testid={`detail-code-usage-row-${row.groupId}-${row.codeValue}`}
                  >
                    <td>{row.groupId}</td>
                    <td>{row.codeValue}</td>
                    <td>{formatUseYn(row.useYn)}</td>
                    <td>{row.effectiveStartDate}</td>
                    <td>{row.effectiveEndDate ?? "-"}</td>
                    <td>
                      <button
                        data-testid={`detail-code-usage-edit-${row.groupId}-${row.codeValue}`}
                        type="button"
                        className="text-action"
                        onClick={() => openEdit(row)}
                      >
                        수정
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </section>
      )}

      {form && (
        <div className="modal-backdrop" role="presentation">
          <section
            className="menu-information-modal"
            data-testid="detail-code-usage-edit-modal"
            role="dialog"
            aria-modal="true"
            aria-labelledby="detail-code-usage-edit-title"
          >
            <div className="section-title">
              <h2 id="detail-code-usage-edit-title">코드 사용 설정 수정</h2>
              <button
                data-testid="detail-code-usage-close-button"
                type="button"
                className="text-action"
                onClick={() => setForm(null)}
              >
                닫기
              </button>
            </div>
            <div className="menu-information-form">
              <label>
                코드그룹
                <input
                  data-testid="detail-code-usage-readonly-group-id"
                  value={form.groupId}
                  readOnly
                />
              </label>
              <label>
                코드값
                <input
                  data-testid="detail-code-usage-readonly-code-value"
                  value={form.codeValue}
                  readOnly
                />
              </label>
              <label>
                사용여부
                <select
                  data-testid="detail-code-usage-edit-use-yn-select"
                  aria-label="사용여부"
                  value={form.useYn}
                  onChange={(event) =>
                    setForm({ ...form, useYn: event.target.value as "Y" | "N" })
                  }
                >
                  <option value="Y">사용</option>
                  <option value="N">중지</option>
                </select>
              </label>
              <label>
                적용 시작일 *
                <input
                  data-testid="detail-code-usage-start-date-input"
                  type="date"
                  value={form.effectiveStartDate}
                  onChange={(event) =>
                    setForm({ ...form, effectiveStartDate: event.target.value })
                  }
                />
              </label>
              <label>
                적용 종료일
                <input
                  data-testid="detail-code-usage-end-date-input"
                  type="date"
                  value={form.effectiveEndDate ?? ""}
                  onChange={(event) =>
                    setForm({
                      ...form,
                      effectiveEndDate: event.target.value || null,
                    })
                  }
                />
              </label>
            </div>
            <p>
              시작일은 필수이며 종료일은 비울 수 있습니다. 시작일은 종료일보다
              늦을 수 없습니다.
            </p>
            {fieldError && (
              <p className="error-message" role="alert">
                {fieldError}
              </p>
            )}
            <div className="form-actions">
              <button
                data-testid="detail-code-usage-cancel-button"
                type="button"
                className="text-action"
                onClick={() => setForm(null)}
              >
                취소
              </button>
              <button
                data-testid="detail-code-usage-save-button"
                type="button"
                className="primary-action"
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
