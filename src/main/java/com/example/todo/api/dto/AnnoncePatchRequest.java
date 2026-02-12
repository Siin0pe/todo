package com.example.todo.api.dto;

import com.example.todo.model.Annonce;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

@Schema(name = "AnnoncePatchRequest", description = "Payload de mise a jour partielle d'une annonce")
public class AnnoncePatchRequest {
    @Schema(description = "Titre de l'annonce", example = "Appartement T2 renove", maxLength = 64)
    @Size(max = 64)
    private String title;

    @Schema(description = "Description detaillee", example = "Cuisine equipee et balcon", maxLength = 256)
    @Size(max = 256)
    private String description;

    @Schema(description = "Adresse", example = "12 rue Victor Hugo, Lyon", maxLength = 64)
    @Size(max = 64)
    private String adress;

    @Schema(description = "Email de contact", example = "agent@example.com", maxLength = 64)
    @Email
    @Size(max = 64)
    private String mail;
    @Schema(description = "Identifiant categorie", example = "3")
    private Long categoryId;
    @Schema(description = "Statut de l'annonce", example = "PUBLISHED", allowableValues = {"DRAFT", "PUBLISHED", "ARCHIVED"})
    private Annonce.Status status;

    public AnnoncePatchRequest() {
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

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Annonce.Status getStatus() {
        return status;
    }

    public void setStatus(Annonce.Status status) {
        this.status = status;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final AnnoncePatchRequest dto = new AnnoncePatchRequest();

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

        public Builder categoryId(Long categoryId) {
            dto.setCategoryId(categoryId);
            return this;
        }

        public Builder status(Annonce.Status status) {
            dto.setStatus(status);
            return this;
        }

        public AnnoncePatchRequest build() {
            return dto;
        }
    }
}
