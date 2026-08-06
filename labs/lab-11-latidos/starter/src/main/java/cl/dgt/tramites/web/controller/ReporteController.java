package cl.dgt.tramites.web.controller;

import cl.dgt.tramites.application.ReporteService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * El tablero de Carolina: totales declarados por período.
 *
 * <p>Es la consulta cara del lab —{@code JOIN} + {@code GROUP BY} sobre todo el histórico— y la
 * que se cachea. El controlador no sabe nada de eso, y así debe ser: el caché es una decisión de la
 * capa de aplicación ({@code @Cacheable} en {@link ReporteService}), no del transporte. Si mañana
 * se decide quitarlo, este archivo no se toca.
 */
@RestController
@RequestMapping("/api/v1/reportes")
public class ReporteController {

    private final ReporteService reportes;

    public ReporteController(ReporteService reportes) {
        this.reportes = reportes;
    }

    /** Totales por período. Servido de caché mientras nadie declare una línea nueva. */
    @GetMapping("/totales-por-periodo")
    public List<ReporteService.TotalPorPeriodo> totalesPorPeriodo() {
        return reportes.totalDeclaradoPorPeriodo();
    }
}
