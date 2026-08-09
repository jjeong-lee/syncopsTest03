import { useState } from "react";
import { ApiRequestError, apiRequest } from "../../shared/api/client";
import type { MenuSummary } from "./MenuStructureManagementPage";

type SearchForm = { parentMenuId: string; useYn: string };
type MenuForm = {
  menuName: string;
  parentMenuId: string;
  displayOrder: string;
  screenId: string;
  url: string;
  icon: string;
  businessCategory: string;
  description: string;
  useYn: string;
  reason: string;
};

const emptySearch: SearchForm = { parentMenuId: "", useYn: "" };
const emptyForm: MenuForm = {
  menuName: "",
  parentMenuId: "",
  displayOrder: "",
  screenId: "",
  url: "",
  icon: "",
  businessCategory: "",
  description: "",
  useYn: "Y",
  reason: "",
};

function menuQueryPath(search: SearchForm): `/api/${string}` {
  const params = new URLSearchParams();
  if (search.parentMenuId) params.set("parentMenuId", search.parentMenuId);
  if (search.useYn) params.set("useYn", search.useYn);
  const query = params.toString();
  return `/api/menus${query ? `?${query}` : ""}`;
}

function toMenuForm(menu: MenuSummary): MenuForm {
  return {
    menuName: menu.menuName,
    parentMenuId: menu.parentMenuId ?? "",
    displayOrder: String(menu.displayOrder),
    screenId: menu.screenId ?? "",
    url: menu.url ?? "",
    icon: menu.icon ?? "",
    businessCategory: menu.businessCategory ?? "",
    description: menu.description ?? "",
    useYn: menu.useYn,
    reason: "",
  };
}

