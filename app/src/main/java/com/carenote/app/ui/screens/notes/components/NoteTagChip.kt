package com.carenote.app.ui.screens.notes.components

import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.carenote.app.R
import com.carenote.app.domain.model.NoteTag
import com.carenote.app.ui.theme.ChipShape
import com.carenote.app.ui.theme.NoteTagConditionColor
import com.carenote.app.ui.theme.NoteTagMealColor
import com.carenote.app.ui.theme.NoteTagOtherColor
import com.carenote.app.ui.theme.NoteTagReportColor

@Composable
fun NoteTagChip(
    tag: NoteTag,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tagColor = when (tag) {
        NoteTag.CONDITION -> NoteTagConditionColor
        NoteTag.MEAL -> NoteTagMealColor
        NoteTag.REPORT -> NoteTagReportColor
        NoteTag.OTHER -> NoteTagOtherColor
    }

    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = noteTagLabel(tag),
                style = MaterialTheme.typography.labelLarge
            )
        },
        shape = ChipShape,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = tagColor.copy(alpha = 0.2f),
            selectedLabelColor = tagColor
        ),
        border = FilterChipDefaults.filterChipBorder(
            borderColor = tagColor.copy(alpha = 0.5f),
            selectedBorderColor = tagColor,
            enabled = true,
            selected = selected
        ),
        modifier = modifier
    )
}

@Composable
fun noteTagLabel(tag: NoteTag): String {
    return when (tag) {
        NoteTag.CONDITION -> stringResource(R.string.notes_tag_condition)
        NoteTag.MEAL -> stringResource(R.string.notes_tag_meal)
        NoteTag.REPORT -> stringResource(R.string.notes_tag_report)
        NoteTag.OTHER -> stringResource(R.string.notes_tag_other)
    }
}
