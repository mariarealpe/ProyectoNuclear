# SPRINT 3 - HOJA DE RUTA DE DESARROLLO

**Rama**: `sprint3-santiago`  
**Estado**: Entity layer completado (17 archivos nuevos + 1 documento arquitectónico)  
**Commit Base**: `2fb8637`  

---

## ✅ COMPLETADO - FASE 1: MODELO DE DATOS

### Entidades Creadas (10 nuevas)
- ✅ `Asignacion` - Asignación estudiante a vacante
- ✅ `CambioEstadoAsignacion` - Bitácora de cambios
- ✅ `Practica` - Instancia real de práctica
- ✅ `PlanPractica` - Plan estructurado de estudiante
- ✅ `SeguimientoPractica` - Reportes semanales
- ✅ `DocumentoPractica` - Almacén de documentos
- ✅ `FirmaDocumento` - Firmas digitales (convenio)
- ✅ `DocumentoEvidencia` - Archivos adjuntos en seguimientos
- ✅ `NotificacionHistorial` - Log de correos enviados
- ✅ `ConfiguracionPrograma` - Settings por programa

### Enums Creados (7 nuevos)
- ✅ `EstadoAsignacion` → ASIGNADA, EN_VINCULACION, EN_CURSO, CANCELADA
- ✅ `EstadoPlan` → BORRADOR, APROBADO_TUTOR, APROBADO_DOCENTE, RECHAZADO
- ✅ `EstadoSeguimiento` → PENDIENTE, APROBADO, RECHAZADO
- ✅ `TipoDocumento` → CARTA_PRESENTACION, CONVENIO, PLAN, EVIDENCIA, OTRO
- ✅ `TipoFirmante` → COORDINADOR, TUTOR_EMPRESARIAL, ESTUDIANTE
- ✅ `TipoNotificacion` → ASIGNACION_CREADA, PLAN_APROBADO, ALERTA_INACTIVIDAD, etc
- ✅ `EstadoNotificacion` → PENDIENTE, ENVIADO, FALLIDO

---

## 🚀 PRÓXIMOS PASOS - FASE 2: REPOSITORIES (2-3 horas)

### Crear en `repository/` subdirectorios:

```
repository/
├── asignacion/
│   ├── AsignacionRepository.java
│   └── CambioEstadoAsignacionRepository.java
├── practica/
│   ├── PracticaRepository.java
│   ├── PlanPracticaRepository.java
│   ├── SeguimientoPracticaRepository.java
│   ├── DocumentoPracticaRepository.java
│   └── FirmaDocumentoRepository.java
├── notificacion/
│   └── NotificacionHistorialRepository.java
├── configuracion/
│   └── ConfiguracionProgramaRepository.java
└── evidencia/
    └── DocumentoEvidenciaRepository.java
```

**Métodos por repository**:

```java
// AsignacionRepository
findByCoordinador_IdAndEstado(Long coordinadorId, EstadoAsignacion estado, Pageable p) → Page
findByEstudiante_Id(Long estudianteId, Pageable p) → Page
findByVacante_IdAndEstado(Long vacanteId, EstadoAsignacion estado) → List
existsByEstudiante_IdAndEstadoIn(Long estudianteId, List<EstadoAsignacion> estados) → boolean

// PracticaRepository
findByEstudiante_Id(Long estudianteId) → Optional
findByEstadoAndDocente_Id(EstadoPractica estado, Long docenteId, Pageable p) → Page
findByEmpresa_IdAndEstado(Long empresaId, EstadoPractica estado) → List

// SeguimientoPracticaRepository
findByPractica_IdAndSemana(Long practicaId, Integer semana) → Optional
findByPractica_IdOrderBySemanaAsc(Long practicaId) → List
findByDocente_IdAndEstado(Long docenteId, EstadoSeguimiento estado, Pageable p) → Page
findByPractica_IdAndEstadoIn(Long practicaId, List<EstadoSeguimiento> estados) → List

// PlanPracticaRepository
findByPractica_Id(Long practicaId) → Optional

// DocumentoPracticaRepository
findByPractica_IdAndTipo(Long practicaId, TipoDocumento tipo) → List
findByPractica_Id(Long practicaId) → List

// FirmaDocumentoRepository
findByDocumento_IdAndTipo(Long documentoId, TipoFirmante tipo) → Optional
countByDocumento_IdAndFechaFirmaNotNull(Long documentoId) → long

// NotificacionHistorialRepository
findByUsuarioDestino_IdOrderByCreatedEnDesc(Long usuarioId, Pageable p) → Page
findByEstadoAndProxReintentoLessThan(EstadoNotificacion estado, LocalDateTime ahora) → List

// ConfiguracionProgramaRepository
findByPrograma_Id(Long programaId) → Optional
```

