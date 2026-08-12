import { useState } from "react";
import { ApiRequestError, apiRequest } from "../../shared/api/client";

type SessionStatus = {
  sessionId: string;
  userId: string;
  loginAt: string;
  lastActivityAt: string;
  ipAddress: string;
  status: string;
};
type SessionHistory = SessionStatus & {
  terminationType: string;
  terminatedAt: string;
  terminatedBy: string | null;
  terminationReason: string | null;
};

const formatDateTime = (value: string | null) =>
  value ? new Date(value).toLocaleString("ko-KR") : "-";

export function SessionStatusManagementPage() {
  const [sessions, setSessions] = useState<SessionStatus[]>([]);
  const [selected, setSelected] = useState<SessionStatus | null>(null);
  const [reason, setReason] = useState("");
  const [state, setState] = useState<
    "idle" | "loading" | "empty" | "error" | "permission" | "success"
  >("idle");
  const [message, setMessage] = useState("");

  const load = async () => {
    setState("loading");
    setMessage("");
    setSelected(null);
    try {
      const response = await apiRequest<SessionStatus[]>(
        "/api/session-status?page=0&size=20",
      );
      setSessions(response.data);
      setState(response.data.length ? "success" : "empty");
    } catch (error) {
      setState(
        error instanceof ApiRequestError && error.status === 403
          ? "permission"
          : "error",
      );
      setMessage(
        error instanceof ApiRequestError && error.status === 403
          ? "권한이 없습니다."
          : "접속현황을 조회하지 못했습니다.",
      );
    }
  };
  const terminate = async () => {
    if (!selected) return;
    try {
      await apiRequest<void>(
        `/api/session-status/${selected.sessionId}/termination`,
        {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ reason }),
        },
      );
      setReason("");
      setSelected(null);
      setMessage("선택한 세션을 강제종료했습니다.");
      await load();
    } catch (error) {
      setMessage(
        error instanceof ApiRequestError
          ? error.message
          : "강제종료에 실패했습니다.",
      );
    }
  };
  if (state === "permission")
    return (
      <section
        className="session-management session-state"
        data-testid="session-status-management"
      >
        <h1>접속현황 관리</h1>
        <p>권한이 없습니다.</p>
      </section>
    );
  return (
    <section
      className="session-management"
      data-testid="session-status-management"
    >
      <p className="breadcrumb">
        보안·감사 관리 &gt; 접속기록 관리 &gt; 접속현황 관리
      </p>
      <h1>접속현황 관리</h1>
      <section className="session-panel">
        <div className="section-title">
          <h2>현재 활성 세션</h2>
          <button
            className="primary-action"
            data-testid="session-status-search-button"
            onClick={() => void load()}
            type="button"
          >
            조회
          </button>
        </div>
        {state === "loading" && (
          <p className="loading-message">접속현황을 불러오는 중입니다.</p>
        )}
        {state === "empty" && (
          <p className="empty-message">현재 활성 세션이 없습니다.</p>
        )}
        {state === "error" && (
          <p className="error-message">
            {message}{" "}
            <button
              className="text-action"
              onClick={() => void load()}
              type="button"
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
                  <th>사용자</th>
                  <th>로그인시각</th>
                  <th>최종활동시각</th>
                  <th>IP</th>
                  <th>세션상태</th>
                  <th>작업</th>
                </tr>
              </thead>
              <tbody>
                {sessions.map((session) => (
                  <tr
                    key={session.sessionId}
                    data-testid={`session-status-row-${session.sessionId}`}
                  >
                    <td>{session.userId}</td>
                    <td>{formatDateTime(session.loginAt)}</td>
                    <td>{formatDateTime(session.lastActivityAt)}</td>
                    <td>{session.ipAddress}</td>
                    <td>{session.status}</td>
                    <td>
                      <button
                        className="row-link"
                        onClick={() => setSelected(session)}
                        data-testid={`session-status-detail-${session.sessionId}`}
                        type="button"
                      >
                        상세
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
        {message && state === "success" && (
          <p className="success-message" role="status">
            {message}
          </p>
        )}
      </section>
      {selected && (
        <div className="modal-backdrop" role="presentation">
          <section
            className="user-role-modal"
            aria-modal="true"
            role="dialog"
            data-testid="session-termination-modal"
          >
            <div className="modal-heading">
              <h2>세션 상세 및 강제종료</h2>
              <button
                className="text-action"
                onClick={() => setSelected(null)}
                type="button"
              >
                닫기
              </button>
            </div>
            <dl className="detail-grid">
              <div>
                <dt>사용자</dt>
                <dd>{selected.userId}</dd>
              </div>
              <div>
                <dt>IP</dt>
                <dd>{selected.ipAddress}</dd>
              </div>
              <div>
                <dt>로그인시각</dt>
                <dd>{formatDateTime(selected.loginAt)}</dd>
              </div>
              <div>
                <dt>최종활동시각</dt>
                <dd>{formatDateTime(selected.lastActivityAt)}</dd>
              </div>
            </dl>
            <label>
              강제종료 사유
              <textarea
                data-testid="session-termination-reason-input"
                value={reason}
                onChange={(event) => setReason(event.target.value)}
              />
            </label>
            {message && <p className="error-message">{message}</p>}
            <div className="form-actions">
              <button
                className="text-action"
                onClick={() => setSelected(null)}
                type="button"
              >
                취소
              </button>
              <button
                className="primary-action"
                data-testid="session-termination-confirm-button"
                onClick={() => void terminate()}
                type="button"
              >
                강제종료 확인
              </button>
            </div>
          </section>
        </div>
      )}
    </section>
  );
}

export function SessionTerminationHistoryPage() {
  const [userId, setUserId] = useState("");
  const [startedAt, setStartedAt] = useState("");
  const [endedAt, setEndedAt] = useState("");
  const [history, setHistory] = useState<SessionHistory[]>([]);
  const [selected, setSelected] = useState<SessionHistory | null>(null);
  const [state, setState] = useState<
    "idle" | "loading" | "empty" | "error" | "permission" | "success"
  >("idle");
  const [message, setMessage] = useState("");
  const load = async () => {
    setState("loading");
    const params = new URLSearchParams({ page: "0", size: "20" });
    if (userId) params.set("userId", userId);
    if (startedAt) params.set("startedAt", new Date(startedAt).toISOString());
    if (endedAt) params.set("endedAt", new Date(endedAt).toISOString());
    try {
      const response = await apiRequest<SessionHistory[]>(
        `/api/session-termination-history?${params}`,
      );
      setHistory(response.data);
      setState(response.data.length ? "success" : "empty");
    } catch (error) {
      setState(
        error instanceof ApiRequestError && error.status === 403
          ? "permission"
          : "error",
      );
      setMessage(
        error instanceof ApiRequestError
          ? error.message
          : "종료이력을 조회하지 못했습니다.",
      );
    }
  };
  if (state === "permission")
    return (
      <section
        className="session-management session-state"
        data-testid="session-history-management"
      >
        <h1>세션 종료이력</h1>
        <p>권한이 없습니다.</p>
      </section>
    );
  return (
    <section
      className="session-management"
      data-testid="session-history-management"
    >
      <p className="breadcrumb">
        보안·감사 관리 &gt; 접속기록 관리 &gt; 세션 종료이력
      </p>
      <h1>세션 종료이력</h1>
      <form
        className="session-filter"
        onSubmit={(event) => {
          event.preventDefault();
          void load();
        }}
      >
        <label>
          사용자 ID
          <input
            value={userId}
            onChange={(event) => setUserId(event.target.value)}
          />
        </label>
        <label>
          시작일시
          <input
            type="datetime-local"
            value={startedAt}
            onChange={(event) => setStartedAt(event.target.value)}
          />
        </label>
        <label>
          종료일시
          <input
            type="datetime-local"
            value={endedAt}
            onChange={(event) => setEndedAt(event.target.value)}
          />
        </label>
        <button
          className="primary-action"
          data-testid="session-history-search-button"
        >
          조회
        </button>
      </form>
      <section className="session-panel">
        <h2>종료이력 목록</h2>
        {state === "loading" && (
          <p className="loading-message">종료이력을 불러오는 중입니다.</p>
        )}
        {state === "empty" && (
          <p className="empty-message">조건에 맞는 종료이력이 없습니다.</p>
        )}
        {state === "error" && <p className="error-message">{message}</p>}
        {state === "success" && (
          <div className="table-scroll">
            <table>
              <thead>
                <tr>
                  <th>사용자</th>
                  <th>종료유형</th>
                  <th>종료시각</th>
                  <th>상세</th>
                </tr>
              </thead>
              <tbody>
                {history.map((item) => (
                  <tr key={item.sessionId}>
                    <td>{item.userId}</td>
                    <td>{item.terminationType}</td>
                    <td>{formatDateTime(item.terminatedAt)}</td>
                    <td>
                      <button
                        className="row-link"
                        onClick={() => setSelected(item)}
                        type="button"
                      >
                        상세
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>
      {selected && (
        <div className="modal-backdrop">
          <section
            className="user-role-modal"
            aria-modal="true"
            role="dialog"
            data-testid="session-history-detail-modal"
          >
            <div className="modal-heading">
              <h2>세션 종료이력 상세</h2>
              <button
                className="text-action"
                onClick={() => setSelected(null)}
                type="button"
              >
                닫기
              </button>
            </div>
            <dl className="detail-grid">
              <div>
                <dt>사용자</dt>
                <dd>{selected.userId}</dd>
              </div>
              <div>
                <dt>종료유형</dt>
                <dd>{selected.terminationType}</dd>
              </div>
              <div>
                <dt>종료시각</dt>
                <dd>{formatDateTime(selected.terminatedAt)}</dd>
              </div>
              <div>
                <dt>처리 사유</dt>
                <dd>{selected.terminationReason ?? "-"}</dd>
              </div>
            </dl>
            <p className="readonly-note">
              종료이력은 읽기 전용이며 수정하거나 삭제할 수 없습니다.
            </p>
          </section>
        </div>
      )}
    </section>
  );
}
