# ANÁLISIS ARQUITECTÓNICO - SPRINT 3
## Sistema de Gestión de Prácticas Empresariales CUE

**Fecha**: Junio 2, 2026  
**Rama**: `sprint3-santiago`  
**Enfoque**: Backend prioritario, Frontend en paralelo  

---

## 📊 ESTADO ACTUAL (Sprints 1-2)

### Entidades Implementadas
```
Usuario
├── rol: Enum(ADMIN_DTI, COORD_ACADEMICA, COORD_PRACTICAS, DOCENTE_ASESOR, TUTOR_EMPRESARIAL, ESTUDIANTE, DIRECCION)
├── estado: Enum(NO_APTO, APTO, EN_PRACTICA, PRACTICA_FINALIZADA)
├── primerIngreso: boolean
└── [relaciones] Expediente, BitacoraValidacion, etc.

Empresa
├── nit: unique
├── razonSocial
├── tutores: OneToMany[TutorEmpresarial]
└── vacantes: OneToMany[Vacante]

Vacante (Sprint 2)
├── empresa: ManyToOne
├── numero_practica: FK a PracticaCatalogo
├── cupo: int
├── estado: Enum(DISPONIBLE, PARCIALMENTE_CUBIERTA, CUBIERTA, CANCELADA)
└── tutorAsignado: ManyToOne[TutorEmpresarial]

Practica (Catálogo, Sprint 2)
├── programa: ManyToOne
├── numero_practica: int
├── tipo: Enum(EMPRESARIAL, PROYECTISTA)
└── duracion_semanas: int

Expediente
├── estudiante: OneToOne
├── documentosEntregados: String (lista comas)
└── [sin documentos binarios aún]

PracticaCatalogo
├── programa: ManyToOne
├── numero_practica: int
├── estado: Enum(BORRADOR, PUBLICADA, ARCHIVADA)
└── requisitos

TutorEmpresarial
├── usuario: OneToOne
├── empresa: ManyToOne
├── cargo: String
├── area: String
└── esResponsablePrincipal: boolean

BitacoraValidacion (Sprint 1)
├── estudiante: ManyToOne
├── estado: Enum(NO_APTO, APTO, RECHAZADO)
├── validador: ManyToOne[Usuario]
└── fechaHora + motivo

BitacoraAuditoria (Sprint 1)
├── usuario
├── tipo_accion: Enum
├── modulo: String
└── fechaHora
```

### Servicios Backend Existentes
- **UsuarioService**: CRUD + búsquedas
- **EmpresaService**: CRUD + validaciones
- **VacanteService**: CRUD, gestión de cupos
- **TutorEmpresarialService**: CRUD
- **BitacoraValidacionService**: Query solo (auditoría pasiva)
- **AuthService**: JWT, login
- **EmailService**: Envío de correos (Gmail SMTP)

### Controllers Existentes
- `UsuarioController`
- `EmpresaController`
- `VacanteController`
- `TutorEmpresarialController`
- `DashboardController` (básico, sin tarjetas de indicadores)

### Patterns Implementados
- **Repository**: Spring Data JPA
- **Service**: Business logic layer
- **DTO**: Separación request/response
- **AOP**: `ScopeValidationAspect` para validar roles/permisos
- **Audit**: Bitácora automática (interceptor en servicio)

---

## 🎯 SPRINT 3: FLUJO DE ASIGNACIÓN Y SEGUIMIENTO

### Línea Crítica del Negocio

