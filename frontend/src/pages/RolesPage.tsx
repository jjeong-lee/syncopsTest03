import { screens } from "../config";
import { ManagementPage } from "./ManagementPage";

const screen = screens.find((item) => item.id === "CMN-ROLE-MGMT")!;

export function RolesPage() {
  return <ManagementPage screen={screen} />;
}
