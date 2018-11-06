package com.leptons.keepmesafe.Models;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Usuario {

    public String userId;
    public String username;
    public String status;
    public String email;
    public long phone;
    public int tiempoEspera;
    public int distancia;
    public ArrayList<Contacto> contactos;

    public Usuario() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Usuario(String userId, String username, String status, String email, long phone, int tiempoEspera, int distancia, ArrayList<Contacto> contactos) {
        this.userId = userId;
        this.username = username;
        this.status = status;
        this.email = email;
        this.phone = phone;
        this.tiempoEspera = tiempoEspera;
        this.distancia = distancia;
        this.contactos = contactos;
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getStatus() {
        return status;
    }

    public String getEmail() {
        return email;
    }

    public long getPhone() {
        return phone;
    }

    public int getTiempoEspera() {
        return tiempoEspera;
    }

    public int getDistancia() {
        return distancia;
    }

    public ArrayList<Contacto> getContactos() {
        return contactos;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(long phone) {
        this.phone = phone;
    }

    public void setTiempoEspera(int tiempoEspera) {
        this.tiempoEspera = tiempoEspera;
    }

    public void setDistancia(int distancia) {
        this.distancia = distancia;
    }

    public void setContactos(ArrayList<Contacto> contactos) {
        this.contactos = contactos;
    }
}
