import api from './api'
import { ApiResponse, Pageable, TutorEmpresarialResponse } from '../types'

export interface CrearTutorRequest {
    nombre: string
    correo: string
    telefono?: string
    empresaId: number
    cargo: string
    area?: string
    telefonoCorporativo?: string
    esResponsablePrincipal?: boolean
}

export interface EditarTutorRequest {
    nombre: string
    telefono?: string
    cargo: string
    area?: string
    telefonoCorporativo?: string
    esResponsablePrincipal?: boolean
}

export const tutorService = {
    async crear(data: CrearTutorRequest): Promise<TutorEmpresarialResponse> {
        const res = await api.post<ApiResponse<TutorEmpresarialResponse>>('/tutores', data)
        return res.data.datos!
    },

    async listar(page = 0, size = 20, empresaId?: number): Promise<Pageable<TutorEmpresarialResponse>> {
        const params = new URLSearchParams({ page: String(page), size: String(size) })
        if (empresaId) params.append('empresaId', String(empresaId))
        const res = await api.get<ApiResponse<Pageable<TutorEmpresarialResponse>>>(`/tutores?${params.toString()}`)
        return res.data.datos!
    },

    async obtener(id: number): Promise<TutorEmpresarialResponse> {
        const res = await api.get<ApiResponse<TutorEmpresarialResponse>>(`/tutores/${id}`)
        return res.data.datos!
    },

    async editar(id: number, data: EditarTutorRequest): Promise<TutorEmpresarialResponse> {
        const res = await api.put<ApiResponse<TutorEmpresarialResponse>>(`/tutores/${id}`, data)
        return res.data.datos!
    },

    async desactivar(id: number): Promise<void> {
        await api.patch(`/tutores/${id}/desactivar`)
    },

    async activar(id: number): Promise<void> {
        await api.patch(`/tutores/${id}/activar`)
    },
}
