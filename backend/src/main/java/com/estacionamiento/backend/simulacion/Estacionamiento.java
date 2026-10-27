package com.estacionamiento.backend.simulacion;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.estacionamiento.backend.dto.EstadoEstacionamientoDTO;

public class Estacionamiento {
    private final int capacidad;
    private final int[] plazas; // 0 = libre, >0 = ID del auto ocupante

    private final Semaphore mutex;          
    private final Semaphore lugaresLibres;   
    private final Semaphore lugaresOcupados; 

    // Dependencia de Spring para enviar mensajes por WebSocket
    private final SimpMessagingTemplate messagingTemplate;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    private String getHora() {
        return LocalTime.now().format(TIME_FORMATTER);
    }

    // Constructor que recibe la capacidad y el template de WebSockets de Spring
    public Estacionamiento(int capacidad, SimpMessagingTemplate messagingTemplate) {
        this.capacidad = capacidad;
        this.plazas = new int[capacidad]; // Por defecto se inicializa en 0 (libres)
        this.messagingTemplate = messagingTemplate;
        
        this.mutex = new Semaphore(1);                   
        this.lugaresLibres = new Semaphore(capacidad);   
        this.lugaresOcupados = new Semaphore(0);         
    }

    // Método auxiliar para notificar cambios a React por WebSocket
    private void notificarCambio(String mensaje) {
        List<Integer> listaPlazas = new ArrayList<>();
    
        // Verificamos que el arreglo 'plazas' no sea nulo y recorremos sus elementos
        if (plazas != null) {
            for (int plaza : plazas) {
                listaPlazas.add(plaza);
            }
        }

        EstadoEstacionamientoDTO dto = new EstadoEstacionamientoDTO(capacidad, listaPlazas, mensaje, getHora());
        
        if (messagingTemplate != null) {
            messagingTemplate.convertAndSend("/topic/estacionamiento", dto);
        }
    } // <-- Aquí cierra correctamente notificarCambio

    public int ingresarAuto(int idAuto) {
        int plazaAsignada = -1;
        try {
            String msgLlegada = "🚗 Auto #" + idAuto + " LLEGÓ y busca lugar...";
            imprimirYNotificar(msgLlegada);

            // Intenta adquirir un lugar libre durante 3 segundos
            boolean adquirido = lugaresLibres.tryAcquire(3, TimeUnit.SECONDS);
            
            if (!adquirido) {
                String msgRetiro = "❌ Auto #" + idAuto + " esperó 3s, estacionamiento lleno y se RETIRÓ.";
                imprimirYNotificar(msgRetiro);
                return -1; 
            }

            // Sección crítica protegida por el mutex para asignar la plaza exacta
            mutex.acquire(); 
            try {
                for (int i = 0; i < capacidad; i++) {
                    if (plazas[i] == 0) {
                        plazas[i] = idAuto; // Guardamos el ID del auto
                        plazaAsignada = i;
                        break;
                    }
                }
                
                String msgEstacionado = "✅ Auto #" + idAuto + " se ESTACIONÓ en la plaza [" + (plazaAsignada + 1) + "]";
                imprimirYNotificar(msgEstacionado);
            } finally {
                mutex.release(); 
            }

            lugaresOcupados.release();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return plazaAsignada; 
    }

    public void retirarAuto(int idAuto, int plazaLiberar) {
        try {
            lugaresOcupados.acquire(); 
            
            mutex.acquire(); 
            try {
                plazas[plazaLiberar] = 0;
                
                String msgSalida = "💨 Auto #" + idAuto + " SALIÓ de la plaza [" + (plazaLiberar + 1) + "]";
                imprimirYNotificar(msgSalida);
            } finally {
                mutex.release(); 
            }
            
            lugaresLibres.release();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // Método sincronizado auxiliar para evitar que los mensajes de consola se mezclen
    private synchronized void imprimirYNotificar(String mensaje) {
        System.out.println("[" + getHora() + "] " + mensaje);
        notificarCambio(mensaje);
    }
}