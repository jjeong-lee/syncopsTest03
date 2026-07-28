import { routeByScreenId } from "../../app/routes";
import { ManagementPage } from "./ManagementPage";

export function MenuInfoPage() {
  return <ManagementPage route={routeByScreenId("MINFO-001")} />;
}
