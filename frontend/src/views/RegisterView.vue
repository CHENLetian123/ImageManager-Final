<template>
  <section class="auth-page">
    <div class="auth-panel">
      <h1>Create Account</h1>
      <p class="muted">Register a user for your cloud image manager.</p>

      <form @submit.prevent="submit">
        <label>
          Username
          <input v-model.trim="form.username" class="input" autocomplete="username" required />
        </label>
        <label>
          Password
          <input v-model="form.password" class="input" type="password" autocomplete="new-password" required />
        </label>
        <label>
          Confirm Password
          <input v-model="form.confirmPassword" class="input" type="password" autocomplete="new-password" required />
        </label>
        <p v-if="message" class="message" :class="{ error: hasError, success: !hasError }">{{ message }}</p>
        <button class="button primary wide" type="submit" :disabled="loading">
          {{ loading ? 'Creating...' : 'Register' }}
        </button>
      </form>

      <p class="switch-link">
        Already have an account?
        <RouterLink to="/login">Back to Login</RouterLink>
      </p>
    </div>
  </section>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { register } from '../api/authApi'

const router = useRouter()
const loading = ref(false)
const message = ref('')
const hasError = ref(false)
const form = reactive({
  username: '',
  password: '',
  confirmPassword: ''
})

async function submit() {
  loading.value = true
  message.value = ''
  hasError.value = false
  try {
    await register(form)
    message.value = 'Register success. Redirecting to login...'
    setTimeout(() => router.push('/login'), 700)
  } catch (error) {
    hasError.value = true
    message.value = error.response?.data?.message || 'Register failed.'
  } finally {
    loading.value = false
  }
}
</script>
