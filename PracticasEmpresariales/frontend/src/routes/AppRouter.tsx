import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import ProtectedRoute from './ProtectedRoute'
import LoginPage from '../pages/auth/LoginPage'
import CambiarPasswordPage from '../pages/auth/CambiarPasswordPage'
import DashboardPage from '../pages/dashboard/DashboardPage'
import UsuariosPage from '../pages/admin-dti/UsuariosPage'
import FacultadesPage from '../pages/admin-dti/FacultadesPage'
import ProgramasPage from '../pages/admin-dti/ProgramasPage'
import AuditoriaPage from '../pages/admin-dti/AuditoriaPage'
import EmpresasPage from '../pages/empresas/EmpresasPage'
import TutoresPage from '../pages/tutor-empresarial/TutoresPage'
import VacantesPage from '../pages/vacantes/VacantesPage'
import NoAutorizadoPage from '../pages/NoAutorizadoPage'
import MainLayout from '../layouts/MainLayout'

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

                        {/* DTI only */}
                        <Route element={<ProtectedRoute rolesPermitidos={['ADMIN_DTI']} />}>
                            <Route path="/usuarios" element={<UsuariosPage />} />
                            <Route path="/facultades" element={<FacultadesPage />} />
                            <Route path="/programas" element={<ProgramasPage />} />
                            <Route path="/auditoria" element={<AuditoriaPage />} />
                        </Route>

                        {/* Gestión Empresarial — Sprint 2 Persona 2 */}
                        <Route element={<ProtectedRoute rolesPermitidos={['ADMIN_DTI', 'COORDINADOR_PRACTICAS']} />}>
                            <Route path="/empresas" element={<EmpresasPage />} />
                            <Route path="/tutores" element={<TutoresPage />} />
                        </Route>

                        {/* Vacantes — visible para varios roles; el Proxy del backend filtra qué se ve */}
                        <Route element={<ProtectedRoute rolesPermitidos={[
                            'ADMIN_DTI', 'COORDINADOR_PRACTICAS', 'COORDINACION_ACADEMICA',
                            'DIRECCION', 'ESTUDIANTE', 'TUTOR_EMPRESARIAL',
                        ]} />}>
                            <Route path="/vacantes" element={<VacantesPage />} />
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
