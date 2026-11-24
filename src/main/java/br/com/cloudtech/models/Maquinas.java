package br.com.cloudtech.models;

/** Máquina industrial cadastrada no sistema de monitoramento. */
public record Maquinas(long id, String nome, String tipo, String setor, String status) {}
