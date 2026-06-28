const DB_NAME = 'imagemanager-directory-store'
const STORE_NAME = 'handles'

export async function saveDirectoryHandle(presetId, handle) {
  if (!supportsIndexedDb()) {
    return false
  }
  const db = await openDb()
  return new Promise((resolve, reject) => {
    const request = db.transaction(STORE_NAME, 'readwrite')
      .objectStore(STORE_NAME)
      .put(handle, keyFor(presetId))
    request.onsuccess = () => resolve(true)
    request.onerror = () => reject(request.error)
  })
}

export async function getDirectoryHandle(presetId) {
  if (!supportsIndexedDb()) {
    return null
  }
  const db = await openDb()
  return new Promise((resolve, reject) => {
    const request = db.transaction(STORE_NAME, 'readonly')
      .objectStore(STORE_NAME)
      .get(keyFor(presetId))
    request.onsuccess = () => resolve(request.result || null)
    request.onerror = () => reject(request.error)
  })
}

export async function removeDirectoryHandle(presetId) {
  if (!supportsIndexedDb()) {
    return false
  }
  const db = await openDb()
  return new Promise((resolve, reject) => {
    const request = db.transaction(STORE_NAME, 'readwrite')
      .objectStore(STORE_NAME)
      .delete(keyFor(presetId))
    request.onsuccess = () => resolve(true)
    request.onerror = () => reject(request.error)
  })
}

function openDb() {
  return new Promise((resolve, reject) => {
    const request = indexedDB.open(DB_NAME, 1)
    request.onupgradeneeded = () => {
      request.result.createObjectStore(STORE_NAME)
    }
    request.onsuccess = () => resolve(request.result)
    request.onerror = () => reject(request.error)
  })
}

function supportsIndexedDb() {
  return typeof indexedDB !== 'undefined'
}

function keyFor(presetId) {
  return `folderPreset:${presetId}`
}