```
FLUJO: Coordinador asigna → Documentos → Vinculación → EN_CURSO → Seguimiento

┌─────────────────────────────────────────────────────────────────┐
│ 1. ASIGNACIÓN (GPE-157)                                         │
│    Coordinador: vacante → estudiantes APTOS → confirma asignación
│    Output: Asignacion creada con estado "Asignada"              │
└─────────────────────────────────────────────────────────────────┘
                                ↓
┌─────────────────────────────────────────────────────────────────┐
│ 2. GESTIÓN ASIGNACIONES (GPE-158)                               │
│    Coordinador: listado de asignaciones → cancelar si no vinculó │
│    Output: Asignacion → "Cancelada" (si) o → "En vinculación"   │
└─────────────────────────────────────────────────────────────────┘
                                ↓
┌─────────────────────────────────────────────────────────────────┐
│ 3. DOCUMENTOS VINCULACIÓN (GPE-162, GPE-163)                    │
│    Coordinador: carga Carta Presentación + Convenio firmado     │
│    Output: Documentos en Expediente, Convenio pendiente firmas  │
└─────────────────────────────────────────────────────────────────┘
                                ↓
┌─────────────────────────────────────────────────────────────────┐
│ 4. CONFIRMACIÓN VINCULACIÓN (GPE-164)                           │
│    Coordinador: confirma 3 firmas + fecha inicio/fin            │
│    Output: Practica → EN_CURSO ⭐ (HITO CRÍTICO)                │
│    Trigger: Asigna Docente Asesor automáticamente               │
└─────────────────────────────────────────────────────────────────┘
                                ↓
┌─────────────────────────────────────────────────────────────────┐
│ 5. TABLERO SEGUIMIENTO (GPE-167)                                │
│    Todos los roles: ven prácticas EN_CURSO + estado seguimiento │
│    Prerrequisito: Plan de Práctica aprobado por Tutor Y Docente │
└─────────────────────────────────────────────────────────────────┘
                                ↓
┌─────────────────────────────────────────────────────────────────┐
│ 6. PLAN DE PRÁCTICA (Prereq para seguimiento)                   │
│    Estudiante: carga objetivos, cronograma, actividades         │
│    Tutor Empresarial: aprueba o rechaza                         │
│    Docente Asesor: aprueba o rechaza                            │
│    Output: Plan → AprobadoDocente (solo entonces inicia seguim) │
└─────────────────────────────────────────────────────────────────┘
                                ↓
┌─────────────────────────────────────────────────────────────────┐
│ 7. SEGUIMIENTO SEMANAL (GPE-170)                                │
│    Estudiante: cada semana carga actividades, logros, evidencias│
│    Docente Asesor: revisa, aprueba o rechaza                    │
│    Output: Seguimiento → APROBADO / RECHAZADO (editable solo    │
│            si RECHAZADO en semana actual)                       │
└─────────────────────────────────────────────────────────────────┘
                                ↓
┌─────────────────────────────────────────────────────────────────┐
│ 8. OBSERVACIONES DOCENTE (GPE-168)                              │
│    Docente: agrega observaciones a cada seguimiento             │
│    Retroalimentación visible para estudiante                    │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🏗️ ENTIDADES FALTANTES (Sprint 3)

Necesarias para completar el flujo:

### 1. **Asignacion** (NEW)
```java
@Entity
@Table(name = "asignaciones", indexes = {
    @Index(columnList = "estudiante_id"),
    @Index(columnList = "vacante_id"),
    @Index(columnList = "coordinador_id"),
    @Index(columnList = "estado")
})
class Asignacion {
    Long id
    Usuario estudiante (ManyToOne)
    Vacante vacante (ManyToOne)
    Usuario coordinador (ManyToOne) // quien asignó
    EstadoAsignacion estado: Enum(ASIGNADA, EN_VINCULACION, EN_CURSO, CANCELADA)
    String motivoCancelacion (null si no cancelada)
    LocalDateTime fechaAsignacion
    LocalDateTime fechaVinculacion (null inicialmente)
    LocalDateTime fechaInicio (null inicialmente)
    LocalDateTime fechaFin (null inicialmente)
    // Bitácora de cambios de estado
    List<CambioEstadoAsignacion> cambiosEstado
}
```

### 2. **CambioEstadoAsignacion** (NEW)
```java
@Entity
class CambioEstadoAsignacion {
    Long id
    Asignacion asignacion (ManyToOne)
    EstadoAsignacion estadoAnterior
    EstadoAsignacion estadoNuevo
    String motivo (nullable)
    Usuario usuario (ManyToOne)
    LocalDateTime fechaHora
}
```

### 3. **Practica** (NEW - instancia real)
```java
@Entity
@Table(name = "practicas", indexes = {
    @Index(columnList = "estudiante_id", unique = true),
    @Index(columnList = "asignacion_id"),
    @Index(columnList = "docente_asesor_id"),
    @Index(columnList = "estado")
})
class Practica {
    Long id
    Usuario estudiante (OneToOne, ManyToOne)
    Asignacion asignacion (OneToOne)
    PracticaCatalogo practica_catalogo (ManyToOne)
    Empresa empresa (ManyToOne)
    TutorEmpresarial tutorEmpresarial (ManyToOne)
    Usuario docenteAsesor (ManyToOne, asignado al confirmar vinculación)
    EstadoPractica estado: Enum(BORRADOR, EN_VINCULACION, EN_CURSO, FINALIZADA, CANCELADA)
    LocalDateTime fechaInicio
    LocalDateTime fechaFin
    // Referencias a documentos y planes
    PlanPractica planPractica (OneToOne)
    List<DocumentoPractica> documentos
    List<SeguimientoPractica> seguimientos
    LocalDateTime creadoEn
    LocalDateTime actualizadoEn
}
```

### 4. **PlanPractica** (NEW)
```java
@Entity
@Table(name = "planes_practica", indexes = {
    @Index(columnList = "practica_id", unique = true)
})
class PlanPractica {
    Long id
    Practica practica (OneToOne)
    List<ObjetivoPlan> objetivos
    List<CronogramaPlan> cronograma
    EstadoPlan estado: Enum(BORRADOR, APROBADO_TUTOR, APROBADO_DOCENTE, RECHAZADO)
    Usuario cargadoPor (estudiante)
    // Aprobaciones
    LocalDateTime fechaAprobacionTutor (null inicialmente)
    LocalDateTime fechaAprobacionDocente (null inicialmente)
    String motivoRechazo (si estado = RECHAZADO)
    Usuario usuarioRechazo
    LocalDateTime creadoEn
    LocalDateTime actualizadoEn
}
```

### 5. **SeguimientoPractica** (NEW)
```java
@Entity
@Table(name = "seguimientos_practica", indexes = {
    @Index(columnList = "practica_id"),
    @Index(columnList = "semana"),
    @Index(columnList = "estado")
})
class SeguimientoPractica {
    Long id
    Practica practica (ManyToOne)
    Integer semana (1..26 típicamente)
    String actividades (requerido)
    String logros (requerido)
    String dificultades
    List<DocumentoEvidencia> evidencias
    String observacionesDocente (agregadas por Docente Asesor)
    EstadoSeguimiento estado: Enum(PENDIENTE, APROBADO, RECHAZADO)
    Usuario cargadoPor (estudiante)
    LocalDateTime fechaCarga
    LocalDateTime fechaRevision
    Usuario usuarioRevision (Docente Asesor)
    LocalDateTime creadoEn
    LocalDateTime actualizadoEn
}
```

### 6. **DocumentoPractica** (NEW)
```java
@Entity
@Table(name = "documentos_practica", indexes = {
    @Index(columnList = "practica_id")
})
class DocumentoPractica {
    Long id
    Practica practica (ManyToOne)
    TipoDocumento tipo: Enum(CARTA_PRESENTACION, CONVENIO, PLAN, OTRO)
    String urlArchivo
    String nombreArchivo
    String mimeType
    Long tamanho
    Integer numPaginas
    // Firmas digitales/confirmaciones
    List<FirmaDocumento> firmas
    boolean esMutable (false para FINALIZADA/CANCELADA)
    LocalDateTime creadoEn
    LocalDateTime actualizadoEn
}
```

### 7. **FirmaDocumento** (NEW - para convenio)
```java
@Entity
@Table(name = "firmas_documento")
class FirmaDocumento {
    Long id
    DocumentoPractica documento (ManyToOne)
    TipoFirmante tipo: Enum(COORDINADOR, TUTOR_EMPRESARIAL, ESTUDIANTE)
    Usuario usuario (ManyToOne)
    LocalDateTime fechaFirma (null si no firmado)
    String hashValidacion
}
```

### 8. **ConfiguracionPrograma** (NEW)
```java
@Entity
@Table(name = "configuraciones_programa")
class ConfiguracionPrograma {
    Long id
    Programa programa (OneToOne)
    Integer diasInactividadAlerta (default: 7)
    Boolean notificacionesAutomaticas (default: true)
    String plantillaCorreoAsignacion
    String plantillaCorreoSeguimiento
}
```

### 9. **NotificacionHistorial** (NEW)
```java
@Entity
@Table(name = "notificaciones_historial", indexes = {
    @Index(columnList = "usuario_id"),
    @Index(columnList = "tipo"),
    @Index(columnList = "estado")
})
class NotificacionHistorial {
    Long id
    Usuario usuarioDestino (ManyToOne)
    TipoNotificacion tipo: Enum(ASIGNACION, CANCELACION, VINCULACION, SEGUIMIENTO, PLAN_APROBACION, etc)
    String correoDestino
    String asunto
    String cuerpo
    EstadoNotificacion estado: Enum(PENDIENTE, ENVIADO, FALLIDO)
    Integer reintentos (default: 0)
    LocalDateTime proxReintento
    String errorMensaje
    Asignacion asignacion (nullable - FK a contexto)
    LocalDateTime creadoEn
}
```

---

## 🎬 ORDEN DE IMPLEMENTACIÓN (Backend Priority)

### **BLOQUE 1: Asignación (GPE-157, GPE-158, GPE-159)**
1. ✅ Crear entidades: `Asignacion`, `CambioEstadoAsignacion`
2. ✅ Crear repositories
3. ✅ Implementar `AsignacionService` con métodos:
   - `crearAsignacion(estudianteId, vacanteId, coordinadorId)`
   - `cancelarAsignacion(asignacionId, motivo)`
   - `obtenerAsignacionesActivas(coordinadorId)`
   - `cambiarEstado(asignacionId, nuevoEstado)` → genera `CambioEstadoAsignacion`
4. ✅ Crear `AsignacionController` REST
5. ✅ Tests (repositories, service básico)

### **BLOQUE 2: Notificaciones (GPE-160)**
1. ✅ Crear entidad: `NotificacionHistorial`
2. ✅ Implementar `NotificacionService` con métodos:
   - `enviarNotificacion(usuario, tipo, cuerpo)` → registra en BD + encola email
   - `procesarColaNotificaciones()` → async
   - `registrarReintento()` → reintentos automáticos
3. ✅ Integrar en `AsignacionService` (trigger en cada cambio de estado)
4. ✅ Tests

### **BLOQUE 3: Documentos Vinculación (GPE-162, GPE-163)**
1. ✅ Crear entidades: `DocumentoPractica`, `FirmaDocumento`
2. ✅ Implementar `DocumentoPracticaService`:
   - `cargarCartaP

