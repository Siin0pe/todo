package com.example.todo.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "AnnonceResponse", description = "Representation d'une annonce")
public class AnnonceResponse {
    @Schema(example = "10")
    private Long id;
    @Schema(example = "Appartement T2 centre ville")
    private String title;
    @Schema(example = "Bel appartement proche metro")
    private String description;
    @Schema(example = "10 rue des Fleurs, Paris")
    private String adress;
    @Schema(example = "contact@example.com")
    private String mail;
    @Schema(description = "Date de creation", example = "2026-02-12T16:00:00Z")
    private String date;
    @Schema(example = "PUBLISHED")
    private String status;
    @Schema(example = "1")
    private Long authorId;
    @Schema(example = "2")
    private Long categoryId;

    public AnnonceResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAdress() {
        return adress;
    }

    public void setAdress(String adress) {
        this.adress = adress;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final AnnonceResponse dto = new AnnonceResponse();

        public Builder id(Long id) {
            dto.setId(id);
            return this;
        }

        public Builder title(String title) {
            dto.setTitle(title);
            return this;
        }

        public Builder description(String description) {
            dto.setDescription(description);
            return this;
        }

        public Builder adress(String adress) {
            dto.setAdress(adress);
            return this;
        }

        public Builder mail(String mail) {
            dto.setMail(mail);
            return this;
        }

        public Builder date(String date) {
            dto.setDate(date);
            return this;
        }

        public Builder status(String status) {
            dto.setStatus(status);
            return this;
        }

        public Builder authorId(Long authorId) {
            dto.setAuthorId(authorId);
            return this;
        }

        public Builder categoryId(Long categoryId) {
            dto.setCategoryId(categoryId);
            return this;
        }

        public AnnonceResponse build() {
            return dto;
        }
    }
}
