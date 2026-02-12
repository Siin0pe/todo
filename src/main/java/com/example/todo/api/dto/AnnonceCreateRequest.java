package com.example.todo.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(name = "AnnonceCreateRequest", description = "Payload de creation d'une annonce")
public class AnnonceCreateRequest {
    @Schema(description = "Titre de l'annonce", example = "Appartement T2 centre ville", maxLength = 64)
    @NotBlank
    @Size(max = 64)
    private String title;

    @Schema(description = "Description detaillee", example = "Bel appartement proche des commerces", maxLength = 256)
    @NotBlank
    @Size(max = 256)
    private String description;

    @Schema(description = "Adresse", example = "10 rue des Fleurs, Paris", maxLength = 64)
    @NotBlank
    @Size(max = 64)
    private String adress;

    @Schema(description = "Email de contact", example = "contact@example.com", maxLength = 64)
    @NotBlank
    @Email
    @Size(max = 64)
    private String mail;

    @Schema(description = "Identifiant auteur (ignore si derive du token cote serveur)", example = "1")
    @NotNull
    private Long authorId;

    @Schema(description = "Identifiant categorie", example = "2")
    @NotNull
    private Long categoryId;

    public AnnonceCreateRequest() {
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
        private final AnnonceCreateRequest dto = new AnnonceCreateRequest();

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

        public Builder authorId(Long authorId) {
            dto.setAuthorId(authorId);
            return this;
        }

        public Builder categoryId(Long categoryId) {
            dto.setCategoryId(categoryId);
            return this;
        }

        public AnnonceCreateRequest build() {
            return dto;
        }
    }
}
