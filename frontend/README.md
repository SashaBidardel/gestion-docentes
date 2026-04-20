# 🖥️ Gestión Docente - Frontend (Vue.js)

Este módulo contiene la interfaz de usuario del sistema de gestión de docentes y guardias. Se ha desarrollado como una **Single Page Application (SPA)** moderna, reactiva y optimizada utilizando el ecosistema de **Vue 3** y **Vuetify**.

---

## 🚀 Comandos de Ejecución

Dentro de la carpeta `frontend`, puedes utilizar los siguientes comandos de NPM:

### 1. Instalación de dependencias

Prepara el entorno instalando todas las librerías necesarias (incluyendo Vuetify, Axios y Vite):

```sh
npm install
### 2. Ejecución en Desarrollo (Hot-Reload)
Inicia el servidor local de Vite con refresco automático:

```bash
npm run dev
### 3. Compilación para Producción (Build)
Genera una versión optimizada y minificada en la carpeta `/dist`:

```bash
npm run build
## 📂 Funcionalidades Principales

* **Autenticación Segura (JWT):** Sistema de login que gestiona tokens de sesión de forma persistente y protege las rutas privadas.
* **Gestión Integral de Docentes (CRUD):** Interfaz completa para el alta, baja, modificación y consulta de profesores con validaciones en tiempo real.
* **Carga Masiva mediante CSV:** Herramienta específica para el administrador que permite importar horarios y datos de personal de forma masiva.
* **Gestión de Ausencias y Guardias:** Panel interactivo para la solicitud de días propios por parte de los docentes y la gestión de sustituciones por el administrador.
* **Componentes Material Design 3:** Uso de tablas dinámicas, formularios reactivos y diálogos modales mediante **Vuetify 3**.
## 💻 Entorno de Desarrollo Recomendado

Para una experiencia de desarrollo profesional y evitar errores de sintaxis, se recomienda:

* **IDE:** Visual Studio Code
* **Extensión Imprescindible:** `Vue - Official` (Volar)
* **Depuración:** `Vue.js devtools` (disponible para Chrome y Firefox) para inspeccionar el estado de los componentes.

---

## 🔗 Conexión con el Backend

La aplicación se comunica con el servidor **Spring Boot** (puerto `8080`) utilizando **Axios**.

* **Seguridad:** Los interceptores de Axios están configurados para adjuntar automáticamente el token `Bearer` en las cabeceras de cada petición tras el inicio de sesión.
* **Consumo de API:** Todas las llamadas a los endpoints están centralizadas para facilitar el mantenimiento y la gestión de errores.