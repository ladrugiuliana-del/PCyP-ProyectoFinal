package com.estacionamiento.backend.dto;

import java.util.List;

public class EstadoEstacionamientoDTO {
    private int capacidadTotal;
    private List<Integer> plazas;
    private String mensajeEvento;
    private String hora;

    public EstadoEstacionamientoDTO(int capacidadTotal, List<Integer> plazas, String mensajeEvento, String hora) {
        this.capacidadTotal = capacidadTotal;
        this.plazas = plazas;
        this.mensajeEvento = mensajeEvento;
        this.hora = hora;
    }

    public int getCapacidadTotal() { return capacidadTotal; }
    public List<Integer> getPlazas() { return plazas; } // <- Vital para que el JSON incluya "plazas"
    public String getMensajeEvento() { return mensajeEvento; }
    public String getHora() { return hora; }
}