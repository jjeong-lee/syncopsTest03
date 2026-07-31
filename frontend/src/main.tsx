import React, { useEffect, useMemo, useState } from "react";
import { createRoot } from "react-dom/client";
import "./index.css";
import { api } from "./api/client";
import type { Entity } from "./api/types";

type Screen = {
  route: string;
  title: string;
  middle: string;
  archetype:
    | "SEARCH_LIST_DETAIL"
    | "TREE_EDITOR"
    | "CONTENT_EDITOR"
    | "PERMISSION_MATRIX"
    | "EFFECTIVE_PERIOD_FORM";
  listPath: string;
  mutation?: {
    label: string;
    path: (row: Entity) => string;
    method: string;
    body: (row: Entity) => Entity;
  };
  columns: string[];
  fields: string[];
};

const screens: Screen[] = [
  {
    route: "/system/users",
    title: "사용자 관리",
    middle: "사용자·조직 관리",
    archetype: "SEARCH_LIST_DETAIL",
    listPath: "/users",
    columns: [
      "staffNo",
      "staffName",
      "organizationName",
      "rankName",
      "positionName",
      "employmentStatus",
      "systemUseYn",
      "roleCodes",
    ],
    fields: ["systemUseYn", "reason"],
    mutation: {
      label: "사용여부 저장",
      method: "PATCH",
      path: (row) => `/users/${row.userId}/usage`,
      body: (row) => ({
        systemUseYn: row.systemUseYn === "Y" ? "N" : "Y",
        reason: "화면 저장",
      }),
    },
  },
  {
    route: "/system/organizations",
    title: "조직 관리",
    middle: "사용자·조직 관리",
    archetype: "TREE_EDITOR",
    listPath: "/organizations",
    columns: [
      "organizationCode",
      "organizationName",
      "organizationType",
      "useYn",
    ],
    fields: ["parentOrganizationId", "validFrom", "validTo", "changeReason"],
  },
  {
    route: "/system/roles",
    title: "역할 관리",
    middle: "역할·권한 관리",
    archetype: "SEARCH_LIST_DETAIL",
    listPath: "/roles",
    columns: ["roleCode", "roleName", "defaultDataScope", "useYn"],
    fields: ["roleName", "purpose", "grantCriteria", "defaultDataScope"],
    mutation: {
      label: "역할 수정",
      method: "PUT",
      path: (row) => `/roles/${row.roleCode}`,
      body: (row) => ({
        roleCode: row.roleCode,
        roleName: row.roleName,
        purpose: row.purpose,
        grantCriteria: row.grantCriteria,
        defaultDataScope: row.defaultDataScope,
        useYn: row.useYn,
        reason: "역할 기준 수정",
      }),
    },
  },
  {
    route: "/system/user-roles",
    title: "사용자 역할 관리",
    middle: "역할·권한 관리",
    archetype: "EFFECTIVE_PERIOD_FORM",
    listPath: "/user-roles",
    columns: [
      "staffName",
      "roleCode",
      "roleName",
      "grantType",
      "validFrom",
      "validTo",
      "status",
    ],
    fields: ["userId", "roleCode", "grantType", "validFrom", "approverUserId"],
  },
  {
    route: "/system/menu-permissions",
    title: "메뉴 권한 관리",
    middle: "역할·권한 관리",
    archetype: "PERMISSION_MATRIX",
    listPath: "/menu-permissions",
    columns: [
      "menuName",
      "subjectType",
      "subjectId",
      "accessAllowed",
      "functionAllowed",
      "decisionEffect",
    ],
    fields: ["subjectType", "subjectId", "menuId", "decisionEffect"],
  },
  {
    route: "/system/menu-structure",
    title: "메뉴 구조 관리",
    middle: "메뉴 관리",
    archetype: "TREE_EDITOR",
    listPath: "/menus/tree",
    columns: ["menuName", "menuLevel", "displayOrder", "parentMenuId", "useYn"],
    fields: ["parentMenuId", "displayOrder"],
  },
  {
    route: "/system/menu-info",
    title: "메뉴 정보 관리",
    middle: "메뉴 관리",
    archetype: "CONTENT_EDITOR",
    listPath: "/menus",
    columns: [
      "menuName",
      "screenId",
      "urlPath",
      "iconName",
      "businessArea",
      "useYn",
    ],
    fields: [
      "menuName",
      "screenId",
      "urlPath",
      "iconName",
      "businessArea",
      "description",
    ],
  },
  {
    route: "/system/code-groups",
    title: "코드그룹 관리",
    middle: "공통코드 관리",
    archetype: "SEARCH_LIST_DETAIL",
    listPath: "/code-groups",
    columns: ["groupId", "groupName", "managingDepartment", "useYn"],
    fields: ["groupId", "groupName", "description", "managingDepartment"],
  },
  {
    route: "/system/detail-codes",
    title: "상세코드 관리",
    middle: "공통코드 관리",
    archetype: "TREE_EDITOR",
    listPath: "/code-groups/STATUS/codes",
    columns: ["groupId", "codeValue", "codeName", "sortOrder", "useYn"],
    fields: [
      "codeValue",
      "codeName",
      "parentDetailCodeId",
      "sortOrder",
      "extraAttributes",
      "validFrom",
      "validTo",
    ],
  },
];

