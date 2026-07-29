import { screens } from "../config";
import { ManagementPage } from "./ManagementPage";

const screen = screens.find((item) => item.id === "CMN-USER-ROLE-MGMT")!;

export function UserRolesPage() {
  return <ManagementPage screen={screen} />;
}
