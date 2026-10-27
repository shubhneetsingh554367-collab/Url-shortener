package com.urlshortener;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.Map;

@RestController
public class UrlController {

    private final UrlService service;

    public UrlController(UrlService service) {
        this.service = service;
    }

    public record ShortenRequest(String url) {}

    @PostMapping("/api/shorten")
    public ResponseEntity<Map<String, String>> shorten(@RequestBody ShortenRequest request) {
        String code = service.shorten(request.url());
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "shortCode", code,
                "shortUrl", "http://localhost:8080/" + code
        ));
    }

    @GetMapping("/{code}")
    public ResponseEntity<Void> redirect(@PathVariable String code) {
        String target = service.resolve(code)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Short URL not found"));
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, URI.create(target).toString())
                .build();
    }
}