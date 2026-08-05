import { ManagementPage } from "../components/ManagementPage";
import { screens } from "../screenConfigs";
export function MenuStructurePage({ readonly }: { readonly: boolean }) {
  return <ManagementPage screen={screens[5]} readonly={readonly} />;
}
