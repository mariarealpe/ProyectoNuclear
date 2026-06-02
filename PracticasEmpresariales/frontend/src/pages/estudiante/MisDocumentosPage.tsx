import { useAuth } from '../../context/AuthContext'

interface DocInfo {
    titulo: string
    icono: string
    estado: string
    colorEstado: string
    descripcion: string
}

const DOCUMENTOS: DocInfo[] = [
    {
        titulo: 'Hoja de Vida',
        icono: '📄',
        estado: 'Pendiente de validación',
        colorEstado: 'bg-yellow-100 text-yellow-800',
        descripcion: 'La carga y validación de hoja de vida estará disponible en Sprint 3.',
    },
    {
        titulo: 'Plan de Práctica',
        icono: '📋',
        estado: 'No disponible aún',
        colorEstado: 'bg-gray-100 text-gray-600',
        descripcion: 'Disponible una vez que el Coordinador de Prácticas te asigne a una vacante.',
    },
    {
        titulo: 'Informe Final',
        icono: '📑',
        estado: 'No disponible aún',
        colorEstado: 'bg-gray-100 text-gray-600',
        descripcion: 'Se habilitará al finalizar el período de práctica.',
    },
    {
        titulo: 'Acta de Sustentación',
        icono: '🎓',
        estado: 'No disponible aún',
        colorEstado: 'bg-gray-100 text-gray-600',
        descripcion: 'Generada automáticamente tras la sustentación final (Sprint 4).',
    },
]

export default function MisDocumentosPage() {
    const { user } = useAuth()

    return (
        <div className="space-y-6">
            <div>
                <h1 className="text-2xl font-bold text-gray-900">Mis Documentos</h1>
                <p className="text-sm text-gray-500">
                    Hola, <span className="font-medium">{user?.nombre}</span>. Gestiona tu hoja de vida y documentos de práctica.
                </p>
            </div>

            <div className="card border-2 border-dashed border-orange-300 bg-orange-50">
                <div className="flex items-start gap-4">
                    <span className="text-3xl">📁</span>
                    <div>
                        <p className="font-bold text-orange-800">Gestión Documental — Sprint 3</p>
                        <p className="text-sm text-orange-700 mt-1">
                            El módulo documental estará disponible en Sprint 3. Incluirá:
                        </p>
                        <ul className="text-sm text-orange-700 mt-1 list-disc list-inside space-y-0.5">
                            <li>Carga de hoja de vida con versioning automático</li>
                            <li>Validación de documentos por la Coordinación Académica</li>
                            <li>Carga y aprobación del plan de práctica</li>
                            <li>Descarga del acta de sustentación</li>
                        </ul>
                    </div>
                </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                {DOCUMENTOS.map(doc => (
                    <div key={doc.titulo} className="card flex items-start gap-4">
                        <span className="text-3xl flex-shrink-0">{doc.icono}</span>
                        <div className="flex-1 min-w-0">
                            <p className="font-semibold text-gray-800">{doc.titulo}</p>
                            <span className={`text-xs font-semibold px-2.5 py-0.5 rounded-full mt-1 inline-block ${doc.colorEstado}`}>
                                {doc.estado}
                            </span>
                            <p className="text-xs text-gray-400 mt-2">{doc.descripcion}</p>
                        </div>
                    </div>
                ))}
            </div>
        </div>
    )
}
