import { useState } from "react";
import { ApiRequestError, apiRequest } from "../../shared/api/client";
import type { MenuSummary } from "./MenuStructureManagementPage";

type UsageMenu = MenuSummary & {
  exposureStartAt: string | null;
  exposureEndAt: string | null;
};

type UsageForm = {
  useYn: "Y" | "N";
  exposureStartAt: string;
  exposureEndAt: string;
};

function toLocalDateTime(value: string | null): string {
  if (!value) return "";
  return value.slice(0, 16);
}

function toUsageForm(menu: UsageMenu): UsageForm {
  return {
    useYn: menu.useYn === "N" ? "N" : "Y",
    exposureStartAt: toLocalDateTime(menu.exposureStartAt),
    exposureEndAt: toLocalDateTime(menu.exposureEndAt),
  };
}

function toOffsetDateTime(value: string): string | null {
  return value ? new Date(value).toISOString() : null;
}

export function MenuUsageManagementPage() {
  const [menus, setMenus] = useState<UsageMenu[]>([]);
  const [selectedMenu, setSelectedMenu] = useState<UsageMenu | null>(null);
  const [form, setForm] = useState<UsageForm | null>(null);
  const [state, setState] = useState<
    "idle" | "loading" | "empty" | "error" | "permission" | "success"
  >("idle");
  const [message, setMessage] = useState("");
  const [fieldError, setFieldError] = useState("");

  const isPermissionError = (error: unknown) =>
    error instanceof ApiRequestError &&
    (error.status === 401 || error.status === 403);

  const loadMenus = async (showSuccess = false) => {
    setState("loading");
    setFieldError("");
    try {
      const response = await apiRequest<UsageMenu[]>("/api/menus");
      setMenus(response.data);
      if (selectedMenu) {
        setSelectedMenu(
          response.data.find((menu) => menu.menuId === selectedMenu.menuId) ??
            null,
        );
      }
      setState(response.data.length === 0 ? "empty" : "success");
      setMessage(
        showSuccess
          ? "저장 후 메뉴 사용여부와 노출기간을 다시 조회했습니다."
          : "",
      );
    } catch (error) {
      const permissionDenied = isPermissionError(error);
      setState(permissionDenied ? "permission" : "error");
      setMessage(
        permissionDenied
          ? "권한이 없습니다."
          : "메뉴 사용 관리 조회에 실패했습니다.",
      );
    }
  };

  const openEdit = (menu: UsageMenu) => {
    setSelectedMenu(menu);
    setForm(toUsageForm(menu));
    setFieldError("");
    setMessage("");
  };

  const cancelEdit = () => {
    setForm(null);
    setFieldError("");
  };

  const saveUsage = async () => {
    if (!selectedMenu || !form) return;
    if (!form.exposureStartAt) {
      setFieldError("노출 시작일시는 필수입니다.");
      return;
    }
    const exposureStartAt = toOffsetDateTime(form.exposureStartAt);
    const exposureEndAt = toOffsetDateTime(form.exposureEndAt);
    if (exposureStartAt && exposureEndAt && exposureStartAt > exposureEndAt) {
      setFieldError("노출 종료일시는 시작일시보다 빠를 수 없습니다.");
      return;
    }
    if (!window.confirm("메뉴 사용여부와 노출기간을 저장하시겠습니까?")) return;

    setState("loading");
    setMessage("");
    setFieldError("");
    try {
      await apiRequest<null>("/api/menus", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          menuName: selectedMenu.menuName,
          parentMenuId: selectedMenu.parentMenuId,
          displayOrder: selectedMenu.displayOrder,
          screenId: selectedMenu.screenId,
          url: selectedMenu.url,
          icon: selectedMenu.icon,
          businessCategory: selectedMenu.businessCategory,
          description: selectedMenu.description,
          useYn: form.useYn,
          exposureStartAt,
          exposureEndAt,
        }),
      });
      setForm(null);
      await loadMenus(true);
    } catch (error) {
      const permissionDenied = isPermissionError(error);
      setState(permissionDenied ? "permission" : "error");
      setMessage(
        permissionDenied
          ? "권한이 없습니다."
          : "메뉴 사용여부와 노출기간을 저장하지 못했습니다.",
      );
    }
  };

  if (state === "permission") {
    return (
      <section
        className="menu-usage-management menu-usage-state"
        data-testid="menu-usage-management-page"
      >
        <h1>메뉴 사용 관리</h1>
        <p>권한이 없습니다.</p>
      </section>
    );
  }

  return (
    <section
      className="menu-usage-management"
      data-testid="menu-usage-management-page"
      aria-labelledby="menu-usage-management-title"
    >
      <p className="breadcrumb">
        시스템 관리 &gt; 메뉴 관리 &gt; 메뉴 사용 관리
      </p>
      <h1 id="menu-usage-management-title">메뉴 사용 관리</h1>
      <p className="menu-usage-description">
        메뉴 사용여부와 노출기간은 서버 시각으로 판정됩니다. 중지되거나 기간
        밖인 메뉴는 직접 URL 접근도 차단됩니다.
      </p>
      <section className="menu-usage-search" aria-label="메뉴 사용 관리 조회">
        <div className="form-actions">
          <span>메뉴별 사용여부와 노출기간을 조회합니다.</span>
          <button
            className="primary-action"
            data-testid="menu-usage-search-button"
            onClick={() => void loadMenus()}
            type="button"
          >
            조회
          </button>
        </div>
      </section>

      {state === "loading" && (
        <p className="loading-message">
          메뉴 사용 정보를 조회하거나 저장하는 중입니다.
        </p>
      )}
      {state === "empty" && (
        <p className="empty-message">조회된 메뉴가 없습니다.</p>
      )}
      {state === "error" && (
        <p className="error-message">
          {message}{" "}
          <button
            className="text-action"
            onClick={() => void loadMenus()}
            type="button"
          >
            다시 시도
          </button>
        </p>
      )}
      {message && state === "success" && (
        <p className="success-message" role="status">
          {message}
        </p>
      )}

      {state === "success" && (
        <section className="menu-usage-results" aria-label="메뉴 사용 목록">
          <div className="table-scroll">
            <table>
              <thead>
                <tr>
                  <th>메뉴명</th>
                  <th>화면ID</th>
                  <th>사용여부</th>
                  <th>노출 시작일시</th>
                  <th>노출 종료일시</th>
                  <th>작업</th>
                </tr>
              </thead>
              <tbody>
                {menus.map((menu) => (
                  <tr
                    data-testid={`menu-usage-row-${menu.menuId}`}
                    key={menu.menuId}
                  >
                    <td>{menu.menuName}</td>
                    <td>{menu.screenId ?? "-"}</td>
                    <td>{menu.useYn}</td>
                    <td>{menu.exposureStartAt ?? "-"}</td>
                    <td>{menu.exposureEndAt ?? "-"}</td>
                    <td>
                      <button
                        className="text-action"
                        data-testid={`menu-usage-edit-${menu.menuId}`}
                        onClick={() => openEdit(menu)}
                        type="button"
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
      )}

      {form && selectedMenu && (
        <div className="modal-backdrop" role="presentation">
          <section
            aria-labelledby="menu-usage-dialog-title"
            aria-modal="true"
            className="menu-usage-modal"
            data-testid="menu-usage-edit-dialog"
            role="dialog"
          >
            <div className="section-title">
              <h2 id="menu-usage-dialog-title">메뉴 사용여부·노출기간 수정</h2>
              <button
                className="text-action"
                data-testid="menu-usage-close-button"
                onClick={cancelEdit}
                type="button"
              >
                닫기
              </button>
            </div>
            <p>
              <strong>{selectedMenu.menuName}</strong>의 사용여부와 노출기간만
              변경할 수 있습니다.
            </p>
            <div className="menu-usage-form">
              <label>
                사용여부
                <select
                  aria-label="사용여부"
                  data-testid="menu-usage-use-yn-select"
                  onChange={(event) =>
                    setForm({ ...form, useYn: event.target.value as "Y" | "N" })
                  }
                  value={form.useYn}
                >
                  <option value="Y">사용</option>
                  <option value="N">중지</option>
                </select>
              </label>
              <label>
                노출 시작일시 <span aria-hidden="true">*</span>
                <input
                  aria-label="노출 시작일시"
                  data-testid="menu-usage-start-input"
                  onChange={(event) =>
                    setForm({ ...form, exposureStartAt: event.target.value })
                  }
                  required
                  type="datetime-local"
                  value={form.exposureStartAt}
                />
              </label>
              <label>
                노출 종료일시
                <input
                  aria-label="노출 종료일시"
                  data-testid="menu-usage-end-input"
                  onChange={(event) =>
                    setForm({ ...form, exposureEndAt: event.target.value })
                  }
                  type="datetime-local"
                  value={form.exposureEndAt}
                />
              </label>
            </div>
            {fieldError && (
              <p className="error-message" role="alert">
                {fieldError}
              </p>
            )}
            <div className="form-actions">
              <button
                className="text-action"
                data-testid="menu-usage-cancel-button"
                onClick={cancelEdit}
                type="button"
              >
                취소
              </button>
              <button
                className="primary-action"
                data-testid="menu-usage-save-button"
                onClick={() => void saveUsage()}
                type="button"
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