**Effort**: ~30 minutos

---

## 🛠️ FASE 3: DTOs (REQUEST/RESPONSE) - 1.5 horas

### Crear DTOs en `model/dto/`:

```
dto/
├── request/
│   ├── CrearAsignacionRequest.java
│   │   - estudianteId, vacanteId
│   ├── CancelarAsignacionRequest.java
│   │   - motivoCancelacion
│   ├── ConfirmarVinculacionRequest.java
│   │   - asignacionId, fechaInicio, fechaFin
│   ├── CrearPlanRequest.java
│   │   - practicaId, objetivos, cronograma, descripcion
│   ├── AprobarPlanRequest.java
│   │   - planId, tipoRevisor (TUTOR/DOCENTE)
│   ├── RechazarPlanRequest.java
│   │   - planId, motivoRechazo
│   ├── CrearSeguimientoRequest.java
│   │   - practicaId, semana, actividades, logros, dificultades, archivos[]
│   ├── RevisarSeguimientoRequest.java
│   │   - seguimientoId, estado, observaciones
│   ├── CargarCartaPresentacionRequest.java
│   │   - practicaId, archivo
│   └── CargarConvenioRequest.java
│       - practicaId, archivo, tiposRequeridos[]
│
└── response/
    ├── AsignacionResponse.java
    ├── PracticaResponse.java
    ├── PlanPracticaResponse.java
    ├── SeguimientoPracticaResponse.java
    ├── DocumentoPracticaResponse.java
    ├── DashboardIndicadoresResponse.java
    │   - tarjetas por rol
    │   - estudiantes NO_APTO, APTO, EN_CURSO, por completar, etc
    └── NotificacionResponse.java
```

**Effort**: ~45 minutos

---

## 📦 FASE 4: SERVICES (CORE BUSINESS LOGIC) - 4-5 horas

### Bloque 1: AsignacionService (1 hora)

```java
interface AsignacionService {
    // CRUD base
    AsignacionResponse crearAsignacion(CrearAsignacionRequest req, Long coordinadorId) throws AsignacionException;
    AsignacionResponse obtenerAsignacion(Long id);
    Page<AsignacionResponse> obtenerAsignacionesActivas(Long coordinadorId, Pageable p);
    
    // Cambios de estado
    AsignacionResponse cancelarAsignacion(Long id, CancelarAsignacionRequest req);
    AsignacionResponse cambiarEstadoAVinculacion(Long id);
    
    // Validaciones
    void validarAsignacionCancelable(Long asignacionId) throws AsignacionException;
    void validarEstudianteApto(Long estudianteId) throws AsignacionException;
    void validarVacanteDisponible(Long vacanteId) throws AsignacionException;
}

// Métodos privados/helpers
- crearCambioEstado(Asignacion, nuevoEstado, usuario, motivo)
- enviarNotificacion(Asignacion, TipoNotificacion)
- afectarVacante(Vacante, +1/-1)  // incrementar/decrementar cupo usado
```

**Transacciones críticas**:
- `crearAsignacion`: Validar + crear + notificar (atomic)
- `cancelarAsignacion`: Validar estado + cambiar + liberar cupo + notificar

### Bloque 2: NotificacionService (1 hora)

```java
interface NotificacionService {
    // Crear notificaciones
    void notificar(Usuario usuario, TipoNotificacion tipo, Map<String, String> contexto);
    void notificarAsignacion(Asignacion asignacion);
    void notificarCancelacion(Asignacion asignacion);
    void notificarVinculacionConfirmada(Practica practica);
    void notificarPlanAprobado(PlanPractica plan, TipoFirmante aprobador);
    void notificarPlanRechazado(PlanPractica plan, TipoFirmante rechazador);
    void notificarSeguimientoRechazado(SeguimientoPractica seguimiento);
    void notificarAlertaInactividad(Practica practica, Long diasInactivo);
    
    // Envío async
    void procesarColaNotificaciones(); // @Scheduled, intenta enviar FALLIDO
    void registrarReintento(NotificacionHistorial notif, String motivo);
    
    // Consulta
    Page<NotificacionResponse> obtenerHistorial(Long usuarioId, Pageable p);
}

// Métodos privados
- obtenerPlantilla(TipoNotificacion, ConfiguracionPrograma)
- renderizarPlantilla(plantilla, contexto) → HTML
- enviarCorreo(destino, asunto, cuerpo) → SMTP + registrar
- construirContexto(objeto) → Map<String, String>
```

