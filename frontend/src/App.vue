<template>
  <div class="app-shell">
    <NavBar />
    <main class="page-shell">
      <RouterView />
    </main>
  </div>
</template>

<script setup>
import { RouterView } from 'vue-router'
import NavBar from './components/NavBar.vue'
</script>
