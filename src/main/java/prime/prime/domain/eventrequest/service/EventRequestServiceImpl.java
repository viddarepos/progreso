package prime.prime.domain.eventrequest.service;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import prime.prime.domain.eventrequest.entity.EventRequest;
import prime.prime.domain.eventrequest.entity.EventRequestStatus;
import prime.prime.domain.eventrequest.mapper.EventRequestMapper;
import prime.prime.domain.eventrequest.models.*;
import prime.prime.domain.eventrequest.repository.EventRequestRepository;
import prime.prime.domain.eventrequest.repository.EventRequestSpecification;
import prime.prime.domain.role.Role;
import prime.prime.domain.season.service.SeasonService;
import prime.prime.domain.user.entity.User;
import prime.prime.domain.user.service.UserService;
import prime.prime.infrastructure.exception.EventRequestException;
import prime.prime.infrastructure.exception.NotFoundException;
import prime.prime.infrastructure.jobs.EmailSendingJob;
import prime.prime.infrastructure.security.ProgresoUserDetails;

import java.util.List;
import java.util.Map;

@Service
public class EventRequestServiceImpl implements EventRequestService {

    private final EventRequestRepository eventRequestRepository;
    private final EventRequestMapper eventRequestMapper;
    private final UserService userService;
    private final EmailSendingJob emailSendingJob;
    private final SeasonService seasonService;

    public EventRequestServiceImpl(EventRequestRepository eventRequestRepository,
        EventRequestMapper eventRequestMapper, UserService userService, EmailSendingJob emailSendingJob,
        SeasonService seasonService) {
        this.eventRequestRepository = eventRequestRepository;
        this.eventRequestMapper = eventRequestMapper;
        this.userService = userService;
        this.emailSendingJob = emailSendingJob;
        this.seasonService = seasonService;
    }

    private void scheduleMailSending(EventRequest eventRequest, User recipient, String subject,
        String comment, String template) {
        emailSendingJob.scheduleEmailJob(recipient.getAccount().getEmail(), subject, Map.of("fullName",
                recipient.getFullName(), "status", eventRequest.getStatus().name(), "title", eventRequest.getTitle(),
                "comment", comment, "template", template));
    }

    @Override
    public EventRequestReturnDto create(EventRequestCreateDto eventRequestDto,
        ProgresoUserDetails user) {
        User requester = userService.getEntityById(user.getUserId());

        EventRequest eventRequest = eventRequestMapper.toEventRequest(eventRequestDto);
        eventRequest.setStatus(EventRequestStatus.REQUESTED);
        eventRequest.setRequester(requester);
        eventRequest.setSeason(
            seasonService.findActiveSeason(requester, eventRequestDto.seasonId()));

        return eventRequestMapper.fromEventRequest(eventRequestRepository.save(eventRequest));
    }

    @Override
    public Page<EventRequestReturnDto> getAll(SearchEventRequestDto searchEventRequestDto,
                                              Pageable pageable, ProgresoUserDetails userDetails) {
        if (searchEventRequestDto == null) {
            throw new EventRequestException("SearchEventRequest cannot be null.");
        }

        var seasonIds = getSeasonIdsForUser(userDetails.getUserId());
        return eventRequestRepository
                .findAll(new EventRequestSpecification(searchEventRequestDto, seasonIds), pageable)
                .map(eventRequestMapper::fromEventRequest);
    }

    @Override
    public EventRequestReturnDto getById(Long id) {
        return eventRequestMapper.fromEventRequest(findById(id));
    }

    @Override
    public EventRequestReturnDto update(Long id, EventRequestUpdateDto eventRequestUpdateDto) {
        EventRequest currentEventRequest = findById(id);
        eventRequestMapper.update(currentEventRequest, eventRequestUpdateDto);

        return eventRequestMapper.fromEventRequest(
            eventRequestRepository.save(currentEventRequest));
    }

    @Override
    public void deleteEventRequest(Long id) {
        if (!eventRequestRepository.existsById(id)) {
            throw new NotFoundException(EventRequest.class.getSimpleName(), "id", id.toString());
        }

        eventRequestRepository.deleteById(id);
    }

