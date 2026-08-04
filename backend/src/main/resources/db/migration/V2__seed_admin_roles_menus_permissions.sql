INSERT INTO role(role_code, role_name, purpose, grant_criteria, data_scope_default) VALUES
('R01','교원','본인 관련 업무 수행','소속 교원 기본 부여','본인'),
('R02','학과장','소속 학과 교원 관련 업무 확인','학과장 보직 기준','소속 학과'),
('R03','단과대학(원) 행정실','단과대학 또는 대학원 행정 처리','대학 행정실 담당자','소속 단과대학'),
('R04','교수지원과','기준정보와 평가 관련 행정 관리','교수지원과 담당자','전체'),
('R05','산학협력단','연구비·간접비·지식재산 자료 관리','산학협력단 담당자','연구 관련 전체'),
('R06','입학인재관리과','입학·취업률 관련 자료 관리','입학인재관리과 담당자','입학·취업 관련 전체'),
('R07','실적부서','담당 실적 자료 관리','실적 담당부서 담당자','담당 실적 범위'),
('R08','점수산출 감사자','산출 과정과 근거 조회','감사자 지정','읽기 전용 전체'),
('R09','시스템관리자','사용자·조직·메뉴·권한·코드 관리','시스템 운영자 지정','시스템 관리 전체')
ON CONFLICT (role_code) DO UPDATE SET role_name=EXCLUDED.role_name, purpose=EXCLUDED.purpose, grant_criteria=EXCLUDED.grant_criteria, data_scope_default=EXCLUDED.data_scope_default, updated_at=CURRENT_TIMESTAMP;

INSERT INTO menu(menu_id,parent_menu_id,menu_level,display_order,menu_name,screen_id,url,icon,business_type,description,active_yn) VALUES
('SYS',NULL,1,1,'시스템 관리',NULL,NULL,'grid','SYSTEM','시스템 관리 대메뉴','Y'),
('SYS-UO','SYS',2,1,'사용자·조직 관리',NULL,NULL,'users','SYSTEM','사용자와 조직 기준정보','Y'),
('SYS-RA','SYS',2,2,'역할·권한 관리',NULL,NULL,'shield','SYSTEM','역할과 권한 기준정보','Y'),
('SYS-MENU','SYS',2,3,'메뉴 관리',NULL,NULL,'menu','SYSTEM','메뉴 구조와 정보','Y'),
('SYS-CODE','SYS',2,4,'공통코드 관리',NULL,NULL,'code','SYSTEM','공통코드 기준정보','Y'),
('M-USERS','SYS-UO',3,1,'사용자 관리','CMN-FR-001','/system/users','user','COMMON','사용자 관리','Y'),
('M-ORGS','SYS-UO',3,2,'조직 관리','CMN-FR-002','/system/organizations','sitemap','COMMON','조직 관리','Y'),
('M-ROLES','SYS-RA',3,1,'역할 관리','CMN-FR-005','/system/roles','badge','COMMON','역할 관리','Y'),
('M-USER-ROLES','SYS-RA',3,2,'사용자 역할 관리','CMN-FR-006','/system/user-roles','key','COMMON','사용자 역할 관리','Y'),
('M-MENU-PERM','SYS-RA',3,3,'메뉴 권한 관리','CMN-FR-007','/system/menu-permissions','lock','COMMON','메뉴 권한 관리','Y'),
('M-MENU-STRUCT','SYS-MENU',3,1,'메뉴 구조 관리','CMN-FR-013','/system/menu-structure','tree','COMMON','메뉴 구조 관리','Y'),
('M-MENU-INFO','SYS-MENU',3,2,'메뉴 정보 관리','CMN-FR-014','/system/menu-info','info','COMMON','메뉴 정보 관리','Y'),
('M-CODE-GROUPS','SYS-CODE',3,1,'코드그룹 관리','CMN-FR-016','/system/code-groups','folder','COMMON','코드그룹 관리','Y'),
('M-DETAIL-CODES','SYS-CODE',3,2,'상세코드 관리','CMN-FR-017','/system/detail-codes','list','COMMON','상세코드 관리','Y')
ON CONFLICT (menu_id) DO UPDATE SET parent_menu_id=EXCLUDED.parent_menu_id, menu_level=EXCLUDED.menu_level, display_order=EXCLUDED.display_order, menu_name=EXCLUDED.menu_name, screen_id=EXCLUDED.screen_id, url=EXCLUDED.url, icon=EXCLUDED.icon, business_type=EXCLUDED.business_type, description=EXCLUDED.description, active_yn=EXCLUDED.active_yn, updated_at=CURRENT_TIMESTAMP;

INSERT INTO menu_permission(target_type,target_id,menu_id,access_allowed_yn,explicit_deny_yn)
SELECT 'ROLE','R09',menu_id,'Y','N' FROM menu WHERE use_yn='Y'
ON CONFLICT (target_type,target_id,menu_id) DO UPDATE SET access_allowed_yn='Y', explicit_deny_yn='N', updated_at=CURRENT_TIMESTAMP;
