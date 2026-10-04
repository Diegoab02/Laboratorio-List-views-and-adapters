import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar';
import CourseCard from '../components/CourseCard';
import { getCourses, getMyPurchases, errorMessage } from '../services/courseService';
import { getUser, isAuthenticated } from '../services/authService';

const FILTERS = ['Todos', 'Python', 'HTML/CSS/JS', 'Git', 'Java', 'React', 'MySQL'];
const SORTS = [
  ['relevancia', 'Más relevantes'],
  ['precio-asc', 'Precio: menor a mayor'],
  ['precio-desc', 'Precio: mayor a menor'],
  ['duracion', 'Duración'],
  ['populares', 'Más matriculados'],
];

export default function Catalog() {
  const navigate = useNavigate();
  const user = getUser();

  const [courses, setCourses] = useState([]);
  const [ownedIds, setOwnedIds] = useState(new Set());
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [filter, setFilter] = useState('Todos');
  const [sort, setSort] = useState('relevancia');
  const [query, setQuery] = useState('');

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const load = async () => {
    setLoading(true);
    setError('');
    try {
      const data = await getCourses();
      setCourses(data);

      // Marca los cursos que el usuario ya compró.
      if (isAuthenticated() && user?.email) {
        try {
          const purchases = await getMyPurchases(user.email.trim().toLowerCase());
          setOwnedIds(new Set(purchases.map((p) => p.courseId)));
        } catch {
          setOwnedIds(new Set());
        }
      }
    } catch (err) {
      setError(
        errorMessage(err, 'No se pudo conectar con el servidor. Verifica que el backend esté corriendo en el puerto 8080.')
      );
    } finally {
      setLoading(false);
    }
  };

  const visible = courses
    .filter((c) => filter === 'Todos' || c.technology === filter)
    .filter((c) => {
      if (!query.trim()) return true;
      const q = query.toLowerCase();
      return (
        c.title.toLowerCase().includes(q) ||
        c.description.toLowerCase().includes(q) ||
        c.technology.toLowerCase().includes(q)
      );
    })
    .sort((a, b) => {
      switch (sort) {
        case 'precio-asc':
          return a.price - b.price;
        case 'precio-desc':
          return b.price - a.price;
        case 'duracion':
          return a.duration - b.duration;
        case 'populares':
          return (b.studentCount ?? 0) - (a.studentCount ?? 0);
        default:
          return 0;
      }
    });

  const goTo = (course) => navigate(`/course/${course.id}`);

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />

      <div className="bg-blue-600 text-white py-12">
        <div className="max-w-7xl mx-auto px-4">
          <h1 className="text-4xl font-bold mb-2">Catálogo de Cursos</h1>
          <p className="text-blue-100">
            Cada curso incluye una lección de muestra gratuita y cupos limitados.
          </p>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-4 py-10">
        <div className="flex flex-col lg:flex-row lg:items-center gap-3 mb-6">
          <input
            type="search"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="Buscar por título, tecnología o descripción..."
            className="flex-1 border border-gray-200 rounded-lg px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
          <select
            value={sort}
            onChange={(e) => setSort(e.target.value)}
            className="border border-gray-200 rounded-lg px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 bg-white"
          >
            {SORTS.map(([v, l]) => (
              <option key={v} value={v}>
                {l}
              </option>
            ))}
          </select>
        </div>

        <div className="mb-8 flex gap-2 flex-wrap">
          {FILTERS.map((f) => (
            <button
              key={f}
              onClick={() => setFilter(f)}
              className={`px-4 py-2 rounded-lg font-medium text-sm transition ${
                filter === f
                  ? 'bg-blue-600 text-white'
                  : 'bg-white text-gray-600 border border-gray-200 hover:border-gray-300'
              }`}
            >
              {f}
            </button>
          ))}
        </div>

        {loading && (
          <div className="flex justify-center py-24">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600" />
          </div>
        )}

        {!loading && error && (
          <div className="bg-white border border-red-200 rounded-2xl p-10 text-center max-w-lg mx-auto">
            <div className="text-4xl mb-3">🔌</div>
            <h3 className="font-semibold text-gray-800 mb-2">Sin conexión con el servidor</h3>
            <p className="text-sm text-gray-500 mb-6">{error}</p>
            <button
              onClick={load}
              className="bg-blue-600 text-white px-6 py-2.5 rounded-lg font-medium hover:bg-blue-700 transition"
            >
              Reintentar
            </button>
          </div>
        )}

        {!loading && !error && visible.length === 0 && (
          <div className="bg-white border border-gray-100 rounded-2xl p-12 text-center">
            <p className="text-gray-500">No hay cursos que coincidan con tu búsqueda.</p>
          </div>
        )}

        {!loading && !error && visible.length > 0 && (
          <>
            <p className="text-sm text-gray-500 mb-4">
              {visible.length} curso{visible.length === 1 ? '' : 's'} disponible
              {visible.length === 1 ? '' : 's'}
            </p>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
              {visible.map((c) => (
                <CourseCard
                  key={c.id}
                  course={c}
                  enrolled={ownedIds.has(c.id)}
                  onViewDetails={() => goTo(c)}
                />
              ))}
            </div>
          </>
        )}
      </div>
    </div>
  );
}
