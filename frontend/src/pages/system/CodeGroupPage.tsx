import { routeByScreenId } from "../../app/routes";
import { ManagementPage } from "./ManagementPage";

export function CodeGroupPage() {
  return <ManagementPage route={routeByScreenId("CGRP-001")} />;
}
