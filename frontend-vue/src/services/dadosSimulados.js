/**
 * Dados fictícios exibidos quando a API não responde.
 *
 * Na Sprint 2 o Front-end precisa ser navegável sozinho, só com `npm run dev`.
 * Dashboard e Relatórios tentam a API primeiro; se o servidor não estiver no ar,
 * usam estes valores e avisam na tela que são simulados. Os formatos seguem os
 * DTOs do Back-end (IndicadoresResponse, ClienteResponse, InsightResponse,
 * TelemetriaResponse), então a tela é a mesma nos dois casos.
 */

export const indicadoresSimulados = {
  totalClientes: 48,
  totalContratos: 112,
  faturamentoTotal: 86_400_000,
  faturamentoMedio: 1_800_000,
  clientesPorSegmento: [
    { rotulo: 'Indústria', valor: 15 },
    { rotulo: 'Comércio', valor: 11 },
    { rotulo: 'Serviços', valor: 9 },
    { rotulo: 'Saúde', valor: 6 },
    { rotulo: 'Educação', valor: 4 },
    { rotulo: 'Governo', valor: 3 },
  ],
  clientesPorNivel: [
    { rotulo: 'A', valor: 9 },
    { rotulo: 'B', valor: 21 },
    { rotulo: 'C', valor: 18 },
  ],
  clientesPorFaixaFaturamento: [
    { rotulo: 'Até R$ 360 mil', valor: 12 },
    { rotulo: 'R$ 360 mil a R$ 4,8 mi', valor: 24 },
    { rotulo: 'R$ 4,8 mi a R$ 30 mi', valor: 9 },
    { rotulo: 'Acima de R$ 30 mi', valor: 3 },
  ],
  contratosPorServico: [
    { rotulo: 'Link Dedicado', valor: 38 },
    { rotulo: 'Cloud', valor: 29 },
    { rotulo: 'Telefonia', valor: 25 },
    { rotulo: 'Segurança', valor: 20 },
  ],
  evolucaoContratacoes: [
    { rotulo: '2021', valor: 14 },
    { rotulo: '2022', valor: 19 },
    { rotulo: '2023', valor: 23 },
    { rotulo: '2024', valor: 27 },
    { rotulo: '2025', valor: 29 },
  ],
}

export const clientesSimulados = [
  { id: 1, codigoCti: 'CTI001', segmento: 'Indústria', nivel: 'A', faturamentoAnual: 12_500_000, faixaFaturamento: 'R$ 4,8 mi a R$ 30 mi', consultor: 'Ana Souza', contratosAtivos: 4 },
  { id: 2, codigoCti: 'CTI002', segmento: 'Comércio', nivel: 'B', faturamentoAnual: 1_250_000, faixaFaturamento: 'R$ 360 mil a R$ 4,8 mi', consultor: 'Bruno Lima', contratosAtivos: 2 },
  { id: 3, codigoCti: 'CTI003', segmento: 'Serviços', nivel: 'C', faturamentoAnual: 290_000, faixaFaturamento: 'Até R$ 360 mil', consultor: 'Carla Dias', contratosAtivos: 1 },
  { id: 4, codigoCti: 'CTI004', segmento: 'Saúde', nivel: 'B', faturamentoAnual: 3_100_000, faixaFaturamento: 'R$ 360 mil a R$ 4,8 mi', consultor: 'Ana Souza', contratosAtivos: 3 },
  { id: 5, codigoCti: 'CTI005', segmento: 'Indústria', nivel: 'A', faturamentoAnual: 34_000_000, faixaFaturamento: 'Acima de R$ 30 mi', consultor: 'Bruno Lima', contratosAtivos: 5 },
  { id: 6, codigoCti: 'CTI006', segmento: 'Educação', nivel: 'C', faturamentoAnual: 180_000, faixaFaturamento: 'Até R$ 360 mil', consultor: 'Carla Dias', contratosAtivos: 1 },
]

export const insightsSimulados = [
  { id: 1, tipo: 'Segmento', resumo: 'Indústria concentra 31% da carteira e a maior parte dos clientes nível A.', geradoEm: '2026-09-30T14:20:00' },
  { id: 2, tipo: 'Faturamento', resumo: 'Metade dos clientes está na faixa de R$ 360 mil a R$ 4,8 mi de faturamento anual.', geradoEm: '2026-09-30T14:20:00' },
  { id: 3, tipo: 'Serviço', resumo: 'Link Dedicado é o serviço mais contratado, presente em 38 contratos.', geradoEm: '2026-09-30T14:20:00' },
]

export const telemetriaSimulada = [
  { id: 2, evento: 'ANALISE_PYTHON', status: 'OK', detalhe: '3 insights gerados', duracaoMs: 4200, timestamp: '2026-09-30T14:20:00' },
  { id: 1, evento: 'UPLOAD', status: 'OK', detalhe: '52 linhas lidas', duracaoMs: 850, timestamp: '2026-09-30T14:19:55' },
]

/** Resumo da qualidade da última planilha: o que o tratamento encontrou. */
export const qualidadeSimulada = {
  registros: 52,
  camposVazios: 7,
  duplicidades: 4,
  registrosValidos: 48,
}
