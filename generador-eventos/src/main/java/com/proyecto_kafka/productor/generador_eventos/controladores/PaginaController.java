package com.proyecto_kafka.productor.generador_eventos.controladores;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto_kafka.productor.generador_eventos.modelo.EventoWeb;
import com.proyecto_kafka.productor.generador_eventos.servicios.ProductorKafkaService;

@Controller
@RequestMapping("/web/home")
public class PaginaController {

    @Autowired
    private ProductorKafkaService productorKafkaService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/")
    public String pagina() {
        return "pagina";
    }

    @PostMapping("/enviar-evento")
    public String sendEvent(@RequestParam String idBoton, @RequestParam String event, Model model) {
        try {
            EventoWeb evento = new EventoWeb("hbarraganb@dian.gov.co", Instant.now().getEpochSecond(),
                    idBoton + "-" + event);
            String eventoCadena = this.objectMapper.writeValueAsString(evento);
            this.productorKafkaService.enviarEventoPagina(eventoCadena);

            model.addAttribute("msgMensaje", "Botón " + idBoton + " presionado!!");
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("msgMensaje", "Error en el procesamiento del evento");
        }

        return "pagina";
    }
}
