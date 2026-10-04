package com.dieletech.mobile.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Almacenamiento local persistente del progreso del estudiante.
 *
 * HU-07: guarda la última posición vista por lección para reanudar.
 * HU-08: guarda el estado "completada" por lección.
 *
 * Es offline-first: la app funciona sin backend; cuando exista el
 * endpoint /api/progress, se puede sincronizar desde aquí.
 */
private val Context.progressDataStore by preferencesDataStore(name = "dieletech_progress")

class ProgressStore(private val context: Context) {

    private fun positionKey(lessonId: Long) = longPreferencesKey("pos_$lessonId")
    private fun completedKey(lessonId: Long) = booleanPreferencesKey("done_$lessonId")

    // ---------- HU-07: última posición ----------

    fun lastPositionMs(lessonId: Long): Flow<Long> =
        context.progressDataStore.data.map { it[positionKey(lessonId)] ?: 0L }

    suspend fun saveLastPosition(lessonId: Long, positionMs: Long) {
        context.progressDataStore.edit { it[positionKey(lessonId)] = positionMs }
    }

    // ---------- HU-08: completadas ----------

    fun isCompleted(lessonId: Long): Flow<Boolean> =
        context.progressDataStore.data.map { it[completedKey(lessonId)] ?: false }

    suspend fun markCompleted(lessonId: Long, completed: Boolean = true) {
        context.progressDataStore.edit { it[completedKey(lessonId)] = completed }
    }

    /** Devuelve la cantidad de lecciones completadas dentro de una lista de IDs. */
    suspend fun completedCount(lessonIds: List<Long>): Int {
        if (lessonIds.isEmpty()) return 0
        val prefs = context.progressDataStore.data.first()
        return lessonIds.count { prefs[completedKey(it)] == true }
    }
}
