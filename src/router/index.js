import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'

const routes = [
  {
    path: '/',
    name: 'home',
    component: HomeView,
    meta: { title: 'InsightFlow — Inteligência de dados a partir das suas planilhas' },
  },
  {
    path: '/login',
    name: 'login',
    // carregada sob demanda: mantém o bundle da landing enxuto
    component: () => import('../views/LoginView.vue'),
    meta: { title: 'Entrar — InsightFlow' },
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/',
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) return savedPosition
    if (to.hash) return { el: to.hash, behavior: 'smooth', top: 88 }
    return { top: 0 }
  },
})

router.afterEach((to) => {
  document.title = to.meta?.title ?? 'InsightFlow'
})

export default router
