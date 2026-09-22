import { useState } from "react";
import { ApiRequestError } from "../../shared/api/client";
import {
  listMenus,
  listMenuUsageSettings,
  saveMenuUsageSettings,
  type MenuUsageSearch,
  type MenuUsageSetting,
} from "./Api";

type SearchForm = MenuUsageSearch;
type EditForm = {
  menuId: string;
  useYn: "Y" | "N";
  exposureStartAt: string;
  exposureEndAt: string;
};

const initialSearch: SearchForm = { menuId: "", useYn: "", size: "20" };

const toLocalDateTime = (value: string | null) =>
  value ? new Date(value).toISOString().slice(0, 16) : "";

export function MenuUsagePage() {
  const [search, setSearch] = useState<SearchForm>(initialSearch);
  const [rows, setRows] = useState<MenuUsageSetting[]>([]);
  const [menuNames, setMenuNames] = useState<Record<string, string>>({});
  const [form, setForm] = useState<EditForm | null>(null);
  const [state, setState] = useState<
    "idle" | "loading" | "empty" | "error" | "permission" | "success"
  >("idle");
  const [message, setMessage] = useState("");
  const [fieldError, setFieldError] = useState("");

  const isPermissionError = (error: unknown) =>
    error instanceof ApiRequestError &&
    (error.status === 401 || error.status === 403);

  const loadSettings = async (showSuccess = false) => {
    setState("loading");
    setMessage("");
    try {
      const [menusResponse, settingsResponse] = await Promise.all([
        listMenus(),
        listMenuUsageSettings(search),
      ]);
      setMenuNames(
        Object.fromEntries(
          menusResponse.data.map((menu) => [menu.menuId, menu.menuName]),
        ),
      );
      setRows(settingsResponse.data);
      setState(settingsResponse.data.length ? "success" : "empty");
      if (showSuccess)
        setMessage("저장 후 메뉴 사용여부와 노출기간을 다시 조회했습니다.");
    } catch (error) {
      const permissionDenied = isPermissionError(error);
      setState(permissionDenied ? "permission" : "error");
      setMessage(
        permissionDenied
          ? "권한이 없어 메뉴 사용 설정을 조회하거나 변경할 수 없습니다."
          : "메뉴 사용 설정을 조회하지 못했습니다.",
      );
    }
  };

  const openEdit = (setting: MenuUsageSetting) => {
    setFieldError("");
    setForm({
      menuId: setting.menuId,
      useYn: setting.useYn,
      exposureStartAt: toLocalDateTime(setting.exposureStartAt),
      exposureEndAt: toLocalDateTime(setting.exposureEndAt),
    });
  };

  const save = async () => {
    if (!form) return;
    if (!form.exposureStartAt) {
      setFieldError("노출 시작일시는 필수입니다.");
      return;
    }
    if (!window.confirm("메뉴 사용여부와 노출기간을 저장하시겠습니까?")) return;

    setState("loading");
    setMessage("");
    setFieldError("");
    try {
      await saveMenuUsageSettings({
        menuId: form.menuId,
        useYn: form.useYn,
        exposureStartAt: new Date(form.exposureStartAt).toISOString(),
        exposureEndAt: form.exposureEndAt
          ? new Date(form.exposureEndAt).toISOString()
          : null,
      });
      setForm(null);
      await loadSettings(true);
    } catch (error) {
      if (isPermissionError(error)) {
        setForm(null);
        setState("permission");
        setMessage(
          "권한이 없어 메뉴 사용 설정을 조회하거나 변경할 수 없습니다.",
        );
        return;
      }
      setState("success");
      setFieldError(
        error instanceof ApiRequestError
          ? error.message
          : "메뉴 사용 설정을 저장하지 못했습니다.",
      );
    }
  };

  if (state === "permission") {
    return (
      <section
        className="menu-information-management menu-information-state"
        data-testid="menu-usage-page"
      >
        <p className="breadcrumb">
          시스템 관리 &gt; 메뉴 관리 &gt; 메뉴 사용 관리
        </p>
        <h1>메뉴 사용 관리</h1>
        <p>{message}</p>
      </section>
    );
  }

  return (
    <section
      className="menu-information-management"
      data-testid="menu-usage-page"
      aria-labelledby="menu-usage-title"
    >
      <p className="breadcrumb">
        시스템 관리 &gt; 메뉴 관리 &gt; 메뉴 사용 관리
      </p>
      <h1 id="menu-usage-title">메뉴 사용 관리</h1>
      <section
        className="menu-information-search"
        aria-label="메뉴 사용 설정 조회"
      >
        <label>
          메뉴 ID
          <input
            data-testid="menu-usage-menu-id-input"
            value={search.menuId}
            onChange={(event) =>
              setSearch({ ...search, menuId: event.target.value })
            }
          />
        </label>
        <label>
          사용여부
          <select
            data-testid="menu-usage-search-use-yn-select"
            aria-label="검색 사용여부"
            value={search.useYn}
            onChange={(event) =>
              setSearch({ ...search, useYn: event.target.value })
            }
          >
            <option value="">전체</option>
            <option value="Y">사용</option>
            <option value="N">중지</option>
          </select>
        </label>
        <label>
          목록 건수
          <select
            data-testid="menu-usage-size-select"
            value={search.size}
            onChange={(event) =>
              setSearch({
                ...search,
                size: event.target.value as SearchForm["size"],
              })
            }
          >
            <option value="20">20</option>
            <option value="50">50</option>
            <option value="100">100</option>
          </select>
        </label>
        <div className="form-actions">
          <button
            data-testid="menu-usage-query-button"
            type="button"
            className="primary-action"
            onClick={() => void loadSettings()}
          >
            조회
          </button>
        </div>
      </section>

      {state === "loading" && (
        <p className="loading-message">메뉴 사용 설정을 조회하고 있습니다.</p>
      )}
      {state === "empty" && (
        <p className="empty-message">
          현재 조건에 맞는 메뉴 사용 설정이 없습니다.
        </p>
      )}
      {state === "error" && (
        <p className="error-message">
          {message}{" "}
          <button
            data-testid="menu-usage-retry-button"
            type="button"
            className="text-action"
            onClick={() => void loadSettings()}
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
        <section
          className="menu-information-results"
          aria-label="메뉴 사용 설정 목록"
        >
          <div className="section-title">
            <h2>메뉴 사용여부·노출기간 설정</h2>
          </div>
          <div className="table-scroll">
            <table>
              <thead>
                <tr>
                  <th>메뉴 ID</th>
                  <th>메뉴명</th>
                  <th>사용여부</th>
                  <th>노출 시작일시</th>
                  <th>노출 종료일시</th>
                  <th>관리</th>
                </tr>
              </thead>
              <tbody>
                {rows.map((row) => (
                  <tr
                    key={row.menuId}
                    data-testid={`menu-usage-row-${row.menuId}`}
                  >
                    <td>{row.menuId}</td>
                    <td>{menuNames[row.menuId] ?? "-"}</td>
                    <td>{row.useYn === "Y" ? "사용" : "중지"}</td>
                    <td>{row.exposureStartAt}</td>
                    <td>{row.exposureEndAt ?? "-"}</td>
                    <td>
                      <button
                        data-testid={`menu-usage-edit-${row.menuId}`}
                        type="button"
                        className="text-action"
                        onClick={() => openEdit(row)}
                      >
                        변경
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </section>
      )}

      {form && (
        <div className="modal-backdrop" role="presentation">
          <section
            className="menu-information-modal"
            data-testid="menu-usage-edit-modal"
            role="dialog"
            aria-modal="true"
            aria-labelledby="menu-usage-edit-title"
          >
            <div className="section-title">
              <h2 id="menu-usage-edit-title">메뉴 사용여부·노출기간 변경</h2>
              <button
                data-testid="menu-usage-close-button"
                type="button"
                className="text-action"
                onClick={() => setForm(null)}
              >
                닫기
              </button>
            </div>
            <div className="menu-information-form">
              <label>
                메뉴 ID
                <input
                  data-testid="menu-usage-readonly-menu-id"
                  value={form.menuId}
                  readOnly
                />
              </label>
              <label>
                사용여부
                <select
                  data-testid="menu-usage-edit-use-yn-select"
                  value={form.useYn}
                  onChange={(event) =>
                    setForm({ ...form, useYn: event.target.value as "Y" | "N" })
                  }
                >
                  <option value="Y">사용</option>
                  <option value="N">중지</option>
                </select>
              </label>
              <label>
                노출 시작일시 *
                <input
                  data-testid="menu-usage-exposure-start-input"
                  type="datetime-local"
                  value={form.exposureStartAt}
                  onChange={(event) =>
                    setForm({ ...form, exposureStartAt: event.target.value })
                  }
                />
              </label>
              <label>
                노출 종료일시
                <input
                  data-testid="menu-usage-exposure-end-input"
                  type="datetime-local"
                  value={form.exposureEndAt}
                  onChange={(event) =>
                    setForm({ ...form, exposureEndAt: event.target.value })
                  }
                />
              </label>
            </div>
            <p>
              시작일시는 필수이며 종료일시는 비울 수 있습니다. 시작일시는
              종료일시보다 늦을 수 없습니다.
            </p>
            {fieldError && (
              <p className="error-message" role="alert">
                {fieldError}
              </p>
            )}
            <div className="form-actions">
              <button
                data-testid="menu-usage-cancel-button"
                type="button"
                className="text-action"
                onClick={() => setForm(null)}
              >
                취소
              </button>
              <button
                data-testid="menu-usage-save-button"
                type="button"
                className="primary-action"
                onClick={() => void save()}
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
