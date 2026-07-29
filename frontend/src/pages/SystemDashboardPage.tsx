import { screens } from "../config";
import { ManagementPage } from "./ManagementPage";

const screen = screens.find((item) => item.id === "SYSTEM-SMOKE-DASHBOARD")!;

export function SystemDashboardPage() {
  return <ManagementPage screen={screen} />;
}
