package kr.ac.knue.facultyassessment.externalintegrations;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@ConditionalOnProperty(name = "app.foundation.enabled", havingValue = "true", matchIfMissing = true)
public class NeisSchoolInformationAdapter implements SchoolInformationPort {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String openApiKey;
    private final String schoolInfoUrl;

    public NeisSchoolInformationAdapter(
        ObjectMapper objectMapper,
        @Value("${app.neis.open-api-key}") String openApiKey,
        @Value("${app.neis.connect-timeout-millis:3000}") int connectTimeoutMillis,
        @Value("${app.neis.read-timeout-millis:5000}") int readTimeoutMillis,
        @Value("${app.neis.school-info-url:https://open.neis.go.kr/hub/schoolInfo}") String schoolInfoUrl
    ) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeoutMillis);
        requestFactory.setReadTimeout(readTimeoutMillis);
        this.restClient = RestClient.builder().requestFactory(requestFactory).build();
        this.objectMapper = objectMapper;
        this.openApiKey = openApiKey;
        this.schoolInfoUrl = schoolInfoUrl;
    }

    @Override
    public List<SchoolInformation> lookup(SchoolInformationQuery query) {
        try {
            String body = restClient.get()
                .uri(UriComponentsBuilder.fromHttpUrl(schoolInfoUrl)
                    .queryParam("Type", "json")
                    .queryParam("pIndex", query.pIndex())
                    .queryParam("pSize", query.pSize())
                    .queryParamIfPresent("SCHUL_NM", optionalText(query.schoolName()))
                    .queryParamIfPresent("ATPT_OFCDC_SC_CODE", optionalText(query.educationOfficeCode()))
                    .queryParamIfPresent("KEY", optionalText(openApiKey))
                    .build()
                    .encode(StandardCharsets.UTF_8)
                    .toUri())
                .retrieve()
                .body(String.class);
            return parseResponse(body);
        } catch (SchoolInformationPort.ExternalLookupException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new SchoolInformationPort.ExternalLookupException("학교정보 외부 연동에 실패했습니다.", exception);
        }
    }

    private List<SchoolInformation> parseResponse(String body) throws Exception {
        JsonNode root = objectMapper.readTree(body);
        JsonNode schoolInfo = root.path("schoolInfo");
        if (schoolInfo.isArray()) {
            JsonNode result = schoolInfo.path(0).path("head").path(1).path("RESULT");
            if (!"INFO-000".equals(result.path("CODE").asText())) {
                throw neisError(result);
            }
            List<SchoolInformation> schools = new ArrayList<>();
            for (JsonNode row : schoolInfo.path(1).path("row")) {
                schools.add(new SchoolInformation(
                    text(row, "ATPT_OFCDC_SC_NM"),
                    text(row, "SCHUL_NM"),
                    text(row, "SCHUL_KND_SC_NM"),
                    text(row, "LCTN_SC_NM"),
                    text(row, "FOND_SC_NM"),
                    text(row, "ORG_RDNMA"),
                    text(row, "ORG_TELNO")
                ));
            }
            return schools;
        }

        JsonNode result = root.path("RESULT");
        if ("INFO-200".equals(result.path("CODE").asText())) {
            return List.of();
        }
        throw neisError(result);
    }

    private SchoolInformationPort.ExternalLookupException neisError(JsonNode result) {
        String message = result.path("MESSAGE").asText("학교정보 외부 연동에 실패했습니다.");
        return new SchoolInformationPort.ExternalLookupException(message);
    }

    private String text(JsonNode row, String field) {
        return row.path(field).asText("");
    }

    private java.util.Optional<String> optionalText(String value) {
        return value == null || value.isBlank() ? java.util.Optional.empty() : java.util.Optional.of(value);
    }
}
