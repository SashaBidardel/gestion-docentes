<template>
  <div class="container-fluid mt-4">
    <div class="row">
      
      <div class="col-md-5">
        <div class="card shadow-sm border-0">
          <div class="card-header bg-dark text-white d-flex justify-content-between align-center py-3">
            <h5 class="mb-0">Mis Asuntos Propios</h5>
            <div class="d-flex gap-2">
              <span class="badge bg-secondary">T1: {{ resumenTrimestres.t1 }}/3</span>
              <span class="badge bg-secondary">T2: {{ resumenTrimestres.t2 }}/3</span>
              <span class="badge bg-secondary">T3: {{ resumenTrimestres.t3 }}/3</span>
              <span class="badge bg-light text-dark px-3">
                Total: <strong>{{ totalDiasAprobados }}</strong> / 4
              </span>
            </div>
          </div>
          
          <div class="card-body p-0">
            <div class="table-responsive">
              <table class="table table-hover mb-0">
                <thead class="bg-light">
                  <tr>
                    <th class="border-top-0">Detalles de la Solicitud</th>
                    <th class="border-top-0 text-center">Estado</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="solicitud in listaSolicitudes" :key="solicitud.id">
                    <td class="align-middle">
                      <div class="font-weight-bold text-primary">{{ formatearFecha(solicitud.diaSolicitado) }}</div>
                      <div class="text-sm text-dark">{{ solicitud.descripcion || 'Sin motivo' }}</div>
                      <small class="text-muted d-block">
                        <i class="mdi mdi-clock-outline"></i> Hora: {{ solicitud.hora }} | Aula: {{ solicitud.aula }}
                      </small>
                    </td>
                    <td class="text-center align-middle">
                      <span :class="getBadgeClass(solicitud.estado)">
                        {{ solicitud.estado || 'PENDIENTE' }}
                      </span>
                    </td>
                  </tr>
                  <tr v-if="listaSolicitudes.length === 0">
                    <td colspan="2" class="text-center text-muted py-5">
                      No tienes solicitudes registradas aún.
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>

      <div class="col-md-7">
        <div class="card shadow border-primary">
          <div class="card-header bg-primary text-white py-3">
            <h5 class="mb-0 text-center">Registrar Nueva Solicitud</h5>
          </div>
          
          <div class="card-body p-4">
            <div class="mb-4">
              <label class="form-label font-weight-bold text-primary">1. Selecciona la clase a cubrir:</label>
              <select class="form-select form-select-lg border-primary" v-model="form.idHorario">
                <option value="" disabled>Elegir tramo horario...</option>
                <option v-for="h in misHorarios" :key="h.id" :value="h.id">
                  {{ h.descripcion }}
                </option>
              </select>
            </div>

            <div class="mb-4" v-if="form.idHorario">
              <label class="form-label font-weight-bold text-primary">2. Elige el día:</label>
              <input 
                type="date" 
                class="form-control form-control-lg border-primary" 
                v-model="form.fecha" 
                :min="minFecha"
                :class="{'is-invalid': !validarDiaSemana && form.fecha}"
              >
              <div class="invalid-feedback" v-if="!validarDiaSemana && form.fecha">
                Error: El día seleccionado no coincide con tu horario (debe ser {{ getNombreDia(misHorarios.find(h => h.id === form.idHorario)?.diaSemana) }}).
              </div>
            </div>

            <div class="mb-4">
              <label class="form-label font-weight-bold">Descripción:</label>
              <textarea class="form-control" v-model="form.motivo" rows="2" placeholder="Ej: Asuntos particulares..."></textarea>
            </div>

            <div class="row mb-4">
              <div class="col-md-6">
                <label class="form-label font-weight-bold text-success">Material para la guardia:</label>
                <input type="text" class="form-control border-success" v-model="form.material" placeholder="Ej: Fotocopias preparadas">
              </div>
              <div class="col-md-6">
                <label class="form-label font-weight-bold text-success">Anotaciones extras:</label>
                <textarea class="form-control border-success" v-model="form.anotaciones" rows="1" placeholder="Ej: El grupo es 2º ESO A"></textarea>
              </div>
            </div>

            <button 
              class="btn btn-primary btn-lg w-100 shadow-sm" 
              :disabled="!form.fecha || !form.idHorario || !validarDiaSemana || cargando"
              @click="enviarSolicitud"
            >
              <span v-if="cargando" class="spinner-border spinner-border-sm me-2"></span>
              <span v-else>Enviar Solicitud</span>
            </button>
          </div>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import axios from 'axios';

// --- ESTADO ---
const cargando = ref(false);
const listaSolicitudes = ref([]);
const misHorarios = ref([]);


const form = ref({ 
  idHorario: '', 
  fecha: '', 
  motivo: '',
  material: '',
  anotaciones: ''
});

const minFecha = new Date().toISOString().split('T')[0];

