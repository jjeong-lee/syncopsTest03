import { useMemo, useState, type ReactNode } from "react";
import { ApiRequestError, apiRequest } from "../../shared/api/client";

export type MenuSummary = {
  menuId: string;
  menuName: string;
  parentMenuId: string | null;
  displayOrder: number;
  screenId: string | null;
  url: string | null;
  icon: string | null;
  businessCategory: string | null;
  description: string | null;
  useYn: string;
};

type SearchForm = { parentMenuId: string; useYn: string };
type StructureForm = {
  parentMenuId: string;
  displayOrder: string;
  reason: string;
};

const emptySearch: SearchForm = { parentMenuId: "", useYn: "" };
const emptyStructure: StructureForm = {
  parentMenuId: "",
  displayOrder: "",
  reason: "",
};

function menuQueryPath(search: SearchForm): `/api/${string}` {
  const params = new URLSearchParams();
  if (search.parentMenuId) params.set("parentMenuId", search.parentMenuId);
  if (search.useYn) params.set("useYn", search.useYn);
  const query = params.toString();
  return `/api/menus${query ? `?${query}` : ""}`;
}

function childMenus(
  menus: MenuSummary[],
  parentMenuId: string | null,
): MenuSummary[] {
  return menus
    .filter((menu) => menu.parentMenuId === parentMenuId)
    .sort((left, right) => left.displayOrder - right.displayOrder);
}

