import { useState } from "react";

import { ApiRequestError, apiRequest } from "../../shared/api/client";

type CodeGroup = {
  groupId: string;
  groupName: string;
  description: string | null;
  managementDepartment: string | null;
  useYn: string;
};

type SearchForm = { groupId: string; useYn: string };
type CodeGroupForm = {
  groupId: string;
  groupName: string;
  description: string;
  managementDepartment: string;
  useYn: string;
  reason: string;
};

const emptySearch: SearchForm = { groupId: "", useYn: "" };
const emptyForm: CodeGroupForm = {
  groupId: "",
  groupName: "",
  description: "",
  managementDepartment: "",
  useYn: "Y",
  reason: "",
};

function codeGroupQueryPath(search: SearchForm): `/api/${string}` {
  const params = new URLSearchParams();
  if (search.groupId) params.set("groupId", search.groupId);
  if (search.useYn) params.set("useYn", search.useYn);
  const query = params.toString();
  return `/api/code-groups${query ? `?${query}` : ""}`;
}

function toCodeGroupForm(codeGroup: CodeGroup): CodeGroupForm {
  return {
    groupId: codeGroup.groupId,
    groupName: codeGroup.groupName,
    description: codeGroup.description ?? "",
    managementDepartment: codeGroup.managementDepartment ?? "",
    useYn: codeGroup.useYn,
    reason: "",
  };
}

