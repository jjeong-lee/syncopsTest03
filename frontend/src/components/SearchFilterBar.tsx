import { AppRoute } from "../app/routes";
import { Button, Card, Input } from "./ui";

export function SearchFilterBar({
  route,
  onSearch,
}: {
  route: AppRoute;
  onSearch: () => void;
}) {
  return (
    <Card className="p-4">
      <div
        className="grid gap-3 md:grid-cols-2 xl:grid-cols-4"
        role="search"
        aria-label={`${route.title} 검색조건`}
      >
        {route.fields.slice(0, 6).map((field) => (
          <label key={field} className="grid gap-1.5 text-sm font-medium">
            {field}
            <Input placeholder={`${field} 검색`} />
          </label>
        ))}
        <div className="flex items-end gap-2 md:col-span-2 xl:col-span-2 xl:justify-end">
          <Button type="button" onClick={onSearch}>
            조회
          </Button>
          <Button type="button" variant="outline">
            초기화
          </Button>
        </div>
      </div>
    </Card>
  );
}
