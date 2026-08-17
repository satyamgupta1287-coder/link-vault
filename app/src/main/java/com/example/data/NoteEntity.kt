package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val content: String,
    val linkUrl: String = "",
    val category: String = "Notes", // Links, Notes, WhatsApp, Work, Personal, Important
    val timestamp: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
) {
    fun toMap(): Map<String, Any> = mapOf(
        "id" to id,
        "title" to title,
        "content" to content,
        "linkUrl" to linkUrl,
        "category" to category,
        "timestamp" to timestamp
    )

    companion object {
        fun fromMap(map: Map<String, Any>): NoteEntity {
            return NoteEntity(
                id = map["id"] as? String ?: java.util.UUID.randomUUID().toString(),
                title = map["title"] as? String ?: "",
                content = map["content"] as? String ?: "",
                linkUrl = map["linkUrl"] as? String ?: "",
                category = map["category"] as? String ?: "Notes",
                timestamp = (map["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                isSynced = true
            )
        }
    }
}
