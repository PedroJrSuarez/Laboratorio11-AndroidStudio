package com.tecsup.suarez.model

/**
 * Documento de la colección "cursos" en Cloud Firestore.
 * Valores por defecto + constructor vacío: requisito de Firestore para deserializar.
 */
data class Course(
    val id: String = "",          // ID del documento (se asigna al leer)
    val titulo: String = "",
    val descripcion: String = "",
    val docente: String = "",
    val creditos: Int = 0,
    val userId: String = "",      // UID del dueño -> filtro por usuario
    val createdAt: Long = 0L
)
