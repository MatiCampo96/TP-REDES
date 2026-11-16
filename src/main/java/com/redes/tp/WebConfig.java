package com.redes.tp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.web-root-dir:${user.dir}}")
    private String webRootDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = "file:" + Path.of(webRootDir).toAbsolutePath().normalize() + "/";
        registry.addResourceHandler("/**")
                .addResourceLocations(location)
                .setCachePeriod(0);
    }
}

@RestController
@RequestMapping("/")
class StaticFileController {

    @Value("${app.web-root-dir:${user.dir}}")
    private String webRootDir;

    @GetMapping(value = {"/", "/**"})
    public ResponseEntity<byte[]> serveGet(HttpServletRequest request) {
        return handleRequest(request);
    }

    @PostMapping(value = {"/", "/**"})
public ResponseEntity<String> servePost(@RequestBody(required = false) String body) {
    return ResponseEntity.ok("Datos recibidos correctamente");
}

    private ResponseEntity<byte[]> handleRequest(HttpServletRequest request) {
        String requestedPath = request.getRequestURI();
        if (requestedPath == null || requestedPath.isBlank() || "/".equals(requestedPath)) {
            requestedPath = "/index.html";
        }

        Path root = Path.of(webRootDir).toAbsolutePath().normalize();
        Path fileToServe = root.resolve(requestedPath.startsWith("/") ? requestedPath.substring(1) : requestedPath).normalize();

        if (!fileToServe.startsWith(root)) {
            return buildNotFound();
        }

        if (Files.isDirectory(fileToServe)) {
            fileToServe = fileToServe.resolve("index.html");
        }

        if (!Files.exists(fileToServe) || !Files.isRegularFile(fileToServe)) {
            return buildNotFound();
        }

        try {
            byte[] content = Files.readAllBytes(fileToServe);
            return ResponseEntity.ok()
                    .contentType(resolveMediaType(fileToServe.getFileName().toString()))
                    .body(content);
        } catch (IOException e) {
            return buildNotFound();
        }
    }

    private MediaType resolveMediaType(String fileName) {
        if (fileName.endsWith(".html") || fileName.endsWith(".htm")) {
            return MediaType.TEXT_HTML;
        }
        if (fileName.endsWith(".css")) {
            return MediaType.valueOf("text/css");
        }
        if (fileName.endsWith(".js")) {
            return MediaType.valueOf("application/javascript");
        }
        if (fileName.endsWith(".png")) {
            return MediaType.IMAGE_PNG;
        }
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
            return MediaType.IMAGE_JPEG;
        }
        if (fileName.endsWith(".gif")) {
            return MediaType.IMAGE_GIF;
        }
        return MediaType.APPLICATION_OCTET_STREAM;
    }

    private ResponseEntity<byte[]> buildNotFound() {
        String body = "<html><body><h1>Error 404</h1><p>El archivo solicitado no existe.</p></body></html>";
        return ResponseEntity.status(404)
                .contentType(MediaType.TEXT_HTML)
                .body(body.getBytes());
    }
}
