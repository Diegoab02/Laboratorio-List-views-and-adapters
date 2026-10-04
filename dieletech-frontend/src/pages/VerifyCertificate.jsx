import { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { verifyCertificate } from '../services/courseService';

/**
 * Página pública de verificación (HU-11). No requiere sesión: cualquier
 * empleador puede confirmar un certificado con su código.
 */
export default function VerifyCertificate() {
  const { code: codeParam } = useParams();
  const navigate = useNavigate();

  const [code, setCode] = useState(codeParam || '');
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (codeParam) run(codeParam);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [codeParam]);

  const run = async (value) => {
    const clean = (value || '').trim().toUpperCase();
    if (!clean) {
      setError('Ingresa el código que aparece en el certificado.');
      return;
    }
    setLoading(true);
    setError('');
    setResult(null);
    try {
      setResult(await verifyCertificate(clean));
    } catch {
      setError('No se pudo contactar al servidor de verificación. Intenta más tarde.');
    } finally {
      setLoading(false);
    }
  };

  const submit = (e) => {
    e.preventDefault();
    const clean = code.trim().toUpperCase();
    if (clean && clean !== codeParam) navigate(`/verificar/${encodeURIComponent(clean)}`);
    else run(clean);
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="bg-white border-b border-gray-100">
        <div className="max-w-3xl mx-auto px-4 sm:px-6 py-3 flex items-baseline gap-2">
          <Link to="/catalog" className="text-xl font-extrabold text-gray-900">
            Dieletech
          </Link>
          <span className="text-xs text-blue-500 hidden sm:inline">Verificación de certificados</span>
        </div>
      </header>

      <main className="max-w-2xl mx-auto px-4 sm:px-6 py-8 sm:py-12">
        <h1 className="text-2xl font-bold text-gray-900 mb-1">Verificar un certificado</h1>
        <p className="text-sm text-gray-500 mb-6">
          Ingresa el código impreso en el certificado para confirmar su autenticidad. No necesitas
          una cuenta.
        </p>

        <form onSubmit={submit} className="flex flex-col sm:flex-row gap-2 mb-6">
          <input
            value={code}
            onChange={(e) => {
              setCode(e.target.value.toUpperCase());
              setError('');
            }}
            placeholder="DTC-XXXX-XXXX-XXXX"
            className="flex-1 border border-gray-300 rounded-lg px-4 py-2.5 font-mono text-sm tracking-wide focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
          <button
            type="submit"
            disabled={loading}
            className="bg-blue-600 text-white px-6 py-2.5 rounded-lg font-semibold text-sm hover:bg-blue-700 disabled:opacity-50 transition"
          >
            {loading ? 'Verificando...' : 'Verificar'}
          </button>
        </form>

        {error && (
          <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg text-sm">
            {error}
          </div>
        )}

        {result && !result.valid && (
          <div className="bg-white rounded-2xl border border-red-200 p-6 sm:p-8 text-center">
            <div className="text-4xl mb-3">⚠️</div>
            <h2 className="font-bold text-gray-900 mb-1">Certificado no válido</h2>
            <p className="text-sm text-gray-500">{result.message}</p>
            <p className="font-mono text-xs text-gray-400 mt-3 break-all">{result.code}</p>
          </div>
        )}

        {result && result.valid && (
          <div className="bg-white rounded-2xl border border-emerald-200 overflow-hidden">
            <div className="bg-emerald-600 text-white px-6 py-4 flex items-center gap-3">
              <span className="text-2xl">✓</span>
              <div>
                <p className="font-bold">Certificado válido</p>
                <p className="text-emerald-50 text-xs">{result.message}</p>
              </div>
            </div>

            <div className="p-6 space-y-4">
              <div>
                <p className="text-xs text-gray-400 uppercase tracking-wide font-semibold mb-0.5">
                  Otorgado a
                </p>
                <p className="text-xl font-bold text-gray-900">{result.studentName}</p>
              </div>

              <div>
                <p className="text-xs text-gray-400 uppercase tracking-wide font-semibold mb-0.5">
                  Curso completado
                </p>
                <p className="text-base font-semibold text-gray-800">{result.courseTitle}</p>
              </div>

              <dl className="grid grid-cols-2 sm:grid-cols-3 gap-4 pt-2 border-t border-gray-100">
                <div>
                  <dt className="text-xs text-gray-400">Duración</dt>
                  <dd className="text-sm font-medium text-gray-800">{result.courseHours} horas</dd>
                </div>
                <div>
                  <dt className="text-xs text-gray-400">Calificación</dt>
                  <dd className="text-sm font-medium text-gray-800">{result.score}%</dd>
                </div>
                <div>
                  <dt className="text-xs text-gray-400">Fecha de emisión</dt>
                  <dd className="text-sm font-medium text-gray-800">{result.issuedAt}</dd>
                </div>
                {result.instructorName && (
                  <div className="col-span-2 sm:col-span-3">
                    <dt className="text-xs text-gray-400">Instructor</dt>
                    <dd className="text-sm font-medium text-gray-800">{result.instructorName}</dd>
                  </div>
                )}
              </dl>

              <div className="pt-3 border-t border-gray-100">
                <p className="text-xs text-gray-400">Código</p>
                <p className="font-mono text-sm font-bold text-emerald-700 break-all">{result.code}</p>
              </div>
            </div>
          </div>
        )}

        <p className="text-xs text-gray-400 text-center mt-8">
          Los certificados de Dieletech se emiten únicamente tras aprobar la evaluación final del
          curso con un mínimo del 70%.
        </p>
      </main>
    </div>
  );
}
