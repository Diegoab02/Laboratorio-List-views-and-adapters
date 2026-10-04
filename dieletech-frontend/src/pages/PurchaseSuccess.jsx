import { useLocation, useNavigate } from 'react-router-dom';

export default function PurchaseSuccess() {
  const location = useLocation();
  const navigate = useNavigate();
  const { courseName = 'Curso', courseId } = location.state || {};

  return (
    <div className="min-h-screen bg-gradient-to-b from-green-50 to-white flex items-center justify-center px-4">
      <div className="bg-white rounded-lg shadow-2xl p-8 max-w-md text-center">
        <div className="mb-6">
          <div className="mx-auto flex items-center justify-center h-16 w-16 rounded-full bg-green-100">
            <svg className="h-8 w-8 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
            </svg>
          </div>
        </div>

        <h1 className="text-3xl font-bold text-gray-800 mb-2">¡Compra Exitosa!</h1>
        <p className="text-gray-600 mb-6">Tu pago ha sido procesado correctamente.</p>

        <div className="bg-blue-50 rounded-lg p-4 mb-6">
          <p className="text-lg font-semibold text-blue-900">{courseName}</p>
          <p className="text-sm text-blue-700 mt-1">ID: {courseId}</p>
        </div>

        <div className="space-y-3 mb-8 text-sm text-gray-700">
          <p>✓ Acceso inmediato al contenido</p>
          <p>✓ Acceso de por vida</p>
          <p>✓ Certificado de finalización</p>
          <p>✓ Soporte en comunidad</p>
        </div>

        <div className="space-y-3">
          <button onClick={() => navigate('/catalog')} className="w-full bg-blue-600 text-white py-3 rounded-lg font-bold hover:bg-blue-700">
            Explorar Más Cursos
          </button>
          <button onClick={() => navigate('/dashboard')} className="w-full bg-gray-200 text-gray-700 py-3 rounded-lg font-bold hover:bg-gray-300">
            Ir a Mi Dashboard
          </button>
        </div>
      </div>
    </div>
  );
}
