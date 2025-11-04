package ph.edu.comteq.gimena


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import ph.edu.comteq.gimena.ui.theme.GimenaTheme


/**
 * Sealed class to define navigation destinations.
 */
sealed class Screen {
    object NoteList : Screen()
    data class AddEditNote(val noteId: Int?) : Screen()
}

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    private val viewModel: NoteViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GimenaTheme() {
                // --- NAVIGATION STATE ---
                var currentScreen by remember { mutableStateOf<Screen>(Screen.NoteList) }

                var searchQuery by remember { mutableStateOf("") }
                var isSearchActive by remember { mutableStateOf(false) }
                val notes by viewModel.allNotes.collectAsState(initial = emptyList())

                // --- NAVIGATION LOGIC ---
                when (val screen = currentScreen) {
                    is Screen.NoteList -> {
                        // --- MAIN LIST / SEARCH SCREEN ---
                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            topBar = {
                                if (isSearchActive) {
                                    // SEARCH MODE: Show the SearchBar
                                    SearchBar(
                                        modifier = Modifier.fillMaxWidth(),
                                        query = searchQuery,
                                        onQueryChange = {
                                            searchQuery = it
                                            viewModel.updateSearchQuery(it)
                                        },
                                        onSearch = { isSearchActive = false },
                                        active = true,
                                        onActiveChange = { shouldExpand ->
                                            if (!shouldExpand) {
                                                isSearchActive = false
                                                searchQuery = ""
                                                viewModel.clearSearch()
                                            }
                                        },
                                        placeholder = { Text("Search notes...") },
                                        leadingIcon = {
                                            IconButton(onClick = {
                                                isSearchActive = false
                                                searchQuery = ""
                                                viewModel.clearSearch()
                                            }) {
                                                Icon(
                                                    Icons.AutoMirrored.Filled.ArrowBack,
                                                    contentDescription = "Close search"
                                                )
                                            }
                                        },
                                        trailingIcon = {
                                            if (searchQuery.isNotEmpty()) {
                                                IconButton(onClick = {
                                                    searchQuery = ""
                                                    viewModel.clearSearch()
                                                }) {
                                                    Icon(
                                                        Icons.Default.Clear,
                                                        contentDescription = "Clear search"
                                                    )
                                                }
                                            }
                                        }
                                    ) {
                                        // Content shown INSIDE the search view
                                        LazyColumn(
                                            modifier = Modifier.fillMaxSize(),
                                            contentPadding = PaddingValues(16.dp)
                                        ) {
                                            if (notes.isEmpty()) {
                                                item {
                                                    Text(
                                                        text = "No notes found",
                                                        modifier = Modifier.padding(16.dp),
                                                        style = MaterialTheme.typography.bodyLarge,
                                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                                    )
                                                }
                                            } else {
                                                items(notes) { note ->
                                                    // Go to edit screen on click
                                                    NoteCard(
                                                        note = note,
                                                        onClick = {
                                                            isSearchActive = false
                                                            currentScreen = Screen.AddEditNote(note.id)
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    // NORMAL MODE: Show TopAppBar
                                    TopAppBar(
                                        title = { Text("Notes") },
                                        actions = {
                                            IconButton(onClick = { isSearchActive = true }) {
                                                Icon(Icons.Filled.Search, "Search")
                                            }
                                        }
                                    )

                                }
                            },
                            floatingActionButton = {
                                // --- FAB CLICK NOW NAVIGATES ---
                                FloatingActionButton(onClick = {
                                    currentScreen = Screen.AddEditNote(noteId = null)
                                }) {
                                    Icon(Icons.Filled.Add, "Add note")
                                }
                            }
                        ) { innerPadding ->
                            NoteListScreen(
                                viewModel = viewModel,
                                modifier = Modifier.padding(innerPadding),
                                // Go to edit screen on click
                                onNoteClick = { note ->
                                    currentScreen = Screen.AddEditNote(note.id)
                                }
                            )
                        }
                    }

                    is Screen.AddEditNote -> {
                        // --- ADD/EDIT SCREEN ---
                        AddEditNoteScreen(
                            viewModel = viewModel,
                            noteId = screen.noteId,
                            onNavigateBack = {
                                currentScreen = Screen.NoteList
                            }
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun NoteListScreen(
    viewModel: NoteViewModel,
    modifier: Modifier = Modifier,
    onNoteClick: (Note) -> Unit // Callback for when a note is clicked
) {
    // Get all notes from viewmodel
    val notes by viewModel.allNotes.collectAsState(initial = emptyList())

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(8.dp) // Add padding around the list
    ) {
        if (notes.isEmpty() && viewModel.searchQuery.value.isBlank()) {
            item {
                Text(
                    text = "No notes yet. Tap '+' to add one.",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        } else {
            items(notes) { note ->
                // Use the clickable NoteCard
                NoteCard(
                    note = note,
                    onClick = { onNoteClick(note) }
                )
            }
        }
    }
}

@Composable
fun NoteCard(
    note: Note,
    modifier: Modifier = Modifier,
    onClick: () -> Unit // Callback for clicks
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(all = 8.dp)
            .clickable { onClick() }, // Make the card clickable
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(all = 16.dp)) {
            Text(
                text = DateUtils.formatDateTime(timestamp = note.createdAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = note.title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2
            )
            if (note.content.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = note.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    maxLines = 4
                )
            }
        }
    }
}

// --- NEW COMPOSABLE ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditNoteScreen(
    viewModel: NoteViewModel,
    noteId: Int?,
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()

    // State for the note being edited
    var note by remember { mutableStateOf<Note?>(null) }
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    // Load the note if noteId is not null
    LaunchedEffect(key1 = noteId) {
        if (noteId != null) {
            val existingNote = viewModel.getNoteById(noteId)
            if (existingNote != null) {
                note = existingNote
                title = existingNote.title
                content = existingNote.content
            }
        }
    }

    val isNewNote = noteId == null
    val screenTitle = if (isNewNote) "Add Note" else "Edit Note"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(screenTitle) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Show Delete button only when editing an existing note
                    if (!isNewNote) {
                        IconButton(onClick = {
                            scope.launch {
                                note?.let { viewModel.delete(it) }
                                onNavigateBack()
                            }
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Note")
                        }
                    }
                    // Save Button
                    IconButton(onClick = {
                        scope.launch {
                            if (isNewNote) {
                                viewModel.insert(
                                    Note(
                                        title = title,
                                        content = content,
                                        createdAt = System.currentTimeMillis()
                                    )
                                )
                            } else {
                                // Update existing note
                                note?.let {
                                    viewModel.update(
                                        it.copy(
                                            title = title,
                                            content = content
                                        )
                                    )
                                }
                            }
                            onNavigateBack()
                        }
                    }) {
                        Icon(Icons.Default.Check, contentDescription = "Save Note")
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
        ) {
            // Title Field
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))
            // Content Field
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Content") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f), // Take remaining space
                textStyle = MaterialTheme.typography.bodyLarge
            )
        }
    }
}