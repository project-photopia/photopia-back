package com.photopia.photopia_back.dto;

public record PresignedUrlResponse(
                String uploadUrl,
                String key,
                String signature,
                String previewUploadUrl,
                String previewKey,
                String previewSignature) {
}
