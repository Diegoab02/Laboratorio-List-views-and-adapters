import { useState, useEffect, useRef } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { getUser, isAuthenticated, logout } from '../services/authService';
import { onInstallAvailable, promptInstall } from '../pwa';

const initials = (name) =>
  (name || '?')
    .trim()
    .split(/\s+/)
    .slice(0, 2)
    .map((w) => w[0])
    .join('')
    .toUpperCase();

export default function Navbar() {
  const navigate = useNavigate();
  const location = useLocation();
  const menuRef = useRef(null);

  const [user, setUser] = useState(getUser());
  const [open, setOpen] = useState(false);
  const [canInstall, setCanInstall] = useState(false);
  const authed = isAuthenticated();

  // El perfil puede cambiar el nombre o el avatar: escuchamos el evento.
  useEffect(() => {
    const refresh = () => setUser(getUser());
    window.addEventListener('dieletech:profile-updated', refresh);
    window.addEventListener('storage', refresh);
    return () => {
      window.removeEventListener('dieletech:profile-updated', refresh);
      window.removeEventListener('storage', refresh);
    };
  }, []);

  useEffect(() => {
    const onClickOutside = (e) => {
      if (menuRef.current && !menuRef.current.contains(e.target)) setOpen(false);
    };
    document.addEventListener('mousedown', onClickOutside);
    return () => document.removeEventListener('mousedown', onClickOutside);
  }, []);

  useEffect(() => setOpen(false), [location.pathname]);

  // HU-12: el botón solo aparece si el navegador considera la app instalable.
  useEffect(() => onInstallAvailable(setCanInstall), []);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const canManage = user?.role === 'INSTRUCTOR' || user?.role === 'ADMIN';

  const linkCls = (path) =>
    `text-sm font-medium transition ${
      location.pathname === path ? 'text-blue-600' : 'text-gray-600 hover:text-blue-600'
    }`;

  return (
    <nav className="bg-white border-b border-gray-100 sticky top-0 z-50">
      <div className="max-w-7xl mx-auto px-4 py-3 flex items-center justify-between">
        <Link to="/catalog" className="flex items-baseline gap-2">
          <span className="text-xl font-extrabold text-gray-900">Dieletech</span>
          <span className="text-xs text-blue-500 hidden sm:inline">Tu ruta hacia tu 1° en Tech</span>
        </Link>

        <div className="flex items-center gap-3 sm:gap-5">
          {canInstall && (
            <button
              onClick={promptInstall}
              title="Instalar Dieletech en tu dispositivo"
              className="hidden sm:inline-flex items-center gap-1.5 text-xs font-semibold text-blue-700 bg-blue-50 border border-blue-100 px-3 py-1.5 rounded-lg hover:bg-blue-100 transition"
            >
              Instalar app
            </button>
          )}

          <Link to="/catalog" className={linkCls('/catalog')}>
            Catálogo
          </Link>

          {authed ? (
            <>
              <Link to="/dashboard" className={linkCls('/dashboard')}>
                Mis Cursos
              </Link>

              {canManage && (
                <Link to="/admin" className={linkCls('/admin')}>
                  {user.role === 'ADMIN' ? 'Administración' : 'Panel Instructor'}
                </Link>
              )}

              <div className="relative" ref={menuRef}>
                <button
                  onClick={() => setOpen((o) => !o)}
                  className="flex items-center gap-2 group"
                  aria-label="Menú de usuario"
                >
                  {user?.avatarUrl ? (
                    <img
                      src={user.avatarUrl}
                      alt={user.name}
                      className="w-8 h-8 rounded-full object-cover border border-gray-200"
                    />
                  ) : (
                    <span className="w-8 h-8 rounded-full bg-blue-600 text-white text-xs font-bold flex items-center justify-center">
                      {initials(user?.name)}
                    </span>
                  )}
                  <span className="text-sm text-gray-700 hidden sm:inline group-hover:text-blue-600 transition">
                    {user?.name?.split(' ')[0]}
                  </span>
                </button>

                {open && (
                  <div className="absolute right-0 mt-2 w-56 bg-white rounded-xl border border-gray-100 shadow-lg py-1.5 z-50">
                    <div className="px-4 py-2.5 border-b border-gray-50">
                      <p className="text-sm font-medium text-gray-900 truncate">{user?.name}</p>
                      <p className="text-xs text-gray-400 truncate">{user?.email}</p>
                      <span className="inline-block mt-1.5 text-[10px] px-1.5 py-0.5 bg-gray-100 text-gray-600 rounded font-semibold">
                        {user?.role}
                      </span>
                    </div>

                    <Link
                      to="/profile"
                      className="block px-4 py-2.5 text-sm text-gray-700 hover:bg-gray-50 transition"
                    >
                      Mi perfil
                    </Link>
                    <Link
                      to="/dashboard"
                      className="block px-4 py-2.5 text-sm text-gray-700 hover:bg-gray-50 transition"
                    >
                      Mis cursos
                    </Link>
                    <Link
                      to="/certificados"
                      className="block px-4 py-2.5 text-sm text-gray-700 hover:bg-gray-50 transition"
                    >
                      Mis certificados
                    </Link>
                    {canManage && (
                      <Link
                        to="/admin"
                        className="block px-4 py-2.5 text-sm text-gray-700 hover:bg-gray-50 transition"
                      >
                        {user.role === 'ADMIN' ? 'Administración' : 'Panel Instructor'}
                      </Link>
                    )}

                    {canInstall && (
                      <button
                        onClick={promptInstall}
                        className="w-full text-left px-4 py-2.5 text-sm text-blue-700 hover:bg-blue-50 transition sm:hidden"
                      >
                        Instalar app
                      </button>
                    )}

                    <div className="border-t border-gray-50 mt-1 pt-1">
                      <button
                        onClick={handleLogout}
                        className="w-full text-left px-4 py-2.5 text-sm text-red-500 hover:bg-red-50 transition"
                      >
                        Cerrar sesión
                      </button>
                    </div>
                  </div>
                )}
              </div>
            </>
          ) : (
            <>
              <Link to="/login" className={linkCls('/login')}>
                Iniciar sesión
              </Link>
              <Link
                to="/register"
                className="text-sm font-medium bg-blue-600 text-white px-4 py-1.5 rounded-lg hover:bg-blue-700 transition"
              >
                Registrarse
              </Link>
            </>
          )}
        </div>
      </div>
    </nav>
  );
}
