    package ph.edu.comteq.gimena

    import androidx.room.Dao
    import androidx.room.Delete
    import  androidx.room.Insert
    import androidx.room.Query
    import androidx.room.Update
    import kotlinx.coroutines.flow.Flow


    @Dao
    interface NoteDao {
        @Insert
        suspend fun insertNote (note: Note): Long

        @Update
        suspend fun updateNote (note: Note)

        @Delete
        suspend fun deleteNote (note: Note)

        @Query("SELECT * FROM notes WHERE id = :id")
        suspend fun getNoteById(id: Int): Note?

        @Query("SELECT * FROM notes ORDER BY created_at DESC")
        fun getAllNotes(): Flow<List<Note>>

        @Query("DELETE FROM notes")
        suspend fun deleteAllNotes()

        @Query("SELECT * FROM notes WHERE title LIKE '%' || :searchQuery || '%' OR content LIKE '%' || :searchQuery || '%' ORDER BY id DESC")
        fun searchNotes(searchQuery: String): Flow<List<Note>>
    }