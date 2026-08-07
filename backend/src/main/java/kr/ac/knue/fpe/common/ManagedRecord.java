package kr.ac.knue.fpe.common;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import jakarta.validation.constraints.NotBlank;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

public class ManagedRecord {
    @NotBlank(message = "식별자는 필수입니다.")
    private String id;
    private String area;
    private String title;
    private String status;
    private String useYn;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private Map<String, Object> payload = new LinkedHashMap<>();
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getUseYn() { return useYn; }
    public void setUseYn(String useYn) { this.useYn = useYn; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
    public Map<String, Object> getPayload() { return payload; }
    public void setPayload(Map<String, Object> payload) { this.payload = payload == null ? new LinkedHashMap<>() : new LinkedHashMap<>(payload); }
    @JsonAnySetter
    public void put(String key, Object value) { if (!Set.ofKeys().contains(key)) payload.put(key, value); }
    @JsonAnyGetter
    public Map<String, Object> any() { return payload; }
    private static class Set { static java.util.Set<String> ofKeys() { return java.util.Set.of("id", "area", "title", "status", "useYn", "createdAt", "updatedAt", "payload"); } }
}
