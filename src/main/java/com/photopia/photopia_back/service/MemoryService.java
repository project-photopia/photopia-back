package com.photopia.photopia_back.service;

import com.photopia.photopia_back.dto.MemoryMediaResponse;
import com.photopia.photopia_back.dto.MemoryResponse;
import com.photopia.photopia_back.model.Capsule;
import com.photopia.photopia_back.model.Media;
import com.photopia.photopia_back.model.User;
import com.photopia.photopia_back.repository.CapsuleRepository;
import com.photopia.photopia_back.repository.MediaRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MemoryService {

    private final CapsuleRepository capsuleRepository;
    private final MediaRepository mediaRepository;
    private final R2Service r2Service;

    public MemoryService(CapsuleRepository capsuleRepository,
                         MediaRepository mediaRepository,
                         R2Service r2Service) {
        this.capsuleRepository = capsuleRepository;
        this.mediaRepository = mediaRepository;
        this.r2Service = r2Service;
    }

    public List<MemoryResponse> getMemoriesForUser(UUID userId) {
        List<Capsule> capsules = capsuleRepository.findByUserOrMember(userId);
        if (capsules.isEmpty()) return Collections.emptyList();

        List<UUID> capsuleIds = capsules.stream().map(Capsule::getId).toList();
        Map<UUID, Capsule> capsuleMap = capsules.stream()
                .collect(Collectors.toMap(Capsule::getId, c -> c));

        Instant now = Instant.now();
        List<MemoryResponse> memories = new ArrayList<>();

        // 1 year ago (±2 days)
        Instant oneYearAgoStart = now.minus(367, ChronoUnit.DAYS);
        Instant oneYearAgoEnd = now.minus(363, ChronoUnit.DAYS);
        List<Media> oneYearMedias = mediaRepository.findByCapsuleIdInAndTakenAtBetween(
                capsuleIds, oneYearAgoStart, oneYearAgoEnd);
        addGroupedMemories(memories, oneYearMedias, capsuleMap, "one_year_ago", "1 year ago");

        // 6 months ago (±2 days)
        Instant sixMonthsAgoStart = now.minus(184, ChronoUnit.DAYS);
        Instant sixMonthsAgoEnd = now.minus(180, ChronoUnit.DAYS);
        List<Media> sixMonthsMedias = mediaRepository.findByCapsuleIdInAndTakenAtBetween(
                capsuleIds, sixMonthsAgoStart, sixMonthsAgoEnd);
        addGroupedMemories(memories, sixMonthsMedias, capsuleMap, "six_months_ago", "6 months ago");

        // 3 months ago (±2 days)
        Instant threeMonthsAgoStart = now.minus(92, ChronoUnit.DAYS);
        Instant threeMonthsAgoEnd = now.minus(88, ChronoUnit.DAYS);
        List<Media> threeMonthsMedias = mediaRepository.findByCapsuleIdInAndTakenAtBetween(
                capsuleIds, threeMonthsAgoStart, threeMonthsAgoEnd);
        addGroupedMemories(memories, threeMonthsMedias, capsuleMap, "three_months_ago", "3 months ago");

        // This week last year (±3 days from today minus 1 year)
        Instant weekLastYearStart = now.minus(368, ChronoUnit.DAYS);
        Instant weekLastYearEnd = now.minus(358, ChronoUnit.DAYS);
        List<Media> weekLastYearMedias = mediaRepository.findByCapsuleIdInAndTakenAtBetween(
                capsuleIds, weekLastYearStart, weekLastYearEnd);
        // Exclude medias already in "1 year ago"
        Set<UUID> oneYearIds = oneYearMedias.stream().map(Media::getId).collect(Collectors.toSet());
        weekLastYearMedias = weekLastYearMedias.stream()
                .filter(m -> !oneYearIds.contains(m.getId()))
                .toList();
        addGroupedMemories(memories, weekLastYearMedias, capsuleMap, "this_week_last_year", "This week last year");

        // Best moments — top reacted medias across all capsules (limit 5 per capsule)
        List<Media> topMedias = mediaRepository.findTopMediasByCapsuleIds(capsuleIds);
        Map<UUID, List<Media>> topByCapsule = topMedias.stream()
                .collect(Collectors.groupingBy(m -> m.getCapsule().getId()));
        for (Map.Entry<UUID, List<Media>> entry : topByCapsule.entrySet()) {
            Capsule capsule = capsuleMap.get(entry.getKey());
            if (capsule == null) continue;
            List<Media> top = entry.getValue().stream().limit(5).toList();
            if (top.size() >= 3) {
                memories.add(new MemoryResponse(
                        "memory-best-" + capsule.getId(),
                        "Best moments",
                        capsule.getName(),
                        "best_of_capsule",
                        capsule.getId(),
                        capsule.getName(),
                        top.stream().map(this::mapToMemoryMedia).toList()
                ));
            }
        }

        return memories;
    }

    private void addGroupedMemories(List<MemoryResponse> memories, List<Media> medias,
                                     Map<UUID, Capsule> capsuleMap, String trigger, String title) {
        if (medias.isEmpty()) return;

        Map<UUID, List<Media>> byCapsule = medias.stream()
                .collect(Collectors.groupingBy(m -> m.getCapsule().getId()));

        for (Map.Entry<UUID, List<Media>> entry : byCapsule.entrySet()) {
            Capsule capsule = capsuleMap.get(entry.getKey());
            if (capsule == null) continue;

            List<MemoryMediaResponse> mediaResponses = entry.getValue().stream()
                    .limit(10)
                    .map(this::mapToMemoryMedia)
                    .toList();

            memories.add(new MemoryResponse(
                    "memory-" + trigger + "-" + capsule.getId(),
                    title,
                    capsule.getName(),
                    trigger,
                    capsule.getId(),
                    capsule.getName(),
                    mediaResponses
            ));
        }
    }

    private MemoryMediaResponse mapToMemoryMedia(Media media) {
        User uploader = media.getUser();
        return new MemoryMediaResponse(
                media.getId(),
                r2Service.generatePresignedGetUrl(media.getOriginalUrl()),
                r2Service.generatePresignedGetUrl(media.getPreviewUrl()),
                r2Service.generatePresignedGetUrl(media.getThumbnailUrl()),
                media.getMediaType() != null ? media.getMediaType().name() : null,
                media.getWidth(),
                media.getHeight(),
                media.getLatitude(),
                media.getLongitude(),
                media.getLocationName(),
                media.getTakenAt(),
                uploader != null ? uploader.getUsername() : null,
                uploader != null ? uploader.getAvatarUrl() : null
        );
    }
}
