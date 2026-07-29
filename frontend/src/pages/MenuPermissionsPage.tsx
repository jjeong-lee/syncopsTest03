import { screens } from "../config";
import { ManagementPage } from "./ManagementPage";

const screen = screens.find((item) => item.id === "CMN-MENU-AUTH-MGMT")!;

export function MenuPermissionsPage() {
  return <ManagementPage screen={screen} />;
}