**Transacciones críticas**:
- `notificar`: crear + encolar (async) + no bloquear API

### Bloque 3: PracticaService (1.5 horas)

```java
interface PracticaService {
    // Crear Practica desde Asignacion
    PracticaResponse confirmarVinculacion(ConfirmarVinculacionRequest req) throws Exception;
    
    // Queries
    PracticaResponse obtenerPractica(Long id);
    Optional<PracticaResponse> obtenerPracticaEstudiante(Long estudianteId);
    Page<PracticaResponse> obtenerPracticasDocente(Long docenteId, EstadoPractica estado, Pageable p);
    
    // Gestión estado
    void cambiarEstado(Long practicaId, EstadoPractica nuevoEstado, String motivo);
    
    // Validaciones
    void validarPuedeConfirmarVinculacion(Long asignacionId) throws Exception;
    void validarDocumentosCompletos(Long practicaId) throws Exception;
}

// Métodos privados
- obtenerDocenteAsesor(Programa) → Usuario (lógica TBD: round-robin, especialidad, etc)
- validarFirmasDocumentos(Practica)
- asignarDocenteAutomaticamente(Practica)
```

**Transacciones críticas**:
- `confirmarVinculacion`: crear Practica + asignar Docente + notificar (atomic)

### Bloque 4: PlanPracticaService (1 hora)

```java
interface PlanPracticaService {
    // CRUD
    PlanPracticaResponse crearPlan(CrearPlanRequest req, Long estudianteId) throws PlanException;
    PlanPracticaResponse obtenerPlan(Long id);
    PlanPracticaResponse editarPlan(Long id, CrearPlanRequest req) throws PlanException;
    
    // Aprobaciones
    PlanPracticaResponse aprobarPorTutor(Long planId, Long tutorId) throws PlanException;
    PlanPracticaResponse aprobarPorDocente(Long planId, Long docenteId) throws PlanException;
    PlanPracticaResponse rechazarPlan(Long planId, RechazarPlanRequest req) throws PlanException;
    
    // Queries
    Optional<PlanPracticaResponse> obtenerPlanPorPractica(Long practicaId);
    List<PlanPracticaResponse> obtenerPlanesRechazados();
    
    // Validaciones
    void validarPlanEditable(PlanPractica plan) throws PlanException;
    void validarPuedeAprobarse(PlanPractica plan, TipoFirmante aprobador) throws PlanException;
}

// Métodos privados
- validarEstadoTransicion(EstadoPlan actual, EstadoPlan nuevo)
- enviarNotificacionAprobacion(PlanPractica, TipoFirmante)
```

**Transacciones críticas**:
- `aprobarPorDocente`: cambiar estado + notificar + permitir seguimientos (atomic)

### Bloque 5: SeguimientoPracticaService (1.5 horas)

```java
interface SeguimientoPracticaService {
    // CRUD
    SeguimientoPracticaResponse crearSeguimiento(CrearSeguimientoRequest req, Long estudianteId) throws SeguimientoException;
    SeguimientoPracticaResponse obtenerSeguimiento(Long id);
    SeguimientoPracticaResponse editarSeguimiento(Long id, CrearSeguimientoRequest req) throws SeguimientoException;
    
    // Consultas
    Optional<SeguimientoPracticaResponse> obtenerPorSemana(Long practicaId, Integer semana);
    Page<SeguimientoPracticaResponse> obtenerPorPractica(Long practicaId, Pageable p);
    Page<SeguimientoPracticaResponse> obtenerPendientesDocente(Long docenteId, Pageable p);
    
    // Revisión
    SeguimientoPracticaResponse aprobarSeguimiento(Long seguimientoId, Long docenteId, String observaciones) throws SeguimientoException;
    SeguimientoPracticaResponse rechazarSeguimiento(Long seguimientoId, Long docenteId, String observaciones) throws SeguimientoException;
    
    // Validaciones
    void validarPuedeCrear(Long practicaId) throws SeguimientoException;
    void validarPuedeEditar(SeguimientoPractica seguimiento, Long estudianteId) throws SeguimientoException;
    void validarSemana(Long practicaId, Integer semana) throws SeguimientoException;
}

// Métodos privados
- validarPlanAprobado(Practica)
- esUltimaSemana(SeguimientoPractica) → boolean
- obtenerSemanaActual(Practica) → int
- enviarNotificacionRechazo(SeguimientoPractica, observaciones)
```

**Transacciones críticas**:
- `crearSeguimiento`: validar plan + validar semana + crear + NO bloquear API
- `rechazarSeguimiento`: cambiar estado + agregar observaciones + notificar (atomic)

