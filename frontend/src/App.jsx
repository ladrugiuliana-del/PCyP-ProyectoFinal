import React, { useEffect, useState } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

export default function EstacionamientoView() {
    // Inicializamos con 3 plazas vacías (ceros) para que se dibujen desde el inicio
    const [plazas, setPlazas] = useState([0, 0, 0]);
    const [capacidadTotal, setCapacidadTotal] = useState(0);
    const [mensajes, setMensajes] = useState([]);
    const [conectado, setConectado] = useState(false);

    useEffect(() => {
        const socket = new SockJS('http://localhost:8080/ws-estacionamiento');
        const stompClient = new Client({
            webSocketFactory: () => socket,
            onConnect: () => {
                setConectado(true);
                stompClient.subscribe('/topic/estacionamiento', (mensaje) => {
                    const data = JSON.parse(mensaje.body);
                   console.log("Datos recibidos del backend:", data); // <-- Agrega esto para inspeccionar
                    setPlazas(data.plazas);
                    setMensajes((prev) => [data.mensajeEvento, ...prev.slice(0, 9)]);
                    setCapacidadTotal(data.capacidadTotal);
                });
            },
            onStompError: (frame) => {
                console.error('Error de STOMP: ' + frame.headers['message']);
            },
        });

        stompClient.activate();

        return () => {
            stompClient.deactivate();
        };
    }, []);

    const iniciarSimulacion = async () => {
        try {
            await fetch('http://localhost:8080/api/estacionamiento/iniciar', {
                method: 'POST',
            });
        } catch (error) {
            console.error('Error al iniciar la simulación:', error);
        }
    };

    return (
        <div style={{ padding: '30px', fontFamily: 'Arial, sans-serif', maxWidth: '800px', margin: '0 auto' }}>
            <h1>Simulación de Estacionamiento</h1>
            <p>Estado del WebSocket: <b>{conectado ? '🟢 Conectado' : '🔴 Desconectado'}</b></p>
            
            <button 
                onClick={iniciarSimulacion}
                style={{ 
                    padding: '12px 24px', 
                    fontSize: '16px', 
                    cursor: 'pointer', 
                    backgroundColor: '#4CAF50', 
                    color: 'white', 
                    border: 'none', 
                    borderRadius: '6px',
                    fontWeight: 'bold',
                    marginBottom: '20px'
                }}
            >
                Iniciar Simulación de Autos
            </button>

            <h2>Plazas del Estacionamiento</h2>
            <div style={{ display: 'flex', gap: '20px', margin: '20px 0' }}>
                {Array.isArray(plazas) && plazas.map((idAuto, index) => {
                    const ocupada = idAuto > 0;
                    return (
                        <div 
                            key={index} 
                            style={{
                                width: '110px',
                                height: '130px',
                                backgroundColor: ocupada ? '#ff4d4d' : '#4CAF50',
                                color: 'white',
                                display: 'flex',
                                flexDirection: 'column',
                                alignItems: 'center',
                                justifyContent: 'center',
                                borderRadius: '10px',
                                fontWeight: 'bold',
                                boxShadow: '0 4px 8px rgba(0,0,0,0.15)',
                                transition: 'background-color 0.3s ease'
                            }}
                        >
                            <span style={{ fontSize: '28px' }}>{ocupada ? '🚗' : '🅿️'}</span>
                            <span style={{ marginTop: '5px' }}>
                                {ocupada ? `Auto #${idAuto}` : `Plaza ${index + 1}`}
                            </span>
                            <span style={{ fontSize: '12px', marginTop: '4px', opacity: 0.9 }}>
                                {ocupada ? 'Ocupada' : 'Libre'}
                            </span>
                        </div>
                    );
                })}
            </div>

            <h3>Registro de Eventos en Tiempo Real:</h3>
            <ul style={{ 
                background: '#f9f9f9', 
                border: '1px solid #ddd', 
                padding: '15px 25px', 
                borderRadius: '8px', 
                listStyleType: 'disc', 
                maxHeight: '220px', 
                overflowY: 'auto' 
            }}>
                {mensajes.map((msg, index) => (
                    <li key={index} style={{ padding: '6px 0', borderBottom: '1px solid #eee', fontSize: '14px' }}>
                        {msg}
                    </li>
                ))}
            </ul>
        </div>
    );
}