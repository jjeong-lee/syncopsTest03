import { screens } from "../config";
import { ManagementPage } from "./ManagementPage";

const screen = screens.find((item) => item.id === "CMN-MENU-INFO-MGMT")!;

export function MenuInfoPage() {
  return <ManagementPage screen={screen} />;
}
