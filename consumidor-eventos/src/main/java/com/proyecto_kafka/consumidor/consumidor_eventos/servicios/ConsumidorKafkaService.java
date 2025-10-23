package com.proyecto_kafka.consumidor.consumidor_eventos.servicios;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.proyecto_kafka.consumidor.consumidor_eventos.jpa.RepositorioMensajesKafka;
import com.proyecto_kafka.consumidor.consumidor_eventos.modelo.EventosKafka;

@Service
public class ConsumidorKafkaService {
    private final RepositorioMensajesKafka repositorioMensajesKafka;

    public ConsumidorKafkaService(RepositorioMensajesKafka repositorioMensajesKafka) {
        this.repositorioMensajesKafka = repositorioMensajesKafka;
    }

    @KafkaListener(topics = "eventos-pagina", groupId = "${spring.kafka.consumer.group-id}")
    public void recibir(String mensaje) {
        System.out.println("Se recibió un mensaje: " + mensaje);

        EventosKafka eventosKafka = new EventosKafka();
        eventosKafka.setTopico("eventos-pagina");
        eventosKafka.setEvento(mensaje);

        try {
            this.repositorioMensajesKafka.save(eventosKafka);
            System.out.println("Mensaje guardado en base de datos con id: " + eventosKafka.getId());
        } catch (Exception e) {
            System.out.println("Error en la persistencia del mensaje recibido");
            e.printStackTrace();
        }
    }
}
