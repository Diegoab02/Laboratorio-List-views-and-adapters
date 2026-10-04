const EMOJI = {
  Python: '🐍',
  'HTML/CSS/JS': '🌐',
  Git: '📚',
  Java: '☕',
  React: '⚛️',
  MySQL: '🗄️',
};

const LEVEL_STYLE = {
  Principiante: 'bg-emerald-100 text-emerald-700',
  Intermedio: 'bg-amber-100 text-amber-700',
  Avanzado: 'bg-rose-100 text-rose-700',
};

export default function CourseCard({ course, onViewDetails, enrolled = false }) {
  const seats = course.seatsAvailable ?? 0;
  const capacity = course.capacity ?? 0;
  const soldOut = course.soldOut || seats === 0;
  const almostFull = !soldOut && capacity > 0 && seats <= Math.max(3, capacity * 0.1);
  const occupancy = capacity > 0 ? Math.round(((capacity - seats) / capacity) * 100) : 0;

  return (
    <div className="bg-white rounded-xl border border-gray-100 hover:shadow-lg transition-shadow overflow-hidden flex flex-col">
      <div className="h-40 bg-gradient-to-br from-blue-500 to-blue-700 flex items-center justify-center text-white relative">
        <div className="text-center">
          <div className="text-5xl mb-1">{EMOJI[course.technology] || '💻'}</div>
          <p className="font-semibold text-sm">{course.technology}</p>
        </div>

        {enrolled && (
          <span className="absolute top-3 right-3 bg-white text-blue-700 text-[10px] font-bold px-2 py-1 rounded-full">
            MATRICULADO
          </span>
        )}
        {!enrolled && soldOut && (
          <span className="absolute top-3 right-3 bg-gray-900/80 text-white text-[10px] font-bold px-2 py-1 rounded-full">
            SIN CUPOS
          </span>
        )}
        {!enrolled && almostFull && (
          <span className="absolute top-3 right-3 bg-amber-400 text-amber-950 text-[10px] font-bold px-2 py-1 rounded-full">
            ÚLTIMOS CUPOS
          </span>
        )}
      </div>

      <div className="p-4 flex flex-col flex-1">
        <h3 className="font-bold text-gray-900 mb-1.5 line-clamp-2">{course.title}</h3>
        <p className="text-gray-500 text-sm mb-3 line-clamp-2 flex-1">{course.description}</p>

        <div className="flex flex-wrap items-center gap-2 mb-3 text-xs">
          <span className={`px-2 py-1 rounded-full font-medium ${LEVEL_STYLE[course.level] || 'bg-gray-100 text-gray-600'}`}>
            {course.level}
          </span>
          <span className="text-gray-500">{course.duration} horas</span>
          {course.instructorName && (
            <span className="text-gray-400 truncate">· {course.instructorName}</span>
          )}
        </div>

        {/* Ocupación real, calculada desde compras efectivas */}
        {capacity > 0 && (
          <div className="mb-3">
            <div className="flex justify-between text-[11px] text-gray-500 mb-1">
              <span>
                {course.studentCount ?? 0} matriculado{(course.studentCount ?? 0) === 1 ? '' : 's'}
              </span>
              <span className={almostFull ? 'text-amber-600 font-medium' : ''}>
                {seats} cupo{seats === 1 ? '' : 's'} libre{seats === 1 ? '' : 's'}
              </span>
            </div>
            <div className="bg-gray-100 rounded-full h-1.5">
              <div
                className={`h-1.5 rounded-full transition-all ${
                  soldOut ? 'bg-gray-400' : almostFull ? 'bg-amber-500' : 'bg-blue-500'
                }`}
                style={{ width: `${occupancy}%` }}
              />
            </div>
          </div>
        )}

        <div className="mb-3 py-2.5 border-t border-gray-100">
          <p className="text-gray-500 text-xs">Precio</p>
          <p className="text-2xl font-bold text-blue-600">
            ${course.price.toLocaleString('es-CO')}
          </p>
        </div>

        <button
          onClick={onViewDetails}
          className={`w-full py-2.5 px-4 rounded-lg font-semibold text-sm transition ${
            enrolled
              ? 'bg-emerald-600 text-white hover:bg-emerald-700'
              : soldOut
                ? 'bg-gray-100 text-gray-500 hover:bg-gray-200'
                : 'bg-blue-600 text-white hover:bg-blue-700'
          }`}
        >
          {enrolled ? 'Continuar curso' : soldOut ? 'Ver detalles (sin cupos)' : 'Ver detalles'}
        </button>
      </div>
    </div>
  );
}
