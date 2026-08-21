import { useState } from "react";
import { ApiRequestError, apiRequest } from "../../shared/api/client";

export type UserRoleSummary = {
  userRoleId: string;
  roleCode: string;
  approvalUserId: string;
  effectiveStartDate: string;
  effectiveEndDate: string | null;
  assignmentType: string;
  status: string;
};

type UserRoleForm = {
  roleCode: string;
  approvalUserId: string;
  effectiveStartDate: string;
  effectiveEndDate: string;
  reason: string;
};

const emptyForm: UserRoleForm = {
  roleCode: "",
  approvalUserId: "",
  effectiveStartDate: "",
  effectiveEndDate: "",
  reason: "",
};

const toForm = (userRole: UserRoleSummary): UserRoleForm => ({
  roleCode: userRole.roleCode,
  approvalUserId: userRole.approvalUserId,
  effectiveStartDate: userRole.effectiveStartDate,
  effectiveEndDate: userRole.effectiveEndDate ?? "",
  reason: "",
});

export function UserRoleManagementPage() {
  const [userId, setUserId] = useState("");
  const [roles, setRoles] = useState<UserRoleSummary[]>([]);
  const [form, setForm] = useState<UserRoleForm>(emptyForm);
  const [editingRole, setEditingRole] = useState<UserRoleSummary | null>(null);
  const [grantOpen, setGrantOpen] = useState(false);
  const [revokeTarget, setRevokeTarget] = useState<UserRoleSummary | null>(
    null,
  );
  const [revokeNotice, setRevokeNotice] = useState("");
  const [state, setState] = useState<
    "idle" | "loading" | "empty" | "error" | "permission" | "success"
  >("idle");
  const [message, setMessage] = useState("");

  const isPermissionError = (error: unknown) =>
    error instanceof ApiRequestError &&
    (error.status === 401 || error.status === 403);

  const loadRoles = async (showSuccess = false) => {
    if (!userId.trim()) {
      setState("error");
      setMessage("조회할 사용자 ID를 입력하세요.");
      return;
    }
    setState("loading");
    setMessage("");
    try {
      const response = await apiRequest<UserRoleSummary[]>(
        `/api/users/${encodeURIComponent(userId)}/roles`,
      );
      setRoles(response.data);
      setState(response.data.length === 0 ? "empty" : "success");
      if (showSuccess)
        setMessage("저장 후 현재 역할 목록을 다시 조회했습니다.");
    } catch (error) {
      const permissionDenied = isPermissionError(error);
      setState(permissionDenied ? "permission" : "error");
      setMessage(
        permissionDenied
          ? "권한이 없습니다."
          : "사용자 역할을 조회하지 못했습니다.",
      );
    }
  };

  const openGrant = (userRole: UserRoleSummary | null) => {
    setEditingRole(userRole);
    setForm(userRole ? toForm(userRole) : emptyForm);
    setGrantOpen(true);
    setMessage("");
  };

  const saveRole = async () => {
    setState("loading");
    setMessage("");
    try {
      const request = {
        roleCode: form.roleCode,
        approvalUserId: form.approvalUserId,
        effectiveStartDate: form.effectiveStartDate,
        ...(form.effectiveEndDate
          ? { effectiveEndDate: form.effectiveEndDate }
          : {}),
        ...(form.reason ? { reason: form.reason } : {}),
      };
      await apiRequest<null>(`/api/users/${encodeURIComponent(userId)}/roles`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(request),
      });
      setGrantOpen(false);
      setEditingRole(null);
      await loadRoles(true);
    } catch (error) {
      const permissionDenied = isPermissionError(error);
      setState(permissionDenied ? "permission" : "error");
      setMessage(
        permissionDenied
          ? "권한이 없습니다."
          : "사용자 역할을 저장하지 못했습니다.",
      );
    }
  };

  const revokeRole = async () => {
    if (!revokeTarget) return;
    setState("loading");
    setRevokeNotice("");
    try {
      await apiRequest<null>(
        `/api/users/${encodeURIComponent(userId)}/roles/${encodeURIComponent(revokeTarget.userRoleId)}`,
        {
          method: "DELETE",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ approvalUserId: revokeTarget.approvalUserId }),
        },
      );
      setRevokeTarget(null);
      await loadRoles(true);
    } catch (error) {
      const permissionDenied = isPermissionError(error);
      setState(permissionDenied ? "permission" : "error");
      setRevokeNotice(
        permissionDenied
          ? "권한이 없습니다."
          : "사용자 역할을 회수하지 못했습니다.",
      );
    }
  };

  if (state === "permission") {
    return (
      <section
        className="user-role-management user-role-state"
        aria-live="polite"
      >
        <h1>사용자 역할 관리</h1>
        <p>권한이 없습니다.</p>
      </section>
    );
  }

  return (
    <section
      className="user-role-management"
      aria-labelledby="user-role-management-title"
    >
      <p className="breadcrumb">
        시스템 관리 &gt; 역할·권한 관리 &gt; 사용자 역할 관리
      </p>
      <h1 id="user-role-management-title">사용자 역할 관리</h1>
      <section className="user-role-search" aria-label="사용자 역할 조회">
        <label>
          사용자 ID
          <input
            aria-label="사용자 ID"
            value={userId}
            onChange={(event) => setUserId(event.target.value)}
          />
        </label>
        <button
          type="button"
          className="primary-action"
          onClick={() => void loadRoles()}
        >
          조회
        </button>
      </section>

      {message && state === "success" && (
        <p className="success-message" role="status">
          {message}
        </p>
      )}
      <section className="user-role-results" aria-live="polite">
        <div className="section-title">
          <h2>현재 역할</h2>
          <button
            type="button"
            className="primary-action"
            disabled={state === "loading" || !userId.trim()}
            onClick={() => openGrant(null)}
          >
            역할 부여
          </button>
        </div>
        {state === "loading" && (
          <p className="loading-message">역할을 조회하는 중입니다.</p>
        )}
        {state === "empty" && (
          <p className="empty-message">현재 역할이 없습니다.</p>
        )}
        {state === "error" && (
          <p className="error-message">
            {message}{" "}
            <button
              type="button"
              className="text-action"
              onClick={() => void loadRoles()}
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
                  <th>역할코드</th>
                  <th>승인자</th>
                  <th>유효 시작일</th>
                  <th>유효 종료일</th>
                  <th>부여 유형</th>
                  <th>작업</th>
                </tr>
              </thead>
              <tbody>
                {roles.map((userRole) => (
                  <tr key={userRole.userRoleId}>
                    <td>{userRole.roleCode}</td>
                    <td>{userRole.approvalUserId}</td>
                    <td>{userRole.effectiveStartDate}</td>
                    <td>{userRole.effectiveEndDate ?? "-"}</td>
                    <td>{userRole.assignmentType}</td>
                    <td>
                      <button
                        type="button"
                        className="text-action"
                        onClick={() => openGrant(userRole)}
                      >
                        변경
                      </button>
                      <button
                        type="button"
                        className="text-action destructive-action"
                        onClick={() => {
                          setRevokeNotice("");
                          setRevokeTarget(userRole);
                        }}
                      >
                        회수
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>

      {grantOpen && (
        <div className="modal-backdrop" role="presentation">
          <section
            className="user-role-modal"
            role="dialog"
            aria-modal="true"
            aria-labelledby="user-role-modal-title"
          >
            <div className="modal-heading">
              <h2 id="user-role-modal-title">역할 부여·변경</h2>
              <button
                type="button"
                className="text-action"
                onClick={() => setGrantOpen(false)}
              >
                닫기
              </button>
            </div>
            <label>
              역할코드
              <input
                aria-label="역할코드"
                value={form.roleCode}
                onChange={(event) =>
                  setForm({ ...form, roleCode: event.target.value })
                }
              />
            </label>
            <label>
              승인자
              <input
                aria-label="승인자"
                value={form.approvalUserId}
                onChange={(event) =>
                  setForm({ ...form, approvalUserId: event.target.value })
                }
              />
            </label>
            <label>
              유효 시작일
              <input
                aria-label="유효 시작일"
                type="date"
                value={form.effectiveStartDate}
                onChange={(event) =>
                  setForm({ ...form, effectiveStartDate: event.target.value })
                }
              />
            </label>
            <label>
              유효 종료일
              <input
                aria-label="유효 종료일"
                type="date"
                value={form.effectiveEndDate}
                onChange={(event) =>
                  setForm({ ...form, effectiveEndDate: event.target.value })
                }
              />
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
            {editingRole && (
              <p className="modal-note">
                선택한 역할의 승인자와 유효기간을 변경합니다.
              </p>
            )}
            {state === "error" && <p className="error-message">{message}</p>}
            <div className="form-actions">
              <button
                type="button"
                className="text-action"
                onClick={() => setGrantOpen(false)}
              >
                취소
              </button>
              <button
                type="button"
                className="primary-action"
                disabled={state === "loading"}
                onClick={() => void saveRole()}
              >
                저장
              </button>
            </div>
          </section>
        </div>
      )}

      {revokeTarget && (
        <div className="modal-backdrop" role="presentation">
          <section
            className="user-role-modal"
            role="dialog"
            aria-modal="true"
            aria-labelledby="revoke-modal-title"
          >
            <div className="modal-heading">
              <h2 id="revoke-modal-title">역할 회수 확인</h2>
              <button
                type="button"
                className="text-action"
                onClick={() => setRevokeTarget(null)}
              >
                닫기
              </button>
            </div>
            <p>{revokeTarget.roleCode} 역할을 회수하시겠습니까?</p>
            <p className="modal-note">
              회수하면 역할 이력은 보존되고 현재 적용 역할에서 제외됩니다.
            </p>
            {revokeNotice && (
              <p className="error-message" role="alert">
                {revokeNotice}
              </p>
            )}
            <div className="form-actions">
              <button
                type="button"
                className="text-action"
                onClick={() => setRevokeTarget(null)}
              >
                취소
              </button>
              <button
                type="button"
                className="destructive-action"
                data-testid="user-role-revoke-confirm-button"
                disabled={state === "loading"}
                onClick={() => void revokeRole()}
              >
                회수
              </button>
            </div>
          </section>
        </div>
      )}
    </section>
  );
}
