package com.estacionamiento.backend.simulacion;

import java.util.Random;

class SalidaAuto implements Runnable {
    private final Estacionamiento estacionamiento;
    private final int idAuto;
    private final int idPlaza;

    public SalidaAuto(Estacionamiento estacionamiento, int idAuto, int idPlaza) {
        this.estacionamiento = estacionamiento;
        this.idAuto = idAuto;
        this.idPlaza = idPlaza;
    }

    @Override
    public void run() {
        try {
            // Simula el tiempo que el auto permanece estacionado (entre 4 y 8 segundos)
            Thread.sleep(new Random().nextInt(4000) + 4000); 
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        // Ejecuta la acción de consumir/retirar
        estacionamiento.retirarAuto(idAuto, idPlaza);
    }
}