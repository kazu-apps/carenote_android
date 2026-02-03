package com.carenote.app.ui.screens.healthrecords.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.carenote.app.R
import com.carenote.app.domain.model.ExcretionType
import com.carenote.app.domain.model.MealAmount
import com.carenote.app.ui.screens.healthrecords.AddEditHealthRecordFormState
import com.carenote.app.ui.screens.healthrecords.AddEditHealthRecordViewModel
import com.carenote.app.ui.theme.ChipShape

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun SelectionFormSection(
    formState: AddEditHealthRecordFormState,
    viewModel: AddEditHealthRecordViewModel
) {
    MealSection(
        selectedMeal = formState.meal,
        onMealSelected = viewModel::updateMeal
    )

    ExcretionSection(
        selectedExcretion = formState.excretion,
        onExcretionSelected = viewModel::updateExcretion
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MealSection(
    selectedMeal: MealAmount?,
    onMealSelected: (MealAmount?) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.health_records_meal),
            style = MaterialTheme.typography.titleMedium
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            MealAmount.entries.forEach { meal ->
                FilterChip(
                    selected = selectedMeal == meal,
                    onClick = {
                        onMealSelected(if (selectedMeal == meal) null else meal)
                    },
                    label = { Text(text = mealAmountLabel(meal)) },
                    shape = ChipShape
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ExcretionSection(
    selectedExcretion: ExcretionType?,
    onExcretionSelected: (ExcretionType?) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.health_records_excretion),
            style = MaterialTheme.typography.titleMedium
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ExcretionType.entries.forEach { excretion ->
                FilterChip(
                    selected = selectedExcretion == excretion,
                    onClick = {
                        onExcretionSelected(
                            if (selectedExcretion == excretion) null else excretion
                        )
                    },
                    label = { Text(text = excretionTypeLabel(excretion)) },
                    shape = ChipShape
                )
            }
        }
    }
}
