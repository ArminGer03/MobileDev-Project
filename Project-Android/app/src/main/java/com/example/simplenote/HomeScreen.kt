package com.example.simplenote

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.simplenote.database.NoteEntity
import com.example.simplenote.database.NotesDatabase
import com.example.simplenote.home.HomeViewModel
import com.example.simplenote.home.HomeViewModelFactory
import com.example.simplenote.worker.SyncScheduler


@Composable
fun HomeScreen(
    accessToken: String,
    onAddNote: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenNote: (Long) -> Unit,
    username: String? = null
) {

    val context = LocalContext.current
    val dao = remember { NotesDatabase.getInstance(context).noteDao() }
    val vm: HomeViewModel = viewModel(factory = HomeViewModelFactory(dao))

    val bg = Color(0xFFFAF8FC)
    val purple = Color(0xFF504EC3)
    var selectedTab by remember { mutableStateOf(0) }
    val fabSize = 72.dp
    val barHeight = 80.dp

    // Load from local DB + refresh from server
    LaunchedEffect(accessToken) {
        vm.init(accessToken)
        SyncScheduler.schedulePeriodicSync(
            context = context,
            accessToken = accessToken
        )
    }
    val ui = vm.uiState.value

    // Refresh on resume (pulls latest from server)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, accessToken) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) vm.refresh(accessToken)
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        containerColor = bg,
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(barHeight)
            ) {
                Divider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter),
                    color = Color(0x1A000000),
                    thickness = 1.dp
                )
                NavigationBar(
                    containerColor = Color.White,
                    modifier = Modifier.matchParentSize()
                ) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = {
                            if (selectedTab == 0) vm.refresh(accessToken)
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
                FloatingActionButton(
                    onClick = onAddNote,
                    containerColor = purple,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(fabSize)
                        .align(Alignment.TopCenter)
                        .offset(y = -fabSize / 2)
                ) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = "Add",
                        modifier = Modifier.size(28.dp)
                    )
                }
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

                !ui.hasNotes -> {
                    EmptyState(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 8.dp),
                        username = username
                    )
                }

                else -> {
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
                            .heightIn(min = 56.dp)
                    )

                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "Notes",
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))

                    val filtered = ui.notes.filter {
                        val q = query.trim().lowercase()
                        q.isBlank() || it.title.lowercase()
                            .contains(q) || it.description.lowercase().contains(q)
                    }

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(
                            bottom = fabSize / 2 + 24.dp
                        )
                    ) {
                        items(filtered, key = { it.id }) { note ->
                            NoteCard(note = note) { onOpenNote(note.id) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NoteCard(note: NoteEntity, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFFFF6C5),
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

@Composable
private fun PageIndicator(
    total: Int,
    current: Int,
    onDotClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (total <= 1) return
    Surface(
        color = Color(0xF2FFFFFF),                     // translucent white
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 4.dp,
        tonalElevation = 2.dp,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Page $current/$total",
                fontSize = 12.sp,
                color = Color(0xFF1C1B1F)
            )
            Spacer(Modifier.width(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                repeat(total) { idx ->
                    val index = idx + 1
                    val active = index == current
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .size(if (active) 10.dp else 6.dp)
                            .clip(CircleShape)
                            .background(if (active) Color(0xFF504EC3) else Color(0x33504EC3))
                            .clickable { onDotClick(index) }
                    )
                }
            }
        }
    }
}


// ---------- Empty state (same as before) ----------
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
