import { ManagementPage } from "../components/ManagementPage";
import { screens } from "../screenConfigs";
export function MenuInfoPage({ readonly }: { readonly: boolean }) {
  return <ManagementPage screen={screens[6]} readonly={readonly} />;
}
