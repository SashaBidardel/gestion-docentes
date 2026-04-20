# 📚 Gestión de Docentes  
Aplicación desarrollada en **Spring Boot** para automatizar diferentes procesos relacionados con la organización del profesorado en un centro educativo.

El sistema gestiona principalmente dos áreas:
-  **Gestión de guardias**
-  **Gestión de asuntos propios**

Este proyecto está diseñado siguiendo las buenas prácticas de Spring, arquitectura por capas (controladores, servicios, repositorios, entidades, DTOs) y probado mediante **Postman**.

---

# Funcionalidades principales

## 1️⃣ Gestión de guardias
La aplicación permitirá:

### ✔ Cargar y modificar horarios del profesorado  
A partir de datos iniciales importados desde varios CSV al inicio del curso.

### ✔ Registrar ausencias del profesorado  
Para que el sistema pueda generar el cuadrante de guardias.

### ✔ Asignar guardias automáticamente según el algoritmo oficial:
1. El docente sustituto debe pertenecer al **mismo departamento** que la persona ausente.  
   - Si hay varios → se elige el que lleve **menos guardias realizadas**.
2. El docente sustituto debe impartir clase en el **mismo grupo**.  
   - Si hay varios → se elige el que lleve **menos guardias realizadas**.
3. Si no aplica ninguno de los casos anteriores → se asigna el que tenga **menos guardias realizadas** en total.

### ✔ Cuadrante de guardias diario
- Consulta a través de la web.
- Con opción de **imprimir**.
- Anotación de:
  - Si la guardia se ha realizado.
  - Motivo de no realización.
  - Información extra (trabajo en jefatura, incidencias, etc.).

---

## 2️⃣ Gestión de días de asuntos propios
La asignación se realiza según las normas oficiales:

### ✔ Orden de prioridad:
1. Tipo de funcionario: **carrera → prácticas → interino**  
2. Antigüedad en el centro  
3. Nota de oposición / posición en lista  

### ✔ Límite de docentes por día  
Configuración establecida al inicio del curso.

### ✔ Flujo del proceso:
- El docente solicita un día → queda en **pendiente_validar**  
- Jefatura valida o rechaza  
- Si se acepta → se envía email automático  
- El docente podrá subir material para cubrir su guardia

---

#  Carga inicial de datos
Al iniciar el curso se cargan varios **CSV**:
- Datos del profesorado
- Horarios
- Departamentos
- Otros datos necesarios

El sistema enviará automáticamente un email con:
- Nombre de usuario (código profesor)
- Contraseña temporal

---

# Arquitectura del proyecto
El proyecto sigue una estructura profesional basada en capas:

Incluye:
- Entidades
- Repositorios
- Servicios
- Controladores
- DTO de una entidad
- DTO combinado de varias entidades
- Validaciones del sistema
- Servicios orientados a casos de uso (UC2, UC5, UC6…)

---

#  Endpoints (test con Postman)
Se utilizara Postaman para testear los endpoints
