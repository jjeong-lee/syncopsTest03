import { ManagementPage } from "../components/ManagementPage";
import { screens } from "../screenConfigs";
export function CodeGroupPage({ readonly }: { readonly: boolean }) {
  return <ManagementPage screen={screens[7]} readonly={readonly} />;
}