    @Override
    public EventRequestReturnDto changeStatus(Long id,
        EventRequestStatusDto eventRequestStatusDto) {
        EventRequest currentEventRequest = findById(id);

        if (eventRequestStatusDto.status().isApproved() && currentEventRequest.getStatus()
            .isApproved()) {
            if (eventRequestStatusDto.assignee() == null) {
                throw new EventRequestException("You can't change the status of this event request "
                    + "from APPROVED to APPROVED");
            }
            assignEventRequest(currentEventRequest, eventRequestStatusDto.assignee());
            sendEmail(currentEventRequest, eventRequestStatusDto.comment());
            return eventRequestMapper.fromEventRequest(
                eventRequestRepository.save(currentEventRequest));
        } else {

            if (!currentEventRequest.getStatus().canBeChangedTo(eventRequestStatusDto.status())) {
                throw new EventRequestException(
                    "You can't change the status of this event request from "
                        + currentEventRequest.getStatus()
                        + " to "
                        + eventRequestStatusDto.status());
            }

            if (eventRequestStatusDto.assignee() != null && !eventRequestStatusDto.status()
                .isApproved()) {
                throw new EventRequestException(
                    "You can assign an event request only during approval!");
            }

            return switch (eventRequestStatusDto.status()) {
                case REJECTED ->
                    rejectEventRequest(currentEventRequest, eventRequestStatusDto.comment());
                case APPROVED ->
                    approveEventRequest(currentEventRequest, eventRequestStatusDto.comment(),
                        eventRequestStatusDto.assignee());
                case SCHEDULED -> scheduleEventRequest(currentEventRequest, "");
                default -> throw new EventRequestException("Invalid activity!");
            };
        }
    }

    private List<Long> getSeasonIdsForUser(Long userId) {
        User user = userService.getEntityById(userId);

        if (Role.ADMIN.equals(user.getAccount().getRole())) {
            return seasonService.getAllSeasonIds();
        }

        return seasonService.getSeasonIdsForUser(userId);
    }

    private EventRequestReturnDto approveEventRequest(EventRequest eventRequest, String comment,
        Long assigneeId) {
        eventRequest.setStatus(EventRequestStatus.APPROVED);

        if (assigneeId != null) {
            assignEventRequest(eventRequest, assigneeId);
        }

        EventRequest saved = eventRequestRepository.save(eventRequest);

        sendEmail(eventRequest, comment);

        return eventRequestMapper.fromEventRequest(saved);
    }

    private EventRequestReturnDto rejectEventRequest(EventRequest eventRequest, String comment) {
        eventRequest.setStatus(EventRequestStatus.REJECTED);
        EventRequest saved = eventRequestRepository.save(eventRequest);

        sendEmail(eventRequest, comment);

        return eventRequestMapper.fromEventRequest(saved);
    }

    private EventRequestReturnDto scheduleEventRequest(EventRequest eventRequest, String comment) {
        eventRequest.setStatus(EventRequestStatus.SCHEDULED);
        EventRequest saved = eventRequestRepository.save(eventRequest);

        sendEmail(eventRequest, comment);

        return eventRequestMapper.fromEventRequest(saved);
    }

    private void assignEventRequest(EventRequest eventRequest, Long assigneeId) {
        User assignee = userService.getEntityById(assigneeId);

        if (assignee.getAccount().getRole() != Role.MENTOR) {
            throw new EventRequestException("You can only assign an event request to a MENTOR!");
        }

        eventRequest.setAssignee(assignee);
    }

    private void sendEmail(EventRequest eventRequest, String comment) {
        User requester = eventRequest.getRequester();

        String subject =
                "Event request " + eventRequest.getStatus().name().toLowerCase() + ": " + eventRequest.getTitle();

        scheduleMailSending(eventRequest, requester, subject, comment, "event_request_email");

        if (eventRequest.getAssignee() != null) {
            User assignee = eventRequest.getAssignee();
            String assigneeSubject = "Event request assigned: " + eventRequest.getTitle();
            scheduleMailSending(eventRequest, assignee, assigneeSubject, comment, "assigned_event_request_email");
        }
    }

    private EventRequest findById(Long id) {
        return eventRequestRepository.findById(id).orElseThrow(
            () -> new NotFoundException(EventRequest.class.getSimpleName(), "id", id.toString()));
    }

}
