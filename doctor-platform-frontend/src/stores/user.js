import { defineStore } from 'pinia'
import { ref } from 'vue'

const TOKEN_KEY = 'doctor_platform_token'
const PROFILE_KEY = 'doctor_platform_profile'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem(TOKEN_KEY) || '')
  const profile = ref(JSON.parse(localStorage.getItem(PROFILE_KEY) || 'null'))

  function setSession(t, p) {
    token.value = t
    profile.value = p
    localStorage.setItem(TOKEN_KEY, t)
    localStorage.setItem(PROFILE_KEY, JSON.stringify(p))
  }

  function clear() {
    token.value = ''
    profile.value = null
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(PROFILE_KEY)
  }

  return { token, profile, setSession, clear }
})