### Bloque 6: DocumentoPracticaService (1 hora)

```java
interface DocumentoPracticaService {
    // Carga de documentos
    DocumentoPracticaResponse cargarCartaPresentacion(Long practicaId, MultipartFile archivo) throws DocumentoException;
    DocumentoPracticaResponse cargarConvenio(Long practicaId, MultipartFile archivo) throws DocumentoException;
    
    // Gestión firmas (convenio)
    FirmaDocumentoResponse confirmarFirma(Long documentoId, TipoFirmante tipo, Long usuarioId) throws DocumentoException;
    DocumentoPracticaResponse obtenerFirmas(Long documentoId);
    boolean validarFirmasCompletas(Long documentoId);
    
    // Consultas
    DocumentoPracticaResponse obtenerDocumento(Long id);
    List<DocumentoPracticaResponse> obtenerDocumentosPorTipo(Long practicaId, TipoDocumento tipo);
    
    // Validaciones
    void validarDocumentoMutable(DocumentoPractica doc) throws DocumentoException;
}

// Métodos privados
- almacenarArchivo(MultipartFile) → String urlGuardada
- validarFormatoArchivo(file)
- crearFirmas(Documento, tiposRequeridos)
- enviarNotificacionDocumento(Practica, TipoDocumento)
```

**Storage**: Usar AWS S3, Google Cloud Storage o similar (TBD configuración)

### Bloque 7: DashboardService (actualizar existente - 1 hora)

```java
interface DashboardService {
    // Método genérico
    DashboardIndicadoresResponse obtenerIndicadores(Long usuarioId, Rol rol);
    
    // Indicadores por rol
    Map<String, Object> indicadoresDTI();
    Map<String, Object> indicadoresCoordAcad();
    Map<String, Object> indicadoresCoordPracticas();
    Map<String, Object> indicadoresDocente(Long docenteId);
    Map<String, Object> indicadoresTutor(Long tutorId);
    Map<String, Object> indicadoresDireccion();
    Map<String, Object> indicadoresEstudiante(Long estudianteId);
    
    // Queries base (usar en los métodos anteriores)
    long contarPorEstado(String entidad, String estado);
    long contarAtrasos(String entidad, int diasUmbral);
    double obtenerTasaExito(String entidad);
}

// Ejemplo de qué contar:
// DTI: usuarios activos por rol, estudiantes por estado (NO_APTO, APTO, EN_PRACTICA, FINALIZADA)
// Coord Prácticas: APTOS disponibles, vacantes disponibles, asignaciones EN_VINCULACION, prácticas EN_CURSO, planes RECHAZADO
// Docente: estudiantes asignados, seguimientos PENDIENTE, planes PENDIENTE_APROBACION
// Tutor: practicantes a cargo (EN_CURSO), planes RECHAZADO, alertas inactividad
// Dirección: prácticas EN_CURSO por programa, tasa aprobación general, promedio duración
// Estudiante: estado actual (EN_CURSO/FINALIZADA), semana actual, docs pendientes, última nota seguimiento
```

**Effort para todos los services**: ~5 horas

---

## 🎮 FASE 5: CONTROLLERS REST - 2 horas

```
controller/
├── AsignacionController.java (NEW)
│   POST   /api/asignaciones                           → crear
│   GET    /api/asignaciones/{id}                      → obtener
│   GET    /api/asignaciones/coordinador/{id}          → listar por coordinador
│   PUT    /api/asignaciones/{id}/cancelar             → cancelar
│   
├── PracticaController.java (actualizar/NEW)
│   POST   /api/practicas/{asignacionId}/vinculacion   → confirmar vinculación
│   GET    /api/practicas/{id}                         → obtener
│   GET    /api/practicas/estudiante/{id}              → obtener práctica estudiante
│   GET    /api/practicas/docente/{id}                 → listar por docente
│   
├── PlanPracticaController.java (NEW)
│   POST   /api/planes                                 → crear plan
│   GET    /api/planes/{id}                            → obtener
│   PUT    /api/planes/{id}                            → editar
│   POST   /api/planes/{id}/aprobar-tutor              → aprobar por tutor
│   POST   /api/planes/{id}/aprobar-docente            → aprobar por docente
│   POST   /api/planes/{id}/rechazar                   → rechazar
│   
├── SeguimientoPracticaController.java (NEW)
│   POST   /api/seguimientos                           → crear seguimiento
│   GET    /api/seguimientos/{id}                      → obtener
│   PUT    /api/seguimientos/{id}                      → editar
│   GET    /api/seguimientos/practica/{id}             → listar por práctica
│   GET    /api/seguimientos/docente/{id}/pendientes   → pendientes del docente
│   POST   /api/seguimientos/{id}/aprobar              → aprobar
│   POST   /api/seguimientos/{id}/rechazar             → rechazar
│   
├── DocumentoPracticaController.java (NEW)
│   POST   /api/documentos/carta-presentacion          → cargar carta
│   POST   /api/documentos/convenio                    → cargar convenio
│   GET    /api/documentos/{id}                        → obtener documento
│   GET    /api/documentos/practica/{id}               → listar documentos
│   POST   /api/documentos/{id}/firmar                 → confirmar firma
│   GET    /api/documentos/{id}/firmas                 → obtener firmas
│   
└── DashboardController.java (actualizar)
    GET   /api/dashboard/indicadores                  → indicadores personalizados por rol
    GET   /api/dashboard/indicadores-coordinador      → específico Coord Prácticas
    GET   /api/dashboard/indicadores-docente          → específico Docente
```

