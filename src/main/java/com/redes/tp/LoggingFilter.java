package com.redes.tp;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;

@Component
public class LoggingFilter implements Filter {

    private final Object logLock = new Object();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {

        ContentCachingRequestWrapper request = new ContentCachingRequestWrapper(
                (HttpServletRequest) servletRequest, 1024 * 1024);

        try {
            chain.doFilter(request, servletResponse);
        } finally {
            String ip = request.getRemoteAddr();
            String method = request.getMethod();
            String uri = request.getRequestURI();
            String query = request.getQueryString();
            String body = "-";

            if ("POST".equalsIgnoreCase(method)) {
                byte[] bytes = request.getContentAsByteArray();
                body = new String(bytes, StandardCharsets.UTF_8);
            }

            String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
            String logLine = String.format("%s | %s | %s | %s | %s | %s%n",
                    today,
                    ip,
                    method,
                    uri,
                    query == null ? "-" : query,
                    body.isBlank() ? "-" : body);

            Path logDir = Path.of("logs");
            Path logFile = logDir.resolve("server-" + today + ".log");
            synchronized (logLock) {
                Files.createDirectories(logDir);
                Files.writeString(logFile, logLine, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            }
        }
    }
}
