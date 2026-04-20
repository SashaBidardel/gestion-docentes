<template>
  <v-container>
    <v-card elevation="2" class="pa-4">
      <v-card-title class="d-flex align-center justify-space-between">
        <span class="text-h5 font-weight-bold">Gestión de Asuntos Propios</span>
        <v-btn color="primary" icon="mdi-refresh" @click="fetchAsuntos" :loading="loading"></v-btn>
      </v-card-title>

      <v-divider class="my-4"></v-divider>

      <v-table hover>
        <thead>
          <tr>
            <th>Docente</th>
            <th>Fecha Ausencia</th>
            <th>Hora/Tramo</th>
            <th>Aula</th>
            <th>Descripción</th>
            <th>Estado</th>
            <th>Acciones</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="asunto in asuntos" :key="asunto.id">
            <td class="font-weight-medium">{{ asunto.nombreDocente }}</td>
            <td>{{ formatDate(asunto.diaSolicitado) }}</td>
            <td>
              <v-chip size="small" variant="tonal" color="blue">
                {{ asunto.hora }}
              </v-chip>
            </td>
            <td>{{ asunto.aula }}</td>
            <td class="text-truncate" style="max-width: 200px;">{{ asunto.descripcion }}</td>
            <td>
              <v-chip :color="getStatusColor(asunto.estado)" size="small" label>
                {{ asunto.estado }}
              </v-chip>
            </td>
            <td>
              <v-btn 
                v-if="asunto.estado === 'PENDIENTE'"
                color="success" 
                size="small" 
                prepend-icon="mdi-check"
                @click="abrirModalAprobar(asunto)"
              >
                Aprobar
              </v-btn>
              <span v-else class="text-caption text-grey">Procesado {{ formatDateTime(asunto.fechaTramitacion) }}</span>
            </td>
          </tr>
        </tbody>
      </v-table>
    </v-card>

    <v-dialog v-model="modalAprobar" max-width="500px" persistent>
      <v-card>
        <v-card-title class="bg-primary text-white">Aprobar Solicitud</v-card-title>
        <v-card-text class="mt-4">
          <p class="mb-4 text-body-2">
            Introduce las instrucciones para la guardia de <b>{{ seleccionado?.nombreDocente }}</b>.
          </p>

          <v-file-input
            v-model="archivoExcel"
            label="Adjuntar Excel de Guardia"
            variant="outlined"
            prepend-icon="mdi-microsoft-excel"
            accept=".xlsx, .xls"
            density="compact"
            class="mb-4"
            hint="Sube el archivo generado previamente"
            persistent-hint
            required
          ></v-file-input>
          
          <v-textarea
            v-model="formGuardia.material"
            label="Material para el aula"
            placeholder="Ej: Libro de texto pág 45, ejercicios 1 al 5..."
            variant="outlined"
            rows="3"
            class="mb-2"
          ></v-textarea>

          <v-textarea
            v-model="formGuardia.anotaciones"
            label="Observaciones adicionales"
            placeholder="Ej: Recoger los móviles al entrar..."
            variant="outlined"
            rows="2"
          ></v-textarea>
        </v-card-text>

        <v-card-actions class="pb-4 px-4">
          <v-spacer></v-spacer>
          <v-btn variant="text" @click="cerrarModal">Cancelar</v-btn>
          <v-btn 
            color="success" 
            variant="elevated" 
            @click="confirmarAprobacion" 
            :loading="btnLoading"
            :disabled="!archivoExcel"
          >
            Confirmar y Crear Guardia
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-container>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';

// --- ESTADO ---
const asuntos = ref([]);
const loading = ref(false);
const btnLoading = ref(false);

// Lógica del Modal y Formulario
const modalAprobar = ref(false);
const seleccionado = ref(null);
const archivoExcel = ref(null); // Aquí se guarda el archivo seleccionado
const formGuardia = ref({
  material: '',
  anotaciones: ''
});

// --- FUNCIONES ---

const getStatusColor = (estado) => {
  switch (estado) {
    case 'APROBADO': return 'success';
    case 'RECHAZADO': return 'error';
    case 'PENDIENTE': return 'warning';
    default: return 'grey';
  }
};

const fetchAsuntos = async () => {
  loading.value = true;
  try {
    const response = await axios.get('http://localhost:8080/api/asuntos-propios/admin/listar');
    asuntos.value = response.data;
  } catch (error) {
    console.error("Error al cargar datos:", error);
    alert("Error al cargar datos del servidor");
  } finally {
    loading.value = false;
  }
};

const abrirModalAprobar = (asunto) => {
  seleccionado.value = asunto;
  formGuardia.value = { material: '', anotaciones: '' };
  archivoExcel.value = null; // Resetear archivo
  modalAprobar.value = true;
};

const cerrarModal = () => {
  modalAprobar.value = false;
  archivoExcel.value = null;
};

const confirmarAprobacion = async () => {
  if (!seleccionado.value || !archivoExcel.value) {
    alert("Debes seleccionar una solicitud y adjuntar el archivo Excel.");
    return;
  }

  btnLoading.value = true;

  try {
    // 1. CREAR EL OBJETO MULTIPART (FormData)
    // Esto es vital para que Java no dé error de MultipartException
    const formData = new FormData();
    
    // El nombre 'archivo' debe coincidir con @RequestParam("archivo") en Java
    formData.append('archivo', archivoExcel.value); 
    formData.append('material', formGuardia.value.material);
    formData.append('anotaciones', formGuardia.value.anotaciones);

    // 2. ENVIAR POST CON EL FORM DATA
    await axios.post(
      `http://localhost:8080/api/asuntos-propios/admin/aprobar/${seleccionado.value.id}`, 
      formData, 
      {
        headers: {
          'Content-Type': 'multipart/form-data' // Indica que va un archivo
        }
      }
    );
    
    alert("¡Solicitud aprobada y guardia creada correctamente!");
    cerrarModal();
    fetchAsuntos(); // Recargar la tabla
    
  } catch (error) {
    console.error("Error al aprobar:", error.response?.data);
    alert("Error: " + (error.response?.data || "Fallo en el servidor (500)"));
  } finally {
    btnLoading.value = false;
  }
};

// --- FORMATEADORES ---
const formatDate = (dateString) => {
  if (!dateString) return '';
  return new Date(dateString).toLocaleDateString('es-ES');
};

const formatDateTime = (dt) => {
  if (!dt) return '';
  return new Date(dt).toLocaleString('es-ES', { 
    day:'2-digit', 
    month:'2-digit', 
    hour:'2-digit', 
    minute:'2-digit' 
  });
};

onMounted(fetchAsuntos);
</script>


