INSERT INTO organization (organization_id, organization_code, organization_name, organization_type, use_yn)
VALUES ('ORG-KNUE-EDU', 'KNUE-EDU', '교육대학원', 'GRADUATE_SCHOOL', 'Y')
ON CONFLICT (organization_id) DO NOTHING;

INSERT INTO organization_relationship (
    organization_relationship_id,
    organization_id,
    parent_organization_id,
    effective_start_date,
    effective_end_date,
    status
)
VALUES ('ORG-REL-KNUE-EDU', 'ORG-KNUE-EDU', 'ORG-KNUE', DATE '2026-01-01', NULL, 'ACTIVE')
ON CONFLICT (organization_relationship_id) DO NOTHING;
