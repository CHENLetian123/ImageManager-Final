<template>
  <section class="content-page">
    <div class="page-header">
      <div>
        <h1>Folder Sync</h1>
        <p class="muted">
          Authorize a browser-selected folder, review changed images, and upload selected files to cloud storage.
        </p>
      </div>
      <RouterLink class="button" to="/dashboard">Back to Dashboard</RouterLink>
    </div>

    <p class="message info">
      Folder sync is available on desktop browsers. You can still upload images manually on mobile.
    </p>
    <p v-if="message" class="message success toast-message">{{ message }}</p>
    <p v-if="error" class="message error toast-message">{{ error }}</p>

    <section class="folder-layout">
      <aside class="form-card folder-panel">
        <h2>Folder Presets</h2>
        <form class="preset-form" @submit.prevent="savePreset">
          <label>
            Name
            <input v-model.trim="presetForm.name" class="input" placeholder="Screenshots" required />
          </label>
          <label>
            Source Path
            <input v-model.trim="presetForm.sourcePath" class="input" placeholder="D:/Screenshots" />
          </label>
          <div class="selection-row">
            <button class="button primary" type="submit">{{ editingId ? 'Save Preset' : 'Add Preset' }}</button>
            <button v-if="editingId" class="button ghost" type="button" @click="cancelEdit">Cancel</button>
          </div>
        </form>

        <div v-if="loadingPresets" class="empty-state compact">Loading presets...</div>
        <div v-else-if="presets.length === 0" class="empty-state compact">No folder presets yet.</div>
        <div v-else class="preset-list">
          <article
            v-for="preset in presets"
            :key="preset.id"
            class="preset-card"
            :class="{ active: activePreset?.id === preset.id }"
          >
            <div>
              <h3>{{ preset.name }}</h3>
              <p class="meta-line">{{ preset.sourcePath || 'No source path' }}</p>
              <p class="meta-line">Last sync: {{ formatDateTime(preset.lastSyncAt) }}</p>
            </div>
            <div class="preset-actions">
              <button class="button small" type="button" @click="editPreset(preset)">Edit</button>
              <button class="button small" type="button" @click="authorizePreset(preset)">Authorize</button>
              <button class="button small primary" type="button" @click="syncPreset(preset)">Sync</button>
              <button class="button small danger" type="button" @click="removePreset(preset)">Delete</button>
            </div>
          </article>
        </div>
      </aside>

      <section class="form-card sync-panel">
        <div class="sync-header">
          <div>
            <h2>{{ activePreset ? activePreset.name : 'Select a preset' }}</h2>
            <p class="muted">
              {{ activePreset ? activePreset.sourcePath || activePreset.name : 'Choose Authorize or Sync from a preset.' }}
            </p>
          </div>
          <div class="selection-row">
            <button class="button" type="button" :disabled="items.length === 0" @click="selectAll(true)">Select All</button>
            <button class="button ghost" type="button" :disabled="items.length === 0" @click="selectAll(false)">Unselect All</button>
            <button class="button ghost" type="button" :disabled="items.length === 0" @click="clearScan">Clear</button>
          </div>
        </div>

        <div class="sync-options">
          <label>
            Category
            <input v-model.trim="category" class="input" placeholder="Uncategorized" />
          </label>
          <label>
            Tags
            <input v-model.trim="tags" class="input" placeholder="Common,Notes,Favorite" />
          </label>
          <label class="checkbox-line">
            <input v-model="deleteMissing" type="checkbox" />
            Delete cloud images missing from this folder
          </label>
        </div>

        <div class="progress-wrap">
          <div class="progress-bar">
            <span :style="{ width: `${progressPercent}%` }"></span>
          </div>
          <p class="muted">
            {{ uploadedCount }} / {{ selectedTotal }} processed
            <span v-if="items.length">| {{ summaryText }}</span>
          </p>
        </div>

        <button
          class="button primary wide"
          type="button"
          :disabled="!canApplySync"
          @click="applySync"
        >
          {{ syncButtonText }}
        </button>

        <input
          ref="fallbackInput"
          class="hidden-input"
          type="file"
          accept="image/*"
          webkitdirectory
          directory
          multiple
          @change="handleFallbackFiles"
        />

        <div v-if="scanning" class="empty-state compact">Scanning folder...</div>
        <div v-else-if="hasScanned && items.length === 0" class="empty-state compact">
          Scan found no images. You can still apply cleanup when Delete Missing is selected.
        </div>
        <div v-else-if="items.length === 0" class="empty-state compact">
          No scan result yet. Authorize a preset and click Sync.
        </div>
        <div v-else class="folder-sync-list">
          <article v-for="item in items" :key="item.id" class="folder-sync-item">
            <input type="checkbox" v-model="item.selected" />
            <img :src="item.previewUrl" :alt="item.file.name" />
            <div>
              <h3>{{ item.file.name }}</h3>
              <p class="muted">{{ item.relativePath }}</p>
              <p class="meta-line">{{ formatSize(item.file.size) }} | {{ formatDate(item.file.lastModified) }}</p>
            </div>
            <span class="status-pill" :class="item.status.toLowerCase()">{{ item.status }}</span>
          </article>
        </div>
      </section>
    </section>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { RouterLink } from 'vue-router'
