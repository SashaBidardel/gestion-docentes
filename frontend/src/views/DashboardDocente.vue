<template>
  <v-container fluid class="pa-6 bg-grey-lighten-4">
    <v-row>
      <v-col cols="12" class="d-flex align-center justify-space-between mb-4">
        <div>
          <h1 class="text-h4 font-weight-bold text-blue-grey-darken-3">Mi Panel de Control</h1>
          <p class="text-subtitle-1 text-grey-darken-1">Gestión personal de ausencias y sustituciones</p>
        </div>
        <div class="d-flex gap-3 align-center">
          <v-chip color="blue-darken-2" variant="flat" size="large" class="px-4">
            <v-icon start icon="mdi-calendar-check"></v-icon>
            Días consumidos: <strong>{{ totalComputado }} / 4</strong>
          </v-chip>
          <v-btn color="white" icon="mdi-refresh" elevation="1" @click="cargarDatos" :loading="cargando"></v-btn>
        </div>
      </v-col>
    </v-row>

    <v-row>
      <v-col cols="12">
        <v-card rounded="xl" elevation="2" border>
          <v-toolbar color="white" flat>
            <v-toolbar-title class="font-weight-bold text-blue-grey-darken-2">
              <v-icon start color="blue">mdi-account-clock</v-icon>
              Mis Solicitudes de Asuntos Propios
            </v-toolbar-title>
          </v-toolbar>
          <v-divider></v-divider>

          <v-table hover density="comfortable">
            <thead>
              <tr class="bg-grey-lighten-4">
                <th class="text-overline font-weight-bold">Fecha</th>
                <th class="text-overline font-weight-bold">Tramo</th>
                <th class="text-overline font-weight-bold">Asignatura</th>
                <th class="text-overline font-weight-bold">Clase (Curso)</th>
                <th class="text-overline font-weight-bold">Aula</th>
                <th class="text-overline font-weight-bold">Motivo</th>
                <th class="text-center text-overline font-weight-bold">Estado</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in solicitudes" :key="item.id">
                <td class="text-capitalize font-weight-medium">
                  {{ formatearFechaLarga(item.diaSolicitado) }}
                </td>

                <td>
                  <v-chip 
                    size="small" 
                    color="blue-grey-darken-3" 
                    variant="flat" 
                    class="font-weight-black"
                  >
                    {{ item.hora || '?' }}ª Hora
                  </v-chip>
                </td>

                <td class="font-weight-bold text-blue-darken-2">
                  {{ item.asignatura || '---' }}
                </td>

                <td>{{ item.curso || '---' }}</td>

                <td>
                  <v-sheet border rounded class="pa-1 text-center font-weight-bold text-caption" max-width="50" color="grey-lighten-4">
                    {{ item.aula || '---' }}
                  </v-sheet>
                </td>

                <td class="text-body-2 text-grey-darken-1">
                  {{ item.descripcion || 'Sin descripción' }}
                </td>

                <td class="text-center">
                  <v-chip
                    :color="getColorEstado(item.estado)"
                    size="small"
                    variant="flat"
                    class="font-weight-black"
                  >
                    {{ item.estado }}
                  </v-chip>
                </td>
              </tr>
              <tr v-if="solicitudes.length === 0">
                <td colspan="7" class="text-center pa-10 text-grey-lighten-1">
                  <v-icon size="64" class="d-block mx-auto mb-2">mdi-clipboard-text-off-outline</v-icon>
                  Aún no has solicitado ningún asunto propio
                </td>
              </tr>
            </tbody>
          </v-table>
        </v-card>
      </v-col>

      <v-col cols="12" class="mt-6">
        <v-card rounded="xl" elevation="2" border>
          <v-toolbar color="white" flat>
            <v-toolbar-title class="font-weight-bold text-orange-darken-4">
              <v-icon start color="orange-darken-2">mdi-shield-account</v-icon>
              Mis Guardias Asignadas (Sustituciones)
            </v-toolbar-title>
          </v-toolbar>
          <v-divider></v-divider>

          <v-table hover density="comfortable">
            <thead>
              <tr class="bg-orange-lighten-5">
                <th class="text-overline font-weight-bold">Fecha Guardia</th>
                <th class="text-overline font-weight-bold">Tramo</th>
                <th class="text-overline font-weight-bold">Docente Ausente</th>
                <th class="text-overline font-weight-bold">Asignatura a Cubrir</th>
                <th class="text-overline font-weight-bold">Aula</th>
              </tr>
            </thead>
            <tbody>
               <tr v-for="guardia in guardias" :key="guardia.id">
                    <td class="text-capitalize">{{ formatearFechaLarga(guardia.fecha) }}</td>
                    
                    <td>
                        <v-chip 
                          size="x-small" 
                          label 
                          color="orange-darken-3" 
                          variant="flat" 
                          class="font-weight-bold text-white"
                        >
                          {{ guardia.hora || '?' }}ª Hora
                        </v-chip>
                    </td>
                    <td class="font-weight-bold">{{ guardia.profesorAusente || '---' }}</td>
                    
                    <td>{{ guardia.asignatura || 'Guardia de Centro' }}</td>
                    
                    <td>
                      <v-chip size="small" label color="grey-darken-4" theme="dark">
                        {{ guardia.aula || 'N/A' }}
                      </v-chip>
                    </td>
                  </tr>
              <tr v-if="guardias.length === 0">
                <td colspan="5" class="text-center pa-10 text-grey-lighten-1">
                  <v-icon size="64" class="d-block mx-auto mb-2">mdi-shield-check-outline</v-icon>
                  No tienes guardias asignadas próximamente
                </td>
              </tr>
            </tbody>
          </v-table>
        </v-card>
      </v-col>
    </v-row>

    <v-overlay v-model="cargando" class="align-center justify-center" persistent>
      <v-progress-circular indeterminate size="64" color="blue-darken-2"></v-progress-circular>
    </v-overlay>
  </v-container>
