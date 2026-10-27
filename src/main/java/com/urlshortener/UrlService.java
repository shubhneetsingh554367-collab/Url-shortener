package com.urlshortener;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.security.SecureRandom;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UrlService {

    private static final String ALPHABET =
            "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int CODE_LENGTH = 6;

    private final SecureRandom random = new SecureRandom();
    private final ConcurrentHashMap<String, String> store = new ConcurrentHashMap<>();

    public String shorten(String longUrl) {
        validate(longUrl);
        while (true) {
            String code = generateCode();
            // putIfAbsent makes collision handling thread-safe
            if (store.putIfAbsent(code, longUrl) == null) {
                return code;
            }
        }
    }

    public Optional<String> resolve(String code) {
        return Optional.ofNullable(store.get(code));
    }

    private String generateCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }

    private void validate(String url) {
        try {
            URI uri = URI.create(url);
            String scheme = uri.getScheme();
            if (scheme == null || !(scheme.equals("http") || scheme.equals("https"))
                    || uri.getHost() == null) {
                throw new IllegalArgumentException();
            }
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid URL");
        }
    }
}