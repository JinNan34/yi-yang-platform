import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

const routes = [
  { path: '/login', name: 'login', component: () => import('../views/Login.vue'), meta: { public: true } },
  {
    path: '/',
    component: () => import('../views/MainLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      { path: '', name: 'home', component: () => import('../views/Home.vue') },
      { path: 'elders', name: 'elders', component: () => import('../views/Elders.vue') },
      { path: 'health-records', name: 'healthRecords', component: () => import('../views/HealthRecords.vue') },
      { path: 'health-alerts', name: 'healthAlerts', component: () => import('../views/HealthAlerts.vue') },
      { path: 'key-followups', name: 'keyFollowups', component: () => import('../views/KeyFollowups.vue') },
      { path: 'followup-interventions', name: 'followupInterventions', component: () => import('../views/FollowupInterventions.vue') },
      { path: 'health-assessments', name: 'healthAssessments', component: () => import('../views/HealthAssessments.vue') },
      { path: 'elder-accounts', name: 'elderAccounts', component: () => import('../views/ElderAccounts.vue') },
      { path: 'profile', name: 'profile', component: () => import('../views/Profile.vue') }
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
