package com.example.sashabf.dto;

public class PasswordChangeRequest {
    private String oldPassword;
    private String newPassword;

    // Constructores
    public PasswordChangeRequest() {}

    public PasswordChangeRequest(String oldPassword, String newPassword) {
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
    }

    // Getters y Setters (IMPORTANTES para que Spring lea el JSON)
    public String getOldPassword() { return oldPassword; }
    public void setOldPassword(String oldPassword) { this.oldPassword = oldPassword; }

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}