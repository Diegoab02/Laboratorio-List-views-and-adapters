import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar';
import { getQuiz, submitQuiz, downloadCertificate, errorMessage } from '../services/courseService';

export default function Quiz() {
  const { id } = useParams();
  const navigate = useNavigate();

  const [info, setInfo] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const [started, setStarted] = useState(false);
  const [current, setCurrent] = useState(0);
  const [answers, setAnswers] = useState({});
  const [submitting, setSubmitting] = useState(false);
  const [result, setResult] = useState(null);
  const [downloading, setDownloading] = useState(false);

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  const load = async () => {
    setLoading(true);
    setError('');
    try {
      setInfo(await getQuiz(id));
    } catch (e) {
      setError(errorMessage(e, 'No se pudo cargar la evaluación.'));
    } finally {
      setLoading(false);
    }
  };

  const questions = info?.questions || [];
  const answered = Object.keys(answers).length;
  const allAnswered = questions.length > 0 && answered === questions.length;

  const pick = (questionId, index) => {
    setAnswers((a) => ({ ...a, [questionId]: index }));
    setError('');
  };

  const send = async () => {
    if (!allAnswered) {
      setError(`Responde las ${questions.length} preguntas antes de enviar.`);
      return;
    }
    setSubmitting(true);
    setError('');
    try {
      const payload = Object.entries(answers).map(([questionId, selectedIndex]) => ({
        questionId: Number(questionId),
        selectedIndex,
      }));
      const r = await submitQuiz(id, payload);
      setResult(r);
      window.scrollTo({ top: 0, behavior: 'smooth' });
    } catch (e) {
      setError(errorMessage(e, 'No se pudo enviar la evaluación.'));
    } finally {
      setSubmitting(false);
    }
  };

  const retry = () => {
    setResult(null);
    setAnswers({});
    setCurrent(0);
    setStarted(false);
    load();
  };

  const getCertificate = async () => {
    setDownloading(true);
    try {
      await downloadCertificate(result.certificateCode);
    } catch (e) {
      setError(errorMessage(e, 'No se pudo descargar el certificado.'));
    } finally {
      setDownloading(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50">
        <Navbar />
        <div className="flex justify-center py-32">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600" />
        </div>
      </div>
    );
  }

  // ── Resultado ──
  if (result) {
    const ok = result.passed;
    return (
      <div className="min-h-screen bg-gray-50">
        <Navbar />
        <main className="max-w-3xl mx-auto px-4 sm:px-6 py-8">
          <div
            className={`rounded-2xl p-6 sm:p-8 text-center text-white mb-6 ${
              ok ? 'bg-gradient-to-br from-emerald-600 to-teal-700'
                 : 'bg-gradient-to-br from-slate-600 to-slate-800'
            }`}
          >
            <div className="text-5xl mb-3">{ok ? '🎓' : '📘'}</div>
            <h1 className="text-2xl sm:text-3xl font-bold mb-1">
              {ok ? '¡Aprobaste!' : 'Casi lo logras'}
            </h1>
            <p className="text-white/85 text-sm mb-5">{result.message}</p>

            <div className="flex justify-center gap-6 sm:gap-10">
              <div>
                <p className="text-3xl font-bold">{result.score}%</p>
                <p className="text-xs text-white/70">Tu puntaje</p>
              </div>
              <div>
                <p className="text-3xl font-bold">
                  {result.correctAnswers}/{result.totalQuestions}
                </p>
                <p className="text-xs text-white/70">Correctas</p>
              </div>
              <div>
                <p className="text-3xl font-bold">{result.passingScore}%</p>
                <p className="text-xs text-white/70">Mínimo</p>
              </div>
            </div>
          </div>

          {ok && result.certificateCode && (
            <div className="bg-white rounded-2xl border border-emerald-200 p-6 mb-6 text-center">
              <h2 className="font-bold text-gray-900 mb-1">Tu certificado está listo</h2>
              <p className="text-sm text-gray-500 mb-1">Código de verificación</p>
              <p className="font-mono text-lg font-bold text-emerald-700 tracking-wide mb-4 break-all">
                {result.certificateCode}
              </p>
              <div className="flex flex-col sm:flex-row gap-2 justify-center">
                <button
                  onClick={getCertificate}
                  disabled={downloading}
                  className="bg-emerald-600 text-white px-6 py-2.5 rounded-lg font-semibold text-sm hover:bg-emerald-700 disabled:opacity-50 transition"
                >
                  {downloading ? 'Generando PDF...' : 'Descargar certificado'}
                </button>
                <button
                  onClick={() => navigate('/certificados')}
                  className="border border-gray-200 text-gray-700 px-6 py-2.5 rounded-lg font-semibold text-sm hover:bg-gray-50 transition"
                >
                  Ver mis certificados
                </button>
              </div>
            </div>
          )}

          <div className="bg-white rounded-2xl border border-gray-100 p-5 sm:p-6">
            <h2 className="font-bold text-gray-900 mb-4">Revisión de tus respuestas</h2>
            <div className="space-y-5">
              {result.results.map((r, i) => (
                <div key={r.questionId} className="pb-5 border-b border-gray-50 last:border-0 last:pb-0">
                  <div className="flex items-start gap-2 mb-3">
                    <span
                      className={`text-xs font-bold px-2 py-1 rounded shrink-0 ${
                        r.correct ? 'bg-emerald-100 text-emerald-700' : 'bg-red-100 text-red-700'
                      }`}
                    >
                      {r.correct ? '✓' : '✕'} {i + 1}
                    </span>
                    <p className="text-sm font-medium text-gray-900">{r.text}</p>
                  </div>

                  <div className="space-y-1.5 ml-1">
                    {r.options.map((opt, oi) => {
                      const isCorrect = oi === r.correctIndex;
                      const isMine = oi === r.selectedIndex;
                      return (
                        <div
                          key={oi}
                          className={`text-sm px-3 py-2 rounded-lg border ${
                            isCorrect
                              ? 'border-emerald-300 bg-emerald-50 text-emerald-900'
                              : isMine
                                ? 'border-red-300 bg-red-50 text-red-900'
                                : 'border-gray-100 text-gray-500'
                          }`}
                        >
                          {opt}
                          {isCorrect && <span className="text-xs font-semibold ml-2">correcta</span>}
                          {isMine && !isCorrect && (
                            <span className="text-xs font-semibold ml-2">tu respuesta</span>
                          )}
                        </div>
                      );
                    })}
                  </div>

                  {r.explanation && (
                    <p className="text-xs text-gray-500 mt-2 ml-1 italic">{r.explanation}</p>
                  )}
                </div>
              ))}
            </div>
          </div>

          <div className="flex flex-col sm:flex-row gap-2 mt-6">
            {!ok && (
              <button
                onClick={retry}
                className="bg-blue-600 text-white px-6 py-2.5 rounded-lg font-semibold text-sm hover:bg-blue-700 transition"
              >
                Intentar de nuevo
              </button>
            )}
            <button
              onClick={() => navigate(`/learn/${id}`)}
              className="border border-gray-200 text-gray-700 px-6 py-2.5 rounded-lg font-semibold text-sm hover:bg-gray-50 transition"
            >
              Volver al curso
            </button>
          </div>
        </main>
      </div>
    );
  }

  // ── No disponible ──
  if (!info?.available) {
    return (
      <div className="min-h-screen bg-gray-50">
        <Navbar />
        <main className="max-w-lg mx-auto px-4 sm:px-6 py-16">
          <div className="bg-white rounded-2xl border border-gray-100 p-8 text-center">
            <div className="text-5xl mb-4">🔒</div>
            <h1 className="text-xl font-bold text-gray-900 mb-2">Evaluación no disponible</h1>
            <p className="text-sm text-gray-500 mb-5">
              {info?.blockedReason || error || 'No puedes presentar esta evaluación todavía.'}
            </p>

            {info?.totalLessons > 0 && (
              <div className="mb-6">
                <div className="bg-gray-100 rounded-full h-2">
                  <div
                    className="bg-blue-500 h-2 rounded-full transition-all"
                    style={{ width: `${(info.lessonsCompleted / info.totalLessons) * 100}%` }}
                  />
                </div>
                <p className="text-xs text-gray-400 mt-1.5">
                  {info.lessonsCompleted} de {info.totalLessons} lecciones completadas
                </p>
              </div>
            )}

            <button
              onClick={() => navigate(`/learn/${id}`)}
              className="bg-blue-600 text-white px-6 py-2.5 rounded-lg font-semibold text-sm hover:bg-blue-700 transition"
            >
              Ir al curso
            </button>
          </div>
        </main>
      </div>
    );
  }

  // ── Portada de la evaluación ──
  if (!started) {
    return (
      <div className="min-h-screen bg-gray-50">
        <Navbar />
        <main className="max-w-2xl mx-auto px-4 sm:px-6 py-10">
          <div className="bg-white rounded-2xl border border-gray-100 p-6 sm:p-8">
            <p className="text-xs font-semibold text-blue-600 uppercase tracking-wide mb-1">
              Evaluación final
            </p>
            <h1 className="text-2xl font-bold text-gray-900 mb-1">{info.courseTitle}</h1>
            <p className="text-sm text-gray-500 mb-6">
              Completaste las {info.totalLessons} lecciones. Esta es la última etapa para obtener tu
              certificado.
            </p>

            <div className="grid grid-cols-3 gap-3 mb-6">
              {[
                [info.totalQuestions, 'Preguntas'],
                [`${info.passingScore}%`, 'Para aprobar'],
                [info.attempts, info.attempts === 1 ? 'Intento previo' : 'Intentos previos'],
              ].map(([v, l]) => (
                <div key={l} className="bg-gray-50 rounded-xl p-3 text-center">
                  <p className="text-2xl font-bold text-gray-900">{v}</p>
                  <p className="text-xs text-gray-500">{l}</p>
                </div>
              ))}
            </div>

            {info.attempts > 0 && (
              <div className="bg-blue-50 rounded-xl p-4 mb-6 text-sm text-gray-700">
                Tu mejor puntaje hasta ahora es <strong>{info.bestScore}%</strong>.
                {info.passed && ' Ya aprobaste este curso, pero puedes volver a presentarla.'}
              </div>
            )}

            <ul className="text-sm text-gray-600 space-y-1.5 mb-7">
              <li>• Debes responder todas las preguntas para poder enviar.</li>
              <li>• No hay límite de tiempo ni de intentos.</li>
              <li>• Al terminar verás la respuesta correcta de cada pregunta.</li>
              <li>• Con {info.passingScore}% o más se emite tu certificado automáticamente.</li>
            </ul>

            <button
              onClick={() => setStarted(true)}
              className="w-full bg-blue-600 text-white py-3 rounded-lg font-bold hover:bg-blue-700 transition"
            >
              Comenzar evaluación
            </button>
          </div>
        </main>
      </div>
    );
  }

  // ── Evaluación en curso ──
  const q = questions[current];
  const selected = answers[q.id];

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <main className="max-w-2xl mx-auto px-4 sm:px-6 py-8">
        <div className="flex items-center justify-between text-sm mb-2">
          <span className="font-semibold text-gray-700">
            Pregunta {current + 1} de {questions.length}
          </span>
          <span className="text-gray-400">{answered} respondidas</span>
        </div>
        <div className="bg-gray-200 rounded-full h-1.5 mb-6">
          <div
            className="bg-blue-500 h-1.5 rounded-full transition-all"
            style={{ width: `${((current + 1) / questions.length) * 100}%` }}
          />
        </div>

        {error && (
          <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg mb-4 text-sm">
            {error}
          </div>
        )}

        <div className="bg-white rounded-2xl border border-gray-100 p-5 sm:p-6 mb-4">
          <p className="text-base font-medium text-gray-900 mb-5">{q.text}</p>
          <div className="space-y-2">
            {q.options.map((opt, i) => (
              <button
                key={i}
                onClick={() => pick(q.id, i)}
                className={`w-full text-left px-4 py-3 rounded-xl border-2 text-sm transition ${
                  selected === i
                    ? 'border-blue-500 bg-blue-50 text-blue-900 font-medium'
                    : 'border-gray-200 hover:border-gray-300 text-gray-700'
                }`}
              >
                <span
                  className={`inline-flex items-center justify-center w-6 h-6 rounded-full border-2 mr-3 text-xs font-bold align-middle ${
                    selected === i ? 'border-blue-500 text-blue-600' : 'border-gray-300 text-gray-400'
                  }`}
                >
                  {String.fromCharCode(65 + i)}
                </span>
                {opt}
              </button>
            ))}
          </div>
        </div>

        <div className="flex flex-wrap gap-2 mb-5">
          {questions.map((qq, i) => (
            <button
              key={qq.id}
              onClick={() => setCurrent(i)}
              className={`w-8 h-8 rounded-lg text-xs font-bold transition ${
                i === current
                  ? 'bg-blue-600 text-white'
                  : answers[qq.id] !== undefined
                    ? 'bg-emerald-100 text-emerald-700'
                    : 'bg-white border border-gray-200 text-gray-400'
              }`}
            >
              {i + 1}
            </button>
          ))}
        </div>

        <div className="flex gap-2">
          <button
            onClick={() => setCurrent((c) => Math.max(0, c - 1))}
            disabled={current === 0}
            className="px-5 py-2.5 rounded-lg font-semibold text-sm border border-gray-200 text-gray-600 hover:bg-gray-50 disabled:opacity-40 transition"
          >
            Anterior
          </button>

          {current < questions.length - 1 ? (
            <button
              onClick={() => setCurrent((c) => c + 1)}
              className="flex-1 bg-gray-900 text-white px-5 py-2.5 rounded-lg font-semibold text-sm hover:bg-gray-800 transition"
            >
              Siguiente
            </button>
          ) : (
            <button
              onClick={send}
              disabled={submitting || !allAnswered}
              className="flex-1 bg-blue-600 text-white px-5 py-2.5 rounded-lg font-semibold text-sm hover:bg-blue-700 disabled:bg-gray-300 disabled:cursor-not-allowed transition"
            >
              {submitting
                ? 'Enviando...'
                : allAnswered
                  ? 'Enviar evaluación'
                  : `Faltan ${questions.length - answered}`}
            </button>
          )}
        </div>
      </main>
    </div>
  );
}
