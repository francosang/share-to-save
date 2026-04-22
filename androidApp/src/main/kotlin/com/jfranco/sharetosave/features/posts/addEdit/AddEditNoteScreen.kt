package com.jfranco.sharetosave.features.posts.addEdit

import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import com.jfranco.sharetosave.domain.Note
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.serialization.Serializable
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import java.time.LocalDateTime

@Serializable
data class AddEditScreenDestinationArgs(
    val note: Note? = null,
    val text: String? = null,
    val image: Uri? = null,
)

@Destination<RootGraph>(
    navArgs = AddEditScreenDestinationArgs::class
)
@Composable
fun AddEditScreen(
    navigator: DestinationsNavigator
) {
    Log.d("MyApp", "AddEditScreen ------------------")

    val viewModel = hiltViewModel<AddEditNoteViewModel>()
    val state by viewModel.collectAsState()

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is AddEditNoteSideEffect.NavigateBackWithResult ->
                navigator.navigateUp()
        }
    }

    Screen(state, navigator, viewModel)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Screen(
    state: AddEditNoteState,
    navigator: DestinationsNavigator,
    viewModel: AddEditNoteViewModel
) {
    Log.d("MyApp", "AddEditScreen: state: $state")

    val noteBgAnimation by animateColorAsState(
        targetValue = Color(state.color),
        animationSpec = tween(durationMillis = 300)
    )

    Scaffold(
        containerColor = noteBgAnimation,
        topBar = {
            TopAppBar(
                title = { Text("Details") },
                navigationIcon = {
                    IconButton(onClick = { navigator.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                modifier = Modifier.imePadding(),
                visible = state.saveEnabled
            ) {
                FloatingActionButton(
                    modifier = Modifier,
                    onClick = {
                        viewModel.onEvent(
                            AddEditNoteEvent.SaveNote(
                                Note(
                                    id = state.noteId,
                                    title = state.title.state.text.toString(),
                                    content = state.content.state.text.toString(),
                                    image = state.image,
                                    created = LocalDateTime.now(),
                                    edited = null,
                                    color = state.color
                                )
                            )
                        )
                    },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = "Save Note")
                }
            }
        },
        content = { padding ->
            Content(
                padding, state,
                onFocusChange = {
                    viewModel.onEvent(it)
                },
                onColorChanged = { colorInt ->
                    viewModel.onEvent(AddEditNoteEvent.ChangeColor(colorInt))
                }
            )
        }
    )
}

@Composable
private fun Content(
    padding: PaddingValues,
    state: AddEditNoteState,
    onColorChanged: (Int) -> Unit,
    onFocusChange: (AddEditNoteEvent.ChangeFocus) -> Unit
) {
    //making a Row to select colors
    Column(
        modifier = Modifier
            .padding(padding)
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            //make circles for each color we have
            Note.noteColors.forEach { color ->
                val colorInt = color.toArgb()

                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .shadow(15.dp, CircleShape)
                        .clip(CircleShape)
                        .background(color)
                        .border(
                            width = 4.dp,
                            color = if (state.color == colorInt) {
                                Color.Black  //color is selected
                            } else {
                                Color.Transparent  //color is deselected
                            },
                            shape = CircleShape
                        )
                        .clickable {
                            onColorChanged(colorInt)
                        }

                )
            }
        }

        if (state.image != null) {
            Log.d("MyApp", "AddEditScreen: image: ${state.image}")

            val localUri = state.image.toUri()
            val context = LocalContext.current
            val btm = if (Build.VERSION.SDK_INT < 28) {
                MediaStore.Images
                    .Media.getBitmap(context.contentResolver, localUri)
            } else {
                val source = ImageDecoder
                    .createSource(context.contentResolver, localUri)
                ImageDecoder.decodeBitmap(source)
            }

            Spacer(Modifier.height(8.dp))

            Image(
                bitmap = btm.asImageBitmap(),
                contentDescription = "Shared image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp)),
            )

//            AsyncImage(
//                model = btm.asImageBitmap(),
//                contentDescription = "Shared image",
//                contentScale = ContentScale.Crop,
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(200.dp)
//                    .clip(RoundedCornerShape(8.dp))
//            )
        }

        Spacer(Modifier.height(16.dp))

        // For Title
        HintUI(
            state = state.title.state,
            hint = state.title.hint,
            onFocusChange = {
                onFocusChange(AddEditNoteEvent.ChangeTitleFocus(it))
            },
            isHintVisible = state.title.isHintVisible,
            lineLimits = TextFieldLineLimits.SingleLine,
            textStyle = MaterialTheme.typography.bodyMedium
        )

        Spacer(Modifier.height(16.dp))

        // For Content
        HintUI(
            state = state.content.state,
            hint = state.content.hint,
            onFocusChange = {
                onFocusChange(AddEditNoteEvent.ChangeContentFocus(it))
            },
            isHintVisible = state.content.isHintVisible,
            textStyle = MaterialTheme.typography.bodyMedium,
            lineLimits = TextFieldLineLimits.MultiLine(),
            modifier = Modifier.fillMaxHeight()
        )
    }
}