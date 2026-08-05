import { ManagementPage } from "../components/ManagementPage";
import { screens } from "../screenConfigs";
export function CodeDetailPage({ readonly }: { readonly: boolean }) {
  return <ManagementPage screen={screens[8]} readonly={readonly} />;
}
