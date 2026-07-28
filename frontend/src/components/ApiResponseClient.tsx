import { StatePanel } from "./StatePanel";

export function ApiResponseClient({
  requestId,
  message,
}: {
  requestId?: string;
  message?: string;
}) {
  if (!requestId && !message) return null;
  return (
    <StatePanel
      state="success"
      message={`${message ?? "API 응답이 정상 처리되었습니다."}${requestId ? ` requestId: ${requestId}` : ""}`}
    />
  );
}
