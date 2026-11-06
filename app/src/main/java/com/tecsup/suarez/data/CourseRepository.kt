package com.tecsup.suarez.data

import com.google.firebase.firestore.FirebaseFirestore
import com.tecsup.suarez.model.Course
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * CRUD de cursos en Cloud Firestore.
 * Cada curso guarda el userId del dueño y SIEMPRE se consulta filtrando por él.
 */
class CourseRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val col get() = db.collection("cursos")

    /** READ en tiempo real: solo los cursos del usuario autenticado. */
    fun observeCourses(userId: String): Flow<Result<List<Course>>> = callbackFlow {
        val listener = col.whereEqualTo("userId", userId)
            .addSnapshotListener { snap, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                val list = snap?.documents?.mapNotNull { doc ->
                    doc.toObject(Course::class.java)?.copy(id = doc.id)
                }.orEmpty()
                    // Orden en cliente: evita crear un índice compuesto en Firestore
                    .sortedByDescending { it.createdAt }
                trySend(Result.success(list))
            }
        awaitClose { listener.remove() }
    }

    /** CREATE */
    suspend fun add(course: Course): Result<Unit> = runCatching {
        col.add(course.copy(createdAt = System.currentTimeMillis())).await()
        Unit
    }

    /** UPDATE */
    suspend fun update(course: Course): Result<Unit> = runCatching {
        col.document(course.id).update(
            mapOf(
                "titulo" to course.titulo,
                "descripcion" to course.descripcion,
                "docente" to course.docente,
                "creditos" to course.creditos
            )
        ).await()
        Unit
    }

    /** DELETE */
    suspend fun delete(courseId: String): Result<Unit> = runCatching {
        col.document(courseId).delete().await()
        Unit
    }
}
