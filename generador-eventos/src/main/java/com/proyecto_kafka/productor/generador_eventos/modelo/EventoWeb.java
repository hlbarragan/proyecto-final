package com.proyecto_kafka.productor.generador_eventos.modelo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventoWeb {
    private String usuario;
    private Long hora;
    private String evento;
}