export function MenuInformationManagementPage() {
  const [search, setSearch] = useState<SearchForm>(emptySearch);
  const [menus, setMenus] = useState<MenuSummary[]>([]);
  const [selectedMenu, setSelectedMenu] = useState<MenuSummary | null>(null);
  const [form, setForm] = useState<MenuForm>(emptyForm);
  const [modalOpen, setModalOpen] = useState(false);
  const [state, setState] = useState<
    "idle" | "loading" | "empty" | "error" | "permission" | "success"
  >("idle");
  const [message, setMessage] = useState("");

  const isPermissionError = (error: unknown) =>
    error instanceof ApiRequestError &&
    (error.status === 401 || error.status === 403);

  const loadMenus = async (showSuccess = false, selectedMenuId?: string) => {
    setState("loading");
    try {
      const response = await apiRequest<MenuSummary[]>(menuQueryPath(search));
      setMenus(response.data);
      const nextSelected = selectedMenuId
        ? (response.data.find((menu) => menu.menuId === selectedMenuId) ?? null)
        : selectedMenu;
      setSelectedMenu(nextSelected);
      setState(response.data.length === 0 ? "empty" : "success");
      setMessage(
        showSuccess ? "저장 후 메뉴 실행정보를 다시 조회했습니다." : "",
      );
    } catch (error) {
      const permissionDenied = isPermissionError(error);
      setState(permissionDenied ? "permission" : "error");
      setMessage(
        permissionDenied
          ? "권한이 없습니다."
          : "조회 또는 저장에 실패했습니다.",
      );
    }
  };

  const openCreate = () => {
    setForm(emptyForm);
    setMessage("");
    setModalOpen(true);
  };

  const openEdit = (menu: MenuSummary) => {
    setSelectedMenu(menu);
    setForm(toMenuForm(menu));
    setMessage("");
    setModalOpen(true);
  };

  const saveMenu = async () => {
    setState("loading");
    setMessage("");
    try {
      await apiRequest<null>("/api/menus", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          menuName: form.menuName,
          parentMenuId: form.parentMenuId || null,
          displayOrder: Number(form.displayOrder),
          screenId: form.screenId,
          url: form.url,
          icon: form.icon || null,
          businessCategory: form.businessCategory || null,
          description: form.description || null,
          useYn: form.useYn,
          ...(form.reason ? { reason: form.reason } : {}),
        }),
      });
      const selectedMenuId = selectedMenu?.menuId;
      setModalOpen(false);
      await loadMenus(true, selectedMenuId);
    } catch (error) {
      const permissionDenied = isPermissionError(error);
      setState(permissionDenied ? "permission" : "error");
      setMessage(
        permissionDenied
          ? "권한이 없습니다."
          : "메뉴 실행정보를 저장하지 못했습니다.",
      );
    }
  };

  if (state === "permission") {
    return (
      <section
        className="menu-information-management menu-information-state"
        aria-live="polite"
      >
        <h1>메뉴 정보 관리</h1>
        <p>권한이 없습니다.</p>
      </section>
    );
  }

  return (
    <section
      className="menu-information-management"
      aria-labelledby="menu-information-management-title"
    >
      <p className="breadcrumb">
        시스템 관리 &gt; 메뉴 관리 &gt; 메뉴 정보 관리
      </p>
      <h1 id="menu-information-management-title">메뉴 정보 관리</h1>
      <section
        className="menu-information-search"
        aria-label="메뉴 실행정보 조회"
      >
        <label>
          상위 메뉴
          <select
            aria-label="상위 메뉴"
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
        <div className="form-actions">
          <button
            type="button"
            className="text-action"
            onClick={() => setSearch(emptySearch)}
          >
            초기화
          </button>
          <button
            type="button"
            className="primary-action"
            onClick={() => void loadMenus()}
          >
            조회
          </button>
        </div>
      </section>

      {message && state === "success" && (
        <p className="success-message" role="status">
          {message}
        </p>
      )}
      {state === "loading" && (
        <p className="loading-message" aria-live="polite">
          메뉴 실행정보를 조회하거나 저장하는 중입니다.
        </p>
      )}
      {state === "empty" && (
        <p className="empty-message">조건에 맞는 메뉴가 없습니다.</p>
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
        <>
          <section
            className="menu-information-results"
            aria-label="메뉴 실행정보 목록"
          >
            <div className="section-title">
              <h2>메뉴 실행정보</h2>
              <button
                type="button"
                className="primary-action"
                onClick={openCreate}
              >
                메뉴 등록
              </button>
            </div>
            <div className="table-scroll">
              <table>
                <thead>
                  <tr>
                    <th>메뉴명</th>
                    <th>화면ID</th>
                    <th>URL</th>
                    <th>아이콘</th>
                    <th>업무구분</th>
                    <th>설명</th>
                    <th>사용여부</th>
                    <th>작업</th>
                  </tr>
                </thead>
                <tbody>
                  {menus.map((menu) => (
                    <tr
                      key={menu.menuId}
                      className={
                        selectedMenu?.menuId === menu.menuId
                          ? "selected-row"
                          : ""
                      }
                      onClick={() => setSelectedMenu(menu)}
                    >
                      <td>{menu.menuName}</td>
                      <td>{menu.screenId ?? "-"}</td>
                      <td>{menu.url ?? "-"}</td>
                      <td>{menu.icon ?? "-"}</td>
                      <td>{menu.businessCategory ?? "-"}</td>
                      <td>{menu.description ?? "-"}</td>
                      <td>{menu.useYn}</td>
                      <td>
                        <button
                          type="button"
                          className="text-action"
                          onClick={(event) => {
                            event.stopPropagation();
                            openEdit(menu);
                          }}
                        >
                          수정
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </section>
          {selectedMenu && (
            <section
              className="menu-information-detail"
              aria-label="선택 메뉴 실행정보"
            >
              <div className="section-title">
                <h2>선택 메뉴 실행정보</h2>
                <button
                  type="button"
                  className="text-action"
                  onClick={() => setSelectedMenu(null)}
                >
                  선택 해제
                </button>
              </div>
              <dl className="detail-grid">
                <div>
                  <dt>메뉴명</dt>
                  <dd>{selectedMenu.menuName}</dd>
                </div>
                <div>
                  <dt>화면ID</dt>
                  <dd>{selectedMenu.screenId ?? "-"}</dd>
                </div>
                <div>
                  <dt>URL</dt>
                  <dd>{selectedMenu.url ?? "-"}</dd>
                </div>
                <div>
                  <dt>아이콘</dt>
                  <dd>{selectedMenu.icon ?? "-"}</dd>
                </div>
                <div>
                  <dt>업무구분</dt>
                  <dd>{selectedMenu.businessCategory ?? "-"}</dd>
                </div>
                <div>
                  <dt>설명</dt>
                  <dd>{selectedMenu.description ?? "-"}</dd>
                </div>
                <div>
                  <dt>표시순서</dt>
                  <dd>{selectedMenu.displayOrder}</dd>
                </div>
                <div>
                  <dt>사용여부</dt>
                  <dd>{selectedMenu.useYn}</dd>
                </div>
              </dl>
            </section>
          )}
        </>
      )}

      {modalOpen && (
        <div className="modal-backdrop" role="presentation">
          <section
            className="menu-information-modal"
            role="dialog"
            aria-modal="true"
            aria-labelledby="menu-information-modal-title"
          >
            <div className="section-title">
              <h2 id="menu-information-modal-title">메뉴 등록·수정</h2>
              <button
                type="button"
                className="text-action"
                onClick={() => setModalOpen(false)}
              >
                닫기
              </button>
            </div>
            <div className="menu-information-form">
              <label>
                메뉴명
                <input
                  aria-label="메뉴명"
                  value={form.menuName}
                  onChange={(event) =>
                    setForm({ ...form, menuName: event.target.value })
                  }
                />
              </label>
              <label>
                상위 메뉴
                <select
                  aria-label="상위 메뉴"
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
                화면ID
                <input
                  aria-label="화면ID"
                  value={form.screenId}
                  onChange={(event) =>
                    setForm({ ...form, screenId: event.target.value })
                  }
                />
              </label>
              <label>
                URL
                <input
                  aria-label="URL"
                  value={form.url}
                  onChange={(event) =>
                    setForm({ ...form, url: event.target.value })
                  }
                />
              </label>
              <label>
                아이콘
                <input
                  aria-label="아이콘"
                  value={form.icon}
                  onChange={(event) =>
                    setForm({ ...form, icon: event.target.value })
                  }
                />
              </label>
              <label>
                업무구분
                <input
                  aria-label="업무구분"
                  value={form.businessCategory}
                  onChange={(event) =>
                    setForm({ ...form, businessCategory: event.target.value })
                  }
                />
              </label>
              <label>
                설명
                <textarea
                  aria-label="설명"
                  value={form.description}
                  onChange={(event) =>
                    setForm({ ...form, description: event.target.value })
                  }
                />
              </label>
              <label>
                사용여부
                <select
                  aria-label="사용여부"
                  value={form.useYn}
                  onChange={(event) =>
                    setForm({ ...form, useYn: event.target.value })
                  }
                >
                  <option value="Y">Y</option>
                  <option value="N">N</option>
                </select>
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
            </div>
            <p>메뉴명, 표시순서, 화면ID, URL은 서버 필수 검증 대상입니다.</p>
            <div className="form-actions">
              <button
                type="button"
                className="text-action"
                onClick={() => setModalOpen(false)}
              >
                취소
              </button>
              <button
                type="button"
                className="primary-action"
                onClick={() => void saveMenu()}
              >
                저장
              </button>
            </div>
          </section>
        </div>
      )}
    </section>
  );
}
