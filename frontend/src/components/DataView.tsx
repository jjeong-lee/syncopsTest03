import {
  ArrowRight,
  CheckCircle2,
  FileJson,
  GitBranch,
  Route,
  Shield,
  UsersRound,
} from "lucide-react";
import { Link } from "react-router-dom";
import type { Page, Role, ScreenKind, User } from "../types";
import { Badge, Card } from "./ui";

type UnknownRecord = Record<string, unknown>;

function asRows(data: unknown) {
  if (Array.isArray(data)) return data;
  if (
    data &&
    typeof data === "object" &&
    Array.isArray((data as Page<unknown>).content)
  )
    return (data as Page<unknown>).content;
  return data ? [data] : [];
}

function text(value: unknown) {
  if (value === null || value === undefined || value === "") return "-";
  if (Array.isArray(value)) return value.join(", ");
  if (typeof value === "object") return JSON.stringify(value);
  return String(value);
}

function GenericTable({
  rows,
  columns,
}: {
  rows: UnknownRecord[];
  columns: string[];
}) {
  return (
    <div className="overflow-hidden rounded-md border border-slate-200">
      <table className="w-full min-w-[48rem] caption-bottom text-sm">
        <thead className="bg-slate-50">
          <tr className="border-b border-slate-200">
            {columns.map((column) => (
              <th
                key={column}
                className="h-10 px-3 text-left align-middle text-xs font-semibold uppercase tracking-wide text-slate-500"
              >
                {column}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {rows.map((row, index) => (
            <tr
              key={String(
                row.id ??
                  row.userId ??
                  row.roleCode ??
                  row.groupId ??
                  row.menuId ??
                  index,
              )}
              className="border-b border-slate-100 transition-colors duration-200 hover:bg-slate-50"
            >
              {columns.map((column) => (
                <td key={column} className="p-3 align-middle text-slate-700">
                  {text(row[column])}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

function UsersView({ rows }: { rows: User[] }) {
  return (
    <GenericTable
      rows={rows as unknown as UnknownRecord[]}
      columns={[
        "staffNo",
        "staffName",
        "organizationName",
        "rankName",
        "employmentStatus",
        "roleCodes",
        "positionName",
        "retirementDate",
        "lastSyncedAt",
        "systemUseYn",
      ]}
    />
  );
}

function RolesView({ rows }: { rows: Role[] }) {
  return (
    <GenericTable
      rows={rows as unknown as UnknownRecord[]}
      columns={[
        "roleCode",
        "roleName",
        "purpose",
        "grantCriteria",
        "defaultDataScope",
        "useYn",
      ]}
    />
  );
}

function DashboardView({ row }: { row: UnknownRecord }) {
  const status = text(row.status ?? row.health ?? row.backend ?? "UP");
  return (
    <div className="grid gap-4 lg:grid-cols-7">
      <Card className="gap-4 p-6 lg:col-span-4">
        <div className="flex items-start justify-between gap-4">
          <div>
            <p className="text-sm font-medium text-slate-500">
              backend getHealth
            </p>
            <h2 className="mt-2 text-3xl font-bold tracking-tight">{status}</h2>
            <p className="mt-2 text-sm text-slate-500">
              /api/health 상대경로 응답 기준으로 표시합니다.
            </p>
          </div>
          <Badge tone={status.toUpperCase() === "UP" ? "success" : "warning"}>
            {status}
          </Badge>
        </div>
        <div className="mt-6 grid gap-3 sm:grid-cols-3">
          {["401 redirect", "403 panel", "R09 menu seed"].map((label) => (
            <div
              key={label}
              className="rounded-lg border border-slate-200 bg-slate-50 p-4"
            >
              <CheckCircle2 className="mb-3 size-5 text-emerald-600" />
              <p className="text-sm font-semibold">{label}</p>
            </div>
          ))}
        </div>
      </Card>
      <Card className="p-6 lg:col-span-3">
        <h2 className="text-base font-semibold">Quick links</h2>
        <div className="mt-4 grid gap-2">
          {[
            ["사용자 관리", "/system/users"],
            ["조직 관리", "/system/organizations"],
            ["역할 관리", "/system/roles"],
            ["메뉴 권한 관리", "/system/menu-permissions"],
            ["공통코드 관리", "/system/code-groups"],
          ].map(([label, to]) => (
            <Link
              key={to}
              className="flex items-center justify-between rounded-md border border-slate-200 px-3 py-2 text-sm font-medium transition-all duration-200 hover:bg-slate-50"
              to={to}
            >
              {label}
              <ArrowRight className="size-4 text-slate-400" />
            </Link>
          ))}
        </div>
      </Card>
    </div>
  );
}

function CardGrid({ rows, kind }: { rows: UnknownRecord[]; kind: ScreenKind }) {
  const iconByKind: Partial<Record<ScreenKind, typeof GitBranch>> = {
    organizations: GitBranch,
    menuStructure: GitBranch,
    menuInfo: Route,
    menuPermissions: Shield,
    userRoles: UsersRound,
    codeGroups: FileJson,
    detailCodes: GitBranch,
  };
  const Icon = iconByKind[kind] ?? FileJson;
  return (
    <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
      {rows.map((row, index) => (
        <Card
          key={String(
            row.id ??
              row.menuId ??
              row.organizationId ??
              row.groupId ??
              row.codeValue ??
              index,
          )}
          className="p-5 transition-all duration-200 hover:-translate-y-0.5 hover:shadow-md"
        >
          <div className="flex items-start gap-3">
            <div className="rounded-lg bg-slate-100 p-2 text-slate-600">
              <Icon className="size-4" />
            </div>
            <div className="min-w-0 flex-1">
              <h3 className="truncate text-sm font-semibold text-slate-950">
                {text(
                  row.menuName ??
                    row.organizationName ??
                    row.groupName ??
                    row.codeName ??
                    row.roleName ??
                    row.name ??
                    row.id ??
                    index,
                )}
              </h3>
              <p className="mt-1 line-clamp-2 text-xs leading-5 text-slate-500">
                {text(
                  row.description ??
                    row.url ??
                    row.screenId ??
                    row.parentMenuId ??
                    row.parentDetailCodeId ??
                    row.status ??
                    row.useYn,
                )}
              </p>
            </div>
            <Badge
              tone={
                text(row.useYn ?? row.status).includes("N")
                  ? "muted"
                  : "default"
              }
            >
              {text(row.useYn ?? row.status ?? "ACTIVE")}
            </Badge>
          </div>
          <pre className="mt-4 max-h-52 overflow-auto rounded-md bg-slate-950 p-3 text-xs leading-5 text-slate-100">
            {JSON.stringify(row, null, 2)}
          </pre>
        </Card>
      ))}
    </div>
  );
}

export function DataView({ kind, data }: { kind: ScreenKind; data: unknown }) {
  const rows = asRows(data) as UnknownRecord[];
  if (kind === "dashboard")
    return <DashboardView row={(rows[0] ?? {}) as UnknownRecord} />;
  if (kind === "users") return <UsersView rows={rows as unknown as User[]} />;
  if (kind === "roles") return <RolesView rows={rows as unknown as Role[]} />;
  if (kind === "menuPermissions")
    return (
      <GenericTable
        rows={rows}
        columns={[
          "menuId",
          "menuName",
          "targetType",
          "targetId",
          "accessDecision",
          "apiAccessDecision",
          "effectiveDecision",
        ]}
      />
    );
  if (kind === "codeGroups")
    return (
      <GenericTable
        rows={rows}
        columns={[
          "groupId",
          "groupName",
          "description",
          "managingDepartment",
          "useYn",
        ]}
      />
    );
  return <CardGrid rows={rows} kind={kind} />;
}
