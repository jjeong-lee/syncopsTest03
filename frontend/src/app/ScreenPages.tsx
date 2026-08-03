import { ManagementPage } from "./ManagementPage";
import { screenById } from "./screens";

export function UsersPage({ currentUserId }: { currentUserId: string }) {
  return (
    <ManagementPage
      screen={screenById("USER_MANAGEMENT")}
      currentUserId={currentUserId}
    />
  );
}

export function OrganizationsPage({
  currentUserId,
}: {
  currentUserId: string;
}) {
  return (
    <ManagementPage
      screen={screenById("ORGANIZATION_MANAGEMENT")}
      currentUserId={currentUserId}
    />
  );
}

export function RolesPage({ currentUserId }: { currentUserId: string }) {
  return (
    <ManagementPage
      screen={screenById("ROLE_MANAGEMENT")}
      currentUserId={currentUserId}
    />
  );
}

export function UserRolesPage({ currentUserId }: { currentUserId: string }) {
  return (
    <ManagementPage
      screen={screenById("USER_ROLE_MANAGEMENT")}
      currentUserId={currentUserId}
    />
  );
}

export function MenuPermissionsPage({
  currentUserId,
}: {
  currentUserId: string;
}) {
  return (
    <ManagementPage
      screen={screenById("MENU_PERMISSION_MANAGEMENT")}
      currentUserId={currentUserId}
    />
  );
}

export function MenuStructurePage({
  currentUserId,
}: {
  currentUserId: string;
}) {
  return (
    <ManagementPage
      screen={screenById("MENU_STRUCTURE_MANAGEMENT")}
      currentUserId={currentUserId}
    />
  );
}

export function MenuInfoPage({ currentUserId }: { currentUserId: string }) {
  return (
    <ManagementPage
      screen={screenById("MENU_INFO_MANAGEMENT")}
      currentUserId={currentUserId}
    />
  );
}

export function CodeGroupsPage({ currentUserId }: { currentUserId: string }) {
  return (
    <ManagementPage
      screen={screenById("CODE_GROUP_MANAGEMENT")}
      currentUserId={currentUserId}
    />
  );
}

export function DetailCodesPage({ currentUserId }: { currentUserId: string }) {
  return (
    <ManagementPage
      screen={screenById("DETAIL_CODE_MANAGEMENT")}
      currentUserId={currentUserId}
    />
  );
}
