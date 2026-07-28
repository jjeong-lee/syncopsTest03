import {
  createContext,
  ReactNode,
  useContext,
  useEffect,
  useMemo,
  useState,
} from "react";
import { api, CurrentUser } from "../services/apiClient";

type AuthState = {
  user: CurrentUser | null;
  loading: boolean;
  error: string | null;
  login: (loginId: string, password: string) => Promise<boolean>;
};
const AuthContext = createContext<AuthState | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<CurrentUser | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  useEffect(() => {
    api
      .me()
      .then(setUser)
      .catch(() => setUser(null))
      .finally(() => setLoading(false));
  }, []);
  const value = useMemo<AuthState>(
    () => ({
      user,
      loading,
      error,
      login: async (loginId, password) => {
        setLoading(true);
        setError(null);
        try {
          const next = await api.login(loginId, password);
          setUser(next);
          return true;
        } catch (err) {
          setError(
            err instanceof Error ? err.message : "로그인에 실패했습니다.",
          );
          return false;
        } finally {
          setLoading(false);
        }
      },
    }),
    [user, loading, error],
  );
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const value = useContext(AuthContext);
  if (!value) throw new Error("AuthProvider is required");
  return value;
}
