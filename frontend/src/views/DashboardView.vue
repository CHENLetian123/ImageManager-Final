<template>
  <section class="content-page" @click="closeMenu">
    <div class="page-header">
      <div>
        <h1>Image Manager</h1>
        <p class="muted">Search, filter, tag and manage uploaded images.</p>
      </div>
      <div class="header-actions">
        <RouterLink class="button" to="/upload">Upload Image</RouterLink>
        <RouterLink class="button primary" to="/batch-upload">Batch Upload</RouterLink>
      </div>
    </div>

    <ImageToolbar
      :filters="filters"
      :sources="sources"
      :view-mode="viewMode"
      @update:filters="updateFilter"
      @update:view-mode="viewMode = $event"
      @apply="loadImages"
      @reset="resetFilters"
    />

    <p v-if="message" class="message success">{{ message }}</p>
    <p v-if="error" class="message error">{{ error }}</p>

    <div v-if="loading" class="empty-state">Loading images...</div>
    <div v-else-if="images.length === 0" class="empty-state">
      No images found. Upload images or adjust filters.
    </div>
    <div v-else class="image-grid" :class="{ 'detail-list': viewMode === 'detail' }">
      <ImageCard
        v-for="image in images"
        :key="image.id"
        :image="image"
        :view-mode="viewMode"
        @copy="copy"
        @detail="openDetail"
        @delete="remove"
        @open-menu="openMenu"
      />
    </div>

    <ContextMenu
      :visible="menu.visible"
      :x="menu.x"
      :y="menu.y"
      :image="menu.image"
      @copy="copy"
      @detail="openDetail"
      @toggle-tag="toggle"
      @delete="remove"
    />
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import ImageToolbar from '../components/ImageToolbar.vue'
import ImageCard from '../components/ImageCard.vue'
import ContextMenu from '../components/ContextMenu.vue'
import { deleteImage, getSources, listImages, toggleTag } from '../api/imageApi'
import { copyImage } from '../utils/clipboard'

const router = useRouter()
const loading = ref(false)
const images = ref([])
const sources = ref([])
const viewMode = ref('grid')
const message = ref('')
const error = ref('')

const filters = reactive({
  keyword: '',
  source: '',
  tag: '',
  timeRange: '',
  sort: 'time'
})

const menu = reactive({
  visible: false,
  x: 0,
  y: 0,
  image: null
})

onMounted(async () => {
  await Promise.all([loadImages(), loadSources()])
})

async function loadImages() {
  loading.value = true
  error.value = ''
  try {
    const { data } = await listImages({ ...filters })
    images.value = data
  } catch (err) {
    error.value = err.response?.data?.message || 'Failed to load images.'
  } finally {
    loading.value = false
  }
}

async function loadSources() {
  try {
    const { data } = await getSources()
    sources.value = data
  } catch (err) {
    sources.value = []
  }
}

function updateFilter({ key, value }) {
  filters[key] = value
}

function resetFilters() {
  filters.keyword = ''
  filters.source = ''
  filters.tag = ''
  filters.timeRange = ''
  filters.sort = 'time'
  loadImages()
}

async function copy(image) {
  closeMenu()
  message.value = await copyImage(image)
}

function openDetail(image) {
  closeMenu()
  router.push(`/images/${image.id}`)
}

async function remove(image) {
  closeMenu()
  if (!window.confirm('Are you sure you want to delete this image?')) {
    return
  }
  try {
    await deleteImage(image.id)
    message.value = 'Image deleted.'
    await Promise.all([loadImages(), loadSources()])
  } catch (err) {
    error.value = err.response?.data?.message || 'Delete failed.'
  }
}

async function toggle(image, tag) {
  closeMenu()
  try {
    const { data } = await toggleTag(image.id, tag)
    images.value = images.value.map((item) => (item.id === data.id ? data : item))
    message.value = 'Tag updated.'
  } catch (err) {
    error.value = err.response?.data?.message || 'Tag update failed.'
  }
}

function openMenu(event, image) {
  event.stopPropagation()
  menu.visible = true
  menu.x = Math.min(event.clientX, window.innerWidth - 180)
  menu.y = Math.min(event.clientY, window.innerHeight - 320)
  menu.image = image
}

function closeMenu() {
  menu.visible = false
}
</script>