import {
  cleanupMissingImages,
  createFolderPreset,
  deleteFolderPreset,
  getFolderImageIndex,
  listFolderPresets,
  updateFolderPreset,
  uploadFolderImage
} from '../api/folderPresetApi'
import { getDirectoryHandle, removeDirectoryHandle, saveDirectoryHandle } from '../utils/directoryStore'

const IMAGE_EXTENSIONS = ['.jpg', '.jpeg', '.png', '.gif', '.webp', '.bmp']

const presets = ref([])
const activePreset = ref(null)
const items = ref([])
const fallbackInput = ref(null)
const fallbackPreset = ref(null)
const loadingPresets = ref(false)
const scanning = ref(false)
const uploading = ref(false)
const hasScanned = ref(false)
const uploadedCount = ref(0)
const selectedTotal = ref(0)
const message = ref('')
const error = ref('')
const category = ref('')
const tags = ref('')
const deleteMissing = ref(false)
const editingId = ref(null)
const results = reactive({
  uploaded: 0,
  updated: 0,
  skipped: 0,
  deleted: 0,
  failed: 0
})

const presetForm = reactive({
  name: '',
  sourcePath: ''
})

const selectedCount = computed(() => items.value.filter((item) => item.selected).length)
const canApplySync = computed(() => {
  return !uploading.value && hasScanned.value && Boolean(activePreset.value) && (selectedCount.value > 0 || deleteMissing.value)
})
const progressPercent = computed(() => {
  if (!selectedTotal.value) {
    return 0
  }
  return Math.round((uploadedCount.value / selectedTotal.value) * 100)
})
const syncButtonText = computed(() => {
  if (uploading.value) {
    return 'Applying Sync...'
  }
  if (deleteMissing.value && selectedCount.value === 0) {
    return 'Apply Sync (cleanup only)'
  }
  return `Apply Sync (${selectedCount.value} selected)`
})
const summaryText = computed(() => {
  const counts = items.value.reduce((acc, item) => {
    acc[item.status] = (acc[item.status] || 0) + 1
    return acc
  }, {})
  return `New ${counts.New || 0}, Changed ${counts.Changed || 0}, Unchanged ${counts.Unchanged || 0}`
})

onMounted(loadPresets)

async function loadPresets() {
  loadingPresets.value = true
  try {
    const { data } = await listFolderPresets()
    presets.value = data
  } catch (err) {
    error.value = err.response?.data?.message || 'Failed to load folder presets.'
  } finally {
    loadingPresets.value = false
  }
}

async function savePreset() {
  error.value = ''
  try {
    if (editingId.value) {
      await updateFolderPreset(editingId.value, { ...presetForm })
      message.value = 'Folder preset updated.'
    } else {
      await createFolderPreset({ ...presetForm })
      message.value = 'Folder preset added.'
    }
    cancelEdit()
    await loadPresets()
  } catch (err) {
    error.value = err.response?.data?.message || 'Save preset failed.'
  }
}

function editPreset(preset) {
  editingId.value = preset.id
  presetForm.name = preset.name
  presetForm.sourcePath = preset.sourcePath || ''
}

function cancelEdit() {
  editingId.value = null
  presetForm.name = ''
  presetForm.sourcePath = ''
}

