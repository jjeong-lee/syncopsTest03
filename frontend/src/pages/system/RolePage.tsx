import { routeByScreenId } from "../../app/routes";
import { ManagementPage } from "./ManagementPage";

export function RolePage() {
  return <ManagementPage route={routeByScreenId("ROLE-001")} />;
}
