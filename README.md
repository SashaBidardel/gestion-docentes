# 🎓 Sistema de Gestión de Docentes y Guardias

Este repositorio contiene la solución integral para la gestión de centros educativos, permitiendo la administración de horarios, el registro de ausencias y la asignación automatizada de guardias. 

El proyecto utiliza una arquitectura de **microservicios** desacoplada, con un backend robusto en Java y una interfaz de usuario reactiva en Vue.js.

---

## 📂 Estructura del Repositorio

El proyecto se organiza en dos módulos principales:

* **[/backend](./backend):** API REST desarrollada con **Java 21** y **Spring Boot 3**. Gestiona la lógica de negocio, seguridad mediante JWT y persistencia en base de datos MySQL.
* **[/frontend](./frontend):** Aplicación SPA (Single Page Application) desarrollada con **Vue.js 3** y **Vuetify 3**. Interfaz profesional basada en Material Design 3.

---

## 🛠️ Stack Tecnológico

### Backend (Servidor)
* **Lenguaje:** Java 21 (LTS).
* **Framework:** Spring Boot 3.x.
* **Seguridad:** Spring Security + JSON Web Tokens (JWT).
* **Base de Datos:** MySQL 8.0.
* **Mapeo de Datos:** ModelMapper (DTOs).

### Frontend (Cliente)
* **Framework:** Vue.js 3 (Composition API).
* **UI Library:** Vuetify 3.
* **Herramientas:** Vite, Axios, Material Design Icons.

### Infraestructura
* **Contenedorización:** Docker & Docker Compose.
* **Servidor Web:** Nginx (Alpine Linux).
* **Despliegue:** VPS Ubuntu 24.04 LTS (IONOS).

---

## 🚀 Instalación y Despliegue

1. **Clonar el repositorio:**
   ```bash
   git clone [https://github.com/SashaBidardel/gestion-docentes.git](https://github.com/SashaBidardel/gestion-docentes.git)
   cd gestion-docentes