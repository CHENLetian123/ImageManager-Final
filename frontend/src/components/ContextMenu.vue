<template>
  <div v-if="visible" class="context-menu" :style="{ left: `${x}px`, top: `${y}px` }" @click.stop>
    <button @click="$emit('copy', image)">Copy</button>
    <button @click="$emit('detail', image)">Detail</button>
    <button @click="$emit('toggleTag', image, 'Common')">{{ tagLabel('Common') }}</button>
    <button @click="$emit('toggleTag', image, 'Notes')">{{ tagLabel('Notes') }}</button>
    <button @click="$emit('toggleTag', image, 'Read Later')">{{ tagLabel('Read Later') }}</button>
    <button @click="$emit('toggleTag', image, 'Favorite')">{{ tagLabel('Favorite') }}</button>
    <button @click="$emit('toggleTag', image, 'Funny')">{{ tagLabel('Funny') }}</button>
    <button @click="customTag">Custom Tag</button>
    <button class="danger-text" @click="$emit('delete', image)">Delete</button>
    <button @click="$emit('close')">Close</button>
  </div>
</template>

<script setup>
const props = defineProps({
  visible: Boolean,
  x: {
    type: Number,
    default: 0
  },
  y: {
    type: Number,
    default: 0
  },
  image: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['copy', 'detail', 'toggleTag', 'delete', 'close'])

function tagLabel(tag) {
  const tags = props.image?.tags ? props.image.tags.split(',').map((item) => item.trim()) : []
  return tags.includes(tag) ? `${tag} (selected)` : `Add ${tag}`
}

function customTag() {
  const tag = window.prompt('Please enter a custom tag:')
  if (tag && tag.trim()) {
    emit('toggleTag', props.image, tag.trim())
  }
}
</script>
