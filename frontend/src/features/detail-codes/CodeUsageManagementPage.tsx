import { useState } from "react";
import { ApiRequestError, apiRequest } from "../../shared/api/client";

type UsageDetailCode = {
  detailCodeId: string;
  codeValue: string;
  codeName: string;
  parentDetailCodeId: string | null;
  displayOrder: number;
  additionalAttributes: Record<string, unknown> | null;
  useYn: "Y" | "N";
  applicationStartDate: string;
  applicationEndDate: string | null;
};

type UsageForm = {
  useYn: "Y" | "N";
  applicationStartDate: string;
  applicationEndDate: string;
};

function detailCodePath(
  groupId: string,
  includeEnded: boolean,
): `/api/${string}` {
  const query = includeEnded ? "?includeEnded=true" : "";
  return `/api/code-groups/${encodeURIComponent(groupId)}/detail-codes${query}`;
}

function toUsageForm(detailCode: UsageDetailCode): UsageForm {
  return {
    useYn: detailCode.useYn,
    applicationStartDate: detailCode.applicationStartDate,
    applicationEndDate: detailCode.applicationEndDate ?? "",
  };
}

export function CodeUsageManagementPage() {
  const [groupId, setGroupId] = useState("");
  const [includeEnded, setIncludeEnded] = useState(false);
  const [detailCodes, setDetailCodes] = useState<UsageDetailCode[]>([]);
  const [selectedDetailCode, setSelectedDetailCode] =
    useState<UsageDetailCode | null>(null);
  const [form, setForm] = useState<UsageForm | null>(null);
  const [state, setState] = useState<
    "idle" | "loading" | "empty" | "error" | "permission" | "success"
  >("idle");
  const [message, setMessage] = useState("");
  const [fieldError, setFieldError] = useState("");

  const isPermissionError = (error: unknown) =>
    error instanceof ApiRequestError &&
    (error.status === 401 || error.status === 403);

  const loadDetailCodes = async (showSuccess = false) => {
    if (!groupId.trim()) {
      setState("error");
      setMessage("코드그룹 ID를 입력하세요.");
      return;
    }
    setState("loading");
    setFieldError("");
    try {
      const response = await apiRequest<UsageDetailCode[]>(
        detailCodePath(groupId.trim(), includeEnded),
      );
      setDetailCodes(response.data);
      setState(response.data.length === 0 ? "empty" : "success");
      setMessage(
        showSuccess
          ? "저장 후 코드 사용여부와 적용기간을 다시 조회했습니다."
          : "",
      );
    } catch (error) {
      const permissionDenied = isPermissionError(error);
      setState(permissionDenied ? "permission" : "error");
      setMessage(
        permissionDenied
          ? "권한이 없습니다."
          : "코드 사용 관리 조회에 실패했습니다.",
      );
    }
  };

  const openEdit = (detailCode: UsageDetailCode) => {
    setSelectedDetailCode(detailCode);
    setForm(toUsageForm(detailCode));
    setFieldError("");
    setMessage("");
  };

  const cancelEdit = () => {
    setSelectedDetailCode(null);
    setForm(null);
    setFieldError("");
  };

  const saveUsage = async () => {
    if (!selectedDetailCode || !form) return;
    if (!form.applicationStartDate) {
      setFieldError("적용 시작일은 필수입니다.");
      return;
    }
    if (
      form.applicationEndDate &&
      form.applicationStartDate > form.applicationEndDate
    ) {
      setFieldError("적용 종료일은 시작일보다 빠를 수 없습니다.");
      return;
    }
    if (!window.confirm("코드 사용여부와 적용기간을 저장하시겠습니까?")) return;

    setState("loading");
    setMessage("");
    setFieldError("");
    try {
      await apiRequest<null>(detailCodePath(groupId.trim(), false), {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          codeValue: selectedDetailCode.codeValue,
          codeName: selectedDetailCode.codeName,
          parentDetailCodeId: selectedDetailCode.parentDetailCodeId,
          displayOrder: selectedDetailCode.displayOrder,
          additionalAttributes: selectedDetailCode.additionalAttributes,
          useYn: form.useYn,
          applicationStartDate: form.applicationStartDate,
          applicationEndDate: form.applicationEndDate || null,
        }),
      });
      cancelEdit();
      await loadDetailCodes(true);
    } catch (error) {
      const permissionDenied = isPermissionError(error);
      setState(permissionDenied ? "permission" : "error");
      if (
        error instanceof ApiRequestError &&
        error.field === "applicationStartDate"
      ) {
        setFieldError(error.message);
      } else if (
        error instanceof ApiRequestError &&
        error.field === "applicationEndDate"
      ) {
        setFieldError(error.message);
      }
      setMessage(
        permissionDenied
          ? "권한이 없습니다."
          : "코드 사용여부와 적용기간을 저장하지 못했습니다.",
      );
    }
  };

  if (state === "permission") {
    return (
      <section
        className="code-usage-management code-usage-state"
        data-testid="code-usage-management-page"
      >
        <h1>코드 사용 관리</h1>
        <p>권한이 없습니다.</p>
      </section>
    );
  }

  return (
    <section
      className="code-usage-management"
      data-testid="code-usage-management-page"
      aria-labelledby="code-usage-management-title"
    >
      <p className="breadcrumb">
        시스템 관리 &gt; 공통코드 관리 &gt; 코드 사용 관리
      </p>
      <h1 id="code-usage-management-title">코드 사용 관리</h1>
      <p className="code-usage-description">
        상세코드의 사용여부와 적용기간은 서버 시각의 일자 기준으로 판정됩니다.
      </p>
      <section className="code-usage-search" aria-label="코드 사용 관리 조회">
        <label>
          코드그룹 ID
          <input
            aria-label="코드그룹 ID"
            data-testid="code-usage-group-id-input"
            value={groupId}
            onChange={(event) => setGroupId(event.target.value)}
          />
        </label>
        <label className="code-usage-checkbox">
          <input
            aria-label="종료·중지 코드 포함"
            checked={includeEnded}
            data-testid="code-usage-include-ended-input"
            onChange={(event) => setIncludeEnded(event.target.checked)}
            type="checkbox"
          />
          종료·중지 코드 포함
        </label>
        <div className="form-actions">
          <button
            className="primary-action"
            data-testid="code-usage-search-button"
            onClick={() => void loadDetailCodes()}
            type="button"
          >
            조회
          </button>
        </div>
      </section>

      {state === "loading" && (
        <p className="loading-message">
          코드 사용 정보를 조회하거나 저장하는 중입니다.
        </p>
      )}
      {state === "empty" && (
        <p className="empty-message">조회 조건에 맞는 상세코드가 없습니다.</p>
      )}
      {state === "error" && (
        <p className="error-message">
          {message}{" "}
          <button
            className="text-action"
            data-testid="code-usage-retry-button"
            onClick={() => void loadDetailCodes()}
            type="button"
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
        <section className="code-usage-results" aria-label="코드 사용 목록">
          <div className="table-scroll">
            <table>
              <thead>
                <tr>
                  <th>코드값</th>
                  <th>코드명</th>
                  <th>사용여부</th>
                  <th>적용 시작일</th>
                  <th>적용 종료일</th>
                  <th>작업</th>
                </tr>
              </thead>
              <tbody>
                {detailCodes.map((detailCode) => (
                  <tr
                    data-testid={`code-usage-row-${detailCode.detailCodeId}`}
                    key={detailCode.detailCodeId}
                  >
                    <td>{detailCode.codeValue}</td>
                    <td>{detailCode.codeName}</td>
                    <td>{detailCode.useYn}</td>
                    <td>{detailCode.applicationStartDate}</td>
                    <td>{detailCode.applicationEndDate ?? "-"}</td>
                    <td>
                      <button
                        className="text-action"
                        data-testid={`code-usage-edit-${detailCode.detailCodeId}`}
                        onClick={() => openEdit(detailCode)}
                        type="button"
                      >
                        사용기간 수정
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </section>
      )}

      {form && selectedDetailCode && (
        <div className="modal-backdrop" role="presentation">
          <section
            aria-labelledby="code-usage-dialog-title"
            aria-modal="true"
            className="code-usage-modal"
            data-testid="code-usage-edit-dialog"
            role="dialog"
          >
            <div className="section-title">
              <h2 id="code-usage-dialog-title">상세코드 사용기간 수정</h2>
              <button
                className="text-action"
                data-testid="code-usage-close-button"
                onClick={cancelEdit}
                type="button"
              >
                닫기
              </button>
            </div>
            <p>
              <strong>{selectedDetailCode.codeValue}</strong> ·{" "}
              {selectedDetailCode.codeName}
            </p>
            <div className="code-usage-form">
              <label>
                사용여부
                <select
                  aria-label="사용여부"
                  data-testid="code-usage-use-yn-select"
                  onChange={(event) =>
                    setForm({ ...form, useYn: event.target.value as "Y" | "N" })
                  }
                  value={form.useYn}
                >
                  <option value="Y">사용</option>
                  <option value="N">중지</option>
                </select>
              </label>
              <label>
                적용 시작일 <span aria-hidden="true">*</span>
                <input
                  aria-label="적용 시작일"
                  data-testid="code-usage-start-input"
                  onChange={(event) =>
                    setForm({
                      ...form,
                      applicationStartDate: event.target.value,
                    })
                  }
                  required
                  type="date"
                  value={form.applicationStartDate}
                />
              </label>
              <label>
                적용 종료일
                <input
                  aria-label="적용 종료일"
                  data-testid="code-usage-end-input"
                  onChange={(event) =>
                    setForm({ ...form, applicationEndDate: event.target.value })
                  }
                  type="date"
                  value={form.applicationEndDate}
                />
              </label>
            </div>
            {fieldError && (
              <p className="error-message" role="alert">
                {fieldError}
              </p>
            )}
            <div className="form-actions">
              <button
                className="text-action"
                data-testid="code-usage-cancel-button"
                onClick={cancelEdit}
                type="button"
              >
                취소
              </button>
              <button
                className="primary-action"
                data-testid="code-usage-save-button"
                onClick={() => void saveUsage()}
                type="button"
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
