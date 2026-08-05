INSERT INTO role(role_code, role_name, purpose, grant_criteria, default_data_scope, is_active)
VALUES
('R01','교원','본인 관련 업무를 수행하는 일반 사용자 역할','교원 인사 원천정보 기준','SELF','Y'),
('R02','학과장','소속 학과 교원 관련 업무를 확인하는 역할','학과장 보직 기준','ORG','Y'),
('R03','단과대학(원) 행정실','단과대학 또는 대학원 행정 처리 역할','단과대학 또는 대학원 행정 보직 기준','ORG','Y'),
('R04','교수지원과','기준정보와 평가 관련 행정 관리 역할','교수지원과 담당자 기준','ALL','Y'),
('R05','산학협력단','연구비·간접비·지식재산 관련 자료 관리 역할','산학협력단 담당자 기준','ORG','Y'),
('R06','입학인재관리과','입학·취업률 관련 자료 관리 역할','입학인재관리과 담당자 기준','ORG','Y'),
('R07','실적부서','담당 실적 자료 관리 역할','실적 담당부서 기준','ORG','Y'),
('R08','점수산출 감사자','산출 과정과 근거를 조회하는 감사 역할','감사 담당자 승인 기준','ALL','Y'),
('R09','시스템관리자','사용자·조직·메뉴·권한·코드 관리를 수행하는 관리자 역할','시스템 운영자 승인 기준','ALL','Y')
ON CONFLICT (role_code) DO UPDATE SET role_name=excluded.role_name, purpose=excluded.purpose, grant_criteria=excluded.grant_criteria, default_data_scope=excluded.default_data_scope, is_active='Y', updated_at=now();
