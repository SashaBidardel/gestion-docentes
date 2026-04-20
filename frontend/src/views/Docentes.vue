<template>
  <v-container fluid class="pa-6">
    <v-row class="mb-6">
      <v-col cols="12" class="d-flex align-center justify-space-between">
        <div>
          <h1 class="text-h4 font-weight-bold text-primary">
            <v-icon icon="mdi-account-tie" class="mr-2"></v-icon>
            Gestión de Docentes
          </h1>
        </div>
        <v-btn color="primary" prepend-icon="mdi-plus" @click="irACrearDocente">
          Nuevo Docente
        </v-btn>
      </v-col>
    </v-row>

    <v-card border flat rounded="xl">
      <v-card-title class="pa-4">
        <v-text-field
          v-model="search"
          prepend-inner-icon="mdi-magnify"
          label="Buscar por nombre, email o departamento..."
          variant="outlined"
          density="compact"
          hide-details
          clearable
        ></v-text-field>
      </v-card-title>

      <v-table hover>
        <thead>
          <tr>
            <th class="text-left">ID</th>
            <th class="text-left">Nombre</th>
            <th class="text-left">Email</th>
            <th class="text-left">Departamento</th>
            <th class="text-center">Tipo</th>
            <th class="text-center">Antigüedad</th>
            <th class="text-center">Posición</th>
            <th class="text-center">Acciones</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="docente in paginatedDocentes" :key="docente.id">
            <td>#{{ docente.id }}</td>
            <td class="font-weight-medium">{{ docente.nombre }} {{ docente.apellidos }}</td>
            <td>{{ docente.email }}</td>
            <td>
              <div class="d-flex align-center">
                <v-icon start size="16" color="indigo-lighten-2">mdi-office-building</v-icon>
                <span class="text-body-2">{{ docente.departamento?.nombre || 'Sin asignar' }}</span>
              </div>
            </td>
            <td class="text-center">
              <v-chip 
                :color="getTipoColor(docente.tipo)" 
                size="small" 
                label
                variant="flat"
                class="text-uppercase font-weight-bold text-white"
              >
                {{ docente.tipo || 'N/A' }}
              </v-chip>
            </td>
            <td class="text-center">
              <v-icon start size="14" color="grey">mdi-calendar</v-icon>
              {{ formatearFecha(docente.antiguedad) }}
            </td>
            <td class="text-center">
              <v-avatar color="blue-lighten-5" size="28" class="text-blue-darken-3 font-weight-bold text-caption">
                {{ docente.posicion }}
              </v-avatar>
            </td>
            <td class="text-center">
              <v-btn 
                icon="mdi-pencil-outline" 
                variant="text" 
                :color="esMismoUsuario(docente.email) ? 'grey-lighten-1' : 'blue'" 
                :disabled="esMismoUsuario(docente.email)" 
                @click="editarDocente(docente.id)"
              ></v-btn>
              <v-btn 
                icon="mdi-delete-outline" 
                variant="text" 
                :color="esMismoUsuario(docente.email) ? 'grey-lighten-1' : 'error'" 
                :disabled="esMismoUsuario(docente.email)" 
                @click="confirmarBorrado(docente)"
              ></v-btn>
            </td>
          </tr>
        </tbody>
      </v-table>

      <v-divider></v-divider>

      <div class="pa-4 d-flex align-center justify-center">
        <v-pagination
          v-model="page"
          :length="pageCount"
          :total-visible="7"
          color="primary"
          rounded="circle"
          density="comfortable"
        ></v-pagination>
      </div>
    </v-card>

    <v-dialog v-model="dialogBorrar" max-width="400px">
      <v-card rounded="lg" class="pa-4">
        <v-card-title class="text-h5 d-flex align-center">
          <v-icon color="error" icon="mdi-alert-circle" class="me-2"></v-icon>
          ¿Confirmar borrado?
        </v-card-title>
        <v-card-text class="pt-2">
          Estás a punto de eliminar a <strong>{{ docenteAeliminar?.nombre }} {{ docenteAeliminar?.apellidos }}</strong>. 
          Esta acción no se puede deshacer.
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn variant="text" @click="dialogBorrar = false">Cancelar</v-btn>
          <v-btn color="error" variant="flat" :loading="loadingBorrado" @click="ejecutarBorrado">
            Eliminar definitivamente
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-container>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue' 
import axios from 'axios'
import { useRouter } from 'vue-router'

