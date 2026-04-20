<template>
  <v-container class="mt-10">
    <v-row justify="center">
      <v-col cols="12" md="8">
        <v-card elevation="4" border>
          <v-card-title class="bg-primary text-white d-flex align-center">
            <v-icon icon="mdi-database-check" class="me-2"></v-icon>
            Estado de la Conexión con Spring Boot
          </v-card-title>

          <v-card-text class="pa-6">
            <div v-if="loading" class="text-center py-4">
              <v-progress-circular indeterminate color="primary"></v-progress-circular>
              <p class="mt-2">Consultando base de datos...</p>
            </div>

            <v-alert v-if="error" type="error" variant="tonal" icon="mdi-alert-circle" class="mb-4">
              {{ error }}
              <v-btn size="small" color="error" class="ms-4" @click="probarConexion">Reintentar</v-btn>
            </v-alert>

            <div v-if="!loading && departamentos.length > 0">
              <h3 class="text-h6 mb-3">Departamentos Detectados:</h3>
              <v-list border rounded>
                <v-list-item
                  v-for="dep in departamentos"
                  :key="dep.id"
                  :title="dep.nombre"
                  :subtitle="'Código: ' + dep.codigo + ' | Tel: ' + dep.telefono"
                  prepend-icon="mdi-office-building"
                >
                  <template v-slot:append>
                    <v-chip size="small" color="success">ID: {{ dep.id }}</v-chip>
                  </template>
                </v-list-item>
              </v-list>
            </div>
          </v-card-text>

          <v-card-actions class="bg-grey-lighten-4">
            <v-spacer></v-spacer>
            <v-btn color="secondary" to="/">Volver al Inicio</v-btn>
          </v-card-actions>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const departamentos = ref([])
const loading = ref(true)
const error = ref(null)

const probarConexion = async () => {
  loading.value = true
  error.value = null
  
  try {
    const response = await axios.get('http://localhost:8080/api/departamentos')
    departamentos.value = response.data
  } catch (err) {
    console.error(err)
    error.value = "No se pudo conectar con el Backend. ¿Está Spring Boot encendido? ¿Has configurado el CORS?"
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  probarConexion()
})
</script>