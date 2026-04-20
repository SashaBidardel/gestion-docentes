<template>
  <v-container class="py-8">
    <div class="d-flex align-center mb-6">
      <v-avatar color="primary" variant="tonal" class="mr-4" size="48">
        <v-icon icon="mdi-history" color="primary"></v-icon>
      </v-avatar>
      <div>
        <h1 class="text-h4 font-weight-bold text-slate-800">Historial de Guardias</h1>
        <p class="text-subtitle-1 text-medium-emphasis">Registro detallado de ausencias y sustituciones</p>
      </div>
    </div>

    <v-card border flat class="rounded-lg shadow-sm">
      <v-table hover class="guardias-table">
        <thead class="bg-grey-lighten-4">
          <tr>
            <th class="text-uppercase text-caption font-weight-bold">Profesor Ausente</th>
            <th class="text-uppercase text-caption font-weight-bold text-center">Dia</th>
            <th class="text-uppercase text-caption font-weight-bold text-center">Hora</th>
            <th class="text-uppercase text-caption font-weight-bold text-center">Aula</th>
            <th class="text-uppercase text-caption font-weight-bold">Asignatura</th>
            <th class="text-uppercase text-caption font-weight-bold">Sustituto Asignado</th>
            <th class="text-uppercase text-caption font-weight-bold text-center">Acumuladas</th>
            <th class="text-uppercase text-caption font-weight-bold">Material</th>
            <th class="text-uppercase text-caption font-weight-bold">Anotaciones</th>
          </tr>
        </thead>
        
        <tbody>
          <tr v-for="(g, index) in guardias" :key="`guardia-${g.idHorario}-${index}`">
            <td class="font-weight-medium text-blue-darken-2">
              <v-icon icon="mdi-account-minus-outline" size="small" class="mr-2"></v-icon>
              {{ g.profesorAusente }}
            </td>
             <td class="text-center">
              <v-chip size="small" variant="outlined" color="grey-darken-1">
                 {{ nombreDia(g.dia) }} 
              </v-chip>
            </td>
            <td class="text-center">
              <v-chip size="small" variant="outlined" color="grey-darken-1">
                {{ g.hora }}ª hora
              </v-chip>
            </td>
            <td class="text-center">
              <v-chip size="small" variant="outlined" color="grey-darken-1">
                {{ g.aula }}
              </v-chip>
            </td>

            <td>{{ g.asignatura }}</td>
            <td :class="{'bg-red-lighten-5': !g.sustitutoAsignado}">
              <v-chip
                v-if="!g.sustitutoAsignado || g.sustitutoAsignado === ''"
                color="error"
                variant="flat"
                size="small"
                prepend-icon="mdi-alert-decagram"
                class="font-weight-bold"
              >
                SIN SUSTITUTO
              </v-chip>

              <v-chip
                v-else
                color="success"
                variant="tonal"
                size="small"
                prepend-icon="mdi-account-check"
              >
                {{ g.sustitutoAsignado }}
              </v-chip>
            </td>
             

            <td class="text-center">
              <v-tooltip text="Total de guardias realizadas por este docente" location="top">
                <template v-slot:activator="{ props }">
                  <v-avatar 
                    v-bind="props" 
                    :color="g.conteoGuardias > 10 ? 'orange-lighten-4' : 'blue-lighten-4'" 
                    size="32"
                    class="font-weight-bold text-caption"
                  >
                    {{ g.conteoGuardias }}
                  </v-avatar>
                </template>
              </v-tooltip>
            </td>
            
      <td class="text-truncate" style="max-width: 180px;">
              <v-tooltip :text="g.material || 'No se ha especificado material'" location="top">
                <template v-slot:activator="{ props }">
                  <span v-bind="props" :class="g.material ? 'text-grey-darken-3' : 'text-grey-lighten-1 italic'">
                    <v-icon icon="mdi-book-open-variant" size="x-small" class="mr-1"></v-icon>
                    {{ g.material || 'Sin material' }}
                  </span>
                </template>
              </v-tooltip>
      </td>

      <td class="text-truncate" style="max-width: 180px;">
            <v-tooltip :text="g.anotacion || 'Sin notas adicionales'" location="top">
              <template v-slot:activator="{ props }">
                <span v-bind="props" :class="g.anotacion ? 'text-blue-grey-darken-1' : 'text-grey-lighten-1 italic'">
                  <v-icon icon="mdi-comment-text-outline" size="x-small" class="mr-1"></v-icon>
                  {{ g.anotacion || 'Sin notas' }}
                </span>
              </template>
            </v-tooltip>
      </td>
          </tr>
        </tbody>
      </v-table>

      <v-divider></v-divider>
      <div class="pa-4 d-flex align-center justify-space-between">
        <span class="text-caption text-grey-darken-1">
          Mostrando página {{ paginaActual + 1 }} de {{ totalPaginas }}
        </span>
        <v-pagination
          v-model="paginaVisual"
          :length="totalPaginas"
          :total-visible="5"
          rounded="circle"
          size="small"
          active-color="primary"
          @update:model-value="cambiarPagina"
        ></v-pagination>
      </div>
    </v-card>
  </v-container>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

// --- ESTADOS ---
const guardias = ref([])
const paginaActual = ref(0)
const paginaVisual = ref(1) 
const totalPaginas = ref(1)

// --- LÓGICA ---
const nombreDia = (numero) => {
  const dias = {
    1: 'Lunes',
    2: 'Martes',
    3: 'Miércoles',
    4: 'Jueves',
    5: 'Viernes'
  };
  return dias[numero] || 'Desconocido';
}
/**
 * Carga los datos desde el Backend
 */
const obtenerGuardias = async (page = 0) => {
  try {
    const res = await axios.get(`http://localhost:8080/api/guardias/admin/paginado`, {
      params: { 
        page: page, 
        size: 10 
      }
    });

    // Mapeo de datos del Page de Spring
    guardias.value = res.data.content;
    totalPaginas.value = res.data.totalPages;
    paginaActual.value = res.data.number;
    
    // Sincronizamos la visualización de la paginación (Vue empieza en 1, Spring en 0)
    paginaVisual.value = res.data.number + 1;

    // Logs para auditoría en consola
    console.log("--- ACTUALIZACIÓN DE CONTEO ---");
    guardias.value.forEach(g => {
      console.log(`[${g.sustitutoAsignado}] lleva ${g.conteoGuardias} guardias.`);
    });

  } catch (error) {
    console.error("Error al obtener el historial:", error);
  }
}

/**
 * Evento al cambiar de página en el componente
 */
const cambiarPagina = (nuevaPagina) => {
  obtenerGuardias(nuevaPagina - 1);
};

// Carga inicial
onMounted(() => {
  obtenerGuardias(0);
})
</script>

<style scoped>
.guardias-table thead th {
  height: 50px !important;
  color: #64748b !important;
}

.guardias-table tbody td {
  height: 60px !important;
}

.shadow-sm {
  box-shadow: 0 1px 3px 0 rgb(0 0 0 / 0.1), 0 1px 2px -1px rgb(0 0 0 / 0.1) !important;
}

.bg-red-lighten-5 {
  background-color: #ffebee !important;
}

.guardias-table tbody tr:hover {
  background-color: #f8fafc !important;
  transition: background-color 0.2s ease;
}
</style>