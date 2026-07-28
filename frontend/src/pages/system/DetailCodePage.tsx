import { routeByScreenId } from "../../app/routes";
import { ManagementPage } from "./ManagementPage";

export function DetailCodePage() {
  return <ManagementPage route={routeByScreenId("DCODE-001")} />;
}
