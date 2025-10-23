package ph.edu.comteq.gimena

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch



class NoteViewModel(application: Application) : AndroidViewModel(application) {
    private val noteDao: NoteDao = AppDatabase.getDatabase(application).noteDao()
    val allNotes: Flow<List<Note>> = noteDao.getAllNotes()

    fun insert(note: Note) = viewModelScope.launch {
        noteDao.insertNote(note)
    }

    fun update(note: Note) = viewModelScope.launch {
        noteDao.updateNote(note)
    }

    fun delete(note: Note) = viewModelScope.launch {
        noteDao.deleteNote(note)
    }
}