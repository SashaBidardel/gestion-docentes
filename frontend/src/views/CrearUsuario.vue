<template>
  <v-container class="fill-height" fluid>
    <v-row align="center" justify="center">
      <v-col cols="12" sm="8" md="6">
        <v-card class="elevation-12" rounded="lg">
          <v-toolbar color="primary" dark flat>
            <v-icon start class="ml-4">mdi-account-plus</v-icon>
            <v-toolbar-title>Registrar Nuevo Docente</v-toolbar-title>
          </v-toolbar>

          <v-card-text class="pa-6">
            <v-form ref="formRef" autocomplete="off">
              <v-row>
                <v-col cols="12" md="6">
                  <v-text-field
                    v-model="nuevoDocente.nombre"
                    label="Nombre"
                    prepend-inner-icon="mdi-account"
                    variant="outlined"
                  ></v-text-field>
                </v-col>
                <v-col cols="12" md="6">
                  <v-text-field
                    v-model="nuevoDocente.apellidos"
                    label="Apellidos"
                    prepend-inner-icon="mdi-account-details"
                    variant="outlined"
                  ></v-text-field>
                </v-col>
              </v-row>

              <v-row>
                <v-col cols="12" md="8">
                  <v-text-field
                    v-model="emailName"
                    label="Usuario de Correo"
                    prepend-inner-icon="mdi-email"
                    suffix="@centro.es"
                    variant="outlined"
                    hint="Ej: alicia.fernandez"
                    persistent-hint
                  ></v-text-field>
                </v-col>
                <v-col cols="12" md="4">
                  <v-text-field
                    v-model="nuevoDocente.siglas"
                    label="Siglas"
                    prepend-inner-icon="mdi-badge-account"
                    variant="outlined"
                    hint="Serán su contraseña inicial"
                    persistent-hint
                    @input="nuevoDocente.siglas = nuevoDocente.siglas.toUpperCase()"
                  ></v-text-field>
                </v-col>
              </v-row>

              <v-divider class="my-4"></v-divider>

              <v-row>
                <v-col cols="12" md="6">
                  <v-select
                    v-model="nuevoDocente.tipo"
                    :items="tiposDocente"
                    item-title="text"
                    item-value="value"
                    label="Tipo de Funcionario"
                    prepend-inner-icon="mdi-briefcase-account"
                    variant="outlined"
                  ></v-select>
                </v-col>
                <v-col cols="12" md="6">
                  <v-text-field
                    v-model="nuevoDocente.antiguedad"
                    label="Fecha de Antigüedad"
                    type="date"
                    prepend-inner-icon="mdi-calendar"
                    variant="outlined"
                  ></v-text-field>
                </v-col>
              </v-row>

              <v-row>
                <v-col cols="12" md="6">
                  <v-select
                    v-model="nuevoDocente.departamento.id"
                    :items="departamentos"
                    item-title="text"
                    item-value="value"
                    label="Departamento"
                    prepend-inner-icon="mdi-domain"
                    variant="outlined"
                    :loading="loadingDepts"
                  ></v-select>
                </v-col>
                <v-col cols="12" md="6">
                  <v-select
                    v-model="nuevoDocente.rol.id"
                    :items="roles"
                    item-title="text"
                    item-value="value"
                    label="Rol de Usuario"
                    prepend-inner-icon="mdi-shield-account"
                    variant="outlined"
                  ></v-select>
                </v-col>
              </v-row>
            </v-form>
          </v-card-text>

          <v-divider></v-divider>

          <v-card-actions class="pa-4">
            <v-btn color="grey-darken-1" variant="text" @click="router.push({ name: 'AdminDashboard' })">
              Cancelar
            </v-btn>
            <v-spacer></v-spacer>
            <v-btn 
              color="primary" 
              elevation="2" 
              :disabled="!isFormValid" 
              :loading="isSaving"
              @click="guardarUsuario"
            >
              Crear Usuario
            </v-btn>
          </v-card-actions>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()
const emailName = ref('')
const loadingDepts = ref(false)
const isSaving = ref(false)
const departamentos = ref([])

const nuevoDocente = ref({
  nombre: '',
  apellidos: '',
  email: '',
  siglas: '',
  password: '', // Se enviará vacío o con las siglas
  tipo: 'CARRERA',
  antiguedad: new Date().toISOString().substr(0, 10),
  posicion: 1,
  rol: { id: 2 },
  departamento: { id: null }
})

const tiposDocente = [
  { text: 'Funcionario de Carrera', value: 'CARRERA' },
  { text: 'En Prácticas', value: 'PRACTICAS' },
  { text: 'Interino', value: 'INTERINO' }
]

const roles = [
  { text: 'Administrador', value: 1 }, 
  { text: 'Docente', value: 2 }
]

const cargarDepartamentos = async () => {
  loadingDepts.value = true
  try {
    const response = await axios.get('http://localhost:8080/api/departamentos')
    departamentos.value = response.data.map(dep => ({ text: dep.nombre, value: dep.id }))
    if (departamentos.value.length > 0) nuevoDocente.value.departamento.id = departamentos.value[0].value
  } catch (error) {
    console.error("Error:", error)
  } finally {
    loadingDepts.value = false
  }
}

onMounted(cargarDepartamentos)

const isFormValid = computed(() => {
  return (
    nuevoDocente.value.nombre &&
    emailName.value &&
    nuevoDocente.value.siglas &&
    nuevoDocente.value.departamento.id
  )
})

const guardarUsuario = async () => {
  isSaving.value = true
  nuevoDocente.value.email = `${emailName.value}@centro.es`.toLowerCase()
  
  // Por precaución, mandamos las siglas como password, 
  // aunque el backend probablemente las asigne igual.
  nuevoDocente.value.password = nuevoDocente.value.siglas

  try {
    const token = localStorage.getItem('user-token')
    await axios.post('http://localhost:8080/api/admin/docentes', 
      nuevoDocente.value, 
      { 
        headers: { 'Authorization': `Basic ${token}` },
        withCredentials: true 
      }
    )
    alert(`¡Usuario creado! Contraseña inicial: ${nuevoDocente.value.siglas}`)
    router.push({ name: 'AdminDashboard' })
  } catch (error) {
    alert("Error: " + (error.response?.data?.message || "Servidor no disponible"));
  } finally {
    isSaving.value = false
  }
}
</script>