const router = useRouter()
const search = ref('')
const docentes = ref([])
const loading = ref(false)
const page = ref(1)
const itemsPerPage = 10

const dialogBorrar = ref(false)
const docenteAeliminar = ref(null)
const loadingBorrado = ref(false)
const emailLogueado = ref(localStorage.getItem('user-email')) 

// --- SEGURIDAD ---
const esMismoUsuario = (emailDocente) => {
  if (!emailDocente || !emailLogueado.value) return false
  return emailDocente.trim().toLowerCase() === emailLogueado.value.trim().toLowerCase()
}

// --- DATOS ---
const cargarDocentes = async () => {
  loading.value = true
  try {
    const token = localStorage.getItem('user-token')
    const response = await axios.get('http://localhost:8080/api/admin/docentes', {
      headers: { 'Authorization': `Basic ${token}` }
    })
    docentes.value = response.data
  } catch (err) {
    console.error("Error al cargar:", err)
  } finally {
    loading.value = false
  }
}

// --- BUSCADOR + PAGINACIÓN (UNIFICADO) ---
const filteredList = computed(() => {
  let result = [...docentes.value]
  
  if (search.value) {
    const term = search.value.toLowerCase().trim()
    result = result.filter(d => 
      d.nombre?.toLowerCase().includes(term) || 
      d.apellidos?.toLowerCase().includes(term) || 
      d.email?.toLowerCase().includes(term) ||
      d.departamento?.nombre?.toLowerCase().includes(term)
    )
  }
  
  // Ordenamos por ID para que la paginación sea estable
  return result.sort((a, b) => a.id - b.id)
})

const paginatedDocentes = computed(() => {
  const start = (page.value - 1) * itemsPerPage
  return filteredList.value.slice(start, start + itemsPerPage)
})

const pageCount = computed(() => {
  return Math.ceil(filteredList.value.length / itemsPerPage) || 1
})

// Reiniciar a la página 1 cuando se busca algo
watch(search, () => {
  page.value = 1
})

// --- FORMATOS ---
const getTipoColor = (tipo) => {
  if (!tipo) return 'grey-lighten-2'; 

  const t = tipo.toString().trim().toUpperCase(); 
  
  // Colores para los tipos reales
  switch (t) {
    case 'CARRERA': 
      return 'blue'; 
    case 'INTERINO': 
      return 'orange';  
    case 'PRÁCTICAS': 
      return 'green';   
    default: 
      return 'grey'; 
  }
};

const formatearFecha = (fecha) => {
  if (!fecha) return 'Sin fecha'
  if (Array.isArray(fecha)) {
    const d = String(fecha[2]).padStart(2, '0')
    const m = String(fecha[1]).padStart(2, '0')
    return `${d}/${m}/${fecha[0]}`
  }
  return new Date(fecha).toLocaleDateString('es-ES')
}

// --- ACCIONES ---
const irACrearDocente = () => router.push('/crear-usuario')
const editarDocente = (id) => router.push(`/editar-usuario/${id}`)
const confirmarBorrado = (docente) => {
  docenteAeliminar.value = docente
  dialogBorrar.value = true
}

const ejecutarBorrado = async () => {
  if (!docenteAeliminar.value) return
  loadingBorrado.value = true
  try {
    const token = localStorage.getItem('user-token')
    await axios.delete(`http://localhost:8080/api/admin/docentes/${docenteAeliminar.value.id}`, {
      headers: { 'Authorization': `Basic ${token}` }
    })
    docentes.value = docentes.value.filter(d => d.id !== docenteAeliminar.value.id)
  } catch (err) {
    alert("Error al eliminar docente")
  } finally {
    loadingBorrado.value = false
    dialogBorrar.value = false
    docenteAeliminar.value = null
  }
}

onMounted(() => {
  emailLogueado.value = localStorage.getItem('user-email') 
  cargarDocentes()
})
</script>