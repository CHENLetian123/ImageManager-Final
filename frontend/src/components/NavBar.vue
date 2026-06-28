<template>
  <header v-if="showNav" class="navbar">
    <RouterLink class="brand" to="/dashboard">ImageManager</RouterLink>
    <nav class="nav-links">
      <RouterLink to="/dashboard">Dashboard</RouterLink>
      <RouterLink to="/upload">Upload</RouterLink>
      <RouterLink to="/batch-upload">Batch Upload</RouterLink>
      <RouterLink to="/folders">Folder Sync</RouterLink>
    </nav>
    <div class="nav-user">
      <span>{{ username }}</span>
      <button class="button ghost" @click="logout">Logout</button>
    </div>
  </header>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const showNav = computed(() => !route.meta.public)
const username = ref('User')

onMounted(() => {
  refreshUsername()
  window.addEventListener('auth-changed', refreshUsername)
  window.addEventListener('storage', refreshUsername)
})

onBeforeUnmount(() => {
  window.removeEventListener('auth-changed', refreshUsername)
  window.removeEventListener('storage', refreshUsername)
})

watch(
  () => route.fullPath,
  () => refreshUsername()
)

function refreshUsername() {
  const raw = localStorage.getItem('user')
  if (!raw) {
    username.value = 'User'
    return
  }
  try {
    username.value = JSON.parse(raw).username || 'User'
  } catch (e) {
    username.value = 'User'
  }
}

function logout() {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  localStorage.removeItem('username')
  sessionStorage.removeItem('token')
  sessionStorage.removeItem('user')
  sessionStorage.removeItem('username')
  window.dispatchEvent(new Event('auth-changed'))
  router.push('/login')
}
</script>
