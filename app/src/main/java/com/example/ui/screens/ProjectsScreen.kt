package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.YapProjectEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.YapViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsScreen(
    viewModel: YapViewModel,
    onOpenStudio: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allProjects by viewModel.allProjects.collectAsState()
    val favoriteProjects by viewModel.favoriteProjects.collectAsState()

    var showOnlyFavorites by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var projectToDelete by remember { mutableStateOf<YapProjectEntity?>(null) }

    val displayedProjects = remember(allProjects, favoriteProjects, showOnlyFavorites, searchQuery) {
        val baseList = if (showOnlyFavorites) favoriteProjects else allProjects
        if (searchQuery.isBlank()) {
            baseList
        } else {
            baseList.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.scriptText.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Screen Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Saved Projects 📁",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "${allProjects.size} yap projects saved offline",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            FilledTonalButton(
                onClick = {
                    viewModel.createNewProject()
                    onOpenStudio()
                },
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = PrimaryNeon,
                    contentColor = Color.White
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("New Yap", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Search bar & Filter Chips
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search projects & scripts...", color = TextMuted) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextMuted)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedContainerColor = DarkSurface,
                unfocusedContainerColor = DarkSurface,
                focusedBorderColor = PrimaryLight,
                unfocusedBorderColor = DarkBorder
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Filter Chips Row
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = !showOnlyFavorites,
                onClick = { showOnlyFavorites = false },
                label = { Text("All (${allProjects.size})") }
            )
            FilterChip(
                selected = showOnlyFavorites,
                onClick = { showOnlyFavorites = true },
                label = { Text("Favorites (${favoriteProjects.size}) ★") }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Project List
        if (displayedProjects.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("🎬", fontSize = 42.sp)
                    Text(
                        text = if (showOnlyFavorites) "No favorite projects yet" else "No saved projects found",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Create and render your first viral yap video now!",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Button(
                        onClick = {
                            viewModel.createNewProject()
                            onOpenStudio()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryNeon)
                    ) {
                        Text("Create Yap Project", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(displayedProjects, key = { it.id }) { project ->
                    ProjectCard(
                        project = project,
                        onLoad = {
                            viewModel.loadProject(project)
                            onOpenStudio()
                        },
                        onToggleFavorite = { viewModel.toggleProjectFavorite(project.id) },
                        onDelete = { projectToDelete = project }
                    )
                }
            }
        }
    }

    // Delete Confirmation Dialog
    projectToDelete?.let { project ->
        AlertDialog(
            onDismissRequest = { projectToDelete = null },
            title = { Text("Delete Project?", color = TextPrimary) },
            text = { Text("Are you sure you want to delete '${project.title}'? This action cannot be undone.", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteProject(project.id)
                    projectToDelete = null
                }) {
                    Text("Delete", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { projectToDelete = null }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = DarkSurface
        )
    }
}

@Composable
private fun ProjectCard(
    project: YapProjectEntity,
    onLoad: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit
) {
    val dateStr = remember(project.lastModified) {
        val sdf = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault())
        sdf.format(Date(project.lastModified))
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onLoad() }
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Brush.linearGradient(listOf(Color(0xFF6366F1), Color(0xFF06B6D4)))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("▶", color = Color.White, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = project.title,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(dateStr, color = TextMuted, fontSize = 10.sp)
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onToggleFavorite, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = if (project.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Favorite",
                            tint = if (project.isFavorite) TertiaryAmber else TextMuted
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete",
                            tint = TextMuted
                        )
                    }
                }
            }

            // Script Snippet
            Text(
                text = project.scriptText,
                color = TextSecondary,
                fontSize = 12.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 16.sp
            )

            // Tags Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TagPill(label = project.aspectRatio, color = PrimaryLight)
                TagPill(label = project.captionStyle, color = SecondaryAqua)
                TagPill(label = project.backgroundTheme, color = TertiaryAmber)
            }
        }
    }
}

@Composable
private fun TagPill(label: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(text = label, color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}
