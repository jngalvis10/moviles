package com.leptons.keepmesafe.Models;

public class Excepcion {
    public int metrosPermitidos;
    public int tiempoDemora;
    public int tipo;

    //tipo 1 excepción de demora y tipo 2 excepción de metros

    public Excepcion(int pmetrosPermitidos, int ptiempoDemora){
        if (pmetrosPermitidos == 0 && ptiempoDemora>0){
            metrosPermitidos=pmetrosPermitidos;
            tiempoDemora=ptiempoDemora;
            tipo=1;
        }
        else {
            metrosPermitidos=pmetrosPermitidos;
            tiempoDemora=ptiempoDemora;
            tipo=2;
        }

    }
}