export function CodeGroupManagementPage() {
  const [search, setSearch] = useState<SearchForm>(emptySearch);
  const [codeGroups, setCodeGroups] = useState<CodeGroup[]>([]);
  const [selectedCodeGroup, setSelectedCodeGroup] = useState<CodeGroup | null>(
    null,
  );
  const [form, setForm] = useState<CodeGroupForm>(emptyForm);
  const [modalOpen, setModalOpen] = useState(false);
  const [state, setState] = useState<
    "idle" | "loading" | "empty" | "error" | "permission" | "success"
  >("idle");
  const [message, setMessage] = useState("");

  const isPermissionError = (error: unknown) =>
    error instanceof ApiRequestError &&
    (error.status === 401 || error.status === 403);

  const loadCodeGroups = async (
    showSuccess = false,
    selectedGroupId?: string,
  ) => {
    setState("loading");
    try {
      const response = await apiRequest<CodeGroup[]>(
        codeGroupQueryPath(search),
      );
      setCodeGroups(response.data);
      const nextSelected = selectedGroupId
        ? (response.data.find(
            (codeGroup) => codeGroup.groupId === selectedGroupId,
          ) ?? null)
        : selectedCodeGroup;
      setSelectedCodeGroup(nextSelected);
      setState(response.data.length === 0 ? "empty" : "success");
      setMessage(showSuccess ? "저장 후 코드그룹을 다시 조회했습니다." : "");
    } catch (error) {
      const permissionDenied = isPermissionError(error);
      setState(permissionDenied ? "permission" : "error");
      setMessage(
        permissionDenied
          ? "권한이 없습니다."
          : "코드그룹 조회 또는 저장에 실패했습니다.",
      );
    }
  };

  const openCreate = () => {
    setForm(emptyForm);
    setMessage("");
    setModalOpen(true);
  };

  const openEdit = (codeGroup: CodeGroup) => {
    setSelectedCodeGroup(codeGroup);
    setForm(toCodeGroupForm(codeGroup));
    setMessage("");
    setModalOpen(true);
  };

  const saveCodeGroup = async () => {
    setState("loading");
    setMessage("");
    try {
      await apiRequest<null>("/api/code-groups", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          groupId: form.groupId,
          groupName: form.groupName,
          description: form.description || null,
          managementDepartment: form.managementDepartment || null,
          useYn: form.useYn,
          ...(form.reason ? { reason: form.reason } : {}),
        }),
      });
      const selectedGroupId = form.groupId;
      setModalOpen(false);
      await loadCodeGroups(true, selectedGroupId);
    } catch (error) {
      const permissionDenied = isPermissionError(error);
      setState(permissionDenied ? "permission" : "error");
      setMessage(
        permissionDenied
          ? "권한이 없습니다."
          : "코드그룹을 저장하지 못했습니다.",
      );
    }
  };

  if (state === "permission") {
    return (
      <section
        className="code-group-management code-group-state"
        aria-live="polite"
      >
        <h1>코드그룹 관리</h1>
        <p>권한이 없습니다.</p>
      </section>
    );
  }

  return (
    <section
      className="code-group-management"
      aria-labelledby="code-group-management-title"
    >
      <p className="breadcrumb">
        시스템 관리 &gt; 공통코드 관리 &gt; 코드그룹 관리
      </p>
      <h1 id="code-group-management-title">코드그룹 관리</h1>
      <section className="code-group-search" aria-label="코드그룹 조회">
        <label>
          그룹ID
          <input
            aria-label="그룹ID"
            value={search.groupId}
            onChange={(event) =>
              setSearch({ ...search, groupId: event.target.value })
            }
          />
        </label>
        <label>
          사용여부
          <select
            aria-label="사용여부"
            value={search.useYn}
            onChange={(event) =>
              setSearch({ ...search, useYn: event.target.value })
            }
          >
            <option value="">전체</option>
            <option value="Y">Y</option>
            <option value="N">N</option>
          </select>
        </label>
        <div className="form-actions">
          <button
            type="button"
            className="text-action"
            onClick={() => setSearch(emptySearch)}
          >
            초기화
          </button>
          <button
            type="button"
            className="primary-action"
            onClick={() => void loadCodeGroups()}
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
          코드그룹을 조회하거나 저장하는 중입니다.
        </p>
      )}
      {state === "empty" && (
        <p className="empty-message">조건에 맞는 코드그룹이 없습니다.</p>
      )}
      {state === "error" && (
        <p className="error-message">
          {message}{" "}
          <button
            type="button"
            className="text-action"
            onClick={() => void loadCodeGroups()}
          >
            다시 시도
          </button>
        </p>
      )}

      {state === "success" && (
        <>
          <section className="code-group-results" aria-label="코드그룹 목록">
            <div className="section-title">
              <h2>코드그룹 목록</h2>
              <button
                type="button"
                className="primary-action"
                onClick={openCreate}
              >
                코드그룹 등록
              </button>
            </div>
            <div className="table-scroll">
              <table>
                <thead>
                  <tr>
                    <th>그룹ID</th>
                    <th>명칭</th>
                    <th>설명</th>
                    <th>관리부서</th>
                    <th>사용여부</th>
                    <th>작업</th>
                  </tr>
                </thead>
                <tbody>
                  {codeGroups.map((codeGroup) => (
                    <tr
                      key={codeGroup.groupId}
                      className={
                        selectedCodeGroup?.groupId === codeGroup.groupId
                          ? "selected-row"
                          : ""
                      }
                      onClick={() => setSelectedCodeGroup(codeGroup)}
                    >
                      <td>{codeGroup.groupId}</td>
                      <td>{codeGroup.groupName}</td>
                      <td>{codeGroup.description ?? "-"}</td>
                      <td>{codeGroup.managementDepartment ?? "-"}</td>
                      <td>{codeGroup.useYn}</td>
                      <td>
                        <button
                          type="button"
                          className="text-action"
                          onClick={(event) => {
                            event.stopPropagation();
                            openEdit(codeGroup);
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
          {selectedCodeGroup && (
            <section
              className="code-group-detail"
              aria-label="선택 코드그룹 상세"
            >
              <div className="section-title">
                <h2>선택 코드그룹 상세</h2>
                <div>
                  <button
                    type="button"
                    className="text-action"
                    onClick={() => setSelectedCodeGroup(null)}
                  >
                    선택 해제
                  </button>
                  <a
                    className="row-link"
                    href={`/system/common-codes/detail-codes?groupId=${encodeURIComponent(selectedCodeGroup.groupId)}`}
                  >
                    상세코드 목록
                  </a>
                </div>
              </div>
              <dl className="detail-grid">
                <div>
                  <dt>그룹ID</dt>
                  <dd>{selectedCodeGroup.groupId}</dd>
                </div>
                <div>
                  <dt>명칭</dt>
                  <dd>{selectedCodeGroup.groupName}</dd>
                </div>
                <div>
                  <dt>설명</dt>
                  <dd>{selectedCodeGroup.description ?? "-"}</dd>
                </div>
                <div>
                  <dt>관리부서</dt>
                  <dd>{selectedCodeGroup.managementDepartment ?? "-"}</dd>
                </div>
                <div>
                  <dt>사용여부</dt>
                  <dd>{selectedCodeGroup.useYn}</dd>
                </div>
              </dl>
            </section>
          )}
        </>
      )}

      {modalOpen && (
        <div className="modal-backdrop" role="presentation">
          <section
            className="code-group-modal"
            role="dialog"
            aria-modal="true"
            aria-labelledby="code-group-modal-title"
          >
            <div className="section-title">
              <h2 id="code-group-modal-title">코드그룹 등록·수정</h2>
              <button
                type="button"
                className="text-action"
                onClick={() => setModalOpen(false)}
              >
                닫기
              </button>
            </div>
            <div className="code-group-form">
              <label>
                그룹ID
                <input
                  aria-label="그룹ID"
                  value={form.groupId}
                  onChange={(event) =>
                    setForm({ ...form, groupId: event.target.value })
                  }
                />
              </label>
              <label>
                명칭
                <input
                  aria-label="명칭"
                  value={form.groupName}
                  onChange={(event) =>
                    setForm({ ...form, groupName: event.target.value })
                  }
                />
              </label>
              <label>
                설명
                <textarea
                  aria-label="설명"
                  value={form.description}
                  onChange={(event) =>
                    setForm({ ...form, description: event.target.value })
                  }
                />
              </label>
              <label>
                관리부서
                <input
                  aria-label="관리부서"
                  value={form.managementDepartment}
                  onChange={(event) =>
                    setForm({
                      ...form,
                      managementDepartment: event.target.value,
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
            <p>그룹ID와 명칭은 서버 필수 검증 대상입니다.</p>
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
                onClick={() => void saveCodeGroup()}
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
