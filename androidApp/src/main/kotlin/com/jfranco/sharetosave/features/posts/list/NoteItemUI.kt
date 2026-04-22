package com.jfranco.sharetosave.features.posts.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.jfranco.sharetosave.domain.Note
import java.time.LocalDateTime

@Composable
fun NoteItemUI(
    note: Note,
    modifier: Modifier = Modifier,
    onDeleteClicked: () -> Unit,
    onShareClicked: (Note) -> Unit
) {
    Card(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(note.color))
        ) {
            // Title and Content Section
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Column {
                    if (note.image != null) {
                        AsyncImage(
                            model = note.image,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Text(
                        text = note.title ?: "",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = note.content ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 10,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Share and Delete Icons
                    if (note.id != null)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            IconButton(
                                onClick = { onShareClicked(note) },
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share Note",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            IconButton(
                                onClick = onDeleteClicked,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Note",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun NoteItemUIPreview() {
    NoteItemUI(
        note = Note(
            id = 1,
            title = "Titulo",
            content = "Contenido de la nota",
            image = null,
            created = LocalDateTime.now(),
            edited = null,
            color = -1,
        ),
        modifier = Modifier.padding(16.dp),
        onDeleteClicked = {},
        onShareClicked = {}
    )
}