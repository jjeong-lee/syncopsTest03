import { screens } from "../config";
import { ManagementPage } from "./ManagementPage";

const screen = screens.find((item) => item.id === "CMN-DETAIL-CODE-MGMT")!;

export function DetailCodesPage() {
  return <ManagementPage screen={screen} />;
}
