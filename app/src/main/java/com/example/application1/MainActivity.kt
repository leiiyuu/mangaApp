package com.example.application

import android.R
import android.icu.text.CaseMap
import android.os.Bundle
import android.widget.Filter
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.collection.IntFloatMap
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.application.ui.theme.ApplicationTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ReadApp()
                }
            }
        }
    }
}

enum class MangaStatus {
    plan_to_read,
    reading,
    completed
}

data class Manga(
    val id: Int,
    val title: String,
    val year: Int,
    val genre: String,
    val chapters: Int,
    var status: MangaStatus
)

val MangaList = listOf(
    Manga( id = 1, title = "sweet home", year = 2017, genre = "horror", chapters = 141, MangaStatus.completed),
    Manga( id = 2, title = "noragami", year = 2010, genre = "fantasy", chapters = 145, MangaStatus.reading),
    Manga( id = 3, title = "shotgun boy", year = 2021, genre = "horror", chapters = 68, MangaStatus.plan_to_read),
    Manga( id = 4, title = "lab rat", year = 2011, genre = "sci-fi", chapters = 1, MangaStatus.completed),
    Manga( id = 5, title = "bastard", year = 2014, genre = "thriller", chapters = 93, MangaStatus.completed),
    Manga( id = 6, title = "restart", year = 2020, genre = "thriller", chapters = 152, MangaStatus.reading),
)

class MangaStateHolder(
    private val allManga: List<Manga>
) {
    var searchQuery by mutableStateOf("")

    var statusFilter by mutableStateOf<MangaStatus?>(null)

    private val manga_list = mutableStateListOf<Manga>().apply {
        addAll(allManga)
    }
    val mangalist: List<Manga> = manga_list

    val filteredManga: List<Manga>
        get() = if (searchQuery.isBlank()) {
            manga_list
        } else {
            manga_list.filter { manga -> manga.title.contains(searchQuery, ignoreCase = true) }
        }

    val stats: String
        get() {
            val total = manga_list.size
            val plan_to_read = manga_list.count { it.status == MangaStatus.plan_to_read }
            val reading = manga_list.count { it.status == MangaStatus.reading }
            val completed = manga_list.count { it.status == MangaStatus.completed }

            return "all: $total | plan to read: $plan_to_read | reading: $reading | completed: $completed"
        }

    fun onSearchChange(newValue: String) {
        searchQuery = newValue
    }

    fun onStatusFilterChange(newFilter: MangaStatus?){
        statusFilter = newFilter
    }
    fun updateStatus(mangaId: Int, newStatus: MangaStatus) {
        val index = manga_list.indexOfFirst { it.id ==  mangaId}
        if (index != -1) {
            val currentManga = manga_list[index]
            manga_list[index] = currentManga.copy(status = newStatus)
        }
    }
}

@Composable
fun ReadApp() {
    val stateHolder = remember {
        MangaStateHolder(MangaList)
    }
    MangaListScreen(stateHolder.filteredManga, stateHolder.searchQuery, stateHolder::onSearchChange, stateHolder::updateStatus,stateHolder.statusFilter, stateHolder::onStatusFilterChange, stateHolder.stats)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MangaListScreen(mangalist: List<Manga>, searchQuery: String, onSearchChange: (String) -> Unit, onStatusUpdate: (Int, MangaStatus) -> Unit, statusFilter: MangaStatus?, onStatusFilterChange: (MangaStatus?) -> Unit, stats: String) {

    val displayList =  if (statusFilter == null) {
        mangalist
    } else {
        mangalist.filter { it.status == statusFilter }
    }

    Scaffold(topBar = { TopAppBar(title = { Text( text = "manga reader") }) } ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("search by title")},
                singleLine = true,
            )
            Spacer(modifier = Modifier.height( height = 16.dp))

            Text(
                text = stats,
                modifier = Modifier.padding(8.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = { onStatusFilterChange(null) }) {
                    Text("all")
                }
                Button(onClick = { onStatusFilterChange(MangaStatus.plan_to_read) }) {
                    Text("plan to read")
                }
                Button(onClick = { onStatusFilterChange(MangaStatus.reading) }) {
                    Text("reading")
                }
                Button(onClick = { onStatusFilterChange(MangaStatus.completed) }) {
                    Text("completed")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))


            if (displayList.isEmpty()) {
                Text( text = "   empty")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy( 8.dp)) {
                    items(items = displayList, key = { it.id }) { item ->
                        MangaCard(manga = item, onStatusUpdate = onStatusUpdate)
                    }
                }
            }
        }
    }
}

@Composable
fun MangaCard(manga: Manga, onStatusUpdate: (Int, MangaStatus) -> Unit) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(all = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween) {
        Column(modifier = Modifier.weight( weight = 1f)) {
            Text( text = manga.title, fontWeight = FontWeight.Bold)
            Text(text = "${manga.year} - ${manga.genre}")
            Text( text = "chapters ${manga.chapters}")

            Text(text = "status: ${manga.status}")

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { onStatusUpdate(manga.id, MangaStatus.plan_to_read) }
                ) {
                    Text("plan to read")
                }

                Button(
                    onClick = { onStatusUpdate(manga.id, MangaStatus.reading) }
                ) {
                    Text("reading")
                }

                Button(
                    onClick = { onStatusUpdate(manga.id, MangaStatus.completed) }
                ) {
                    Text("completed")
                }
            }
        }
    }
}
