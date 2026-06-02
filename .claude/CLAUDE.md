# Sistema de Gestión de Prácticas Empresariales CUE

## Propósito

Plataforma web para gestionar prácticas empresariales de estudiantes de ingeniería. Permite a múltiples actores (DTI, Dirección, Coordinación Académica, Coordinador de Práctica, Empresas, Tutores, Docentes, Estudiantes) gestionar vacantes, asignaciones, evaluaciones y seguimiento de prácticas con control de acceso basado en roles.

## Stack

- **Backend**: Spring Boot 3.x + Java 17+ (Maven)
- **Frontend**: React 18+ + TypeScript + Tailwind CSS + Vite
- **Database**: PostgreSQL o MySQL (perfiles Maven configurados)
- **Auth**: JWT + Spring Security
- **Testing**: JUnit 5 + Mockito (backend), Jest/Vitest (frontend)

## Branching & Git Workflow

- **Main branch**: `main` (producción)
- **Working branches**: Crear una rama nueva para cada feature/fix (NO trabajar directamente en main)
- **Commit format**: Usar skill `/commit` para commits con convenciones Sentry
  - Commits breves y descriptivos
  - Mensajes en imperative form: "Add...", "Fix...", "Update..."
- **PR workflow**: Al terminar una rama
  1. `git push` con `-u` flag
  2. Crear PR desde GitHub con título y descripción detallada
  3. Incluir todos los cambios realizados en la rama

## Project Structure

```
ProyectoNuclear/
├── PracticasEmpresariales/
│   ├── backend/          # Spring Boot application
│   │   ├── src/main/java/co/edu/cue/practicas/
│   │   ├── src/test/      # Tests unitarios
│   │   └── pom.xml        # Maven (perfiles: postgres, mysql)
│   ├── frontend/          # React + Vite
│   │   ├── src/
│   │   ├── src/pages/     # Auth, dashboard
│   │   ├── src/services/  # API calls
│   │   └── vite.config.ts
│   └── docs/              # Documentación del producto
├── docs/                  # Requisitos funcionales, diseño
└── .claude/               # Configuración de Claude Code
```

## Key Features & Modules

### MÓDULO 01: Dashboard & Panel de Inicio
- Panel personalizado por rol (DTI, Dirección, Coordinación, Empresas, Tutores, Docentes, Estudiantes)
- Tarjetas con indicadores clave dinámicos
- Centro de notificaciones con alertas contextuales
- Filtros globales persistentes por facultad/programa

### MÓDULO 02: Gestión de Usuarios & Acceso
- Registro y gestión de usuarios por rol (DTI admin)
- Creación de usuarios por CSV
- Permisos basados en roles y scopes
- Bitácora de auditoría

### Otros módulos
- Gestión de vacantes (empresas)
- Asignación de estudiantes a prácticas
- Evaluaciones y calificaciones
- Cierre de prácticas

## Database

- **Profiles**: 
  - PostgreSQL (default): `mvn spring-boot:run -P postgres`
  - MySQL: `mvn spring-boot:run -P mysql` y cambiar `spring.profiles.active=mysql`
- **Schema**: Versioned con Liquibase o Flyway
- **Auditoría**: Tabla `bitacora_auditoria` para todas las operaciones

## Documentation

- `docs/REQUISITOS_FUNCIONALES.md` — Especificación funcional detallada (RF-01 a RF-0X)
- `docs/REQUISITOS_FUNCIONALES_F.md` — Variante detallada de requisitos
- `docs/REQUISITOS_FUNCIONALES_SGPE.md` — Especificación completa del sistema
- Todo en Markdown, actualizar cuando cambien requisitos

## Development Workflow

### Backend
```bash
cd PracticasEmpresariales/backend
mvn spring-boot:run -P postgres   # Con PostgreSQL
mvn clean test                     # Ejecutar tests
mvn compile                        # Compilar
```

### Frontend
```bash
cd PracticasEmpresariales/frontend
npm install
npm run dev                        # Desarrollo (Vite dev server)
npm run build                      # Build producción
npm run test                       # Tests
```

### Running Full Stack
1. Backend runs on `http://localhost:8080` (or configured port)
2. Frontend runs on `http://localhost:5173` (Vite default)
3. Frontend makes API calls to backend on CORS-configured domain

## Key Conventions

- **Java/Backend**: Spring Boot conventions (Controllers, Services, Repositories)
- **React/Frontend**: Functional components, TypeScript, context for auth state
- **Branches**: feature/*, fix/*, docs/*, refactor/* (descriptive names)
- **No DB migrations yet**: Schema changes are manual or liquibase-based (verify current approach)

## Deployment

- **Status**: Not deployed yet
- **Options being considered**: Railway, DigitalOcean, AWS/GCP
- **Decision pending**: Will update CLAUDE.md once decided

## CI/CD

- **Status**: Not configured yet
- **Planned**: GitHub Actions for tests, builds, automated deployment

## Team & Contact

- **Project**: Universidad Alexander Von Humboldt, Programa de Ingeniería de Software
- **Faculty Advisor**: Ing. Diana María Valencia
- **Lead Contributors**: Santiago Acosta Calvo, Jeshua Gomez Cortes, María José Realpe Vallejo
