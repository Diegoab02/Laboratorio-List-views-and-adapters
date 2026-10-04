import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar';
import {
  getCourseById,
  adminCreateCourse,
  adminUpdateCourse,
  errorMessage,
} from '../services/courseService';

const TECHNOLOGIES = ['Python', 'HTML/CSS/JS', 'Git', 'Java', 'React', 'MySQL', 'Kotlin', 'Docker'];
const LEVELS = ['Principiante', 'Intermedio', 'Avanzado'];

/** Reglas espejo de las que valida AdminController en el backend. */
const VALIDATORS = {
  title: (v) => {
    if (!v.trim()) return 'El título es obligatorio';
    if (v.trim().length < 5) return 'El título debe tener al menos 5 caracteres';
    return '';
  },
  description: (v) => {
    if (!v.trim()) return 'La descripción es obligatoria';
    if (v.trim().length < 20) return 'La descripción debe tener al menos 20 caracteres';
    return '';
  },
  technology: (v) => (v.trim() ? '' : 'La tecnología es obligatoria'),
  level: (v) => (LEVELS.includes(v) ? '' : 'Selecciona un nivel válido'),
  price: (v) => {
    if (v === '' || v === null) return 'El precio es obligatorio';
    if (Number.isNaN(Number(v)) || Number(v) < 0) return 'El precio no puede ser negativo';
    return '';
  },
  duration: (v) => {
    if (v === '' || v === null) return 'La duración es obligatoria';
    if (!Number.isInteger(Number(v)) || Number(v) <= 0) return 'La duración debe ser mayor a cero';
    return '';
  },
  capacity: (v) => {
    if (v === '' || v === null) return 'El cupo es obligatorio';
    if (!Number.isInteger(Number(v)) || Number(v) <= 0) return 'El cupo debe ser mayor a cero';
    return '';
  },
};

const EMPTY = {
  title: '',
  description: '',
  longDescription: '',
  technology: 'Python',
  level: 'Principiante',
  price: '',
  duration: '',
  capacity: '50',
  imageUrl: '',
  curriculum: '',
  prerequisites: '',
  learningObjectives: '',
  targetAudience: '',
  previewVideoUrl: '',
  instructorName: '',
};

