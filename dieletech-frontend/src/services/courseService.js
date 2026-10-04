import axios from 'axios';

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

const client = axios.create({
  baseURL: API_URL,
  headers: { 'Content-Type': 'application/json' },
  timeout: 10000,
});

client.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

/**
 * 401 = no hay sesión válida: se limpia y se manda al login.
 * 403 = hay sesión pero falta el rol: NO se cierra la sesión, se deja que
 * la pantalla muestre el mensaje. Confundir ambos casos hacía que un
 * permiso insuficiente expulsara al usuario de su propia cuenta.
 */
client.interceptors.response.use(
  (res) => res,
  (error) => {
    const status = error.response?.status;
    const hadToken = Boolean(localStorage.getItem('token'));

    if (status === 401 || (status === 403 && !hadToken)) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      const path = window.location.pathname;
      const publica = ['/login', '/register', '/catalog', '/verificar', '/forgot-password', '/reset-password'];
      if (!publica.some((p) => path.startsWith(p))) {
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

/**
 * Extrae un mensaje legible de la respuesta de error del backend.
 * El backend puede responder string plano o { message, errors }.
 */
export const errorMessage = (error, fallback = 'Ocurrió un error inesperado') => {
  const status = error?.response?.status;
  const data = error?.response?.data;

  if (!error?.response) {
    return 'No hay conexión con el servidor. Verifica que el backend esté corriendo en el puerto 8080.';
  }
  if (typeof data === 'string' && data.trim()) return data;
  if (data?.message) return data.message;
  if (status === 401) return 'Tu sesión expiró. Inicia sesión de nuevo.';
  if (status === 403) return 'Tu cuenta no tiene permiso para esta acción.';
  if (status === 404) return 'No se encontró lo que buscabas.';
  return fallback;
};

/** Errores por campo para resaltar el input exacto que falló. */
export const fieldErrors = (error) => error?.response?.data?.errors || {};

// ── Catálogo (HU-04/05) ──
// Sin datos simulados: si el backend no responde, la pantalla lo informa.

export const getCourses = async () => {
  const response = await client.get('/courses');
  return response.data;
};

export const getCourseById = async (id) => {
  const response = await client.get(`/courses/${id}`);
  return response.data;
};

export const getCoursesByTechnology = async (technology) => {
  const response = await client.get(`/courses/technology/${technology}`);
  return response.data;
};

export const searchCourses = async (keyword) => {
  const response = await client.get('/courses/search', { params: { q: keyword } });
  return response.data;
};

// ── Compras (HU-06) ──

export const purchaseCourse = async (purchaseData) => {
  const response = await client.post('/purchases', purchaseData);
  return response.data;
};

export const getMyPurchases = async (email) => {
  if (!email) return [];
  const response = await client.get(`/purchases/user/${encodeURIComponent(email)}`);
  return response.data;
};

export const checkPurchase = async (courseId, email) => {
  if (!email) return false;
  try {
    const response = await client.get('/purchases/check', { params: { courseId, email } });
    return response.data?.hasPurchased === true;
  } catch {
    return false;
  }
};

// ── Lecciones y progreso (HU-07/08) ──

/** El backend devuelve videoUrl en null y locked=true si no hay compra. */
export const getLessons = async (courseId, email) => {
  const response = await client.get(`/courses/${courseId}/lessons`, {
    params: email ? { email } : {},
  });
  return response.data;
};

export const updateLessonProgress = async (lessonId, data) => {
  const response = await client.post(`/lessons/${lessonId}/progress`, data);
  return response.data;
};

export const getCourseProgress = async (courseId, email) => {
  if (!email) {
    return { courseId, totalLessons: 0, completedLessons: 0, progressPercent: 0, lessonProgress: [] };
  }
  const response = await client.get(`/courses/${courseId}/progress`, { params: { email } });
  return response.data;
};

// ── Panel Admin / Instructor (HU-09, HU-13) ──

export const getAdminDashboard = async () => {
  const response = await client.get('/admin/dashboard');
  return response.data;
};

export const getCourseStudents = async (courseId) => {
  const response = await client.get(`/admin/courses/${courseId}/students`);
  return response.data;
};

export const getAdminUsers = async () => {
  const response = await client.get('/admin/users');
  return response.data;
};

export const changeUserRole = async (userId, role) => {
  const response = await client.put(`/admin/users/${userId}/role`, { role });
  return response.data;
};

export const adminCreateCourse = async (courseData) => {
  const response = await client.post('/admin/courses', courseData);
  return response.data;
};

export const adminUpdateCourse = async (id, courseData) => {
  const response = await client.put(`/admin/courses/${id}`, courseData);
  return response.data;
};

export const adminDeleteCourse = async (id) => {
  await client.delete(`/admin/courses/${id}`);
};

export const adminCreateLesson = async (courseId, lessonData) => {
  const response = await client.post(`/admin/courses/${courseId}/lessons`, lessonData);
  return response.data;
};

export const adminUpdateLesson = async (lessonId, lessonData) => {
  const response = await client.put(`/admin/lessons/${lessonId}`, lessonData);
  return response.data;
};

export const adminDeleteLesson = async (lessonId) => {
  await client.delete(`/admin/lessons/${lessonId}`);
};

// ── Evaluación final (HU-10) ──

export const getQuiz = async (courseId) => {
  const response = await client.get(`/courses/${courseId}/quiz`);
  return response.data;
};

export const submitQuiz = async (courseId, answers) => {
  const response = await client.post(`/courses/${courseId}/quiz/attempts`, { answers });
  return response.data;
};

export const getCourseQuestions = async (courseId) => {
  const response = await client.get(`/admin/courses/${courseId}/questions`);
  return response.data;
};

export const createQuestion = async (courseId, data) => {
  const response = await client.post(`/admin/courses/${courseId}/questions`, data);
  return response.data;
};

export const updateQuestion = async (questionId, data) => {
  const response = await client.put(`/admin/questions/${questionId}`, data);
  return response.data;
};

export const deleteQuestion = async (questionId) => {
  await client.delete(`/admin/questions/${questionId}`);
};

// ── Tareas practicas (HU-37) ──

/** Tareas de un curso vistas por el estudiante. Sin compra llegan bloqueadas. */
export const getCourseAssignments = async (courseId) => {
  const response = await client.get(`/courses/${courseId}/assignments`);
  return response.data;
};

export const getAssignment = async (assignmentId) => {
  const response = await client.get(`/assignments/${assignmentId}`);
  return response.data;
};

/** Gestion del instructor: solo el dueño del curso recibe 200. */
export const getAdminAssignments = async (courseId) => {
  const response = await client.get(`/admin/courses/${courseId}/assignments`);
  return response.data;
};

export const createAssignment = async (courseId, data) => {
  const response = await client.post(`/admin/courses/${courseId}/assignments`, data);
  return response.data;
};

export const updateAssignment = async (assignmentId, data) => {
  const response = await client.put(`/admin/assignments/${assignmentId}`, data);
  return response.data;
};

export const archiveAssignment = async (assignmentId) => {
  await client.delete(`/admin/assignments/${assignmentId}`);
};

// ── Entregas del estudiante (HU-38) ──

/** Tareas del curso con mi entrega y si puedo entregar ahora. */
export const getMyCourseAssignments = async (courseId) => {
  const response = await client.get(`/courses/${courseId}/assignments/mine`);
  return response.data;
};

export const getMyAssignment = async (assignmentId) => {
  const response = await client.get(`/assignments/${assignmentId}/mine`);
  return response.data;
};

/** draft=true guarda borrador; draft=false entrega de forma definitiva. */
export const saveSubmission = async (assignmentId, { content, fileUrl, draft }) => {
  const response = await client.post(`/assignments/${assignmentId}/submission`, {
    content,
    fileUrl: fileUrl || null,
    draft,
  });
  return response.data;
};

// ── Certificados (HU-11) ──

export const getMyCertificates = async () => {
  const response = await client.get('/certificates/mine');
  return response.data;
};

/** Descarga el PDF firmado por el backend. */
export const downloadCertificate = async (code) => {
  const response = await client.get(`/certificates/${code}/pdf`, { responseType: 'blob' });
  const url = window.URL.createObjectURL(new Blob([response.data], { type: 'application/pdf' }));
  const a = document.createElement('a');
  a.href = url;
  a.download = `Certificado-Dieletech-${code}.pdf`;
  document.body.appendChild(a);
  a.click();
  a.remove();
  window.URL.revokeObjectURL(url);
};

/** Verificación pública: no requiere sesión. */
export const verifyCertificate = async (code) => {
  const response = await axios.get(`${API_URL}/certificates/verify/${encodeURIComponent(code)}`);
  return response.data;
};

// ── Perfil (HU-14) ──

export const getMyProfile = async () => {
  const response = await client.get('/users/me');
  return response.data;
};

export const updateMyProfile = async (data) => {
  const response = await client.put('/users/me', data);
  return response.data;
};

export const changeMyPassword = async (data) => {
  const response = await client.put('/users/me/password', data);
  return response.data;
};

export const updateMyAvatar = async (avatarUrl) => {
  const response = await client.put('/users/me/avatar', { avatarUrl });
  return response.data;
};

export default client;
