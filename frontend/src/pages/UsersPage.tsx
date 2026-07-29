import { screens } from "../config";
import { ManagementPage } from "./ManagementPage";

const screen = screens.find((item) => item.id === "CMN-USER-MGMT")!;

export function UsersPage() {
  return <ManagementPage screen={screen} />;
}
