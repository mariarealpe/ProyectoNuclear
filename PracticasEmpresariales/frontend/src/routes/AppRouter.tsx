import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import ProtectedRoute from './ProtectedRoute'
import MainLayout from '../layouts/MainLayout'

// Páginas públicas
import LoginPage from '../pages/auth/LoginPage'
import CambiarPasswordPage from '../pages/auth/CambiarPasswordPage'
import NoAutorizadoPage from '../pages/NoAutorizadoPage'

// Shared
import DashboardPage from '../pages/dashboard/DashboardPage'

// ADMIN_DTI
import UsuariosPage from '../pages/admin-dti/UsuariosPage'
import FacultadesPage from '../pages/admin-dti/FacultadesPage'
import ProgramasPage from '../pages/admin-dti/ProgramasPage'
import AuditoriaPage from '../pages/admin-dti/AuditoriaPage'

// Empresas y tutores
import EmpresasPage from '../pages/empresas/EmpresasPage'
import TutoresPage from '../pages/tutor-empresarial/TutoresPage'

// Vacantes
import VacantesPage from '../pages/vacantes/VacantesPage'

// Estudiantes (vista gestión)
import EstudiantesPage from '../pages/estudiantes/EstudiantesPage'

// Catálogo de prácticas
import CatalogoPracticasPage from '../pages/practicas/CatalogoPracticasPage'

// Coordinador de Prácticas
import AsignacionesPage from '../pages/coordinador-practicas/AsignacionesPage'

// Docente Asesor
import MisEstudiantesPage from '../pages/docente-asesor/MisEstudiantesPage'
import SeguimientosPage from '../pages/docente-asesor/SeguimientosPage'
import SustentacionesPage from '../pages/docente-asesor/SustentacionesPage'

// Tutor Empresarial
import MisPracticantesPage from '../pages/tutor-empresarial/MisPracticantesPage'
import PlanesPracticaPage from '../pages/tutor-empresarial/PlanesPracticaPage'
import EncuestasPage from '../pages/tutor-empresarial/EncuestasPage'

// Estudiante (vista propia)
import MiPracticaPage from '../pages/estudiante/MiPracticaPage'
import MiSeguimientoPage from '../pages/estudiante/MiSeguimientoPage'
import MisDocumentosPage from '../pages/estudiante/MisDocumentosPage'

// Dirección
import IndicadoresPage from '../pages/direccion/IndicadoresPage'
import ReportesPage from '../pages/direccion/ReportesPage'

export default function AppRouter() {
    const { user } = useAuth()

    return (
        <BrowserRouter>
            <Routes>
                {/* Públicas */}
                <Route path="/login" element={<LoginPage />} />
                <Route path="/no-autorizado" element={<NoAutorizadoPage />} />

                {/* Cambio de contraseña obligatorio en primer ingreso */}
                <Route element={<ProtectedRoute />}>
                    <Route path="/cambiar-password" element={<CambiarPasswordPage />} />
                </Route>

                {/* Rutas protegidas con layout */}
                <Route element={<ProtectedRoute />}>
                    <Route element={<MainLayout />}>
                        <Route path="/dashboard" element={<DashboardPage />} />

                        {/* ── Solo DTI ───────────────────────────────────────── */}
                        <Route element={<ProtectedRoute rolesPermitidos={['ADMIN_DTI']} />}>
                            <Route path="/usuarios"   element={<UsuariosPage />} />
                            <Route path="/facultades" element={<FacultadesPage />} />
                            <Route path="/programas"  element={<ProgramasPage />} />
                            <Route path="/auditoria"  element={<AuditoriaPage />} />
                        </Route>

                        {/* ── Gestión Empresarial (DTI + Coordinador Prácticas) */}
                        <Route element={<ProtectedRoute rolesPermitidos={['ADMIN_DTI', 'COORDINADOR_PRACTICAS']} />}>
                            <Route path="/empresas" element={<EmpresasPage />} />
                            <Route path="/tutores"  element={<TutoresPage />} />
                        </Route>

                        {/* ── Vacantes — Proxy backend filtra por rol ────────── */}
                        <Route element={<ProtectedRoute rolesPermitidos={[
                            'ADMIN_DTI', 'COORDINADOR_PRACTICAS', 'COORDINACION_ACADEMICA',
                            'DIRECCION', 'ESTUDIANTE', 'TUTOR_EMPRESARIAL',
                        ]} />}>
                            <Route path="/vacantes" element={<VacantesPage />} />
                        </Route>

                        {/* ── Gestión de Estudiantes (multirrol) ────────────── */}
                        <Route element={<ProtectedRoute rolesPermitidos={[
                            'ADMIN_DTI', 'COORDINACION_ACADEMICA', 'COORDINADOR_PRACTICAS',
                            'DOCENTE_ASESOR', 'TUTOR_EMPRESARIAL', 'DIRECCION',
                        ]} />}>
                            <Route path="/estudiantes" element={<EstudiantesPage />} />
                        </Route>

                        {/* ── Catálogo de Prácticas ──────────────────────────── */}
                        <Route element={<ProtectedRoute rolesPermitidos={[
                            'ADMIN_DTI', 'COORDINACION_ACADEMICA', 'COORDINADOR_PRACTICAS', 'DIRECCION',
                        ]} />}>
                            <Route path="/practicas" element={<CatalogoPracticasPage />} />
                        </Route>

                        {/* ── Coordinador de Prácticas ───────────────────────── */}
                        <Route element={<ProtectedRoute rolesPermitidos={['COORDINADOR_PRACTICAS', 'ADMIN_DTI']} />}>
                            <Route path="/asignaciones" element={<AsignacionesPage />} />
                        </Route>

                        {/* ── Docente Asesor ─────────────────────────────────── */}
                        <Route element={<ProtectedRoute rolesPermitidos={['DOCENTE_ASESOR']} />}>
                            <Route path="/mis-estudiantes"  element={<MisEstudiantesPage />} />
                            <Route path="/seguimientos"     element={<SeguimientosPage />} />
                            <Route path="/sustentaciones"   element={<SustentacionesPage />} />
                        </Route>

                        {/* ── Tutor Empresarial ──────────────────────────────── */}
                        <Route element={<ProtectedRoute rolesPermitidos={['TUTOR_EMPRESARIAL']} />}>
                            <Route path="/mis-practicantes" element={<MisPracticantesPage />} />
                            <Route path="/planes"           element={<PlanesPracticaPage />} />
                            <Route path="/encuestas"        element={<EncuestasPage />} />
                        </Route>

                        {/* ── Estudiante (vista propia) ──────────────────────── */}
                        <Route element={<ProtectedRoute rolesPermitidos={['ESTUDIANTE']} />}>
                            <Route path="/mi-practica"    element={<MiPracticaPage />} />
                            <Route path="/mi-seguimiento" element={<MiSeguimientoPage />} />
                            <Route path="/mis-documentos" element={<MisDocumentosPage />} />
                        </Route>

                        {/* ── Dirección ──────────────────────────────────────── */}
                        <Route element={<ProtectedRoute rolesPermitidos={['DIRECCION', 'ADMIN_DTI']} />}>
                            <Route path="/indicadores" element={<IndicadoresPage />} />
                            <Route path="/reportes"    element={<ReportesPage />} />
                        </Route>
                    </Route>
                </Route>

                {/* Redirección raíz */}
                <Route path="/" element={
                    user ? <Navigate to="/dashboard" replace /> : <Navigate to="/login" replace />
                } />
                <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
        </BrowserRouter>
    )
}
