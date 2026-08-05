import React from "react";
import { Link, useNavigate, useSearchParams } from "react-router-dom";
import { ChevronRight, RefreshCcw, Save, Search } from "lucide-react";
import { api, ApiClientError, ApiPage, toQuery } from "../../api/client";
import { FieldControl } from "./FieldControl";
import { SkeletonGrid, StateBlock } from "./StatusBlocks";
import { Row, ScreenConfig } from "../types";

type Props = { screen: ScreenConfig; readonly: boolean };
type ViewState = "loading" | "ready" | "empty" | "error" | "success";

const cleanPayload = (screen: ScreenConfig, row: Row) => {
  const payload: Row = {};
  screen.fields.forEach((field) => {
    if (field.readonly) return;
    const key = field.key;
    const value = row[key];
    if (
      [
        "children",
        "displayName",
        "roleName",
        "approvedByName",
        "menuName",
        "orgName",
        "rankName",
        "positionName",
        "retirementDate",
        "lastSyncedAt",
      ].includes(key)
    )
      return;
    if (value === undefined || value === "") payload[key] = null;
    else payload[key] = value;
  });
  return payload;
};

const roleArray = (value: unknown) => {
  if (Array.isArray(value))
    return value
      .map((item) =>
        typeof item === "string" ? item : String((item as Row).roleCode ?? ""),
      )
      .filter(Boolean);
  if (typeof value === "string")
    return value
      .split(",")
      .map((item) => item.trim())
      .filter(Boolean);
  return [];
};

function TreePreview({ data }: { data: Row[] }) {
  const render = (rows: Row[], level = 0): React.ReactNode =>
    rows.map((row) => (
      <li
        key={String(row.orgCode ?? row.menuId ?? row.codeId)}
        className="py-1"
      >
        <button
          className="rounded-xl px-2 py-1 text-left text-sm font-semibold hover:bg-warmSand"
          style={{ marginLeft: level * 14 }}
          type="button"
        >
          {String(
            row.orgName ??
              row.menuName ??
              row.codeName ??
              row.orgCode ??
              row.menuId,
          )}
        </button>
        {Array.isArray(row.children) && row.children.length > 0 && (
          <ul>{render(row.children as Row[], level + 1)}</ul>
        )}
      </li>
    ));
  return (
    <ul className="max-h-[360px] overflow-auto rounded-[24px] bg-[#f6f6f3] p-3">
      {render(data)}
    </ul>
  );
}

