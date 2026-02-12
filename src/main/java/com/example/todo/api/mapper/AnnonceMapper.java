package com.example.todo.api.mapper;

import com.example.todo.api.dto.AnnonceResponse;
import com.example.todo.model.Annonce;
import com.example.todo.model.Category;
import com.example.todo.model.User;

import java.sql.Timestamp;

public final class AnnonceMapper {
    private AnnonceMapper() {
    }

    public static AnnonceResponse toResponse(Annonce annonce) {
        if (annonce == null) {
            return null;
        }
        return AnnonceResponse.builder()
                .id(annonce.getId())
                .title(annonce.getTitle())
                .description(annonce.getDescription())
                .adress(annonce.getAdress())
                .mail(annonce.getMail())
                .date(formatTimestamp(annonce.getDate()))
                .status(formatStatus(annonce.getStatus()))
                .authorId(resolveUserId(annonce.getAuthor()))
                .categoryId(resolveCategoryId(annonce.getCategory()))
                .build();
    }

    private static String formatTimestamp(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        return timestamp.toInstant().toString();
    }

    private static String formatStatus(Annonce.Status status) {
        return status == null ? null : status.name();
    }

    private static Long resolveUserId(User user) {
        return user == null ? null : user.getId();
    }

    private static Long resolveCategoryId(Category category) {
        return category == null ? null : category.getId();
    }
}
