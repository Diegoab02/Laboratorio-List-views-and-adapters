import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar';
import { getMyCertificates, downloadCertificate, errorMessage } from '../services/courseService';

export default function Certificates() {
  const navigate = useNavigate();
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(null);
  const [copied, setCopied] = useState(null);

  useEffect(() => {
    load();
  }, []);

  const load = async () => {
    setLoading(true);
    setError('');
    try {
      setItems(await getMyCertificates());
    } catch (e) {
      setError(errorMessage(e, 'No se pudieron cargar tus certificados.'));
    } finally {
      setLoading(false);
    }
  };

  const download = async (code) => {
    setBusy(code);
    setError('');
    try {
      await downloadCertificate(code);
    } catch (e) {
      setError(errorMessage(e, 'No se pudo generar el PDF.'));
    } finally {
      setBusy(null);
    }
  };

  const copy = async (text, code) => {
    try {
      await navigator.clipboard.writeText(text);
      setCopied(code);
      setTimeout(() => setCopied(null), 2000);
    } catch {
      setError('Tu navegador bloqueó el portapapeles. Copia el enlace manualmente.');
    }
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <main className="max-w-4xl mx-auto px-4 sm:px-6 py-8 sm:py-10">
        <h1 className="text-2xl font-bold text-gray-900 mb-1">Mis certificados</h1>
        <p className="text-gray-500 text-sm mb-7">
          Cada certificado lleva un código único que cualquier persona puede verificar sin necesidad
          de una cuenta.
        </p>

        {error && (
          <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg mb-5 text-sm">
            {error}
          </div>
        )}

        {loading && (
          <div className="flex justify-center py-20">
            <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-blue-600" />
          </div>
        )}

        {!loading && items.length === 0 && (
          <div className="bg-white border border-gray-100 rounded-2xl p-10 sm:p-12 text-center">
            <div className="text-5xl mb-4">🎓</div>
            <h2 className="font-semibold text-gray-800 mb-2">Todavía no tienes certificados</h2>
            <p className="text-gray-500 text-sm mb-6 max-w-md mx-auto">
              Completa todas las lecciones de un curso y aprueba su evaluación final con al menos
              70% para obtener tu certificado.
            </p>
            <button
              onClick={() => navigate('/dashboard')}
              className="bg-blue-600 text-white px-6 py-2.5 rounded-lg font-medium hover:bg-blue-700 transition"
            >
              Ir a mis cursos
            </button>
          </div>
        )}

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-5">
          {items.map((c) => (
            <div
              key={c.code}
              className="bg-white rounded-2xl border border-gray-100 overflow-hidden flex flex-col"
            >
              <div className="bg-gradient-to-br from-emerald-600 to-teal-700 text-white p-5">
                <div className="flex items-start justify-between gap-3 mb-3">
                  <span className="text-[10px] font-bold tracking-widest uppercase text-white/70">
                    Certificado de finalización
                  </span>
                  <span className="text-xs bg-white/20 px-2 py-0.5 rounded-full font-semibold shrink-0">
                    {c.score}%
                  </span>
                </div>
                <h2 className="text-lg font-bold leading-snug">{c.courseTitle}</h2>
                <p className="text-white/80 text-sm mt-0.5">{c.studentName}</p>
              </div>

              <div className="p-5 flex-1 flex flex-col">
                <dl className="grid grid-cols-2 gap-3 text-sm mb-4">
                  <div>
                    <dt className="text-xs text-gray-400">Duración</dt>
                    <dd className="text-gray-800 font-medium">{c.courseHours} horas</dd>
                  </div>
                  <div>
                    <dt className="text-xs text-gray-400">Emitido</dt>
                    <dd className="text-gray-800 font-medium">{c.issuedAt}</dd>
                  </div>
                  {c.instructorName && (
                    <div className="col-span-2">
                      <dt className="text-xs text-gray-400">Instructor</dt>
                      <dd className="text-gray-800 font-medium">{c.instructorName}</dd>
                    </div>
                  )}
                </dl>

                <div className="bg-gray-50 rounded-xl p-3 mb-4">
                  <p className="text-xs text-gray-400 mb-1">Código de verificación</p>
                  <p className="font-mono text-sm font-bold text-emerald-700 tracking-wide break-all">
                    {c.code}
                  </p>
                </div>

                <div className="flex flex-col sm:flex-row gap-2 mt-auto">
                  <button
                    onClick={() => download(c.code)}
                    disabled={busy === c.code}
                    className="flex-1 bg-gray-900 text-white px-4 py-2.5 rounded-lg font-semibold text-sm hover:bg-gray-800 disabled:opacity-50 transition"
                  >
                    {busy === c.code ? 'Generando...' : 'Descargar PDF'}
                  </button>
                  <button
                    onClick={() => copy(c.verifyUrl, c.code)}
                    className="flex-1 border border-gray-200 text-gray-700 px-4 py-2.5 rounded-lg font-semibold text-sm hover:bg-gray-50 transition"
                  >
                    {copied === c.code ? 'Enlace copiado' : 'Copiar enlace'}
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>
      </main>
    </div>
  );
}
