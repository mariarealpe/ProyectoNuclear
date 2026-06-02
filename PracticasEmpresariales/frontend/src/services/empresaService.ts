import api from './api'
import { ApiResponse, EmpresaResponse, Pageable } from '../types'

export interface CrearEmpresaRequest {
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
}

export type EditarEmpresaRequest = CrearEmpresaRequest

export const empresaService = {
    async crear(data: CrearEmpresaRequest): Promise<EmpresaResponse> {
        const res = await api.post<ApiResponse<EmpresaResponse>>('/empresas', data)
        return res.data.datos!
    },

    async listar(page = 0, size = 20, filtro?: string): Promise<Pageable<EmpresaResponse>> {
        const params = new URLSearchParams({ page: String(page), size: String(size) })
        if (filtro) params.append('filtro', filtro)
        const res = await api.get<ApiResponse<Pageable<EmpresaResponse>>>(`/empresas?${params.toString()}`)
        return res.data.datos!
    },

    async obtener(id: number): Promise<EmpresaResponse> {
        const res = await api.get<ApiResponse<EmpresaResponse>>(`/empresas/${id}`)
        return res.data.datos!
    },

    async editar(id: number, data: EditarEmpresaRequest): Promise<EmpresaResponse> {
        const res = await api.put<ApiResponse<EmpresaResponse>>(`/empresas/${id}`, data)
        return res.data.datos!
    },

    /** GPE-150: transición Pendiente → Aprobada */
    async aprobar(id: number): Promise<void> {
        await api.patch(`/empresas/${id}/aprobar`)
    },

    /** GPE-150: transición Pendiente → Rechazada (motivo obligatorio) */
    async rechazar(id: number, motivo: string): Promise<void> {
        await api.patch(`/empresas/${id}/rechazar`, { motivo })
    },

    /** GPE-150: transición Aprobada → Inactiva (no se puede revertir) */
    async inactivar(id: number): Promise<void> {
        await api.patch(`/empresas/${id}/desactivar`)
    },

    async desactivar(id: number): Promise<void> {
        await api.patch(`/empresas/${id}/desactivar`)
    },

    async activar(id: number): Promise<void> {
        await api.patch(`/empresas/${id}/activar`)
    },
}
