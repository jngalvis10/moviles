package com.leptons.keepmesafe.Models;

import com.google.android.gms.location.places.Place;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

public class ViajeM {
    private String destino;
    private double latitud;
    private double longitud;
    private Contacto contacto;
    private Date horaSalida;
    private ArrayList<Punto> puntos;

    public ViajeM(){
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public ViajeM(String destino, double latitud, double longitud, Contacto contacto, Date horaSalida, ArrayList<Punto> puntos){
        this.destino = destino;
        this.latitud = latitud;
        this.longitud = longitud;
        this.contacto = contacto;
        this.horaSalida = horaSalida;
        this.puntos = puntos;
    }

    public String getDestino() {
        return destino;
    }

    public double getLatitud() {
        return latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    public Date getHoraSalida() {
        return horaSalida;
    }

    public ArrayList<Punto> getPuntos() {
        return puntos;
    }

    public void setDestino(String destino) {
        this.destino = destino;
    }

    public void setLatitud(double latitud) {
        this.latitud = latitud;
    }

    public void setLongitud(double longitud) {
        this.longitud = longitud;
    }

    public void setHoraSalida(Date horaSalida) {
        this.horaSalida = horaSalida;
    }

    public void setPuntos(ArrayList<Punto> puntos) {
        this.puntos = puntos;
    }

    public Contacto getContacto() {
        return contacto;
    }

    public void setContacto(Contacto contacto) {
        this.contacto = contacto;
    }
}
