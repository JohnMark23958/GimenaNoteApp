package ph.edu.comteq.gimena

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch



class NoteViewModel(application: Application) : AndroidViewModel(application) {
    private val noteDao: NoteDao = AppDatabase.getDatabase(application).noteDao()


    // Track what the user is searching for
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Smart notes: shows all notes OR search results
    val allNotes: Flow<List<Note>> = searchQuery.flatMapLatest { query ->
        if (query.isBlank()) {
            noteDao.getAllNotes()  // Show everything
        } else {
            noteDao.searchNotes(query)  // Show only matches
        }
    }

    // Call this when user types in search box
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // Call this to clear the search
    fun clearSearch() {
        _searchQuery.value = ""
    }

    fun insert(note: Note) = viewModelScope.launch {
        noteDao.insertNote(note)
    }

    fun update(note: Note) = viewModelScope.launch {
        noteDao.updateNote(note)
    }

    fun delete(note: Note) = viewModelScope.launch {
        noteDao.deleteNote(note)
    }

    // --- ADDED FUNCTION ---
    /**
     * Fetches a single note by its ID from the database.
     */
    suspend fun getNoteById(id: Int): Note? {
        return noteDao.getNoteById(id)
    }
}