// --- LÓGICA COMPUTADA ---
const totalDiasAprobados = computed(() => {
  return listaSolicitudes.value.filter(s => s.estado === 'APROBADO').length;
});

const resumenTrimestres = computed(() => {
  const conteo = { t1: 0, t2: 0, t3: 0 };
  listaSolicitudes.value.forEach(s => {
    if (s.estado === 'APROBADO' && s.diaSolicitado) {
      const fechaObj = new Date(s.diaSolicitado);
      const mes = fechaObj.getMonth() + 1;
      if (mes >= 9 && mes <= 12) conteo.t1++;
      else if (mes >= 1 && mes <= 3) conteo.t2++;
      else conteo.t3++;
    }
  });
  return conteo;
});

const validarDiaSemana = computed(() => {
  if (!form.value.idHorario || !form.value.fecha) return true;
  
  // 1. Forzamos la creación de la fecha sin zonas horarias
  const [year, month, day] = form.value.fecha.split('-').map(Number);
  const fechaObj = new Date(year, month - 1, day);
  
  // 2. OBTENER DÍA JS (0-6)
  let diaJS = fechaObj.getDay(); 
  
  // 3. CONVERTIR A FORMATO "LUNES = 1" (Si tu BD usa ese estándar)
  // Si diaJS es 0 (Domingo), lo pasamos a 7, si no, se queda igual.
  let diaEstandar = (diaJS === 0) ? 7 : diaJS;

  const horario = misHorarios.value.find(h => h.id === form.value.idHorario);
  
  console.log(`Fecha: ${form.value.fecha} | Día JS: ${diaJS} | Día Estándar: ${diaEstandar} | Día BD: ${horario?.diaSemana}`);

  return horario && horario.diaSemana === diaEstandar;
});

// --- FUNCIONES ---
const getNombreDia = (n) => {
  const dias = { 1: 'Lunes', 2: 'Martes', 3: 'Miércoles', 4: 'Jueves', 5: 'Viernes' };
  return dias[n] || 'Día';
};

const cargarMisHorarios = async () => {
  try {
    
    const res = await axios.get(`http://localhost:8080/api/horarios/mis-horarios`);
    
    if (res.data) {
      misHorarios.value = res.data.map(h => ({
        id: h.id,
        diaSemana: h.dia,
        descripcion: `${getNombreDia(h.dia)} - ${h.hora}ª hora | ${h.asignatura?.siglas || 'Clase'} (Aula: ${h.aula || '?'})`
      }));
    }
  } catch (e) { 
    console.error("Error al cargar horarios:", e); 
    // Si da error 403/401 es que el token ha expirado
  }
};


const cargarHistorial = async () => {
  try {
    const res = await axios.get('http://localhost:8080/api/asuntos-propios/mis-solicitudes');
    listaSolicitudes.value = Array.isArray(res.data) ? res.data : [];
  } catch (e) { 
    console.error("Error al cargar historial:", e); 
  }
};

const enviarSolicitud = async () => {
  cargando.value = true;
  try {
    // 1. Extraemos el ID como un número puro
    const idHorarioPuro = parseInt(form.value.idHorario);

    // 2. Creamos el payload PLANO (sin objetos anidados)
    const payload = {
      diaSolicitado: form.value.fecha,        // String "YYYY-MM-DD"
      descripcion: form.value.motivo, // String
      idHorario: idHorarioPuro,       // Number (Long en Java)
      material: form.value.material,  // String
      anotaciones: form.value.anotaciones // String
    };

    console.log("Enviando payload plano:", payload);

    const res = await axios.post('http://localhost:8080/api/asuntos-propios/solicitar', payload);
    
    alert("¡Enviado con éxito!");
    
    // Resetear formulario
    form.value = { idHorario: '', fecha: '', motivo: '', material: '', anotaciones: '' };
    await cargarHistorial();
    
  } catch (e) {
    console.error("Error en el envío:", e.response?.data);
    alert((e.response?.data?.message || "Error de formato en el servidor"));
  } finally {
    cargando.value = false;
  }
};

const getBadgeClass = (estado) => {
  if (estado === 'APROBADO') return 'badge bg-success text-white';
  if (estado === 'RECHAZADO') return 'badge bg-danger text-white';
  return 'badge bg-warning text-dark';
};

const formatearFecha = (f) => {
  if (!f) return '---';
  // Rompemos el string "2026-10-09" por el guion
  const [year, month, day] = f.split('-').map(Number);
  // Creamos la fecha usando números, lo que garantiza el horario LOCAL
  const fechaLocal = new Date(year, month - 1, day);
  
  return fechaLocal.toLocaleDateString('es-ES', { 
    weekday: 'long', 
    day: '2-digit', 
    month: 'long',
    year: 'numeric'
  });
};

onMounted(() => {
  cargarMisHorarios();
  cargarHistorial();
});
</script>