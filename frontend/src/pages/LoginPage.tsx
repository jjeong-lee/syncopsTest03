import { FormEvent, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../app/AuthProvider";
import { StatePanel } from "../components/StatePanel";
import {
  Button,
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
  Input,
} from "../components/ui";

export function LoginPage() {
  const { login, loading, error } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [loginId, setLoginId] = useState("admin");
  const [password, setPassword] = useState("admin");

  async function submit(event: FormEvent) {
    event.preventDefault();
    const ok = await login(loginId, password);
    if (ok) {
      const from =
        (location.state as { from?: string } | null)?.from ?? "/system/users";
      navigate(from, { replace: true });
    }
  }

  return (
    <main className="grid min-h-svh bg-background lg:grid-cols-2">
      <section className="flex items-center justify-center px-6 py-10 lg:p-8">
        <div className="mx-auto flex w-full max-w-sm flex-col justify-center space-y-6">
          <div className="flex items-center justify-center gap-2">
            <span className="grid size-9 place-items-center rounded-lg bg-primary text-sm font-bold text-primary-foreground">
              KN
            </span>
            <span className="text-xl font-semibold">교수업적평가</span>
          </div>
          <Card>
            <CardHeader>
              <CardTitle className="text-lg">
                공통기능 1차 관리자 검증
              </CardTitle>
              <CardDescription>
                admin/admin 계정으로 9개 시스템 관리 메뉴 접근을 확인합니다.
              </CardDescription>
            </CardHeader>
            <CardContent className="pt-0">
              <form className="grid gap-4" onSubmit={submit}>
                <label className="grid gap-1.5 text-sm font-medium">
                  아이디
                  <Input
                    value={loginId}
                    onChange={(event) => setLoginId(event.target.value)}
                    autoComplete="username"
                  />
                </label>
                <label className="grid gap-1.5 text-sm font-medium">
                  비밀번호
                  <Input
                    type="password"
                    value={password}
                    onChange={(event) => setPassword(event.target.value)}
                    autoComplete="current-password"
                  />
                </label>
                <Button disabled={loading} type="submit" className="w-full">
                  {loading ? "확인 중" : "로그인"}
                </Button>
                {loading && (
                  <StatePanel
                    state="loading"
                    message="로그인 정보를 확인하는 중입니다."
                  />
                )}
                {error && <StatePanel state="error" message={error} />}
              </form>
            </CardContent>
          </Card>
        </div>
      </section>
      <section
        className="relative hidden overflow-hidden bg-muted lg:block"
        aria-hidden="true"
      >
        <div className="absolute left-20 top-[15%] w-[760px] rounded-2xl border bg-card p-5 shadow-2xl">
          <div className="mb-4 flex items-center justify-between border-b pb-4">
            <div>
              <p className="text-xs font-semibold text-primary">
                SYSTEM MANAGEMENT
              </p>
              <h2 className="text-2xl font-bold tracking-tight">
                R09 관리자 콘솔
              </h2>
            </div>
            <span className="rounded-md border bg-background px-3 py-1 text-sm">
              9 routes
            </span>
          </div>
          <div className="grid gap-3 md:grid-cols-2">
            {[
              "사용자 관리",
              "조직 관리",
              "역할 관리",
              "메뉴 권한 관리",
              "메뉴 구조 관리",
              "공통코드 관리",
            ].map((name) => (
              <div key={name} className="rounded-lg border bg-background p-4">
                <div className="mb-3 h-2 w-16 rounded-full bg-primary/30" />
                <p className="font-medium">{name}</p>
                <p className="mt-1 text-sm text-muted-foreground">
                  loading · empty · error · success
                </p>
              </div>
            ))}
          </div>
        </div>
      </section>
    </main>
  );
}
