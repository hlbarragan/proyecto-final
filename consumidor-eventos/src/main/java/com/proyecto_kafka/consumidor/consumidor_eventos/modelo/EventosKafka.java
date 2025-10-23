package com.proyecto_kafka.consumidor.consumidor_eventos.modelo;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "eventos_kafka")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventosKafka {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "topico")
    private String topico;

    @Column(name = "fecha")
    private Instant fecha;

    @Column(name = "evento")
    private String evento;

    @PrePersist
    private void onCreate() {
        this.fecha = Instant.now();
    }
}
