package br.com.cloudtech.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

/** Registro de produção e consumo de uma máquina (histórico). */
public record Producao(
        long id,
        long maquinaId,
        BigDecimal consumoKw,
        LocalTime horarioAcionamento,
        int pecasProduzidas,
        LocalDateTime registradoEm) {}
