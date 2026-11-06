package com.tecsup.suarez.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.suarez.data.CourseRepository
import com.tecsup.suarez.model.Course
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CourseUiState(
    val courses: List<Course> = emptyList(),
    val loading: Boolean = true,
    val message: String? = null   // para el Snackbar
)

class CourseViewModel(
    private val userId: String,
    private val repo: CourseRepository = CourseRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(CourseUiState())
    val state: StateFlow<CourseUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repo.observeCourses(userId).collect { result ->
                result
                    .onSuccess { _state.value = _state.value.copy(courses = it, loading = false) }
                    .onFailure {
                        _state.value = _state.value.copy(
                            loading = false,
                            message = it.localizedMessage ?: "Error al leer cursos"
                        )
                    }
            }
        }
    }

    /** Crea o actualiza según tenga id o no. */
    fun save(course: Course) = viewModelScope.launch {
        val result = if (course.id.isEmpty()) repo.add(course.copy(userId = userId))
        else repo.update(course)
        notify(result, if (course.id.isEmpty()) "Curso registrado" else "Curso actualizado")
    }

    fun delete(courseId: String) = viewModelScope.launch {
        notify(repo.delete(courseId), "Curso eliminado")
    }

    fun messageShown() { _state.value = _state.value.copy(message = null) }

    private fun notify(result: Result<Unit>, ok: String) {
        _state.value = _state.value.copy(
            message = result.fold({ ok }, { it.localizedMessage ?: "Ocurrió un error" })
        )
    }
}
