import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar';
import { getCourseById, getLessons, adminCreateLesson, adminUpdateLesson, adminDeleteLesson } from '../services/courseService';

const EMPTY_LESSON = { title: '', description: '', videoUrl: '', orderIndex: 0, durationMinutes: 0, contentType: 'VIDEO', materialUrl: '', freePreview: false };

export default function LessonManager() {
  const { courseId } = useParams();
  const navigate = useNavigate();
  const [course, setCourse] = useState(null);
  const [lessons, setLessons] = useState([]);
  const [loading, setLoading] = useState(true);
  const [editingLesson, setEditingLesson] = useState(null);
  const [form, setForm] = useState({ ...EMPTY_LESSON });
  const [saving, setSaving] = useState(false);
  const [showForm, setShowForm] = useState(false);

  useEffect(() => { load(); }, [courseId]);

  const load = async () => {
    setLoading(true);
    try {
      const [c, l] = await Promise.all([getCourseById(courseId), getLessons(courseId)]);
      setCourse(c);
      setLessons(l);
    } finally {
      setLoading(false);
    }
  };

  const openNew = () => {
    setEditingLesson(null);
    setForm({ ...EMPTY_LESSON, orderIndex: lessons.length + 1 });
    setShowForm(true);
  };

  const openEdit = (lesson) => {
    setEditingLesson(lesson);
    setForm({
      title: lesson.title || '',
      description: lesson.description || '',
      videoUrl: lesson.videoUrl || '',
      orderIndex: lesson.orderIndex || 0,
      durationMinutes: lesson.durationMinutes || 0,
      freePreview: lesson.freePreview || false,
      contentType: lesson.contentType || 'VIDEO',
      materialUrl: lesson.materialUrl || '',
    });
    setShowForm(true);
  };

  const handleChange = (e) => {
    const val = e.target.type === 'number' ? parseInt(e.target.value) || 0 : e.target.value;
    setForm({ ...form, [e.target.name]: val });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!form.title) return;
    setSaving(true);
    try {
      if (editingLesson) {
        await adminUpdateLesson(editingLesson.id, form);
      } else {
        await adminCreateLesson(courseId, form);
      }
      setShowForm(false);
      await load();
    } catch (err) {
      alert('Error: ' + err.message);
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (lessonId, title) => {
    if (!window.confirm(`¿Eliminar la lección "${title}"?`)) return;
    try {
      await adminDeleteLesson(lessonId);
      await load();
    } catch (e) {
      alert('Error: ' + e.message);
    }
  };

  if (loading) return (
    <div className="min-h-screen bg-gray-50"><Navbar />
      <div className="flex items-center justify-center py-20">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    </div>
  );

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <main className="max-w-4xl mx-auto px-6 py-10">
        <button onClick={() => navigate('/admin')} className="text-blue-600 hover:text-blue-800 text-sm mb-6 inline-block">
          ← Volver al panel
        </button>

        <div className="flex justify-between items-start mb-8">
          <div>
            <h1 className="text-2xl font-bold text-gray-900">Lecciones</h1>
            <p className="text-gray-500 text-sm mt-1">{course?.title} · {lessons.length} lecciones</p>
          </div>
          <button onClick={openNew}
            className="bg-blue-600 text-white px-4 py-2 rounded-lg font-medium hover:bg-blue-700 transition flex items-center gap-1">
            <span className="text-lg">+</span> Nueva Lección
          </button>
        </div>

        {/* Formulario de lección */}
        {showForm && (
          <div className="bg-white rounded-xl border border-blue-200 p-6 mb-6">
            <h3 className="font-semibold text-gray-900 mb-4">
              {editingLesson ? `Editar: ${editingLesson.title}` : 'Nueva Lección'}
            </h3>
            <form onSubmit={handleSubmit} className="space-y-4">
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div className="sm:col-span-2">
                  <label className="block text-sm font-medium text-gray-700 mb-1">Título *</label>
                  <input name="title" value={form.title} onChange={handleChange}
                    className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:ring-2 focus:ring-blue-500"
                    placeholder="Introducción a Python" />
                </div>
                <div className="sm:col-span-2">
                  <label className="block text-sm font-medium text-gray-700 mb-1">Descripción</label>
                  <textarea name="description" value={form.description} onChange={handleChange} rows={2}
                    className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:ring-2 focus:ring-blue-500"
                    placeholder="Qué aprenderá el estudiante en esta lección" />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">URL del video (YouTube embed)</label>
                  <input name="videoUrl" value={form.videoUrl} onChange={handleChange}
                    className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:ring-2 focus:ring-blue-500"
                    placeholder="https://www.youtube.com/embed/..." />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">URL de material</label>
                  <input name="materialUrl" value={form.materialUrl} onChange={handleChange}
                    className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:ring-2 focus:ring-blue-500"
                    placeholder="https://..." />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Orden</label>
                  <input name="orderIndex" type="number" value={form.orderIndex} onChange={handleChange}
                    className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:ring-2 focus:ring-blue-500" />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Duración (min)</label>
                  <input name="durationMinutes" type="number" value={form.durationMinutes} onChange={handleChange}
                    className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:ring-2 focus:ring-blue-500" />
                </div>
                <label className="sm:col-span-2 flex items-start gap-3 cursor-pointer p-3 border border-gray-200 rounded-lg hover:border-gray-300 transition">
                <input
                  type="checkbox"
                  name="freePreview"
                  checked={form.freePreview}
                  onChange={(e) => setForm({ ...form, freePreview: e.target.checked })}
                  className="mt-0.5"
                />
                <span>
                  <span className="block text-sm font-medium text-gray-800">Leccion de muestra gratuita</span>
                  <span className="block text-xs text-gray-500">
                    Visible para cualquier visitante sin necesidad de comprar el curso.
                  </span>
                  </span>
                </label>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Tipo de contenido</label>
                  <select name="contentType" value={form.contentType} onChange={handleChange}
                    className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:ring-2 focus:ring-blue-500">
                    <option value="VIDEO">Video</option>
                    <option value="PDF">PDF / Documento</option>
                    <option value="QUIZ">Quiz</option>
                  </select>
                </div>
              </div>
              <div className="flex justify-end gap-2 pt-3 border-t border-gray-100">
                <button type="button" onClick={() => setShowForm(false)}
                  className="px-4 py-2 border border-gray-300 rounded-lg text-gray-700 hover:bg-gray-50">
                  Cancelar
                </button>
                <button type="submit" disabled={saving}
                  className="px-4 py-2 bg-blue-600 text-white rounded-lg font-medium hover:bg-blue-700 disabled:opacity-50">
                  {saving ? 'Guardando...' : editingLesson ? 'Actualizar' : 'Crear Lección'}
                </button>
              </div>
            </form>
          </div>
        )}

        {/* Lista de lecciones */}
        <div className="space-y-2">
          {lessons.length === 0 && (
            <div className="bg-white rounded-xl border border-gray-100 p-8 text-center text-gray-500">
              Este curso aún no tiene lecciones. Crea la primera.
            </div>
          )}
          {lessons.map((l) => (
            <div key={l.id} className="bg-white rounded-xl border border-gray-100 p-4 flex items-center gap-4 hover:shadow-sm transition">
              <div className="w-8 h-8 rounded-full bg-blue-100 text-blue-700 flex items-center justify-center text-sm font-bold flex-shrink-0">
                {l.orderIndex}
              </div>
              <div className="flex-1 min-w-0">
                <p className="font-medium text-gray-900 truncate">{l.title}</p>
                <p className="text-xs text-gray-400">
                  {l.contentType} · {l.durationMinutes} min{l.freePreview ? ' · GRATIS' : ''}
                  {l.videoUrl && ' · Video configurado'}
                </p>
              </div>
              <div className="flex gap-2 flex-shrink-0">
                <button onClick={() => openEdit(l)}
                  className="text-blue-600 hover:text-blue-800 text-sm font-medium">
                  Editar
                </button>
                <button onClick={() => handleDelete(l.id, l.title)}
                  className="text-red-500 hover:text-red-700 text-sm font-medium">
                  Eliminar
                </button>
              </div>
            </div>
          ))}
        </div>
      </main>
    </div>
  );
}
