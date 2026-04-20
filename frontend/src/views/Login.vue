<template>
  <v-container class="fill-height bg-grey-lighten-4" fluid>
    <v-row align="center" justify="center">
      <v-col cols="12" sm="8" md="4" lg="4">
        
        <div class="text-center mb-6">
          <v-icon size="64" color="indigo" icon="mdi-account-circle"></v-icon>
          <h1 class="text-h4 font-weight-bold mt-2">Acceso al Sistema</h1>
        </div>

        <v-card class="elevation-12 pa-4" rounded="lg">
          <v-card-text>
            <v-form @submit.prevent="handleLogin">
              
              <v-text-field
              v-model="email"
              label="Correo Electrónico"
              prepend-icon="mdi-email"
              type="text" 
              variant="outlined"
              autocomplete="off"
              readonly
              @focus="removeReadonly"
              placeholder="Introduce tu email"
            ></v-text-field>

            <v-text-field
              v-model="password"
              label="Contraseña"
              prepend-icon="mdi-lock"
              type="password"
              variant="outlined"
              autocomplete="new-password"
              readonly
              @focus="removeReadonly"
              placeholder="Introduce tu contraseña"
            ></v-text-field>

              <v-alert
                v-if="error"
                type="error"
                variant="tonal"
                class="mt-4"
                density="compact"
              >
                {{ error }}
              </v-alert>

              <v-btn
                type="submit"
                color="indigo"
                block
                size="large"
                class="mt-6 font-weight-bold"
                :loading="loading"
              >
                Entrar
              </v-btn>
            </v-form>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup>
import { ref } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'

const router = useRouter()
const email = ref('')
const password = ref('')
const loading = ref(false)
const error = ref(null)

// Evita que el autocompletado del navegador bloquee los campos
const removeReadonly = (event) => {
  event.target.readOnly = false
}

const handleLogin = async () => {
  // 1. Validación inicial
  if (!email.value || !password.value) {
    error.value = "Por favor, rellena todos los campos"
    return
  }

  loading.value = true
  error.value = null

  // 2. Limpieza de datos (Vital para evitar el error 401 por espacios invisibles)
  const emailLimpio = email.value.trim()
  const passwordLimpia = password.value.trim()

  // 3. Generación del Token Basic Auth
  // Usamos un try-catch interno por si hay caracteres especiales no soportados por btoa
  let token;
  try {
    token = btoa(`${emailLimpio}:${passwordLimpia}`)
  } catch (e) {
    error.value = "Caracteres no válidos en las credenciales"
    loading.value = false
    return
  }

  try {
    // 4. Petición al backend
   
    const response = await axios.get('http://localhost:8080/api/admin/docentes/usuario-actual', {
      headers: { 
        'Authorization': `Basic ${token}`,
      }
    })

    const usuario = response.data
    // Extraemos el nombre del rol (asegurándonos de que existe el objeto rol)
    const rolNombre = usuario.rol?.nombre 

    // 5. Persistencia de la sesión
    // Guardamos los datos necesarios para que el resto de la App sepa quién ha entrado
    localStorage.setItem('user-token', token)
    localStorage.setItem('user-role', rolNombre)
    localStorage.setItem('user-nombre', usuario.nombre)
    localStorage.setItem('user-email', usuario.email)

    // 6. Redirección inteligente según el Rol
    console.log("Login exitoso. Rol detectado:", rolNombre)
    
    if (rolNombre === 'ADMIN') {
      router.push('/admin/dashboard')
    } else if (rolNombre === 'DOCENTE') {
      router.push('/docente/dashboard')
    } else {
      error.value = "Usuario sin rol asignado. Contacte con el administrador."
      localStorage.clear()
    }

  } catch (err) {
    console.error("Error detallado de login:", err)
    
    if (err.response) {
      // El servidor respondió con un código de error
      switch (err.response.status) {
        case 401:
          error.value = "Correo o contraseña incorrectos"
          break
        case 403:
          error.value = "No tienes permisos para acceder a esta zona"
          break
        case 404:
          error.value = "Servicio de autenticación no encontrado (404)"
          break
        default:
          error.value = "Error en el servidor (" + err.response.status + ")"
      }
    } else if (err.request) {
      // La petición se hizo pero no hubo respuesta (CORS o Servidor caído)
      error.value = "No se pudo conectar con el servidor. Revisa el CORS o el estado del backend."
    } else {
      error.value = "Error al procesar la solicitud"
    }
    
    localStorage.clear()
  } finally {
    loading.value = false
  }
}
</script>