resentacion(practicaId, archivo)`
   - `cargarConvenio(practicaId, archivo)`
   - `confirmarFirma(documentoId, usuario)`
   - `validarFirmasCompletas(documentoId)` → true cuando Coord + Tutor + Est
3. ✅ Integrar con `NotificacionService` (notificar cuando nuevo documento)

### **BLOQUE 4: Confirmación Vinculación (GPE-164)**
1. ✅ Crear entidad: `Practica`
2. ✅ Implementar `PracticaService`:
   - `confirmarVinculacion(asignacionId, fechaInicio, fechaFin)` → crea Practica EN_CURSO
   - Automático: asigna Docente Asesor (lógica TBD - ¿round-robin? ¿especialidad?)
3. ✅ Transacción atomica: Asignacion → Practica → notificaciones

### **BLOQUE 5: Plan de Práctica (Pre-requisito para Seguimiento)**
1. ✅ Crear entidades: `PlanPractica`, `ObjetivoPlan`, `CronogramaPlan`
2. ✅ Implementar `PlanPracticaService`:
   - `crearPlan(practicaId, objetivos, cronograma)` → BORRADOR
   - `aprobarPorTutor(planId, tutorId)` → APROBADO_TUTOR
   - `aprobarPorDocente(planId, docenteId)` → APROBADO_DOCENTE
   - `rechazarPlan(planId, motivo)` → RECHAZADO
3. ✅ Validación: no puede iniciarse seguimiento sin APROBADO_DOCENTE

### **BLOQUE 6: Seguimiento Semanal (GPE-170)**
1. ✅ Crear entidades: `SeguimientoPractica`, `DocumentoEvidencia`
2. ✅ Implementar `SeguimientoPracticaService`:
   - `crearSeguimiento(practicaId, semana, actividades, logros, evidencias)` → PENDIENTE
   - `editarSeguimiento(seguimientoId, ...)` → solo si es RECHAZADO Y última semana
   - `obtenerSeguimientoPorSemana(practicaId, semana)`
3. ✅ Validación: `PlanPractica.estado == APROBADO_DOCENTE`

### **BLOQUE 7: Observaciones Docente (GPE-168)**
1. ✅ Agregar método a `SeguimientoPracticaService`:
   - `agregarObservacionesDocente(seguimientoId, observaciones, estado)`
   - `obtenerObservacionesDocente(seguimientoId)`
2. ✅ Permiso: solo Docente Asesor puede

### **BLOQUE 8: Dashboard Indicadores (GPE-132)**
1. ✅ Implementar métodos en servicios para contar:
   - DTI: usuarios activos por rol, estudiantes NO_APTO, APTO
   - Coord Acad: estudiantes NO_APTO pendientes, APTO a enviar
   - Coord Prácticas: APTOS disponibles, vacantes, prácticas EN_CURSO, planes pendientes
   - Docente: estudiantes asignados, seguimientos pendientes
   - Tutor: practicantes a cargo, planes pendientes
   - Dirección: prácticas EN_CURSO por programa, tasa aprobación
   - Estudiante: estado práctica, semana actual, docs pendientes
2. ✅ Crear `DashboardService` con métodos por rol
3. ✅ Actualizar `DashboardController` con endpoints

### **BLOQUE 9: Configuraciones (GPE-167 - alertas inactividad)**
1. ✅ Crear entidad: `ConfiguracionPrograma`
2. ✅ Implementar lógica de alertas:
   - Task scheduled: cada hora chequea `SeguimientoPractica.fechaCarga`
   - Si `ahora - últimaCarga > diasInactividadAlerta` → marca como ALERTA
   - Notifica a Docente Asesor
3. ✅ Hacer configurable en `ConfiguracionPrograma`

---

## 🔄 REPOSITORIOS NECESARIOS

```java
AsignacionRepository extends JpaRepository<Asignacion, Long> {
    Page<Asignacion> findByCoordinador_IdAndEstado(Long coordinadorId, EstadoAsignacion estado, Pageable p);
    Page<Asignacion> findByEstudiante_Id(Long estudianteId, Pageable p);
    Page<Asignacion> findByVacante_Id(Long vacanteId, Pageable p);
    boolean existsByEstudiante_IdAndEstadoIn(Long estudiId, List<EstadoAsignacion> estados);
}