function App() {
  const [route, setRoute] = useState(
    location.pathname === "/" ? "/login" : location.pathname,
  );
  const [currentUser, setCurrentUser] = useState<Entity | null>(null);
  const [health, setHealth] = useState("확인 중");
  useEffect(() => {
    api
      .health()
      .then((h) => setHealth(h.status))
      .catch(() => setHealth("DOWN"));
    api
      .me()
      .then((user) => setCurrentUser(user as Entity))
      .catch(() => setCurrentUser(null));
  }, []);
  const navigate = (next: string) => {
    history.pushState({}, "", next);
    setRoute(next);
  };
  if (route === "/login" || !currentUser)
    return (
      <LoginPage
        health={health}
        onLogin={(user) => {
          setCurrentUser(user as Entity);
          navigate("/system/users");
        }}
      />
    );
  const screen = screens.find((s) => s.route === route) ?? screens[0];
  const roles = (currentUser.roles as string[] | undefined) ?? [];
  return (
    <SystemShell
      user={currentUser}
      roles={roles}
      route={route}
      navigate={navigate}
    >
      <ManagementScreen screen={screen} permitted={roles.includes("R09")} />
    </SystemShell>
  );
}

function LoginPage({
  health,
  onLogin,
}: {
  health: string;
  onLogin: (user: unknown) => void;
}) {
  const [loginId, setLoginId] = useState("admin");
  const [password, setPassword] = useState("admin");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const submit = async () => {
    setLoading(true);
    setError("");
    try {
      onLogin(await api.login(loginId, password));
    } catch (e) {
      setError(e instanceof Error ? e.message : "로그인 실패");
    } finally {
      setLoading(false);
    }
  };
  return (
    <main className="min-h-screen bg-white pt-[55px] text-[#191919] font-acumin">
      <header className="fixed inset-x-0 top-0 z-50 h-[55px] border-b border-[#ECECEC] bg-white px-6 flex items-center justify-between">
        <strong>KNUE Faculty Performance Admin</strong>
        <span className="rounded-full bg-[#F5F8FF] px-4 py-1.5 text-sm font-semibold text-[#0057FF]">
          system health: {health}
        </span>
      </header>
      <section className="mx-auto mt-[83px] max-w-[740px] rounded-xl border border-[#E0EAFF] bg-white p-6 shadow-sm">
        <p className="text-sm font-semibold text-[#707070]">
          시스템 관리 공통기능 1차 범위
        </p>
        <h1 className="mt-2 text-[36px] font-bold leading-[1.1]">로그인</h1>
        {error && <Message kind="error" text={error} />}
        <label className="mt-6 block text-sm font-semibold">
          loginId
          <input
            className="mt-2 h-[44px] w-full rounded-full border border-[#E8E8E8] bg-[#F9F9F9] px-4 focus:border-[#0057FF] focus:outline-none"
            value={loginId}
            onChange={(e) => setLoginId(e.target.value)}
          />
        </label>
        <label className="mt-4 block text-sm font-semibold">
          password
          <input
            className="mt-2 h-[44px] w-full rounded-full border border-[#E8E8E8] bg-[#F9F9F9] px-4 focus:border-[#0057FF] focus:outline-none"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            type="password"
          />
        </label>
        <button
          className="mt-6 h-10 rounded-full bg-[#0057FF] px-6 font-semibold text-white transition-colors duration-200 hover:bg-[#003ECB] disabled:bg-[#909090]"
          disabled={loading}
          onClick={submit}
        >
          {loading ? "loading..." : "로그인"}
        </button>
      </section>
    </main>
  );
}

