package prime.prime.domain.absence.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import prime.prime.domain.absence.models.*;
import prime.prime.infrastructure.security.ProgresoUserDetails;

import java.time.LocalDate;
import java.util.List;

public interface AbsenceTrackingService {

    AbsenceResponseDto createAbsenceRequest(AbsenceRequestDto absenceRequestDto, ProgresoUserDetails user);

    AbsenceResponseDto changeStatus(Long id, AbsenceRequestStatusDto absenceRequestStatusDto);

    AbsenceResponseDto getById(Long id);

    Page<AbsenceResponseDto> getAll(SearchAbsenceRequestDto searchAbsenceRequestDto, Pageable pageable, ProgresoUserDetails user);

    AbsenceResponseDto update(Long id, AbsenceRequestUpdateDto absenceRequestUpdateDto, ProgresoUserDetails user);

    void delete(Long id);

    List<CalendarAbsenceResponseDto> getAbsencesByDate(LocalDate startDate, LocalDate endDate,
                                                       SearchCalendarAbsenceRequestDto calendarAbsenceRequestDto,
                                                       ProgresoUserDetails user);
}
