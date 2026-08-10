import { useState } from "react";
import { ApiRequestError, apiRequest } from "../../shared/api/client";

type DetailCode = {
  detailCodeId: string;
  codeValue: string;
  codeName: string;
  parentDetailCodeId: string | null;
  displayOrder: number;
  additionalAttributes: Record<string, unknown> | null;
  useYn: string;
};

type DetailCodeForm = {
  codeValue: string;
  codeName: string;
  parentDetailCodeId: string;
  displayOrder: string;
  additionalAttributes: string;
  useYn: string;
  reason: string;
};

const emptyForm: DetailCodeForm = {
  codeValue: "",
  codeName: "",
  parentDetailCodeId: "",
  displayOrder: "",
  additionalAttributes: "",
  useYn: "Y",
  reason: "",
};

function groupIdFromLocation(): string | null {
  return new URLSearchParams(window.location.search).get("groupId");
}

function detailCodePath(groupId: string, useYn: string): `/api/${string}` {
  const query = useYn ? `?${new URLSearchParams({ useYn }).toString()}` : "";
  return `/api/code-groups/${encodeURIComponent(groupId)}/detail-codes${query}`;
}

function toForm(detailCode: DetailCode): DetailCodeForm {
  return {
    codeValue: detailCode.codeValue,
    codeName: detailCode.codeName,
    parentDetailCodeId: detailCode.parentDetailCodeId ?? "",
    displayOrder: String(detailCode.displayOrder),
    additionalAttributes: detailCode.additionalAttributes
      ? JSON.stringify(detailCode.additionalAttributes)
      : "",
    useYn: detailCode.useYn,
    reason: "",
  };
}

function formatAttributes(attributes: Record<string, unknown> | null): string {
  return attributes ? JSON.stringify(attributes) : "-";
}

