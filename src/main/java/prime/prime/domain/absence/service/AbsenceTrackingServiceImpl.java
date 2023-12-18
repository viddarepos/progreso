package prime.prime.domain.absence.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import prime.prime.domain.absence.entity.AbsenceRequest;
import prime.prime.domain.absence.entity.AbsenceRequestStatus;
import prime.prime.domain.absence.entity.AbsenceRequestType;
import prime.prime.domain.absence.mapper.AbsenceRequestMapper;
import prime.prime.domain.absence.models.*;
import prime.prime.domain.absence.repository.AbsenceRepository;
import prime.prime.domain.absence.repository.AbsenceRequestSpecification;
import prime.prime.domain.absence.repository.CalendarAbsenceSpecification;
import prime.prime.domain.role.Role;
import prime.prime.domain.season.entity.Season;
import prime.prime.domain.season.service.SeasonService;
import prime.prime.domain.season.utility.SeasonUtility;
import prime.prime.domain.user.entity.User;
import prime.prime.domain.user.service.UserService;
import prime.prime.infrastructure.exception.AbsenceException;
import prime.prime.infrastructure.exception.InvalidDateException;
import prime.prime.infrastructure.exception.NotFoundException;
import prime.prime.infrastructure.jobs.EmailSendingJob;
import prime.prime.infrastructure.security.ProgresoUserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Service
public class AbsenceTrackingServiceImpl implements AbsenceTrackingService {

    private final UserService userService;
    private final AbsenceRepository absenceRepository;
    private final AbsenceRequestMapper absenceRequestMapper;
    private final SeasonService seasonService;
    private final EmailSendingJob emailSendingJob;

    public AbsenceTrackingServiceImpl(UserService userService, AbsenceRepository absenceRepository,
                                      AbsenceRequestMapper absenceRequestMapper, SeasonService seasonService,
                                      EmailSendingJob emailSendingJob) {
        this.userService = userService;
        this.absenceRepository = absenceRepository;
        this.absenceRequestMapper = absenceRequestMapper;
        this.seasonService = seasonService;
        this.emailSendingJob = emailSendingJob;
    }

    @Override
    public AbsenceResponseDto createAbsenceRequest(AbsenceRequestDto absenceRequestDto, ProgresoUserDetails user) {
        User requester = userService.getEntityById(user.getUserId());

        AbsenceRequest absenceRequest = absenceRequestMapper.toAbsenceRequest(absenceRequestDto);
        absenceRequest.setRequester(requester);
        Season activeSeason = seasonService.findActiveSeason(requester, absenceRequestDto.seasonId());
        absenceRequest.setSeason(activeSeason);

        LocalDateTime requestedStartTime = absenceRequestDto.startTime();
        LocalDateTime requestedEndTime = absenceRequestDto.endTime();

        SeasonUtility.validateStartDateIsBeforeEndDate(requestedStartTime.toLocalDate(),requestedEndTime.toLocalDate());
        checkDateWithinSeason(activeSeason, requestedStartTime, requestedEndTime);
        checkNumberOfAbsences(requester, activeSeason, requestedStartTime, requestedEndTime);

        if (absenceRequestDto.absenceType().equals(AbsenceRequestType.SICK_LEAVE)) {
            absenceRequest.setStatus(AbsenceRequestStatus.APPROVED);
            scheduleMailSending(absenceRequest,"Absence approved");
        } else {
            absenceRequest.setStatus(AbsenceRequestStatus.PENDING);
        }

        return absenceRequestMapper.toAbsenceResponse(absenceRepository.save(absenceRequest));
    }

    private void checkNumberOfAbsences(User requester, Season activeSeason, LocalDateTime requestedStartTime,
                           LocalDateTime requestedEndTime) {
        if (absenceRepository.numberOfAbsencesForGivenDatesAndEmployee(requester.getId(), activeSeason.getId(), requestedStartTime, requestedEndTime) > 0) {
            throw new AbsenceException("You have already taken days off in requested time span");
        }
    }

    private void checkDateWithinSeason(Season activeSeason, LocalDateTime requestedStartTime, LocalDateTime requestedEndTime) {
        if (!SeasonUtility.isWithinSeason(activeSeason, requestedStartTime, requestedEndTime)) {
            throw new InvalidDateException("Start date and end date of absence must be within season date range");
        }
    }

    @Override
    public AbsenceResponseDto changeStatus(Long id, AbsenceRequestStatusDto absenceRequestStatusDto) {
        AbsenceRequest absenceRequest = findById(id);
        AbsenceRequestStatus absenceRequestStatus = absenceRequest.getStatus();

        if(!absenceRequestStatus.equals(AbsenceRequestStatus.PENDING)){
            throw new AbsenceException("Can't change status of already approved or rejected absence requests");
        }

        LocalDate absenceStartDate = absenceRequest.getStartTime().toLocalDate();
        if(absenceRequestStatusDto.status().equals(AbsenceRequestStatus.APPROVED) && absenceStartDate.isBefore(LocalDate.now().plusDays(1))){
            throw new AbsenceException("Absence must be approved at least 1 day before the requested start time");
        }

        AbsenceRequestStatus newStatus =  switch (absenceRequestStatusDto.status()) {
            case REJECTED -> AbsenceRequestStatus.REJECTED;
            case APPROVED -> AbsenceRequestStatus.APPROVED;
            default -> throw new AbsenceException("Invalid absence status!");
        };
        absenceRequest.setStatus(newStatus);
        absenceRepository.save(absenceRequest);
        scheduleMailSending(absenceRequest,absenceRequestStatusDto.comment());

        return absenceRequestMapper.toAbsenceResponse(absenceRequest);

    }

