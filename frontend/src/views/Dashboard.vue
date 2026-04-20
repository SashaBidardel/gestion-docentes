<template>
  <div class="admin-dashboard p-6 bg-slate-50 min-h-screen font-sans">
    <header class="mb-8 flex justify-between items-center">
      <div>
        <h1 class="text-3xl font-bold text-slate-800">Panel de Control</h1>
        <p class="text-slate-500">Gestión masiva de datos y automatización de guardias</p>
      </div>
      
      <button @click="reiniciarSistema" 
        class="bg-red-50 text-red-600 border border-red-200 px-4 py-2 rounded-lg font-bold text-sm hover:bg-red-600 hover:text-white transition-all shadow-sm">
        Reiniciar Sistema 🔄
      </button>
    </header>

    <div class="grid grid-cols-1 lg:grid-cols-2 gap-8">
      
      <section class="admin-card border-t-4 border-t-blue-400">
        <div class="flex items-center gap-3 mb-6">
          <div class="p-2 bg-blue-100 rounded-lg text-blue-600 font-bold text-xl">📊</div>
          <h2 class="text-xl font-bold text-slate-800">Base de Datos</h2>
        </div>
        <p class="text-sm text-slate-600 mb-4 font-medium">Actualiza la lista de docentes y sus horarios semanales.</p>

        <div class="dropzone"
          :class="{ 'dragging': isDraggingExcel, 'has-file': fileExcel }"
          @dragover.prevent="isDraggingExcel = true"
          @dragleave.prevent="isDraggingExcel = false"
          @drop.prevent="handleDropExcel"
          @click="$refs.fileInputExcel.click()">
          <input type="file" ref="fileInputExcel" class="hidden" accept=".xlsx" @change="e => fileExcel = e.target.files[0]" />
          
          <div v-if="!fileExcel" class="py-8 text-slate-500 text-center">
            <p class="font-bold">Subir Horarios / Docentes</p>
            <p class="text-xs text-slate-400">Haz clic o arrastra un archivo .xlsx</p>
          </div>
          <div v-else class="py-8 text-green-700 font-bold text-sm text-center">
            ✅ {{ fileExcel.name }}
          </div>
        </div>

        <button @click="subirExcelGeneral" :disabled="!fileExcel || loading"
          class="w-full py-3 mt-4 bg-blue-400 text-black rounded-lg font-bold hover:bg-blue-500 disabled:bg-slate-300 transition-all shadow-sm">
          {{ loading ? 'Procesando...' : 'Actualizar Base de Datos' }}
        </button>
      </section>

      <section class="admin-card border-t-4 border-t-amber-400">
        <div class="flex items-center gap-3 mb-6">
          <div class="p-2 bg-amber-100 rounded-lg text-amber-600 font-bold text-xl">🚨</div>
          <h2 class="text-xl font-bold text-slate-800">Generar Guardias Lote</h2>
        </div>
        <p class="text-sm text-slate-600 mb-4 font-medium">Sube el Excel de ausencias del día para generar guardias.</p>

        <div class="dropzone"
          :class="{ 'dragging': isDraggingAusencia, 'has-file': fileAusencia }"
          @dragover.prevent="isDraggingAusencia = true"
          @dragleave.prevent="isDraggingAusencia = false"
          @drop.prevent="handleDropAusencia"
          @click="$refs.fileInputAusencia.click()">
          <input type="file" ref="fileInputAusencia" class="hidden" accept=".xlsx" @change="e => fileAusencia = e.target.files[0]" />
          
          <div v-if="!fileAusencia" class="py-8 text-amber-900/60 text-center">
            <p class="font-bold">Importar IDs de Ausencia</p>
            <p class="text-xs text-amber-700/50">Haz clic o arrastra el Excel diario</p>
          </div>
          <div v-else class="py-8 text-amber-800 font-bold text-sm text-center">
            📄 {{ fileAusencia.name }}
          </div>
        </div>

        <button @click="procesarAusencias" :disabled="!fileAusencia || loadingGuardias"
          class="w-full py-3 mt-4 bg-amber-400 text-black rounded-lg font-bold hover:bg-amber-500 disabled:bg-slate-300 transition-all shadow-sm">
          {{ loadingGuardias ? 'Procesando...' : 'Generar Guardias' }}
        </button>

        <div v-if="mensajeExito" class="mt-4 p-4 bg-green-100 border border-green-200 text-green-800 rounded-lg flex items-center gap-2">
            <span>✅</span> <b>{{ mensajeExito }}</b>
        </div>
      </section>

    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import axios from 'axios';

