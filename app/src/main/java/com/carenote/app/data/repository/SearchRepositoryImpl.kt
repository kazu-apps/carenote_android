package com.carenote.app.data.repository

import com.carenote.app.domain.model.SearchResult
import com.carenote.app.domain.repository.CalendarEventRepository
import com.carenote.app.domain.repository.EmergencyContactRepository
import com.carenote.app.domain.repository.HealthRecordRepository
import com.carenote.app.domain.repository.MedicationRepository
import com.carenote.app.domain.repository.NoteRepository
import com.carenote.app.domain.repository.SearchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SearchRepositoryImpl @Inject constructor(
    private val medicationRepository: MedicationRepository,
    private val noteRepository: NoteRepository,
    private val healthRecordRepository: HealthRecordRepository,
    private val calendarEventRepository: CalendarEventRepository,
    private val emergencyContactRepository: EmergencyContactRepository
) : SearchRepository {

    override fun searchAll(query: String): Flow<List<SearchResult>> {
        val medicationFlow = medicationRepository.searchMedications(query)
            .map { list -> list.map { SearchResult.MedicationResult(it) } }

        val noteFlow = noteRepository.searchNotes(query, null)
            .map { list -> list.map { SearchResult.NoteResult(it) } }

        val healthRecordFlow = healthRecordRepository.getAllRecords()
            .map { list ->
                list.filter { record ->
                    record.conditionNote.contains(query, ignoreCase = true)
                }.map { SearchResult.HealthRecordResult(it) }
            }

        val calendarEventFlow = calendarEventRepository.getAllEvents()
            .map { list ->
                list.filter { event ->
                    event.title.contains(query, ignoreCase = true) ||
                        event.description.contains(query, ignoreCase = true)
                }.map { SearchResult.CalendarEventResult(it) }
            }

        val emergencyContactFlow = emergencyContactRepository.getAllContacts()
            .map { list ->
                list.filter { contact ->
                    contact.name.contains(query, ignoreCase = true) ||
                        contact.phoneNumber.contains(query, ignoreCase = true) ||
                        contact.memo.contains(query, ignoreCase = true)
                }.map { SearchResult.EmergencyContactResult(it) }
            }

        val firstHalf = combine(medicationFlow, noteFlow, healthRecordFlow) { a, b, c -> a + b + c }
        val secondHalf = combine(calendarEventFlow, emergencyContactFlow) { d, e -> d + e }

        return combine(firstHalf, secondHalf) { first, second ->
            (first + second).sortedByDescending { it.timestamp }
        }
    }
}
