package com.leptons.keepmesafe.Models;

public class Contacto {

    public String username;
    public String email;
    public long phone;

    public Contacto() {
        // Default constructor required for calls to DataSnapshot.getValue(Contacto.class)
    }

    public Contacto(String username, String email, long phone) {
        this.username = username;
        this.email = email;
        this.phone = phone;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public long getPhone() {
        return phone;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(long phone) {
        this.phone = phone;
    }
}
