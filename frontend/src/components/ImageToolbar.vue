<template>
  <section class="toolbar">
    <div class="toolbar-main">
      <input
        :value="filters.keyword"
        class="input search-input"
        placeholder="Search title, category, tags, file name or path"
        @input="update('keyword', $event.target.value)"
        @keyup.enter="$emit('apply')"
      />
      <button class="button primary" @click="$emit('apply')">Search</button>
      <button class="button ghost" @click="$emit('reset')">Reset</button>
    </div>

    <div class="toolbar-filters">
      <label>
        Source
        <select :value="filters.source" class="input" @change="update('source', $event.target.value)">
          <option value="">All Sources</option>
          <option v-for="source in sources" :key="source" :value="source">{{ source }}</option>
        </select>
      </label>

      <label>
        Tag
        <select :value="filters.tag" class="input" @change="update('tag', $event.target.value)">
          <option value="">All Tags</option>
          <option value="Common">Common</option>
          <option value="Notes">Notes</option>
          <option value="Read Later">Read Later</option>
          <option value="Favorite">Favorite</option>
          <option value="Funny">Funny</option>
        </select>
      </label>

      <label>
        Time
        <select :value="filters.timeRange" class="input" @change="update('timeRange', $event.target.value)">
          <option value="">All Time</option>
          <option value="thisWeek">This Week</option>
          <option value="lastWeek">Last Week</option>
          <option value="older">Older</option>
        </select>
      </label>

      <label>
        Sort
        <select :value="filters.sort" class="input" @change="update('sort', $event.target.value)">
          <option value="time">File Time</option>
          <option value="created">Upload Time</option>
          <option value="title">Title</option>
          <option value="source">Source</option>
          <option value="size">File Size</option>
        </select>
      </label>

      <label>
        View
        <select :value="viewMode" class="input" @change="$emit('update:viewMode', $event.target.value)">
          <option value="grid">Grid</option>
          <option value="detail">Detail</option>
        </select>
      </label>
    </div>
  </section>
</template>

<script setup>
defineProps({
  filters: {
    type: Object,
    required: true
  },
  sources: {
    type: Array,
    default: () => []
  },
  viewMode: {
    type: String,
    default: 'grid'
  }
})

const emit = defineEmits(['update:filters', 'update:viewMode', 'apply', 'reset'])

function update(key, value) {
  emit('update:filters', { key, value })
}
</script>
