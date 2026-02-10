package com.carenote.app.di

import com.carenote.app.domain.repository.MedicationLogRepository
import com.carenote.app.domain.repository.MedicationRepository
import com.carenote.app.domain.repository.TaskRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun medicationRepository(): MedicationRepository
    fun medicationLogRepository(): MedicationLogRepository
    fun taskRepository(): TaskRepository
}
