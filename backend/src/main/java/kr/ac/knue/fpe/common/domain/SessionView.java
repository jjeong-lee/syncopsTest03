package kr.ac.knue.fpe.common.domain;

import java.time.OffsetDateTime;
import java.util.List;

public record SessionView(String sessionId, String loginId, String userName, List<String> roleCodes, String state, OffsetDateTime issuedAt) {}
