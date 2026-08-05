import { AlertCircle, CheckCircle2, Lock, SearchX } from "lucide-react";

type StateProps = {
  label: string;
  detail?: string;
  tone?: "neutral" | "success" | "error" | "permission";
};

export function StateBlock({ label, detail, tone = "neutral" }: StateProps) {
  const Icon =
    tone === "success"
      ? CheckCircle2
      : tone === "error"
        ? AlertCircle
        : tone === "permission"
          ? Lock
          : SearchX;
  const toneClass =
    tone === "success"
      ? "bg-[#e7f3eb] text-[#103c25]"
      : tone === "error"
        ? "border border-[#9e0a0a] bg-white text-[#9e0a0a]"
        : "bg-[#f6f6f3] text-plumBlack";
  return (
    <div className={`rounded-[32px] p-6 text-center ${toneClass}`}>
      <Icon className="mx-auto h-6 w-6" />
      <strong className="mt-3 block text-base">{label}</strong>
      {detail && (
        <p className="mx-auto mt-2 max-w-2xl text-sm text-oliveMuted">
          {detail}
        </p>
      )}
    </div>
  );
}

export function SkeletonGrid() {
  return (
    <div className="grid grid-cols-1 gap-4 md:grid-cols-3" aria-label="로딩 중">
      {[180, 132, 212, 156, 192, 144].map((height, index) => (
        <div
          key={index}
          className="animate-pulse rounded-[32px] bg-warmSand"
          style={{ minHeight: height }}
        />
      ))}
    </div>
  );
}