function SystemShell({
  user,
  roles,
  route,
  navigate,
  children,
}: {
  user: Entity;
  roles: string[];
  route: string;
  navigate: (path: string) => void;
  children: React.ReactNode;
}) {
  const groups = useMemo(
    () => Array.from(new Set(screens.map((s) => s.middle))),
    [],
  );
  return (
    <div className="min-h-screen bg-white font-acumin text-[#191919]">
      <header className="fixed inset-x-0 top-0 z-50 h-[55px] border-b border-[#ECECEC] bg-white pr-6 transition-[background] duration-300 flex items-center">
        <div className="flex h-full items-center px-6 font-bold">
          KNUE Admin
        </div>
        <nav className="hidden h-full items-center gap-6 lg:flex">
          <span className="relative flex h-full items-center text-sm font-semibold after:absolute after:bottom-[-1px] after:h-0.5 after:w-full after:bg-[#191919]">
            시스템 관리
          </span>
        </nav>
        <div className="mx-4 hidden h-8 flex-1 items-center rounded-full border border-[#E8E8E8] bg-[#F9F9F9] px-3 text-sm text-[#707070] md:flex">
          9개 공통기능 검색 / 권한 / 코드 / 메뉴
        </div>
        <div className="ml-auto flex items-center gap-3">
          <span className="text-sm text-[#707070]">
            {String(user.loginId)} · {roles.join(",")}
          </span>
          <button
            className="h-8 rounded-full border border-[#E8E8E8] px-4 text-xs font-semibold hover:bg-[#F0F0F0]"
            onClick={() => location.assign("/login")}
          >
            Logout
          </button>
        </div>
      </header>
      <div className="flex pt-[55px]">
        <aside className="sticky top-[55px] hidden h-[calc(100vh-55px)] w-[328px] shrink-0 overflow-y-auto border-r border-[#E8E8E8] bg-white px-6 pb-6 lg:block">
          {groups.map((group) => (
            <section key={group} className="border-t border-[#E8E8E8] py-4">
              <h2 className="mb-2 text-[13px] font-semibold">{group}</h2>
              <div className="flex flex-col gap-2">
                {screens
                  .filter((s) => s.middle === group)
                  .map((s) => (
                    <button
                      key={s.route}
                      onClick={() => navigate(s.route)}
                      className={`relative rounded-md px-3 py-2 text-left text-sm transition-all duration-100 ${route === s.route ? "bg-[#EBF1FF] font-bold text-[#0057FF] before:absolute before:left-0 before:top-0 before:h-full before:w-[5px] before:bg-[#0057FF]" : "hover:bg-[#F9F9F9]"}`}
                    >
                      {s.title}
                    </button>
                  ))}
              </div>
            </section>
          ))}
        </aside>
        <main className="min-w-0 flex-1 bg-gradient-to-t from-[#EBF1FF] to-white">
          {children}
        </main>
      </div>
    </div>
  );
}

