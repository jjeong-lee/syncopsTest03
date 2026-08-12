import { useState } from "react";
import { ApiRequestError, apiRequest } from "../../shared/api/client";

type SessionSummary = {
  sessionId: string;
  userId: string;
  loginAt: string;
  lastActivityAt: string;
  ipAddress: string;
  status: string;
};
type SessionEndHistory = {
  sessionId: string;
  userId: string;
  loginAt: string;
  endedAt: string;
  endType: string;
  actorUserId: string | null;
  reason: string | null;
  ipAddress: string;
};

const format = (value: string) => new Date(value).toLocaleString("ko-KR");

export function SessionStatusManagementPage() {
  const [activeSessions, setActiveSessions] = useState<SessionSummary[]>([]);
  const [history, setHistory] = useState<SessionEndHistory[]>([]);
  const [selectedSession, setSelectedSession] = useState<SessionSummary | null>(
    null,
  );
  const [selectedHistory, setSelectedHistory] =
    useState<SessionEndHistory | null>(null);
  const [reason, setReason] = useState("");
  const [state, setState] = useState<
    "idle" | "loading" | "success" | "empty" | "error"
  >("idle");
  const [message, setMessage] = useState("");

  const loadActiveSessions = async () => {
    setState("loading");
    setMessage("");
    try {
      const response = await apiRequest<SessionSummary[]>(
        "/api/session-status/active",
      );
      setActiveSessions(response.data);
      setState(response.data.length ? "success" : "empty");
    } catch (error) {
      setState("error");
      setMessage(
        error instanceof ApiRequestError && error.status === 403
          ? "권한이 없습니다."
          : "현재 접속현황을 조회하지 못했습니다.",
      );
    }
  };
  const loadHistory = async () => {
    setState("loading");
    setMessage("");
    try {
      const response = await apiRequest<SessionEndHistory[]>(
        "/api/session-status/history",
      );
      setHistory(response.data);
      setState(response.data.length ? "success" : "empty");
    } catch {
      setState("error");
      setMessage("세션 종료이력을 조회하지 못했습니다.");
    }
  };
  const forceTerminate = async () => {
    if (!selectedSession || !reason.trim()) return;
    try {
      await apiRequest<void>(
        `/api/session-status/${selectedSession.sessionId}/force-terminate`,
        {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ reason: reason.trim() }),
        },
      );
      setActiveSessions((sessions) =>
        sessions.filter(
          (session) => session.sessionId !== selectedSession.sessionId,
        ),
      );
      setState("success");
      setMessage("세션을 강제종료했습니다.");
      setSelectedSession(null);
      setReason("");
    } catch (error) {
      setMessage(
        error instanceof ApiRequestError && error.status === 403
          ? "권한이 없습니다."
          : "세션을 강제종료하지 못했습니다.",
      );
    }
  };

  return (
    <section
      className="session-status-management"
      data-testid="session-status-management-page"
      aria-labelledby="session-status-title"
    >
      <p className="breadcrumb">
        보안·감사 관리 &gt; 접속기록 관리 &gt; 접속현황 관리
      </p>
      <h1 id="session-status-title">접속현황 관리</h1>
      <section className="session-command-panel">
        <h2>현재 활성 세션</h2>
        <p>
          로그인시각, 최종활동시각, IP 및 세션상태를 확인하고 권한 있는 운영자가
          강제종료할 수 있습니다.
        </p>
        <div className="form-actions">
          <button
            type="button"
            className="primary-action"
            data-testid="session-status-active-search-button"
            onClick={() => void loadActiveSessions()}
          >
            현재 접속현황 조회
          </button>
          <button
            type="button"
            className="text-action"
            data-testid="session-status-history-search-button"
            onClick={() => void loadHistory()}
          >
            종료이력 조회
          </button>
        </div>
      </section>
      {message && (
        <p
          className={state === "error" ? "error-message" : "success-message"}
          role="status"
        >
          {message}
        </p>
      )}
      {state === "loading" && (
        <p className="loading-message">세션 정보를 불러오는 중입니다.</p>
      )}
      {state === "empty" && (
        <p className="empty-message">조회 결과가 없습니다.</p>
      )}
      {activeSessions.length > 0 && (
        <section
          className="session-results"
          data-testid="session-status-active-results"
        >
          <div className="section-title">
            <h2>활성 세션 목록</h2>
            <span>{activeSessions.length}건</span>
          </div>
          <div className="table-scroll">
            <table>
              <thead>
                <tr>
                  <th>사용자</th>
                  <th>로그인시각</th>
                  <th>최종활동시각</th>
                  <th>IP</th>
                  <th>상태</th>
                </tr>
              </thead>
              <tbody>
                {activeSessions.map((session) => (
                  <tr
                    key={session.sessionId}
                    data-testid={`session-status-row-${session.sessionId}`}
                  >
                    <td>
                      <button
                        type="button"
                        className="row-link"
                        aria-label={`${session.userId} 세션 상세 열기`}
                        onClick={() => setSelectedSession(session)}
                      >
                        {session.userId}
                      </button>
                    </td>
                    <td>{format(session.loginAt)}</td>
                    <td>{format(session.lastActivityAt)}</td>
                    <td>{session.ipAddress}</td>
                    <td>{session.status}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </section>
      )}
      {history.length > 0 && (
        <section
          className="session-results"
          data-testid="session-status-history-results"
        >
          <div className="section-title">
            <h2>세션 종료이력</h2>
            <span>{history.length}건</span>
          </div>
          <div className="table-scroll">
            <table>
              <thead>
                <tr>
                  <th>사용자</th>
                  <th>종료시각</th>
                  <th>종료유형</th>
                  <th>처리자</th>
                </tr>
              </thead>
              <tbody>
                {history.map((entry) => (
                  <tr key={`${entry.sessionId}-${entry.endedAt}`}>
                    <td>
                      <button
                        type="button"
                        className="row-link"
                        aria-label={`${entry.userId} 종료이력 상세 열기`}
                        onClick={() => setSelectedHistory(entry)}
                      >
                        {entry.userId}
                      </button>
                    </td>
                    <td>{format(entry.endedAt)}</td>
                    <td>{entry.endType}</td>
                    <td>{entry.actorUserId ?? "-"}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </section>
      )}
      {selectedSession && (
        <div className="modal-backdrop" role="presentation">
          <section
            className="session-modal"
            role="dialog"
            aria-modal="true"
            aria-label="세션 상세정보"
            data-testid="session-status-detail-modal"
          >
            <div className="section-title">
              <h2>세션 상세정보</h2>
              <button
                type="button"
                className="text-action"
                onClick={() => setSelectedSession(null)}
              >
                닫기
              </button>
            </div>
            <dl className="detail-grid">
              <div>
                <dt>사용자</dt>
                <dd>{selectedSession.userId}</dd>
              </div>
              <div>
                <dt>IP</dt>
                <dd>{selectedSession.ipAddress}</dd>
              </div>
              <div>
                <dt>로그인시각</dt>
                <dd>{format(selectedSession.loginAt)}</dd>
              </div>
              <div>
                <dt>최종활동시각</dt>
                <dd>{format(selectedSession.lastActivityAt)}</dd>
              </div>
              <div>
                <dt>세션상태</dt>
                <dd>{selectedSession.status}</dd>
              </div>
            </dl>
            <div className="form-actions">
              <button
                type="button"
                className="primary-action"
                onClick={() => setReason(" ")}
              >
                강제종료
              </button>
            </div>
          </section>
        </div>
      )}
      {selectedSession && reason !== "" && (
        <div className="modal-backdrop" role="presentation">
          <section
            className="session-modal"
            role="dialog"
            aria-modal="true"
            aria-label="세션 강제종료"
            data-testid="session-status-force-terminate-modal"
          >
            <h2>세션 강제종료</h2>
            <p>{selectedSession.userId} 사용자의 세션을 즉시 무효화합니다.</p>
            <label>
              강제종료 사유
              <textarea
                data-testid="session-status-force-reason-input"
                value={reason.trimStart()}
                onChange={(event) => setReason(event.target.value)}
              />
            </label>
            <div className="form-actions">
              <button
                type="button"
                className="text-action"
                onClick={() => setReason("")}
              >
                취소
              </button>
              <button
                type="button"
                className="primary-action"
                data-testid="session-status-force-confirm-button"
                disabled={!reason.trim()}
                onClick={() => void forceTerminate()}
              >
                강제종료 확인
              </button>
            </div>
          </section>
        </div>
      )}
      {selectedHistory && (
        <div className="modal-backdrop" role="presentation">
          <section
            className="session-modal"
            role="dialog"
            aria-modal="true"
            aria-label="세션 종료이력 상세"
            data-testid="session-status-history-detail-modal"
          >
            <div className="section-title">
              <h2>세션 종료이력 상세</h2>
              <button
                type="button"
                className="text-action"
                onClick={() => setSelectedHistory(null)}
              >
                닫기
              </button>
            </div>
            <dl className="detail-grid">
              <div>
                <dt>사용자</dt>
                <dd>{selectedHistory.userId}</dd>
              </div>
              <div>
                <dt>종료유형</dt>
                <dd>{selectedHistory.endType}</dd>
              </div>
              <div>
                <dt>처리자</dt>
                <dd>{selectedHistory.actorUserId ?? "-"}</dd>
              </div>
              <div>
                <dt>사유</dt>
                <dd>{selectedHistory.reason ?? "-"}</dd>
              </div>
            </dl>
          </section>
        </div>
      )}
    </section>
  );
}
