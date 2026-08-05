import { ManagementPage } from "../components/ManagementPage";
import { screens } from "../screenConfigs";
export function RoleManagementPage({ readonly }: { readonly: boolean }) {
  return <ManagementPage screen={screens[2]} readonly={readonly} />;
}