async function removePreset(preset) {
  if (!window.confirm('Delete this folder preset? Images already uploaded will stay in your gallery.')) {
    return
  }
  try {
    await deleteFolderPreset(preset.id)
    await removeDirectoryHandle(preset.id)
    if (activePreset.value?.id === preset.id) {
      clearScan()
      activePreset.value = null
    }
    message.value = 'Folder preset deleted.'
    await loadPresets()
  } catch (err) {
    error.value = err.response?.data?.message || 'Delete preset failed.'
  }
}

async function authorizePreset(preset) {
  activePreset.value = preset
  fallbackPreset.value = preset
  error.value = ''
  if (!supportsDirectoryPicker()) {
    message.value = 'Directory authorization is not supported here. Please choose a folder with the file picker.'
    fallbackInput.value?.click()
    return
  }

  try {
    const handle = await window.showDirectoryPicker()
    await saveDirectoryHandle(preset.id, handle)
    message.value = 'Folder authorized. Click Sync to scan it.'
  } catch (err) {
    error.value = 'Folder authorization was cancelled or blocked.'
  }
}

async function syncPreset(preset) {
  activePreset.value = preset
  fallbackPreset.value = preset
  error.value = ''
  if (!supportsDirectoryPicker()) {
    message.value = 'Please choose the folder again with the file picker.'
    fallbackInput.value?.click()
    return
  }

  try {
    let handle = await getDirectoryHandle(preset.id)
    if (!handle) {
      handle = await window.showDirectoryPicker()
      await saveDirectoryHandle(preset.id, handle)
    }
    const permission = await verifyPermission(handle)
    if (!permission) {
      error.value = 'Folder permission was not granted.'
      return
    }
    await scanHandle(preset, handle)
  } catch (err) {
    error.value = 'Folder scan failed or was cancelled.'
  }
}

async function scanHandle(preset, handle) {
  scanning.value = true
  clearScan()
  try {
    const files = await readDirectoryFiles(handle)
    await buildScanItems(preset, files)
    hasScanned.value = true
    message.value = `Scan finished. ${items.value.length} image(s) found.`
  } finally {
    scanning.value = false
  }
}

async function handleFallbackFiles(event) {
  const preset = fallbackPreset.value
  const files = Array.from(event.target.files || [])
  event.target.value = ''
  if (!preset) {
    return
  }
  scanning.value = true
  try {
    await buildScanItems(preset, files.map((file) => ({
      file,
      relativePath: normalizePath(file.webkitRelativePath || file.name)
    })))
    hasScanned.value = true
    message.value = `Scan finished. ${items.value.length} image(s) found.`
  } catch (err) {
    error.value = 'Folder scan failed.'
  } finally {
    scanning.value = false
  }
}

async function buildScanItems(preset, entries) {
  revokePreviews()
  const { data: cloudImages } = await getFolderImageIndex(preset.id)
  const cloudIndex = new Map(cloudImages.map((image) => [normalizePath(image.relativePath), image]))
  items.value = entries
    .filter((entry) => isImageFile(entry.file))
    .map((entry) => {
      const relativePath = normalizePath(entry.relativePath || entry.file.name)
      const remote = cloudIndex.get(relativePath)
      const status = resolveStatus(entry.file, remote)
      return {
        id: crypto.randomUUID(),
        file: entry.file,
        relativePath,
        previewUrl: URL.createObjectURL(entry.file),
        status,
        selected: status === 'New' || status === 'Changed'
      }
    })
}

async function applySync() {
  const selected = items.value.filter((item) => item.selected)
  if (!activePreset.value || !hasScanned.value || (selected.length === 0 && !deleteMissing.value)) {
    return
  }
  if (deleteMissing.value && !window.confirm('This will delete cloud images that are missing from this local folder. Continue?')) {
    return
  }

  resetResults()
  uploading.value = true
  uploadedCount.value = 0
  selectedTotal.value = selected.length
  error.value = ''
  message.value = ''

  try {
    for (let index = 0; index < selected.length; index += 5) {
      const batch = selected.slice(index, index + 5)
      await Promise.all(batch.map((item) => uploadOne(item)))
    }
  } catch (err) {
    error.value = 'Upload failed.'
  }

  try {
    const cleanup = await cleanupMissingImages(activePreset.value.id, {
      deleteMissing: deleteMissing.value,
      currentRelativePaths: items.value.map((item) => item.relativePath)
    })
    results.deleted = cleanup.data.deleted || 0
    const failedText = results.failed > 0 ? ` ${results.failed} failed.` : ''
    message.value = `${results.uploaded} uploaded, ${results.updated} updated, ${results.skipped} skipped, ${results.deleted} deleted.${failedText} Dashboard data has been updated.`
  } catch (err) {
    error.value = err.response?.data?.message || 'Cleanup failed.'
    uploading.value = false
    return
  }

  try {
    await refreshScanStatus()
    await loadPresets()
    refreshActivePreset()
  } catch (err) {
    error.value = 'Sync finished, but refresh failed. Please scan again.'
  } finally {
    uploading.value = false
  }
}

