package com.example.todo.service;

import com.example.todo.model.Annonce;

import java.time.Instant;

public record AnnonceSearchCriteria(
        String q,
        Annonce.Status status,
        Long categoryId,
        Long authorId,
        Instant fromDate,
        Instant toDate
) {
}
