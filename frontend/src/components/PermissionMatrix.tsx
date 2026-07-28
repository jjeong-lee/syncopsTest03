import { AppRoute, adminRoutes } from "../app/routes";
import {
  Button,
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
  Input,
} from "./ui";
import { ConfirmDialog } from "./ConfirmDialog";

export function PermissionMatrix({ route }: { route: AppRoute }) {
  return (
    <Card>
      <CardHeader className="grid gap-4 lg:grid-cols-[1fr_auto] lg:items-end">
        <div>
          <CardTitle>권한 매트릭스</CardTitle>
          <CardDescription>
            UI 메뉴 노출과 서버 접근통제를 같은 값으로 저장합니다.
          </CardDescription>
        </div>
        <div className="grid gap-2 sm:grid-cols-2">
          <label className="grid gap-1.5 text-sm font-medium">
            대상 유형
            <Input defaultValue="ROLE" />
          </label>
          <label className="grid gap-1.5 text-sm font-medium">
            대상ID
            <Input defaultValue="R09" />
          </label>
        </div>
      </CardHeader>
      <CardContent className="space-y-4 pt-0">
        <div className="overflow-hidden rounded-md border">
          <table className="w-full min-w-[680px] text-sm">
            <thead className="bg-muted/50">
              <tr className="border-b">
                <th className="h-10 px-3 text-left font-medium text-muted-foreground">
                  메뉴 또는 화면
                </th>
                <th className="h-10 px-3 text-left font-medium text-muted-foreground">
                  접근허용
                </th>
                <th className="h-10 px-3 text-left font-medium text-muted-foreground">
                  서버차단
                </th>
                <th className="h-10 px-3 text-left font-medium text-muted-foreground">
                  변경
                </th>
              </tr>
            </thead>
            <tbody>
              {adminRoutes.map((item) => (
                <tr
                  key={item.path}
                  className="border-b transition-colors duration-200 hover:bg-muted/50"
                >
                  <td className="p-3">
                    {item.group} &gt; {item.title}
                  </td>
                  <td className="p-3">
                    <input
                      aria-label={`${item.title} 접근허용`}
                      type="checkbox"
                      defaultChecked
                      className="size-4 rounded border-input"
                    />
                  </td>
                  <td className="p-3 text-muted-foreground">403 적용</td>
                  <td className="p-3">-</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        <div className="flex flex-wrap gap-2">
          <Button variant="outline" type="button">
            선택 행 일괄 허용
          </Button>
          <Button variant="outline" type="button">
            선택 행 일괄 차단
          </Button>
        </div>
        <ConfirmDialog
          title="변경 요약 확인"
          description={`${route.title} 저장 전 변경된 cell과 접근 차단 영향을 확인합니다.`}
          confirmText="저장"
        />
      </CardContent>
    </Card>
  );
}