// --- CONFIGURACIÓN ---
const BASE_URL = 'http://localhost:8080/api';

// --- ESTADOS ---
const loading = ref(false);
const loadingGuardias = ref(false);
const mensajeExito = ref("");

const fileExcel = ref(null);
const isDraggingExcel = ref(false);

const fileAusencia = ref(null);
const isDraggingAusencia = ref(false);

// --- HANDLERS DRAG & DROP ---
const handleDropExcel = (e) => { 
  isDraggingExcel.value = false; 
  if (e.dataTransfer.files[0]) fileExcel.value = e.dataTransfer.files[0]; 
};
const handleDropAusencia = (e) => { 
  isDraggingAusencia.value = false; 
  if (e.dataTransfer.files[0]) fileAusencia.value = e.dataTransfer.files[0]; 
};

// --- LÓGICA SUBIR EXCEL DOCENTES ---
const subirExcelGeneral = async () => {
  if (!fileExcel.value) return;
  loading.value = true;
  const formData = new FormData();
  formData.append('file', fileExcel.value);
  
  try {
    await axios.post(`${BASE_URL}/importar/excel`, formData);
    alert('Base de datos actualizada con éxito.');
    fileExcel.value = null; // Corrección: resetear el objeto, no la propiedad name
  } catch (e) { 
    alert('Error al importar base de datos.'); 
  } finally { 
    loading.value = false; 
  }
};

// --- LÓGICA PROCESAR GUARDIAS ---
const procesarAusencias = async () => {
  if (!fileAusencia.value) return;
  loadingGuardias.value = true;
  const formData = new FormData();
  formData.append('file', fileAusencia.value);
  
  try {
    await axios.post(`${BASE_URL}/guardias/admin/procesar-lote`, formData);
    mensajeExito.value = "¡Guardias generadas con éxito!";
    fileAusencia.value = null;
    setTimeout(() => { mensajeExito.value = ""; }, 5000);
  } catch (error) { 
    alert("Error al procesar el lote de guardias."); 
  } finally { 
    loadingGuardias.value = false; 
  }
};

// --- REINICIAR SISTEMA ---
const reiniciarSistema = async () => {
  if (!confirm("🚨 ¡ATENCIÓN! Esto borrará TODAS las guardias, horarios y docentes (excepto administración). ¿Estás seguro?")) return;
  if (!confirm("⚠️ ¿Realmente quieres vaciar el sistema? Esta acción es irreversible.")) return;

  try {
    await axios.delete(`${BASE_URL}/admin/mantenimiento/reiniciar-datos`);
    alert("✅ Sistema reseteado correctamente.");
    window.location.reload(); 
  } catch (error) {
    alert("Error al resetear el sistema. Comprueba la consola del backend.");
  }
};
</script>

<style scoped>
.admin-card {
  background: white;
  padding: 1.5rem;
  border-radius: 1.25rem;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  border: 1px solid #f1f5f9;
}

.dropzone {
  border: 2px dashed #cbd5e1;
  border-radius: 1rem;
  cursor: pointer;
  transition: all 0.3s ease;
  min-height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f8fafc;
}

.dropzone:hover {
  background-color: #f1f5f9;
  border-color: #94a3b8;
}

.dropzone.dragging {
  border-color: #60a5fa;
  background-color: #eff6ff;
}

.dropzone.has-file {
  border-style: solid;
  border-color: #10b981;
  background-color: #f0fdf4;
}

.hidden { display: none; }
</style>