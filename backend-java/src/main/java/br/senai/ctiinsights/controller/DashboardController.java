package br.senai.ctiinsights.controller;

import br.senai.ctiinsights.dto.IndicadoresResponse;
import br.senai.ctiinsights.dto.InsightResponse;
import br.senai.ctiinsights.dto.TelemetriaResponse;
import br.senai.ctiinsights.service.DashboardService;
import br.senai.ctiinsights.service.TelemetriaService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Alimenta os cartoes, graficos e insights da tela de dashboard. */
@RestController
@RequestMapping("/api")
public class DashboardController {

    private final DashboardService dashboard;
    private final TelemetriaService telemetria;

    public DashboardController(DashboardService dashboard, TelemetriaService telemetria) {
        this.dashboard = dashboard;
        this.telemetria = telemetria;
    }

    @GetMapping("/indicadores")
    public IndicadoresResponse indicadores() {
        return dashboard.indicadores();
    }

    @GetMapping("/insights")
    public List<InsightResponse> insights() {
        return dashboard.insights();
    }

    @GetMapping("/telemetria")
    public List<TelemetriaResponse> telemetria() {
        return telemetria.ultimosEventos().stream().map(TelemetriaResponse::de).toList();
    }
}
