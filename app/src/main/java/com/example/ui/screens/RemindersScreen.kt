package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.ReminderEntity
import com.example.ui.components.HudCard
import com.example.ui.components.HudHeader
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.BorderCyan
import com.example.ui.theme.BorderGold
import com.example.ui.theme.JarvisRedAlert
import com.example.ui.theme.JarvisSuccessGreen
import com.example.ui.theme.MetallicObsidian
import com.example.ui.theme.StarkGold
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.RemindersViewModel

@Composable
fun RemindersScreen(remindersViewModel: RemindersViewModel) {
    val reminders by remindersViewModel.reminders.collectAsState()

    var showAddCard by remember { mutableStateOf(false) }
    var titleInput by remember { mutableStateOf("") }
    var descInput by remember { mutableStateOf("") }
    var timeInput by remember { mutableStateOf("18:00") }
    var selectedPriority by remember { mutableStateOf("HIGH") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MetallicObsidian)
            .padding(16.dp)
    ) {
        HudHeader(title = "PROTOCOLS", subtitle = "STARK TASK & AGENDA MATRIX")

        Spacer(modifier = Modifier.height(16.dp))

        // Top Action Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ACTIVE PROTOCOLS (${reminders.count { !it.isCompleted }})",
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                color = ArcCyan,
                fontWeight = FontWeight.Bold
            )

            Button(
                onClick = { showAddCard = !showAddCard },
                colors = ButtonDefaults.buttonColors(containerColor = StarkGold),
                shape = CutCornerShape(8.dp),
                modifier = Modifier.testTag("add_protocol_button")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add", tint = MetallicObsidian)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = if (showAddCard) "FECHAR" else "NOVO PROTOCOLO", color = MetallicObsidian, fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Add New Protocol Card
        if (showAddCard) {
            HudCard(borderColor = BorderGold, cutCornerSize = 12.dp) {
                Column {
                    Text(text = "AGENDAR NOVO PROTOCOLO", fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = StarkGold, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = titleInput,
                        onValueChange = { titleInput = it },
                        label = { Text("Título do Protocolo") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("protocol_title_field"),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ArcCyan, unfocusedBorderColor = BorderCyan)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = descInput,
                        onValueChange = { descInput = it },
                        label = { Text("Descrição / Instruções") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("protocol_desc_field"),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ArcCyan, unfocusedBorderColor = BorderCyan)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = timeInput,
                            onValueChange = { timeInput = it },
                            label = { Text("Horário") },
                            modifier = Modifier
                                .width(120.dp)
                                .testTag("protocol_time_field"),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ArcCyan, unfocusedBorderColor = BorderCyan)
                        )

                        Row {
                            listOf("HIGH", "MEDIUM", "LOW").forEach { priority ->
                                val selected = selectedPriority == priority
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 4.dp)
                                        .clip(CutCornerShape(6.dp))
                                        .background(if (selected) StarkGold else BorderCyan.copy(alpha = 0.2f))
                                        .clickable { selectedPriority = priority }
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = priority,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        color = if (selected) MetallicObsidian else TextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (titleInput.isNotBlank()) {
                                remindersViewModel.addReminder(titleInput, descInput, timeInput, selectedPriority)
                                titleInput = ""
                                descInput = ""
                                showAddCard = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ArcCyan),
                        shape = CutCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_protocol_button")
                    ) {
                        Text(text = "INICIALIZAR PROTOCOLO", color = MetallicObsidian, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Reminders List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(reminders) { reminder ->
                ReminderItemCard(
                    reminder = reminder,
                    onToggle = { remindersViewModel.toggleReminder(reminder) },
                    onDelete = { remindersViewModel.deleteReminder(reminder.id) }
                )
            }
        }
    }
}

@Composable
fun ReminderItemCard(
    reminder: ReminderEntity,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    HudCard(
        borderColor = if (reminder.isCompleted) BorderCyan.copy(alpha = 0.3f) else BorderCyan,
        cutCornerSize = 10.dp,
        contentPadding = 12.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onToggle) {
                    Icon(
                        imageVector = if (reminder.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = "Toggle Protocol",
                        tint = if (reminder.isCompleted) JarvisSuccessGreen else ArcCyan
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Text(
                        text = reminder.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (reminder.isCompleted) TextSecondary else TextPrimary
                    )
                    if (reminder.description.isNotBlank()) {
                        Text(
                            text = reminder.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                    Text(
                        text = "HORÁRIO: ${reminder.timeFormatted} | PRIORIDADE: ${reminder.priority}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = StarkGold
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Protocol",
                    tint = JarvisRedAlert
                )
            }
        }
    }
}
