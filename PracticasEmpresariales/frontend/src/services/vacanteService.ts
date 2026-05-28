import api from './api'
import {
    ApiResponse, BitacoraVacanteResponse, EstadoVacante, JornadaVacante,
    ModalidadVacante, Pageable, VacanteResponse,
} from '../types'

export interface CrearVacanteRequest {
    titulo: string
    descripcion: string
    empresaId: number
    tutorId?: number
    programaId: number
    modalidad: ModalidadVacante
    jornada: JornadaVacante
    ciudad?: string
    cupos: number
    duracionMeses: number
    remunerada: boolean
    valorRemuneracion?: number
    fechaInicio?: string
    fechaFin?: string
    requisitos?: string
    responsabilidades?: string
}

export interface EditarVacanteRequest {
    titulo: string
    descripcion: string
    tutorId?: number
    modalidad: ModalidadVacante
    jornada: JornadaVacante
    ciudad?: string
    cupos: number
    duracionMeses: number
    remunerada: boolean
    valorRemuneracion?: number
    fechaInicio?: string
    fechaFin?: string
    requisitos?: string
    responsabilidades?: string
}

export interface DecisionVacanteRequest {
    motivo?: string
}

export const vacanteService = {
    async crear(data: CrearVacanteRequest): Promise<VacanteResponse> {
        const res = await api.post<ApiResponse<VacanteResponse>>('/vacantes', data)
        return res.data.datos!
    },

    async listar(page = 0, size = 20, estado?: EstadoVacante): Promise<Pageable<VacanteResponse>> {
        const params = new URLSearchParams({ page: String(page), size: String(size) })
        if (estado) params.append('estado', estado)
        const res = await api.get<ApiResponse<Pageable<VacanteResponse>>>(`/vacantes?${params.toString()}`)
        return res.data.datos!
    },

    async obtener(id: number): Promise<VacanteResponse> {
        const res = await api.get<ApiResponse<VacanteResponse>>(`/vacantes/${id}`)
        return res.data.datos!
    },

    async historial(id: number): Promise<BitacoraVacanteResponse[]> {
        const res = await api.get<ApiResponse<BitacoraVacanteResponse[]>>(`/vacantes/${id}/historial`)
        return res.data.datos!
    },

    async editar(id: number, data: EditarVacanteRequest): Promise<VacanteResponse> {
        const res = await api.put<ApiResponse<VacanteResponse>>(`/vacantes/${id}`, data)
        return res.data.datos!
    },

    async aprobar(id: number, motivo?: string): Promise<VacanteResponse> {
        const res = await api.patch<ApiResponse<VacanteResponse>>(`/vacantes/${id}/aprobar`, { motivo })
        return res.data.datos!
    },

    async rechazar(id: number, motivo: string): Promise<VacanteResponse> {
        const res = await api.patch<ApiResponse<VacanteResponse>>(`/vacantes/${id}/rechazar`, { motivo })
        return res.data.datos!
    },

    async cerrar(id: number, motivo?: string): Promise<VacanteResponse> {
        const res = await api.patch<ApiResponse<VacanteResponse>>(`/vacantes/${id}/cerrar`, { motivo })
        return res.data.datos!
    },
}
