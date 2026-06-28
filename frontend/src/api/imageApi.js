import http from './http'

export function listImages(params) {
  return http.get('/api/images', { params })
}

export function getSources() {
  return http.get('/api/images/sources')
}

export function uploadImage(formData, onUploadProgress) {
  return http.post('/api/images/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    onUploadProgress
  })
}

export function getImage(id) {
  return http.get(`/api/images/${id}`)
}

export function getImageContent(id) {
  return http.get(`/api/images/${id}/content`, { responseType: 'blob' })
}

export function deleteImage(id) {
  return http.delete(`/api/images/${id}`)
}

export function toggleTag(id, tag) {
  return http.post(`/api/images/${id}/tags/toggle`, { tag })
}
