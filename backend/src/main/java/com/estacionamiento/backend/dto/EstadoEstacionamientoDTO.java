package com.estacionamiento.backend.dto;

import java.util.List;

public class EstadoEstacionamientoDTO {
    private int capacidadTotal;
    private List<Boolean> plazas; // true = ocupado, false = libre
    private String mensajeEvento;  // Mensaje descriptivo para la interfaz (ej: "Auto #1 se estacionó")
    private String hora;

    public EstadoEstacionamientoDTO(int capacidadTotal, List<Boolean> plazas, String mensajeEvento, String hora) {
        this.capacidadTotal = capacidadTotal;
        this.plazas = plazas;
        this.mensajeEvento = mensajeEvento;
        this.hora = hora;
    }

    // Getters y Setters
    public int getCapacidadTotal() { return capacidadTotal; }
    public void setCapacidadTotal(int capacidadTotal) { this.capacidadTotal = capacidadTotal; }

    public List<Boolean> getPlazas() { return plazas; }
    public void setPlazas(List<Boolean> plazas) { this.plazas = plazas; }

    public String getMensajeEvento() { return mensajeEvento; }
    public void setMensajeEvento(String mensajeEvento) { this.mensajeEvento = mensajeEvento; }

    public String getHora() { return hora; }
    public void setHora(String hora) { this.hora = hora; }
}