    @Override
    public AbsenceResponseDto getById(Long id) {
       return absenceRequestMapper.toAbsenceResponse(findById(id));
    }

    @Override
    public Page<AbsenceResponseDto> getAll(SearchAbsenceRequestDto searchAbsenceRequestDto, Pageable pageable, ProgresoUserDetails userDetails) {

        List<Long> seasons = getSeasonsForUser(userDetails);
        return absenceRepository.
               findAll(new AbsenceRequestSpecification(searchAbsenceRequestDto, seasons),pageable).
               map(absenceRequestMapper::toAbsenceResponse);
    }

    @Override
    public AbsenceResponseDto update(Long id, AbsenceRequestUpdateDto absenceRequestUpdateDto, ProgresoUserDetails user) {
        User requester = userService.getEntityById(user.getUserId());
        AbsenceRequest absenceRequest = findById(id);
        Season season = seasonService.findById(absenceRequest.getSeason().getId());

        Season activeSeason = seasonService.findActiveSeason(requester, season.getId());
        absenceRequest.setSeason(activeSeason);

        LocalDateTime startTime = absenceRequestUpdateDto.startTime();
        LocalDateTime endTime = absenceRequestUpdateDto.endTime();
        if (startTime != null && endTime != null) {
            SeasonUtility.validateStartDateIsBeforeEndDate(startTime.toLocalDate(), endTime.toLocalDate());
        }
        checkIfDateTimeIsWithinSeason(startTime, activeSeason);
        checkIfDateTimeIsWithinSeason(endTime, activeSeason);

        absenceRequestMapper.update(absenceRequest,absenceRequestUpdateDto);
        return absenceRequestMapper.toAbsenceResponse(absenceRepository.save(absenceRequest));
    }

    @Override
    public void delete(Long id) {
        if (!absenceRepository.existsById(id)) {
            throw new NotFoundException(AbsenceRequest.class.getSimpleName(), "id", id.toString());
        }
        absenceRepository.deleteById(id);
    }

    @Override
    public List<CalendarAbsenceResponseDto> getAbsencesByDate(LocalDate startDate, LocalDate endDate, SearchCalendarAbsenceRequestDto searchCalendarAbsenceRequestDto, ProgresoUserDetails user) {

        List<Long> seasonIds = getSeasonsForUser(user);

        LocalDateTime startTime = startDate.atTime(LocalTime.MIN);
        LocalDateTime endTime = endDate.atTime(LocalTime.MAX);

        List<Long> absenceIds = absenceRepository.
                getAbsenceIdsByDateForUser(startTime, endTime, seasonIds);

        return absenceRepository.
                findAll(new CalendarAbsenceSpecification(searchCalendarAbsenceRequestDto, absenceIds)).
                    stream().map(absenceRequestMapper::toCalendarAbsences).toList();
    }

    private void scheduleMailSending(AbsenceRequest absenceRequest,String comment) {
        User requester = absenceRequest.getRequester();
        String subject =
                "Absence request " + absenceRequest.getStatus().name().toLowerCase() + ": " + absenceRequest.getTitle();

        emailSendingJob.scheduleEmailJob(requester.getAccount().getEmail(),subject,Map.of("fullName", requester.getFullName(),
                "status", absenceRequest.getStatus().name(),
                "title", absenceRequest.getTitle(),
                "comment",comment,
                "template", "event_request_email"));
    }

    private AbsenceRequest findById(Long id) {
        return absenceRepository.findById(id).orElseThrow(
                () -> new NotFoundException(AbsenceRequest.class.getSimpleName(), "id", id.toString()));
    }

    private List<Long> getSeasonsForUser(ProgresoUserDetails userDetails) {
        User user = userService.getEntityById(userDetails.getUserId());

        if (user.getSeasons() != null && isInternOrMentor(user)) {
            return user.getSeasons().stream().map(Season::getId).toList();
        }

        return seasonService.getAllSeasonIds();
    }

    private boolean isInternOrMentor(User user) {
        return Role.INTERN.equals(user.getAccount().getRole())
                || Role.MENTOR.equals(user.getAccount().getRole());
    }

    private void checkIfDateTimeIsWithinSeason(LocalDateTime localDateTime, Season activeSeason) {
        if (localDateTime != null && !SeasonUtility.isWithinSeason(activeSeason, localDateTime)) {
            throw new InvalidDateException("Start date and end date of absence must be within season date range");
        }
    }
}
