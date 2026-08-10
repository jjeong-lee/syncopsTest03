import { useState } from "react";
import { ApiRequestError, apiRequest } from "../../shared/api/client";

export type UserSummary = {
  userId: string;
  personnelNo: string;
  name: string;
  organization: string | null;
  position: string | null;
  employmentStatus: string | null;
  roleCodes: string[];
  useYn: "Y" | "N";
  positionTitle: string | null;
  retirementDate: string | null;
  lastSyncedAt: string;
};

type SearchFilters = {
  personnelNo: string;
  name: string;
  organization: string;
  position: string;
  employmentStatus: string;
  roleCode: string;
  useYn: string;
};

const emptyFilters: SearchFilters = {
  personnelNo: "",
  name: "",
  organization: "",
  position: "",
  employmentStatus: "",
  roleCode: "",
  useYn: "",
};

const filterLabels: Array<[keyof SearchFilters, string]> = [
  ["personnelNo", "교번"],
  ["name", "성명"],
  ["organization", "소속"],
  ["position", "직급"],
  ["employmentStatus", "재직상태"],
  ["roleCode", "역할"],
  ["useYn", "사용여부"],
];

export function UserManagementPage() {
  const [filters, setFilters] = useState<SearchFilters>(emptyFilters);
  const [users, setUsers] = useState<UserSummary[]>([]);
  const [selectedUser, setSelectedUser] = useState<UserSummary | null>(null);
  const [state, setState] = useState<
    "idle" | "loading" | "empty" | "error" | "permission" | "success"
  >("idle");
  const [message, setMessage] = useState("");

  const search = async (preserveMessage = false) => {
    setState("loading");
    setSelectedUser(null);
    if (!preserveMessage) setMessage("");
    const params = new URLSearchParams();
    Object.entries(filters).forEach(([key, value]) => {
      if (value) params.set(key, value);
    });
    try {
      const response = await apiRequest<UserSummary[]>(
        `/api/users${params.size ? `?${params}` : ""}` as `/api/${string}`,
      );
      setUsers(response.data);
      setState(response.data.length === 0 ? "empty" : "success");
    } catch (error) {
      setState(
        error instanceof ApiRequestError && error.status === 403
          ? "permission"
          : "error",
      );
      setMessage(
        error instanceof ApiRequestError && error.status === 403
          ? "권한이 없습니다."
          : "사용자 목록을 조회하지 못했습니다.",
      );
    }
  };

  const selectUser = (user: UserSummary) => {
    setSelectedUser(user);
    setMessage("");
  };

  if (state === "permission") {
    return (
      <section className="user-management user-state" aria-live="polite">
        <h1>사용자 관리</h1>
        <p>권한이 없습니다.</p>
      </section>
    );
  }

  return (
    <section
      className="user-management"
      aria-labelledby="user-management-title"
    >
      <p className="breadcrumb">
        시스템 관리 &gt; 사용자·조직 관리 &gt; 사용자 관리
      </p>
      <h1 id="user-management-title">사용자 관리</h1>
      <form
        className="user-search-form"
        onSubmit={(event) => {
          event.preventDefault();
          void search();
        }}
      >
        <h2>검색조건</h2>
        <div className="filter-grid">
          {filterLabels.map(([key, label]) => (
            <label key={key}>
              {label}
              {key === "useYn" ? (
                <select
                  aria-label={label}
                  value={filters[key]}
                  onChange={(event) =>
                    setFilters({ ...filters, [key]: event.target.value })
                  }
                >
                  <option value="">전체</option>
                  <option value="Y">사용</option>
                  <option value="N">미사용</option>
                </select>
              ) : (
                <input
                  aria-label={label}
                  value={filters[key]}
                  onChange={(event) =>
                    setFilters({ ...filters, [key]: event.target.value })
                  }
                />
              )}
            </label>
          ))}
        </div>
        <div className="form-actions">
          <button
            type="button"
            className="text-action"
            onClick={() => {
              setFilters(emptyFilters);
              setUsers([]);
              setSelectedUser(null);
              setState("idle");
            }}
          >
            초기화
          </button>
          <button type="submit" className="primary-action">
            검색
          </button>
        </div>
      </form>
      {message && state === "success" && (
        <p className="success-message" role="status">
          {message}
        </p>
      )}

      <section className="user-results" aria-live="polite">
        <div className="section-title">
          <h2>검색 결과</h2>
          {state === "success" && <span>{users.length}명</span>}
        </div>
        {state === "loading" && (
          <p className="loading-message">사용자 목록을 불러오는 중입니다.</p>
        )}
        {state === "empty" && (
          <p className="empty-message">검색 결과가 없습니다.</p>
        )}
        {state === "error" && (
          <p className="error-message">
            {message}{" "}
            <button
              type="button"
              className="text-action"
              onClick={() => void search()}
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
                  <th>교번</th>
                  <th>성명</th>
                  <th>소속</th>
                  <th>직급</th>
                  <th>재직상태</th>
                  <th>역할</th>
                  <th>사용여부</th>
                  <th>보직</th>
                  <th>퇴직일자</th>
                  <th>최종 동기화일시</th>
                </tr>
              </thead>
              <tbody>
                {users.map((user) => (
                  <tr
                    key={user.userId}
                    className={
                      selectedUser?.userId === user.userId ? "selected-row" : ""
                    }
                    onClick={() => selectUser(user)}
                  >
                    <td>{user.personnelNo}</td>
                    <td>
                      <button
                        type="button"
                        className="row-link"
                        aria-label={`${user.name} 사용자 상세 열기`}
                        onClick={() => selectUser(user)}
                      >
                        {user.name}
                      </button>
                    </td>
                    <td>{user.organization ?? "-"}</td>
                    <td>{user.position ?? "-"}</td>
                    <td>{user.employmentStatus ?? "-"}</td>
                    <td>{user.roleCodes.join(", ") || "-"}</td>
                    <td>{user.useYn}</td>
                    <td>{user.positionTitle ?? "-"}</td>
                    <td>{user.retirementDate ?? "-"}</td>
                    <td>
                      {new Date(user.lastSyncedAt).toLocaleString("ko-KR")}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>

      {selectedUser && (
        <section className="user-detail">
          <div className="section-title">
            <h2>선택 사용자 상세</h2>
            <button
              type="button"
              className="text-action"
              onClick={() => setSelectedUser(null)}
            >
              선택 해제
            </button>
          </div>
          <dl className="detail-grid">
            <div>
              <dt>교번</dt>
              <dd>{selectedUser.personnelNo}</dd>
            </div>
            <div>
              <dt>성명</dt>
              <dd>{selectedUser.name}</dd>
            </div>
            <div>
              <dt>소속</dt>
              <dd>{selectedUser.organization ?? "-"}</dd>
            </div>
            <div>
              <dt>직급</dt>
              <dd>{selectedUser.position ?? "-"}</dd>
            </div>
            <div>
              <dt>재직상태</dt>
              <dd>{selectedUser.employmentStatus ?? "-"}</dd>
            </div>
            <div>
              <dt>역할</dt>
              <dd>{selectedUser.roleCodes.join(", ") || "-"}</dd>
            </div>
            <div>
              <dt>사용여부</dt>
              <dd>{selectedUser.useYn}</dd>
            </div>
            <div>
              <dt>보직</dt>
              <dd>{selectedUser.positionTitle ?? "-"}</dd>
            </div>
            <div>
              <dt>퇴직일자</dt>
              <dd>{selectedUser.retirementDate ?? "-"}</dd>
            </div>
            <div>
              <dt>최종 동기화일시</dt>
              <dd>
                {new Date(selectedUser.lastSyncedAt).toLocaleString("ko-KR")}
              </dd>
            </div>
          </dl>
          <p className="readonly-note">
            KORUS 원천 인사 정보와 역할·사용여부 변경은 이 화면에서 직접 수정할
            수 없습니다.
          </p>
        </section>
      )}
    </section>
  );
}
