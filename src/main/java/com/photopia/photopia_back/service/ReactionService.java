package com.photopia.photopia_back.service;

import com.photopia.photopia_back.model.Media;
import com.photopia.photopia_back.model.Reaction;
import com.photopia.photopia_back.model.User;
import com.photopia.photopia_back.repository.ReactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class ReactionService {

    private final ReactionRepository reactionRepository;
    private final MediaService mediaService;

    public ReactionService(ReactionRepository reactionRepository, MediaService mediaService) {
        this.reactionRepository = reactionRepository;
        this.mediaService = mediaService;
    }

    @Transactional
    public void toggleReaction(UUID mediaId, User user, String emoji) {
        Media media = mediaService.getMediaById(mediaId);

        Optional<Reaction> existingReaction = reactionRepository.findByMediaIdAndUserIdAndEmoji(
                mediaId, user.getId(), emoji);

        if (existingReaction.isPresent()) {
            reactionRepository.delete(existingReaction.get());
            media.setReactionCount(Math.max(0, media.getReactionCount() - 1));
        } else {
            Reaction reaction = Reaction.builder()
                    .media(media)
                    .user(user)
                    .emoji(emoji)
                    .build();
            reactionRepository.save(reaction);
            media.setReactionCount(media.getReactionCount() + 1);
        }
    }
}
