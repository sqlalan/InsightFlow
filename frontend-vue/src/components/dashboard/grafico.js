import {
  ArcElement,
  BarElement,
  CategoryScale,
  Chart,
  Filler,
  Legend,
  LineElement,
  LinearScale,
  PointElement,
  Tooltip,
} from 'chart.js'

/**
 * Registro único dos módulos do Chart.js usados no projeto. Importar só o que é
 * desenhado mantém o bundle menor do que puxar `chart.js/auto`.
 */
Chart.register(
  ArcElement,
  BarElement,
  CategoryScale,
  Filler,
  Legend,
  LineElement,
  LinearScale,
  PointElement,
  Tooltip,
)

/** Mesmos tons do tema (`@theme` do Tailwind), para gráfico e interface combinarem. */
export const CORES = [
  '#38c5e8',
  '#4f8cff',
  '#34d399',
  '#f59e0b',
  '#a855f7',
  '#f472b6',
  '#94a3b8',
]

const TEXTO = '#8496b3'
const GRADE = 'rgba(255, 255, 255, 0.07)'

/** Opções comuns: fundo escuro, sem proporção fixa (o card controla a altura). */
export function opcoesBase(extras = {}) {
  return {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { display: false },
      tooltip: {
        backgroundColor: '#0d1526',
        borderColor: 'rgba(255,255,255,0.12)',
        borderWidth: 1,
        titleColor: '#f4f7fb',
        bodyColor: '#b3c1d6',
        padding: 10,
        displayColors: false,
      },
      ...extras.plugins,
    },
    scales: extras.scales,
    ...extras.raiz,
  }
}

/** Escalas cartesianas com o mesmo acabamento em todos os gráficos. */
export function escalasPadrao({ rotacao = 0 } = {}) {
  return {
    x: {
      ticks: { color: TEXTO, font: { size: 11 }, maxRotation: rotacao, minRotation: rotacao },
      grid: { display: false },
      border: { color: GRADE },
    },
    y: {
      beginAtZero: true,
      ticks: { color: TEXTO, font: { size: 11 }, precision: 0 },
      grid: { color: GRADE },
      border: { display: false },
    },
  }
}

/** Converte a lista `{ rotulo, valor }` da API no formato que o Chart.js espera. */
export function separar(contagens = []) {
  return {
    rotulos: contagens.map((item) => item.rotulo),
    valores: contagens.map((item) => item.valor),
  }
}