**Validaciones por endpoint**:
- Auth: @Secured según roles
- Input: @Valid DTOs
- Error handling: Excepciones custom (AsignacionException, PlanException, etc)
- Responses: HTTP 201 (POST create), 200 (GET/PUT/DELETE success), 400 (validation), 403 (forbidden), 404 (not found)

**Effort**: ~2 horas

---

## 🔒 FASE 6: EXCEPCIONES CUSTOM - 30 minutos

```
exception/
├── AsignacionException.java
├── PracticaException.java
├── PlanException.java
├── SeguimientoException.java
├── DocumentoException.java
└── NotificacionException.java
```

---

## 📋 ORDEN RECOMENDADO DE IMPLEMENTACIÓN

1. **Repositories** → 30 min (simple CRUD queries)
2. **DTOs** → 45 min (mapeos, constructores)
3. **Excepciones** → 15 min (clases simples)
4. **AsignacionService** → 45 min (menos complejo)
5. **NotificacionService** → 45 min (más async)
6. **PlanPracticaService** → 45 min
7. **SeguimientoPracticaService** → 1 hora
8. **DocumentoPracticaService** → 45 min
9. **PracticaService** → 1 hora (transacciones complejas)
10. **DashboardService** → 1 hora (queries complejas)
11. **Controllers** → 2 horas (routing, error handling)

**Total estimado**: 8-9 horas de desarrollo puro

---

## 🧪 TESTING (Skipped por user request)

- ✅ Entidades JPA: validaciones de restricciones
- ✅ Repositories: queries custom
- ✅ Services: lógica de negocio
- ✅ Controllers: endpoints REST
- ✅ Integración e2e

**Status**: DEFERRED hasta que user solicite

---

## 🎨 FRONTEND (Paralelo pero secundario)

Basado en APIs creadas en backend:

```
src/pages/
├── asignaciones/
│   ├── ListaAsignaciones.tsx        → tabla de asignaciones activas
│   ├── CrearAsignacion.tsx          → modal/formulario
│   └── DetalleAsignacion.tsx        → vista detalle + acciones
├── practicas/
│   ├── ListaPracticas.tsx           → tabla mi práctica (estudiante)
│   ├── DetallePractica.tsx          → estado, documentos, seguimientos
│   └── CargarDocumentos.tsx         → carga carta + convenio + firmas
├── planes/
│   ├── CrearPlan.tsx                → formulario objetivos + cronograma
│   ├── RevisarPlan.tsx              → Tutor/Docente revisan
│   └── EstadoPlan.tsx               → ver aprobaciones
├── seguimientos/
│   ├── RegistroSemanal.tsx          → cargar seguimiento
│   ├── ListaSeguimientos.tsx        → histórico
│   ├── RevisarSeguimiento.tsx       → Docente revisa
│   └── DetalleObservaciones.tsx     → ver observaciones
└── dashboard/
    ├── DashboardCoord.tsx           → indicadores Coordinador
    ├── DashboardDocente.tsx         → indicadores Docente
    ├── DashboardEstudiante.tsx      → indicadores Estudiante
    └── TarjetasIndicadores.tsx      → componente reutilizable
```

**Frontend effort**: Paralelo, estimado 12-15 horas

---

## 🚀 DEPLOYMENT & MERGE

1. Backend development en `sprint3-santiago`
2. Frontend development en rama paralela (crear después)
3. QA/Review cuando backend compilado sin errores
4. Merge a `main` con PR review
5. Deploy a staging → testing → producción

---

**Próximo paso**: ¿Procedo directamente con Repositories o prefieres revisar algo del documento arquitectónico primero?

