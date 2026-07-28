import { routeByScreenId } from "../../app/routes";
import { ManagementPage } from "./ManagementPage";

export function UserPage() {
  return <ManagementPage route={routeByScreenId("USR-001")} />;
}
