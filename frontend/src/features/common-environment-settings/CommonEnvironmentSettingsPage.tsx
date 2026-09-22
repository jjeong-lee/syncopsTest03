import { useState } from "react";
import { ApiRequestError } from "../../shared/api/client";
import {
  listCommonEnvironmentSettings,
  saveCommonEnvironmentSettings,
  type CommonEnvironmentSetting,
  type CommonEnvironmentSettingsSearch,
} from "./Api";

const initialSearch: CommonEnvironmentSettingsSearch = {
  settingKey: "",
  size: "20",
};

export function CommonEnvironmentSettingsPage() {
  const [search, setSearch] =
    useState<CommonEnvironmentSettingsSearch>(initialSearch);
  const [rows, setRows] = useState<CommonEnvironmentSetting[]>([]);
  const [form, setForm] = useState<CommonEnvironmentSetting | null>(null);
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
      const response = await listCommonEnvironmentSettings(search);
      setRows(response.data);
      setState(response.data.length ? "success" : "empty");
      if (showSuccess) setMessage("저장 후 공통 환경설정을 다시 조회했습니다.");
    } catch (error) {
      const denied = isPermissionError(error);
      setState(denied ? "permission" : "error");
      setMessage(
        denied
          ? "권한이 없어 공통 환경설정을 조회하거나 변경할 수 없습니다."
          : "공통 환경설정을 조회하지 못했습니다.",
      );
    }
  };
  const save = async () => {
    if (!form) return;
    if (!form.settingValue.trim()) {
      setFieldError("설정값은 필수입니다.");
      return;
    }
    if (!window.confirm("공통 환경설정을 저장하시겠습니까?")) return;
    setState("loading");
    setMessage("");
    setFieldError("");
    try {
      await saveCommonEnvironmentSettings(form);
      setForm(null);
      await loadSettings(true);
    } catch (error) {
      if (isPermissionError(error)) {
        setForm(null);
        setState("permission");
        setMessage(
          "권한이 없어 공통 환경설정을 조회하거나 변경할 수 없습니다.",
        );
        return;
      }
      setState("success");
      setFieldError(
        error instanceof ApiRequestError
          ? error.message
          : "공통 환경설정을 저장하지 못했습니다.",
      );
    }
  };
  if (state === "permission")
    return (
      <section
        className="menu-information-management menu-information-state"
        data-testid="common-environment-settings-page"
      >
        <p className="breadcrumb">
          시스템 관리 &gt; 시스템 환경설정 &gt; 공통 환경설정
        </p>
        <h1>공통 환경설정</h1>
        <p>{message}</p>
      </section>
    );
  return (
    <section
      className="menu-information-management"
      data-testid="common-environment-settings-page"
      aria-labelledby="common-environment-settings-title"
    >
      <p className="breadcrumb">
        시스템 관리 &gt; 시스템 환경설정 &gt; 공통 환경설정
      </p>
      <h1 id="common-environment-settings-title">공통 환경설정</h1>
      <section
        className="menu-information-search"
        aria-label="공통 환경설정 조회"
      >
        <label>
          설정 항목 키
          <input
            data-testid="common-environment-setting-key-input"
            value={search.settingKey}
            onChange={(event) =>
              setSearch({ ...search, settingKey: event.target.value })
            }
          />
        </label>
        <label>
          목록 건수
          <select
            data-testid="common-environment-size-select"
            value={search.size}
            onChange={(event) =>
              setSearch({
                ...search,
                size: event.target
                  .value as CommonEnvironmentSettingsSearch["size"],
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
            data-testid="common-environment-query-button"
            type="button"
            className="primary-action"
            onClick={() => void loadSettings()}
          >
            조회
          </button>
        </div>
      </section>
      <p className="settings-guidance">
        세션 유휴시간 변경은 새로 만들어지는 세션부터 적용되며, 이미 열린 세션의
        유휴시간은 변경하지 않습니다.
      </p>
      {state === "loading" && (
        <p className="loading-message">공통 환경설정을 조회하고 있습니다.</p>
      )}
      {state === "empty" && (
        <p className="empty-message">조회된 공통 환경설정이 없습니다.</p>
      )}
      {state === "error" && (
        <p className="error-message">
          {message}{" "}
          <button
            data-testid="common-environment-retry-button"
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
          aria-label="공통 환경설정 목록"
        >
          <div className="section-title">
            <h2>공통 환경설정</h2>
          </div>
          <div className="table-scroll">
            <table>
              <thead>
                <tr>
                  <th>설정 항목</th>
                  <th>현재 값</th>
                  <th>동작</th>
                </tr>
              </thead>
              <tbody>
                {rows.map((row) => (
                  <tr
                    key={row.settingKey}
                    data-testid={`common-environment-row-${row.settingKey}`}
                  >
                    <td>{row.settingKey}</td>
                    <td>{row.settingValue}</td>
                    <td>
                      <button
                        data-testid={`common-environment-edit-${row.settingKey}`}
                        type="button"
                        className="text-action"
                        onClick={() => {
                          setFieldError("");
                          setForm({ ...row });
                        }}
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
            data-testid="common-environment-settings-edit-modal"
            role="dialog"
            aria-modal="true"
            aria-labelledby="common-environment-settings-edit-title"
          >
            <div className="section-title">
              <h2 id="common-environment-settings-edit-title">
                공통 환경설정 변경
              </h2>
              <button
                data-testid="common-environment-close-button"
                type="button"
                className="text-action"
                onClick={() => setForm(null)}
              >
                닫기
              </button>
            </div>
            <div className="menu-information-form">
              <label>
                설정 항목
                <input
                  data-testid="common-environment-readonly-key-input"
                  value={form.settingKey}
                  readOnly
                />
              </label>
              <label>
                설정값 *
                <input
                  data-testid="common-environment-setting-value-input"
                  value={form.settingValue}
                  onChange={(event) =>
                    setForm({ ...form, settingValue: event.target.value })
                  }
                />
              </label>
            </div>
            <p>페이지당 조회건수 항목은 20, 50, 100만 입력할 수 있습니다.</p>
            {fieldError && (
              <p className="error-message" role="alert">
                {fieldError}
              </p>
            )}
            <div className="form-actions">
              <button
                data-testid="common-environment-cancel-button"
                type="button"
                className="text-action"
                onClick={() => setForm(null)}
              >
                취소
              </button>
              <button
                data-testid="common-environment-save-button"
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
