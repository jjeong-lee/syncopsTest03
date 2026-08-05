import { ManagementPage } from "../components/ManagementPage";
import { screens } from "../screenConfigs";
export function OrganizationManagementPage({
  readonly,
}: {
  readonly: boolean;
}) {
  return <ManagementPage screen={screens[1]} readonly={readonly} />;
}
