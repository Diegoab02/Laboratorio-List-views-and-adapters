import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar';
import { getUser } from '../services/authService';
import {
  getCourses,
  getMyPurchases,
  getCourseProgress,
  errorMessage,
} from '../services/courseService';

const EMOJI = {
  Python: '🐍',
  'HTML/CSS/JS': '🌐',
  Git: '📚',
  Java: '☕',
  React: '⚛️',
  MySQL: '🗄️',
};

export default function Dashboard() {
  const user = getUser();
  const navigate = useNavigate();

  const [enrolled, setEnrolled] = useState([]);
  const [progressMap, setProgressMap] = useState({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const load = async () => {
    setLoading(true);
    setError('');
    try {
      const email = (user?.email || '').trim().toLowerCase();
      const [courses, purchases] = await Promise.all([getCourses(), getMyPurchases(email)]);

      // Cruza compras con el catálogo. Una compra sin curso activo
      // igual se muestra, con el dato que trae la propia compra.
      const byId = Object.fromEntries(courses.map((c) => [c.id, c]));
      const seen = new Set();
      const list = [];

      for (const p of purchases) {
        if (seen.has(p.courseId)) continue;
        seen.add(p.courseId);
        const c = byId[p.courseId];
        list.push(
          c
            ? { ...c, orderId: p.orderId, purchasedAt: p.purchasedAt, archived: false }
            : {
                id: p.courseId,
                title: 'Curso archivado',
                technology: '',
                level: '',
                orderId: p.orderId,
                purchasedAt: p.purchasedAt,
                archived: true,
              }
        );
      }
      setEnrolled(list);

      if (list.length > 0) {
        const results = await Promise.all(
          list.map((c) =>
            getCourseProgress(c.id, email)
              .then((p) => [c.id, p])
              .catch(() => [c.id, null])
          )
        );
        setProgressMap(Object.fromEntries(results.filter(([, p]) => p)));
      }
    } catch (e) {
      setError(
        errorMessage(e, 'No se pudieron cargar tus cursos. Verifica tu conexión con el servidor.')
      );
    } finally {
      setLoading(false);
    }
  };

  const progressFor = (id) => progressMap[id]?.progressPercent ?? 0;
  const statsFor = (id) => {
    const p = progressMap[id];
    if (!p || p.totalLessons === 0) return 'Sin lecciones publicadas';
    return `${p.completedLessons} de ${p.totalLessons} lecciones`;
  };

  const totals = {
    courses: enrolled.length,
    completed: enrolled.filter((c) => progressFor(c.id) === 100).length,
    lessons: Object.values(progressMap).reduce((s, p) => s + (p.completedLessons || 0), 0),
    avg:
      enrolled.length > 0
        ? Math.round(enrolled.reduce((s, c) => s + progressFor(c.id), 0) / enrolled.length)
        : 0,
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <main className="max-w-6xl mx-auto px-6 py-10">
        <h2 className="text-2xl font-bold text-gray-900 mb-1">
          ¡Hola, {user?.name?.split(' ')[0]}! 👋
        </h2>
        <p className="text-gray-500 mb-8">Estos son los cursos en los que estás matriculado.</p>

        {!loading && !error && enrolled.length > 0 && (
          <div className="grid grid-cols-2 md:grid-cols-4 gap-3 mb-8">
            {[
              ['Cursos', totals.courses, 'text-blue-600'],
              ['Completados', totals.completed, 'text-emerald-600'],
              ['Lecciones vistas', totals.lessons, 'text-purple-600'],
              ['Progreso medio', `${totals.avg}%`, 'text-amber-600'],
            ].map(([label, value, color]) => (
              <div key={label} className="bg-white rounded-xl p-4 border border-gray-100">
                <p className={`text-2xl font-bold ${color}`}>{value}</p>
                <p className="text-xs text-gray-500 mt-0.5">{label}</p>
              </div>
            ))}
          </div>
        )}

        {loading && (
          <div className="text-center py-16">
            <div className="inline-block animate-spin rounded-full h-10 w-10 border-b-2 border-blue-600" />
          </div>
        )}

        {!loading && error && (
          <div className="bg-white border border-red-200 rounded-2xl p-10 text-center max-w-lg mx-auto">
            <div className="text-4xl mb-3">🔌</div>
            <h3 className="font-semibold text-gray-800 mb-2">No se pudo cargar tu información</h3>
            <p className="text-sm text-gray-500 mb-6">{error}</p>
            <button
              onClick={load}
              className="bg-blue-600 text-white px-6 py-2.5 rounded-lg font-medium hover:bg-blue-700 transition"
            >
              Reintentar
            </button>
          </div>
        )}

        {!loading && !error && enrolled.length === 0 && (
          <div className="bg-white border border-gray-100 rounded-2xl p-12 text-center">
            <div className="text-5xl mb-4">📚</div>
            <h3 className="font-semibold text-gray-800 mb-2">Aún no tienes cursos</h3>
            <p className="text-gray-500 mb-6 max-w-md mx-auto text-sm">
              Cuando te matricules en un curso aparecerá aquí junto con tu progreso real,
              lección por lección.
            </p>
            <button
              onClick={() => navigate('/catalog')}
              className="bg-blue-600 text-white px-6 py-2.5 rounded-lg font-medium hover:bg-blue-700 transition"
            >
              Explorar catálogo
            </button>
          </div>
        )}

        {!loading && !error && enrolled.length > 0 && (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
            {enrolled.map((c) => {
              const prog = progressFor(c.id);
              return (
                <div
                  key={c.id}
                  onClick={() => !c.archived && navigate(`/learn/${c.id}`)}
                  className={`bg-white border border-gray-100 rounded-2xl p-6 transition ${
                    c.archived ? 'opacity-60' : 'hover:shadow-md cursor-pointer'
                  }`}
                >
                  <div className="flex items-start justify-between mb-3">
                    <div className="text-4xl">{EMOJI[c.technology] || '💻'}</div>
                    {prog === 100 && (
                      <span className="text-[10px] px-2 py-1 bg-emerald-100 text-emerald-700 rounded-full font-bold">
                        COMPLETADO
                      </span>
                    )}
                  </div>

                  <h3 className="font-semibold text-gray-900 mb-1">{c.title}</h3>
                  {c.level && (
                    <span className="text-xs px-2 py-1 rounded-full font-medium text-blue-700 bg-blue-100">
                      {c.level}
                    </span>
                  )}
                  {c.orderId && (
                    <p className="text-[11px] text-gray-400 mt-2 font-mono">{c.orderId}</p>
                  )}

                  <div className="mt-4 bg-gray-100 rounded-full h-2">
                    <div
                      className={`h-2 rounded-full transition-all duration-300 ${
                        prog === 100 ? 'bg-emerald-500' : 'bg-blue-500'
                      }`}
                      style={{ width: `${prog}%` }}
                    />
                  </div>
                  <div className="flex justify-between items-center mt-1.5">
                    <p className="text-xs text-gray-400">{statsFor(c.id)}</p>
                    <p className="text-xs font-semibold text-gray-600">{prog}%</p>
                  </div>

                  {!c.archived && (
                    <button className="mt-4 w-full bg-blue-600 text-white py-2 rounded-lg font-medium text-sm hover:bg-blue-700 transition">
                      {prog === 100 ? 'Repasar' : prog > 0 ? 'Continuar' : 'Empezar'}
                    </button>
                  )}
                  {c.archived && (
                    <p className="mt-4 text-xs text-center text-gray-400">
                      Este curso fue retirado del catálogo.
                    </p>
                  )}
                </div>
              );
            })}
          </div>
        )}
      </main>
    </div>
  );
}
