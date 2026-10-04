import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import Register from './pages/Register';
import Login from './pages/Login';
import ForgotPassword from './pages/ForgotPassword';
import ResetPassword from './pages/ResetPassword';
import Dashboard from './pages/Dashboard';
import Profile from './pages/Profile';
import Quiz from './pages/Quiz';
import Certificates from './pages/Certificates';
import VerifyCertificate from './pages/VerifyCertificate';
import Catalog from './pages/Catalog';
import CourseDetail from './pages/CourseDetail';
import Checkout from './pages/Checkout';
import PurchaseSuccess from './pages/PurchaseSuccess';
import Learn from './pages/Learn';
import AdminPanel from './pages/AdminPanel';
import CourseForm from './pages/CourseForm';
import LessonManager from './pages/LessonManager';
import ProtectedRoute from './components/ProtectedRoute';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Navigate to="/catalog" replace />} />
        <Route path="/register" element={<Register />} />
        <Route path="/login" element={<Login />} />
        <Route path="/forgot-password" element={<ForgotPassword />} />
        <Route path="/reset-password" element={<ResetPassword />} />

        {/* Catálogo y compra (públicos) */}
        <Route path="/catalog" element={<Catalog />} />
        <Route path="/course/:id" element={<CourseDetail />} />
        <Route path="/checkout/:id" element={<Checkout />} />
        <Route path="/purchase-success" element={<PurchaseSuccess />} />

        {/* Verificacion publica de certificados (HU-11) */}
        <Route path="/verificar" element={<VerifyCertificate />} />
        <Route path="/verificar/:code" element={<VerifyCertificate />} />

        {/* Rutas protegidas (estudiante) */}
        <Route path="/dashboard" element={<ProtectedRoute><Dashboard /></ProtectedRoute>} />
        <Route path="/learn/:id" element={<ProtectedRoute><Learn /></ProtectedRoute>} />
        <Route path="/profile" element={<ProtectedRoute><Profile /></ProtectedRoute>} />
        <Route path="/quiz/:id" element={<ProtectedRoute><Quiz /></ProtectedRoute>} />
        <Route path="/certificados" element={<ProtectedRoute><Certificates /></ProtectedRoute>} />

        {/* Panel del Instructor (HU-09) - protegido */}
        <Route path="/admin" element={<ProtectedRoute><AdminPanel /></ProtectedRoute>} />
        <Route path="/admin/courses/new" element={<ProtectedRoute><CourseForm /></ProtectedRoute>} />
        <Route path="/admin/courses/:id/edit" element={<ProtectedRoute><CourseForm /></ProtectedRoute>} />
        <Route path="/admin/courses/:courseId/lessons" element={<ProtectedRoute><LessonManager /></ProtectedRoute>} />

        <Route path="*" element={<Navigate to="/catalog" replace />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
