import { useState, useEffect, useRef } from 'react';
import Navbar from '../components/Navbar';
import {
  getMyProfile,
  updateMyProfile,
  changeMyPassword,
  updateMyAvatar,
  errorMessage,
} from '../services/courseService';

const INTEREST_OPTIONS = [
  'Python', 'JavaScript', 'React', 'Java', 'Spring Boot', 'Kotlin', 'Android',
  'MySQL', 'PostgreSQL', 'Git', 'Docker', 'Cloud', 'Ciberseguridad',
  'Ciencia de datos', 'Inteligencia artificial', 'DevOps', 'UX/UI', 'Testing',
];

const ACCENT_COLORS = [
  { hex: '#2563eb', name: 'Azul' },
  { hex: '#7c3aed', name: 'Violeta' },
  { hex: '#059669', name: 'Esmeralda' },
  { hex: '#dc2626', name: 'Rojo' },
  { hex: '#ea580c', name: 'Naranja' },
  { hex: '#0891b2', name: 'Cian' },
  { hex: '#db2777', name: 'Rosa' },
  { hex: '#4b5563', name: 'Grafito' },
];

const initials = (name) =>
  (name || '?')
    .trim()
    .split(/\s+/)
    .slice(0, 2)
    .map((w) => w[0])
    .join('')
    .toUpperCase();

function Stat({ label, value, accent }) {
  return (
    <div className="bg-white rounded-xl p-4 border border-gray-100 text-center">
      <p className="text-2xl font-bold" style={{ color: accent }}>{value}</p>
      <p className="text-xs text-gray-500 mt-0.5">{label}</p>
    </div>
  );
}

