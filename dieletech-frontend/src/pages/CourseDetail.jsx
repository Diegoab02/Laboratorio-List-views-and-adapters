import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar';
import {
  getCourseById,
  getLessons,
  checkPurchase,
  errorMessage,
} from '../services/courseService';
import { getUser, isAuthenticated } from '../services/authService';

const EMOJI = {
  Python: '🐍',
  'HTML/CSS/JS': '🌐',
  Git: '📚',
  Java: '☕',
  React: '⚛️',
  MySQL: '🗄️',
};

/** Convierte una URL de embed de YouTube en su enlace público para "ver en YouTube". */
const watchUrl = (embedUrl) => {
  if (!embedUrl) return null;
  const m = embedUrl.match(/embed\/([\w-]+)/);
  if (!m) return embedUrl;
  const start = embedUrl.match(/start=(\d+)/);
  return `https://www.youtube.com/watch?v=${m[1]}${start ? `&t=${start[1]}s` : ''}`;
};

export default function CourseDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const user = getUser();
  const email = (user?.email || '').trim().toLowerCase();

  const [course, setCourse] = useState(null);
  const [lessons, setLessons] = useState([]);
  const [owned, setOwned] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [playing, setPlaying] = useState(null);

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  const load = async () => {
    setLoading(true);
    setError('');
    try {
      const [c, l, o] = await Promise.all([
        getCourseById(id),
        getLessons(id, email).catch(() => []),
        email ? checkPurchase(id, email) : Promise.resolve(false),
      ]);
      setCourse(c);
      setLessons(l);
      setOwned(o);
      const free = l.find((x) => x.freePreview);
      setPlaying(free?.videoUrl || c.previewVideoUrl || null);
    } catch (err) {
      setError(errorMessage(err, 'No se pudo cargar el curso.'));
    } finally {
      setLoading(false);
    }
  };

  const onEnroll = () => {
    if (!isAuthenticated()) {
      navigate('/login', { state: { redirectTo: `/checkout/${id}` } });
      return;
    }
    navigate(`/checkout/${id}`);
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

  if (error || !course) {
    return (
      <div className="min-h-screen bg-gray-50">
        <Navbar />
        <div className="max-w-md mx-auto mt-24 bg-white rounded-2xl border border-gray-100 p-8 text-center">
          <div className="text-4xl mb-3">⚠️</div>
          <p className="text-sm text-gray-500 mb-6">{error || 'Curso no encontrado.'}</p>
          <button
            onClick={() => navigate('/catalog')}
            className="bg-blue-600 text-white px-6 py-2.5 rounded-lg font-medium hover:bg-blue-700"
          >
            Volver al catálogo
          </button>
        </div>
      </div>
    );
  }

  const freeLesson = lessons.find((l) => l.freePreview);
  const totalMinutes = lessons.reduce((s, l) => s + (l.durationMinutes || 0), 0);

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />

      {/* Hero */}
      <div className="bg-gradient-to-br from-blue-700 to-blue-900 text-white">
        <div className="max-w-6xl mx-auto px-4 py-10">
          <button
            onClick={() => navigate('/catalog')}
            className="text-blue-200 hover:text-white text-sm mb-4 transition"
          >
            ← Volver al catálogo
          </button>

          <div className="flex flex-wrap items-start gap-6">
            <div className="text-6xl">{EMOJI[course.technology] || '💻'}</div>
            <div className="flex-1 min-w-[280px]">
              <h1 className="text-3xl font-bold mb-2">{course.title}</h1>
              <p className="text-blue-100 mb-4 max-w-3xl">{course.description}</p>
              <div className="flex flex-wrap gap-2 text-xs">
                <span className="bg-white/15 px-3 py-1 rounded-full">{course.technology}</span>
                <span className="bg-white/15 px-3 py-1 rounded-full">Nivel {course.level}</span>
                <span className="bg-white/15 px-3 py-1 rounded-full">{course.duration} horas</span>
                <span className="bg-white/15 px-3 py-1 rounded-full">
                  {lessons.length} lecciones
                </span>
                <span className="bg-white/15 px-3 py-1 rounded-full">
                  {course.studentCount} matriculado{course.studentCount === 1 ? '' : 's'}
                </span>
                {course.instructorName && (
                  <span className="bg-white/15 px-3 py-1 rounded-full">
                    Instructor: {course.instructorName}
                  </span>
                )}
              </div>
            </div>
          </div>
        </div>
      </div>

      <div className="max-w-6xl mx-auto px-4 py-8 grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Columna principal */}
        <div className="lg:col-span-2 space-y-6">
          {/* Reproductor de la lección gratuita */}
          {playing && (
            <div className="bg-white rounded-2xl border border-gray-100 overflow-hidden">
              <div className="px-5 pt-5 pb-3 flex flex-wrap items-center justify-between gap-2">
                <div>
                  <h2 className="font-bold text-gray-900">
                    {owned ? 'Vista previa del curso' : 'Lección de muestra gratuita'}
                  </h2>
                  {freeLesson && (
                    <p className="text-xs text-gray-500 mt-0.5">{freeLesson.title}</p>
                  )}
                </div>
                {watchUrl(playing) && (
                  <a
                    href={watchUrl(playing)}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="text-xs font-medium text-blue-600 hover:text-blue-800 hover:underline"
                  >
                    Ver en YouTube ↗
                  </a>
                )}
              </div>
              <div className="aspect-video bg-black">
                <iframe
                  key={playing}
                  src={playing}
                  title="Lección de muestra"
                  className="w-full h-full"
                  allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                  allowFullScreen
                />
              </div>
            </div>
          )}

          {/* Descripción detallada */}
          {course.longDescription && (
            <div className="bg-white rounded-2xl border border-gray-100 p-6">
              <h2 className="font-bold text-gray-900 mb-3">Sobre este curso</h2>
              <div className="space-y-3 text-sm text-gray-600 leading-relaxed">
                {course.longDescription.split('\n\n').map((p, i) => (
                  <p key={i}>{p.trim()}</p>
                ))}
              </div>
            </div>
          )}

          {/* Objetivos de aprendizaje */}
          {course.learningObjectives?.length > 0 && (
            <div className="bg-white rounded-2xl border border-gray-100 p-6">
              <h2 className="font-bold text-gray-900 mb-4">Qué vas a lograr</h2>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                {course.learningObjectives.map((o, i) => (
                  <div key={i} className="flex gap-2.5 text-sm text-gray-600">
                    <span className="text-emerald-500 font-bold shrink-0">✓</span>
                    <span>{o}</span>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Temario con estado de bloqueo */}
          <div className="bg-white rounded-2xl border border-gray-100 p-6">
            <div className="flex items-baseline justify-between mb-4">
              <h2 className="font-bold text-gray-900">Temario</h2>
              <span className="text-xs text-gray-400">
                {lessons.length} lecciones · {Math.round(totalMinutes / 60)}h aprox.
              </span>
            </div>

            {lessons.length === 0 ? (
              <p className="text-sm text-gray-400 py-6 text-center">
                El temario de este curso aún no está publicado.
              </p>
            ) : (
              <div className="divide-y divide-gray-50">
                {lessons.map((l, i) => {
                  const unlocked = !l.locked;
                  return (
                    <div key={l.id} className="py-3 flex items-start gap-3">
                      <span className="text-xs font-mono text-gray-300 w-6 shrink-0 pt-0.5">
                        {String(i + 1).padStart(2, '0')}
                      </span>
                      <div className="flex-1 min-w-0">
                        <div className="flex flex-wrap items-center gap-2">
                          <p className="text-sm font-medium text-gray-800">{l.title}</p>
                          {l.freePreview && (
                            <span className="text-[10px] px-1.5 py-0.5 bg-emerald-100 text-emerald-700 rounded font-semibold">
                              GRATIS
                            </span>
                          )}
                          {l.locked && (
                            <span className="text-[10px] px-1.5 py-0.5 bg-gray-100 text-gray-500 rounded font-semibold">
                              🔒 REQUIERE COMPRA
                            </span>
                          )}
                        </div>
                        {l.description && (
                          <p className="text-xs text-gray-500 mt-0.5">{l.description}</p>
                        )}
                      </div>
                      <div className="flex items-center gap-2 shrink-0">
                        <span className="text-xs text-gray-400">{l.durationMinutes} min</span>
                        {unlocked && l.videoUrl && (
                          <button
                            onClick={() => {
                              setPlaying(l.videoUrl);
                              window.scrollTo({ top: 0, behavior: 'smooth' });
                            }}
                            className="text-xs font-medium text-blue-600 hover:text-blue-800"
                          >
                            Ver
                          </button>
                        )}
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </div>

          {/* Requisitos y público objetivo */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-6">
            {course.prerequisites?.length > 0 && (
              <div className="bg-white rounded-2xl border border-gray-100 p-6">
                <h2 className="font-bold text-gray-900 mb-3">Requisitos</h2>
                <ul className="space-y-2">
                  {course.prerequisites.map((p, i) => (
                    <li key={i} className="text-sm text-gray-600 flex gap-2">
                      <span className="text-gray-300">•</span>
                      <span>{p}</span>
                    </li>
                  ))}
                </ul>
              </div>
            )}

            {course.targetAudience?.length > 0 && (
              <div className="bg-white rounded-2xl border border-gray-100 p-6">
                <h2 className="font-bold text-gray-900 mb-3">Para quién es</h2>
                <ul className="space-y-2">
                  {course.targetAudience.map((a, i) => (
                    <li key={i} className="text-sm text-gray-600 flex gap-2">
                      <span className="text-gray-300">•</span>
                      <span>{a}</span>
                    </li>
                  ))}
                </ul>
              </div>
            )}
          </div>
        </div>

        {/* Panel lateral de compra */}
        <div>
          <div className="bg-white rounded-2xl border border-gray-100 p-6 sticky top-20">
            <p className="text-3xl font-bold text-blue-600 mb-1">
              ${course.price.toLocaleString('es-CO')}
            </p>
            <p className="text-xs text-gray-400 mb-5">Pago único · acceso de por vida</p>

            {/* Estado de cupos, calculado desde matrículas reales */}
            <div className="mb-5">
              <div className="flex justify-between text-xs text-gray-500 mb-1.5">
                <span>{course.studentCount} matriculados</span>
                <span className={course.seatsAvailable <= 5 ? 'text-amber-600 font-semibold' : ''}>
                  {course.seatsAvailable} de {course.capacity} cupos
                </span>
              </div>
              <div className="bg-gray-100 rounded-full h-2">
                <div
                  className={`h-2 rounded-full ${
                    course.soldOut
                      ? 'bg-gray-400'
                      : course.seatsAvailable <= 5
                        ? 'bg-amber-500'
                        : 'bg-blue-500'
                  }`}
                  style={{
                    width: `${course.capacity > 0 ? (course.studentCount / course.capacity) * 100 : 0}%`,
                  }}
                />
              </div>
            </div>

            {owned ? (
              <button
                onClick={() => navigate(`/learn/${id}`)}
                className="w-full bg-emerald-600 text-white py-3 rounded-lg font-bold hover:bg-emerald-700 transition"
              >
                Ir a mi curso
              </button>
            ) : course.soldOut ? (
              <button
                disabled
                className="w-full bg-gray-200 text-gray-500 py-3 rounded-lg font-bold cursor-not-allowed"
              >
                Cupos agotados
              </button>
            ) : (
              <button
                onClick={onEnroll}
                className="w-full bg-blue-600 text-white py-3 rounded-lg font-bold hover:bg-blue-700 transition"
              >
                Matricularme ahora
              </button>
            )}

            {!isAuthenticated() && !course.soldOut && (
              <p className="text-xs text-center text-gray-400 mt-2">
                Necesitas una cuenta para matricularte.
              </p>
            )}

            <div className="mt-5 pt-5 border-t border-gray-100 space-y-2">
              {[
                `${lessons.length} lecciones en video`,
                `${course.duration} horas de contenido`,
                'Lección de muestra gratuita',
                'Seguimiento de progreso',
                'Certificado al completar',
                'Acceso de por vida',
              ].map((f) => (
                <p key={f} className="text-xs text-gray-600 flex gap-2">
                  <span className="text-emerald-500">✓</span>
                  {f}
                </p>
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
