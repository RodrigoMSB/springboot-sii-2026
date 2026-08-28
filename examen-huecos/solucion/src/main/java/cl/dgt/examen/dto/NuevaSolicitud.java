package cl.dgt.examen.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Viene resuelto: es el cuerpo del POST. */
public record NuevaSolicitud(
        @NotBlank String tipo,
        @NotBlank String estado,
        @NotNull LocalDate fecha,
        @NotNull @Positive BigDecimal monto,
        @NotBlank String oficinaCodigo) {
}
