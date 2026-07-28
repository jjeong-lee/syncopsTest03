import { routeByScreenId } from "../../app/routes";
import { ManagementPage } from "./ManagementPage";

export function MenuStructurePage() {
  return <ManagementPage route={routeByScreenId("MSTRUCT-001")} />;
}
