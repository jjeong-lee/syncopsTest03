import { routeByScreenId } from "../../app/routes";
import { ManagementPage } from "./ManagementPage";

export function OrganizationPage() {
  return <ManagementPage route={routeByScreenId("ORG-001")} />;
}
