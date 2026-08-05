import { ManagementPage } from "../components/ManagementPage";
import { screens } from "../screenConfigs";
export function MenuPermissionPage({ readonly }: { readonly: boolean }) {
  return <ManagementPage screen={screens[4]} readonly={readonly} />;
}
