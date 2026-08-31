package kr.ac.knue.facultyassessment.externalintegrations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NeisSchoolInformationAdapterTest {

    @Test
    void encodesKoreanSchoolNameAndMapsInfo000RowsFromTheStubbedNeisEndpoint() throws Exception {
        AtomicReference<String> rawQuery = new AtomicReference<>();
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/hub/schoolInfo", exchange -> respond(exchange, rawQuery));
        server.start();
        try {
            NeisSchoolInformationAdapter adapter = new NeisSchoolInformationAdapter(
                new ObjectMapper(),
                "",
                1000,
                1000,
                "http://127.0.0.1:" + server.getAddress().getPort() + "/hub/schoolInfo"
            );

            var schools = adapter.lookup(new SchoolInformationPort.SchoolInformationQuery("가락", null, 1, 100));

            assertEquals("Type=json&pIndex=1&pSize=100&SCHUL_NM=%EA%B0%80%EB%9D%BD", rawQuery.get());
            assertEquals(1, schools.size());
            assertEquals("가락초등학교", schools.get(0).schoolName());
        } finally {
            server.stop(0);
        }
    }

    private void respond(HttpExchange exchange, AtomicReference<String> rawQuery) throws java.io.IOException {
        rawQuery.set(exchange.getRequestURI().getRawQuery());
        byte[] body = """
            {"schoolInfo":[{"head":[{"list_total_count":1},{"RESULT":{"CODE":"INFO-000"}}]},{"row":[{"ATPT_OFCDC_SC_NM":"서울특별시교육청","SCHUL_NM":"가락초등학교","SCHUL_KND_SC_NM":"초등학교","LCTN_SC_NM":"송파구","FOND_SC_NM":"공립","ORG_RDNMA":"서울 송파구 가락로 1","ORG_TELNO":"02-0000-0000"}]}]}
            """.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, body.length);
        exchange.getResponseBody().write(body);
        exchange.close();
    }
}
