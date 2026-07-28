export function StatePanel({
  state,
  message,
  compact = false,
}: {
  state: "loading" | "empty" | "error" | "permission" | "success";
  message: string;
  compact?: boolean;
}) {
  const labels = {
    loading: "불러오는 중",
    empty: "결과 없음",
    error: "오류",
    permission: "권한 없음",
    success: "완료",
  };
  const tone =
    state === "error" || state === "permission"
      ? "border-destructive/25 bg-destructive/10 text-destructive"
      : state === "success"
        ? "border-emerald-200 bg-emerald-50 text-emerald-700"
        : state === "loading"
          ? "border-border bg-muted/50 text-muted-foreground"
          : "border-dashed bg-card text-muted-foreground";
  return (
    <section
      className={`${compact ? "inline-flex" : "flex"} items-center gap-3 rounded-lg border px-4 py-3 text-sm shadow-sm ${tone}`}
      role={state === "error" ? "alert" : "status"}
      aria-live="polite"
    >
      {state === "loading" && (
        <span
          className="size-4 animate-pulse rounded-full bg-current opacity-50"
          aria-hidden="true"
        />
      )}
      <strong>{labels[state]}</strong>
      <span>{message}</span>
    </section>
  );
}
