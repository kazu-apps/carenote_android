package com.carenote.app.ui.screens.notes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.carenote.app.R
import com.carenote.app.config.AppConfig
import com.carenote.app.domain.model.NoteTag
import com.carenote.app.domain.model.Photo
import com.carenote.app.config.AppConfig.Photo as PhotoConfig
import com.carenote.app.domain.model.NoteComment
import com.carenote.app.ui.components.CareNoteAddEditScaffold
import com.carenote.app.ui.components.CareNoteTextField
import com.carenote.app.ui.components.PhotoPickerSection
import com.carenote.app.ui.screens.notes.components.NoteTagChip
import com.carenote.app.ui.preview.LightDarkPreview
import com.carenote.app.ui.preview.PreviewData
import com.carenote.app.ui.theme.ButtonShape
import com.carenote.app.ui.theme.CareNoteTheme
import com.carenote.app.ui.util.SnackbarEvent
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditNoteScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: AddEditNoteViewModel = hiltViewModel()
) {
    val formState by viewModel.formState.collectAsStateWithLifecycle()
    val photos by viewModel.photos.collectAsStateWithLifecycle()
    val comments by viewModel.comments.collectAsStateWithLifecycle()
    val commentText by viewModel.commentText.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.savedEvent.collect { saved ->
            if (saved) {
                onNavigateBack()
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.snackbarController.events.collect { event ->
            val message = when (event) {
                is SnackbarEvent.WithResId -> context.getString(event.messageResId)
                is SnackbarEvent.WithString -> event.message
            }
            snackbarHostState.showSnackbar(message)
        }
    }

    val title = if (formState.isEditMode) {
        stringResource(R.string.notes_edit)
    } else {
        stringResource(R.string.notes_add)
    }

    CareNoteAddEditScaffold(
        title = title,
        onNavigateBack = onNavigateBack,
        isDirty = viewModel.isDirty,
        snackbarHostState = snackbarHostState
    ) { innerPadding ->
        NoteFormBody(
            formState = formState,
            photos = photos,
            comments = comments,
            commentText = commentText,
            viewModel = viewModel,
            onNavigateBack = onNavigateBack,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Suppress("LongParameterList")
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NoteFormBody(
    formState: AddEditNoteFormState,
    photos: List<Photo>,
    comments: List<NoteComment>,
    commentText: String,
    viewModel: AddEditNoteViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        NoteTextFields(formState, viewModel::updateTitle, viewModel::updateContent)
        NoteTagSelector(formState, viewModel::updateTag)
        PhotoPickerSection(
            photos = photos,
            onAddPhotos = viewModel::addPhotos,
            onRemovePhoto = viewModel::removePhoto,
            maxPhotos = PhotoConfig.MAX_PHOTOS_PER_PARENT
        )
        if (formState.isEditMode) {
            HorizontalDivider()
            NoteCommentSection(
                comments = comments,
                commentText = commentText,
                onCommentTextChanged = viewModel::updateCommentText,
                onAddComment = viewModel::addComment,
                onDeleteComment = viewModel::deleteComment
            )
        }
        NoteFormActions(formState.isSaving, viewModel::saveNote, onNavigateBack)
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun NoteTextFields(
    formState: AddEditNoteFormState,
    onUpdateTitle: (String) -> Unit,
    onUpdateContent: (String) -> Unit
) {
    CareNoteTextField(
        value = formState.title,
        onValueChange = onUpdateTitle,
        label = stringResource(R.string.notes_title_label),
        placeholder = stringResource(R.string.notes_title_placeholder),
        errorMessage = formState.titleError,
        singleLine = true
    )
    CareNoteTextField(
        value = formState.content,
        onValueChange = onUpdateContent,
        label = stringResource(R.string.notes_content_label),
        placeholder = stringResource(R.string.notes_content_placeholder),
        errorMessage = formState.contentError,
        singleLine = false,
        maxLines = Int.MAX_VALUE,
        modifier = Modifier.height(
            (AppConfig.Note.CONTENT_MIN_LINES * 28).dp
        )
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NoteTagSelector(
    formState: AddEditNoteFormState,
    onUpdateTag: (NoteTag) -> Unit
) {
    Text(
        text = stringResource(R.string.notes_tag_label),
        style = MaterialTheme.typography.titleMedium
    )
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        NoteTag.entries.forEach { tag ->
            NoteTagChip(
                tag = tag,
                selected = formState.tag == tag,
                onClick = { onUpdateTag(tag) }
            )
        }
    }
}

@Composable
private fun NoteFormActions(
    isSaving: Boolean,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    Spacer(modifier = Modifier.height(8.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier.weight(1f),
            shape = ButtonShape
        ) {
            Text(text = stringResource(R.string.common_cancel))
        }
        Button(
            onClick = onSave,
            modifier = Modifier.weight(1f),
            shape = ButtonShape,
            enabled = !isSaving
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .height(20.dp)
                        .width(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(text = stringResource(R.string.common_save))
            }
        }
    }
}

@Suppress("LongParameterList")
@Composable
private fun NoteCommentSection(
    comments: List<NoteComment>,
    commentText: String,
    onCommentTextChanged: (String) -> Unit,
    onAddComment: () -> Unit,
    onDeleteComment: (Long) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.note_comment_section_title),
            style = MaterialTheme.typography.titleMedium
        )
        CommentList(comments = comments, onDeleteComment = onDeleteComment)
        CommentInput(
            commentText = commentText,
            onCommentTextChanged = onCommentTextChanged,
            onAddComment = onAddComment
        )
    }
}

@Composable
private fun CommentList(
    comments: List<NoteComment>,
    onDeleteComment: (Long) -> Unit
) {
    val dateTimeFormatter = remember {
        DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")
    }
    if (comments.isEmpty()) {
        Text(
            text = stringResource(R.string.note_comment_empty),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    } else {
        comments.forEach { comment ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = comment.content,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = comment.createdAt.format(dateTimeFormatter),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { onDeleteComment(comment.id) }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CommentInput(
    commentText: String,
    onCommentTextChanged: (String) -> Unit,
    onAddComment: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = commentText,
            onValueChange = onCommentTextChanged,
            modifier = Modifier.weight(1f),
            placeholder = { Text(stringResource(R.string.note_comment_placeholder)) },
            singleLine = true
        )
        Button(
            onClick = onAddComment,
            enabled = commentText.isNotBlank(),
            shape = ButtonShape
        ) {
            Text(stringResource(R.string.note_comment_add))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@LightDarkPreview
@Composable
private fun AddEditNoteFormPreview() {
    CareNoteTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CareNoteTextField(
                value = PreviewData.addEditNoteFormState.title,
                onValueChange = {},
                label = "Title",
                placeholder = ""
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                NoteTag.entries.forEach { tag ->
                    NoteTagChip(
                        tag = tag,
                        selected = PreviewData.addEditNoteFormState.tag == tag,
                        onClick = {}
                    )
                }
            }
        }
    }
}
