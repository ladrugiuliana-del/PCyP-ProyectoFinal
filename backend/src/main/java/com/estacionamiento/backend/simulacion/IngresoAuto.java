package com.estacionamiento.backend.simulacion;
class IngresoAuto implements Runnable {
    private final Estacionamiento estacionamiento;
    private final int idAuto;

    public IngresoAuto(Estacionamiento estacionamiento, int idAuto) {
        this.estacionamiento = estacionamiento;
        this.idAuto = idAuto;
    }

    @Override
    public void run() {
        // Ejecuta la acción de producir/estacionar
        estacionamiento.ingresarAuto(idAuto);
    }
}