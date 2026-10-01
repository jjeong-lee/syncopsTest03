import { useEffect, useState } from "react";
import { ApiRequestError, apiRequest } from "../../shared/api/client";

type ReferenceYearSettings = {
  currentEvaluationYear: number;
  defaultSearchYear: number;
  targetYear: number;
  referenceDataCopyYn: "Y" | "N";
  initializationYn: "Y" | "N";
};

type FormValues = Record<keyof ReferenceYearSettings, string>;

const emptyForm: FormValues = {
  currentEvaluationYear: "",
  defaultSearchYear: "",
  targetYear: "",
  referenceDataCopyYn: "Y",
  initializationYn: "N",
};

const labels: Record<keyof ReferenceYearSettings, string> = {
  currentEvaluationYear: "현재 평가연도",
  defaultSearchYear: "기본 조회연도",
  targetYear: "대상 연도",
  referenceDataCopyYn: "기준정보 복사 여부",
  initializationYn: "초기화 여부",
};

function toFormValues(settings: ReferenceYearSettings | null): FormValues {
  if (!settings) return emptyForm;
  return Object.fromEntries(
    Object.entries(settings).map(([key, value]) => [key, String(value)]),
  ) as FormValues;
}

export function ReferenceYearManagementPage() {
  const [form, setForm] = useState<FormValues>(emptyForm);
  const [lastLoaded, setLastLoaded] = useState<FormValues>(emptyForm);
  const [state, setState] = useState<
    "loading" | "empty" | "error" | "permission" | "success"
  >("loading");
  const [message, setMessage] = useState("");
  const [fieldError, setFieldError] = useState<
    keyof ReferenceYearSettings | null
  >(null);
  const [showConfirmation, setShowConfirmation] = useState(false);

  const loadSettings = async (showSuccess = false) => {
    setState("loading");
    setMessage("");
    try {
      const response = await apiRequest<ReferenceYearSettings | null>(
        "/api/settings/reference-years",
      );
      const values = toFormValues(response.data);
      setForm(values);
      setLastLoaded(values);
      setState(response.data ? "success" : "empty");
      if (showSuccess)
        setMessage("저장 후 기준연도 설정값을 다시 조회했습니다.");
    } catch (error) {
      const permissionDenied =
        error instanceof ApiRequestError &&
        (error.status === 401 || error.status === 403);
      setState(permissionDenied ? "permission" : "error");
      setMessage(
        permissionDenied
          ? "권한이 없습니다."
          : "기준연도 설정 조회에 실패했습니다.",
      );
    }
  };

  useEffect(() => {
    void loadSettings();
  }, []);

  const update = (key: keyof ReferenceYearSettings, value: string) => {
    setForm((current) => ({ ...current, [key]: value }));
    if (fieldError === key) setFieldError(null);
  };

  const openConfirmation = () => {
    const missing = (
      Object.keys(labels) as (keyof ReferenceYearSettings)[]
    ).find((key) => !form[key].trim());
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
      await apiRequest<null>("/api/settings/reference-years", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          currentEvaluationYear: Number(form.currentEvaluationYear),
          defaultSearchYear: Number(form.defaultSearchYear),
          targetYear: Number(form.targetYear),
          referenceDataCopyYn: form.referenceDataCopyYn,
          initializationYn: form.initializationYn,
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
          ? (error.field as keyof ReferenceYearSettings | undefined)
          : undefined;
      setFieldError(key && key in labels ? key : null);
      setState("error");
      setMessage(
        error instanceof ApiRequestError
          ? error.message
          : "기준연도 설정을 저장하지 못했습니다.",
      );
    }
  };

  if (state === "permission") {
    return (
      <section
        className="reference-year-management settings-state"
        data-testid="reference-year-management-page"
      >
        <h1>기준연도 관리</h1>
        <p>권한이 없습니다.</p>
      </section>
    );
  }

  return (
    <section
      className="reference-year-management"
      data-testid="reference-year-management-page"
      aria-labelledby="reference-year-management-title"
    >
      <p className="breadcrumb">
        시스템 관리 &gt; 시스템 환경설정 &gt; 기준연도 관리
      </p>
      <h1 id="reference-year-management-title">기준연도 관리</h1>
      <p className="readonly-note">
        기본 조회연도는 사용자 화면 진입 시 기본값으로 제공되며, 사용자는 다른
        연도를 조회할 수 있습니다. 이 화면은 실제 기준정보 복사·초기화나 기존
        연도 평가자료 변경을 수행하지 않습니다.
      </p>
      <section className="settings-form" aria-label="기준연도 설정 입력">
        {(Object.keys(labels) as (keyof ReferenceYearSettings)[]).map((key) => (
          <label key={key}>
            {labels[key]} <span aria-hidden="true">*</span>
            {key === "referenceDataCopyYn" || key === "initializationYn" ? (
              <select
                aria-label={labels[key]}
                data-testid={`reference-year-${key}-select`}
                value={form[key]}
                onChange={(event) => update(key, event.target.value)}
              >
                <option value="Y">예</option>
                <option value="N">아니오</option>
              </select>
            ) : (
              <input
                aria-label={labels[key]}
                data-testid={`reference-year-${key}-input`}
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
            data-testid="reference-year-cancel-button"
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
            data-testid="reference-year-save-button"
            onClick={openConfirmation}
            type="button"
          >
            저장
          </button>
        </div>
      </section>
      {state === "loading" && (
        <p className="loading-message">
          기준연도 설정을 불러오거나 저장 후 재조회하고 있습니다.
        </p>
      )}
      {state === "empty" && <p className="empty-message">설정값이 없습니다.</p>}
      {state === "error" && !fieldError && (
        <p className="error-message">
          {message}{" "}
          <button
            className="text-action"
            data-testid="reference-year-retry-button"
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
            aria-labelledby="reference-year-confirm-title"
            aria-modal="true"
            className="settings-confirmation"
            data-testid="reference-year-confirm-dialog"
            role="dialog"
          >
            <div className="section-title">
              <h2 id="reference-year-confirm-title">기준연도 설정 저장 확인</h2>
              <button
                className="text-action"
                data-testid="reference-year-close-button"
                onClick={() => setShowConfirmation(false)}
                type="button"
              >
                닫기
              </button>
            </div>
            {Object.entries(labels).map(([key, label]) => (
              <p key={key}>
                {label}: {form[key as keyof ReferenceYearSettings]}
              </p>
            ))}
            <p>이 저장은 대상 연도의 설정값만 기록합니다.</p>
            <p>
              실제 복사·초기화나 기존 연도 평가자료 변경은 수행하지 않습니다.
            </p>
            <div className="form-actions">
              <button
                className="text-action"
                data-testid="reference-year-confirm-cancel-button"
                onClick={() => setShowConfirmation(false)}
                type="button"
              >
                취소
              </button>
              <button
                className="primary-action"
                data-testid="reference-year-confirm-save-button"
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
