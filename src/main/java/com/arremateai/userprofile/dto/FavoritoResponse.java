package com.arremateai.userprofile.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record FavoritoResponse(
        UUID id,
        UUID userId,
        UUID imovelId,
        LocalDateTime createdAt
) {
}
