# 구현 증빙 — Phase 1 / T001

## Binding status

- request key: `FPE01-110-scope-analysis-20261001T045346`
- generation attempt: `initial-2026-10-01T05-38-34Z`
- approved review state: `approved`
- approval reason: `reviewer_code_generation_approval`
- approved change-set schema: `approved_design_doc_change_set.v1`
- approved change-set SHA-256: `b70dc7792e4d8d3f05dbad18c388d5a32074bbd97a0236d3e76e107c82aafedf`

## Immutable contract binding

| Input | SHA-256 | Applied boundary |
|---|---|---|
| implementation contract | `1c5fc5fa17fa5ca19b955757bead2b9eb8d23aba8cf9d6d7c53a553080e8d494` | Spring Boot 3.3.x / Java 17 / Maven, React 18 / Vite 5 / npm, PostgreSQL 16, Docker Compose |
| approved change-set | `21b713209b00bddece18a48ace7916cc9082b70e722e42c42e144b684cae0208` | effective design documents are authoritative; canonical documents are provenance only |
| review acceptance | `37e884572c8fb0413d8e796a0faa0dd4694219fb5bdcb44dfa296315671caa14` | `spec.md`, `plan.md`, and `tasks.md` are approved for this request |
| handoff manifest | `1620ccbb4d3ab5958bb4598099466d34d50da59f55620321c0f4e3a38604c3c2` | request identity, artifact references, and handoff integrity are bound |
| effective OpenAPI contract | `1defcf6a85bee5b89f5fcf2a7fbc96caf39cfb24ea1e88e39cf3c866ab88c178` | later API implementation must preserve the approved HTTP contract |
| effective data model | `433c8cbeca67527fd1a244b9dd9019a19b304f15af0ccceee6b9e2e003a0d674` | later schema work owns only the declared period fields and `system_setting` contract |
| effective UI design | `72bb04f2ba0cfe829dfc094979eaf9ccffdc8973b36f9e23a5a07d6135d89bbb` | later UI work owns the four approved R09 screen slices |

The review acceptance binds the generated core-document digests: `spec.md` `a0906b43ec3d10cd65c0e1e244cdfaa8425f4a31172aca7c6a4a1acb74ef91a2`, `plan.md` `98d43d2a0bae223d09781b0f8e2bf0a8b15bbe5449af92422b9cf63b44bc3f76`, and `tasks.md` `1c5ddd0ed6b7643cfa66d20d7d4ddc7dd3ed9b6d0af80535fad63ceeaed0616e`.

## Registry and allocation binding

The requirement registry identifies this setup phase as evidence-only:

- `REQ-097` through `REQ-103` remain out of scope, including adjacent-domain changes, external authentication, physical deletion, unrequested batch work, and real reference-data copy/initialization.
- `REQ-100` preserves existing menu structure, execution metadata, access permissions, code values/names, role codes `R01`–`R09`, and the role-permission matrix.
- `REQ-107`, `REQ-113`, and the later data/authorization requirements are recorded as future implementation obligations; T001 does not modify their runtime behavior.

The seed allocation assigns `R09` the four approved change screens: `SCR-MENU-USAGE-MANAGEMENT`, `SCR-CODE-USAGE-MANAGEMENT`, `SCR-COMMON-ENVIRONMENT-SETTINGS`, and `SCR-REFERENCE-YEAR-MANAGEMENT`. It also preserves the existing R09 administration screens. `system_setting` is a first-order master record; `menu` is a first-order master record; `detail_code` is a second-order record after `code_group`.

## Phase 1 scope decision

This evidence completes T001 only. No backend, frontend, infrastructure, migration, seed, API, route, authorization, or test behavior is changed in Phase 1. Later tasks must use the effective design bundle and retain the above immutable bindings.
