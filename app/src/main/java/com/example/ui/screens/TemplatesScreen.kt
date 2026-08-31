package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.YapTemplate
import com.example.ui.theme.*
import com.example.ui.viewmodel.YapViewModel

@Composable
fun TemplatesScreen(
    viewModel: YapViewModel,
    onOpenStudio: () -> Unit,
    modifier: Modifier = Modifier
) {
    val templates = viewModel.trendingTemplates

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Viral Templates 🔥",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Ready-made scripts, synced themes & kinetic presets",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Brush.linearGradient(listOf(TertiaryAmber, PrimaryNeon))),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Templates List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(templates, key = { it.id }) { template ->
                TemplateItemCard(
                    template = template,
                    onUse = {
                        viewModel.applyTemplate(template)
                        onOpenStudio()
                    }
                )
            }
        }
    }
}

@Composable
private fun TemplateItemCard(
    template: YapTemplate,
    onUse: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onUse() }
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Category & Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(PrimaryNeon.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = template.category,
                        color = PrimaryLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(template.mascot.emoji, fontSize = 20.sp)
            }

            Text(
                text = template.title,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            // Script Preview Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkSurfaceElevated)
                    .padding(12.dp)
            ) {
                Text(
                    text = "\"${template.script}\"",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }

            // Presets row & Action button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Theme: ${template.backgroundTheme.displayName} • ${template.captionStyle.displayName}",
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                    Text(
                        text = "BGM: ${template.bgm.title}",
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }

                Button(
                    onClick = onUse,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryNeon),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Use Template", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}
