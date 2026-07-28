import { routeByScreenId } from "../../app/routes";
import { ManagementPage } from "./ManagementPage";

export function MenuPermissionPage() {
  return <ManagementPage route={routeByScreenId("MPERM-001")} />;
}
