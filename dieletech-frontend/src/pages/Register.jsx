import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { register } from '../services/authService';

export default function Register() {
  const [form, setForm] = useState({ name: '', email: '', password: '' });
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [showPw, setShowPw] = useState(false);
  const navigate = useNavigate();

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    setMessage('');
    try {
      const msg = await register(form);
      setMessage(typeof msg === 'string' ? msg : 'Registro exitoso.');
    } catch (err) {
      setError(err.response?.data || 'Error al registrarse. Intenta de nuevo.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center px-4">
      <div className="bg-white p-8 rounded-2xl shadow-sm w-full max-w-md border border-gray-100">
        <div className="mb-6">
          <h1 className="text-2xl font-bold text-gray-900">Dieletech</h1>
          <p className="text-sm text-blue-600 font-medium">Tu ruta hacia tu 1° en Tech</p>
        </div>

        <h2 className="text-lg font-semibold text-gray-800 mb-5">Crear cuenta</h2>

        {message && (
          <div className="bg-green-50 border border-green-200 text-green-700 p-4 rounded-lg mb-4 text-sm">
            ✅ {message}
            <button onClick={() => navigate('/login')} className="mt-3 w-full bg-blue-600 text-white py-2 rounded-lg font-medium hover:bg-blue-700 transition">
              Ir a iniciar sesión
            </button>
          </div>
        )}
        {error && (
          <div className="bg-red-50 border border-red-200 text-red-600 p-3 rounded-lg mb-4 text-sm">❌ {error}</div>
        )}

        {!message && (
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Nombre completo</label>
            <input type="text" name="name" value={form.name} onChange={handleChange} placeholder="Diego Betancur" required
              className="w-full border border-gray-300 rounded-lg px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent" />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Correo electrónico</label>
            <input type="email" name="email" value={form.email} onChange={handleChange} placeholder="diego@email.com" required
              className="w-full border border-gray-300 rounded-lg px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent" />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Contraseña</label>
            <div className="relative">
              <input type={showPw ? 'text' : 'password'} name="password" value={form.password} onChange={handleChange} placeholder="Mínimo 6 caracteres" required
                className="w-full border border-gray-300 rounded-lg px-4 py-2.5 pr-16 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent" />
              <button type="button" onClick={() => setShowPw(!showPw)}
                className="absolute inset-y-0 right-0 px-3 text-xs font-medium text-blue-600 hover:text-blue-800">
                {showPw ? 'Ocultar' : 'Ver'}
              </button>
            </div>
          </div>
          <button type="submit" disabled={loading}
            className="w-full bg-blue-600 hover:bg-blue-700 text-white py-2.5 rounded-lg font-medium text-sm transition-colors disabled:opacity-50">
            {loading ? 'Creando cuenta...' : 'Crear cuenta'}
          </button>
        </form>
        )}

        <p className="text-center text-sm text-gray-500 mt-5">
          ¿Ya tienes cuenta?{' '}
          <Link to="/login" className="text-blue-600 hover:underline font-medium">Inicia sesión</Link>
        </p>
      </div>
    </div>
  );
}
