import { AppRoute } from "../app/routes";
import {
  Button,
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
  Input,
  Textarea,
} from "./ui";
import { ConfirmDialog } from "./ConfirmDialog";

export function TreeEditorPanel({
  route,
  items,
}: {
  route: AppRoute;
  items: Record<string, unknown>[];
}) {
  return (
    <div className="grid gap-4 xl:grid-cols-[minmax(260px,0.85fr)_minmax(0,1.15fr)]">
      <Card>
        <CardHeader>
          <CardTitle>
            {route.screenId === "ORG-001" ? "조직 계층 트리" : "메뉴 계층 트리"}
          </CardTitle>
          <CardDescription>
            노드를 선택해 상위 관계와 표시 순서를 확인합니다.
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-2 pt-0">
          {(items.length ? items.slice(0, 8) : route.fields.slice(0, 4)).map(
            (item, index) => (
              <button
                key={index}
                type="button"
                className="flex w-full items-center gap-2 rounded-md px-3 py-2 text-left text-sm transition-colors duration-200 hover:bg-accent focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
              >
                <span className="size-2 rounded-full bg-primary" />
                <span>
                  {typeof item === "string"
                    ? item
                    : String(
                        Object.values(item)[0] ?? `${route.title} ${index + 1}`,
                      )}
                </span>
              </button>
            ),
          )}
        </CardContent>
      </Card>
      <Card>
        <CardHeader>
          <CardTitle>선택 노드 상세</CardTitle>
          <CardDescription>
            cycle, 중복 순서, 적용기간 검증 후 저장합니다.
          </CardDescription>
        </CardHeader>
        <CardContent className="grid gap-4 pt-0 md:grid-cols-2">
          {route.fields.slice(0, 6).map((field) => (
            <label key={field} className="grid gap-1.5 text-sm font-medium">
              {field}
              <Input
                placeholder={field}
                readOnly={field.includes("read-only")}
              />
            </label>
          ))}
          <label className="grid gap-1.5 text-sm font-medium md:col-span-2">
            변경 사유
            <Textarea required placeholder="변경 사유를 입력하세요." />
          </label>
          <div className="md:col-span-2">
            <ConfirmDialog
              title="저장 전 확인"
              description="상위 관계와 적용기간 충돌 여부를 확인한 후 저장합니다."
              confirmText="관계 저장"
            />
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
