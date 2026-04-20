<template>
  <v-container class="fill-height" fluid>
    <v-row align="center" justify="center">
      <v-col cols="12" sm="8" md="4">
        <v-card class="elevation-12" rounded="lg">
          <v-toolbar color="primary" dark flat>
            <v-icon start class="ml-4">mdi-lock-reset</v-icon>
            <v-toolbar-title>Actualizar Contraseña</v-toolbar-title>
          </v-toolbar>

          <v-card-text class="pa-6">
            <v-form ref="formRef" v-model="isFormValid" lazy-validation>
              <v-text-field
                v-model="passwordData.actual"
                label="Contraseña Actual"
                prepend-inner-icon="mdi-lock-outline"
                :type="showActual ? 'text' : 'password'"
                @click:append-inner="showActual = !showActual"
                :append-inner-icon="showActual ? 'mdi-eye' : 'mdi-eye-off'"
                variant="outlined"
                :rules="[rules.required]"
                class="mb-2"
              ></v-text-field>

              <v-divider class="my-4"></v-divider>

              <v-text-field
                v-model="passwordData.nueva"
                label="Nueva Contraseña"
                prepend-inner-icon="mdi-lock-plus"
                :type="showNueva ? 'text' : 'password'"
                @click:append-inner="showNueva = !showNueva"
                :append-inner-icon="showNueva ? 'mdi-eye' : 'mdi-eye-off'"
                variant="outlined"
                :rules="[rules.required, rules.min]"
                hint="Mínimo 6 caracteres"
                persistent-hint
                class="mb-4"
              ></v-text-field>

              <v-text-field
                v-model="repeatPassword"
                label="Confirmar Nueva Contraseña"
                prepend-inner-icon="mdi-lock-check"
                type="password"
                variant="outlined"
                :rules="[rules.required, passwordMatch]"
                class="mb-2"
              ></v-text-field>
            </v-form>

            <v-alert v-if="errorMsg" type="error" variant="tonal" class="mt-4" closable>
              {{ errorMsg }}
            </v-alert>
          </v-card-text>

          <v-divider></v-divider>

          <v-card-actions class="pa-4">
            <v-btn variant="text" color="grey-darken-1" @click="router.back()">
              Volver
            </v-btn>
            <v-spacer></v-spacer>
            <v-btn 
              color="primary" 
              elevation="2" 
              :disabled="!isFormValid" 
              :loading="loading"
              @click="handlePasswordChange"
            >
              Guardar Cambios
            </v-btn>
          </v-card-actions>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()
const formRef = ref(null)

// Estado del formulario
const isFormValid = ref(false)
const loading = ref(false)
const errorMsg = ref('')
const showActual = ref(false)
const showNueva = ref(false)
const repeatPassword = ref('')

const passwordData = reactive({
  actual: '',
  nueva: ''
})

// Reglas de validación
const rules = {
  required: value => !!value || 'Este campo es obligatorio.',
  min: v => v.length >= 6 || 'La contraseña debe tener al menos 6 caracteres',
}

const passwordMatch = computed(() => {
  return passwordData.nueva === repeatPassword.value || 'Las contraseñas no coinciden'
})

// Función para enviar los datos al Backend
const handlePasswordChange = async () => {
  const { valid } = await formRef.value.validate()
  if (!valid) return

  loading.value = true
  errorMsg.value = '' 

  try {
    const token = localStorage.getItem('user-token')
    
   
    const payload = {
      oldPassword: passwordData.actual, // Coincide con private String oldPassword
      newPassword: passwordData.nueva   // Coincide con private String newPassword
    }

    // Enviamos la petición
    await axios.post('http://localhost:8080/api/admin/docentes/cambiar-password', payload, {
      headers: { 'Authorization': `Basic ${token}` }
    })

    alert("Contraseña actualizada con éxito. Por seguridad, inicia sesión de nuevo.")
    localStorage.clear()
    router.push('/login')

  } catch (error) {

    //  GlobalExceptionHandler atrapará las excepciones (PasswordInvalidException, etc.)
    console.log("Respuesta completa del servidor:", error.response?.data);

    if (error.response && error.response.data) {
      const data = error.response.data;
      // Extraemos el mensaje específico del throw del Service
      errorMsg.value = data.message || data.error || (typeof data === 'string' ? data : "Error interno");
    } else {
      errorMsg.value = "No se pudo conectar con el servidor.";
    }
  } finally {
    loading.value = false;
  }
}

</script>