package com.proyecto_kafka.consumidor.consumidor_eventos.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.proyecto_kafka.consumidor.consumidor_eventos.modelo.EventosKafka;

public interface RepositorioMensajesKafka extends JpaRepository<EventosKafka, Long> {

}
