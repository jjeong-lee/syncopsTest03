import { ManagementPage } from "../components/ManagementPage";
import { screens } from "../screenConfigs";
export function UserRoleManagementPage({ readonly }: { readonly: boolean }) {
  return <ManagementPage screen={screens[3]} readonly={readonly} />;
}
