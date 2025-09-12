package com.example.simplenote

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.simplenote.home.HomeViewModel
import com.example.simplenote.network.NoteDto

@Composable
fun HomeScreen(
    accessToken: String,
    onAddNote: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenNote: (Long) -> Unit = {},
    username: String? = null,
    vm: HomeViewModel = viewModel()
) {
    val bg = Color(0xFFFAF8FC)
    val purple = Color(0xFF504EC3)
    var selectedTab by remember { mutableStateOf(0) } // 0=Home, 1=Settings
    var query by remember { mutableStateOf("") }

    // Load once per token
    LaunchedEffect(accessToken) { vm.loadNotes(accessToken, allPages = true) }
    val ui = vm.uiState.value

    Scaffold(
        containerColor = bg,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddNote,
                containerColor = purple,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(64.dp)
            ) { Icon(Icons.Filled.Add, contentDescription = "Add") }
        },
        floatingActionButtonPosition = FabPosition.Center,
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = {
                        if (selectedTab == 0) {
                            vm.loadNotes(accessToken, allPages = true)
                        }
                        selectedTab = 0
                    },
                    icon = { Icon(Icons.Outlined.Home, contentDescription = "Home") },
                    label = { Text("Home") }
                )

                Spacer(Modifier.weight(1f))

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1; onOpenSettings() },
                    icon = { Icon(Icons.Outlined.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") }
                )
            }
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .systemBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            when {
                ui.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }

                ui.error != null -> Text(
                    text = ui.error ?: "",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )

                ui.notes.isEmpty() -> {
                    // EMPTY STATE — NO SEARCH BOX HERE
                    EmptyState(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 8.dp),
                        username = username
                    )
                }

                else -> {
                    // NOTES VERSION — SHOW SEARCH + GRID
                    var query by remember { mutableStateOf("") }

                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        singleLine = true,
                        placeholder = { Text("Search…", maxLines = 1) },
                        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 56.dp) // avoids cropping
                    )

                    Spacer(Modifier.height(10.dp))

                    Text(
                        text = "Notes",
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))

                    val filtered = ui.notes.filter { n ->
                        val q = query.trim().lowercase()
                        q.isBlank() || n.title.lowercase().contains(q) || n.description.lowercase().contains(q)
                    }

                    Box(Modifier.weight(1f)) {
                        NotesGrid(
                            notes = filtered,
                            onClick = onOpenNote,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }

    }
}

@Composable
private fun NotesGrid(
    notes: List<NoteDto>,
    onClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        itemsIndexed(notes, key = { _, n -> n.id }) { index, note ->
            val cardColor =
                if (index % 2 == 0) Color(0xFFFFF6C5) else Color(0xFFFFEDB3) // soft yellows
            NoteCard(note = note, bg = cardColor, onClick = { onClick(note.id) })
        }
    }
}

@Composable
private fun NoteCard(note: NoteDto, bg: Color, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bg,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 160.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(
                text = "💡 ${note.title}",
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = note.description,
                fontSize = 13.sp,
                color = Color(0xFF5A5761),
                maxLines = 6,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ---------- Empty state (unchanged) ----------
@Composable
private fun EmptyState(modifier: Modifier = Modifier, username: String?) {
    val textDark = Color(0xFF1C1B1F)
    val textMute = Color(0xFF7D7A85)

    Column(
        modifier = modifier.padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(Modifier.height(24.dp))
        runCatching {
            Image(
                painter = painterResource(id = R.drawable.home_empty_illustration),
                contentDescription = "Empty state",
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 180.dp, max = 240.dp),
                contentScale = ContentScale.Fit
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Start Your Journey",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = textDark,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Every big step start with small step.\nNotes your first idea and start your journey!",
            fontSize = 14.sp,
            color = textMute,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(36.dp))
        runCatching {
            Image(
                painter = painterResource(id = R.drawable.arrow_curved),
                contentDescription = null,
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Fit
            )
        }
    }
}
