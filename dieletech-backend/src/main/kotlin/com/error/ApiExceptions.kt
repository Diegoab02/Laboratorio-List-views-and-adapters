package com.dieletech.backend.error

/**
 * Excepciones de negocio con codigo HTTP propio.
 *
 * Hasta el Sprint 9 toda RuntimeException terminaba en 400, asi que el
 * cliente no podia distinguir "no existe" de "no tienes permiso" ni de
 * "el dato que enviaste esta mal". Estas tres clases dan esa diferencia
 * sin obligar a cada controlador a construir la respuesta a mano.
 */

/** 404: el recurso no existe o fue archivado. */
class NotFoundException(message: String) : RuntimeException(message)

/** 403: hay sesion valida, pero la cuenta no es dueña del recurso. */
class ForbiddenException(message: String) : RuntimeException(message)

/** 409: el estado actual del recurso impide la operacion. */
class ConflictException(message: String) : RuntimeException(message)
