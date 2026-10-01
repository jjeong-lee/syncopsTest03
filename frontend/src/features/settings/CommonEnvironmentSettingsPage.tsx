import { useEffect, useState } from "react";
import { ApiRequestError, apiRequest } from "../../shared/api/client";

type CommonSettings = {
  sessionIdleMinutes: number;
  pageSize: 20 | 50 | 100;
  defaultSearchPeriodDays: number;
  bulkQueryThreshold: number;
  longRunningWorkNoticeSeconds: number;
};

type FormValues = Record<keyof CommonSettings, string>;

const emptyForm: FormValues = {
  sessionIdleMinutes: "",
  pageSize: "20",
  defaultSearchPeriodDays: "",
  bulkQueryThreshold: "",
  longRunningWorkNoticeSeconds: "",
};

const labels: Record<keyof CommonSettings, string> = {
  sessionIdleMinutes: "세션 유휴시간(분)",
  pageSize: "페이지당 조회건수",
  defaultSearchPeriodDays: "기본 검색기간(일)",
  bulkQueryThreshold: "대량조회 기준건수(건)",
  longRunningWorkNoticeSeconds: "장시간작업 안내 기준(초)",
};

function toFormValues(settings: CommonSettings | null): FormValues {
  if (!settings) return emptyForm;
  return Object.fromEntries(
    Object.entries(settings).map(([key, value]) => [key, String(value)]),
  ) as FormValues;
}

