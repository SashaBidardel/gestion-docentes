<template>
  <v-navigation-drawer v-model="drawer" temporary>
    <v-list>
      <v-list-item class="pa-4">
        <template v-slot:prepend>
          <v-avatar color="primary" class="mr-2">
            <span class="text-white font-weight-bold">
              {{ userName.charAt(0).toUpperCase() }}
            </span>
          </v-avatar>
        </template>
        <v-list-item-title class="text-h6">{{ userName }}</v-list-item-title>
        <v-list-item-subtitle>{{ userRole }}</v-list-item-subtitle>
      </v-list-item>

      <v-divider></v-divider>

      <template v-if="userRole === 'ADMIN'">
        <v-list-item prepend-icon="mdi-view-dashboard" to="/admin/dashboard" title="Dashboard"></v-list-item>
        <v-list-item prepend-icon="mdi-account-plus" to="/crear-usuario" title="Registrar Usuarios"></v-list-item>
        <v-list-item prepend-icon="mdi-account-group" to="/admin/docentes" title="Docentes"></v-list-item>
        <v-list-item prepend-icon="mdi-calendar-edit" to="/asuntos-propios" title="Asuntos Propios"></v-list-item>
        <v-list-item prepend-icon="mdi-shield-check" to="/guardias" title="Guardias"></v-list-item>
      </template>

      <template v-if="userRole === 'DOCENTE'">
        <v-list-item prepend-icon="mdi-view-dashboard" to="/docente/dashboard" title="Mi Panel"></v-list-item>
        <v-list-item prepend-icon="mdi-calendar-edit" to="/asuntos-propios-docente" title="Mis Asuntos"></v-list-item>
        <v-list-item prepend-icon="mdi-shield-check" to="/guardias-docente" title="Mis Guardias"></v-list-item>
      </template>

      <v-divider></v-divider>

      <v-list-item 
        prepend-icon="mdi-key" 
        to="/perfil/cambiar-password" 
        title="Cambiar Contraseña"
      ></v-list-item>
      
      <v-list-item 
        prepend-icon="mdi-logout" 
        title="Cerrar Sesión" 
        color="error" 
        @click="logout"
      ></v-list-item>
    </v-list>
  </v-navigation-drawer>

  <v-app-bar color="primary" elevation="2">
    <v-app-bar-nav-icon @click="drawer = !drawer" class="d-md-none"></v-app-bar-nav-icon>

    <v-app-bar-title class="font-weight-bold d-flex align-center">
      <v-icon icon="mdi-school" class="mr-2"></v-icon>
      <span class="d-none d-sm-inline">Gestión Docente</span>
    </v-app-bar-title>

    <v-spacer></v-spacer>

    <div class="d-none d-md-flex">
      <template v-if="userRole === 'ADMIN'">
        <v-btn variant="text" to="/admin/dashboard">Dashboard</v-btn>
        <v-btn variant="text" to="/crear-usuario">Usuarios</v-btn>
        <v-btn variant="text" to="/admin/docentes">Docentes</v-btn>
        <v-btn variant="text" to="/asuntos-propios">Asuntos</v-btn>
        <v-btn variant="text" to="/guardias">Guardias</v-btn>
      </template>
      
      <template v-if="userRole === 'DOCENTE'">
        <v-btn variant="text" to="/docente/dashboard">Mi Dashboard</v-btn>
        <v-btn variant="text" to="/asuntos-propios-docente">Asuntos</v-btn>
        <v-btn variant="text" to="/guardias-docente">Guardias</v-btn>
      </template>
    </div>

    <v-menu min-width="220px" rounded="lg" location="bottom end">
      <template v-slot:activator="{ props }">
        <v-btn icon v-bind="props" class="ml-2">
          <v-avatar color="white" size="38">
            <span class="text-primary font-weight-bold">
              {{ userName.charAt(0).toUpperCase() }}
            </span>
          </v-avatar>
        </v-btn>
      </template>

      <v-card class="mt-2">
        <v-list>
          <v-list-item
            class="text-center"
            :title="userName"
            :subtitle="userRole"
          >
            <template v-slot:prepend>
              <v-avatar color="primary" class="mx-auto mb-2">
                <span class="text-white">{{ userName.charAt(0).toUpperCase() }}</span>
              </v-avatar>
            </template>
          </v-list-item>
        </v-list>

        <v-divider></v-divider>

        <v-card-actions class="flex-column">
          <v-btn 
            variant="text" 
            block 
            prepend-icon="mdi-key-outline" 
            to="/perfil/cambiar-password"
            class="justify-start"
          >
            Cambiar Contraseña
          </v-btn>

          <v-btn 
            variant="text" 
            block 
            prepend-icon="mdi-logout" 
            color="error" 
            @click="logout"
            class="justify-start mt-1"
          >
            Cerrar Sesión
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-menu>
  </v-app-bar>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// Variables reactivas
const drawer = ref(false)
const userRole = ref('')
const userName = ref('')

// Recuperar datos de sesión al montar
onMounted(() => {
  userRole.value = localStorage.getItem('user-role') || ''
  userName.value = localStorage.getItem('user-nombre') || 'Usuario'
})

// Función de Cierre de Sesión
const logout = () => {
  // 1. Limpiar almacenamiento local
  localStorage.clear()
  
  // 2. Redirección forzada al login para limpiar memoria de la App
  window.location.href = '/login'
}
</script>

<style scoped>
/* Eliminar mayúsculas forzadas en botones de Vuetify */
.v-btn {
  text-transform: none;
  letter-spacing: normal;
}

/* Estilo para que el botón de logout no parezca un botón de acción estándar */
.justify-start {
  justify-content: flex-start !important;
}
</style>