export function MenuStructureManagementPage() {
  const [search, setSearch] = useState<SearchForm>(emptySearch);
  const [menus, setMenus] = useState<MenuSummary[]>([]);
  const [selectedMenu, setSelectedMenu] = useState<MenuSummary | null>(null);
  const [form, setForm] = useState<StructureForm>(emptyStructure);
  const [state, setState] = useState<
    "idle" | "loading" | "empty" | "error" | "permission" | "success"
  >("idle");
  const [message, setMessage] = useState("");
  const menuById = useMemo(
    () => new Map(menus.map((menu) => [menu.menuId, menu])),
    [menus],
  );

  const selectMenu = (menu: MenuSummary | null, clearMessage = true) => {
    setSelectedMenu(menu);
    setForm(
      menu
        ? {
            parentMenuId: menu.parentMenuId ?? "",
            displayOrder: String(menu.displayOrder),
            reason: "",
          }
        : emptyStructure,
    );
    if (clearMessage) setMessage("");
  };

  const isPermissionError = (error: unknown) =>
    error instanceof ApiRequestError &&
    (error.status === 401 || error.status === 403);

  const loadMenus = async (preserveSelection = false, showSuccess = false) => {
    setState("loading");
    if (!preserveSelection) {
      selectMenu(null);
      setMessage("");
    }
    try {
      const response = await apiRequest<MenuSummary[]>(menuQueryPath(search));
      setMenus(response.data);
      if (preserveSelection && selectedMenu) {
        selectMenu(
          response.data.find((menu) => menu.menuId === selectedMenu.menuId) ??
            null,
          false,
        );
      }
      setState(response.data.length === 0 ? "empty" : "success");
      if (showSuccess)
        setMessage("저장 후 메뉴 계층과 표시순서를 다시 조회했습니다.");
    } catch (error) {
      const permissionDenied = isPermissionError(error);
      setState(permissionDenied ? "permission" : "error");
      setMessage(
        permissionDenied
          ? "권한이 없습니다."
          : "메뉴 계층 조회 또는 저장에 실패했습니다.",
      );
    }
  };

  const saveParent = async () => {
    if (!selectedMenu) return;
    setState("loading");
    setMessage("");
    try {
      await apiRequest<null>("/api/menus", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          menuName: selectedMenu.menuName,
          parentMenuId: form.parentMenuId || null,
          displayOrder: Number(form.displayOrder),
          screenId: selectedMenu.screenId,
          url: selectedMenu.url,
          icon: selectedMenu.icon,
          businessCategory: selectedMenu.businessCategory,
          description: selectedMenu.description,
          useYn: selectedMenu.useYn,
          ...(form.reason ? { reason: form.reason } : {}),
        }),
      });
      await loadMenus(true, true);
    } catch (error) {
      const permissionDenied = isPermissionError(error);
      setState(permissionDenied ? "permission" : "error");
      setMessage(
        permissionDenied
          ? "권한이 없습니다."
          : "부모메뉴를 저장하지 못했습니다.",
      );
    }
  };

  const saveOrder = async () => {
    if (!selectedMenu) return;
    setState("loading");
    setMessage("");
    try {
      await apiRequest<null>("/api/menus/order", {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          menuId: selectedMenu.menuId,
          displayOrder: Number(form.displayOrder),
          ...(form.reason ? { reason: form.reason } : {}),
        }),
      });
      await loadMenus(true, true);
    } catch (error) {
      const permissionDenied = isPermissionError(error);
      setState(permissionDenied ? "permission" : "error");
      setMessage(
        permissionDenied
          ? "권한이 없습니다."
          : "표시순서를 저장하지 못했습니다.",
      );
    }
  };

  const renderTree = (parentMenuId: string | null, level = 0): ReactNode[] =>
    childMenus(menus, parentMenuId).flatMap((menu) => [
      <li
        key={menu.menuId}
        className={
          selectedMenu?.menuId === menu.menuId ? "selected-tree-node" : ""
        }
        style={{ paddingLeft: `${level * 20}px` }}
      >
        <button
          type="button"
          className="row-link"
          onClick={() => selectMenu(menu)}
        >
          {menu.menuName}
        </button>
        <span> · 표시순서 {menu.displayOrder}</span>
      </li>,
      ...renderTree(menu.menuId, level + 1),
    ]);

  if (state === "permission") {
    return (
      <section
        className="menu-structure-management menu-structure-state"
        aria-live="polite"
      >
        <h1>메뉴 구조 관리</h1>
        <p>권한이 없습니다.</p>
      </section>
    );
  }

  return (
    <section
      className="menu-structure-management"
      aria-labelledby="menu-structure-management-title"
    >
      <p className="breadcrumb">
        시스템 관리 &gt; 메뉴 관리 &gt; 메뉴 구조 관리
      </p>
      <h1 id="menu-structure-management-title">메뉴 구조 관리</h1>
      <section className="menu-structure-search" aria-label="메뉴 계층 조회">
        <label>
          조회 부모메뉴
          <select
            aria-label="조회 부모메뉴"
            value={search.parentMenuId}
            onChange={(event) =>
              setSearch({ ...search, parentMenuId: event.target.value })
            }
          >
            <option value="">전체</option>
            {menus.map((menu) => (
              <option key={menu.menuId} value={menu.menuId}>
                {menu.menuName}
              </option>
            ))}
          </select>
        </label>
        <label>
          사용여부
          <select
            aria-label="사용여부"
            value={search.useYn}
            onChange={(event) =>
              setSearch({ ...search, useYn: event.target.value })
            }
          >
            <option value="">전체</option>
            <option value="Y">Y</option>
            <option value="N">N</option>
          </select>
        </label>
        <button
          type="button"
          className="primary-action"
          onClick={() => void loadMenus()}
        >
          조회
        </button>
      </section>

      {message && state === "success" && (
        <p className="success-message" role="status">
          {message}
        </p>
      )}
      {state === "loading" && (
        <p className="loading-message" aria-live="polite">
          메뉴 계층을 불러오거나 저장하는 중입니다.
        </p>
      )}
      {state === "empty" && (
        <p className="empty-message">조회된 메뉴가 없습니다.</p>
      )}
      {state === "error" && (
        <p className="error-message">
          {message}{" "}
          <button
            type="button"
            className="text-action"
            onClick={() => void loadMenus()}
          >
            다시 시도
          </button>
        </p>
      )}

      {state === "success" && (
        <div className="menu-structure-editor">
          <section className="menu-tree-panel" aria-label="메뉴 계층">
            <h2>메뉴 계층</h2>
            <ul>{renderTree(null)}</ul>
          </section>
          <section className="menu-structure-form">
            <h2>선택 메뉴 구조 편집</h2>
            {selectedMenu ? (
              <>
                <p>
                  <strong>{selectedMenu.menuName}</strong>{" "}
                  <span>
                    {form.parentMenuId
                      ? `${menuById.get(form.parentMenuId)?.menuName ?? form.parentMenuId} 아래`
                      : "최상위 메뉴"}
                  </span>
                </p>
                <label>
                  부모메뉴
                  <select
                    aria-label="부모메뉴"
                    value={form.parentMenuId}
                    onChange={(event) =>
                      setForm({ ...form, parentMenuId: event.target.value })
                    }
                  >
                    <option value="">없음</option>
                    {menus.map((menu) => (
                      <option key={menu.menuId} value={menu.menuId}>
                        {menu.menuName}
                      </option>
                    ))}
                  </select>
                </label>
                <label>
                  표시순서
                  <input
                    aria-label="표시순서"
                    type="number"
                    value={form.displayOrder}
                    onChange={(event) =>
                      setForm({ ...form, displayOrder: event.target.value })
                    }
                  />
                </label>
                <label>
                  사유
                  <textarea
                    aria-label="사유"
                    value={form.reason}
                    onChange={(event) =>
                      setForm({ ...form, reason: event.target.value })
                    }
                  />
                </label>
                <div className="form-actions">
                  <button
                    type="button"
                    className="text-action"
                    onClick={() => selectMenu(selectedMenu)}
                  >
                    취소
                  </button>
                  <button
                    type="button"
                    className="primary-action"
                    onClick={() => void saveParent()}
                  >
                    부모 변경 저장
                  </button>
                </div>
                <div className="form-actions">
                  <span>표시순서: {form.displayOrder}</span>
                  <button
                    type="button"
                    className="primary-action"
                    onClick={() => void saveOrder()}
                  >
                    순서 저장
                  </button>
                </div>
              </>
            ) : (
              <p>메뉴 트리에서 편집할 메뉴를 선택하세요.</p>
            )}
          </section>
        </div>
      )}
    </section>
  );
}