export function CommonEnvironmentSettingsPage() {
  const [form, setForm] = useState<FormValues>(emptyForm);
  const [lastLoaded, setLastLoaded] = useState<FormValues>(emptyForm);
  const [state, setState] = useState<
    "loading" | "empty" | "error" | "permission" | "success"
  >("loading");
  const [message, setMessage] = useState("");
  const [fieldError, setFieldError] = useState<keyof CommonSettings | null>(
    null,
  );
  const [showConfirmation, setShowConfirmation] = useState(false);

  const loadSettings = async (showSuccess = false) => {
    setState("loading");
    setMessage("");
    try {
      const response = await apiRequest<CommonSettings | null>(
        "/api/settings/common",
      );
      const values = toFormValues(response.data);
      setForm(values);
      setLastLoaded(values);
      setState(response.data ? "success" : "empty");
      if (showSuccess)
        setMessage("저장 후 공통 환경설정 값을 다시 조회했습니다.");
    } catch (error) {
      const permissionDenied =
        error instanceof ApiRequestError &&
        (error.status === 401 || error.status === 403);
      setState(permissionDenied ? "permission" : "error");
      setMessage(
        permissionDenied
          ? "권한이 없습니다."
          : "공통 환경설정 조회에 실패했습니다.",
      );
    }
  };

  useEffect(() => {
    void loadSettings();
  }, []);

  const update = (key: keyof CommonSettings, value: string) => {
    setForm((current) => ({ ...current, [key]: value }));
    if (fieldError === key) setFieldError(null);
  };

  const openConfirmation = () => {
    const missing = (Object.keys(labels) as (keyof CommonSettings)[]).find(
      (key) => !form[key].trim(),
    );
    if (missing) {
      setFieldError(missing);
      return;
    }
    setFieldError(null);
    setShowConfirmation(true);
  };

  const save = async () => {
    setState("loading");
    setMessage("");
    setFieldError(null);
    try {
      await apiRequest<null>("/api/settings/common", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          sessionIdleMinutes: Number(form.sessionIdleMinutes),
          pageSize: Number(form.pageSize),
          defaultSearchPeriodDays: Number(form.defaultSearchPeriodDays),
          bulkQueryThreshold: Number(form.bulkQueryThreshold),
          longRunningWorkNoticeSeconds: Number(
            form.longRunningWorkNoticeSeconds,
          ),
        }),
      });
      setShowConfirmation(false);
      await loadSettings(true);
    } catch (error) {
      const permissionDenied =
        error instanceof ApiRequestError &&
        (error.status === 401 || error.status === 403);
      if (permissionDenied) {
        setShowConfirmation(false);
        setState("permission");
        setMessage("권한이 없습니다.");
        return;
      }
      const key =
        error instanceof ApiRequestError
          ? (error.field as keyof CommonSettings | undefined)
          : undefined;
      setFieldError(key && key in labels ? key : null);
      setState("error");
      setMessage(
        error instanceof ApiRequestError
          ? error.message
          : "공통 환경설정을 저장하지 못했습니다.",
      );
    }
  };

  if (state === "permission") {
    return (
      <section
        className="common-environment-settings settings-state"
        data-testid="common-environment-settings-page"
      >
        <h1>공통 환경설정</h1>
        <p>권한이 없습니다.</p>
      </section>
    );
  }

  return (
    <section
      className="common-environment-settings"
      data-testid="common-environment-settings-page"
      aria-labelledby="common-environment-settings-title"
    >
      <p className="breadcrumb">
        시스템 관리 &gt; 시스템 환경설정 &gt; 공통 환경설정
      </p>
      <h1 id="common-environment-settings-title">공통 환경설정</h1>
      <p className="readonly-note">
        세션 유휴시간 변경은 새로 만들어지는 세션부터 적용되며, 이미 열린 세션은
        변경하지 않습니다.
      </p>
      <section className="settings-form" aria-label="공통 환경설정 입력">
        {(Object.keys(labels) as (keyof CommonSettings)[]).map((key) => (
          <label key={key}>
            {labels[key]} <span aria-hidden="true">*</span>
            {key === "pageSize" ? (
              <select
                aria-label={labels[key]}
                data-testid="common-settings-page-size-select"
                value={form[key]}
                onChange={(event) => update(key, event.target.value)}
              >
                <option value="20">20</option>
                <option value="50">50</option>
                <option value="100">100</option>
              </select>
            ) : (
              <input
                aria-label={labels[key]}
                data-testid={`common-settings-${key}-input`}
                min="1"
                type="number"
                value={form[key]}
                onChange={(event) => update(key, event.target.value)}
              />
            )}
            {fieldError === key && (
              <span className="field-error" role="alert">
                {form[key].trim() ? message : `${labels[key]}은 필수입니다.`}
              </span>
            )}
          </label>
        ))}
        <div className="form-actions">
          <button
            className="text-action"
            data-testid="common-settings-cancel-button"
            onClick={() => {
              setForm(lastLoaded);
              setFieldError(null);
            }}
            type="button"
          >
            취소
          </button>
          <button
            className="primary-action"
            data-testid="common-settings-save-button"
            onClick={openConfirmation}
            type="button"
          >
            저장
          </button>
        </div>
      </section>
      {state === "loading" && (
        <p className="loading-message">
          공통 환경설정 값을 불러오거나 저장 후 재조회하고 있습니다.
        </p>
      )}
      {state === "empty" && <p className="empty-message">설정값이 없습니다.</p>}
      {state === "error" && !fieldError && (
        <p className="error-message">
          {message}{" "}
          <button
            className="text-action"
            data-testid="common-settings-retry-button"
            onClick={() => void loadSettings()}
            type="button"
          >
            다시 시도
          </button>
        </p>
      )}
      {state === "success" && message && (
        <p className="success-message" role="status">
          {message}
        </p>
      )}
      {showConfirmation && (
        <div className="modal-backdrop" role="presentation">
          <section
            aria-labelledby="common-settings-confirm-title"
            aria-modal="true"
            className="settings-confirmation"
            data-testid="common-settings-confirm-dialog"
            role="dialog"
          >
            <div className="section-title">
              <h2 id="common-settings-confirm-title">
                공통 환경설정 저장 확인
              </h2>
              <button
                className="text-action"
                data-testid="common-settings-close-button"
                onClick={() => setShowConfirmation(false)}
                type="button"
              >
                닫기
              </button>
            </div>
            {Object.entries(labels).map(([key, label]) => (
              <p key={key}>
                {label}: {form[key as keyof CommonSettings]}
              </p>
            ))}
            <p>세션 유휴시간은 새로 만들어지는 세션부터 적용됩니다.</p>
            <p>이미 열린 세션의 유휴시간은 변경하지 않습니다.</p>
            <div className="form-actions">
              <button
                className="text-action"
                data-testid="common-settings-confirm-cancel-button"
                onClick={() => setShowConfirmation(false)}
                type="button"
              >
                취소
              </button>
              <button
                className="primary-action"
                data-testid="common-settings-confirm-save-button"
                onClick={() => void save()}
                type="button"
              >
                확인 저장
              </button>
            </div>
          </section>
        </div>
      )}
    </section>
  );
}
