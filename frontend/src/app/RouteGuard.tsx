import { Navigate, Outlet, useLocation } from "react-router-dom";
import { useAuth } from "./AuthProvider";
import { StatePanel } from "../components/StatePanel";

export function RouteGuard() {
  const auth = useAuth();
  const location = useLocation();
  if (auth.loading)
    return <StatePanel state="loading" message="세션을 확인하는 중입니다." />;
  if (!auth.user)
    return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  if (!auth.user.roleCodes.includes("R09")) {
    return (
      <StatePanel
        state="permission"
        message="이 화면에 접근할 권한이 없습니다."
      />
    );
  }
  return <Outlet />;
}
