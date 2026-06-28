<template>
  <section class="auth-page">
    <div class="auth-panel">
      <h1>ImageManager</h1>
      <p class="muted">Sign in to manage your image library.</p>

      <form @submit.prevent="submit">
        <label>
          Username
          <input v-model.trim="form.username" class="input" autocomplete="username" required />
        </label>
        <label>
          Password
          <input v-model="form.password" class="input" type="password" autocomplete="current-password" required />
        </label>
        <p v-if="message" class="message error">{{ message }}</p>
        <button class="button primary wide" type="submit" :disabled="loading">
          {{ loading ? 'Signing in...' : 'Login' }}
        </button>
      </form>

      <p class="switch-link">
        No account?
        <RouterLink to="/register">Create one</RouterLink>
      </p>
    </div>
  </section>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { login } from '../api/authApi'

const router = useRouter()
const loading = ref(false)
const message = ref('')
const form = reactive({
  username: '',
  password: ''
})

async function submit() {
  loading.value = true
  message.value = ''
  try {
    const { data } = await login(form)
    if (!data.token || !data.user?.username) {
      throw new Error('Invalid login response.')
    }
    localStorage.setItem('token', data.token)
    localStorage.setItem('user', JSON.stringify(data.user))
    localStorage.removeItem('username')
    sessionStorage.removeItem('token')
    sessionStorage.removeItem('user')
    sessionStorage.removeItem('username')
    window.dispatchEvent(new Event('auth-changed'))
    router.push('/dashboard')
  } catch (error) {
    message.value = error.response?.data?.message || error.message || 'Login failed.'
  } finally {
    loading.value = false
  }
}
</script>
