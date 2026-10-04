import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { forgotPassword } from '../services/authService';

export default function ForgotPassword() {
  const [email, setEmail] = useState('');
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [sent, setSent] = useState(false);
  const navigate = useNavigate();

  const emailValid = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.trim());

  const submit = async (e) => {
    e.preventDefault();
    if (!emailValid) {
      setError('Ingresa un correo electrónico válido');
      return;
    }
    setLoading(true);
    setError('');
    setMessage('');
    try {
      const msg = await forgotPassword(email.trim().toLowerCase());
      setMessage(typeof msg === 'string' ? msg : 'Revisa tu correo.');
      setSent(true);
    } catch (err) {
      setError(err.response?.data || 'Error. Intenta de nuevo.');
    } finally {
      setLoading(false);
    }
  };

  const goToReset = () =>
    navigate(`/reset-password?email=${encodeURIComponent(email.trim().toLowerCase())}`);

  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center px-4">
      <div className="bg-white p-8 rounded-2xl shadow-sm w-full max-w-md border border-gray-100">
        <div className="mb-6">
          <h1 className="text-2xl font-bold text-gray-900">Dieletech</h1>
          <p className="text-sm text-blue-600 font-medium">Recuperar contraseña</p>
        </div>

        {message && (
          <div className="bg-green-50 border border-green-200 text-green-700 p-3 rounded-lg mb-4 text-sm">
            ✅ {message}
          </div>
        )}
        {error && (
          <div className="bg-red-50 border border-red-200 text-red-600 p-3 rounded-lg mb-4 text-sm">
            ❌ {error}
          </div>
        )}

        <p className="text-sm text-gray-600 mb-4">
          Ingresa tu correo y te enviaremos un <strong>código de 6 dígitos</strong> para crear una
          nueva contraseña. El código expira en 30 minutos.
        </p>

        <form onSubmit={submit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Correo electrónico
            </label>
            <input
              type="email"
              value={email}
              onChange={(e) => {
                setEmail(e.target.value);
                setError('');
              }}
              placeholder="diego@email.com"
              required
              className="w-full border border-gray-300 rounded-lg px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            />
          </div>

          <button
            type="submit"
            disabled={loading || !emailValid}
            className="w-full bg-blue-600 hover:bg-blue-700 text-white py-2.5 rounded-lg font-medium text-sm transition-colors disabled:opacity-50"
          >
            {loading ? 'Enviando...' : sent ? 'Reenviar código' : 'Enviar código'}
          </button>
        </form>

        {sent && (
          <button
            onClick={goToReset}
            className="w-full mt-3 bg-gray-900 hover:bg-gray-800 text-white py-2.5 rounded-lg font-medium text-sm transition-colors"
          >
            Ya tengo el código →
          </button>
        )}

        <p className="text-center text-sm text-gray-500 mt-5">
          <Link to="/login" className="text-blue-600 hover:underline font-medium">
            Volver a iniciar sesión
          </Link>
        </p>
      </div>
    </div>
  );
}
