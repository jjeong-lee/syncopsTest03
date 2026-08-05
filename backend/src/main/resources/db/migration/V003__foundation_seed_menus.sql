INSERT INTO menu(menu_id,parent_menu_id,menu_name,screen_id,route_path,icon_name,business_category,description,display_order,is_active) VALUES
('M-SYSTEM',NULL,'시스템 관리',NULL,NULL,'settings','SYSTEM','시스템 관리 대메뉴',1,'Y'),
('M-USER-ORG','M-SYSTEM','사용자·조직 관리',NULL,NULL,'users','USER_ORG','사용자와 조직 관리 중메뉴',1,'Y'),
('M-ROLE-PERM','M-SYSTEM','역할·권한 관리',NULL,NULL,'shield','ROLE_PERMISSION','역할과 권한 관리 중메뉴',2,'Y'),
('M-MENU','M-SYSTEM','메뉴 관리',NULL,NULL,'menu','MENU','메뉴 구조와 정보를 관리하는 중메뉴',3,'Y'),
('M-CODE','M-SYSTEM','공통코드 관리',NULL,NULL,'code','COMMON_CODE','공통코드 관리 중메뉴',4,'Y'),
('SCR-USER-MGMT','M-USER-ORG','사용자 관리','SCR-USER-MGMT','/system/users','user','USER_ORG','사용자 검색과 시스템 사용여부 관리',1,'Y'),
('SCR-ORG-MGMT','M-USER-ORG','조직 관리','SCR-ORG-MGMT','/system/organizations','building','USER_ORG','조직 계층과 적용기간 관리',2,'Y'),
('SCR-ROLE-MGMT','M-ROLE-PERM','역할 관리','SCR-ROLE-MGMT','/system/roles','key','ROLE_PERMISSION','역할 정의와 부여 기준 관리',1,'Y'),
('SCR-USER-ROLE','M-ROLE-PERM','사용자 역할 관리','SCR-USER-ROLE','/system/user-roles','badge','ROLE_PERMISSION','사용자별 역할 부여와 회수 관리',2,'Y'),
('SCR-MENU-PERMISSION','M-ROLE-PERM','메뉴 권한 관리','SCR-MENU-PERMISSION','/system/menu-permissions','lock','ROLE_PERMISSION','역할·조직·사용자별 메뉴 권한 관리',3,'Y'),
('SCR-MENU-STRUCTURE','M-MENU','메뉴 구조 관리','SCR-MENU-STRUCTURE','/system/menu-structure','tree','MENU','메뉴 부모와 표시순서 관리',1,'Y'),
('SCR-MENU-INFO','M-MENU','메뉴 정보 관리','SCR-MENU-INFO','/system/menu-info','layout','MENU','메뉴 실행 정보 관리',2,'Y'),
('SCR-CODE-GROUP','M-CODE','코드그룹 관리','SCR-CODE-GROUP','/system/code-groups','folder','COMMON_CODE','코드그룹 관리',1,'Y'),
('SCR-CODE-DETAIL','M-CODE','상세코드 관리','SCR-CODE-DETAIL','/system/code-details','list','COMMON_CODE','상세코드 관리',2,'Y')
ON CONFLICT (menu_id) DO UPDATE SET parent_menu_id=excluded.parent_menu_id, menu_name=excluded.menu_name, screen_id=excluded.screen_id, route_path=excluded.route_path, icon_name=excluded.icon_name, business_category=excluded.business_category, description=excluded.description, display_order=excluded.display_order, is_active='Y', updated_at=now();
