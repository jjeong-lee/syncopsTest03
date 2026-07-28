import { AppRoute } from "../app/routes";
import {
  Button,
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "./ui";
import { StatePanel } from "./StatePanel";

export function ManagementTable({
  route,
  items,
}: {
  route: AppRoute;
  items: Record<string, unknown>[];
}) {
  return (
    <Card className="overflow-hidden">
      <CardHeader className="grid-cols-[1fr_auto] items-start">
        <div>
          <CardTitle>목록</CardTitle>
          <CardDescription>
            기본 정렬과 페이지 단위 조회 결과를 표시합니다.
          </CardDescription>
        </div>
        <Button variant="outline" type="button">
          열 설정
        </Button>
      </CardHeader>
      <CardContent className="pt-0">
        <div className="overflow-hidden rounded-md border">
          <table className="w-full min-w-[720px] caption-bottom text-sm">
            <thead className="bg-muted/50">
              <tr className="border-b transition-colors">
                <th className="h-10 px-3 text-left align-middle font-medium text-muted-foreground">
                  선택
                </th>
                {route.fields.slice(0, 5).map((field) => (
                  <th
                    key={field}
                    className="h-10 px-3 text-left align-middle font-medium text-muted-foreground"
                  >
                    {field}
                  </th>
                ))}
                <th className="h-10 px-3 text-left align-middle font-medium text-muted-foreground">
                  상세
                </th>
              </tr>
            </thead>
            <tbody>
              {items.length === 0 ? (
                <tr>
                  <td
                    className="h-28 px-3 text-center"
                    colSpan={route.fields.slice(0, 5).length + 2}
                  >
                    <StatePanel
                      state="empty"
                      message={`${route.title} 조회 결과가 없습니다.`}
                      compact
                    />
                  </td>
                </tr>
              ) : (
                items.slice(0, 20).map((item, rowIndex) => (
                  <tr
                    key={rowIndex}
                    className="border-b transition-colors duration-200 hover:bg-muted/50"
                  >
                    <td className="p-3 align-middle">
                      <input
                        aria-label={`${rowIndex + 1}행 선택`}
                        type="checkbox"
                        className="size-4 rounded border-input"
                      />
                    </td>
                    {route.fields.slice(0, 5).map((field, fieldIndex) => (
                      <td key={field} className="p-3 align-middle">
                        {String(
                          Object.values(item)[fieldIndex] ??
                            Object.values(item)[0] ??
                            "-",
                        )}
                      </td>
                    ))}
                    <td className="p-3 align-middle">
                      <Button
                        variant="ghost"
                        type="button"
                        className="h-8 px-2"
                      >
                        열기
                      </Button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
        <div className="mt-4 flex flex-col-reverse items-center justify-between gap-3 text-sm text-muted-foreground sm:flex-row">
          <span>총 {items.length}건 · 페이지 20건</span>
          <div className="flex gap-2">
            <Button variant="outline" type="button" className="h-8 px-3">
              이전
            </Button>
            <Button variant="outline" type="button" className="h-8 px-3">
              다음
            </Button>
          </div>
        </div>
      </CardContent>
    </Card>
  );
}
