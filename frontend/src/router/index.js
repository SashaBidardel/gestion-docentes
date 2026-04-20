import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: () => {
        const role = localStorage.getItem('user-role')
        if (role === 'ADMIN') return { name: 'AdminDashboard' }
        if (role === 'DOCENTE') return { name: 'DocenteDashboard' }
        return { name: 'login' }
      }
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/Login.vue'),
      meta: { isPublic: true }
    },
    {
      path: '/admin/dashboard',
      name: 'AdminDashboard', // <--- Este es el nombre exacto que debes usar
      component: () => import('../views/Dashboard.vue'),
      meta: { requiresAuth: true, role: 'ADMIN' }
    },
    {
      path: '/admin/docentes',
      name: 'Docentes', // <--- Este es el nombre exacto que debes usar
      component: () => import('../views/Docentes.vue'),
      meta: { requiresAuth: true, role: 'ADMIN' }
    },
    {
      path: '/docente/dashboard',
      name: 'DocenteDashboard',
      component: () => import('../views/DashboardDocente.vue'),
      meta: { requiresAuth: true, role: 'DOCENTE' }
    },
    
    {
    path: '/perfil/cambiar-password',
    name: 'CambiarPassword',
    component: () => import('../views/CambiarPassword.vue'),
    meta: { 
      requiresAuth: true // Marcamos que necesita estar logueado
    }
  },
    {
      path: '/crear-usuario',
      name: 'crear-usuario',
      component: () => import('../views/CrearUsuario.vue'),
      meta: { requiresAuth: true, role: 'ADMIN' }
    },
    {
      path: '/test-deps',
      name: 'test-deps',
      component: () => import('../views/ListaDepartamentos.vue'),
      meta: { requiresAuth: true, role: 'ADMIN' }
    },
    {
      path: '/asuntos-propios',
      name: 'asuntos-propios',
      component: () => import('../views/AsuntosPropios.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/asuntos-propios-docente',
      name: 'asuntos-propios-docente',
      component: () => import('../views/AsuntosPropiosDocente.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/guardias',
      name: 'guardias',
      component: () => import('../views/Guardias.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/guardias-docente',
      name: 'guardias-docente',
      component: () => import('../views/GuardiasDocente.vue'),
      meta: { requiresAuth: true }
    },
    {
       path: '/editar-usuario/:id', 
       name: 'EditarUsuario', 
       component: () => import('../views/EditarUsuario.vue'), // Carga perezosa (opcional)
       meta: { requiresAuth: true, role: 'ADMIN' } },
    {
      path: '/register',
      name: 'register',
      component: () => import('../views/Register.vue'),
      meta: { isPublic: true }
    },
    // --- RUTA COMODÍN: Si escribes algo mal, te manda al login ---
    {
      path: '/:pathMatch(.*)*',
      redirect: { name: 'login' }
    }
  ]
})

// --- EL GUARDIÁN MODERNO (Sin avisos de 'next') ---
router.beforeEach((to) => {
  const token = localStorage.getItem('user-token')
  const role = localStorage.getItem('user-role')

  // 1. Si no es pública y no hay token -> al login
  if (!to.meta.isPublic && !token) {
    return { name: 'login' }
  }

  // 2. Si ya hay token e intenta ir al login -> fuera de allí
  if (to.name === 'login' && token) {
    return role === 'ADMIN' ? { name: 'AdminDashboard' } : { name: 'DocenteDashboard' }
  }

  // 3. Protección de roles
  if (to.meta.role && to.meta.role !== role) {
    return role === 'DOCENTE' ? { name: 'DocenteDashboard' } : { name: 'login' }
  }

  // Si todo está bien, no devolvemos nada y la navegación continúa
})

export default router