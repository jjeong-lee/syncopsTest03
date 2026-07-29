import { useEffect, useMemo, useState } from "react";
import { useParams } from "react-router-dom";
import { DataView } from "../components/DataView";
import { ScreenToolbar } from "../components/ScreenToolbar";
import {
  EmptyState,
  ErrorState,
  HealthEmptyState,
  LoadingState,
  PermissionState,
} from "../components/StateViews";
import { Badge, Card } from "../components/ui";
import { apiRequest } from "../services/apiClient";
import type { Page, ScreenConfig, UiState } from "../types";

function resolveEndpoint(screen: ScreenConfig, groupId?: string) {
  return screen.endpoint.replace(
    ":groupId",
    encodeURIComponent(groupId ?? "USER_STATUS"),
  );
}

function isEmptyData(data: unknown) {
  if (Array.isArray(data)) return data.length === 0;
  if (
    data &&
    typeof data === "object" &&
    Array.isArray((data as Page<unknown>).content)
  )
    return (data as Page<unknown>).content.length === 0;
  return !data;
}

function emptyCopy(screen: ScreenConfig) {
  const ctaByKind: Partial<Record<ScreenConfig["kind"], string>> = {
    users: "조건 초기화",
    organizations: "seed 확인",
    roles: "R01~R09 seed 확인",
    userRoles: "역할 부여 준비",
    menuPermissions: "대상 선택",
    menuStructure: "메뉴 seed 확인",
    menuInfo: "메뉴 선택",
    codeGroups: "코드그룹 등록",
    detailCodes: "상세코드 등록",
  };
  return {
    title: `${screen.title} 데이터가 없습니다.`,
    description: `${screen.menuPath} 화면의 ${screen.primaryEntity} 조회 결과가 비어 있습니다. 검색 조건 또는 기준 seed를 확인하세요.`,
    cta: ctaByKind[screen.kind],
  };
}

export function ManagementPage({ screen }: { screen: ScreenConfig }) {
  const params = useParams();
  const endpoint = useMemo(
    () => resolveEndpoint(screen, params.groupId),
    [params.groupId, screen],
  );
  const [state, setState] = useState<UiState>("loading");
  const [data, setData] = useState<unknown>(null);
  const [message, setMessage] = useState("");
  const [refreshToken, setRefreshToken] = useState(0);

  useEffect(() => {
    let live = true;
    setState("loading");
    setMessage("");
    apiRequest(endpoint)
      .then((response) => {
        if (!live) return;
        setData(response);
        setState(isEmptyData(response) ? "empty" : "success");
      })
      .catch((e) => {
        if (!live) return;
        setMessage(
          e instanceof Error ? e.message : "조회 중 오류가 발생했습니다.",
        );
        setState(
          (e as { status?: number }).status === 403 ? "permission" : "error",
        );
      });
    return () => {
      live = false;
    };
  }, [endpoint, refreshToken]);

  const empty = emptyCopy(screen);

  return (
    <div className="mx-auto grid w-full max-w-7xl gap-6">
      <div className="flex flex-wrap items-start justify-between gap-4">
        <div className="grid gap-2">
          <div className="flex flex-wrap items-center gap-2">
            <Badge tone="muted">{screen.archetype}</Badge>
            <Badge tone="default">{screen.id}</Badge>
          </div>
          <h1 className="text-2xl font-bold tracking-tight text-slate-950 md:text-3xl">
            {screen.title}
          </h1>
          <p className="max-w-3xl text-sm leading-6 text-slate-500">
            {screen.description}
          </p>
        </div>
        <Card className="min-w-64 p-4">
          <p className="text-xs font-semibold uppercase tracking-wide text-slate-500">
            Route
          </p>
          <p className="mt-1 font-mono text-sm text-slate-900">
            {params.groupId
              ? `/system/code-groups/${params.groupId}/detail-codes`
              : screen.route}
          </p>
          <p className="mt-2 text-xs text-slate-500">{screen.primaryEntity}</p>
        </Card>
      </div>

      {state === "loading" && (
        <LoadingState label={`${screen.title} 데이터를 불러오는 중입니다.`} />
      )}
      {state === "permission" && <PermissionState />}
      {state === "error" && (
        <ErrorState
          message={message}
          onRetry={() => setRefreshToken((value) => value + 1)}
        />
      )}
      {state === "empty" &&
        (screen.kind === "dashboard" ? (
          <HealthEmptyState />
        ) : (
          <EmptyState {...empty} />
        ))}
      {state === "success" && (
        <Card className="grid gap-5 p-5">
          <ScreenToolbar
            kind={screen.kind}
            onRefresh={() => setRefreshToken((value) => value + 1)}
          />
          <DataView kind={screen.kind} data={data} />
          <div className="rounded-lg border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-800">
            조회가 완료되었습니다. 저장 후에는 같은 route에서 follow-up read로
            갱신된 row를 확인합니다.
          </div>
        </Card>
      )}
    </div>
  );
}
