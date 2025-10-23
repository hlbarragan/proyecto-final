package com.proyecto_kafka.productor.generador_eventos.controladores;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto_kafka.productor.generador_eventos.modelo.EventoWeb;
import com.proyecto_kafka.productor.generador_eventos.servicios.ProductorKafkaService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/v1/eventos")
public class EventosController {

    @Autowired
    private ProductorKafkaService productorKafkaService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/enviar")
    public ResponseEntity<String> publicarEvento(@RequestBody EventoWeb evento) {
        try {
            evento.setHora(Instant.now().getEpochSecond());
            String eventoCadena = this.objectMapper.writeValueAsString(evento);
            this.productorKafkaService.enviarEventoPagina(eventoCadena);

            return ResponseEntity.ok("Evento publicado con éxito");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error publicando el evento: " + e);
        }
    }
    
}
