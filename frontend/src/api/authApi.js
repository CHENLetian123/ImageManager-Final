import http from './http'

export function register(data) {
  return http.post('/api/auth/register', data)
}

export function login(data) {
  return http.post('/api/auth/login', data)
}

export function me() {
  return http.get('/api/auth/me')
}
