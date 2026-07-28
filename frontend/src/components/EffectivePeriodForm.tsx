import { AppRoute } from "../app/routes";
import {
  Badge,
  Button,
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
  Input,
  Textarea,
} from "./ui";
import { ManagementTable } from "./ManagementTable";

export function EffectivePeriodForm({
  route,
  items,
}: {
  route: AppRoute;
  items: Record<string, unknown>[];
}) {
  return (
    <div className="grid gap-4 xl:grid-cols-[minmax(0,1.15fr)_minmax(320px,0.85fr)]">
      <ManagementTable route={route} items={items} />
      <Card>
        <CardHeader>
          <div className="flex items-start justify-between gap-3">
            <div>
              <CardTitle>
                {route.screenId === "UROLE-001"
                  ? "역할 부여/회수"
                  : "상세코드 등록/수정"}
              </CardTitle>
              <CardDescription>
                기간 충돌과 종료일 검증 후 저장합니다.
              </CardDescription>
            </div>
            <Badge className="border-emerald-200 bg-emerald-50 text-emerald-700">
              ACTIVE
            </Badge>
          </div>
        </CardHeader>
        <CardContent className="grid gap-4 pt-0">
          {route.fields.slice(0, 7).map((field) => (
            <label key={field} className="grid gap-1.5 text-sm font-medium">
              {field}
              <Input placeholder={field} />
            </label>
          ))}
          <label className="grid gap-1.5 text-sm font-medium">
            변경 사유
            <Textarea required placeholder="변경 사유를 입력하세요." />
          </label>
          <div className="flex flex-wrap gap-2">
            <Button type="button">저장</Button>
            <Button variant="destructive" type="button">
              회수/사용중지
            </Button>
            <Button variant="outline" type="button">
              취소
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
