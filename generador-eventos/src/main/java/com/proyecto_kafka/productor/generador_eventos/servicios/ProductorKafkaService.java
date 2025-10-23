package com.proyecto_kafka.productor.generador_eventos.servicios;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class ProductorKafkaService {
    private static final String TOPICO = "eventos-pagina";

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public void enviarEventoPagina(String evento) {
        kafkaTemplate.send(TOPICO, evento);
        System.out.println("Evento generado al tópico " + TOPICO);
    }
}
