package com.example.simplenote

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Home
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen(
    onAddNote: () -> Unit,
    onOpenSettings: () -> Unit,
    username: String? = null
) {
    val bg = Color(0xFFFAF8FC)
    val purple = Color(0xFF504EC3)

    var selectedTab by remember { mutableStateOf(0) } // 0=Home, 1=Settings

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
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Outlined.Home, contentDescription = "Home") },
                    label = { Text("Home") }
                )
                Spacer(Modifier.weight(1f)) // space under FAB
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1; onOpenSettings() },
                    icon = { Icon(Icons.Outlined.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") }
                )
            }
        }
    ) { inner ->
        EmptyState(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .systemBarsPadding(),
            username = username
        )
    }
}

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

        // Illustration (put your asset in res/drawable as home_empty_illustration.png)
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

        // Optional curved arrow image pointing to the FAB
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
