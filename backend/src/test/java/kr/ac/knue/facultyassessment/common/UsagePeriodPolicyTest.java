package kr.ac.knue.facultyassessment.common;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class UsagePeriodPolicyTest {

    private static final Instant SERVER_NOW = Instant.parse("2026-09-22T10:15:30Z");
    private final UsagePeriodPolicy policy = new UsagePeriodPolicy(Clock.fixed(SERVER_NOW, ZoneOffset.UTC));

    @Test
    void menuExposureUsesServerInstantAndIncludesBothTimeBoundaries() {
        OffsetDateTime serverNow = OffsetDateTime.ofInstant(SERVER_NOW, ZoneOffset.UTC);

        assertTrue(policy.isMenuCurrentlyAvailable("Y", serverNow, serverNow));
        assertFalse(policy.isMenuCurrentlyAvailable("Y", serverNow.plusSeconds(1), null));
        assertFalse(policy.isMenuCurrentlyAvailable("Y", serverNow.minusSeconds(1), serverNow.minusNanos(1)));
    }

    @Test
    void detailCodeUsageUsesServerDateAndIncludesBothDateBoundaries() {
        LocalDate serverDate = LocalDate.ofInstant(SERVER_NOW, ZoneOffset.UTC);

        assertTrue(policy.isDetailCodeCurrentlyAvailable("Y", serverDate, serverDate));
        assertFalse(policy.isDetailCodeCurrentlyAvailable("Y", serverDate.plusDays(1), null));
        assertFalse(policy.isDetailCodeCurrentlyAvailable("Y", serverDate.minusDays(1), serverDate.minusDays(1)));
    }

    @Test
    void openEndedPeriodsRemainAvailableUntilTheyAreExplicitlyStopped() {
        assertTrue(policy.isMenuCurrentlyAvailable("Y", OffsetDateTime.ofInstant(SERVER_NOW.minusSeconds(1), ZoneOffset.UTC), null));
        assertTrue(policy.isDetailCodeCurrentlyAvailable("Y", LocalDate.ofInstant(SERVER_NOW, ZoneOffset.UTC).minusDays(1), null));
        assertFalse(policy.isMenuCurrentlyAvailable("N", OffsetDateTime.ofInstant(SERVER_NOW.minusSeconds(1), ZoneOffset.UTC), null));
        assertFalse(policy.isDetailCodeCurrentlyAvailable("N", LocalDate.ofInstant(SERVER_NOW, ZoneOffset.UTC).minusDays(1), null));
    }

    @Test
    void invalidOrMissingStartIsRejectedBeforePersistence() {
        ApiException missingMenuStart = assertThrows(
            ApiException.class,
            () -> policy.validateMenuExposurePeriod(null, OffsetDateTime.ofInstant(SERVER_NOW, ZoneOffset.UTC))
        );
        ApiException invertedCodePeriod = assertThrows(
            ApiException.class,
            () -> policy.validateDetailCodeEffectivePeriod(LocalDate.of(2026, 9, 23), LocalDate.of(2026, 9, 22))
        );

        org.junit.jupiter.api.Assertions.assertEquals(HttpStatus.BAD_REQUEST, missingMenuStart.status());
        org.junit.jupiter.api.Assertions.assertEquals("exposureStartAt", missingMenuStart.field());
        org.junit.jupiter.api.Assertions.assertEquals("effectiveEndDate", invertedCodePeriod.field());
    }

    @Test
    void expiredRowsRemainHistoricalButAreNotCurrentCandidates() {
        OffsetDateTime expiredAt = OffsetDateTime.ofInstant(SERVER_NOW.minusSeconds(1), ZoneOffset.UTC);
        LocalDate expiredOn = LocalDate.ofInstant(SERVER_NOW, ZoneOffset.UTC).minusDays(1);

        assertTrue(policy.isHistoricalMenuRecord("Y", expiredAt, expiredAt));
        assertTrue(policy.isHistoricalDetailCodeRecord("Y", expiredOn, expiredOn));
        assertFalse(policy.isMenuCurrentlyAvailable("Y", expiredAt, expiredAt));
        assertFalse(policy.isDetailCodeCurrentlyAvailable("Y", expiredOn, expiredOn));
    }
}
