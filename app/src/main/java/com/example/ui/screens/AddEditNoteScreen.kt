package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.NotesViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditNoteScreen(
    notesViewModel: NotesViewModel,
    noteId: String?,
    onBack: () -> Unit
) {
    val notes by notesViewModel.notes.collectAsState()
    val existingNote = remember(noteId, notes) { notes.find { it.id == noteId } }

    var title by remember { mutableStateOf(existingNote?.title ?: "") }
    var content by remember { mutableStateOf(existingNote?.content ?: "") }
    var linkUrl by remember { mutableStateOf(existingNote?.linkUrl ?: "") }
    var category by remember { mutableStateOf(existingNote?.category ?: "Links") }

    val categories = listOf("Links", "Notes", "WhatsApp", "Work", "Personal", "Important")
    val clipboardManager: ClipboardManager = LocalClipboardManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = if (noteId == null) "Add Note or Link" else "Edit Note or Link", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            notesViewModel.saveNote(
                                id = noteId,
                                title = title,
                                content = content,
                                linkUrl = linkUrl,
                                category = category
                            )
                            onBack()
                        }
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = "Save")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Quick Paste from Clipboard button
            OutlinedButton(
                onClick = {
                    val pastedText = clipboardManager.getText()?.text ?: ""
                    if (pastedText.isNotBlank()) {
                        val urlRegex = "(https?://\\S+)".toRegex()
                        val match = urlRegex.find(pastedText)
                        if (match != null && linkUrl.isBlank()) {
                            linkUrl = match.value
                        }
                        if (content.isBlank()) {
                            content = pastedText
                        } else {
                            content = "$content\n$pastedText"
                        }
                        if (title.isBlank()) {
                            title = pastedText.take(30) + if (pastedText.length > 30) "..." else ""
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.ContentPaste, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Paste WhatsApp Message / Clipboard")
            }

            // Category Selection
            Text(text = "Category", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    FilterChip(
                        selected = category == cat,
                        onClick = { category = cat },
                        label = { Text(cat) }
                    )
                }
            }

            // Title Field
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )

            // Link URL Field
            OutlinedTextField(
                value = linkUrl,
                onValueChange = { linkUrl = it },
                label = { Text("Important Link URL (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )

            // Content Field
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Notes / Details / WhatsApp Text") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    notesViewModel.saveNote(
                        id = noteId,
                        title = title,
                        content = content,
                        linkUrl = linkUrl,
                        category = category
                    )
                    onBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(imageVector = Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save to Vault & Cloud", fontWeight = FontWeight.Bold)
            }
        }
    }
}
