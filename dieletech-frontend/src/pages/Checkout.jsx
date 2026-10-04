import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar';
import {
  getCourseById,
  purchaseCourse,
  checkPurchase,
  errorMessage,
  fieldErrors,
} from '../services/courseService';
import { getUser } from '../services/authService';

const PAYMENT_METHODS = [
  { value: 'card', label: 'Tarjeta de crédito/débito', hint: 'Visa, Mastercard, American Express' },
  { value: 'pse', label: 'PSE — Débito bancario', hint: 'Débito desde tu cuenta de ahorros o corriente' },
  { value: 'paypal', label: 'PayPal', hint: 'Serás redirigido para autorizar el pago' },
];

/** Reglas de validación espejo de las del backend (DTO con Bean Validation). */
const VALIDATORS = {
  fullName: (v) => {
    const t = v.trim();
    if (!t) return 'El nombre completo es obligatorio';
    if (t.length < 5) return 'El nombre debe tener al menos 5 caracteres';
    if (t.length > 100) return 'El nombre no puede exceder 100 caracteres';
    if (!/^[\p{L} .'-]+$/u.test(t)) return 'El nombre solo puede contener letras y espacios';
    return '';
  },
  documentId: (v) => {
    const t = v.trim();
    if (!t) return 'El documento de identidad es obligatorio';
    if (!/^\d{6,15}$/.test(t)) return 'El documento debe tener entre 6 y 15 dígitos';
    return '';
  },
  phone: (v) => {
    const t = v.trim();
    if (!t) return 'El teléfono es obligatorio';
    if (!/^[0-9+ ()-]{7,20}$/.test(t)) return 'El teléfono no tiene un formato válido';
    return '';
  },
  paymentMethod: (v) => (v ? '' : 'Selecciona un método de pago'),
  acceptTerms: (v) => (v ? '' : 'Debes aceptar los términos y condiciones para continuar'),
};

export default function Checkout() {
  const { id } = useParams();
  const navigate = useNavigate();
  const user = getUser();

  const [course, setCourse] = useState(null);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState('');
  const [submitError, setSubmitError] = useState('');
  const [purchasing, setPurchasing] = useState(false);
  const [alreadyOwned, setAlreadyOwned] = useState(false);

  // El correo siempre viene de la sesión: nunca es editable.
  const sessionEmail = (user?.email || '').trim().toLowerCase();

  const [form, setForm] = useState({
    fullName: user?.name || '',
    documentId: '',
    phone: '',
    paymentMethod: '',
    acceptTerms: false,
  });
  const [errors, setErrors] = useState({});
  const [touched, setTouched] = useState({});

  useEffect(() => {
    if (!sessionEmail) {
      navigate('/login', { replace: true });
      return;
    }
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  const load = async () => {
    try {
      setLoading(true);
      setLoadError('');
      const [data, owned] = await Promise.all([
        getCourseById(id),
        checkPurchase(id, sessionEmail),
      ]);
      setCourse(data);
      setAlreadyOwned(owned);
    } catch (err) {
      setLoadError(errorMessage(err, 'No se pudo cargar el curso. Verifica que el backend esté corriendo.'));
    } finally {
      setLoading(false);
    }
  };

  const validateField = (name, value) => VALIDATORS[name]?.(value) ?? '';

  const validateAll = () => {
    const next = {};
    for (const key of Object.keys(VALIDATORS)) {
      const msg = validateField(key, form[key]);
      if (msg) next[key] = msg;
    }
    setErrors(next);
    setTouched(Object.fromEntries(Object.keys(VALIDATORS).map((k) => [k, true])));
    return Object.keys(next).length === 0;
  };

  const handleChange = (e) => {
    const { name, type, value, checked } = e.target;
    const val = type === 'checkbox' ? checked : value;
    setForm((prev) => ({ ...prev, [name]: val }));
    if (touched[name]) {
      setErrors((prev) => ({ ...prev, [name]: validateField(name, val) }));
    }
    setSubmitError('');
  };

  const handleBlur = (e) => {
    const { name, type, value, checked } = e.target;
    const val = type === 'checkbox' ? checked : value;
    setTouched((prev) => ({ ...prev, [name]: true }));
    setErrors((prev) => ({ ...prev, [name]: validateField(name, val) }));
  };

  const formComplete =
    Object.keys(VALIDATORS).every((k) => !validateField(k, form[k])) && !alreadyOwned;

  const handlePurchase = async (e) => {
    e.preventDefault();
    setSubmitError('');

    if (!validateAll()) {
      setSubmitError('Revisa los campos marcados antes de continuar.');
      return;
    }
    if (alreadyOwned) {
      setSubmitError('Ya tienes este curso.');
      return;
    }
    if (course?.soldOut) {
      setSubmitError('No quedan cupos disponibles para este curso.');
      return;
    }

    try {
      setPurchasing(true);
      await purchaseCourse({
        courseId: parseInt(id, 10),
        fullName: form.fullName.trim(),
        email: sessionEmail,
        documentId: form.documentId.trim(),
        phone: form.phone.trim(),
        paymentMethod: form.paymentMethod,
        amount: course.price,
        acceptTerms: form.acceptTerms,
      });
      navigate('/purchase-success', { state: { courseName: course.title, courseId: id } });
    } catch (err) {
      setSubmitError(errorMessage(err, 'No se pudo procesar la compra.'));
      const backendFields = fieldErrors(err);
      if (Object.keys(backendFields).length) setErrors((prev) => ({ ...prev, ...backendFields }));
    } finally {
      setPurchasing(false);
    }
  };

  const inputCls = (name) =>
    `w-full px-4 py-2.5 border rounded-lg text-sm focus:outline-none focus:ring-2 transition ${
      touched[name] && errors[name]
        ? 'border-red-400 focus:ring-red-400 bg-red-50'
        : 'border-gray-300 focus:ring-blue-500'
    }`;

  const FieldError = ({ name }) =>
    touched[name] && errors[name] ? (
      <p className="text-xs text-red-600 mt-1">{errors[name]}</p>
    ) : null;

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50">
        <Navbar />
        <div className="flex items-center justify-center py-32">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600" />
        </div>
      </div>
    );
  }

  if (loadError || !course) {
    return (
      <div className="min-h-screen bg-gray-50">
        <Navbar />
        <div className="max-w-md mx-auto mt-24 bg-white rounded-2xl border border-gray-100 p-8 text-center">
          <div className="text-4xl mb-3">⚠️</div>
          <h2 className="font-semibold text-gray-800 mb-2">No se pudo cargar el curso</h2>
          <p className="text-sm text-gray-500 mb-6">{loadError}</p>
          <button
            onClick={() => navigate('/catalog')}
            className="px-6 py-2.5 bg-blue-600 text-white rounded-lg font-medium hover:bg-blue-700"
          >
            Volver al catálogo
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <div className="max-w-5xl mx-auto px-4 py-8">
        <h1 className="text-3xl font-bold mb-2 text-gray-900">Finalizar compra</h1>
        <p className="text-sm text-gray-500 mb-8">
          Todos los campos son obligatorios. La matrícula se registra a nombre de tu cuenta.
        </p>

        {alreadyOwned && (
          <div className="bg-amber-50 border border-amber-200 text-amber-800 px-4 py-3 rounded-lg mb-6 flex items-center justify-between">
            <span className="text-sm">Ya tienes este curso en tu cuenta.</span>
            <button
              onClick={() => navigate(`/learn/${id}`)}
              className="text-sm font-semibold underline hover:no-underline"
            >
              Ir al curso
            </button>
          </div>
        )}

        {course.soldOut && !alreadyOwned && (
          <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg mb-6 text-sm">
            Este curso agotó sus {course.capacity} cupos. No es posible matricularse en este momento.
          </div>
        )}

        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div className="md:col-span-2">
            <div className="bg-white rounded-2xl border border-gray-100 p-6">
              <h2 className="text-lg font-bold mb-1 text-gray-900">Datos de facturación</h2>
              <p className="text-xs text-gray-500 mb-6">
                Necesitamos estos datos para emitir tu comprobante de matrícula.
              </p>

              {submitError && (
                <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg mb-5 text-sm">
                  {submitError}
                </div>
              )}

              <form onSubmit={handlePurchase} className="space-y-5" noValidate>
                <div>
                  <label className="block text-sm font-semibold text-gray-700 mb-1">
                    Nombre completo <span className="text-red-500">*</span>
                  </label>
                  <input
                    type="text"
                    name="fullName"
                    value={form.fullName}
                    onChange={handleChange}
                    onBlur={handleBlur}
                    placeholder="Diego Alejandro Betancur"
                    className={inputCls('fullName')}
                  />
                  <FieldError name="fullName" />
                </div>

                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-semibold text-gray-700 mb-1">
                      Documento de identidad <span className="text-red-500">*</span>
                    </label>
                    <input
                      type="text"
                      inputMode="numeric"
                      name="documentId"
                      value={form.documentId}
                      onChange={handleChange}
                      onBlur={handleBlur}
                      placeholder="1020304050"
                      className={inputCls('documentId')}
                    />
                    <FieldError name="documentId" />
                  </div>

                  <div>
                    <label className="block text-sm font-semibold text-gray-700 mb-1">
                      Teléfono de contacto <span className="text-red-500">*</span>
                    </label>
                    <input
                      type="tel"
                      name="phone"
                      value={form.phone}
                      onChange={handleChange}
                      onBlur={handleBlur}
                      placeholder="+57 300 123 4567"
                      className={inputCls('phone')}
                    />
                    <FieldError name="phone" />
                  </div>
                </div>

                <div>
                  <label className="block text-sm font-semibold text-gray-700 mb-1">
                    Correo electrónico
                  </label>
                  <input
                    type="email"
                    value={sessionEmail}
                    readOnly
                    className="w-full px-4 py-2.5 border border-gray-200 bg-gray-50 rounded-lg text-sm text-gray-600 cursor-not-allowed"
                  />
                  <p className="text-xs text-gray-500 mt-1">
                    La matrícula queda ligada al correo de tu sesión para que aparezca en Mis Cursos.
                  </p>
                </div>

                <div>
                  <label className="block text-sm font-semibold text-gray-700 mb-2">
                    Método de pago <span className="text-red-500">*</span>
                  </label>
                  <div className="space-y-2">
                    {PAYMENT_METHODS.map((m) => (
                      <label
                        key={m.value}
                        className={`flex items-start gap-3 p-3 border rounded-lg cursor-pointer transition ${
                          form.paymentMethod === m.value
                            ? 'border-blue-500 bg-blue-50'
                            : 'border-gray-200 hover:border-gray-300'
                        }`}
                      >
                        <input
                          type="radio"
                          name="paymentMethod"
                          value={m.value}
                          checked={form.paymentMethod === m.value}
                          onChange={handleChange}
                          onBlur={handleBlur}
                          className="mt-1"
                        />
                        <span>
                          <span className="block text-sm font-medium text-gray-800">{m.label}</span>
                          <span className="block text-xs text-gray-500">{m.hint}</span>
                        </span>
                      </label>
                    ))}
                  </div>
                  <FieldError name="paymentMethod" />
                </div>

                <div>
                  <label className="flex items-start gap-3 cursor-pointer">
                    <input
                      type="checkbox"
                      name="acceptTerms"
                      checked={form.acceptTerms}
                      onChange={handleChange}
                      onBlur={handleBlur}
                      className="mt-1"
                    />
                    <span className="text-sm text-gray-600">
                      Acepto los términos y condiciones y la política de tratamiento de datos
                      personales de Dieletech. <span className="text-red-500">*</span>
                    </span>
                  </label>
                  <FieldError name="acceptTerms" />
                </div>

                <button
                  type="submit"
                  disabled={purchasing || !formComplete || course.soldOut}
                  className="w-full bg-blue-600 text-white py-3 rounded-lg font-bold hover:bg-blue-700 disabled:bg-gray-300 disabled:cursor-not-allowed transition"
                >
                  {purchasing
                    ? 'Procesando...'
                    : course.soldOut
                      ? 'Cupos agotados'
                      : `Pagar $${course.price.toLocaleString('es-CO')}`}
                </button>

                {!formComplete && !alreadyOwned && !course.soldOut && (
                  <p className="text-xs text-center text-gray-400">
                    Completa todos los campos obligatorios para habilitar el pago.
                  </p>
                )}
              </form>
            </div>
          </div>

          <div>
            <div className="bg-white rounded-2xl border border-gray-100 p-6 sticky top-20">
              <h3 className="font-bold text-gray-900 mb-4">Resumen del pedido</h3>

              <div className="mb-4 pb-4 border-b border-gray-100">
                <h4 className="font-semibold text-gray-800 mb-1">{course.title}</h4>
                <p className="text-xs text-gray-500 line-clamp-3">{course.description}</p>
                <div className="flex flex-wrap gap-1.5 mt-3">
                  <span className="text-xs px-2 py-0.5 bg-blue-50 text-blue-700 rounded-full">
                    {course.technology}
                  </span>
                  <span className="text-xs px-2 py-0.5 bg-gray-100 text-gray-600 rounded-full">
                    {course.level}
                  </span>
                  <span className="text-xs px-2 py-0.5 bg-gray-100 text-gray-600 rounded-full">
                    {course.duration}h
                  </span>
                </div>
              </div>

              <div className="space-y-1.5 mb-4 pb-4 border-b border-gray-100 text-sm">
                <div className="flex justify-between text-gray-600">
                  <span>Matriculados</span>
                  <span className="font-medium text-gray-800">{course.studentCount}</span>
                </div>
                <div className="flex justify-between text-gray-600">
                  <span>Cupos disponibles</span>
                  <span
                    className={`font-medium ${
                      course.seatsAvailable <= 5 ? 'text-amber-600' : 'text-gray-800'
                    }`}
                  >
                    {course.seatsAvailable} de {course.capacity}
                  </span>
                </div>
              </div>

              <div className="flex justify-between items-baseline mb-4">
                <span className="text-sm text-gray-600">Total</span>
                <span className="text-2xl font-bold text-blue-600">
                  ${course.price.toLocaleString('es-CO')}
                </span>
              </div>

              <div className="bg-blue-50 rounded-lg p-4 space-y-1">
                <p className="text-xs text-gray-700">✓ Acceso inmediato al contenido completo</p>
                <p className="text-xs text-gray-700">✓ Acceso de por vida sin renovaciones</p>
                <p className="text-xs text-gray-700">✓ Seguimiento de progreso por lección</p>
                <p className="text-xs text-gray-700">✓ Certificado al completar el 100%</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