CambioEstadoAsignacionRepository extends JpaRepository<...> {
    List<CambioEstadoAsignacion> findByAsignacion_IdOrderByFechaHoraDesc(Long asignacionId);
}

PracticaRepository extends JpaRepository<Practica, Long> {
    Optional<Practica> findByEstudiante_Id(Long estudianteId);
    Page<Practica> findByEstadoAndDocente_Id(EstadoPractica estado, Long docenteId, Pageable p);
    List<Practica> findByEmpresa_IdAndEstado(Long empresaId, EstadoPractica estado);
}

SeguimientoPracticaRepository extends JpaRepository<...> {
    Optional<SeguimientoPractica> findByPractica_IdAndSemana(Long practicaId, Integer semana);
    List<SeguimientoPractica> findByPractica_IdOrderBySemanaAsc(Long practicaId);
    Page<SeguimientoPractica> findByDocente_IdAndEstado(Long docenteId, EstadoSeguimiento estado, Pageable p);
}

PlanPracticaRepository extends JpaRepository<...> {
    Optional<PlanPractica> findByPractica_Id(Long practicaId);
}

DocumentoPracticaRepository extends JpaRepository<...> {
    List<DocumentoPractica> findByPractica_IdAndTipo(Long practicaId, TipoDocumento tipo);
    boolean existsByPractica_IdAndTipoAndEsMutableTrue(Long practicaId, TipoDocumento tipo);
}

