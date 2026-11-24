package br.com.cloudtech.models;

import jakarta.validation.constraints.NotBlank;

/** Dados recebidos no cadastro (POST) e na atualização (PUT) de uma máquina. */
public record CadastroMaquina(
        @NotBlank String nome,
        @NotBlank String tipo,
        @NotBlank String setor,
        @NotBlank String status) {}
