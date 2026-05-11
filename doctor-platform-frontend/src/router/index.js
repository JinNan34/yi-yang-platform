import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

const routes = [
  { path: '/login', name: 'login', component: () => import('../views/Login.vue'), meta: { public: true, title: '登录' } },
  {
    path: '/',
    component: () => import('../views/MainLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      { path: '', name: 'home', component: () => import('../views/Home.vue'), meta: { title: '首页' } },
      { path: 'elders', name: 'elders', component: () => import('../views/Elders.vue'), meta: { title: '老人健康档案' } },
      {
        path: 'health-records',
        name: 'healthRecords',
        component: () => import('../views/HealthRecords.vue'),
        meta: { title: '健康档案记录' }
      },
      { path: 'health-alerts', name: 'healthAlerts', component: () => import('../views/HealthAlerts.vue'), meta: { title: '健康预警' } },
      { path: 'key-followups', name: 'keyFollowups', component: () => import('../views/KeyFollowups.vue'), meta: { title: '重点人群随访' } },
      {
        path: 'followup-interventions',
        name: 'followupInterventions',
        component: () => import('../views/FollowupInterventions.vue'),
        meta: { title: '随访干预记录' }
      },
      {
        path: 'health-assessments',
        name: 'healthAssessments',
        component: () => import('../views/HealthAssessments.vue'),
        meta: { title: '健康评估' }
      },
      { path: 'elder-accounts', name: 'elderAccounts', component: () => import('../views/ElderAccounts.vue'), meta: { title: '老人账户管理' } },
      { path: 'profile', name: 'profile', component: () => import('../views/Profile.vue'), meta: { title: '个人账户' } }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  const user = useUserStore()
  if (to.meta.requiresAuth && !user.token) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
  if (to.name === 'login' && user.token) {
    return { name: 'home' }
  }
})

export default router
