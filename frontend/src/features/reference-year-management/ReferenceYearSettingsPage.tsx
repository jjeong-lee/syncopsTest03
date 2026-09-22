import { useState } from "react";
import { ApiRequestError } from "../../shared/api/client";
import {
  listReferenceYearSettings,
  saveReferenceYearSettings,
  type ReferenceYearSetting,
  type ReferenceYearSettingsSearch,
} from "./Api";

const initialSearch: ReferenceYearSettingsSearch = {
  targetYear: "",
  size: "20",
};
type EditForm = {
  currentEvaluationYear: string;
  defaultQueryYear: string;
  targetYear: string;
  baselineCopyYn: "예" | "아니오";
  initializationYn: "예" | "아니오";
};

const emptyForm = (): EditForm => ({
  currentEvaluationYear: "",
  defaultQueryYear: "",
  targetYear: "",
  baselineCopyYn: "아니오",
  initializationYn: "아니오",
});

export function ReferenceYearSettingsPage() {
  const [search, setSearch] =
    useState<ReferenceYearSettingsSearch>(initialSearch);
  const [rows, setRows] = useState<ReferenceYearSetting[]>([]);
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
      const response = await listReferenceYearSettings(search);
      setRows(response.data);
      setState(response.data.length ? "success" : "empty");
      if (showSuccess) setMessage("저장 후 기준연도 설정을 다시 조회했습니다.");
    } catch (error) {
      const denied = isPermissionError(error);
      setState(denied ? "permission" : "error");
      setMessage(
        denied
          ? "권한이 없어 기준연도 설정을 조회하거나 변경할 수 없습니다."
          : "기준연도 설정을 조회하지 못했습니다.",
      );
    }
  };

  const openEdit = (setting?: ReferenceYearSetting) => {
    setFieldError("");
    setForm(
      setting
        ? {
            currentEvaluationYear: String(setting.currentEvaluationYear),
            defaultQueryYear: String(setting.defaultQueryYear),
            targetYear: String(setting.targetYear),
            baselineCopyYn: setting.baselineCopyYn,
            initializationYn: setting.initializationYn,
          }
        : emptyForm(),
    );
  };

  const save = async () => {
    if (!form) return;
    if (
      !form.currentEvaluationYear ||
      !form.defaultQueryYear ||
      !form.targetYear
    ) {
      setFieldError("모든 연도 입력은 필수입니다.");
      return;
    }
    if (!window.confirm("기준연도 설정을 저장하시겠습니까?")) return;
    setState("loading");
    setMessage("");
    setFieldError("");
    try {
      await saveReferenceYearSettings({
        currentEvaluationYear: Number(form.currentEvaluationYear),
        defaultQueryYear: Number(form.defaultQueryYear),
        targetYear: Number(form.targetYear),
        baselineCopyYn: form.baselineCopyYn,
        initializationYn: form.initializationYn,
      });
      setForm(null);
      await loadSettings(true);
    } catch (error) {
      if (isPermissionError(error)) {
        setForm(null);
        setState("permission");
        setMessage(
          "권한이 없어 기준연도 설정을 조회하거나 변경할 수 없습니다.",
        );
        return;
      }
      setState("success");
      setFieldError(
        error instanceof ApiRequestError
          ? error.message
          : "기준연도 설정을 저장하지 못했습니다.",
      );
    }
  };

  if (state === "permission") {
    return (
      <section
        className="menu-information-management menu-information-state"
        data-testid="reference-year-settings-page"
      >
        <p className="breadcrumb">
          시스템 관리 &gt; 시스템 환경설정 &gt; 기준연도 관리
        </p>
        <h1>기준연도 관리</h1>
        <p>{message}</p>
      </section>
    );
  }

  return (
    <section
      className="menu-information-management"
      data-testid="reference-year-settings-page"
      aria-labelledby="reference-year-settings-title"
    >
      <p className="breadcrumb">
        시스템 관리 &gt; 시스템 환경설정 &gt; 기준연도 관리
      </p>
      <h1 id="reference-year-settings-title">기준연도 관리</h1>
      <section
        className="menu-information-search"
        aria-label="기준연도 설정 조회"
      >
        <label>
          대상 연도
          <input
            data-testid="reference-year-target-year-input"
            inputMode="numeric"
            value={search.targetYear}
            onChange={(event) =>
              setSearch({ ...search, targetYear: event.target.value })
            }
          />
        </label>
        <label>
          목록 건수
          <select
            data-testid="reference-year-size-select"
            value={search.size}
            onChange={(event) =>
              setSearch({
                ...search,
                size: event.target.value as ReferenceYearSettingsSearch["size"],
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
            data-testid="reference-year-query-button"
            type="button"
            className="primary-action"
            onClick={() => void loadSettings()}
          >
            조회
          </button>
          <button
            data-testid="reference-year-create-button"
            type="button"
            className="text-action"
            onClick={() => openEdit()}
          >
            설정 변경
          </button>
        </div>
      </section>
      {state === "loading" && (
        <p className="loading-message">기준연도 설정을 조회하고 있습니다.</p>
      )}
      {state === "empty" && (
        <p className="empty-message">
          현재 조건에 맞는 기준연도 설정이 없습니다.
        </p>
      )}
      {state === "error" && (
        <p className="error-message">
          {message}{" "}
          <button
            data-testid="reference-year-retry-button"
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
          aria-label="기준연도 설정 목록"
        >
          <div className="section-title">
            <h2>기준연도 설정</h2>
          </div>
          <div className="table-scroll">
            <table>
              <thead>
                <tr>
                  <th>현재 평가연도</th>
                  <th>기본 조회연도</th>
                  <th>대상 연도</th>
                  <th>기준정보 복사 여부</th>
                  <th>초기화 여부</th>
                  <th>관리</th>
                </tr>
              </thead>
              <tbody>
                {rows.map((row) => (
                  <tr
                    key={row.targetYear}
                    data-testid={`reference-year-row-${row.targetYear}`}
                  >
                    <td>{row.currentEvaluationYear}</td>
                    <td>{row.defaultQueryYear}</td>
                    <td>{row.targetYear}</td>
                    <td>{row.baselineCopyYn}</td>
                    <td>{row.initializationYn}</td>
                    <td>
                      <button
                        data-testid={`reference-year-edit-${row.targetYear}`}
                        type="button"
                        className="text-action"
                        onClick={() => openEdit(row)}
                      >
                        변경
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
            data-testid="reference-year-settings-edit-modal"
            role="dialog"
            aria-modal="true"
            aria-labelledby="reference-year-settings-edit-title"
          >
            <div className="section-title">
              <h2 id="reference-year-settings-edit-title">
                기준연도 설정 변경
              </h2>
              <button
                data-testid="reference-year-close-button"
                type="button"
                className="text-action"
                onClick={() => setForm(null)}
              >
                닫기
              </button>
            </div>
            <div className="menu-information-form">
              <label>
                현재 평가연도 *
                <input
                  data-testid="reference-year-current-evaluation-year-input"
                  inputMode="numeric"
                  value={form.currentEvaluationYear}
                  onChange={(event) =>
                    setForm({
                      ...form,
                      currentEvaluationYear: event.target.value,
                    })
                  }
                />
              </label>
              <label>
                기본 조회연도 *
                <input
                  data-testid="reference-year-default-query-year-input"
                  inputMode="numeric"
                  value={form.defaultQueryYear}
                  onChange={(event) =>
                    setForm({ ...form, defaultQueryYear: event.target.value })
                  }
                />
              </label>
              <label>
                대상 연도 *
                <input
                  data-testid="reference-year-target-year-edit-input"
                  inputMode="numeric"
                  value={form.targetYear}
                  onChange={(event) =>
                    setForm({ ...form, targetYear: event.target.value })
                  }
                />
              </label>
              <label>
                기준정보 복사 여부 *
                <select
                  data-testid="reference-year-baseline-copy-select"
                  value={form.baselineCopyYn}
                  onChange={(event) =>
                    setForm({
                      ...form,
                      baselineCopyYn: event.target
                        .value as EditForm["baselineCopyYn"],
                    })
                  }
                >
                  <option value="예">예</option>
                  <option value="아니오">아니오</option>
                </select>
              </label>
              <label>
                초기화 여부 *
                <select
                  data-testid="reference-year-initialization-select"
                  value={form.initializationYn}
                  onChange={(event) =>
                    setForm({
                      ...form,
                      initializationYn: event.target
                        .value as EditForm["initializationYn"],
                    })
                  }
                >
                  <option value="예">예</option>
                  <option value="아니오">아니오</option>
                </select>
              </label>
            </div>
            <p>
              기준정보 복사·초기화 여부는 대상 연도의 설정값만 저장합니다. 기존
              연도의 평가자료를 변경하지 않습니다.
            </p>
            {fieldError && (
              <p className="error-message" role="alert">
                {fieldError}
              </p>
            )}
            <div className="form-actions">
              <button
                data-testid="reference-year-cancel-button"
                type="button"
                className="text-action"
                onClick={() => setForm(null)}
              >
                취소
              </button>
              <button
                data-testid="reference-year-save-button"
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
