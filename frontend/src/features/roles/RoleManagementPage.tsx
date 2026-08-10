import { useState } from "react";
import { ApiRequestError, apiRequest } from "../../shared/api/client";

export type RoleSummary = {
  roleCode: string;
  roleName: string;
  purpose: string;
  assignmentCriteria: string | null;
  defaultDataScope: string | null;
};

type RoleForm = {
  roleCode: string;
  roleName: string;
  purpose: string;
  assignmentCriteria: string;
  defaultDataScope: string;
  reason: string;
};

const emptyRoleForm: RoleForm = {
  roleCode: "",
  roleName: "",
  purpose: "",
  assignmentCriteria: "",
  defaultDataScope: "",
  reason: "",
};

const toRoleForm = (role: RoleSummary): RoleForm => ({
  roleCode: role.roleCode,
  roleName: role.roleName,
  purpose: role.purpose,
  assignmentCriteria: role.assignmentCriteria ?? "",
  defaultDataScope: role.defaultDataScope ?? "",
  reason: "",
});

export function RoleManagementPage() {
  const [roles, setRoles] = useState<RoleSummary[]>([]);
  const [selectedRole, setSelectedRole] = useState<RoleSummary | null>(null);
  const [form, setForm] = useState<RoleForm>(emptyRoleForm);
  const [state, setState] = useState<
    "idle" | "loading" | "empty" | "error" | "permission" | "success"
  >("idle");
  const [message, setMessage] = useState("");
  const [fieldError, setFieldError] = useState<{
    field?: string;
    message: string;
  } | null>(null);

  const selectRole = (role: RoleSummary | null, clearMessage = true) => {
    setSelectedRole(role);
    setForm(role ? toRoleForm(role) : emptyRoleForm);
    if (clearMessage) {
      setMessage("");
      setFieldError(null);
    }
  };

  const loadRoles = async (preserveSelection = false) => {
    setState("loading");
    if (!preserveSelection) {
      selectRole(null);
      setMessage("");
    }
    try {
      const response = await apiRequest<RoleSummary[]>("/api/roles");
      setRoles(response.data);
      if (preserveSelection && selectedRole) {
        selectRole(
          response.data.find(
            (role) => role.roleCode === selectedRole.roleCode,
          ) ?? null,
          false,
        );
      }
      setState(response.data.length === 0 ? "empty" : "success");
    } catch (error) {
      const permissionDenied =
        error instanceof ApiRequestError &&
        (error.status === 401 || error.status === 403);
      setState(permissionDenied ? "permission" : "error");
      setMessage(
        permissionDenied
          ? "권한이 없습니다."
          : "역할 목록을 조회하지 못했습니다.",
      );
    }
  };

  const saveRole = async () => {
    if (!selectedRole) return;
    setState("loading");
    setMessage("");
    setFieldError(null);
    try {
      await apiRequest<null>("/api/roles", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(form),
      });
      setMessage("저장한 역할 정보를 다시 조회했습니다.");
      await loadRoles(true);
    } catch (error) {
      const permissionDenied =
        error instanceof ApiRequestError &&
        (error.status === 401 || error.status === 403);
      setState(permissionDenied ? "permission" : "error");
      setFieldError(
        error instanceof ApiRequestError && error.field
          ? { field: error.field, message: error.message }
          : null,
      );
      setMessage(
        permissionDenied
          ? "권한이 없습니다."
          : "역할 정보를 저장하지 못했습니다.",
      );
    }
  };

  if (state === "permission") {
    return (
      <section className="role-management role-state" aria-live="polite">
        <h1>역할 관리</h1>
        <p>권한이 없습니다.</p>
      </section>
    );
  }

  return (
    <section
      className="role-management"
      aria-labelledby="role-management-title"
    >
      <p className="breadcrumb">
        시스템 관리 &gt; 역할·권한 관리 &gt; 역할 관리
      </p>
      <h1 id="role-management-title">역할 관리</h1>
      <section className="role-search" aria-label="역할 목록 조회">
        <p>역할 목록과 역할별 목적을 확인합니다.</p>
        <button
          type="button"
          className="primary-action"
          onClick={() => void loadRoles()}
        >
          역할 목록 조회
        </button>
      </section>

      {message && state === "success" && (
        <p className="success-message" role="status">
          {message}
        </p>
      )}
      <section className="role-results" aria-live="polite">
        <div className="section-title">
          <h2>역할 목록</h2>
          {state === "success" && <span>{roles.length}개</span>}
        </div>
        {state === "loading" && (
          <p className="loading-message">역할 목록을 불러오는 중입니다.</p>
        )}
        {state === "empty" && (
          <p className="empty-message">역할 목록이 없습니다.</p>
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
                  <th>역할명</th>
                  <th>역할별 목적</th>
                </tr>
              </thead>
              <tbody>
                {roles.map((role) => (
                  <tr
                    key={role.roleCode}
                    className={
                      selectedRole?.roleCode === role.roleCode
                        ? "selected-row"
                        : ""
                    }
                  >
                    <td>{role.roleCode}</td>
                    <td>
                      <button
                        type="button"
                        className="row-link"
                        aria-label={`${role.roleCode} 역할 선택`}
                        onClick={() => selectRole(role)}
                      >
                        {role.roleName}
                      </button>
                    </td>
                    <td>{role.purpose}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>

      {selectedRole && (
        <section className="role-form">
          <h2>선택한 역할 상세·편집</h2>
          <label>
            역할코드
            <input aria-label="역할코드" readOnly value={form.roleCode} />
          </label>
          <label>
            역할명
            <input
              aria-label="역할명"
              value={form.roleName}
              onChange={(event) =>
                setForm({ ...form, roleName: event.target.value })
              }
            />
            {fieldError?.field === "roleName" && (
              <span className="field-error" role="alert">
                {fieldError.message}
              </span>
            )}
          </label>
          <label>
            역할별 목적
            <textarea
              aria-label="역할별 목적"
              value={form.purpose}
              onChange={(event) =>
                setForm({ ...form, purpose: event.target.value })
              }
            />
          </label>
          <label>
            부여 기준
            <textarea
              aria-label="부여 기준"
              value={form.assignmentCriteria}
              onChange={(event) =>
                setForm({ ...form, assignmentCriteria: event.target.value })
              }
            />
          </label>
          <label>
            데이터 범위 기본값
            <textarea
              aria-label="데이터 범위 기본값"
              value={form.defaultDataScope}
              onChange={(event) =>
                setForm({ ...form, defaultDataScope: event.target.value })
              }
            />
          </label>
          <label>
            변경 사유
            <textarea
              aria-label="변경 사유"
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
              onClick={() => selectRole(selectedRole)}
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
      )}
    </section>
  );
}
