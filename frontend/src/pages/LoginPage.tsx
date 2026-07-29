import { Loader2, LogIn } from "lucide-react";
import { useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { login } from "../services/foundationApi";
import { Button } from "../components/ui";

export function LoginPage({ onLogin }: { onLogin: () => Promise<void> }) {
  const [loginId, setLoginId] = useState("admin");
  const [password, setPassword] = useState("admin");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();
  const redirectTo =
    new URLSearchParams(location.search).get("redirect") || "/system";

  async function submit(event: React.FormEvent) {
    event.preventDefault();
    setLoading(true);
    setError("");
    try {
      await login(loginId, password);
      await onLogin();
      navigate(redirectTo, { replace: true });
    } catch (e) {
      setError(e instanceof Error ? e.message : "로그인 오류");
    } finally {
      setLoading(false);
    }
  }

  return (
    <main className="grid min-h-svh bg-slate-950 text-slate-950 lg:grid-cols-2">
      <section className="flex items-center justify-center bg-white px-6 py-10">
        <div className="mx-auto flex w-full max-w-sm flex-col justify-center space-y-6">
          <div className="flex items-center justify-center gap-2">
            <div className="grid size-9 place-items-center rounded-lg bg-slate-950 text-white">
              <LogIn className="size-4" />
            </div>
            <span className="text-xl font-semibold">FPE 공통관리</span>
          </div>
          <div className="grid gap-2 text-start">
            <p className="text-xs font-semibold uppercase tracking-[0.18em] text-blue-600">
              KNUE Faculty Performance
            </p>
            <h1 className="text-2xl font-bold tracking-tight">
              교수업적평가 시스템
            </h1>
            <p className="text-sm leading-6 text-slate-500">
              공통기능 1차 범위 관리를 시작하려면 시드 관리자 계정으로
              로그인하세요.
            </p>
          </div>
          <form className="grid gap-4" onSubmit={submit}>
            <label className="grid gap-2 text-sm font-medium">
              아이디
              <input
                className="h-9 rounded-md border border-slate-200 bg-white px-3 text-sm shadow-sm transition-colors duration-200 focus:border-slate-400 focus:outline-none focus:ring-2 focus:ring-slate-200"
                value={loginId}
                onChange={(e) => setLoginId(e.target.value)}
              />
            </label>
            <label className="grid gap-2 text-sm font-medium">
              비밀번호
              <input
                className="h-9 rounded-md border border-slate-200 bg-white px-3 text-sm shadow-sm transition-colors duration-200 focus:border-slate-400 focus:outline-none focus:ring-2 focus:ring-slate-200"
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
              />
            </label>
            {error && (
              <div
                className="rounded-md border border-rose-200 bg-rose-50 px-3 py-2 text-sm text-rose-700"
                role="alert"
              >
                {error}
              </div>
            )}
            <Button
              className="bg-slate-950 text-white hover:bg-slate-800"
              disabled={loading}
            >
              {loading ? (
                <Loader2 className="size-4 animate-spin" />
              ) : (
                <LogIn className="size-4" />
              )}
              {loading ? "확인 중..." : "로그인"}
            </Button>
          </form>
          <p className="text-center text-xs leading-5 text-slate-500">
            로그인 성공 시 요청한 보호 route 또는 /system으로 이동합니다.
          </p>
        </div>
      </section>
      <section className="relative hidden overflow-hidden bg-slate-950 lg:block">
        <div className="absolute inset-8 rounded-3xl border border-white/10 bg-[radial-gradient(circle_at_20%_10%,rgba(59,130,246,0.35),transparent_30%),linear-gradient(135deg,rgba(15,23,42,1),rgba(30,41,59,1))] p-8 text-white shadow-2xl">
          <p className="text-sm font-medium text-blue-200">
            shadcn-admin shell reference
          </p>
          <div className="mt-16 grid gap-4">
            <div className="h-12 rounded-xl bg-white/10" />
            <div className="grid grid-cols-3 gap-4">
              <div className="h-28 rounded-xl bg-white/10" />
              <div className="h-28 rounded-xl bg-blue-400/20" />
              <div className="h-28 rounded-xl bg-white/10" />
            </div>
            <div className="h-64 rounded-xl bg-white/10 p-4">
              <div className="mb-3 h-4 w-40 rounded bg-white/20" />
              <div className="grid gap-2">
                {[0, 1, 2, 3, 4].map((row) => (
                  <div key={row} className="h-8 rounded bg-white/10" />
                ))}
              </div>
            </div>
          </div>
        </div>
      </section>
    </main>
  );
}
