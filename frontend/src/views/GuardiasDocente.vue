<template>
  <div class="container mt-4">
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h2 class="text-primary fw-bold">
        <i class="bi bi-shield-lock me-2"></i>Mi Historial de Guardias
      </h2>
      <button @click="cargarGuardias" class="btn btn-outline-primary btn-sm">
        <i class="bi bi-arrow-clockwise"></i> Actualizar
      </button>
    </div>

    <div class="row mb-4" v-if="guardias.length > 0">
      <div class="col-md-4">
        <div class="card bg-white border-0 shadow-sm border-start border-primary border-4">
          <div class="card-body">
            <h6 class="text-muted mb-1">Sustituciones realizadas</h6>
            <h3 class="fw-bold mb-0">{{ guardias.length }}</h3>
          </div>
        </div>
      </div>
    </div>

    <div class="card shadow-sm border-0">
      <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
          <thead class="table-dark">
            <tr>
              <th class="ps-3">Fecha</th>
              <th>Hora</th>
              <th>Aula</th>
              <th>Profesor Ausente</th>
              <th>Asignatura</th>
              <th>Material</th>
              <th>Anotaciones</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="g in guardias" :key="g.id">
              <td class="ps-3 fw-bold">{{ formatearFecha(g.fecha) }}</td>
              
              <td>
                <span class="badge bg-dark text-light px-2 py-1">
                  {{ g.hora }}ª Hora (Día {{ g.dia }})
                </span>
              </td>

              <td>
                <span class="badge bg-info text-dark fw-medium">{{ g.aula }}</span>
              </td>

              <td>{{ g.profesorAusente }}</td>
              
              <td>
                <span class="text-truncate d-inline-block" style="max-width: 150px;">
                  {{ g.asignatura }}
                </span>
              </td>

              <td>
                <span v-if="g.material && g.material !== 'Sin material'" class="text-muted small">
                  {{ g.material }}
                </span>
                <span v-else class="text-muted fst-italic small">---</span>
              </td>

              <td>
                <span v-if="g.anotacion && g.anotacion.trim() !== ''" class="text-muted small">
                  {{ g.anotacion }}
                </span>
                <span v-else class="text-muted fst-italic small">Sin notas</span>
              </td>
            </tr>

            <tr v-if="guardias.length === 0 && !cargando">
              <td colspan="7" class="text-center py-5 text-muted">
                <i class="bi bi-info-circle me-2"></i> No tienes guardias registradas.
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';

const guardias = ref([]);
const cargando = ref(true);
const docenteId = 29; 

// Formateador robusto para evitar el texto estático
const formatearFecha = (f) => {
  if (!f) return '---';
  // Si viene como string "2026-04-10"
  if (typeof f === 'string' && f.includes('-')) {
    const [y, m, d] = f.split('-');
    return `${d}/${m}/${y}`;
  }
  return f;
};

const cargarGuardias = async () => {
  try {
    cargando.value = true;
    
    // USAMOS EL MISMO ENDPOINT QUE EL DASHBOARD
    // No hace falta pasarle ID ni Email, el Token se encarga de todo
    const response = await axios.get('http://localhost:8080/api/guardias/mis-guardias');
    
    console.log("Datos recibidos del historial (Identificado por Token):", response.data);
    guardias.value = response.data;
    
  } catch (error) {
    console.error("Error en el historial:", error);
  } finally {
    cargando.value = false;
  }
};
onMounted(cargarGuardias);
</script>

<style scoped>
.container { max-width: 1200px; }
.card { border-radius: 12px; overflow: hidden; }
.table thead th { 
  font-size: 0.85rem; 
  text-transform: uppercase; 
  letter-spacing: 0.5px;
  padding: 15px 10px;
}
.table tbody td { padding: 12px 10px; }
.badge { font-size: 0.75rem; border-radius: 4px; }
</style>