async function refreshScanStatus() {
  if (!activePreset.value || items.value.length === 0) {
    return
  }
  const { data: cloudImages } = await getFolderImageIndex(activePreset.value.id)
  const cloudIndex = new Map(cloudImages.map((image) => [normalizePath(image.relativePath), image]))
  items.value = items.value.map((item) => {
    const remote = cloudIndex.get(item.relativePath)
    const status = resolveStatus(item.file, remote)
    return {
      ...item,
      status,
      selected: status === 'New' || status === 'Changed'
    }
  })
}

async function uploadOne(item) {
  const data = new FormData()
  data.append('file', item.file)
  data.append('title', item.file.name)
  data.append('description', '')
  data.append('category', category.value || 'Uncategorized')
  data.append('tags', tags.value)
  data.append('sourceName', activePreset.value.sourcePath || activePreset.value.name)
  data.append('relativePath', item.relativePath)
  data.append('originalPath', buildOriginalPath(activePreset.value, item.relativePath))
  data.append('lastModified', String(item.file.lastModified || Date.now()))

  try {
    const { data: response } = await uploadFolderImage(activePreset.value.id, data)
    results[response.status] = (results[response.status] || 0) + 1
    item.status = response.status === 'skipped' ? 'Unchanged' : 'Uploaded'
  } catch (err) {
    item.status = 'Failed'
    results.failed += 1
  } finally {
    uploadedCount.value += 1
  }
}

function selectAll(value) {
  items.value.forEach((item) => {
    item.selected = value
  })
}

function clearScan() {
  revokePreviews()
  items.value = []
  hasScanned.value = false
  selectedTotal.value = 0
  uploadedCount.value = 0
  resetResults()
}

function resetResults() {
  results.uploaded = 0
  results.updated = 0
  results.skipped = 0
  results.deleted = 0
  results.failed = 0
}

function revokePreviews() {
  items.value.forEach((item) => URL.revokeObjectURL(item.previewUrl))
}

function refreshActivePreset() {
  if (!activePreset.value) {
    return
  }
  const updated = presets.value.find((preset) => preset.id === activePreset.value.id)
  if (updated) {
    activePreset.value = updated
  }
}

async function readDirectoryFiles(handle, prefix = '') {
  const entries = []
  for await (const [name, child] of handle.entries()) {
    const relativePath = prefix ? `${prefix}/${name}` : name
    if (child.kind === 'file') {
      const file = await child.getFile()
      entries.push({ file, relativePath })
    } else if (child.kind === 'directory') {
      entries.push(...await readDirectoryFiles(child, relativePath))
    }
  }
  return entries
}

async function verifyPermission(handle) {
  const options = { mode: 'read' }
  if ((await handle.queryPermission(options)) === 'granted') {
    return true
  }
  return (await handle.requestPermission(options)) === 'granted'
}

function resolveStatus(file, remote) {
  if (!remote) {
    return 'New'
  }
  if (Number(remote.fileSize) !== Number(file.size) || Number(remote.lastModified) !== Number(file.lastModified)) {
    return 'Changed'
  }
  return 'Unchanged'
}

function buildOriginalPath(preset, relativePath) {
  if (!preset.sourcePath) {
    return relativePath
  }
  return `${normalizePath(preset.sourcePath).replace(/\/$/, '')}/${relativePath}`
}

function isImageFile(file) {
  const lower = file.name.toLowerCase()
  return IMAGE_EXTENSIONS.some((extension) => lower.endsWith(extension))
}

function supportsDirectoryPicker() {
  return typeof window.showDirectoryPicker === 'function'
}

function normalizePath(value) {
  return (value || '').replace(/\\/g, '/')
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

function formatDate(value) {
  if (!value) {
    return '-'
  }
  return new Date(Number(value)).toLocaleString()
}

function formatDateTime(value) {
  if (!value) {
    return 'Never'
  }
  return new Date(value).toLocaleString()
}
</script>
