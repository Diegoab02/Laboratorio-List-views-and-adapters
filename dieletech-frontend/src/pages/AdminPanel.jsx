import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar';
import { getUser } from '../services/authService';
import {
  getAdminDashboard,
  getCourseStudents,
  getAdminUsers,
  changeUserRole,
  adminDeleteCourse,
  getAdminAssignments,
  createAssignment,
  updateAssignment,
  archiveAssignment,
  getLessons,
  errorMessage,
  fieldErrors,
} from '../services/courseService';

const money = (n) => `$${Math.round(n || 0).toLocaleString('es-CO')}`;
const pct = (n) => `${n || 0}%`;

const ROLE_STYLES = {
  ADMIN: 'bg-purple-100 text-purple-700',
  INSTRUCTOR: 'bg-emerald-100 text-emerald-700',
  STUDENT: 'bg-blue-100 text-blue-700',
};

function KpiCard({ label, value, sub, accent = 'text-gray-900' }) {
  return (
    <div className="bg-white rounded-xl p-5 border border-gray-100">
      <p className="text-gray-500 text-xs font-medium uppercase tracking-wide">{label}</p>
      <p className={`text-3xl font-bold mt-1 ${accent}`}>{value}</p>
      {sub && <p className="text-xs text-gray-400 mt-1">{sub}</p>}
    </div>
  );
}

/** Gráfica de barras en CSS puro: sin dependencias adicionales. */
function TrendChart({ data }) {
  const max = Math.max(1, ...data.map((d) => d.enrollments));
  const total = data.reduce((s, d) => s + d.enrollments, 0);

  return (
    <div className="bg-white rounded-xl p-5 border border-gray-100">
      <div className="flex items-baseline justify-between mb-4">
        <h3 className="font-semibold text-gray-900">Matrículas — últimos 30 días</h3>
        <span className="text-sm text-gray-500">{total} en total</span>
      </div>

      {total === 0 ? (
        <div className="h-32 flex items-center justify-center text-sm text-gray-400">
          Aún no hay matrículas registradas en este periodo.
        </div>
      ) : (
        <>
          <div className="flex items-end gap-[3px] h-32">
            {data.map((d) => (
              <div
                key={d.date}
                className="flex-1 bg-blue-500 hover:bg-blue-600 rounded-t transition-all min-h-[2px] relative group"
                style={{ height: `${(d.enrollments / max) * 100}%` }}
                title={`${d.date}: ${d.enrollments} matrícula(s) · ${money(d.revenue)}`}
              >
                <span className="hidden group-hover:block absolute -top-7 left-1/2 -translate-x-1/2 bg-gray-900 text-white text-[10px] px-2 py-1 rounded whitespace-nowrap z-10">
                  {d.enrollments} · {money(d.revenue)}
                </span>
              </div>
            ))}
          </div>
          <div className="flex justify-between text-[10px] text-gray-400 mt-2">
            <span>{data[0]?.date}</span>
            <span>{data[data.length - 1]?.date}</span>
          </div>
        </>
      )}
    </div>
  );
}

