import { screens } from "../config";
import { ManagementPage } from "./ManagementPage";

const screen = screens.find((item) => item.id === "CMN-ORG-MGMT")!;

export function OrganizationsPage() {
  return <ManagementPage screen={screen} />;
}
