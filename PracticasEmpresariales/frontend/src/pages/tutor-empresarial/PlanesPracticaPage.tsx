/**
 * Vista del Tutor Empresarial — planes de práctica pendientes de aprobación.
 * Sprint 3: requiere el endpoint /planes en el backend.
 */
export default function PlanesPracticaPage() {
    return (
        <div className="space-y-6">
            <div>
                <h1 className="text-2xl font-bold text-gray-900">Planes de Práctica</h1>
                <p className="text-sm text-gray-500">Aprueba o rechaza los planes de práctica cargados por tus practicantes.</p>
            </div>

            <div className="card border-2 border-dashed border-orange-300 bg-orange-50">
                <div className="flex items-start gap-4">
                    <span className="text-3xl">📄</span>
                    <div>
                        <p className="font-bold text-orange-800">Planes de Práctica — Sprint 3</p>
                        <p className="text-sm text-orange-700 mt-1">Disponible en Sprint 3. Incluirá:</p>
                        <ul className="text-sm text-orange-700 mt-1 list-disc list-inside space-y-0.5">
                            <li>Listado de planes pendientes de tu aprobación</li>
                            <li>Visualización del plan completo con objetivos y cronograma</li>
                            <li>Aprobación o rechazo con observaciones</li>
                            <li>Historial de planes por practicante</li>
                        </ul>
                    </div>
                </div>
            </div>

            <div className="card text-center py-10 text-gray-400">
                <span className="text-4xl block mb-3">📋</span>
                No hay planes pendientes de revisión.
            </div>
        </div>
    )
}
