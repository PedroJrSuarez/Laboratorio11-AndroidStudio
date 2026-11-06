package com.tecsup.suarez.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.tecsup.suarez.model.Course
import com.tecsup.suarez.viewmodel.CourseUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoursesScreen(
    email: String,
    state: CourseUiState,
    onSave: (Course) -> Unit,
    onDelete: (String) -> Unit,
    onLogout: () -> Unit,
    onMessageShown: () -> Unit
) {
    val snackbar = remember { SnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    var editing by remember { mutableStateOf<Course?>(null) }   // curso en edición
    var showSheet by remember { mutableStateOf(false) }
    var toDelete by remember { mutableStateOf<Course?>(null) }  // curso por confirmar borrado

    LaunchedEffect(state.message) {
        state.message?.let { snackbar.showSnackbar(it); onMessageShown() }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text("Mis Cursos") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Cerrar sesión")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { editing = null; showSheet = true },
                icon = { Icon(Icons.Filled.Add, null) },
                text = { Text("Nuevo curso") }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when {
                state.loading -> CircularProgressIndicator(Modifier.align(Alignment.Center).size(64.dp))
                state.courses.isEmpty() -> EmptyState(email, Modifier.align(Alignment.Center))
                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        AssistChip(
                            onClick = {},
                            label = { Text(email) },
                            leadingIcon = { Icon(Icons.Filled.Person, null) }
                        )
                    }
                    items(state.courses, key = { it.id }) { course ->
                        CourseCard(
                            course = course,
                            onEdit = { editing = course; showSheet = true },
                            onDelete = { toDelete = course }
                        )
                    }
                }
            }
        }
    }

    // CREATE / UPDATE en un ModalBottomSheet (API experimental)
    if (showSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState
        ) {
            CourseForm(
                initial = editing,
                onSubmit = { onSave(it); showSheet = false },
                onCancel = { showSheet = false }
            )
        }
    }

    // DELETE con confirmación
    toDelete?.let { course ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            icon = { Icon(Icons.Filled.Delete, null) },
            title = { Text("Eliminar curso") },
            text = { Text("¿Seguro que deseas eliminar \"${course.titulo}\"? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = { onDelete(course.id); toDelete = null }) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { toDelete = null }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun EmptyState(email: String, modifier: Modifier = Modifier) {
    Column(modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            Icons.Filled.MenuBook, null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(12.dp))
        Text("Aún no tienes cursos", style = MaterialTheme.typography.titleLarge)
        Text(
            "Hola, $email. Toca \"Nuevo curso\" para registrar el primero.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CourseCard(course: Course, onEdit: () -> Unit, onDelete: () -> Unit) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(course.titulo, style = MaterialTheme.typography.titleLarge)
                    Text(
                        "Docente: ${course.docente}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                FilledTonalIconButton(onClick = onEdit) {
                    Icon(Icons.Filled.Edit, contentDescription = "Editar")
                }
                Spacer(Modifier.width(4.dp))
                FilledTonalIconButton(
                    onClick = onDelete,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) { Icon(Icons.Filled.Delete, contentDescription = "Eliminar") }
            }
            if (course.descripcion.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(course.descripcion, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.height(12.dp))
            SuggestionChip(onClick = {}, label = { Text("${course.creditos} créditos") })
        }
    }
}

@Composable
private fun CourseForm(initial: Course?, onSubmit: (Course) -> Unit, onCancel: () -> Unit) {
    var titulo by remember { mutableStateOf(initial?.titulo ?: "") }
    var descripcion by remember { mutableStateOf(initial?.descripcion ?: "") }
    var docente by remember { mutableStateOf(initial?.docente ?: "") }
    var creditos by remember { mutableStateOf(initial?.creditos?.toString() ?: "") }
    var triedSubmit by remember { mutableStateOf(false) }

    val valid = titulo.isNotBlank() && docente.isNotBlank() && creditos.toIntOrNull() != null

    Column(
        Modifier.padding(horizontal = 24.dp).padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            if (initial == null) "Registrar curso" else "Editar curso",
            style = MaterialTheme.typography.headlineSmall
        )
        OutlinedTextField(
            value = titulo, onValueChange = { titulo = it },
            label = { Text("Nombre del curso") }, singleLine = true,
            isError = triedSubmit && titulo.isBlank(),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = docente, onValueChange = { docente = it },
            label = { Text("Docente") }, singleLine = true,
            isError = triedSubmit && docente.isBlank(),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = creditos, onValueChange = { creditos = it.filter(Char::isDigit) },
            label = { Text("Créditos") }, singleLine = true,
            isError = triedSubmit && creditos.toIntOrNull() == null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = descripcion, onValueChange = { descripcion = it },
            label = { Text("Descripción (opcional)") }, minLines = 3,
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
        ) {
            OutlinedButton(onClick = onCancel) { Text("Cancelar") }
            Button(onClick = {
                triedSubmit = true
                if (valid) {
                    onSubmit(
                        (initial ?: Course()).copy(
                            titulo = titulo.trim(),
                            descripcion = descripcion.trim(),
                            docente = docente.trim(),
                            creditos = creditos.toInt()
                        )
                    )
                }
            }) { Text(if (initial == null) "Guardar" else "Actualizar") }
        }
    }
}
