<template>
  <section class="content-page">
    <div class="page-header">
      <div>
        <h1>Batch Upload</h1>
        <p class="muted">
          Folder import uses browser-selected files or folders. Automatic desktop directory sync can be added in a future local sync client.
        </p>
      </div>
      <RouterLink class="button" to="/dashboard">Back</RouterLink>
    </div>

    <section class="form-card">
      <div class="batch-controls">
        <label>
          Select Images
          <input class="input" type="file" accept="image/*" multiple @change="pickFiles" />
        </label>
        <label>
          Select Folder
          <input class="input" type="file" accept="image/*" webkitdirectory directory multiple @change="pickFiles" />
        </label>
      </div>

      <div class="batch-controls">
        <label>
          Source Path / Source Label
          <input v-model.trim="sourceRoot" class="input" placeholder="D:/Screenshots or Project Notes" />
        </label>
        <label>
          Category
          <input v-model.trim="category" class="input" placeholder="Uncategorized" />
        </label>
        <label>
          Tags
          <input v-model.trim="tags" class="input" placeholder="Common,Notes,Favorite" />
        </label>
      </div>

      <div class="selection-row">
        <button class="button" type="button" @click="selectAll(true)">Select All</button>
        <button class="button ghost" type="button" @click="selectAll(false)">Clear Selection</button>
        <button class="button ghost" type="button" @click="clearItems">Remove All</button>
        <button class="button primary" type="button" :disabled="uploading || selectedCount === 0" @click="uploadSelected">
          {{ uploading ? 'Uploading...' : `Upload Selected (${selectedCount})` }}
        </button>
      </div>

      <p v-if="message" class="message success">{{ message }}</p>
      <p v-if="error" class="message error">{{ error }}</p>
      <p class="muted">Progress: {{ uploadedCount }} / {{ selectedTotal }}</p>
    </section>

    <div v-if="items.length === 0" class="empty-state">No images selected.</div>
    <div v-else class="batch-list">
      <article v-for="item in items" :key="item.id" class="batch-item">
        <input type="checkbox" v-model="item.selected" />
        <img :src="item.previewUrl" :alt="item.file.name" />
        <div>
          <h3>{{ item.file.name }}</h3>
          <p class="muted">{{ item.relativePath }}</p>
          <p class="meta-line">{{ formatSize(item.file.size) }} · {{ statusLabel(item) }}</p>
        </div>
        <button class="button small danger" type="button" @click="removeItem(item.id)">Remove</button>
      </article>
    </div>
  </section>
</template>

<script setup>
import { computed, ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { uploadImage } from '../api/imageApi'

const router = useRouter()
const items = ref([])
const sourceRoot = ref('')
const category = ref('')
const tags = ref('')
const uploading = ref(false)
const uploadedCount = ref(0)
const selectedTotal = ref(0)
const message = ref('')
const error = ref('')

const selectedCount = computed(() => items.value.filter((item) => item.selected).length)

function pickFiles(event) {
  const files = Array.from(event.target.files || []).filter((file) => file.type.startsWith('image/'))
  const mapped = files.map((file) => ({
    id: crypto.randomUUID(),
    file,
    selected: true,
    previewUrl: URL.createObjectURL(file),
    relativePath: normalizePath(file.webkitRelativePath || file.name),
    status: 'pending'
  }))
  items.value = [...items.value, ...mapped]
  event.target.value = ''
}

function selectAll(value) {
  items.value.forEach((item) => {
    item.selected = value
  })
}

function clearItems() {
  items.value.forEach((item) => URL.revokeObjectURL(item.previewUrl))
  items.value = []
}

function removeItem(id) {
  const item = items.value.find((entry) => entry.id === id)
  if (item) {
    URL.revokeObjectURL(item.previewUrl)
  }
  items.value = items.value.filter((entry) => entry.id !== id)
}

async function uploadSelected() {
  const selected = items.value.filter((item) => item.selected)
  if (selected.length === 0) {
    return
  }

  uploading.value = true
  uploadedCount.value = 0
  selectedTotal.value = selected.length
  message.value = ''
  error.value = ''

  for (let index = 0; index < selected.length; index += 5) {
    const batch = selected.slice(index, index + 5)
    await Promise.all(batch.map(uploadOne))
  }

  uploading.value = false
  const failed = selected.filter((item) => item.status === 'failed').length
  if (failed > 0) {
    error.value = `${failed} image(s) failed to upload.`
  } else {
    message.value = 'Batch upload success.'
    setTimeout(() => router.push('/dashboard'), 700)
  }
}

async function uploadOne(item) {
  item.status = 'uploading'
  const data = new FormData()
  data.append('file', item.file)
  data.append('title', item.file.name)
  data.append('description', '')
  data.append('category', category.value || 'Uncategorized')
  data.append('tags', tags.value)
  data.append('sourceName', sourceRoot.value || 'Batch Upload')
  data.append('sourceType', 'BATCH_UPLOAD')
  data.append('relativePath', item.relativePath)
  data.append('originalPath', buildOriginalPath(item.relativePath))
  data.append('lastModified', String(item.file.lastModified || Date.now()))

  try {
    await uploadImage(data)
    item.status = 'uploaded'
    uploadedCount.value += 1
  } catch (err) {
    item.status = 'failed'
  }
}

function buildOriginalPath(relativePath) {
  if (!sourceRoot.value) {
    return relativePath || ''
  }
  return `${normalizePath(sourceRoot.value).replace(/\/$/, '')}/${relativePath || ''}`
}

function normalizePath(value) {
  return (value || '').replace(/\\/g, '/')
}

function statusLabel(item) {
  if (item.status === 'uploading') {
    return 'Uploading'
  }
  if (item.status === 'uploaded') {
    return 'Uploaded'
  }
  if (item.status === 'failed') {
    return 'Failed'
  }
  return 'Pending'
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
