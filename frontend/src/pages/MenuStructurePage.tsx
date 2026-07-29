import { screens } from "../config";
import { ManagementPage } from "./ManagementPage";

const screen = screens.find((item) => item.id === "CMN-MENU-STRUCT-MGMT")!;

export function MenuStructurePage() {
  return <ManagementPage screen={screen} />;
}
