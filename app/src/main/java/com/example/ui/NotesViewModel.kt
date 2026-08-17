package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.NoteEntity
import com.example.data.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotesViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: NoteRepository

    init {
        val noteDao = AppDatabase.getDatabase(application).noteDao()
        repository = NoteRepository(noteDao)
        viewModelScope.launch {
            repository.fetchNotesFromCloud()
        }
    }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    val notes: StateFlow<List<NoteEntity>> = combine(
        repository.allNotes,
        _searchQuery,
        _selectedCategory
    ) { allNotes, query, category ->
        allNotes.filter { note ->
            val matchesCategory = category == "All" || note.category.equals(category, ignoreCase = true)
            val matchesQuery = query.isBlank() ||
                    note.title.contains(query, ignoreCase = true) ||
                    note.content.contains(query, ignoreCase = true) ||
                    note.linkUrl.contains(query, ignoreCase = true) ||
                    note.category.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun saveNote(id: String?, title: String, content: String, linkUrl: String, category: String) {
        viewModelScope.launch {
            val note = NoteEntity(
                id = id ?: java.util.UUID.randomUUID().toString(),
                title = title.ifBlank { "Untitled Note" },
                content = content,
                linkUrl = linkUrl,
                category = category,
                timestamp = System.currentTimeMillis()
            )
            repository.saveNote(note)
        }
    }

    fun deleteNote(id: String) {
        viewModelScope.launch {
            repository.deleteNote(id)
        }
    }

    fun refreshCloudSync() {
        viewModelScope.launch {
            repository.fetchNotesFromCloud()
        }
    }
}
