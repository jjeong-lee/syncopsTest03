import {
  AlertTriangle,
  Database,
  Loader2,
  LockKeyhole,
  SearchX,
} from "lucide-react";
import { Button, Card } from "./ui";

export function LoadingState({
  label = "데이터를 불러오는 중입니다.",
}: {
  label?: string;
}) {
  return (
    <Card className="overflow-hidden p-6">
      <div className="mb-5 flex items-center gap-3 text-sm font-medium text-slate-600">
        <Loader2 className="size-4 animate-spin text-blue-600" />
        {label}
      </div>
      <div className="grid gap-3">
        {[0, 1, 2, 3].map((row) => (
          <div
            key={row}
            className="grid grid-cols-[1.2fr_0.8fr_1fr_0.7fr] gap-3"
          >
            {[0, 1, 2, 3].map((cell) => (
              <div
                key={cell}
                className="h-9 animate-pulse rounded-md bg-slate-100"
              />
            ))}
          </div>
        ))}
      </div>
    </Card>
  );
}

export function EmptyState({
  title,
  description,
  cta,
}: {
  title: string;
  description: string;
  cta?: string;
}) {
  return (
    <Card className="flex min-h-72 flex-col items-center justify-center gap-3 p-8 text-center">
      <div className="rounded-full bg-slate-100 p-4 text-slate-500">
        <SearchX className="size-8" />
      </div>
      <h2 className="text-lg font-semibold tracking-tight text-slate-950">
        {title}
      </h2>
      <p className="max-w-md text-sm leading-6 text-slate-500">{description}</p>
      {cta && (
        <Button className="mt-2 border border-slate-200 bg-white text-slate-900 hover:bg-slate-50">
          {cta}
        </Button>
      )}
    </Card>
  );
}

export function ErrorState({
  message,
  onRetry,
}: {
  message: string;
  onRetry?: () => void;
}) {
  return (
    <Card className="border-rose-200 bg-rose-50 p-6 text-rose-900">
      <div className="flex items-start gap-3">
        <AlertTriangle className="mt-0.5 size-5" />
        <div className="grid gap-1">
          <h2 className="font-semibold">조회 중 오류가 발생했습니다.</h2>
          <p className="text-sm text-rose-700">
            {message || "API 응답을 확인한 뒤 다시 시도하세요."}
          </p>
          {onRetry && (
            <Button
              className="mt-3 w-fit bg-rose-700 text-white hover:bg-rose-800"
              onClick={onRetry}
            >
              다시 조회
            </Button>
          )}
        </div>
      </div>
    </Card>
  );
}

export function PermissionState() {
  return (
    <Card className="border-amber-200 bg-amber-50 p-6 text-amber-950">
      <div className="flex items-start gap-3">
        <LockKeyhole className="mt-0.5 size-5" />
        <div className="grid gap-1">
          <h2 className="font-semibold">접근 권한이 없습니다.</h2>
          <p className="text-sm text-amber-800">
            R09 시스템관리자 권한이 필요한 화면입니다. 메뉴가 보이지 않는 경우
            권한 matrix를 확인하세요.
          </p>
        </div>
      </div>
    </Card>
  );
}

export function HealthEmptyState() {
  return (
    <Card className="flex min-h-72 flex-col items-center justify-center gap-3 p-8 text-center">
      <Database className="size-10 text-slate-400" />
      <h2 className="text-lg font-semibold tracking-tight text-slate-950">
        health 데이터가 없습니다.
      </h2>
      <p className="max-w-md text-sm leading-6 text-slate-500">
        /api/health 응답을 받을 수 있도록 백엔드와 DB 상태를 먼저 확인하세요.
      </p>
    </Card>
  );
}
