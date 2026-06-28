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
import { computed } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const showNav = computed(() => !route.meta.public)
const username = computed(() => {
  const raw = localStorage.getItem('user')
  if (!raw) {
    return 'User'
  }
  try {
    return JSON.parse(raw).username || 'User'
  } catch (e) {
    return 'User'
  }
})

function logout() {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  router.push('/login')
}
</script>