function ManagementScreen({
  screen,
  permitted,
}: {
  screen: Screen;
  permitted: boolean;
}) {
  const [rows, setRows] = useState<Entity[]>([]);
  const [selected, setSelected] = useState<Entity | null>(null);
  const [state, setState] = useState<"loading" | "empty" | "error" | "success">(
    "loading",
  );
  const [message, setMessage] = useState("");
  const load = async () => {
    setState("loading");
    try {
      const data = (await api.list(screen.listPath)) as Entity[];
      setRows(data);
      setSelected(data[0] ?? null);
      setState(data.length ? "success" : "empty");
    } catch (e) {
      setMessage(e instanceof Error ? e.message : "조회 실패");
      setState("error");
    }
  };
  useEffect(() => {
    if (permitted) void load();
  }, [screen.route, permitted]);
  if (!permitted) return <PermissionDenied />;
  return (
    <section className="h-[calc(100vh-55px-30px)] min-h-[720px] p-6">
      <div className="mb-4 flex items-center justify-between">
        <div>
          <p className="text-sm font-semibold text-[#707070]">
            시스템 관리 &gt; {screen.middle} &gt; {screen.title}
          </p>
          <h1 className="text-2xl font-bold">{screen.title}</h1>
        </div>
        <span className="rounded-full bg-[#F5F8FF] px-3 py-[5px] text-xs font-semibold text-[#0057FF]">
          {screen.archetype}
        </span>
      </div>
      <div className="mb-4 flex gap-3 overflow-x-auto rounded-xl border border-[#E0EAFF] bg-white p-4">
        <input
          className="h-[44px] flex-1 rounded-full border border-[#E8E8E8] bg-[#F9F9F9] px-4 font-semibold placeholder:text-[#707070] focus:border-[#0057FF] focus:outline-none"
          placeholder="검색어 또는 조건을 입력하세요"
        />
        <button
          onClick={load}
          className="h-[44px] rounded-full bg-[#0057FF] px-6 font-semibold text-white hover:bg-[#003ECB]"
        >
          검색
        </button>
        <button
          onClick={() =>
            setMessage("편집 form을 마지막 조회 snapshot으로 되돌렸습니다.")
          }
          className="h-[44px] rounded-full border border-[#E8E8E8] px-6 font-semibold hover:bg-[#F0F0F0]"
        >
          취소
        </button>
      </div>
      {state === "loading" && (
        <div className="rounded-xl border border-[#E8E8E8] bg-white p-6">
          <div className="h-5 w-1/3 animate-pulse rounded bg-[#E8E8E8]" />
          <div className="mt-4 h-40 animate-pulse rounded bg-[#F0F0F0]" />
        </div>
      )}
      {state === "empty" && (
        <Message
          kind="info"
          text="조회 결과가 없습니다. 검색 조건을 줄이거나 신규 등록 CTA를 사용하세요."
        />
      )}
      {state === "error" && <Message kind="error" text={message} />}
      {message && state !== "error" && (
        <Message kind="success" text={message} />
      )}
      {state !== "loading" && (
        <div className="flex min-h-0 gap-2">
          <div className="w-1/2 max-w-[650px] overflow-hidden rounded-xl border border-[#E0EAFF] bg-[#EBF1FF]">
            <table className="w-full border-collapse text-left text-sm">
              <thead>
                <tr>
                  {screen.columns.map((c) => (
                    <th
                      className="border-b border-[#E0EAFF] bg-white p-3 text-xs text-[#707070]"
                      key={c}
                    >
                      {c}
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {rows.map((row, i) => (
                  <tr
                    key={i}
                    onClick={() => setSelected(row)}
                    className={`cursor-pointer border-b border-[#E0EAFF] bg-white transition-all duration-100 hover:bg-[#F9F9F9] ${selected === row ? "bg-[#EBF1FF]" : ""}`}
                  >
                    {screen.columns.map((c) => (
                      <td className="max-w-[180px] truncate p-3" key={c}>
                        {String(row[c] ?? "")}
                      </td>
                    ))}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <DetailPanel
            screen={screen}
            row={selected}
            reload={load}
            setMessage={setMessage}
          />
        </div>
      )}
    </section>
  );
}

function DetailPanel({
  screen,
  row,
  reload,
  setMessage,
}: {
  screen: Screen;
  row: Entity | null;
  reload: () => Promise<void>;
  setMessage: (s: string) => void;
}) {
  const [confirming, setConfirming] = useState(false);
  const save = async () => {
    if (!row || !screen.mutation) return;
    await api.mutate(
      screen.mutation.path(row),
      screen.mutation.method,
      screen.mutation.body(row),
    );
    setMessage("저장 후 동일 조회 operation을 재실행했습니다.");
    await reload();
  };
  return (
    <aside className="min-w-0 flex-1 overflow-y-auto rounded-xl border border-[#E0EAFF] bg-white p-6">
      <h2 className="text-base font-bold">선택 행 상세/관리</h2>
      <p className="mt-1 text-sm text-[#707070]">
        KORUS readonly와 로컬 관리 필드를 분리 표시합니다.
      </p>
      {!row ? (
        <p className="mt-6 text-sm text-[#707070]">행을 선택하세요.</p>
      ) : (
        <div className="mt-4 grid gap-3">
          {screen.fields.map((field) => (
            <label key={field} className="text-xs font-semibold text-[#707070]">
              {field}
              <input
                className="mt-1 h-8 w-full rounded-[20px] border border-[#E8E8E8] bg-[#F9F9F9] px-3 text-sm text-[#191919] focus:border-[#0057FF] focus:outline-none"
                readOnly
                value={String(row[field] ?? "")}
              />
            </label>
          ))}
        </div>
      )}
      <div className="mt-6 flex flex-wrap gap-2">
        {screen.mutation && (
          <button
            onClick={() => setConfirming(true)}
            className="h-8 rounded-full bg-[#0057FF] px-4 text-sm font-semibold text-white hover:bg-[#003ECB]"
          >
            {screen.mutation.label}
          </button>
        )}
        <button
          className="h-8 rounded-full border border-[#E8E8E8] px-4 text-sm font-semibold hover:bg-[#F0F0F0]"
          onClick={() => setMessage("취소되었습니다.")}
        >
          취소
        </button>
      </div>
      {confirming && (
        <div className="fixed inset-0 z-[60] flex items-center justify-center bg-black/65">
          <div className="w-[420px] rounded-xl bg-white p-6">
            <h3 className="font-bold">변경을 저장할까요?</h3>
            <p className="mt-2 text-sm text-[#707070]">
              성공하면 같은 화면의 조회 API를 다시 호출합니다.
            </p>
            <div className="mt-4 flex justify-end gap-2">
              <button
                className="h-8 rounded-full border px-4"
                onClick={() => setConfirming(false)}
              >
                계속 편집
              </button>
              <button
                className="h-8 rounded-full bg-[#0057FF] px-4 text-white"
                onClick={() => {
                  setConfirming(false);
                  void save();
                }}
              >
                저장
              </button>
            </div>
          </div>
        </div>
      )}
    </aside>
  );
}

function PermissionDenied() {
  return (
    <section className="m-6 rounded-xl border border-[#F97C00] bg-[#FFFCF8] p-6 text-[#F97C00]">
      <h1 className="text-xl font-bold">권한이 없습니다</h1>
      <p className="mt-2 text-sm">
        허용되지 않은 메뉴는 숨겨지고 직접 접근한 route는 업무 form 대신
        permission panel을 표시합니다.
      </p>
    </section>
  );
}
function Message({
  kind,
  text,
}: {
  kind: "success" | "error" | "info";
  text: string;
}) {
  const cls =
    kind === "error"
      ? "bg-[#FAE7E5] text-[#D00D00]"
      : kind === "success"
        ? "bg-[#E8F3E8] text-[#046700]"
        : "bg-[#F5F8FF] text-[#0057FF]";
  return (
    <div className={`my-4 rounded-lg px-4 py-3 text-sm font-semibold ${cls}`}>
      {text}
    </div>
  );
}

createRoot(document.getElementById("root")!).render(<App />);
