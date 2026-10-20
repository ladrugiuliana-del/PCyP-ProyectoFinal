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
    private final boolean[] plazas; // true = ocupada, false = libre

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
        this.plazas = new boolean[capacidad];
        this.messagingTemplate = messagingTemplate;
        
        this.mutex = new Semaphore(1);                   
        this.lugaresLibres = new Semaphore(capacidad);   
        this.lugaresOcupados = new Semaphore(0);         
    }

    // Método auxiliar para notificar cambios a React por WebSocket
    private void notificarCambio(String mensaje) {
        List<Boolean> listaPlazas = new ArrayList<>();
        for (boolean plaza : plazas) {
            listaPlazas.add(plaza);
        }
        EstadoEstacionamientoDTO dto = new EstadoEstacionamientoDTO(capacidad, listaPlazas, mensaje, getHora());
        
        // Envía el mensaje a todos los clientes React suscritos al canal /topic/estacionamiento
        if (messagingTemplate != null) {
            messagingTemplate.convertAndSend("/topic/estacionamiento", dto);
        }
    }

    public int ingresarAuto(int idAuto) {
        int plazaAsignada = -1;
        try {
            String msgLlegada = "🚗 Auto #" + idAuto + " LLEGÓ y busca lugar...";
            System.out.println("[" + getHora() + "] " + msgLlegada);
            notificarCambio(msgLlegada);

            boolean adquirido = lugaresLibres.tryAcquire(3, TimeUnit.SECONDS);
            
            if (!adquirido) {
                String msgRetiro = "❌ Auto #" + idAuto + " esperó 3s, estacionamiento lleno y se RETIRÓ.";
                System.out.println("[" + getHora() + "] " + msgRetiro);
                notificarCambio(msgRetiro);
                return -1; 
            }

            mutex.acquire();         

            for (int i = 0; i < capacidad; i++) {
                if (!plazas[i]) {
                    plazas[i] = true; 
                    plazaAsignada = i;
                    break;
                }
            }
            
            String msgEstacionado = "✅ Auto #" + idAuto + " se ESTACIONÓ en la plaza [" + (plazaAsignada + 1) + "]";
            System.out.println("[" + getHora() + "] " + msgEstacionado);
            notificarCambio(msgEstacionado);

            mutex.release();         
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

            plazas[plazaLiberar] = false;
            
            String msgSalida = "💨 Auto #" + idAuto + " SALIÓ de la plaza [" + (plazaLiberar + 1) + "]";
            System.out.println("[" + getHora() + "] " + msgSalida);
            notificarCambio(msgSalida);

            mutex.release();           
            lugaresLibres.release();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}