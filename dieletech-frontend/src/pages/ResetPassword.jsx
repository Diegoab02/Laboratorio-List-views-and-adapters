import { useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { resetPassword, forgotPassword } from '../services/authService';

export default function ResetPassword() {
  const [params] = useSearchParams();
  const emailFromQuery = params.get('email') || '';
  // Compatibilidad: si alguien llega con ?token= lo precargamos como codigo
  const [code, setCode] = useState(params.get('token') || '');
  const [password, setPassword] = useState('');
  const [confirm, setConfirm] = useState('');
  const [show, setShow] = useState(false);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [resending, setResending] = useState(false);
  const navigate = useNavigate();

  const codeValid = /^\d{6}$/.test(code);
  const passwordValid = password.length >= 6;
  const confirmValid = confirm.length > 0 && confirm === password;
  const formValid = codeValid && passwordValid && confirmValid;

  const submit = async (e) => {
    e.preventDefault();
    if (!codeValid) {
      setError('El código debe tener exactamente 6 dígitos');
      return;
    }
    if (!passwordValid) {
      setError('La contraseña debe tener mínimo 6 caracteres');
      return;
    }
    if (!confirmValid) {
      setError('Las contraseñas no coinciden');
      return;
    }
    setLoading(true);
    setError('');
    setMessage('');
    try {
      const msg = await resetPassword(code.trim(), password);
      setMessage(typeof msg === 'string' ? msg : 'Contraseña actualizada.');
    } catch (err) {
      setError(err.response?.data || 'Código inválido o expirado. Solicita uno nuevo.');
    } finally {
      setLoading(false);
    }
  };

  const resend = async () => {
    if (!emailFromQuery) {
      navigate('/forgot-password');
      return;
    }
    setResending(true);
    setError('');
    try {
      await forgotPassword(emailFromQuery);
      setMessage('Enviamos un código nuevo a tu correo.');
    } catch {
      setError('No se pudo reenviar el código.');
    } finally {
      setResending(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center px-4">
      <div className="bg-white p-8 rounded-2xl shadow-sm w-full max-w-md border border-gray-100">
        <div className="mb-6">
          <h1 className="text-2xl font-bold text-gray-900">Dieletech</h1>
          <p className="text-sm text-blue-600 font-medium">Nueva contraseña</p>
        </div>

        {message && !error ? (
          <div className="bg-green-50 border border-green-200 text-green-700 p-4 rounded-lg text-sm">
            ✅ {message}
            <button
              onClick={() => navigate('/login')}
              className="mt-3 w-full bg-blue-600 text-white py-2 rounded-lg font-medium hover:bg-blue-700 transition"
            >
              Ir a iniciar sesión
            </button>
          </div>
        ) : (
          <>
            <p className="text-sm text-gray-600 mb-4">
              Ingresa el código de 6 dígitos que enviamos
              {emailFromQuery ? (
                <>
                  {' '}
                  a <strong>{emailFromQuery}</strong>
                </>
              ) : (
                ' a tu correo'
              )}{' '}
              y elige tu nueva contraseña.
            </p>

            {error && (
              <div className="bg-red-50 border border-red-200 text-red-600 p-3 rounded-lg mb-4 text-sm">
                ❌ {error}
              </div>
            )}

            <form onSubmit={submit} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Código de verificación
                </label>
                <input
                  type="text"
                  inputMode="numeric"
                  value={code}
                  onChange={(e) => {
                    const v = e.target.value.replace(/\D/g, '').slice(0, 6);
                    setCode(v);
                    setError('');
                  }}
                  placeholder="000000"
                  required
                  className="w-full border border-gray-300 rounded-lg px-4 py-2.5 text-center text-2xl tracking-[0.5em] font-mono focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
                <p className="text-xs text-gray-400 mt-1 text-right">{code.length}/6 dígitos</p>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Nueva contraseña
                </label>
                <div className="relative">
                  <input
                    type={show ? 'text' : 'password'}
                    value={password}
                    onChange={(e) => {
                      setPassword(e.target.value);
                      setError('');
                    }}
                    placeholder="Mínimo 6 caracteres"
                    required
                    className="w-full border border-gray-300 rounded-lg px-4 py-2.5 pr-16 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  />
                  <button
                    type="button"
                    onClick={() => setShow(!show)}
                    className="absolute inset-y-0 right-0 px-3 text-xs font-medium text-blue-600 hover:text-blue-800"
                  >
                    {show ? 'Ocultar' : 'Ver'}
                  </button>
                </div>
                {password.length > 0 && !passwordValid && (
                  <p className="text-xs text-red-500 mt-1">Mínimo 6 caracteres</p>
                )}
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Confirmar contraseña
                </label>
                <input
                  type={show ? 'text' : 'password'}
                  value={confirm}
                  onChange={(e) => {
                    setConfirm(e.target.value);
                    setError('');
                  }}
                  placeholder="Repite la contraseña"
                  required
                  className="w-full border border-gray-300 rounded-lg px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
                {confirm.length > 0 && !confirmValid && (
                  <p className="text-xs text-red-500 mt-1">Las contraseñas no coinciden</p>
                )}
              </div>

              <button
                type="submit"
                disabled={loading || !formValid}
                className="w-full bg-blue-600 hover:bg-blue-700 text-white py-2.5 rounded-lg font-medium text-sm transition-colors disabled:opacity-50"
              >
                {loading ? 'Guardando...' : 'Cambiar contraseña'}
              </button>
            </form>

            <button
              onClick={resend}
              disabled={resending}
              className="w-full mt-3 text-sm text-gray-600 hover:text-blue-600 transition disabled:opacity-50"
            >
              {resending ? 'Reenviando...' : 'No recibí el código. Reenviar'}
            </button>
          </>
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
