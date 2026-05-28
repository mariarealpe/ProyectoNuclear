export type Rol =
    | 'ADMIN_DTI'
    | 'COORDINACION_ACADEMICA'
    | 'COORDINADOR_PRACTICAS'
    | 'DOCENTE_ASESOR'
    | 'TUTOR_EMPRESARIAL'
    | 'ESTUDIANTE'
    | 'DIRECCION'

export type EtiquetaCargo = 'COORDINACION_ACADEMICA' | 'SECRETARIA'

export type EstadoEstudiante = 'NO_APTO' | 'APTO'

export type TipoAccion =
    | 'LOGIN_EXITOSO' | 'LOGIN_FALLIDO' | 'LOGOUT'
    | 'CREAR' | 'EDITAR' | 'DESACTIVAR' | 'ACTIVAR'
    | 'CAMBIO_ESTADO' | 'ACCESO_NO_AUTORIZADO'
    | 'RESET_PASSWORD' | 'CAMBIO_PASSWORD' | 'EXPORTAR' | 'CONSULTAR'

export interface AuthUser {
    usuarioId: number
    nombre: string
    correo: string
    rol: Rol
    etiquetaCargo?: EtiquetaCargo
    primerIngreso: boolean
    facultadId?: number
    programaId?: number
    token: string
}

export interface UsuarioResponse {
    id: number
    nombre: string
    correo: string
    telefono?: string
    fotoPerfil?: string
    rol: Rol
    etiquetaCargo?: EtiquetaCargo
    activo: boolean
    primerIngreso: boolean
    ultimoAcceso?: string
    creadoEn: string
    facultadId?: number
    facultadNombre?: string
    programaId?: number
    programaNombre?: string
    estadoEstudiante?: EstadoEstudiante
}

export interface FacultadResponse {
    id: number
    nombre: string
    descripcion?: string
    activa: boolean
    numeroProgramas: number
    creadaEn: string
}

export interface ProgramaResponse {
    id: number
    nombre: string
    descripcion?: string
    facultadId: number
    facultadNombre: string
    numeroTotalPracticas: number
    promedioMinimoGeneral: number
    activo: boolean
    creadoEn: string
}

export interface BitacoraResponse {
    id: number
    usuarioId?: number
    nombreUsuario: string
    rolUsuario: Rol
    etiquetaCargoUsuario?: EtiquetaCargo
    fechaHora: string
    modulo: string
    tipoAccion: TipoAccion
    registroAfectadoId?: number
    registroAfectadoTipo?: string
    valoresAnteriores?: string
    valoresNuevos?: string
    ipOrigen?: string
    exitoso: boolean
    motivoFallo?: string
}

export interface DashboardSeccion {
    id: string
    titulo: string
    ruta: string
    contador: number
}

export interface DashboardResponse {
    rol: Rol
    etiquetaCargo?: EtiquetaCargo
    nombreUsuario: string
    titulo: string
    soloLectura: boolean
    secciones: DashboardSeccion[]
}

export interface ApiResponse<T> {
    exitoso: boolean
    mensaje?: string
    datos?: T
}

export interface Pageable<T> {
    content: T[]
    totalElements: number
    totalPages: number
    number: number
    size: number
}

// ============================================================================
// Gestión Empresarial y Vacantes (Sprint 2 — Persona 2)
// ============================================================================

export type EstadoVacante = 'PENDIENTE' | 'DISPONIBLE' | 'CERRADA' | 'RECHAZADA'

export type ModalidadVacante = 'PRESENCIAL' | 'REMOTO' | 'HIBRIDO'

export type JornadaVacante = 'TIEMPO_COMPLETO' | 'MEDIO_TIEMPO'

export interface EmpresaResponse {
    id: number
    nit: string
    razonSocial: string
    nombreComercial?: string
    sector?: string
    direccion?: string
    ciudad?: string
    correoContacto: string
    telefono?: string
    sitioWeb?: string
    representanteLegal?: string
    descripcion?: string
    activo: boolean
    creadoEn: string
    actualizadoEn: string
}

export interface TutorEmpresarialResponse {
    id: number
    usuarioId: number
    nombre: string
    correo: string
    telefono?: string
    empresaId: number
    empresaRazonSocial: string
    cargo: string
    area?: string
    telefonoCorporativo?: string
    esResponsablePrincipal: boolean
    activo: boolean
    creadoEn: string
}

export interface VacanteResponse {
    id: number
    titulo: string
    descripcion: string
    empresaId: number
    empresaRazonSocial: string
    empresaNit: string
    tutorId?: number
    tutorNombre?: string
    programaId: number
    programaNombre: string
    modalidad: ModalidadVacante
    jornada: JornadaVacante
    ciudad?: string
    cupos: number
    cuposOcupados: number
    duracionMeses: number
    remunerada: boolean
    valorRemuneracion?: number
    fechaInicio?: string
    fechaFin?: string
    fechaPublicacion: string
    fechaCierre?: string
    requisitos?: string
    responsabilidades?: string
    estado: EstadoVacante
    motivoRechazo?: string
    aprobadorId?: number
    aprobadorNombre?: string
    fechaDecision?: string
}

export interface BitacoraVacanteResponse {
    id: number
    vacanteId: number
    usuarioId?: number
    usuarioNombre?: string
    estadoAnterior?: EstadoVacante
    estadoNuevo: EstadoVacante
    motivo?: string
    fechaHora: string
}
