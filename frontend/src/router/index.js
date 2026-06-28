import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../views/LoginView.vue'
import RegisterView from '../views/RegisterView.vue'
import DashboardView from '../views/DashboardView.vue'
import UploadView from '../views/UploadView.vue'
import BatchUploadView from '../views/BatchUploadView.vue'
import FolderSyncView from '../views/FolderSyncView.vue'
import ImageDetailView from '../views/ImageDetailView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/dashboard' },
    { path: '/login', component: LoginView, meta: { public: true } },
    { path: '/register', component: RegisterView, meta: { public: true } },
    { path: '/dashboard', component: DashboardView },
    { path: '/upload', component: UploadView },
    { path: '/batch-upload', component: BatchUploadView },
    { path: '/folders', component: FolderSyncView },
    { path: '/images/:id', component: ImageDetailView }
  ]
})

router.beforeEach((to) => {
  const token = localStorage.getItem('token')
  if (!to.meta.public && !token) {
    return '/login'
  }
  if (to.meta.public && token && to.path !== '/register') {
    return '/dashboard'
  }
  return true
})

export default router
