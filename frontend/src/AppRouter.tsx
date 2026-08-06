import {
  Navigate,
  Route,
  BrowserRouter as Router,
  Routes,
} from "react-router-dom";
import { AdminShell } from "./components/AdminShell";
import { AdminScreenPage } from "./pages/AdminScreenPage";
import { LoginPage } from "./pages/LoginPage";
import { screens } from "./featureCatalog";

export function AppRouter() {
  return (
    <Router>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route element={<AdminShell />}>
          <Route index element={<Navigate to={screens[0].route} replace />} />
          {screens.map((screen) => (
            <Route
              key={screen.route}
              path={screen.route}
              element={<AdminScreenPage screen={screen} />}
            />
          ))}
        </Route>
        <Route path="*" element={<Navigate to={screens[0].route} replace />} />
      </Routes>
    </Router>
  );
}
