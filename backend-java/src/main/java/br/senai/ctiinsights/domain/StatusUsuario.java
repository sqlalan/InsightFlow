package br.senai.ctiinsights.domain;

/**
 * Situacao da conta. Toda conta nova nasce PENDENTE e so entra no sistema
 * depois que o administrador muda para ATIVO. BLOQUEADO serve tanto para o
 * pedido recusado quanto para quem perdeu o acesso; o admin pode desbloquear.
 */
public enum StatusUsuario {
    PENDENTE, ATIVO, BLOQUEADO
}