FirmaDocumentoRepository extends JpaRepository<...> {
    Optional<FirmaDocumento> findByDocumento_IdAndTipo(Long documentoId, TipoFirmante tipo);
    long countByDocumento_IdAndFechaFirmaNotNull(Long documentoId);
}

NotificacionHistorialRepository extends JpaRepository<...> {
    Page<NotificacionHistorial> findByUsuarioDestino_IdOrderByCreatedEnDesc(Long usuarioId, Pageable p);
    List<NotificacionHistorial> findByEstadoAndProxReintentoLessThan(EstadoNotificacion estado, LocalDateTime ahora);
}

ConfiguracionProgramaRepository extends JpaRepository<...> {
    Optional<ConfiguracionPrograma> findByPrograma_Id(Long programaId);
}
```

---

## 📋 ENUMS REQUERIDOS

```java
enum EstadoAsignacion {
    ASIGNADA,          // acaba de asignarse
    EN_VINCULACION,    // Coord cargó docs, esperando confirmación
    EN_CURSO,          // confirmada, practica activa
    CANCELADA          // cancelada por Coord antes de vinculación
}

enum EstadoPractica {
    BORRADOR,              // creada pero no confirmada
    EN_VINCULACION,        // entre asignación y confirmación
    EN_CURSO,              // confirmada, activa
    FINALIZADA,            // completada
    CANCELADA              // cancelada
}

