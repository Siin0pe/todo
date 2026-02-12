package com.example.todo.api.dto;

import com.example.todo.model.Annonce;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class AnnonceUpdateRequest {
    @NotBlank
    @Size(max = 64)
    private String title;

    @NotBlank
    @Size(max = 256)
    private String description;

    @NotBlank
    @Size(max = 64)
    private String adress;

    @NotBlank
    @Email
    @Size(max = 64)
    private String mail;

    @NotNull
    private Long categoryId;
    private Annonce.Status status;

    public AnnonceUpdateRequest() {
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
        private final AnnonceUpdateRequest dto = new AnnonceUpdateRequest();

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

        public AnnonceUpdateRequest build() {
            return dto;
        }
    }
}