export default function Profile() {
  const fileRef = useRef(null);

  const [profile, setProfile] = useState(null);
  const [form, setForm] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [tab, setTab] = useState('datos');

  const [pwForm, setPwForm] = useState({
    currentPassword: '',
    newPassword: '',
    confirmPassword: '',
  });
  const [pwError, setPwError] = useState('');
  const [pwSuccess, setPwSuccess] = useState('');
  const [pwSaving, setPwSaving] = useState(false);

  useEffect(() => {
    load();
  }, []);

  const load = async () => {
    setLoading(true);
    try {
      const p = await getMyProfile();
      setProfile(p);
      setForm({
        name: p.name || '',
        displayName: p.displayName || '',
        bio: p.bio || '',
        jobTitle: p.jobTitle || '',
        phone: p.phone || '',
        country: p.country || '',
        city: p.city || '',
        interests: p.interests || [],
        linkedinUrl: p.linkedinUrl || '',
        githubUrl: p.githubUrl || '',
        websiteUrl: p.websiteUrl || '',
        themePreference: p.themePreference || 'system',
        accentColor: p.accentColor || '#2563eb',
        languagePreference: p.languagePreference || 'es',
        notifyEmail: p.notifyEmail,
        notifyNewCourses: p.notifyNewCourses,
        notifyProgress: p.notifyProgress,
        publicProfile: p.publicProfile,
      });
    } catch (e) {
      setError(errorMessage(e, 'No se pudo cargar tu perfil.'));
    } finally {
      setLoading(false);
    }
  };

  const set = (key, value) => {
    setForm((f) => ({ ...f, [key]: value }));
    setSuccess('');
    setError('');
  };

  const toggleInterest = (topic) => {
    const has = form.interests.includes(topic);
    if (!has && form.interests.length >= 10) {
      setError('Puedes elegir un máximo de 10 intereses.');
      return;
    }
    set('interests', has ? form.interests.filter((i) => i !== topic) : [...form.interests, topic]);
  };

  const save = async () => {
    setSaving(true);
    setError('');
    setSuccess('');
    try {
      const updated = await updateMyProfile(form);
      setProfile(updated);
      setSuccess('Cambios guardados correctamente.');
      // Mantiene sincronizado el nombre que muestra el Navbar.
      const stored = JSON.parse(localStorage.getItem('user') || '{}');
      localStorage.setItem(
        'user',
        JSON.stringify({ ...stored, name: updated.name, avatarUrl: updated.avatarUrl })
      );
      window.dispatchEvent(new Event('dieletech:profile-updated'));
    } catch (e) {
      setError(errorMessage(e, 'No se pudieron guardar los cambios.'));
    } finally {
      setSaving(false);
    }
  };

  const onAvatarPick = (e) => {
    const file = e.target.files?.[0];
    if (!file) return;
    if (!file.type.startsWith('image/')) {
      setError('El archivo debe ser una imagen.');
      return;
    }
    if (file.size > 350 * 1024) {
      setError('La imagen supera los 350 KB. Usa una versión más liviana.');
      return;
    }
    const reader = new FileReader();
    reader.onload = async () => {
      try {
        const updated = await updateMyAvatar(reader.result);
        setProfile(updated);
        setSuccess('Foto de perfil actualizada.');
        const stored = JSON.parse(localStorage.getItem('user') || '{}');
        localStorage.setItem('user', JSON.stringify({ ...stored, avatarUrl: updated.avatarUrl }));
        window.dispatchEvent(new Event('dieletech:profile-updated'));
      } catch (err) {
        setError(errorMessage(err, 'No se pudo actualizar la foto.'));
      }
    };
    reader.readAsDataURL(file);
  };

  const removeAvatar = async () => {
    try {
      const updated = await updateMyProfile({ ...form, avatarUrl: '' });
      setProfile(updated);
      setSuccess('Foto eliminada.');
      window.dispatchEvent(new Event('dieletech:profile-updated'));
    } catch (e) {
      setError(errorMessage(e, 'No se pudo eliminar la foto.'));
    }
  };

  const submitPassword = async (e) => {
    e.preventDefault();
    setPwError('');
    setPwSuccess('');
    if (pwForm.newPassword.length < 6) {
      setPwError('La nueva contraseña debe tener mínimo 6 caracteres.');
      return;
    }
    if (pwForm.newPassword !== pwForm.confirmPassword) {
      setPwError('La nueva contraseña y su confirmación no coinciden.');
      return;
    }
    setPwSaving(true);
    try {
      const res = await changeMyPassword(pwForm);
      setPwSuccess(res.message || 'Contraseña actualizada.');
      setPwForm({ currentPassword: '', newPassword: '', confirmPassword: '' });
    } catch (err) {
      setPwError(errorMessage(err, 'No se pudo cambiar la contraseña.'));
    } finally {
      setPwSaving(false);
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

  if (!profile || !form) {
    return (
      <div className="min-h-screen bg-gray-50">
        <Navbar />
        <div className="max-w-md mx-auto mt-24 bg-white rounded-2xl border border-gray-100 p-8 text-center">
          <p className="text-sm text-gray-500">{error || 'No se pudo cargar tu perfil.'}</p>
        </div>
      </div>
    );
  }

  const accent = form.accentColor;
  const input =
    'w-full border border-gray-300 rounded-lg px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent';
  const label = 'block text-sm font-medium text-gray-700 mb-1';

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <main className="max-w-5xl mx-auto px-6 py-8">
        {/* Encabezado */}
        <div
          className="rounded-2xl p-6 mb-6 text-white"
          style={{ background: `linear-gradient(135deg, ${accent}, ${accent}cc)` }}
        >
          <div className="flex flex-wrap items-center gap-5">
            <div className="relative">
              {profile.avatarUrl ? (
                <img
                  src={profile.avatarUrl}
                  alt={profile.name}
                  className="w-24 h-24 rounded-full object-cover border-4 border-white/30"
                />
              ) : (
                <div className="w-24 h-24 rounded-full bg-white/20 border-4 border-white/30 flex items-center justify-center text-3xl font-bold">
                  {initials(profile.displayName || profile.name)}
                </div>
              )}
              <button
                onClick={() => fileRef.current?.click()}
                title="Cambiar foto"
                className="absolute bottom-0 right-0 bg-white text-gray-700 rounded-full w-8 h-8 shadow flex items-center justify-center hover:bg-gray-50 transition"
              >
                ✎
              </button>
              <input
                ref={fileRef}
                type="file"
                accept="image/*"
                onChange={onAvatarPick}
                className="hidden"
              />
            </div>

            <div className="min-w-0 flex-1">
              <h1 className="text-2xl font-bold">{profile.displayName || profile.name}</h1>
              {profile.jobTitle && <p className="text-white/90 text-sm">{profile.jobTitle}</p>}
              <p className="text-white/70 text-sm">{profile.email}</p>
              <div className="flex flex-wrap items-center gap-2 mt-2">
                <span className="text-xs bg-white/20 px-2 py-0.5 rounded-full font-medium">
                  {profile.role}
                </span>
                <span className="text-xs bg-white/20 px-2 py-0.5 rounded-full">
                  {profile.verified ? 'Verificado' : 'Sin verificar'}
                </span>
                <span className="text-xs text-white/70">Miembro desde {profile.memberSince}</span>
              </div>
            </div>

            {profile.avatarUrl && (
              <button
                onClick={removeAvatar}
                className="text-xs text-white/80 hover:text-white underline self-start"
              >
                Quitar foto
              </button>
            )}
          </div>
        </div>

        {/* Estadísticas reales */}
        <div className="grid grid-cols-2 md:grid-cols-5 gap-3 mb-6">
          <Stat label="Cursos" value={profile.coursesEnrolled} accent={accent} />
          <Stat label="Completados" value={profile.coursesCompleted} accent={accent} />
          <Stat label="Lecciones" value={profile.lessonsCompleted} accent={accent} />
          <Stat label="Progreso medio" value={`${profile.averageProgress}%`} accent={accent} />
          <Stat
            label="Invertido"
            value={`$${Math.round(profile.totalInvested).toLocaleString('es-CO')}`}
            accent={accent}
          />
        </div>

        {error && (
          <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg mb-4 text-sm">
            {error}
          </div>
        )}
        {success && (
          <div className="bg-green-50 border border-green-200 text-green-700 px-4 py-3 rounded-lg mb-4 text-sm">
            {success}
          </div>
        )}

        <div className="flex gap-1 border-b border-gray-200 mb-6 overflow-x-auto">
          {[
            ['datos', 'Datos personales'],
            ['intereses', 'Intereses'],
            ['apariencia', 'Apariencia'],
            ['notificaciones', 'Notificaciones'],
            ['seguridad', 'Seguridad'],
          ].map(([k, l]) => (
            <button
              key={k}
              onClick={() => setTab(k)}
              className={`px-4 py-2.5 text-sm font-medium border-b-2 -mb-px transition whitespace-nowrap ${
                tab === k
                  ? 'border-blue-600 text-blue-600'
                  : 'border-transparent text-gray-500 hover:text-gray-800'
              }`}
            >
              {l}
            </button>
          ))}
        </div>

        <div className="bg-white rounded-2xl border border-gray-100 p-6">
          {/* ── DATOS ── */}
          {tab === 'datos' && (
            <div className="space-y-5">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label className={label}>Nombre completo</label>
                  <input
                    className={input}
                    value={form.name}
                    onChange={(e) => set('name', e.target.value)}
                  />
                </div>
                <div>
                  <label className={label}>Nombre para mostrar</label>
                  <input
                    className={input}
                    value={form.displayName}
                    onChange={(e) => set('displayName', e.target.value)}
                    placeholder="Cómo quieres que te llamemos"
                  />
                </div>
              </div>

              <div>
                <label className={label}>Cargo o rol profesional</label>
                <input
                  className={input}
                  value={form.jobTitle}
                  onChange={(e) => set('jobTitle', e.target.value)}
                  placeholder="Estudiante de Ingeniería de Sistemas"
                />
              </div>

              <div>
                <label className={label}>
                  Biografía{' '}
                  <span className="text-gray-400 font-normal">({form.bio.length}/500)</span>
                </label>
                <textarea
                  className={`${input} resize-none`}
                  rows={4}
                  maxLength={500}
                  value={form.bio}
                  onChange={(e) => set('bio', e.target.value)}
                  placeholder="Cuéntanos en qué estás trabajando y hacia dónde quieres llevar tu carrera."
                />
              </div>

              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div>
                  <label className={label}>Teléfono</label>
                  <input
                    className={input}
                    value={form.phone}
                    onChange={(e) => set('phone', e.target.value)}
                    placeholder="+57 300 123 4567"
                  />
                </div>
                <div>
                  <label className={label}>País</label>
                  <input
                    className={input}
                    value={form.country}
                    onChange={(e) => set('country', e.target.value)}
                    placeholder="Colombia"
                  />
                </div>
                <div>
                  <label className={label}>Ciudad</label>
                  <input
                    className={input}
                    value={form.city}
                    onChange={(e) => set('city', e.target.value)}
                    placeholder="Bogotá D.C."
                  />
                </div>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div>
                  <label className={label}>LinkedIn</label>
                  <input
                    className={input}
                    value={form.linkedinUrl}
                    onChange={(e) => set('linkedinUrl', e.target.value)}
                    placeholder="https://linkedin.com/in/tu-perfil"
                  />
                </div>
                <div>
                  <label className={label}>GitHub</label>
                  <input
                    className={input}
                    value={form.githubUrl}
                    onChange={(e) => set('githubUrl', e.target.value)}
                    placeholder="https://github.com/usuario"
                  />
                </div>
                <div>
                  <label className={label}>Sitio web</label>
                  <input
                    className={input}
                    value={form.websiteUrl}
                    onChange={(e) => set('websiteUrl', e.target.value)}
                    placeholder="https://tusitio.com"
                  />
                </div>
              </div>

              <label className="flex items-start gap-3 cursor-pointer pt-2">
                <input
                  type="checkbox"
                  checked={form.publicProfile}
                  onChange={(e) => set('publicProfile', e.target.checked)}
                  className="mt-1"
                />
                <span className="text-sm text-gray-600">
                  Hacer mi perfil visible para otros estudiantes de la plataforma.
                </span>
              </label>
            </div>
          )}

          {/* ── INTERESES ── */}
          {tab === 'intereses' && (
            <div>
              <p className="text-sm text-gray-600 mb-4">
                Elige hasta 10 temas. Los usamos para ordenar el catálogo según lo que te interesa.{' '}
                <span className="text-gray-400">({form.interests.length}/10)</span>
              </p>
              <div className="flex flex-wrap gap-2">
                {INTEREST_OPTIONS.map((topic) => {
                  const active = form.interests.includes(topic);
                  return (
                    <button
                      key={topic}
                      onClick={() => toggleInterest(topic)}
                      className={`px-3 py-1.5 rounded-full text-sm font-medium border transition ${
                        active
                          ? 'text-white border-transparent'
                          : 'bg-white text-gray-600 border-gray-200 hover:border-gray-400'
                      }`}
                      style={active ? { backgroundColor: accent } : undefined}
                    >
                      {topic}
                    </button>
                  );
                })}
              </div>
            </div>
          )}

          {/* ── APARIENCIA ── */}
          {tab === 'apariencia' && (
            <div className="space-y-6">
              <div>
                <label className={label}>Color de acento</label>
                <p className="text-xs text-gray-500 mb-3">
                  Define el color de tu perfil y de los indicadores de progreso.
                </p>
                <div className="flex flex-wrap gap-3">
                  {ACCENT_COLORS.map((c) => (
                    <button
                      key={c.hex}
                      onClick={() => set('accentColor', c.hex)}
                      title={c.name}
                      className={`w-11 h-11 rounded-full transition ring-offset-2 ${
                        form.accentColor === c.hex ? 'ring-2 ring-gray-800 scale-110' : 'hover:scale-105'
                      }`}
                      style={{ backgroundColor: c.hex }}
                    />
                  ))}
                </div>
              </div>

              <div>
                <label className={label}>Tema de la interfaz</label>
                <div className="grid grid-cols-3 gap-3 mt-2">
                  {[
                    ['light', 'Claro'],
                    ['dark', 'Oscuro'],
                    ['system', 'Según el sistema'],
                  ].map(([val, name]) => (
                    <button
                      key={val}
                      onClick={() => set('themePreference', val)}
                      className={`py-3 rounded-lg border text-sm font-medium transition ${
                        form.themePreference === val
                          ? 'border-blue-500 bg-blue-50 text-blue-700'
                          : 'border-gray-200 text-gray-600 hover:border-gray-300'
                      }`}
                    >
                      {name}
                    </button>
                  ))}
                </div>
              </div>

              <div>
                <label className={label}>Idioma</label>
                <select
                  className={`${input} max-w-xs`}
                  value={form.languagePreference}
                  onChange={(e) => set('languagePreference', e.target.value)}
                >
                  <option value="es">Español</option>
                  <option value="en">English</option>
                </select>
              </div>
            </div>
          )}

          {/* ── NOTIFICACIONES ── */}
          {tab === 'notificaciones' && (
            <div className="space-y-4">
              {[
                ['notifyEmail', 'Correos de la plataforma', 'Avisos de cuenta, compras y certificados.'],
                ['notifyNewCourses', 'Nuevos cursos', 'Te avisamos cuando publiquemos algo de tus intereses.'],
                ['notifyProgress', 'Recordatorios de progreso', 'Un recordatorio si llevas días sin retomar un curso.'],
              ].map(([key, title, desc]) => (
                <label
                  key={key}
                  className="flex items-start justify-between gap-4 p-4 border border-gray-100 rounded-xl cursor-pointer hover:border-gray-200 transition"
                >
                  <span>
                    <span className="block text-sm font-medium text-gray-800">{title}</span>
                    <span className="block text-xs text-gray-500 mt-0.5">{desc}</span>
                  </span>
                  <input
                    type="checkbox"
                    checked={form[key]}
                    onChange={(e) => set(key, e.target.checked)}
                    className="mt-1 w-5 h-5"
                  />
                </label>
              ))}
            </div>
          )}

          {/* ── SEGURIDAD ── */}
          {tab === 'seguridad' && (
            <form onSubmit={submitPassword} className="max-w-md space-y-4">
              <h3 className="font-semibold text-gray-900">Cambiar contraseña</h3>

              {pwError && (
                <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-2.5 rounded-lg text-sm">
                  {pwError}
                </div>
              )}
              {pwSuccess && (
                <div className="bg-green-50 border border-green-200 text-green-700 px-4 py-2.5 rounded-lg text-sm">
                  {pwSuccess}
                </div>
              )}

              <div>
                <label className={label}>Contraseña actual</label>
                <input
                  type="password"
                  className={input}
                  value={pwForm.currentPassword}
                  onChange={(e) => setPwForm({ ...pwForm, currentPassword: e.target.value })}
                  required
                />
              </div>
              <div>
                <label className={label}>Nueva contraseña</label>
                <input
                  type="password"
                  className={input}
                  value={pwForm.newPassword}
                  onChange={(e) => setPwForm({ ...pwForm, newPassword: e.target.value })}
                  placeholder="Mínimo 6 caracteres"
                  required
                />
              </div>
              <div>
                <label className={label}>Confirmar nueva contraseña</label>
                <input
                  type="password"
                  className={input}
                  value={pwForm.confirmPassword}
                  onChange={(e) => setPwForm({ ...pwForm, confirmPassword: e.target.value })}
                  required
                />
              </div>

              <button
                type="submit"
                disabled={pwSaving}
                className="bg-gray-900 text-white px-5 py-2.5 rounded-lg font-medium text-sm hover:bg-gray-800 disabled:opacity-50 transition"
              >
                {pwSaving ? 'Guardando...' : 'Cambiar contraseña'}
              </button>
            </form>
          )}

          {tab !== 'seguridad' && (
            <div className="flex items-center gap-3 mt-8 pt-6 border-t border-gray-100">
              <button
                onClick={save}
                disabled={saving}
                className="text-white px-6 py-2.5 rounded-lg font-medium text-sm disabled:opacity-50 transition"
                style={{ backgroundColor: accent }}
              >
                {saving ? 'Guardando...' : 'Guardar cambios'}
              </button>
              <button
                onClick={load}
                disabled={saving}
                className="px-5 py-2.5 rounded-lg font-medium text-sm border border-gray-200 text-gray-600 hover:bg-gray-50 transition"
              >
                Descartar
              </button>
            </div>
          )}
        </div>
      </main>
    </div>
  );
}
