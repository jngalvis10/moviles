package com.leptons.keepmesafe.Models;

import java.util.Date;

public class Punto {

    double latitud;
    double longitud;
    Date hora;

    public Punto(){ }

    public Punto (double latitud, double longitud, Date hora){
        this.latitud=latitud;
        this.longitud=longitud;
        this.hora = hora;
    }

    public void setLatitud(double latitud) {
        this.latitud = latitud;
    }

    public void setLongitud(double longitud) {
        this.longitud = longitud;
    }

    public void setHora(Date hora) {
        this.hora = hora;
    }

    public double getLatitud() {
        return latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    public Date getHora() {
        return hora;
    }
}
