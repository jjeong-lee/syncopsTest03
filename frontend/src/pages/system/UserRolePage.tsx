import { routeByScreenId } from "../../app/routes";
import { ManagementPage } from "./ManagementPage";

export function UserRolePage() {
  return <ManagementPage route={routeByScreenId("UROLE-001")} />;
}
