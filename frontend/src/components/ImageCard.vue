<template>
  <article
    class="image-card"
    :class="{ 'image-card-detail': viewMode === 'detail' }"
    @contextmenu.prevent="$emit('openMenu', $event, image)"
  >
    <div class="thumb-wrap">
      <img :src="image.publicUrl" :alt="image.title || image.originalFileName" loading="lazy" />
    </div>
    <div class="card-body">
      <h3>{{ image.title || image.originalFileName }}</h3>
      <p class="muted">{{ image.category || 'Uncategorized' }}</p>
      <p class="meta-line">{{ image.sourceName || 'Upload' }}</p>
      <p class="meta-line">{{ image.sourceType || 'UPLOAD' }} | {{ formatDate(image.lastModified) }}</p>
      <div class="tag-row">
        <span v-for="tag in tagList" :key="tag" class="tag">{{ tag }}</span>
      </div>
      <div class="card-actions">
        <button class="button small" @click="$emit('copy', image)">Copy</button>
        <button class="button small" @click="$emit('detail', image)">Detail</button>
        <button class="button small danger" @click="$emit('delete', image)">Delete</button>
        <button class="button small ghost" @click="$emit('openMenu', $event, image)">More</button>
      </div>
    </div>
  </article>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  image: {
    type: Object,
    required: true
  },
  viewMode: {
    type: String,
    default: 'grid'
  }
})

defineEmits(['copy', 'detail', 'delete', 'openMenu'])

const tagList = computed(() => {
  if (!props.image.tags) {
    return []
  }
  return props.image.tags.split(',').map((tag) => tag.trim()).filter(Boolean)
})

function formatDate(value) {
  if (!value) {
    return 'No file time'
  }
  return new Date(Number(value)).toLocaleString()
}
</script>
