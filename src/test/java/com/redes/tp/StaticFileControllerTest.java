package com.redes.tp;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.client.RestTemplate;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class StaticFileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @LocalServerPort
    private int port;

    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
    }

    @Test
    void shouldServeIndexPageOnRoot() {
        ResponseEntity<String> response = restTemplate.getForEntity(baseUrl(), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Bienvenido");
    }

    @Test
    void shouldReturnCustom404ForMissingFile() {
        try {
            restTemplate.getForEntity(baseUrl() + "/archivo-inexistente.html", String.class);
            assertThat(false).as("Expected a 404 response for a missing file").isTrue();
        } catch (Exception ex) {
            assertThat(ex).hasMessageContaining("404");
        }
    }

    @Test
    void shouldAcceptPostAndReturnSuccess() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<String> request = new HttpEntity<>("nombre=Ana&edad=20", headers);
        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl() + "/", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Datos recibidos");

        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        Path logFile = Path.of("logs", "server-" + today + ".log");

        try {
            String logContent = Files.readString(logFile);
            assertThat(logContent).contains("nombre=Ana&edad=20");
        } catch (IOException e) {
            assertThat(false).as("Expected log file to be created for POST request").isTrue();
        }
    }

    @Test
    void shouldCapturePostRequestBodyEvenWhenControllerDoesNotReadIt() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content("nombre=Ana&edad=20"))
                .andExpect(status().isOk());

        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        Path logFile = Path.of("logs", "server-" + today + ".log");
        String logContent = Files.readString(logFile);

        assertThat(logContent).contains("nombre=Ana&edad=20");
    }

    private String baseUrl() {
        return "http://localhost:" + port;
    }
}
