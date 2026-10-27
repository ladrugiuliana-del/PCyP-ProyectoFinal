package com.estacionamiento.backend.controlador;

import java.util.Random;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.estacionamiento.backend.simulacion.Estacionamiento;

@RestController
@RequestMapping("/api/estacionamiento")
@CrossOrigin(origins = "http://localhost:5173") // Permite la conexión desde el cliente React (Vite)
public class ControladorEstacionamiento {

    private final SimpMessagingTemplate messagingTemplate;

    public ControladorEstacionamiento(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @PostMapping("/iniciar")
    public String iniciarSimulacion() {
        int capacidadEstacionamiento = 3;
        int totalAutos = 15;         // Total de autos que participarán en la simulación

        Estacionamiento estacionamiento = new Estacionamiento(capacidadEstacionamiento, messagingTemplate);

        // Ejecutamos la simulación en un hilo separado para no bloquear la respuesta HTTP del servidor
        new Thread(() -> {
            for (int i = 1; i <= totalAutos; i++) {
                final int idAuto = i;
                
                Thread tAuto = new Thread(() -> {
                    int idPlaza = estacionamiento.ingresarAuto(idAuto);
                    
                    // Si consiguió plaza, espera un momento y se retira
                    if (idPlaza != -1) {
                        try {
                            Thread.sleep(new Random().nextInt(3000) + 5000); // Permanece unos segundos
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        estacionamiento.retirarAuto(idAuto, idPlaza);
                    }
                }, "Auto-" + idAuto);
                tAuto.start();

                // Pausa corta entre la llegada de cada auto para simular flujo real
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }, "Hilo-Productor-Autos").start();

        return "Simulación de estacionamiento iniciada con éxito.";
    }
}
