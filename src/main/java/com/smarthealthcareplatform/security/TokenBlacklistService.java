package com.smarthealthcareplatform.security;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenBlacklistService {

    private final Map<String, Instant> revokedTokenIds = new ConcurrentHashMap<>();

    public void revoke(String tokenId, Instant expiresAt) {
        if (tokenId == null || tokenId.isBlank() || expiresAt == null) {
            return;
        }
        revokedTokenIds.put(tokenId, expiresAt);
        cleanupExpiredEntries();
    }

    public boolean isRevoked(String tokenId) {
        cleanupExpiredEntries();
        if (tokenId == null || tokenId.isBlank()) {
            return false;
        }
        Instant expiresAt = revokedTokenIds.get(tokenId);
        return expiresAt != null && expiresAt.isAfter(Instant.now());
    }

    private void cleanupExpiredEntries() {
        Instant now = Instant.now();
        revokedTokenIds.entrySet().removeIf(entry -> entry.getValue() == null || !entry.getValue().isAfter(now));
    }
}