export default function CourseForm() {
  const { id } = useParams();
  const navigate = useNavigate();
  const isEdit = Boolean(id);

  const [form, setForm] = useState(EMPTY);
  const [enrolledCount, setEnrolledCount] = useState(0);
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [errors, setErrors] = useState({});
  const [touched, setTouched] = useState({});

  useEffect(() => {
    if (isEdit) loadCourse();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  const pipe = (v) => (Array.isArray(v) ? v.join('\n') : (v || '').split('|').join('\n'));

  const loadCourse = async () => {
    setLoading(true);
    try {
      const c = await getCourseById(id);
      setEnrolledCount(c.studentCount || 0);
      setForm({
        title: c.title || '',
        description: c.description || '',
        longDescription: c.longDescription || '',
        technology: c.technology || 'Python',
        level: c.level || 'Principiante',
        price: String(c.price ?? ''),
        duration: String(c.duration ?? ''),
        capacity: String(c.capacity ?? 50),
        imageUrl: c.imageUrl || '',
        curriculum: pipe(c.curriculum),
        prerequisites: pipe(c.prerequisites),
        learningObjectives: pipe(c.learningObjectives),
        targetAudience: pipe(c.targetAudience),
        previewVideoUrl: c.previewVideoUrl || '',
        instructorName: c.instructorName || '',
      });
    } catch (e) {
      setError(errorMessage(e, 'No se pudo cargar el curso.'));
    } finally {
      setLoading(false);
    }
  };

  const validateField = (name, value) => VALIDATORS[name]?.(value) ?? '';

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm((f) => ({ ...f, [name]: value }));
    if (touched[name]) setErrors((p) => ({ ...p, [name]: validateField(name, value) }));
    setError('');
  };

  const handleBlur = (e) => {
    const { name, value } = e.target;
    setTouched((t) => ({ ...t, [name]: true }));
    setErrors((p) => ({ ...p, [name]: validateField(name, value) }));
  };

  const formValid = Object.keys(VALIDATORS).every((k) => !validateField(k, form[k]));

  const handleSubmit = async (e) => {
    e.preventDefault();
    const next = {};
    for (const k of Object.keys(VALIDATORS)) {
      const m = validateField(k, form[k]);
      if (m) next[k] = m;
    }
    setErrors(next);
    setTouched(Object.fromEntries(Object.keys(VALIDATORS).map((k) => [k, true])));

    if (Object.keys(next).length) {
      setError('Revisa los campos marcados antes de guardar.');
      return;
    }
    if (isEdit && Number(form.capacity) < enrolledCount) {
      setError(`El cupo no puede ser menor a los ${enrolledCount} estudiantes ya matriculados.`);
      return;
    }

    setSaving(true);
    setError('');
    try {
      // Las listas viajan separadas por '|' como espera el backend.
      const lines = (v) =>
        v.split('\n').map((s) => s.trim()).filter(Boolean).join('|') || null;

      const payload = {
        title: form.title.trim(),
        description: form.description.trim(),
        longDescription: form.longDescription.trim() || null,
        technology: form.technology,
        level: form.level,
        price: parseFloat(form.price),
        duration: parseInt(form.duration, 10),
        capacity: parseInt(form.capacity, 10),
        imageUrl: form.imageUrl.trim() || null,
        curriculum: lines(form.curriculum),
        prerequisites: lines(form.prerequisites),
        learningObjectives: lines(form.learningObjectives),
        targetAudience: lines(form.targetAudience),
        previewVideoUrl: form.previewVideoUrl.trim() || null,
        instructorName: form.instructorName.trim() || null,
      };

      if (isEdit) await adminUpdateCourse(id, payload);
      else await adminCreateCourse(payload);

      navigate('/admin');
    } catch (e) {
      setError(errorMessage(e, 'No se pudo guardar el curso.'));
    } finally {
      setSaving(false);
    }
  };

  const inputCls = (name) =>
    `w-full px-4 py-2.5 border rounded-lg text-sm focus:outline-none focus:ring-2 transition ${
      touched[name] && errors[name]
        ? 'border-red-400 focus:ring-red-400 bg-red-50'
        : 'border-gray-300 focus:ring-blue-500'
    }`;
  const label = 'block text-sm font-medium text-gray-700 mb-1';
  const Err = ({ name }) =>
    touched[name] && errors[name] ? (
      <p className="text-xs text-red-600 mt-1">{errors[name]}</p>
    ) : null;

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

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <main className="max-w-3xl mx-auto px-6 py-8">
        <button
          onClick={() => navigate('/admin')}
          className="text-sm text-gray-500 hover:text-gray-800 mb-4 transition"
        >
          ← Volver al panel
        </button>

        <h1 className="text-2xl font-bold text-gray-900 mb-1">
          {isEdit ? 'Editar curso' : 'Nuevo curso'}
        </h1>
        <p className="text-sm text-gray-500 mb-6">
          Los campos marcados con <span className="text-red-500">*</span> son obligatorios.
        </p>

        {error && (
          <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg mb-5 text-sm">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-6" noValidate>
          <section className="bg-white rounded-2xl border border-gray-100 p-6 space-y-4">
            <h2 className="font-semibold text-gray-900">Información básica</h2>

            <div>
              <label className={label}>
                Título <span className="text-red-500">*</span>
              </label>
              <input
                name="title"
                value={form.title}
                onChange={handleChange}
                onBlur={handleBlur}
                className={inputCls('title')}
                placeholder="Fundamentos de Python"
              />
              <Err name="title" />
            </div>

            <div>
              <label className={label}>
                Descripción corta <span className="text-red-500">*</span>{' '}
                <span className="text-gray-400 font-normal">({form.description.length} car.)</span>
              </label>
              <textarea
                name="description"
                rows={2}
                value={form.description}
                onChange={handleChange}
                onBlur={handleBlur}
                className={`${inputCls('description')} resize-none`}
                placeholder="Aparece en la tarjeta del catálogo. Mínimo 20 caracteres."
              />
              <Err name="description" />
            </div>

            <div>
              <label className={label}>Descripción detallada</label>
              <textarea
                name="longDescription"
                rows={6}
                value={form.longDescription}
                onChange={handleChange}
                className={`${inputCls('longDescription')} resize-none`}
                placeholder="Texto largo de la ficha del curso. Separa los párrafos con una línea en blanco."
              />
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div>
                <label className={label}>
                  Tecnología <span className="text-red-500">*</span>
                </label>
                <select
                  name="technology"
                  value={form.technology}
                  onChange={handleChange}
                  className={inputCls('technology')}
                >
                  {TECHNOLOGIES.map((t) => (
                    <option key={t}>{t}</option>
                  ))}
                </select>
              </div>
              <div>
                <label className={label}>
                  Nivel <span className="text-red-500">*</span>
                </label>
                <select
                  name="level"
                  value={form.level}
                  onChange={handleChange}
                  className={inputCls('level')}
                >
                  {LEVELS.map((l) => (
                    <option key={l}>{l}</option>
                  ))}
                </select>
              </div>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
              <div>
                <label className={label}>
                  Precio (COP) <span className="text-red-500">*</span>
                </label>
                <input
                  name="price"
                  type="number"
                  min="0"
                  step="100"
                  value={form.price}
                  onChange={handleChange}
                  onBlur={handleBlur}
                  className={inputCls('price')}
                  placeholder="79900"
                />
                <Err name="price" />
              </div>
              <div>
                <label className={label}>
                  Duración (horas) <span className="text-red-500">*</span>
                </label>
                <input
                  name="duration"
                  type="number"
                  min="1"
                  value={form.duration}
                  onChange={handleChange}
                  onBlur={handleBlur}
                  className={inputCls('duration')}
                  placeholder="40"
                />
                <Err name="duration" />
              </div>
              <div>
                <label className={label}>
                  Cupos <span className="text-red-500">*</span>
                </label>
                <input
                  name="capacity"
                  type="number"
                  min={isEdit ? enrolledCount || 1 : 1}
                  value={form.capacity}
                  onChange={handleChange}
                  onBlur={handleBlur}
                  className={inputCls('capacity')}
                  placeholder="50"
                />
                <Err name="capacity" />
                {isEdit && enrolledCount > 0 && (
                  <p className="text-xs text-gray-400 mt-1">
                    Mínimo {enrolledCount}: ya hay estudiantes matriculados.
                  </p>
                )}
              </div>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div>
                <label className={label}>Instructor</label>
                <input
                  name="instructorName"
                  value={form.instructorName}
                  onChange={handleChange}
                  className={inputCls('instructorName')}
                  placeholder="Nombre visible del instructor"
                />
              </div>
              <div>
                <label className={label}>Imagen (URL)</label>
                <input
                  name="imageUrl"
                  value={form.imageUrl}
                  onChange={handleChange}
                  className={inputCls('imageUrl')}
                  placeholder="/images/curso.png"
                />
              </div>
            </div>

            <div>
              <label className={label}>Video de presentación (embed de YouTube)</label>
              <input
                name="previewVideoUrl"
                value={form.previewVideoUrl}
                onChange={handleChange}
                className={inputCls('previewVideoUrl')}
                placeholder="https://www.youtube.com/embed/VIDEO_ID"
              />
              <p className="text-xs text-gray-400 mt-1">
                Usa el formato /embed/ para que se pueda reproducir dentro de la plataforma.
              </p>
            </div>
          </section>

          <section className="bg-white rounded-2xl border border-gray-100 p-6 space-y-4">
            <h2 className="font-semibold text-gray-900">Contenido del curso</h2>
            <p className="text-xs text-gray-500 -mt-2">Escribe un elemento por línea.</p>

            <div>
              <label className={label}>Objetivos de aprendizaje</label>
              <textarea
                name="learningObjectives"
                rows={5}
                value={form.learningObjectives}
                onChange={handleChange}
                className={`${inputCls('learningObjectives')} resize-none`}
                placeholder={'Escribir programas con soltura\nModelar problemas reales\nAplicar POO'}
              />
            </div>

            <div>
              <label className={label}>Temario</label>
              <textarea
                name="curriculum"
                rows={5}
                value={form.curriculum}
                onChange={handleChange}
                className={`${inputCls('curriculum')} resize-none`}
                placeholder={'Introducción\nVariables y tipos\nEstructuras de control'}
              />
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div>
                <label className={label}>Requisitos</label>
                <textarea
                  name="prerequisites"
                  rows={4}
                  value={form.prerequisites}
                  onChange={handleChange}
                  className={`${inputCls('prerequisites')} resize-none`}
                  placeholder={'Computador\nEditor de código'}
                />
              </div>
              <div>
                <label className={label}>Para quién es</label>
                <textarea
                  name="targetAudience"
                  rows={4}
                  value={form.targetAudience}
                  onChange={handleChange}
                  className={`${inputCls('targetAudience')} resize-none`}
                  placeholder={'Personas sin experiencia\nEstudiantes de carreras técnicas'}
                />
              </div>
            </div>
          </section>

          <div className="flex gap-3">
            <button
              type="submit"
              disabled={saving || !formValid}
              className="bg-blue-600 text-white px-6 py-2.5 rounded-lg font-medium text-sm hover:bg-blue-700 disabled:bg-gray-300 disabled:cursor-not-allowed transition"
            >
              {saving ? 'Guardando...' : isEdit ? 'Guardar cambios' : 'Crear curso'}
            </button>
            <button
              type="button"
              onClick={() => navigate('/admin')}
              className="px-5 py-2.5 rounded-lg font-medium text-sm border border-gray-200 text-gray-600 hover:bg-gray-50 transition"
            >
              Cancelar
            </button>
          </div>
        </form>
      </main>
    </div>
  );
}
