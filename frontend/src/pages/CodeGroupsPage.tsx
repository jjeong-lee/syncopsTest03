import { screens } from "../config";
import { ManagementPage } from "./ManagementPage";

const screen = screens.find((item) => item.id === "CMN-CODE-GROUP-MGMT")!;

export function CodeGroupsPage() {
  return <ManagementPage screen={screen} />;
}
