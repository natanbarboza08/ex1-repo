package br.com.cloudtech.models;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalTime;

/** Dados recebidos ao registrar (POST) ou alterar (PUT) um registro de produção. */
public record DadosProducao(
        @NotNull @Positive Long maquinaId,
        @NotNull @DecimalMin("0") BigDecimal consumoKw,
        @NotNull LocalTime horarioAcionamento,
        @NotNull @PositiveOrZero Integer pecasProduzidas) {}
