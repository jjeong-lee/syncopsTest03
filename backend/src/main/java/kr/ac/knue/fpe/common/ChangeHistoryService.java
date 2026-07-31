package kr.ac.knue.fpe.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class ChangeHistoryService {
  private final CommonMapper mapper;
  private final ObjectMapper objectMapper;

  public ChangeHistoryService(CommonMapper mapper, ObjectMapper objectMapper) {
    this.mapper = mapper;
    this.objectMapper = objectMapper;
  }

  public void record(String entityName, String entityId, String operationType, Object beforeValue, Object afterValue, String operatorUserId, String reason) {
    Map<String, Object> params = new LinkedHashMap<>();
    params.put("entityName", entityName);
    params.put("entityId", entityId);
    params.put("operationType", operationType);
    params.put("beforeValue", toJson(beforeValue));
    params.put("afterValue", toJson(afterValue));
    params.put("operatorUserId", operatorUserId);
    params.put("reason", reason);
    mapper.insertHistory(params);
  }

  private String toJson(Object value) {
    if (value == null) return null;
    try { return objectMapper.writeValueAsString(value); }
    catch (JsonProcessingException e) { return "{}"; }
  }
}
