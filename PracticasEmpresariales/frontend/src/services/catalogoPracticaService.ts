import api from './api'
import { ApiResponse, Pageable, CatalogoPracticaResponse } from '../types'

export interface CrearCatalogoPracticaRequest {
    numeroPractica: number
    nombre: string
    materiaNucleoNombre: string
    materiaNucleoCodigo: string
    programaId: number
    numeroCortesSegumiento: number
    duracionSemanas: number
    documentosRequeridos: string[]
}

export type EditarCatalogoPracticaRequest = Partial<CrearCatalogoPracticaRequest>

/**
 * Singleton: una sola instancia centraliza las operaciones del catálogo.
 * El catálogo es la fuente Prototype: sus entradas se clonan al crear
 * instancias individuales de práctica para cada estudiante APTO.
 */
export const catalogoPracticaService = {
    async crear(data: CrearCatalogoPracticaRequest): Promise<CatalogoPracticaResponse> {
        const res = await api.post<ApiResponse<CatalogoPracticaResponse>>(
            '/catalogo-practicas',
            data,
        )
        return res.data.datos!
    },

    async listar(
        page = 0,
        size = 50,
        programaId?: number,
    ): Promise<Pageable<CatalogoPracticaResponse>> {
        const params = new URLSearchParams({ page: String(page), size: String(size) })
        if (programaId) params.append('programaId', String(programaId))
        const res = await api.get<ApiResponse<Pageable<CatalogoPracticaResponse>>>(
            `/catalogo-practicas?${params.toString()}`,
        )
        return res.data.datos!
    },

    async obtener(id: number): Promise<CatalogoPracticaResponse> {
        const res = await api.get<ApiResponse<CatalogoPracticaResponse>>(
            `/catalogo-practicas/${id}`,
        )
        return res.data.datos!
    },

    async editar(
        id: number,
        data: EditarCatalogoPracticaRequest,
    ): Promise<CatalogoPracticaResponse> {
        const res = await api.put<ApiResponse<CatalogoPracticaResponse>>(
            `/catalogo-practicas/${id}`,
            data,
        )
        return res.data.datos!
    },

    async desactivar(id: number): Promise<void> {
        await api.patch(`/catalogo-practicas/${id}/desactivar`)
    },
}