export function DetailCodeManagementPage() {
  const groupId = groupIdFromLocation();
  const [useYn, setUseYn] = useState("");
  const [detailCodes, setDetailCodes] = useState<DetailCode[]>([]);
  const [selectedDetailCode, setSelectedDetailCode] =
    useState<DetailCode | null>(null);
  const [form, setForm] = useState<DetailCodeForm>(emptyForm);
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState(false);
  const [state, setState] = useState<
    "idle" | "loading" | "empty" | "error" | "permission" | "success"
  >(groupId ? "idle" : "error");
  const [message, setMessage] = useState(
    groupId ? "" : "코드그룹을 선택해야 상세코드를 조회할 수 있습니다.",
  );

  const isPermissionError = (error: unknown) =>
    error instanceof ApiRequestError &&
    (error.status === 401 || error.status === 403);

  const loadDetailCodes = async (
    showSuccess = false,
    selectedCodeValue?: string,
  ) => {
    if (!groupId) return;
    setState("loading");
    try {
      const response = await apiRequest<DetailCode[]>(
        detailCodePath(groupId, useYn),
      );
      setDetailCodes(response.data);
      const nextSelected = selectedCodeValue
        ? (response.data.find(
            (detailCode) => detailCode.codeValue === selectedCodeValue,
          ) ?? null)
        : selectedDetailCode;
      setSelectedDetailCode(nextSelected);
      setState(response.data.length === 0 ? "empty" : "success");
      setMessage(
        showSuccess ? "저장 후 상세코드 목록을 다시 조회했습니다." : "",
      );
    } catch (error) {
      const permissionDenied = isPermissionError(error);
      setState(permissionDenied ? "permission" : "error");
      setMessage(
        permissionDenied
          ? "권한이 없습니다."
          : "상세코드 조회 또는 저장에 실패했습니다.",
      );
    }
  };

  const openCreate = () => {
    setForm(emptyForm);
    setEditing(false);
    setMessage("");
    setModalOpen(true);
  };

  const openEdit = (detailCode: DetailCode) => {
    setSelectedDetailCode(detailCode);
    setForm(toForm(detailCode));
    setEditing(true);
    setMessage("");
    setModalOpen(true);
  };

  const saveDetailCode = async () => {
    if (!groupId) return;
    let additionalAttributes: Record<string, unknown> | null = null;
    if (form.additionalAttributes.trim()) {
      try {
        additionalAttributes = JSON.parse(form.additionalAttributes) as Record<
          string,
          unknown
        >;
      } catch {
        setState("error");
        setMessage("추가속성은 JSON 객체 형식으로 입력하세요.");
        return;
      }
    }
    setState("loading");
    setMessage("");
    try {
      await apiRequest<null>(detailCodePath(groupId, ""), {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          codeValue: form.codeValue,
          codeName: form.codeName,
          parentDetailCodeId: form.parentDetailCodeId || null,
          displayOrder: form.displayOrder ? Number(form.displayOrder) : null,
          additionalAttributes,
          useYn: form.useYn,
          ...(form.reason ? { reason: form.reason } : {}),
        }),
      });
      const selectedCodeValue = form.codeValue;
      setModalOpen(false);
      await loadDetailCodes(true, selectedCodeValue);
    } catch (error) {
      const permissionDenied = isPermissionError(error);
      setState(permissionDenied ? "permission" : "error");
      setMessage(
        permissionDenied
          ? "권한이 없습니다."
          : "상세코드를 저장하지 못했습니다.",
      );
    }
  };

  if (!groupId) {
    return (
      <section
        className="detail-code-management detail-code-state"
        aria-live="polite"
      >
        <h1>상세코드 관리</h1>
        <p>{message}</p>
        <a className="row-link" href="/system/common-codes/groups">
          코드그룹 관리로 이동
        </a>
      </section>
    );
  }

  if (state === "permission") {
    return (
      <section
        className="detail-code-management detail-code-state"
        aria-live="polite"
      >
        <h1>상세코드 관리</h1>
        <p>권한이 없습니다.</p>
      </section>
    );
  }

  return (
    <section
      className="detail-code-management"
      aria-labelledby="detail-code-management-title"
    >
      <p className="breadcrumb">
        시스템 관리 &gt; 공통코드 관리 &gt; 상세코드 관리
      </p>
      <h1 id="detail-code-management-title">상세코드 관리</h1>
      <section className="detail-code-context" aria-label="코드그룹 컨텍스트">
        <strong>코드그룹: {groupId}</strong>
        <a className="row-link" href="/system/common-codes/groups">
          코드그룹 관리로 이동
        </a>
      </section>
      <section className="detail-code-search" aria-label="상세코드 조회">
        <label>
          사용여부
          <select
            aria-label="사용여부"
            value={useYn}
            onChange={(event) => setUseYn(event.target.value)}
          >
            <option value="">전체</option>
            <option value="Y">Y</option>
            <option value="N">N</option>
          </select>
        </label>
        <div className="form-actions">
          <button
            type="button"
            className="primary-action"
            onClick={() => void loadDetailCodes()}
          >
            조회
          </button>
        </div>
      </section>

      {message && state === "success" && (
        <p className="success-message" role="status">
          {message}
        </p>
      )}
      {state === "loading" && (
        <p className="loading-message" aria-live="polite">
          상세코드를 조회하거나 저장하는 중입니다.
        </p>
      )}
      {state === "empty" && (
        <p className="empty-message">
          선택한 코드그룹에 조건에 맞는 상세코드가 없습니다.
        </p>
      )}
      {state === "error" && (
        <p className="error-message">
          {message}{" "}
          <button
            type="button"
            className="text-action"
            onClick={() => void loadDetailCodes()}
          >
            다시 시도
          </button>
        </p>
      )}

      {state === "success" && (
        <>
          <section className="detail-code-results" aria-label="상세코드 목록">
            <div className="section-title">
              <h2>상세코드 목록</h2>
              <button
                type="button"
                className="primary-action"
                onClick={openCreate}
              >
                상세코드 등록
              </button>
            </div>
            <div className="table-scroll">
              <table>
                <thead>
                  <tr>
                    <th>코드값</th>
                    <th>코드명</th>
                    <th>상위코드</th>
                    <th>정렬순서</th>
                    <th>추가속성</th>
                    <th>사용여부</th>
                    <th>작업</th>
                  </tr>
                </thead>
                <tbody>
                  {detailCodes.map((detailCode) => (
                    <tr
                      key={detailCode.detailCodeId}
                      className={
                        selectedDetailCode?.detailCodeId ===
                        detailCode.detailCodeId
                          ? "selected-row"
                          : ""
                      }
                      onClick={() => setSelectedDetailCode(detailCode)}
                    >
                      <td>{detailCode.codeValue}</td>
                      <td>{detailCode.codeName}</td>
                      <td>{detailCode.parentDetailCodeId ?? "-"}</td>
                      <td>{detailCode.displayOrder}</td>
                      <td>
                        {formatAttributes(detailCode.additionalAttributes)}
                      </td>
                      <td>{detailCode.useYn}</td>
                      <td>
                        <button
                          type="button"
                          className="text-action"
                          onClick={(event) => {
                            event.stopPropagation();
                            openEdit(detailCode);
                          }}
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
          {selectedDetailCode && (
            <section
              className="detail-code-detail"
              aria-label="선택 상세코드 상세"
            >
              <div className="section-title">
                <h2>선택 상세코드 상세</h2>
                <button
                  type="button"
                  className="text-action"
                  onClick={() => setSelectedDetailCode(null)}
                >
                  선택 해제
                </button>
              </div>
              <dl className="detail-grid">
                <div>
                  <dt>코드값</dt>
                  <dd>{selectedDetailCode.codeValue}</dd>
                </div>
                <div>
                  <dt>코드명</dt>
                  <dd>{selectedDetailCode.codeName}</dd>
                </div>
                <div>
                  <dt>상위코드</dt>
                  <dd>{selectedDetailCode.parentDetailCodeId ?? "-"}</dd>
                </div>
                <div>
                  <dt>정렬순서</dt>
                  <dd>{selectedDetailCode.displayOrder}</dd>
                </div>
                <div>
                  <dt>추가속성</dt>
                  <dd>
                    {formatAttributes(selectedDetailCode.additionalAttributes)}
                  </dd>
                </div>
                <div>
                  <dt>사용여부</dt>
                  <dd>{selectedDetailCode.useYn}</dd>
                </div>
              </dl>
            </section>
          )}
        </>
      )}

      {modalOpen && (
        <div className="modal-backdrop" role="presentation">
          <section
            className="detail-code-modal"
            role="dialog"
            aria-modal="true"
            aria-labelledby="detail-code-modal-title"
          >
            <div className="section-title">
              <h2 id="detail-code-modal-title">상세코드 등록·수정</h2>
              <button
                type="button"
                className="text-action"
                onClick={() => setModalOpen(false)}
              >
                닫기
              </button>
            </div>
            <div className="detail-code-form">
              <label>
                코드값
                <input
                  aria-label="코드값"
                  value={form.codeValue}
                  disabled={editing}
                  onChange={(event) =>
                    setForm({ ...form, codeValue: event.target.value })
                  }
                />
              </label>
              <label>
                코드명
                <input
                  aria-label="코드명"
                  value={form.codeName}
                  onChange={(event) =>
                    setForm({ ...form, codeName: event.target.value })
                  }
                />
              </label>
              <label>
                상위코드
                <input
                  aria-label="상위코드"
                  value={form.parentDetailCodeId}
                  onChange={(event) =>
                    setForm({ ...form, parentDetailCodeId: event.target.value })
                  }
                />
              </label>
              <label>
                정렬순서
                <input
                  aria-label="정렬순서"
                  type="number"
                  value={form.displayOrder}
                  onChange={(event) =>
                    setForm({ ...form, displayOrder: event.target.value })
                  }
                />
              </label>
              <label>
                추가속성
                <textarea
                  aria-label="추가속성"
                  value={form.additionalAttributes}
                  onChange={(event) =>
                    setForm({
                      ...form,
                      additionalAttributes: event.target.value,
                    })
                  }
                />
              </label>
              <label>
                사용여부
                <select
                  aria-label="사용여부"
                  value={form.useYn}
                  onChange={(event) =>
                    setForm({ ...form, useYn: event.target.value })
                  }
                >
                  <option value="Y">Y</option>
                  <option value="N">N</option>
                </select>
              </label>
              <label>
                사유
                <textarea
                  aria-label="사유"
                  value={form.reason}
                  onChange={(event) =>
                    setForm({ ...form, reason: event.target.value })
                  }
                />
              </label>
            </div>
            <p>코드값, 코드명, 정렬순서는 서버 필수 검증 대상입니다.</p>
            <div className="form-actions">
              <button
                type="button"
                className="text-action"
                onClick={() => setModalOpen(false)}
              >
                취소
              </button>
              <button
                type="button"
                className="primary-action"
                onClick={() => void saveDetailCode()}
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
