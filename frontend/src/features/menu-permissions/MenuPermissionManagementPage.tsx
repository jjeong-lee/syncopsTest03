import { useState } from "react";
import { ApiRequestError, apiRequest } from "../../shared/api/client";

export type MenuPermissionSummary = {
  menuPermissionId: string;
  subjectType: string;
  subjectId: string;
  menuId: string;
  majorMenuName: string | null;
  middleMenuName: string | null;
  screenName: string | null;
  accessAllowed: string;
};

type SearchForm = {
  subjectType: string;
  subjectId: string;
  menuId: string;
};

type PermissionForm = SearchForm & {
  accessAllowed: string;
  reason: string;
};

const emptySearch: SearchForm = { subjectType: "", subjectId: "", menuId: "" };
const emptyPermission: PermissionForm = {
  ...emptySearch,
  accessAllowed: "Y",
  reason: "",
};

function queryPath(search: SearchForm): `/api/${string}` {
  const params = new URLSearchParams();
  if (search.subjectType) params.set("subjectType", search.subjectType);
  if (search.subjectId) params.set("subjectId", search.subjectId);
  if (search.menuId) params.set("menuId", search.menuId);
  const query = params.toString();
  return `/api/menu-permissions${query ? `?${query}` : ""}`;
}

export function MenuPermissionManagementPage() {
  const [search, setSearch] = useState<SearchForm>(emptySearch);
  const [permissions, setPermissions] = useState<MenuPermissionSummary[]>([]);
  const [form, setForm] = useState<PermissionForm>(emptyPermission);
  const [modalOpen, setModalOpen] = useState(false);
  const [state, setState] = useState<
    "idle" | "loading" | "empty" | "error" | "permission" | "success"
  >("idle");
  const [message, setMessage] = useState("");

  const isPermissionError = (error: unknown) =>
    error instanceof ApiRequestError &&
    (error.status === 401 || error.status === 403);

  const loadPermissions = async (showSuccess = false) => {
    setState("loading");
    setMessage("");
    try {
      const response = await apiRequest<MenuPermissionSummary[]>(
        queryPath(search),
      );
      setPermissions(response.data);
      setState(response.data.length === 0 ? "empty" : "success");
      if (showSuccess)
        setMessage("저장 후 메뉴 권한 목록을 다시 조회했습니다.");
    } catch (error) {
      const permissionDenied = isPermissionError(error);
      setState(permissionDenied ? "permission" : "error");
      setMessage(
        permissionDenied
          ? "권한이 없습니다."
          : "메뉴 권한을 조회하지 못했습니다.",
      );
    }
  };

  const openEdit = (permission: MenuPermissionSummary) => {
    setForm({
      subjectType: permission.subjectType,
      subjectId: permission.subjectId,
      menuId: permission.menuId,
      accessAllowed: permission.accessAllowed,
      reason: "",
    });
    setModalOpen(true);
    setMessage("");
  };

  const savePermission = async () => {
    setState("loading");
    setMessage("");
    try {
      await apiRequest<null>("/api/menu-permissions", {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          subjectType: form.subjectType,
          subjectId: form.subjectId,
          menuId: form.menuId,
          accessAllowed: form.accessAllowed,
          ...(form.reason ? { reason: form.reason } : {}),
        }),
      });
      setModalOpen(false);
      await loadPermissions(true);
    } catch (error) {
      const permissionDenied = isPermissionError(error);
      setState(permissionDenied ? "permission" : "error");
      setMessage(
        permissionDenied
          ? "권한이 없습니다."
          : "메뉴 권한을 저장하지 못했습니다.",
      );
    }
  };

  if (state === "permission") {
    return (
      <section
        className="menu-permission-management menu-permission-state"
        aria-live="polite"
      >
        <h1>메뉴 권한 관리</h1>
        <p>권한이 없습니다.</p>
      </section>
    );
  }

  return (
    <section
      className="menu-permission-management"
      aria-labelledby="menu-permission-management-title"
    >
      <p className="breadcrumb">
        시스템 관리 &gt; 역할·권한 관리 &gt; 메뉴 권한 관리
      </p>
      <h1 id="menu-permission-management-title">메뉴 권한 관리</h1>
      <section className="menu-permission-search" aria-label="메뉴 권한 조회">
        <label>
          대상 구분
          <select
            aria-label="대상 구분"
            value={search.subjectType}
            onChange={(event) =>
              setSearch({ ...search, subjectType: event.target.value })
            }
          >
            <option value="">전체</option>
            <option value="ROLE">역할</option>
            <option value="ORGANIZATION">조직</option>
            <option value="USER">사용자</option>
          </select>
        </label>
        <label>
          대상 ID
          <input
            aria-label="대상 ID"
            value={search.subjectId}
            onChange={(event) =>
              setSearch({ ...search, subjectId: event.target.value })
            }
          />
        </label>
        <label>
          메뉴 ID
          <input
            aria-label="메뉴 ID"
            value={search.menuId}
            onChange={(event) =>
              setSearch({ ...search, menuId: event.target.value })
            }
          />
        </label>
        <button
          type="button"
          className="primary-action"
          onClick={() => void loadPermissions()}
        >
          조회
        </button>
      </section>

      {message && state === "success" && (
        <p className="success-message" role="status">
          {message}
        </p>
      )}
      <section className="menu-permission-results" aria-live="polite">
        <div className="section-title">
          <h2>메뉴 접근권한</h2>
          {state === "success" && <span>{permissions.length}건</span>}
        </div>
        {state === "loading" && (
          <p className="loading-message">메뉴 권한을 조회하는 중입니다.</p>
        )}
        {state === "empty" && (
          <p className="empty-message">조건에 맞는 메뉴 권한이 없습니다.</p>
        )}
        {state === "error" && (
          <p className="error-message">
            {message}{" "}
            <button
              type="button"
              className="text-action"
              onClick={() => void loadPermissions()}
            >
              다시 시도
            </button>
          </p>
        )}
        {state === "success" && (
          <div className="table-scroll">
            <table>
              <thead>
                <tr>
                  <th>대상 구분</th>
                  <th>대상 ID</th>
                  <th>대메뉴</th>
                  <th>중메뉴</th>
                  <th>화면</th>
                  <th>접근 허용</th>
                  <th>작업</th>
                </tr>
              </thead>
              <tbody>
                {permissions.map((permission) => (
                  <tr key={permission.menuPermissionId}>
                    <td>{permission.subjectType}</td>
                    <td>{permission.subjectId}</td>
                    <td>{permission.majorMenuName ?? "-"}</td>
                    <td>{permission.middleMenuName ?? "-"}</td>
                    <td>{permission.screenName ?? "-"}</td>
                    <td>{permission.accessAllowed}</td>
                    <td>
                      <button
                        type="button"
                        className="text-action"
                        onClick={() => openEdit(permission)}
                      >
                        권한 설정
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>

      {modalOpen && (
        <div className="modal-backdrop" role="presentation">
          <section
            className="user-role-modal"
            role="dialog"
            aria-modal="true"
            aria-labelledby="menu-permission-modal-title"
          >
            <div className="modal-heading">
              <h2 id="menu-permission-modal-title">메뉴 접근권한 설정</h2>
              <button
                type="button"
                className="text-action"
                onClick={() => setModalOpen(false)}
              >
                닫기
              </button>
            </div>
            <label>
              대상 구분
              <select
                aria-label="대상 구분"
                value={form.subjectType}
                onChange={(event) =>
                  setForm({ ...form, subjectType: event.target.value })
                }
              >
                <option value="ROLE">역할</option>
                <option value="ORGANIZATION">조직</option>
                <option value="USER">사용자</option>
              </select>
            </label>
            <label>
              대상 ID
              <input
                aria-label="대상 ID"
                value={form.subjectId}
                onChange={(event) =>
                  setForm({ ...form, subjectId: event.target.value })
                }
              />
            </label>
            <label>
              메뉴 ID
              <input
                aria-label="메뉴 ID"
                value={form.menuId}
                onChange={(event) =>
                  setForm({ ...form, menuId: event.target.value })
                }
              />
            </label>
            <label>
              접근 허용 여부
              <select
                aria-label="접근 허용 여부"
                value={form.accessAllowed}
                onChange={(event) =>
                  setForm({ ...form, accessAllowed: event.target.value })
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
            {state === "error" && <p className="error-message">{message}</p>}
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
                disabled={state === "loading"}
                onClick={() => void savePermission()}
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
