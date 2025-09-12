package com.example.simplenote

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.simplenote.database.NotesDatabase
import com.example.simplenote.home.HomeViewModel
import com.example.simplenote.home.HomeViewModelFactory
import com.example.simplenote.notes.NoteEditorViewModel
import com.example.simplenote.notes.NoteEditorViewModelFactory
import com.example.simplenote.notes.NotesRepository
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    accessToken: String,
    sessionKey: String,
    onBack: () -> Unit,
    onSavedAndExit: () -> Unit,
    existingNoteId: Long? = null
) {

    val context = LocalContext.current
    val dao = remember { NotesDatabase.getInstance(context).noteDao() }
    val repo = remember { NotesRepository(dao) }

    // Use both key and factory
    val vm: NoteEditorViewModel = viewModel(
        key = "editor-$sessionKey",
        factory = NoteEditorViewModelFactory(repo)
    )
    val ui = vm.uiState.value
    val purple = Color(0xFF504EC3)
    val snackbar = remember { SnackbarHostState() }

    var noteId by rememberSaveable(sessionKey) { mutableStateOf<Long?>(null) }
    var title by rememberSaveable(sessionKey) { mutableStateOf("") }
    var body by rememberSaveable(sessionKey) { mutableStateOf("") }
    var lastEdited by remember { mutableStateOf(LocalTime.now()) }
    var showDeleteSheet by remember { mutableStateOf(false) }

    fun touchEdited() { lastEdited = LocalTime.now() }

    // Load note from local DB if editing
    LaunchedEffect(existingNoteId) {
        existingNoteId?.let { id ->
            // For offline-first: just fetch from repo (Room)
            // You can expose a suspend getNoteById() from repository
            val localNote = vm.repo.getNoteById(id)
            if (localNote != null) {
                noteId = localNote.id
                title = localNote.title
                body = localNote.description
            } else {
                snackbar.showSnackbar("Note not found locally")
            }
        }
    }

    // Autosave when user stops typing
    LaunchedEffect(title, body) {
        if (title.isBlank() && body.isBlank()) return@LaunchedEffect
        touchEdited()
        delay(900)
        vm.saveOrUpdate(accessToken, noteId, title.trim(), body.trim())
    }

    // Update UI when note is saved (sync may run later)
    LaunchedEffect(ui.savedNoteId) {
        ui.savedNoteId?.let {
            noteId = it
            snackbar.showSnackbar("Saved locally")
            delay(400)
            vm.consumeSaved()
        }
    }

    // Show error snackbar if sync or DB fails
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

            // -------- Title --------
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
                Text(
                    "• $it",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 16.dp, bottom = 4.dp)
                )
            }

            // -------- Body --------
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
                    textStyle = TextStyle(
                        fontSize = 16.sp,
                        color = Color(0xFF3D3A45),
                        lineHeight = 22.sp
                    ),
                    modifier = Modifier.fillMaxSize()
                )
            }
            ui.fieldErrors["description"]?.forEach {
                Text(
                    "• $it",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 16.dp, bottom = 4.dp)
                )
            }

            Spacer(Modifier.height(4.dp))
            Divider(color = Color(0x14000000))

            // -------- Bottom bar --------
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
                        .clickable(enabled = noteId != null) { showDeleteSheet = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = Color.White)
                }
            }
        }
    }

    if (showDeleteSheet) {
        DeleteNoteSheet(
            onDismiss = { showDeleteSheet = false },
            onDelete = {
                noteId?.let { id ->
                    vm.delete(accessToken, id) { onSavedAndExit() }
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeleteNoteSheet(
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        containerColor = Color.White,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Want to Delete this Note?",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1C1B1F),
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF1F0F5))
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.Close, contentDescription = "Close", tint = Color(0xFF8D8A96))
                }
            }

            Spacer(Modifier.height(10.dp))
            Divider(color = Color(0x14000000))
            Spacer(Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onDelete()
                        onDismiss()
                    }
                    .padding(vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Outlined.DeleteForever,
                    contentDescription = null,
                    tint = Color(0xFFCF3A3A)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    "Delete Note",
                    color = Color(0xFFCF3A3A),
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}