</template>
<script setup>
import { ref, onMounted, computed } from 'vue';
import axios from 'axios';

//  Inicializar siempre como [] para evitar el error .length
const solicitudes = ref([]);
const guardias = ref([]);
const cargando = ref(false);

// Conteo: Solo APROBADOS y PENDIENTES (Los rechazados no cuentan)
const totalComputado = computed(() => {
  if (!solicitudes.value) return 0; // Salvaguarda extra
  return solicitudes.value.filter(s => 
    s.estado === 'APROBADO' || s.estado === 'PENDIENTE'
  ).length;
});

// Colores según el String del DTO
const getColorEstado = (estado) => {
  if (estado === 'APROBADO') return 'success';
  if (estado === 'RECHAZADO') return 'error';
  return 'warning'; // PENDIENTE
};
const cargarDatos = async () => {
  cargando.value = true;
  
  // 1. Cargar Asuntos Propios (de forma independiente)
  try {
    const resAsuntos = await axios.get('http://localhost:8080/api/asuntos-propios/mis-solicitudes');
    solicitudes.value = resAsuntos.data || [];
  } catch (e) {
    console.error("Error en Asuntos Propios:", e);
    solicitudes.value = [];
  }

  // 2. Cargar Guardias (de forma independiente)
  try {
    const resGuardias = await axios.get('http://localhost:8080/api/guardias/mis-guardias');
    guardias.value = resGuardias.data || [];
  } catch (e) {
    console.error("Error en Guardias (Backend falló):", e);
    guardias.value = []; // Si falla, la tabla de guardias saldrá vacía pero no romperá la otra
  }

  cargando.value = false;
};

// Formateador de fecha amigable (ej: mar, 14 abr)
const formatearFechaLarga = (f) => {
  if (!f) return '---';
  
  // Si la fecha viene como "2026-04-12" (String del JSON)
  if (typeof f === 'string' && f.includes('-')) {
    const [y, m, d] = f.split('-');
    // Esto es mucho más seguro porque no crea objetos Date que fallan con la zona horaria
    return `${d}/${m}/${y}`; 
  }
  return f;
};

onMounted(cargarDatos);
</script>