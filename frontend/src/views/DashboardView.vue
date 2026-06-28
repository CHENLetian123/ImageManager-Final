<template>
  <section class="content-page" @click="closeMenu">
    <div class="page-header">
      <div>
        <h1>Image Manager</h1>
        <p class="muted">Search, filter, tag and manage uploaded images.</p>
      </div>
      <div class="header-actions">
        <RouterLink class="button" to="/upload">Upload Image</RouterLink>
        <RouterLink class="button" to="/folders">Folder Sync</RouterLink>
        <RouterLink class="button primary" to="/batch-upload">Batch Upload</RouterLink>
      </div>
    </div>

    <ImageToolbar
      :filters="filters"
      :sources="sources"
      :view-mode="viewMode"
      :group-by="groupBy"
      @update:filters="updateFilter"
      @update:view-mode="updateViewMode"
      @update:group-by="updateGroupBy"
      @apply="applyNow"
      @reset="resetFilters"
    />

    <p v-if="message" class="message success toast-message">{{ message }}</p>
    <p v-if="error" class="message error toast-message">{{ error }}</p>

    <div v-if="loading" class="empty-state">Loading images...</div>
    <div v-else-if="images.length === 0" class="empty-state">
      <h2>No images found</h2>
      <p>Upload images, sync a folder, or adjust filters.</p>
    </div>
    <div v-else-if="!groupBy" class="image-grid" :class="{ 'detail-list': viewMode === 'detail' }">
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
    <div v-else class="grouped-gallery">
      <section v-for="group in groupedImages" :key="group.name" class="image-group">
        <div class="group-header">
          <h2>{{ group.name }}</h2>
          <span class="muted">{{ group.items.length }} image(s)</span>
        </div>
        <div class="image-grid" :class="{ 'detail-list': viewMode === 'detail' }">
          <ImageCard
            v-for="image in group.items"
            :key="image.id"
            :image="image"
            :view-mode="viewMode"
            @copy="copy"
            @detail="openDetail"
            @delete="remove"
            @open-menu="openMenu"
          />
        </div>
      </section>
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
      @close="closeMenu"
    />
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import ImageToolbar from '../components/ImageToolbar.vue'
import ImageCard from '../components/ImageCard.vue'
import ContextMenu from '../components/ContextMenu.vue'
import { deleteImage, getSources, listImages, toggleTag } from '../api/imageApi'
import { copyImage } from '../utils/clipboard'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const images = ref([])
const sources = ref([])
const viewMode = ref('grid')
const groupBy = ref('')
const message = ref('')
const error = ref('')
let filterTimer = null

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

const groupedImages = computed(() => {
  const groups = new Map()
  images.value.forEach((image) => {
    groupNames(image).forEach((name) => {
      if (!groups.has(name)) {
        groups.set(name, [])
      }
      groups.get(name).push(image)
    })
  })
  return Array.from(groups.entries()).map(([name, items]) => ({ name, items }))
})

onMounted(async () => {
  applyQueryState()
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
  scheduleFilterApply(key === 'keyword' ? 300 : 0)
}

function updateViewMode(value) {
  viewMode.value = value
  updateQuery()
}

function updateGroupBy(value) {
  groupBy.value = value
  updateQuery()
}

function applyNow() {
  window.clearTimeout(filterTimer)
  updateQuery()
  loadImages()
}

function resetFilters() {
  filters.keyword = ''
  filters.source = ''
  filters.tag = ''
  filters.timeRange = ''
  filters.sort = 'time'
  groupBy.value = ''
  updateQuery()
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
  try {
    const { data } = await toggleTag(image.id, tag)
    images.value = images.value.map((item) => (item.id === data.id ? data : item))
    if (menu.image?.id === data.id) {
      menu.image = data
    }
    message.value = 'Tag updated.'
  } catch (err) {
    error.value = err.response?.data?.message || 'Tag update failed.'
  }
}

function openMenu(event, image) {
  event.stopPropagation()
  menu.visible = true
  menu.x = Math.min(event.clientX, window.innerWidth - 190)
  menu.y = Math.min(event.clientY, window.innerHeight - 360)
  menu.image = image
}

function closeMenu() {
  menu.visible = false
}

function scheduleFilterApply(delay) {
  window.clearTimeout(filterTimer)
  filterTimer = window.setTimeout(async () => {
    updateQuery()
    await loadImages()
    await loadSources()
  }, delay)
}

function updateQuery() {
  const query = {}
  Object.entries(filters).forEach(([key, value]) => {
    if (value) {
      query[key === 'timeRange' ? 'time' : key] = value
    }
  })
  if (viewMode.value !== 'grid') {
    query.view = viewMode.value
  }
  if (groupBy.value) {
    query.groupBy = groupBy.value
  }
  router.replace({ path: '/dashboard', query })
}

function applyQueryState() {
  filters.keyword = String(route.query.keyword || '')
  filters.source = String(route.query.source || '')
  filters.tag = String(route.query.tag || '')
  filters.timeRange = String(route.query.timeRange || route.query.time || '')
  filters.sort = String(route.query.sort || 'time')
  viewMode.value = String(route.query.view || 'grid')
  groupBy.value = String(route.query.groupBy || '')
}

function groupNames(image) {
  if (groupBy.value === 'source') {
    return [image.sourceName || 'Unknown Source']
  }
  if (groupBy.value === 'tag') {
    const tags = image.tags ? image.tags.split(',').map((item) => item.trim()).filter(Boolean) : []
    return tags.length > 0 ? tags : ['No Tags']
  }
  if (groupBy.value === 'time') {
    return [timeGroup(image.lastModified)]
  }
  return ['All Images']
}

function timeGroup(value) {
  if (!value) {
    return 'No File Time'
  }
  const date = new Date(Number(value))
  const now = new Date()
  const startThisWeek = startOfWeek(now)
  const startLastWeek = new Date(startThisWeek)
  startLastWeek.setDate(startThisWeek.getDate() - 7)
  if (date >= startThisWeek) {
    return 'This Week'
  }
  if (date >= startLastWeek) {
    return 'Last Week'
  }
  return 'Older'
}

function startOfWeek(date) {
  const result = new Date(date.getFullYear(), date.getMonth(), date.getDate())
  const day = result.getDay() || 7
  result.setDate(result.getDate() - day + 1)
  result.setHours(0, 0, 0, 0)
  return result
}
</script>
