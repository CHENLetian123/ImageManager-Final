<template>
  <section class="content-page">
    <div class="page-header">
      <div>
        <h1>{{ image?.title || image?.originalFileName || 'Image Detail' }}</h1>
        <p class="muted">Image metadata and actions.</p>
      </div>
      <RouterLink class="button" to="/dashboard">Back</RouterLink>
    </div>

    <p v-if="message" class="message success">{{ message }}</p>
    <p v-if="error" class="message error">{{ error }}</p>
    <div v-if="loading" class="empty-state">Loading image...</div>

    <section v-else-if="image" class="detail-layout">
      <div class="detail-image-wrap">
        <img :src="image.publicUrl" :alt="image.title || image.originalFileName" />
      </div>

      <div class="detail-panel">
        <h2>Metadata</h2>
        <dl>
          <dt>Title</dt>
          <dd>{{ image.title || '-' }}</dd>
          <dt>Description</dt>
          <dd>{{ image.description || '-' }}</dd>
          <dt>Category</dt>
          <dd>{{ image.category || 'Uncategorized' }}</dd>
          <dt>Tags</dt>
          <dd>{{ image.tags || '-' }}</dd>
          <dt>Source</dt>
          <dd>{{ image.sourceName || '-' }}</dd>
          <dt>Type</dt>
          <dd>{{ image.sourceType || '-' }}</dd>
          <dt>Original Path</dt>
          <dd>{{ image.originalPath || '-' }}</dd>
          <dt>Relative Path</dt>
          <dd>{{ image.relativePath || '-' }}</dd>
          <dt>Original File Name</dt>
          <dd>{{ image.originalFileName || '-' }}</dd>
          <dt>File Size</dt>
          <dd>{{ formatSize(image.fileSize) }}</dd>
          <dt>File Time</dt>
          <dd>{{ formatDate(image.lastModified) }}</dd>
          <dt>Created At</dt>
          <dd>{{ formatDateTime(image.createdAt) }}</dd>
        </dl>

        <div class="detail-actions">
          <button class="button" @click="copy">Copy</button>
          <a class="button" :href="image.publicUrl" target="_blank" rel="noreferrer">Download</a>
          <button class="button danger" @click="remove">Delete</button>
        </div>
      </div>
    </section>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { deleteImage, getImage } from '../api/imageApi'
import { copyImage } from '../utils/clipboard'

const route = useRoute()
const router = useRouter()
const image = ref(null)
const loading = ref(false)
const message = ref('')
const error = ref('')

onMounted(load)

async function load() {
  loading.value = true
  try {
    const { data } = await getImage(route.params.id)
    image.value = data
  } catch (err) {
    error.value = err.response?.data?.message || 'Failed to load image.'
  } finally {
    loading.value = false
  }
}

async function copy() {
  message.value = await copyImage(image.value)
}

async function remove() {
  if (!window.confirm('Are you sure you want to delete this image?')) {
    return
  }
  try {
    await deleteImage(image.value.id)
    router.push('/dashboard')
  } catch (err) {
    error.value = err.response?.data?.message || 'Delete failed.'
  }
}

function formatDate(value) {
  if (!value) {
    return '-'
  }
  return new Date(Number(value)).toLocaleString()
}

function formatDateTime(value) {
  if (!value) {
    return '-'
  }
  return new Date(value).toLocaleString()
}

function formatSize(size) {
  if (!size) {
    return '0 B'
  }
  if (size < 1024) {
    return `${size} B`
  }
  if (size < 1024 * 1024) {
    return `${(size / 1024).toFixed(1)} KB`
  }
  return `${(size / 1024 / 1024).toFixed(1)} MB`
}
</script>
