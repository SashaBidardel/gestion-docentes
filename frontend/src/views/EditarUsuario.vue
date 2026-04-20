<template>
  <v-container class="pa-6">
    <v-row justify="center">
      <v-col cols="12" md="8">
        <v-card rounded="xl" elevation="2" class="pa-4">
          <v-card-title class="text-h5 font-weight-bold text-primary d-flex align-center">
            <v-btn icon="mdi-arrow-left" variant="text" @click="router.back()" class="me-2"></v-btn>
            Editar Docente
          </v-card-title>
          
          <v-divider class="my-4"></v-divider>

          <v-form @submit.prevent="actualizarDocente">
            <v-row>
              <v-col cols="12" md="6">
                <v-text-field v-model="form.nombre" label="Nombre" variant="outlined" required></v-text-field>
              </v-col>
              <v-col cols="12" md="6">
                <v-text-field v-model="form.apellidos" label="Apellidos" variant="outlined" required></v-text-field>
              </v-col>
              <v-col cols="12">
                <v-text-field v-model="form.email" label="Email" variant="outlined" type="email" required></v-text-field>
              </v-col>
              
              <v-col cols="12" md="4">
                <v-select
                  v-model="form.tipo"
                  :items="['CARRERA', 'INTERINO', 'PRACTICAS']"
                  label="Tipo"
                  variant="outlined"
                ></v-select>
              </v-col>
              <v-col cols="12" md="4">
                <v-text-field v-model="form.antiguedad" label="Antigüedad" type="date" variant="outlined"></v-text-field>
              </v-col>
              <v-col cols="12" md="4">
                <v-text-field v-model.number="form.posicion" label="Posición" type="number" variant="outlined"></v-text-field>
              </v-col>
            </v-row>

            <v-card-actions class="mt-6">
              <v-spacer></v-spacer>
              <v-btn color="grey-darken-1" variant="text" @click="router.back()">Cancelar</v-btn>
              <v-btn color="primary" variant="flat" type="submit" :loading="saving" size="large">
                Guardar Cambios
              </v-btn>
            </v-card-actions>
          </v-form>
        </v-card>
      </v-col>
    </v-row>

    <v-snackbar v-model="snackbar" :color="snackColor">{{ snackText }}</v-snackbar>
  </v-container>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'

const route = useRoute()
const router = useRouter()
const saving = ref(false)

const form = ref({
  nombre: '',
  apellidos: '',
  email: '',
  tipo: '',
  antiguedad: '',
  posicion: 0
})

// Notificaciones
const snackbar = ref(false)
const snackText = ref('')
const snackColor = ref('success')

const cargarDatos = async () => {
  try {
    const id = route.params.id
    const token = localStorage.getItem('user-token')
    console.log("Cargando docente con ID:", id) // <---consola del navegador
    const response = await axios.get(`http://localhost:8080/api/admin/docentes/${id}`, {
      headers: { 'Authorization': `Basic ${token}` }
    })
    
    // Rellenamos el formulario
    form.value = response.data
    
    // Si la fecha viene como Array de Java [2024, 3, 23], la convertimos a YYYY-MM-DD para el input
    if (Array.isArray(response.data.antiguedad)) {
      const [y, m, d] = response.data.antiguedad
      form.value.antiguedad = `${y}-${String(m).padStart(2, '0')}-${String(d).padStart(2, '0')}`
    }
  } catch (err) {
    mostrarMsg("Error al cargar docente", "error")
  }
}

const actualizarDocente = async () => {
  saving.value = true
  try {
    const id = route.params.id
    const token = localStorage.getItem('user-token')
    await axios.put(`http://localhost:8080/api/admin/docentes/${id}`, form.value, {
      headers: { 'Authorization': `Basic ${token}` }
    })
    mostrarMsg("Docente actualizado con éxito")
    setTimeout(() => router.push('/docentes'), 1000)
  } catch (err) {
    mostrarMsg("Error al actualizar", "error")
  } finally {
    saving.value = false
  }
}

const mostrarMsg = (txt, color = 'success') => {
  snackText.value = txt
  snackColor.value = color
  snackbar.value = true
}

onMounted(cargarDatos)
</script>