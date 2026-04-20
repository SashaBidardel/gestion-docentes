<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios' // Importante para enviar los datos

const router = useRouter()

// 1. Creamos el objeto reactivo para capturar los datos
const usuario = ref({
  nombre: '',
  apellidos: '', // Añadido para que coincida con tu JSON de Java
  email: '',
  password: '',
  confirmPassword: '',
  siglas: '' // Recuerda que tu lógica de Java usa siglas
})

const handleRegister = async () => {
  // Validación básica antes de enviar
  if (usuario.value.password !== usuario.value.confirmPassword) {
    alert("Las contraseñas no coinciden")
    return
  }

  try {
    // 2. Enviamos el objeto al backend 
    const response = await axios.post('http://localhost:8080/api/admin/docentes', {
      nombre: usuario.value.nombre,
      apellidos: usuario.value.apellidos,
      email: usuario.value.email,
      password: usuario.value.password,
      siglas: usuario.value.email.split('@')[0].toUpperCase() // Generamos siglas auto si no hay
    })

    console.log("Registro exitoso:", response.data)
    router.push('/login')
  } catch (error) {
    console.error("Error en el registro:", error.response?.data || error.message)
    alert("Error al crear la cuenta: " + (error.response?.data || "Servidor no disponible"))
  }
}
</script>

<template>
  <v-form @submit.prevent="handleRegister">
    <v-text-field
      v-model="usuario.nombre"
      label="Nombre"
      prepend-inner-icon="mdi-account-outline"
      variant="outlined"
    ></v-text-field>

    <v-text-field
      v-model="usuario.apellidos"
      label="Apellidos"
      prepend-inner-icon="mdi-account-details-outline"
      variant="outlined"
    ></v-text-field>

    <v-text-field
      v-model="usuario.email"
      label="Correo Electrónico"
      type="email"
      variant="outlined"
    ></v-text-field>

    <v-text-field
      v-model="usuario.password"
      label="Contraseña"
      type="password"
      variant="outlined"
    ></v-text-field>

    <v-text-field
      v-model="usuario.confirmPassword"
      label="Confirmar Contraseña"
      type="password"
      variant="outlined"
    ></v-text-field>
    
    </v-form>
</template>