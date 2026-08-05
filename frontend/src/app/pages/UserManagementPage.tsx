import { ManagementPage } from "../components/ManagementPage";
import { screens } from "../screenConfigs";
export function UserManagementPage({ readonly }: { readonly: boolean }) {
  return <ManagementPage screen={screens[0]} readonly={readonly} />;
}
