package com.example.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class NoteRepository(private val noteDao: NoteDao) {
    val allNotes: Flow<List<NoteEntity>> = noteDao.getAllNotes()

    fun searchNotes(query: String): Flow<List<NoteEntity>> = noteDao.searchNotes(query)

    suspend fun saveNote(note: NoteEntity) {
        // Save locally
        noteDao.insertNote(note)
        // Sync to Firestore if user is logged in
        try {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null) {
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .collection("notes")
                    .document(note.id)
                    .set(note.toMap())
                    .await()
            }
        } catch (e: Exception) {
            // Local persistence handles offline state or missing config
        }
    }

    suspend fun deleteNote(id: String) {
        noteDao.deleteNoteById(id)
        try {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null) {
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .collection("notes")
                    .document(id)
                    .delete()
                    .await()
            }
        } catch (e: Exception) {
            // Ignore
        }
    }

    suspend fun fetchNotesFromCloud() {
        try {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
            val snapshot = FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("notes")
                .get()
                .await()

            val cloudNotes = snapshot.documents.mapNotNull { doc ->
                val data = doc.data
                if (data != null) NoteEntity.fromMap(data) else null
            }
            if (cloudNotes.isNotEmpty()) {
                noteDao.insertNotes(cloudNotes)
            }
        } catch (e: Exception) {
            // Handle network/firestore error or missing config gracefully
        }
    }
}
