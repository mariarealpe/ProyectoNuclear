import api from './api'
import {
    ApiResponse, Pageable,
    EstadoEstudiante,
    EstudianteResponse,
} from '../types'

export interface CrearEstudianteRequest {
    nombre: string
    identificacion: string
    correo: string
    telefono?: string
    contactoEmergencia?: string
    programaId: number
    semestre: number
}

export interface ValidarAptitudRequest {
    aprobar: boolean
    motivoRechazo?: string
}

export interface EnviarAlProcesoRequest {
    estudianteIds: number[]
}

/**
 * Singleton: una sola instancia del servicio centraliza todas las
 * operaciones de estudiantes. Dependency Inversion: las páginas dependen
 * de esta abstracción, no de llamadas Axios directas.
 */
export const estudianteService = {
    async crear(data: CrearEstudianteRequest): Promise<EstudianteResponse> {
        const res = await api.post<ApiResponse<EstudianteResponse>>('/estudiantes', data)
        return res.data.datos!
    },

    async listar(
        page = 0,
        size = 50,
        estado?: EstadoEstudiante,
        programaId?: number,
        busqueda?: string,
    ): Promise<Pageable<EstudianteResponse>> {
        const params = new URLSearchParams({ page: String(page), size: String(size) })
        if (estado) params.append('estado', estado)
        if (programaId) params.append('programaId', String(programaId))
        if (busqueda) params.append('busqueda', busqueda)
        const res = await api.get<ApiResponse<Pageable<EstudianteResponse>>>(
            `/estudiantes?${params.toString()}`,
        )
        return res.data.datos!
    },

    async obtener(id: number): Promise<EstudianteResponse> {
        const res = await api.get<ApiResponse<EstudianteResponse>>(`/estudiantes/${id}`)
        return res.data.datos!
    },

    /**
     * Chain of Responsibility: el backend aplica la cadena
     * créditos → promedio → práctica anterior → documentos → HV válida.
     * Si algún eslabón falla con aprobar=true, retorna 400.
     */
    async validarAptitud(id: number, data: ValidarAptitudRequest): Promise<EstudianteResponse> {
        const res = await api.patch<ApiResponse<EstudianteResponse>>(
            `/estudiantes/${id}/aptitud`,
            data,
        )
        return res.data.datos!
    },

    /**
     * Observer: al enviar al proceso el backend notifica internamente al
     * Coordinador de Prácticas que tiene nuevos candidatos disponibles.
     */
    async enviarAlProceso(data: EnviarAlProcesoRequest): Promise<void> {
        await api.post('/estudiantes/enviar-al-proceso', data)
    },

    async desactivar(id: number): Promise<void> {
        await api.patch(`/estudiantes/${id}/desactivar`)
    },

    async activar(id: number): Promise<void> {
        await api.patch(`/estudiantes/${id}/activar`)
    },
}
