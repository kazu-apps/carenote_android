package com.carenote.app.di

import com.carenote.app.data.export.HealthRecordCsvExporter
import com.carenote.app.data.export.HealthRecordPdfExporter
import com.carenote.app.data.export.MedicationLogCsvExporter
import com.carenote.app.data.export.MedicationLogPdfExporter
import com.carenote.app.data.export.NoteCsvExporter
import com.carenote.app.data.export.NotePdfExporter
import com.carenote.app.data.export.TaskCsvExporter
import com.carenote.app.data.export.TaskPdfExporter
import com.carenote.app.data.local.ImageCompressor
import com.carenote.app.domain.repository.HealthRecordCsvExporterInterface
import com.carenote.app.domain.repository.HealthRecordPdfExporterInterface
import com.carenote.app.domain.repository.MedicationLogCsvExporterInterface
import com.carenote.app.domain.repository.MedicationLogPdfExporterInterface
import com.carenote.app.domain.repository.NoteCsvExporterInterface
import com.carenote.app.domain.repository.NotePdfExporterInterface
import com.carenote.app.domain.repository.TaskCsvExporterInterface
import com.carenote.app.domain.repository.TaskPdfExporterInterface
import com.carenote.app.domain.repository.ImageCompressorInterface
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ExporterModule {

    @Provides
    @Singleton
    fun provideImageCompressor(
        imageCompressor: ImageCompressor
    ): ImageCompressorInterface {
        return imageCompressor
    }

    @Provides
    @Singleton
    fun provideHealthRecordCsvExporter(
        csvExporter: HealthRecordCsvExporter
    ): HealthRecordCsvExporterInterface {
        return csvExporter
    }

    @Provides
    @Singleton
    fun provideHealthRecordPdfExporter(
        pdfExporter: HealthRecordPdfExporter
    ): HealthRecordPdfExporterInterface {
        return pdfExporter
    }

    @Provides
    @Singleton
    fun provideMedicationLogCsvExporter(
        csvExporter: MedicationLogCsvExporter
    ): MedicationLogCsvExporterInterface {
        return csvExporter
    }

    @Provides
    @Singleton
    fun provideMedicationLogPdfExporter(
        pdfExporter: MedicationLogPdfExporter
    ): MedicationLogPdfExporterInterface {
        return pdfExporter
    }

    @Provides
    @Singleton
    fun provideTaskCsvExporter(
        csvExporter: TaskCsvExporter
    ): TaskCsvExporterInterface {
        return csvExporter
    }

    @Provides
    @Singleton
    fun provideTaskPdfExporter(
        pdfExporter: TaskPdfExporter
    ): TaskPdfExporterInterface {
        return pdfExporter
    }

    @Provides
    @Singleton
    fun provideNoteCsvExporter(
        csvExporter: NoteCsvExporter
    ): NoteCsvExporterInterface {
        return csvExporter
    }

    @Provides
    @Singleton
    fun provideNotePdfExporter(
        pdfExporter: NotePdfExporter
    ): NotePdfExporterInterface {
        return pdfExporter
    }
}