function TechBreakdown({ data }) {
  const maxRevenue = Math.max(1, ...data.map((d) => d.revenue));
  return (
    <div className="bg-white rounded-xl p-5 border border-gray-100">
      <h3 className="font-semibold text-gray-900 mb-4">Ingresos por tecnología</h3>
      {data.length === 0 || data.every((d) => d.revenue === 0) ? (
        <p className="text-sm text-gray-400 py-8 text-center">Sin ingresos registrados todavía.</p>
      ) : (
        <div className="space-y-3">
          {data.map((t) => (
            <div key={t.technology}>
              <div className="flex justify-between text-sm mb-1">
                <span className="text-gray-700 font-medium">{t.technology}</span>
                <span className="text-gray-500">
                  {money(t.revenue)} · {t.enrollments} matrícula(s)
                </span>
              </div>
              <div className="bg-gray-100 rounded-full h-2">
                <div
                  className="bg-emerald-500 h-2 rounded-full transition-all"
                  style={{ width: `${(t.revenue / maxRevenue) * 100}%` }}
                />
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}


/* ════════════════════════════════════════════════════════
   HU-37 · Tareas practicas por modulo
   El servidor valida cada regla; este formulario es su espejo,
   no su sustituto. Si aqui pasa algo invalido, el backend lo
   rechaza igual y se muestra el campo exacto que fallo.
   ════════════════════════════════════════════════════════ */

const TYPE_LABELS = { CODE: 'Codigo', FILE: 'Archivo o enlace', TEXT: 'Respuesta escrita' };

const EMPTY_ASSIGNMENT = {
  title: '',
  statement: '',
  type: 'CODE',
  lessonId: '',
  maxScore: 100,
  dueOffsetDays: 14,
  orderIndex: 0,
};

function AssignmentsTab({ courses }) {
  const [courseId, setCourseId] = useState(courses[0]?.courseId ?? null);
  const [items, setItems] = useState([]);
  const [lessons, setLessons] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState(EMPTY_ASSIGNMENT);
  const [errors, setErrors] = useState({});
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (courseId) load(courseId);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [courseId]);

  const load = async (id) => {
    setLoading(true);
    setError('');
    try {
      const [a, l] = await Promise.all([getAdminAssignments(id), getLessons(id)]);
      setItems(a);
      setLessons(l);
    } catch (e) {
      setItems([]);
      setLessons([]);
      setError(errorMessage(e, 'No se pudieron cargar las tareas de este curso.'));
    } finally {
      setLoading(false);
    }
  };

  const openNew = () => {
    setEditing('new');
    setForm(EMPTY_ASSIGNMENT);
    setErrors({});
  };

  const openEdit = (a) => {
    setEditing(a.id);
    setForm({
      title: a.title,
      statement: a.statement,
      type: a.type,
      lessonId: a.lessonId ?? '',
      maxScore: a.maxScore,
      dueOffsetDays: a.dueOffsetDays,
      orderIndex: a.orderIndex,
    });
    setErrors({});
  };

  const close = () => {
    setEditing(null);
    setErrors({});
  };

  // Espejo exacto de CreateAssignmentDTO. No sustituye al servidor.
  const validate = () => {
    const e = {};
    if (form.title.trim().length < 5) e.title = 'El titulo debe tener al menos 5 caracteres';
    if (form.title.trim().length > 160) e.title = 'El titulo no puede superar 160 caracteres';
    if (form.statement.trim().length < 20) e.statement = 'El enunciado debe tener al menos 20 caracteres';
    if (form.statement.trim().length > 4000) e.statement = 'El enunciado no puede superar 4000 caracteres';
    if (!['CODE', 'FILE', 'TEXT'].includes(form.type)) e.type = 'Tipo de entrega invalido';
    if (!(form.maxScore >= 1 && form.maxScore <= 100)) e.maxScore = 'El puntaje va de 1 a 100';
    if (!(form.dueOffsetDays >= 1 && form.dueOffsetDays <= 365)) e.dueOffsetDays = 'El plazo va de 1 a 365 dias';
    setErrors(e);
    return Object.keys(e).length === 0;
  };

  const save = async () => {
    if (!validate()) return;
    setSaving(true);
    setError('');
    const payload = {
      title: form.title.trim(),
      statement: form.statement.trim(),
      type: form.type,
      lessonId: form.lessonId === '' ? null : Number(form.lessonId),
      maxScore: Number(form.maxScore),
      dueOffsetDays: Number(form.dueOffsetDays),
      orderIndex: Number(form.orderIndex) || 0,
    };
    try {
      if (editing === 'new') await createAssignment(courseId, payload);
      else await updateAssignment(editing, payload);
      close();
      await load(courseId);
    } catch (e) {
      setErrors(fieldErrors(e));
      setError(errorMessage(e, 'No se pudo guardar la tarea.'));
    } finally {
      setSaving(false);
    }
  };

  const archive = async (a) => {
    if (!window.confirm(`Se archivara la tarea "${a.title}". Las entregas ya hechas se conservan. Continuar?`)) return;
    try {
      await archiveAssignment(a.id);
      await load(courseId);
    } catch (e) {
      setError(errorMessage(e, 'No se pudo archivar la tarea.'));
    }
  };

  const usedLessons = items
    .filter((a) => a.id !== editing && a.lessonId != null)
    .map((a) => a.lessonId);

  if (courses.length === 0) {
    return (
      <div className="bg-white rounded-xl border border-gray-100 p-12 text-center text-sm text-gray-400">
        No hay cursos a tu cargo, asi que todavia no hay modulos a los que asignar tareas.
      </div>
    );
  }

  return (
    <div className="space-y-4">
      <div className="flex flex-wrap items-center gap-3">
        <select
          value={courseId ?? ''}
          onChange={(ev) => setCourseId(Number(ev.target.value))}
          className="border border-gray-200 rounded-lg px-3 py-2 text-sm bg-white"
        >
          {courses.map((c) => (
            <option key={c.courseId} value={c.courseId}>{c.title}</option>
          ))}
        </select>
        <button
          onClick={openNew}
          className="bg-blue-600 text-white px-4 py-2 rounded-lg font-medium hover:bg-blue-700 transition text-sm"
        >
          + Nueva tarea
        </button>
        <span className="text-xs text-gray-400">
          {items.length} de {lessons.length} modulos con ejercicio practico
        </span>
      </div>

      {error && (
        <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg text-sm">
          {error}
        </div>
      )}

      {loading ? (
        <div className="bg-white rounded-xl border border-gray-100 p-12 text-center text-sm text-gray-400">
          Cargando tareas...
        </div>
      ) : (
        <div className="bg-white rounded-xl border border-gray-100 divide-y divide-gray-50">
          {items.length === 0 && (
            <p className="px-5 py-12 text-center text-sm text-gray-400">
              Este curso todavia no tiene ejercicios practicos.
            </p>
          )}
          {items.map((a) => (
            <div key={a.id} className="px-5 py-4 flex flex-wrap gap-3 items-start">
              <div className="flex-1 min-w-[260px]">
                <div className="flex items-center gap-2 flex-wrap">
                  <span className="text-xs font-mono text-gray-400">#{a.orderIndex}</span>
                  <p className="font-medium text-gray-900">{a.title}</p>
                  <span className="text-xs px-2 py-0.5 rounded-full bg-blue-50 text-blue-700 font-medium">
                    {TYPE_LABELS[a.type] ?? a.type}
                  </span>
                </div>
                <p className="text-xs text-gray-500 mt-1">
                  {a.lessonTitle ? `Modulo: ${a.lessonTitle}` : 'Tarea del curso completo'}
                  {' · '}{a.maxScore} puntos{' · '}plazo de {a.dueOffsetDays} dias desde la compra
                </p>
                <p className="text-sm text-gray-600 mt-2 line-clamp-2">{a.statement}</p>
              </div>
              <div className="flex gap-2">
                <button
                  onClick={() => openEdit(a)}
                  className="px-3 py-1.5 rounded-lg border border-gray-200 text-gray-700 hover:bg-gray-50 transition text-xs font-medium"
                >
                  Editar
                </button>
                <button
                  onClick={() => archive(a)}
                  className="px-3 py-1.5 rounded-lg border border-red-200 text-red-600 hover:bg-red-50 transition text-xs font-medium"
                >
                  Archivar
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      {editing !== null && (
        <div className="fixed inset-0 bg-black/40 flex items-end sm:items-center justify-center z-50 p-0 sm:p-6">
          <div className="bg-white w-full sm:max-w-2xl rounded-t-2xl sm:rounded-2xl max-h-[88vh] overflow-y-auto">
            <div className="px-6 py-4 border-b border-gray-100 flex items-center justify-between sticky top-0 bg-white">
              <h3 className="font-bold text-gray-900">
                {editing === 'new' ? 'Nueva tarea practica' : 'Editar tarea'}
              </h3>
              <button onClick={close} className="text-gray-400 hover:text-gray-700 text-xl leading-none">&times;</button>
            </div>

            <div className="px-6 py-5 space-y-4">
              <label className="block">
                <span className="text-xs font-semibold text-gray-500 uppercase">Titulo</span>
                <input
                  value={form.title}
                  onChange={(ev) => setForm({ ...form, title: ev.target.value })}
                  className={`w-full mt-1 border rounded-lg px-3 py-2 text-sm ${errors.title ? 'border-red-400 bg-red-50' : 'border-gray-200'}`}
                  placeholder="Conversor de unidades con validacion"
                />
                {errors.title && <p className="text-xs text-red-600 mt-1">{errors.title}</p>}
              </label>

              <label className="block">
                <span className="text-xs font-semibold text-gray-500 uppercase">Enunciado</span>
                <textarea
                  rows={6}
                  value={form.statement}
                  onChange={(ev) => setForm({ ...form, statement: ev.target.value })}
                  className={`w-full mt-1 border rounded-lg px-3 py-2 text-sm ${errors.statement ? 'border-red-400 bg-red-50' : 'border-gray-200'}`}
                  placeholder="Que tiene que hacer el estudiante y como se evaluara."
                />
                <span className="text-xs text-gray-400">{form.statement.length} / 4000 caracteres</span>
                {errors.statement && <p className="text-xs text-red-600 mt-1">{errors.statement}</p>}
              </label>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <label className="block">
                  <span className="text-xs font-semibold text-gray-500 uppercase">Modulo</span>
                  <select
                    value={form.lessonId}
                    onChange={(ev) => setForm({ ...form, lessonId: ev.target.value })}
                    className="w-full mt-1 border border-gray-200 rounded-lg px-3 py-2 text-sm bg-white"
                  >
                    <option value="">Tarea del curso completo</option>
                    {lessons.map((l) => (
                      <option key={l.id} value={l.id} disabled={usedLessons.includes(l.id)}>
                        {l.orderIndex}. {l.title}{usedLessons.includes(l.id) ? ' (ya tiene tarea)' : ''}
                      </option>
                    ))}
                  </select>
                  <span className="text-xs text-gray-400">Un modulo admite una sola tarea.</span>
                </label>

                <label className="block">
                  <span className="text-xs font-semibold text-gray-500 uppercase">Tipo de entrega</span>
                  <select
                    value={form.type}
                    onChange={(ev) => setForm({ ...form, type: ev.target.value })}
                    className="w-full mt-1 border border-gray-200 rounded-lg px-3 py-2 text-sm bg-white"
                  >
                    {Object.entries(TYPE_LABELS).map(([k, v]) => (
                      <option key={k} value={k}>{v}</option>
                    ))}
                  </select>
                </label>

                <label className="block">
                  <span className="text-xs font-semibold text-gray-500 uppercase">Puntaje maximo</span>
                  <input
                    type="number" min={1} max={100}
                    value={form.maxScore}
                    onChange={(ev) => setForm({ ...form, maxScore: ev.target.value })}
                    className={`w-full mt-1 border rounded-lg px-3 py-2 text-sm ${errors.maxScore ? 'border-red-400 bg-red-50' : 'border-gray-200'}`}
                  />
                  {errors.maxScore && <p className="text-xs text-red-600 mt-1">{errors.maxScore}</p>}
                </label>

                <label className="block">
                  <span className="text-xs font-semibold text-gray-500 uppercase">Plazo en dias</span>
                  <input
                    type="number" min={1} max={365}
                    value={form.dueOffsetDays}
                    onChange={(ev) => setForm({ ...form, dueOffsetDays: ev.target.value })}
                    className={`w-full mt-1 border rounded-lg px-3 py-2 text-sm ${errors.dueOffsetDays ? 'border-red-400 bg-red-50' : 'border-gray-200'}`}
                  />
                  <span className="text-xs text-gray-400">Se cuentan desde que el estudiante compra.</span>
                  {errors.dueOffsetDays && <p className="text-xs text-red-600 mt-1">{errors.dueOffsetDays}</p>}
                </label>
              </div>
            </div>

            <div className="px-6 py-4 border-t border-gray-100 flex gap-2 sticky bottom-0 bg-white">
              <button
                onClick={save}
                disabled={saving}
                className="bg-blue-600 text-white px-5 py-2.5 rounded-lg font-medium hover:bg-blue-700 transition text-sm disabled:opacity-50"
              >
                {saving ? 'Guardando...' : editing === 'new' ? 'Crear tarea' : 'Guardar cambios'}
              </button>
              <button
                onClick={close}
                className="px-5 py-2.5 rounded-lg border border-gray-200 text-gray-700 hover:bg-gray-50 transition text-sm font-medium"
              >
                Cancelar
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default function AdminPanel() {
  const user = getUser();
  const navigate = useNavigate();
  const isAdmin = user?.role === 'ADMIN';

  const [tab, setTab] = useState('resumen');
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const [studentsOf, setStudentsOf] = useState(null);
  const [students, setStudents] = useState([]);
  const [studentsLoading, setStudentsLoading] = useState(false);

  const [users, setUsers] = useState([]);
  const [usersLoading, setUsersLoading] = useState(false);

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const load = async () => {
    setLoading(true);
    setError('');
    try {
      setData(await getAdminDashboard());
    } catch (e) {
      setError(errorMessage(e, 'No se pudo cargar el panel. Verifica tu sesión y el backend.'));
    } finally {
      setLoading(false);
    }
  };

  const openStudents = async (course) => {
    setStudentsOf(course);
    setStudentsLoading(true);
    try {
      setStudents(await getCourseStudents(course.courseId));
    } catch (e) {
      setStudents([]);
      setError(errorMessage(e, 'No se pudo cargar la lista de estudiantes.'));
    } finally {
      setStudentsLoading(false);
    }
  };

  const loadUsers = async () => {
    setUsersLoading(true);
    try {
      setUsers(await getAdminUsers());
    } catch (e) {
      setError(errorMessage(e, 'No se pudieron cargar los usuarios.'));
    } finally {
      setUsersLoading(false);
    }
  };

  const onTab = (t) => {
    setTab(t);
    if (t === 'usuarios' && users.length === 0) loadUsers();
  };

  const onChangeRole = async (u, role) => {
    if (!window.confirm(`¿Cambiar el rol de ${u.name} a ${role}?`)) return;
    try {
      await changeUserRole(u.id, role);
      await loadUsers();
    } catch (e) {
      alert(errorMessage(e, 'No se pudo cambiar el rol.'));
    }
  };

  const onDeleteCourse = async (c) => {
    const warning =
      c.enrolled > 0
        ? `"${c.title}" tiene ${c.enrolled} estudiante(s) matriculado(s). Se archivará para conservar su historial. ¿Continuar?`
        : `¿Archivar el curso "${c.title}"?`;
    if (!window.confirm(warning)) return;
    try {
      await adminDeleteCourse(c.courseId);
      await load();
    } catch (e) {
      alert(errorMessage(e, 'No se pudo archivar el curso.'));
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50">
        <Navbar />
        <div className="flex justify-center py-32">
          <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-blue-600" />
        </div>
      </div>
    );
  }

  const o = data?.overview;
  const tabs = [
    ['resumen', 'Resumen'],
    ['cursos', 'Cursos'],
    ['tareas', 'Tareas'],
    ['actividad', 'Actividad'],
    ...(isAdmin ? [['usuarios', 'Usuarios']] : []),
  ];

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <main className="max-w-7xl mx-auto px-6 py-8">
        <div className="flex flex-wrap justify-between items-start gap-4 mb-6">
          <div>
            <div className="flex items-center gap-2">
              <h1 className="text-2xl font-bold text-gray-900">
                {isAdmin ? 'Panel de Administración' : 'Panel del Instructor'}
              </h1>
              <span className={`text-xs px-2 py-0.5 rounded-full font-semibold ${ROLE_STYLES[user?.role]}`}>
                {user?.role}
              </span>
            </div>
            <p className="text-gray-500 text-sm mt-1">
              {isAdmin
                ? 'Métricas de toda la plataforma, calculadas sobre matrículas reales.'
                : 'Métricas de los cursos que tienes a cargo.'}
            </p>
          </div>
          <div className="flex gap-2">
            <button
              onClick={load}
              className="px-4 py-2.5 rounded-lg font-medium border border-gray-200 text-gray-700 hover:bg-gray-50 transition text-sm"
            >
              Actualizar
            </button>
            <button
              onClick={() => navigate('/admin/courses/new')}
              className="bg-blue-600 text-white px-5 py-2.5 rounded-lg font-medium hover:bg-blue-700 transition text-sm"
            >
              + Nuevo curso
            </button>
          </div>
        </div>

        {error && (
          <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg mb-6 text-sm">
            {error}
          </div>
        )}

        <div className="flex gap-1 border-b border-gray-200 mb-6 overflow-x-auto">
          {tabs.map(([key, label]) => (
            <button
              key={key}
              onClick={() => onTab(key)}
              className={`px-4 py-2.5 text-sm font-medium border-b-2 -mb-px transition whitespace-nowrap ${
                tab === key
                  ? 'border-blue-600 text-blue-600'
                  : 'border-transparent text-gray-500 hover:text-gray-800'
              }`}
            >
              {label}
            </button>
          ))}
        </div>

        {/* ── RESUMEN ── */}
        {tab === 'resumen' && o && (
          <div className="space-y-5">
            <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
              <KpiCard
                label="Ingresos totales"
                value={money(o.totalRevenue)}
                sub={`${money(o.revenueLast30Days)} en los últimos 30 días`}
                accent="text-emerald-600"
              />
              <KpiCard
                label="Matrículas"
                value={o.totalEnrollments}
                sub={`${o.uniqueStudents} estudiante(s) distinto(s)`}
                accent="text-blue-600"
              />
              <KpiCard
                label="Ocupación"
                value={pct(o.occupancyPercent)}
                sub={`${o.seatsAvailable} de ${o.totalCapacity} cupos libres`}
                accent="text-amber-600"
              />
              <KpiCard
                label="Progreso promedio"
                value={pct(o.averageProgressPercent)}
                sub={`${pct(o.completionRatePercent)} de finalización`}
                accent="text-purple-600"
              />
            </div>

            <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
              <KpiCard label="Cursos" value={o.totalCourses} sub={`${o.activeCourses} activo(s)`} />
              <KpiCard label="Lecciones" value={o.totalLessons} sub="contenido publicado" />
              <KpiCard label="Nuevas matrículas" value={o.enrollmentsLast30Days} sub="últimos 30 días" />
              <KpiCard label="Cupos libres" value={o.seatsAvailable} sub="disponibles ahora" />
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-2 gap-5">
              <TrendChart data={data.enrollmentTrend} />
              <TechBreakdown data={data.technologyBreakdown} />
            </div>
          </div>
        )}

        {/* ── CURSOS ── */}
        {tab === 'cursos' && (
          <div className="bg-white rounded-xl border border-gray-100 overflow-x-auto">
            <table className="w-full min-w-[900px]">
              <thead className="bg-gray-50 border-b border-gray-100">
                <tr>
                  {['Curso', 'Ocupación', 'Ingresos', 'Progreso medio', 'Completados', 'Acciones'].map(
                    (h) => (
                      <th
                        key={h}
                        className={`px-5 py-3 text-xs font-semibold text-gray-500 uppercase ${
                          h === 'Acciones' ? 'text-right' : 'text-left'
                        }`}
                      >
                        {h}
                      </th>
                    )
                  )}
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-50">
                {data.courses.length === 0 && (
                  <tr>
                    <td colSpan={6} className="px-5 py-12 text-center text-sm text-gray-400">
                      No hay cursos asignados todavía.
                    </td>
                  </tr>
                )}
                {data.courses.map((c) => (
                  <tr key={c.courseId} className="hover:bg-gray-50 transition">
                    <td className="px-5 py-4">
                      <div className="flex items-center gap-2">
                        <p className="font-medium text-gray-900">{c.title}</p>
                        {!c.active && (
                          <span className="text-[10px] px-1.5 py-0.5 bg-gray-200 text-gray-600 rounded">
                            archivado
                          </span>
                        )}
                      </div>
                      <p className="text-xs text-gray-400 mt-0.5">
                        {c.technology} · {c.level} · {c.lessonCount} lección(es) · {money(c.price)}
                      </p>
                    </td>
                    <td className="px-5 py-4">
                      <div className="flex items-center gap-2">
                        <div className="w-20 bg-gray-100 rounded-full h-2">
                          <div
                            className={`h-2 rounded-full ${
                              c.occupancyPercent >= 90 ? 'bg-red-500' : 'bg-blue-500'
                            }`}
                            style={{ width: `${Math.min(100, c.occupancyPercent)}%` }}
                          />
                        </div>
                        <span className="text-xs text-gray-600 whitespace-nowrap">
                          {c.enrolled}/{c.capacity}
                        </span>
                      </div>
                    </td>
                    <td className="px-5 py-4 text-sm font-medium text-emerald-600">
                      {money(c.revenue)}
                    </td>
                    <td className="px-5 py-4 text-sm text-gray-600">
                      {pct(c.averageProgressPercent)}
                    </td>
                    <td className="px-5 py-4 text-sm text-gray-600">{c.studentsCompleted}</td>
                    <td className="px-5 py-4">
                      <div className="flex justify-end gap-3 text-sm font-medium">
                        <button
                          onClick={() => openStudents(c)}
                          className="text-gray-600 hover:text-gray-900"
                        >
                          Estudiantes
                        </button>
                        <button
                          onClick={() => navigate(`/admin/courses/${c.courseId}/edit`)}
                          className="text-blue-600 hover:text-blue-800"
                        >
                          Editar
                        </button>
                        <button
                          onClick={() => navigate(`/admin/courses/${c.courseId}/lessons`)}
                          className="text-green-600 hover:text-green-800"
                        >
                          Lecciones
                        </button>
                        <button
                          onClick={() => onDeleteCourse(c)}
                          className="text-red-500 hover:text-red-700"
                        >
                          Archivar
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {/* ── ACTIVIDAD ── */}
        {/* ── TAREAS PRACTICAS (HU-37) ── */}
        {tab === 'tareas' && <AssignmentsTab courses={data.courses} />}

        {tab === 'actividad' && (
          <div className="bg-white rounded-xl border border-gray-100 p-5">
            <h3 className="font-semibold text-gray-900 mb-4">Matrículas recientes</h3>
            {data.recentEnrollments.length === 0 ? (
              <p className="text-sm text-gray-400 py-12 text-center">
                Todavía no hay matrículas. Cuando un estudiante compre un curso aparecerá aquí.
              </p>
            ) : (
              <div className="divide-y divide-gray-50">
                {data.recentEnrollments.map((e) => (
                  <div key={e.orderId} className="py-3 flex items-center justify-between gap-4">
                    <div className="min-w-0">
                      <p className="text-sm font-medium text-gray-900 truncate">
                        {e.studentName}{' '}
                        <span className="text-gray-400 font-normal">se matriculó en</span>{' '}
                        {e.courseTitle}
                      </p>
                      <p className="text-xs text-gray-400">
                        {e.studentEmail} · {e.purchasedAt} · {e.orderId}
                      </p>
                    </div>
                    <span className="text-sm font-semibold text-emerald-600 whitespace-nowrap">
                      {money(e.amount)}
                    </span>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}

        {/* ── USUARIOS (solo ADMIN) ── */}
        {tab === 'usuarios' && isAdmin && (
          <div className="bg-white rounded-xl border border-gray-100 overflow-x-auto">
            {usersLoading ? (
              <div className="flex justify-center py-16">
                <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600" />
              </div>
            ) : (
              <table className="w-full min-w-[800px]">
                <thead className="bg-gray-50 border-b border-gray-100">
                  <tr>
                    {['Usuario', 'Rol', 'Estado', 'Cursos', 'Invertido', 'Cambiar rol'].map((h) => (
                      <th
                        key={h}
                        className={`px-5 py-3 text-xs font-semibold text-gray-500 uppercase ${
                          h === 'Cambiar rol' ? 'text-right' : 'text-left'
                        }`}
                      >
                        {h}
                      </th>
                    ))}
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-50">
                  {users.map((u) => (
                    <tr key={u.id} className="hover:bg-gray-50 transition">
                      <td className="px-5 py-4">
                        <p className="font-medium text-gray-900">{u.name}</p>
                        <p className="text-xs text-gray-400">
                          {u.email} · desde {u.createdAt}
                        </p>
                      </td>
                      <td className="px-5 py-4">
                        <span
                          className={`text-xs px-2 py-1 rounded-full font-semibold ${ROLE_STYLES[u.role]}`}
                        >
                          {u.role}
                        </span>
                      </td>
                      <td className="px-5 py-4">
                        <span
                          className={`text-xs font-medium ${
                            u.verified ? 'text-emerald-600' : 'text-amber-600'
                          }`}
                        >
                          {u.verified ? 'Verificado' : 'Sin verificar'}
                        </span>
                      </td>
                      <td className="px-5 py-4 text-sm text-gray-600">{u.coursesEnrolled}</td>
                      <td className="px-5 py-4 text-sm text-gray-600">{money(u.totalSpent)}</td>
                      <td className="px-5 py-4 text-right">
                        <select
                          value={u.role}
                          onChange={(e) => onChangeRole(u, e.target.value)}
                          disabled={u.email === user?.email}
                          className="text-sm border border-gray-200 rounded-lg px-2 py-1.5 disabled:opacity-40 disabled:cursor-not-allowed"
                        >
                          <option value="STUDENT">STUDENT</option>
                          <option value="INSTRUCTOR">INSTRUCTOR</option>
                          <option value="ADMIN">ADMIN</option>
                        </select>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        )}
      </main>

      {/* Modal de estudiantes */}
      {studentsOf && (
        <div
          className="fixed inset-0 bg-black/40 flex items-center justify-center p-4 z-50"
          onClick={() => setStudentsOf(null)}
        >
          <div
            className="bg-white rounded-2xl max-w-3xl w-full max-h-[80vh] overflow-hidden flex flex-col"
            onClick={(e) => e.stopPropagation()}
          >
            <div className="px-6 py-4 border-b border-gray-100 flex justify-between items-center">
              <div>
                <h3 className="font-bold text-gray-900">Estudiantes matriculados</h3>
                <p className="text-xs text-gray-500">{studentsOf.title}</p>
              </div>
              <button
                onClick={() => setStudentsOf(null)}
                className="text-gray-400 hover:text-gray-700 text-xl leading-none"
              >
                ×
              </button>
            </div>

            <div className="overflow-y-auto p-6">
              {studentsLoading ? (
                <div className="flex justify-center py-12">
                  <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600" />
                </div>
              ) : students.length === 0 ? (
                <p className="text-sm text-gray-400 py-12 text-center">
                  Este curso aún no tiene estudiantes matriculados.
                </p>
              ) : (
                <div className="divide-y divide-gray-50">
                  {students.map((s) => (
                    <div key={s.orderId} className="py-3">
                      <div className="flex justify-between items-start gap-4 mb-2">
                        <div className="min-w-0">
                          <p className="text-sm font-medium text-gray-900">{s.fullName}</p>
                          <p className="text-xs text-gray-400">
                            {s.email} · {s.purchasedAt}
                          </p>
                        </div>
                        <span className="text-sm font-semibold text-emerald-600 whitespace-nowrap">
                          {money(s.amount)}
                        </span>
                      </div>
                      <div className="flex items-center gap-2">
                        <div className="flex-1 bg-gray-100 rounded-full h-1.5">
                          <div
                            className={`h-1.5 rounded-full ${
                              s.progressPercent === 100 ? 'bg-emerald-500' : 'bg-blue-500'
                            }`}
                            style={{ width: `${s.progressPercent}%` }}
                          />
                        </div>
                        <span className="text-xs text-gray-500 whitespace-nowrap">
                          {s.completedLessons}/{s.totalLessons} · {pct(s.progressPercent)}
                        </span>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
