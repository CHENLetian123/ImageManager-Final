import http from './http'

export function listFolderPresets() {
  return http.get('/api/folder-presets')
}

export function createFolderPreset(data) {
  return http.post('/api/folder-presets', data)
}

export function updateFolderPreset(id, data) {
  return http.put(`/api/folder-presets/${id}`, data)
}

export function deleteFolderPreset(id) {
  return http.delete(`/api/folder-presets/${id}`)
}

export function getFolderImageIndex(id) {
  return http.get(`/api/folder-presets/${id}/images/index`)
}

export function uploadFolderImage(id, formData, onUploadProgress) {
  return http.post(`/api/folder-presets/${id}/images/upload`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    onUploadProgress
  })
}

export function cleanupMissingImages(id, data) {
  return http.post(`/api/folder-presets/${id}/cleanup-missing`, data)
}
