import { useEffect, useState } from "react";
import { AppRoute } from "../../app/routes";
import { api } from "../../services/apiClient";
import { EffectivePeriodForm } from "../../components/EffectivePeriodForm";
import { ManagementTable } from "../../components/ManagementTable";
import { PermissionMatrix } from "../../components/PermissionMatrix";
import { SearchFilterBar } from "../../components/SearchFilterBar";
import { StatePanel } from "../../components/StatePanel";
import { TreeEditorPanel } from "../../components/TreeEditorPanel";
import {
  Button,
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
  Input,
  Textarea,
} from "../../components/ui";

type PageData =
  | { items?: Record<string, unknown>[]; requestId?: string }
  | Record<string, unknown>[]
  | Record<string, unknown>
  | null;

function itemsFrom(data: PageData): Record<string, unknown>[] {
  if (Array.isArray(data)) return data;
  if (data && "items" in data && Array.isArray(data.items)) return data.items;
  if (data && typeof data === "object" && Object.keys(data).length > 0)
    return [data];
  return [];
}

function DetailForm({ route }: { route: AppRoute }) {
  return (
    <Card>
      <CardHeader>
        <CardTitle>상세/편집</CardTitle>
        <CardDescription>
          원천 필드와 로컬 변경 필드를 분리하고 저장 전 변경 사유를 확인합니다.
        </CardDescription>
      </CardHeader>
      <CardContent className="grid gap-4 pt-0 md:grid-cols-2">
        {route.fields.slice(0, 7).map((field) => {
          const readOnly =
            route.screenId === "USR-001" &&
            ["교번", "성명", "소속", "직급", "재직상태"].includes(field);
          return (
            <label key={field} className="grid gap-1.5 text-sm font-medium">
              {field}
              <Input
                readOnly={readOnly}
                placeholder={readOnly ? "KORUS 원천 read-only" : field}
              />
            </label>
          );
        })}
        <label className="grid gap-1.5 text-sm font-medium md:col-span-2">
          변경 사유
          <Textarea required placeholder="변경 사유를 입력하세요." />
        </label>
        <div className="flex flex-wrap gap-2 md:col-span-2">
          <Button type="button">저장</Button>
          <Button variant="outline" type="button">
            취소
          </Button>
          <Button variant="destructive" type="button">
            사용중지
          </Button>
        </div>
      </CardContent>
    </Card>
  );
}

export function ManagementPage({ route }: { route: AppRoute }) {
  const [data, setData] = useState<PageData>(null);
  const [state, setState] = useState<"loading" | "empty" | "error" | "success">(
    "loading",
  );
  const [error, setError] = useState("");
  const [reloadKey, setReloadKey] = useState(0);

  useEffect(() => {
    setState("loading");
    setError("");
    api
      .listRoute<PageData>(route.screenId, route.apiPath)
      .then((next) => {
        setData(next);
        setState(itemsFrom(next).length > 0 ? "success" : "empty");
      })
      .catch((err) => {
        setError(err instanceof Error ? err.message : "조회 실패");
        setData(null);
        setState("error");
      });
  }, [route.apiPath, reloadKey]);

  const items = itemsFrom(data);
  return (
    <section className="grid gap-5">
      <div className="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
        <div className="space-y-2">
          <div className="text-sm text-muted-foreground">
            시스템 관리 &gt; {route.group} &gt; {route.title}
          </div>
          <div>
            <p className="text-xs font-semibold uppercase tracking-wider text-primary">
              {route.screenId} · {route.archetype}
            </p>
            <h1 className="text-2xl font-bold tracking-tight md:text-3xl">
              {route.title}
            </h1>
            <p className="mt-2 text-sm text-muted-foreground">
              {route.apiPath} 조회 결과를 기반으로 목록, 상세, 저장 확인 UI
              state를 표시합니다.
            </p>
          </div>
        </div>
        <Button
          type="button"
          onClick={() => setReloadKey((value) => value + 1)}
        >
          조회
        </Button>
      </div>

      <SearchFilterBar
        route={route}
        onSearch={() => setReloadKey((value) => value + 1)}
      />

      {state === "loading" && (
        <div className="grid gap-4 md:grid-cols-2" aria-live="polite">
          <div className="h-48 animate-pulse rounded-xl border bg-muted/60" />
          <div className="h-48 animate-pulse rounded-xl border bg-muted/40" />
        </div>
      )}
      {state === "empty" && (
        <StatePanel
          state="empty"
          message="조회 결과가 없습니다. 검색조건을 초기화하거나 seed 데이터를 확인하세요."
        />
      )}
      {state === "error" && <StatePanel state="error" message={error} />}
      {state === "success" && (
        <StatePanel
          state="success"
          message="조회가 완료되었습니다. 저장 작업은 변경 사유 확인 후 처리됩니다."
        />
      )}

      {route.archetype === "TREE_EDITOR" && (
        <TreeEditorPanel route={route} items={items} />
      )}
      {route.archetype === "PERMISSION_MATRIX" && (
        <PermissionMatrix route={route} />
      )}
      {route.archetype === "EFFECTIVE_PERIOD_FORM" && (
        <EffectivePeriodForm route={route} items={items} />
      )}
      {route.archetype === "SEARCH_LIST_DETAIL" && (
        <div className="grid gap-4 xl:grid-cols-[minmax(0,1.15fr)_minmax(320px,0.85fr)]">
          <ManagementTable route={route} items={items} />
          <DetailForm route={route} />
        </div>
      )}
    </section>
  );
}