export function ManagementPage({ screen, readonly }: Props) {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const [filters, setFilters] = React.useState<Row>(() => {
    const initial: Row = {};
    screen.filters.forEach((filter) => {
      initial[filter.key] = searchParams.get(filter.key) ?? "";
    });
    return initial;
  });
  const [items, setItems] = React.useState<Row[]>([]);
  const [tree, setTree] = React.useState<Row[]>([]);
  const [selected, setSelected] = React.useState<Row | null>(null);
  const [lastSelected, setLastSelected] = React.useState<Row | null>(null);
  const [state, setState] = React.useState<ViewState>("loading");
  const [message, setMessage] = React.useState("");
  const [fieldErrors, setFieldErrors] = React.useState<Record<string, string>>(
    {},
  );

  const loadTree = React.useCallback(async () => {
    if (!screen.treeEndpoint) return;
    const data = await api<{ items: Row[] }>(screen.treeEndpoint);
    setTree(data.items ?? []);
  }, [screen.treeEndpoint]);

  const selectRow = React.useCallback(
    async (row: Row) => {
      setFieldErrors({});
      if (screen.detailPath) {
        const path = screen.detailPath(row);
        if (path) {
          const detail = await api<Row>(path);
          setSelected(detail);
          setLastSelected(detail);
          return;
        }
      }
      setSelected(row);
      setLastSelected(row);
    },
    [screen],
  );

  const load = React.useCallback(
    async (preferredId?: unknown) => {
      setState("loading");
      setMessage("");
      setFieldErrors({});
      try {
        const page = await api<ApiPage<Row>>(
          `${screen.endpoint}${toQuery(filters)}` as `/api/${string}`,
        );
        setItems(page.items);
        const nextSelected =
          preferredId === undefined
            ? page.items[0]
            : (page.items.find((item) => item[screen.idKey] === preferredId) ??
              page.items[0]);
        if (nextSelected) await selectRow(nextSelected);
        else setSelected(null);
        await loadTree();
        setState(page.items.length ? "ready" : "empty");
      } catch (e) {
        setMessage(e instanceof Error ? e.message : "조회 실패");
        setFieldErrors(e instanceof ApiClientError ? e.fields : {});
        setState(
          e instanceof ApiClientError && e.status === 403 ? "error" : "error",
        );
      }
    },
    [filters, loadTree, screen.endpoint, screen.idKey, selectRow],
  );

  React.useEffect(() => {
    void load();
  }, [screen.route]);

  const startCreate = () => {
    setSelected({ ...(screen.newRecord ?? {}) });
    setLastSelected(null);
    setFieldErrors({});
    setMessage("등록 모드입니다. 필수값을 입력한 뒤 저장하세요.");
    setState("ready");
  };

  const save = async () => {
    if (!selected || readonly) return;
    const isCreate = !selected[screen.idKey] && Boolean(screen.createPath);
    const path = isCreate ? screen.createPath : screen.updatePath?.(selected);
    if (!path) return;
    setState("loading");
    setFieldErrors({});
    try {
      const payload = cleanPayload(screen, selected);
      if (screen.extraAction === "userRoles") {
        await saveUserUsage();
        return;
      } else if (screen.extraAction === "menuPermissions") {
        const targetType = String(
          selected.targetType ?? filters.targetType ?? "",
        );
        const targetId = String(selected.targetId ?? filters.targetId ?? "");
        const permissions = items.map((item) => ({
          menuId: item.menuId,
          permissionLevel:
            item.permissionId === selected.permissionId
              ? selected.permissionLevel
              : item.permissionLevel,
        }));
        await api(path, {
          method: "PUT",
          body: JSON.stringify({
            targetType,
            targetId,
            permissions,
            reason: selected.reason,
          }),
        });
      } else {
        await api<Row>(path, {
          method: isCreate ? "POST" : "PUT",
          body: JSON.stringify(payload),
        });
      }
      setMessage(
        `${screen.title} 저장 후 선택 행을 유지하고 목록과 상세를 다시 조회했습니다.`,
      );
      await load(selected[screen.idKey]);
      setState("success");
    } catch (e) {
      setMessage(e instanceof Error ? e.message : "저장 실패");
      setFieldErrors(e instanceof ApiClientError ? e.fields : {});
      setState("error");
    }
  };

  const saveUserUsage = async () => {
    if (!selected?.userId || readonly) return;
    setState("loading");
    setFieldErrors({});
    try {
      await api<Row>(`/api/users/${selected.userId}/usage`, {
        method: "PATCH",
        body: JSON.stringify({
          systemEnabled: selected.systemEnabled,
          reason: selected.reason,
        }),
      });
      setMessage(
        "사용여부 저장 후 선택 사용자를 유지하고 목록과 상세를 다시 조회했습니다.",
      );
      await load(selected.userId);
      setState("success");
    } catch (e) {
      setMessage(e instanceof Error ? e.message : "사용여부 저장 실패");
      setFieldErrors(e instanceof ApiClientError ? e.fields : {});
      setState("error");
    }
  };

  const saveUserRoles = async () => {
    if (!selected?.userId || readonly) return;
    setState("loading");
    setFieldErrors({});
    try {
      await api<Row>(`/api/users/${selected.userId}/roles`, {
        method: "PUT",
        body: JSON.stringify({
          roleCodes: roleArray(selected.roleCodes ?? selected.roles),
          reason: selected.reason,
        }),
      });
      setMessage(
        "업무 역할 저장 후 선택 사용자를 유지하고 목록과 상세를 다시 조회했습니다.",
      );
      await load(selected.userId);
      setState("success");
    } catch (e) {
      setMessage(e instanceof Error ? e.message : "업무 역할 저장 실패");
      setFieldErrors(e instanceof ApiClientError ? e.fields : {});
      setState("error");
    }
  };

  const revoke = async () => {
    if (!selected?.assignmentId || readonly) return;
    if (
      !window.confirm(
        "선택한 역할 배정을 회수하시겠습니까? row는 삭제하지 않고 REVOKED 상태로 보존됩니다.",
      )
    )
      return;
    setState("loading");
    try {
      await api(`/api/user-roles/${selected.assignmentId}`, {
        method: "DELETE",
      });
      setMessage("역할 회수 후 선택 행을 유지하고 목록을 다시 조회했습니다.");
      await load(selected.assignmentId);
      setState("success");
    } catch (e) {
      setMessage(e instanceof Error ? e.message : "회수 실패");
      setFieldErrors(e instanceof ApiClientError ? e.fields : {});
      setState("error");
    }
  };

  const reorder = async () => {
    if (readonly) return;
    setState("loading");
    try {
      await api("/api/menus/reorder", {
        method: "PUT",
        body: JSON.stringify({
          items: items.map((item) => ({
            menuId: item.menuId,
            displayOrder: item.displayOrder,
            parentMenuId: item.parentMenuId,
          })),
        }),
      });
      setMessage(
        "표시순서를 저장하고 선택 메뉴를 유지한 채 메뉴 계층을 다시 조회했습니다.",
      );
      await load(selected?.[screen.idKey]);
      setState("success");
    } catch (e) {
      setMessage(e instanceof Error ? e.message : "재정렬 실패");
      setFieldErrors(e instanceof ApiClientError ? e.fields : {});
      setState("error");
    }
  };

  return (
    <article>
      <header className="py-6 text-center">
        <p className="text-sm font-bold text-oliveMuted">{screen.menuPath}</p>
        <h1 className="mt-2 text-4xl font-black tracking-[-0.04em] md:text-[42px]">
          {screen.title}
        </h1>
        <p className="mx-auto mt-3 max-w-3xl text-sm leading-6 text-oliveMuted">
          {screen.goal}
        </p>
      </header>

      <section
        className="mb-6 rounded-[32px] bg-[#f6f6f3] p-4 md:p-6"
        aria-label="검색 조건"
      >
        <div className="grid grid-cols-1 gap-3 md:grid-cols-3 xl:grid-cols-4">
          {screen.filters.map((filter) => (
            <label key={filter.key} className="text-sm font-semibold">
              {filter.label}
              {filter.type === "select" ? (
                <select
                  className="pin-input mt-2"
                  value={String(filters[filter.key] ?? "")}
                  onChange={(event) =>
                    setFilters({ ...filters, [filter.key]: event.target.value })
                  }
                >
                  {(filter.options ?? []).map((option) => (
                    <option key={option} value={option}>
                      {option || "전체"}
                    </option>
                  ))}
                </select>
              ) : (
                <input
                  className="pin-input mt-2"
                  type={filter.type === "date" ? "date" : "text"}
                  value={String(filters[filter.key] ?? "")}
                  onChange={(event) =>
                    setFilters({ ...filters, [filter.key]: event.target.value })
                  }
                  onKeyDown={(event) => event.key === "Enter" && load()}
                />
              )}
            </label>
          ))}
        </div>
        <div className="mt-4 flex flex-wrap gap-2">
          <button
            type="button"
            className="pin-button-primary h-12"
            onClick={() => void load()}
          >
            <Search className="mr-2 h-4 w-4" />
            조회
          </button>
          {screen.createPath && (
            <button
              type="button"
              className="pin-button-secondary h-12"
              onClick={startCreate}
            >
              {screen.title.replace(" 관리", "")} 등록
            </button>
          )}
          {screen.treeEndpoint && (
            <button
              type="button"
              className="pin-button-secondary h-12"
              onClick={loadTree}
            >
              <RefreshCcw className="mr-2 h-4 w-4" />
              계층 새로고침
            </button>
          )}
        </div>
      </section>

      {readonly && (
        <div className="mb-6">
          <StateBlock
            tone="permission"
            label="읽기 전용 권한"
            detail="R04/R08은 조회 가능하지만 저장·회수·재정렬 버튼은 비활성화됩니다."
          />
        </div>
      )}
      {state === "loading" && (
        <div className="mb-6">
          <SkeletonGrid />
        </div>
      )}
      {state === "empty" && (
        <div className="mb-6">
          <StateBlock
            label="검색 결과가 없습니다"
            detail="조회 조건을 조정하면 상세/편집 폼이 다시 활성화됩니다."
          />
        </div>
      )}
      {state === "error" && (
        <div className="mb-6">
          <StateBlock tone="error" label="오류 안내" detail={message} />
        </div>
      )}
      {state === "success" && (
        <div className="mb-6">
          <StateBlock tone="success" label="저장 완료" detail={message} />
        </div>
      )}

      <div className="grid grid-cols-1 gap-6 xl:grid-cols-[minmax(0,1.35fr)_minmax(360px,0.75fr)]">
        <section className="space-y-4">
          {tree.length > 0 && <TreePreview data={tree} />}
          <div className="overflow-auto rounded-[32px] bg-white shadow-[0_1px_20px_rgba(0,0,0,0.08)]">
            <table className="w-full min-w-[780px] text-sm">
              <thead className="bg-warmSand">
                <tr>
                  {screen.columns.map((column) => (
                    <th key={column.key} className="p-3 text-left font-black">
                      {column.label}
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {items.map((row) => {
                  const active =
                    selected?.[screen.idKey] &&
                    selected?.[screen.idKey] === row[screen.idKey];
                  return (
                    <tr
                      key={String(row[screen.idKey] ?? JSON.stringify(row))}
                      onClick={() => selectRow(row)}
                      className={`cursor-pointer border-t border-[#e5e5e0] transition-colors hover:bg-[#f6f6f3] ${active ? "bg-[#fff0f2]" : ""}`}
                    >
                      {screen.columns.map((column) => (
                        <td
                          key={column.key}
                          className="max-w-[240px] truncate p-3 whitespace-nowrap"
                        >
                          {String(row[column.key] ?? "")}
                        </td>
                      ))}
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </section>

        <form
          className="pin-card space-y-4 p-6"
          onSubmit={(event) => {
            event.preventDefault();
            void save();
          }}
        >
          <div className="flex items-start justify-between gap-3">
            <div>
              <h2 className="text-xl font-black">상세 / 편집</h2>
              <p className="mt-1 text-xs leading-5 text-oliveMuted">
                원천 조회 필드는 readonly로 유지하고, 계약에 명시된 로컬 관리
                필드만 payload에 포함합니다.
              </p>
            </div>
            <span className="rounded-xl bg-[#f6f6f3] px-2 py-1 text-xs font-bold text-oliveMuted">
              {screen.archetype}
            </span>
          </div>
          {screen.fields.map((field) => (
            <FieldControl
              key={field.key}
              field={field}
              row={selected ?? {}}
              disabled={readonly || !selected}
              immutableLocked={Boolean(
                field.immutable && selected?.[screen.idKey],
              )}
              error={fieldErrors[field.key]}
              onChange={(key, value) =>
                setSelected({ ...(selected ?? {}), [key]: value })
              }
            />
          ))}
          <div className="flex flex-wrap gap-2 pt-2">
            <button
              type="submit"
              disabled={
                readonly || !selected || screen.extraAction === "userRoles"
              }
              className="pin-button-primary h-12"
            >
              <Save className="mr-2 h-4 w-4" />
              저장
            </button>
            {screen.extraAction === "userRoles" && (
              <>
                <button
                  type="button"
                  disabled={readonly || !selected?.userId}
                  className="pin-button-primary h-12"
                  onClick={saveUserUsage}
                >
                  사용여부 저장
                </button>
                <button
                  type="button"
                  disabled={readonly || !selected?.userId}
                  className="pin-button-secondary h-12"
                  onClick={saveUserRoles}
                >
                  업무 역할 저장
                </button>
              </>
            )}
            {screen.extraAction === "revokeUserRole" && (
              <button
                type="button"
                disabled={readonly || !selected?.assignmentId}
                className="pin-button-secondary h-12"
                onClick={revoke}
              >
                회수
              </button>
            )}
            {screen.extraAction === "menuReorder" && (
              <button
                type="button"
                disabled={readonly || !items.length}
                className="pin-button-secondary h-12"
                onClick={reorder}
              >
                표시순서 저장
              </button>
            )}
            {screen.extraAction === "codeGroupDetailLink" &&
              (selected?.groupId ? (
                <Link
                  className="pin-button-secondary h-12"
                  to={`/system/code-details?groupId=${selected.groupId}`}
                >
                  상세코드 목록
                  <ChevronRight className="ml-1 h-4 w-4" />
                </Link>
              ) : (
                <span className="text-sm text-oliveMuted">
                  코드그룹 선택 후 이동 가능
                </span>
              ))}
            <button
              type="button"
              className="pin-button-secondary h-12"
              onClick={() => {
                setSelected(lastSelected);
                setFieldErrors({});
              }}
            >
              취소
            </button>
          </div>
        </form>
      </div>
    </article>
  );
}
