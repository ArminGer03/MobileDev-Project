package com.example.simplenote

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.simplenote.notes.NoteEditorViewModel
import com.example.simplenote.notes.NotesRepository
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    accessToken: String,
    onBack: () -> Unit,
    onSavedAndExit: () -> Unit,
    existingNoteId: Long? = null,
    vm: NoteEditorViewModel = viewModel()
) {
    val ui = vm.uiState.value
    val purple = Color(0xFF504EC3)
    val snackbar = remember { SnackbarHostState() }

    var noteId by rememberSaveable { mutableStateOf<Long?>(null) }
    var title by rememberSaveable { mutableStateOf("") }
    var body by rememberSaveable { mutableStateOf("") }
    var lastEdited by remember { mutableStateOf(LocalTime.now()) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    fun touchEdited() { lastEdited = LocalTime.now() }

    LaunchedEffect(existingNoteId) {
        existingNoteId?.let { id ->
            try {
                val dto = NotesRepository().getNote(accessToken, id)
                noteId = dto.id
                title = dto.title
                body = dto.description
            } catch (e: Exception) {
                snackbar.showSnackbar("Failed to load note")
            }
        }
    }

    // -------- AUTOSAVE (debounced) --------
    LaunchedEffect(title, body) {
        if (title.isBlank() && body.isBlank()) return@LaunchedEffect
        touchEdited()
        delay(900) // debounce typing
        vm.saveOrUpdate(accessToken, noteId, title.trim(), body.trim())
    }

    // Save result
    LaunchedEffect(ui.savedNoteId) {
        ui.savedNoteId?.let {
            noteId = it
            snackbar.showSnackbar("Saved")
            delay(400)
            vm.consumeSaved()
        }
    }

    // Global/backend error
    LaunchedEffect(ui.error) {
        ui.error?.let {
            snackbar.showSnackbar(it)
            vm.clearError()
        }
    }

    val timeStr = remember(lastEdited) {
        lastEdited.format(DateTimeFormatter.ofPattern("HH.mm"))
    }

    Scaffold(
        containerColor = Color.White,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = null, tint = purple)
                        Spacer(Modifier.width(4.dp))
                        Text("Back", color = purple)
                    }
                }
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .imePadding()
        ) {
            Divider(color = Color(0x1F000000))

            // -------- Title (large) with placeholder --------
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                if (title.isBlank()) {
                    Text(
                        text = "Title",
                        color = Color(0xFFB7B5C3),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                BasicTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        vm.clearFieldErrors("title")
                    },
                    singleLine = false,
                    textStyle = TextStyle(
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1B1F),
                        lineHeight = 36.sp
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            ui.fieldErrors["title"]?.forEach {
                Text("• $it", color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.padding(start = 16.dp, bottom = 4.dp))
            }

            // -------- Body (multiline) with placeholder --------
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                if (body.isBlank()) {
                    Text(
                        text = "Feel Free to Write Here...",
                        color = Color(0xFFB7B5C3),
                        fontSize = 16.sp
                    )
                }
                BasicTextField(
                    value = body,
                    onValueChange = {
                        body = it
                        vm.clearFieldErrors("description")
                    },
                    textStyle = TextStyle(fontSize = 16.sp, color = Color(0xFF3D3A45), lineHeight = 22.sp),
                    modifier = Modifier.fillMaxSize()
                )
            }
            ui.fieldErrors["description"]?.forEach {
                Text("• $it", color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.padding(start = 16.dp, bottom = 4.dp))
            }

            Spacer(Modifier.height(4.dp))
            Divider(color = Color(0x14000000))

            // -------- Bottom bar: last edited + trash --------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = "Last edited on $timeStr",
                    fontSize = 12.sp,
                    color = Color(0xFF3D3A45),
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(start = 16.dp)
                        .weight(1f)
                )
                Box(
                    modifier = Modifier
                        .width(76.dp)
                        .fillMaxHeight()
                        .background(purple)
                        .clickable(enabled = noteId != null) { showDeleteConfirm = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = Color.White)
                }
            }
        }
    }

    // Confirm delete dialog
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete note?") },
            text = { Text("This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        val id = noteId
                        if (id != null) vm.delete(accessToken, id) { onSavedAndExit() }
                    },
                    enabled = noteId != null
                ) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") } }
        )
    }
}
