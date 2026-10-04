import { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar';
import {
  getCourseById,
  getLessons,
  updateLessonProgress,
  getCourseProgress,
  getMyCourseAssignments,
  saveSubmission,
  errorMessage,
} from '../services/courseService';
import { getUser } from '../services/authService';

/* ════════════════════════════════════════════════════════
   HU-38 · Entrega del ejercicio practico del modulo

   El servidor decide si se puede entregar y por que no: este
   panel solo pinta canSubmit y blockedReason. No duplica las
   reglas, porque duplicarlas es como se desincronizan.
   ════════════════════════════════════════════════════════ */

const TYPE_HINT = {
  CODE: 'Pega tu codigo. Incluye la salida que viste al ejecutarlo.',
  FILE: 'Describe tu entrega y pega el enlace a tu archivo o repositorio.',
  TEXT: 'Redacta tu respuesta.',
};

const STATUS_BADGE = {
  DRAFT: ['Borrador guardado', 'bg-amber-500/15 text-amber-300'],
  SUBMITTED: ['Entregada', 'bg-blue-500/15 text-blue-300'],
  GRADED: ['Calificada', 'bg-emerald-500/15 text-emerald-300'],
};

function AssignmentPanel({ view, onSaved }) {
  const { assignment, submission, canSubmit, blockedReason, dueDate, daysLeft } = view;
  const graded = submission?.status === 'GRADED';

  const [content, setContent] = useState(submission?.content ?? '');
  const [fileUrl, setFileUrl] = useState(submission?.fileUrl ?? '');
  const [busy, setBusy] = useState(false);
  const [msg, setMsg] = useState('');
  const [err, setErr] = useState('');

  useEffect(() => {
    setContent(submission?.content ?? '');
    setFileUrl(submission?.fileUrl ?? '');
    setMsg('');
    setErr('');
  }, [assignment.id, submission?.id, submission?.status]);

  const send = async (draft) => {
    setBusy(true);
    setMsg('');
    setErr('');
    try {
      await saveSubmission(assignment.id, { content, fileUrl, draft });
      setMsg(draft ? 'Borrador guardado.' : 'Entrega registrada.');
      await onSaved();
    } catch (e) {
      setErr(errorMessage(e, 'No se pudo guardar la entrega.'));
    } finally {
      setBusy(false);
    }
  };

  const badge = submission ? STATUS_BADGE[submission.status] : null;

  return (
    <div className="mt-6 bg-gray-900/60 border border-gray-700 rounded-xl overflow-hidden">
      <div className="px-5 py-4 border-b border-gray-700 flex flex-wrap items-center gap-2">
        <span className="text-xs font-semibold uppercase tracking-wide text-blue-400">
          Ejercicio practico
        </span>
        <h3 className="text-white font-semibold">{assignment.title}</h3>
        {badge && (
          <span className={`text-xs px-2 py-0.5 rounded-full font-medium ${badge[1]}`}>{badge[0]}</span>
        )}
        {submission?.late && (
          <span className="text-xs px-2 py-0.5 rounded-full font-medium bg-red-500/15 text-red-300">
            Fuera de plazo
          </span>
        )}
        <span className="text-xs text-gray-500 ml-auto">{assignment.maxScore} puntos</span>
      </div>

      <div className="px-5 py-4 space-y-4">
        {assignment.statement ? (
          <p className="text-gray-300 text-sm whitespace-pre-line leading-relaxed">
            {assignment.statement}
          </p>
        ) : (
          <p className="text-gray-500 text-sm">
            Compra el curso para ver el enunciado de este ejercicio.
          </p>
        )}

        {dueDate && (
          <p className="text-xs text-gray-500">
            Plazo: {new Date(dueDate).toLocaleDateString('es-CO')}
            {typeof daysLeft === 'number' && !graded && (
              daysLeft >= 0
                ? ` · te quedan ${daysLeft} dia(s)`
                : ` · vencio hace ${Math.abs(daysLeft)} dia(s), aun puedes entregar`
            )}
          </p>
        )}

        {graded && (
          <div className="bg-emerald-500/10 border border-emerald-500/30 rounded-lg px-4 py-3">
            <p className="text-emerald-300 font-semibold text-sm">
              Nota: {submission.score} / {assignment.maxScore}
            </p>
            {submission.feedback && (
              <p className="text-gray-300 text-sm mt-1 whitespace-pre-line">{submission.feedback}</p>
            )}
            <p className="text-gray-500 text-xs mt-2">
              Calificada por {submission.gradedBy} el{' '}
              {submission.gradedAt && new Date(submission.gradedAt).toLocaleDateString('es-CO')}
            </p>
          </div>
        )}

        {!canSubmit && !graded && blockedReason && (
          <div className="bg-amber-500/10 border border-amber-500/30 rounded-lg px-4 py-3 text-amber-200 text-sm">
            {blockedReason}
          </div>
        )}

        {canSubmit && (
          <>
            <label className="block">
              <span className="text-xs font-semibold uppercase text-gray-500">Tu entrega</span>
              <textarea
                rows={8}
                value={content}
                onChange={(e) => setContent(e.target.value)}
                placeholder={TYPE_HINT[assignment.type]}
                className="w-full mt-1 bg-gray-800 border border-gray-700 rounded-lg px-3 py-2 text-sm text-gray-100 font-mono focus:outline-none focus:border-blue-500"
              />
              <span className="text-xs text-gray-600">{content.length} / 20000 caracteres</span>
            </label>

            {assignment.type === 'FILE' && (
              <label className="block">
                <span className="text-xs font-semibold uppercase text-gray-500">
                  Enlace a tu archivo o repositorio
                </span>
                <input
                  value={fileUrl}
                  onChange={(e) => setFileUrl(e.target.value)}
                  placeholder="https://github.com/usuario/mi-tarea"
                  className="w-full mt-1 bg-gray-800 border border-gray-700 rounded-lg px-3 py-2 text-sm text-gray-100 focus:outline-none focus:border-blue-500"
                />
              </label>
            )}

            {err && (
              <div className="bg-red-500/10 border border-red-500/30 rounded-lg px-4 py-2 text-red-300 text-sm">
                {err}
              </div>
            )}
            {msg && (
              <div className="bg-emerald-500/10 border border-emerald-500/30 rounded-lg px-4 py-2 text-emerald-300 text-sm">
                {msg}
              </div>
            )}

            <div className="flex flex-wrap gap-2 items-center">
              <button
                onClick={() => send(false)}
                disabled={busy || content.trim().length === 0}
                className="bg-blue-600 text-white px-5 py-2 rounded-lg text-sm font-medium hover:bg-blue-700 transition disabled:opacity-40"
              >
                {busy ? 'Guardando...' : submission?.status === 'SUBMITTED' ? 'Reenviar entrega' : 'Entregar'}
              </button>
              <button
                onClick={() => send(true)}
                disabled={busy || content.trim().length === 0}
                className="px-5 py-2 rounded-lg text-sm font-medium border border-gray-600 text-gray-300 hover:bg-gray-800 transition disabled:opacity-40"
              >
                Guardar borrador
              </button>
              <span className="text-xs text-gray-600">
                Puedes reenviar mientras no este calificada.
              </span>
            </div>
          </>
        )}
      </div>
    </div>
  );
}

export default function Learn() {
  const { id } = useParams();
  const navigate = useNavigate();
  const user = getUser();
  const [course, setCourse] = useState(null);
  const [lessons, setLessons] = useState([]);
  const [progress, setProgress] = useState({ totalLessons: 0, completedLessons: 0, progressPercent: 0, lessonProgress: [] });
  const [activeLesson, setActiveLesson] = useState(null);
  const [assignments, setAssignments] = useState([]);
  const [loading, setLoading] = useState(true);

  const email = (user?.email || '').trim().toLowerCase();

  const load = useCallback(async () => {
    try {
      setLoading(true);
      const [courseData, lessonData, progressData, assignmentData] = await Promise.all([
        getCourseById(id),
        getLessons(id, email),
        getCourseProgress(id, email),
        // Si falla, el curso se sigue viendo: las tareas no bloquean la clase.
        getMyCourseAssignments(id).catch(() => []),
      ]);
      setCourse(courseData);
      setLessons(lessonData);
      setProgress(progressData);
      setAssignments(assignmentData);
      if (lessonData.length > 0 && !activeLesson) {
        // Abrir la primera lección no completada, o la primera si todas están listas
        const completedIds = new Set(progressData.lessonProgress.filter(p => p.completed).map(p => p.lessonId));
        const next = lessonData.find(l => !completedIds.has(l.id)) || lessonData[0];
        setActiveLesson(next);
      }
    } finally {
      setLoading(false);
    }
  }, [id, email]);

  useEffect(() => { load(); }, [load]);

  /** Recarga solo las tareas: guardar una entrega no tiene por que recargar el video. */
  const reloadAssignments = async () => {
    setAssignments(await getMyCourseAssignments(id).catch(() => []));
  };

  const assignmentOf = (lessonId) =>
    assignments.find((a) => a.assignment.lessonId === lessonId);

  const isCompleted = (lessonId) => {
    return progress.lessonProgress.some(p => p.lessonId === lessonId && p.completed);
  };

  const toggleComplete = async (lesson) => {
    const wasCompleted = isCompleted(lesson.id);
    await updateLessonProgress(lesson.id, { email, completed: !wasCompleted });
    // Recargar progreso
    const updated = await getCourseProgress(id, email);
    setProgress(updated);
    // Completar el modulo es justo lo que habilita su entrega.
    await reloadAssignments();
  };

  if (loading) return (
    <div className="min-h-screen bg-gray-50"><Navbar />
      <div className="flex items-center justify-center py-20">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    </div>
  );

  if (!course) return (
    <div className="min-h-screen bg-gray-50"><Navbar />
      <p className="text-center py-20 text-gray-600">Curso no encontrado</p>
    </div>
  );

  const pct = progress.progressPercent || 0;

  return (
    <div className="min-h-screen bg-gray-900">
      <Navbar />

      <div className="flex flex-col lg:flex-row lg:h-[calc(100vh-64px)]">
        {/* Panel izquierdo: Video */}
        <div className="flex-1 flex flex-col">
          {/* Reproductor de video (HU-07) */}
          <div className="bg-black aspect-video w-full relative">
            {activeLesson?.videoUrl ? (
              <iframe
                src={activeLesson.videoUrl}
                title={activeLesson.title}
                className="w-full h-full"
                allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                allowFullScreen
              />
            ) : (
              <div className="w-full h-full flex items-center justify-center text-gray-400">
                <div className="text-center">
                  <svg className="w-16 h-16 mx-auto mb-3 opacity-50" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M14.752 11.168l-3.197-2.132A1 1 0 0010 9.87v4.263a1 1 0 001.555.832l3.197-2.132a1 1 0 000-1.664z" />
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                  <p>Video no disponible</p>
                </div>
              </div>
            )}
          </div>

          {/* Info de la lección activa */}
          <div className="bg-gray-800 p-6 flex-1 overflow-y-auto">
            <div className="flex items-center justify-between mb-4">
              <button onClick={() => navigate('/dashboard')} className="text-blue-400 hover:text-blue-300 text-sm flex items-center gap-1">
                ← Mis Cursos
              </button>
              {activeLesson && (
                <button
                  onClick={() => toggleComplete(activeLesson)}
                  className={`px-4 py-2 rounded-lg text-sm font-medium transition ${
                    isCompleted(activeLesson.id)
                      ? 'bg-green-600 text-white hover:bg-green-700'
                      : 'bg-blue-600 text-white hover:bg-blue-700'
                  }`}
                >
                  {isCompleted(activeLesson.id) ? '✓ Completada' : 'Marcar como completada'}
                </button>
              )}
            </div>

            <h1 className="text-2xl font-bold text-white mb-2">
              {activeLesson ? `${activeLesson.orderIndex}. ${activeLesson.title}` : course.title}
            </h1>
            <p className="text-gray-400 text-sm mb-4">
              {course.title} · {course.technology} · {course.level}
            </p>
            {activeLesson?.description && (
              <p className="text-gray-300">{activeLesson.description}</p>
            )}
            {activeLesson?.durationMinutes > 0 && (
              <p className="text-gray-500 text-sm mt-2">Duración: {activeLesson.durationMinutes} min</p>
            )}

            {/* HU-38: ejercicio practico del modulo */}
            {activeLesson && assignmentOf(activeLesson.id) && (
              <AssignmentPanel
                key={activeLesson.id}
                view={assignmentOf(activeLesson.id)}
                onSaved={reloadAssignments}
              />
            )}
          </div>
        </div>

        {/* Panel derecho: Lista de lecciones */}
        <div className="w-full lg:w-96 bg-gray-800 border-l border-gray-700 flex flex-col">
          {/* Progreso general (HU-08) */}
          <div className="p-4 border-b border-gray-700">
            <div className="flex justify-between items-center mb-2">
              <span className="text-white font-semibold text-sm">Tu progreso</span>
              <span className="text-blue-400 font-bold text-sm">{pct}%</span>
            </div>
            <div className="bg-gray-700 rounded-full h-2">
              <div
                className="bg-blue-500 h-2 rounded-full transition-all duration-300"
                style={{ width: `${pct}%` }}
              />
            </div>
            <p className="text-gray-400 text-xs mt-1">
              {progress.completedLessons}/{progress.totalLessons} lecciones completadas
            </p>
            {pct === 100 ? (
              <>
                <p className="text-green-400 font-medium text-sm mt-2">
                  🎉 ¡Curso completado!
                </p>
                {/* HU-10: la evaluación se habilita al terminar todas las lecciones */}
                <button
                  onClick={() => navigate(`/quiz/${id}`)}
                  className="mt-3 w-full bg-emerald-600 text-white py-2.5 rounded-lg font-bold text-sm hover:bg-emerald-700 transition"
                >
                  Presentar evaluación final
                </button>
                <p className="text-gray-500 text-[11px] mt-1.5 text-center">
                  Apruébala con 70% y recibes tu certificado
                </p>
              </>
            ) : (
              <button
                disabled
                title="Completa todas las lecciones para habilitar la evaluación"
                className="mt-3 w-full bg-gray-700 text-gray-400 py-2.5 rounded-lg font-semibold text-sm cursor-not-allowed"
              >
                🔒 Evaluación final
              </button>
            )}
          </div>

          {/* Lista de lecciones */}
          <div className="flex-1 overflow-y-auto">
            <h3 className="text-gray-400 text-xs font-semibold uppercase tracking-wider px-4 py-3">
              Contenido del curso ({lessons.length} lecciones)
            </h3>
            <div className="space-y-0.5">
              {lessons.map((lesson) => {
                const completed = isCompleted(lesson.id);
                const isActive = activeLesson?.id === lesson.id;
                return (
                  <button
                    key={lesson.id}
                    onClick={() => setActiveLesson(lesson)}
                    className={`w-full text-left px-4 py-3 flex items-start gap-3 transition ${
                      isActive
                        ? 'bg-blue-600/20 border-l-2 border-blue-500'
                        : 'hover:bg-gray-700/50 border-l-2 border-transparent'
                    }`}
                  >
                    <div className={`mt-0.5 flex-shrink-0 w-6 h-6 rounded-full flex items-center justify-center text-xs font-bold ${
                      completed
                        ? 'bg-green-500 text-white'
                        : isActive
                          ? 'bg-blue-500 text-white'
                          : 'bg-gray-600 text-gray-300'
                    }`}>
                      {completed ? '✓' : lesson.orderIndex}
                    </div>
                    <div className="flex-1 min-w-0">
                      <p className={`text-sm font-medium truncate ${
                        isActive ? 'text-white' : completed ? 'text-green-400' : 'text-gray-300'
                      }`}>
                        {lesson.title}
                      </p>
                      {lesson.durationMinutes > 0 && (
                        <p className="text-xs text-gray-500 mt-0.5">{lesson.durationMinutes} min</p>
                      )}
                    </div>
                  </button>
                );
              })}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
