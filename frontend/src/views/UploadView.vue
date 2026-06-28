<template>
  <section class="content-page narrow">
    <div class="page-header">
      <div>
        <h1>Upload Image</h1>
        <p class="muted">Upload one image from desktop or mobile browser.</p>
      </div>
      <RouterLink class="button" to="/dashboard">Back</RouterLink>
    </div>

    <form class="form-card" @submit.prevent="submit">
      <label>
        Title
        <input v-model.trim="form.title" class="input" placeholder="Image title" />
      </label>
      <label>
        Description
        <textarea v-model.trim="form.description" class="input" rows="4" placeholder="Optional description"></textarea>
      </label>
      <label>
        Category
        <input v-model.trim="form.category" class="input" placeholder="Uncategorized" />
      </label>
      <label>
        Tags
        <input v-model.trim="form.tags" class="input" placeholder="Common,Notes,Favorite" />
      </label>
      <label class="upload-dropzone">
        Image File
        <input class="input" type="file" accept="image/*" required @change="pickFile" />
      </label>

      <img v-if="previewUrl" :src="previewUrl" alt="Preview" class="preview-image" />

      <p v-if="message" class="message success">{{ message }}</p>
      <p v-if="error" class="message error">{{ error }}</p>

      <button class="button primary wide" type="submit" :disabled="loading || !file">
        {{ loading ? 'Uploading...' : 'Upload' }}
      </button>
    </form>
  </section>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { uploadImage } from '../api/imageApi'

const router = useRouter()
const file = ref(null)
const previewUrl = ref('')
const loading = ref(false)
const message = ref('')
const error = ref('')
const form = reactive({
  title: '',
  description: '',
  category: '',
  tags: ''
})

function pickFile(event) {
  const selected = event.target.files[0]
  file.value = selected || null
  previewUrl.value = selected ? URL.createObjectURL(selected) : ''
}

async function submit() {
  if (!file.value) {
    error.value = 'Please choose an image file.'
    return
  }

  loading.value = true
  message.value = ''
  error.value = ''

  const data = new FormData()
  data.append('file', file.value)
  data.append('title', form.title || file.value.name)
  data.append('description', form.description)
  data.append('category', form.category || 'Uncategorized')
  data.append('tags', form.tags)
  data.append('sourceName', 'Upload')
  data.append('sourceType', 'UPLOAD')
  data.append('originalPath', file.value.name)
  data.append('relativePath', file.value.name)
  data.append('lastModified', String(file.value.lastModified || Date.now()))

  try {
    await uploadImage(data)
    message.value = 'Upload success.'
    router.push('/dashboard')
  } catch (err) {
    error.value = err.response?.data?.message || 'Upload failed.'
  } finally {
    loading.value = false
  }
}
</script>