enum EstadoPlan {
    BORRADOR,              // creado por estudiante
    APROBADO_TUTOR,        // aprobado por Tutor Empresarial
    APROBADO_DOCENTE,      // aprobado por Docente Asesor (DESBLOQUEADOR)
    RECHAZADO              // rechazado, puede volver a BORRADOR
}

enum EstadoSeguimiento {
    PENDIENTE,             // esperando revisión
    APROBADO,              // Docente lo aprobó
    RECHAZADO              // Docente lo rechazó, estudiante debe editar
}

enum TipoDocumento {
    CARTA_PRESENTACION,
    CONVENIO,
    PLAN,
    EVIDENCIA,
    OTRO
}

enum TipoFirmante {
    COORDINADOR,
    TUTOR_EMPRESARIAL,
    ESTUDIANTE
}

enum TipoNotificacion {
    ASIGNACION_CREADA,
    ASIGNACION_CANCELADA,
    VINCULACION_CONFIRMADA,
    PLAN_APROBADO,
    PLAN_RECHAZADO,
    SEGUIMIENTO_RECHAZADO,
    ALERTA_INACTIVIDAD,
    OTRO
}

enum EstadoNotificacion {
    PENDIENTE,
    ENVIADO,
    FALLIDO
}
```

---

## 🔐 MATRIX DE PERMISOS

| Función | Usuario | Asignación | Plan | Seguimiento | Docs |
|---------|---------|-----------|------|-------------|------|
| Crear Asignación | Coord Prácticas | ✅ | - | - | - |
| Cancelar Asignación | Coord Prácticas | ✅ | - | - | - |
| Cargar Documentos | Coord Prácticas | - | - | - | ✅ |
| Confirmar Firmas | Coord/Tutor/Est | - | - | - | ✅ |
| Confirmar Vinculación | Coord Prácticas | - | - | - | - |
| Crear Plan | Estudiante | - | ✅ | - | - |
| Aprobar Plan | Tutor/Docente | - | ✅ | - | - |
| Cargar Seguimiento | Estudiante | - | - | ✅ | - |
| Revisar Seguimiento | Docente Asesor | - | - | ✅ | - |
| Ver Tablero | Todos (filtrado) | - | - | ✅ | - |

---

## 🛠️ CONFIGURACIÓN TÉCNICA

- **DB**: MySQL (ya en proyecto)
- **ORM**: JPA/Hibernate
- **Async**: `@Async` para notificaciones (no bloquear API)
- **Scheduler**: `@Scheduled` para alertas inactividad
- **Transacciones**: `@Transactional` en métodos críticos
- **Auditoría**: Aprovechar `BitacoraAuditoria` existente
- **Validaciones**: JSR-303 (`@NotNull`, `@Email`, etc)
- **Paginación**: `Pageable` en queries grandes

---

## 📦 ESTRUCTURA DE CARPETAS PROPUESTA

```
backend/src/main/java/co/edu/cue/practicas/
├── model/
│   ├── entity/
│   │   ├── Asignacion.java           (NEW)
│   │   ├── CambioEstadoAsignacion.java (NEW)
│   │   ├── Practica.java             (NEW)
│   │   ├── PlanPractica.java         (NEW)
│   │   ├── SeguimientoPractica.java  (NEW)
│   │   ├── DocumentoPractica.java    (NEW)
│   │   ├── FirmaDocumento.java       (NEW)
│   │   ├── ConfiguracionPrograma.java (NEW)
│   │   ├── NotificacionHistorial.java (NEW)
│   │   └── [resto de entidades]
│   ├── enums/
│   │   ├── EstadoAsignacion.java     (NEW)
│   │   ├── EstadoPractica.java       (NEW)
│   │   ├── EstadoPlan.java           (NEW)
│   │   ├── EstadoSeguimiento.java    (NEW)
│   │   ├── TipoDocumento.java        (NEW)
│   │   ├── TipoFirmante.java         (NEW)
│   │   ├── TipoNotificacion.java     (NEW)
│   │   └── EstadoNotificacion.java   (NEW)
│   └── dto/
│       ├── request/
│       │   ├── CrearAsignacionRequest (NEW)
│       │   ├── CrearPlanRequest      (NEW)
│       │   └── ...
│       └── response/
│           ├── AsignacionResponse    (NEW)
│           ├── PracticaResponse      (NEW)
│           └── ...
├── repository/
│   ├── asignacion/
│   │   ├── AsignacionRepository.java (NEW)
│   │   └── CambioEstadoAsignacionRepository.java (NEW)
│   ├── practica/
│   │   ├── PracticaRepository.java (NEW)
│   │   ├── PlanPracticaRepository.java (NEW)
│   │   ├── SeguimientoPracticaRepository.java (NEW)
│   │   ├── DocumentoPracticaRepository.java (NEW)
│   │   └── FirmaDocumentoRepository.java (NEW)
│   ├── configuracion/
│   │   └── ConfiguracionProgramaRepository.java (NEW)
│   ├── notificacion/
│   │   └── NotificacionHistorialRepository.java (NEW)
│   └── [repos existentes]
├── service/
│   ├── asignacion/
│   │   ├── AsignacionService.java (NEW)
│   │   └── AsignacionServiceImpl.java (NEW)
│   ├── practica/
│   │   ├── PracticaService.java (NEW)
│   │   ├── PlanPracticaService.java (NEW)
│   │   ├── SeguimientoPracticaService.java (NEW)
│   │   ├── DocumentoPracticaService.java (NEW)
│   │   └── [Impl clases]
│   ├── notificacion/
│   │   ├── NotificacionService.java (NEW)
│   │   └── NotificacionServiceImpl.java (NEW)
│   ├── dashboard/
│   │   ├── DashboardService.java (actualizar)
│   │   └── DashboardServiceImpl.java (actualizar)
│   └── [servicios existentes]
├── controller/
│   ├── AsignacionController.java (NEW)
│   ├── PracticaController.java (NEW)
│   ├── SeguimientoPracticaController.java (NEW)
│   ├── DashboardController.java (actualizar)
│   └── [controllers existentes]
├── security/
│   ├── filter/
│   │   └── ScopeValidationAspect.java (actualizar permisos)
│   └── [security existente]
├── exception/
│   ├── AsignacionException.java (NEW)
│   ├── PracticaException.java (NEW)
│   └── [excepciones existentes]
├── task/
│   └── AlertasInactividadTask.java (NEW)
└── util/
    └── [utilidades]
```

---

## ✅ PRÓXIMOS PASOS

1. **Crear Enums** → file generation
2. **Crear Entidades** → JPA entities con validaciones
3. **Crear Repositories** → Spring Data JPA
4. **Implementar BLOQUE 1** (Asignación)
5. **Implementar BLOQUE 2** (Notificaciones)
6. **... continuar por bloques**
7. **Tests unitarios** (cuando user diga OK)
8. **Frontend** (en paralelo, basado en APIs del backend)

---

**Rama**: `sprint3-santiago`  
**Estado**: Ready para BEGIN DEVELOPMENT  
**Autor**: GitHub Copilot + Santiago (Team)  

