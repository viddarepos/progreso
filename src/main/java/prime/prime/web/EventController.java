package prime.prime.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import prime.prime.domain.event.models.*;
import prime.prime.domain.event.service.EventService;
import prime.prime.infrastructure.security.ProgresoUserDetails;
import prime.prime.infrastructure.utility.CustomPageDto;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/events")
@Tag(description = "Resource for event endpoints",
    name = "Event Controller")
@SecurityRequirement(name = "Bearer Authentication")
public class EventController {

    private final EventService eventService;

    @Autowired
    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MENTOR')")
    @Operation(summary = "Create new event", description = "ADMIN or MENTOR role required")
    @Parameter(name = "userDetails", hidden = true)
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(schema = @Schema(implementation = EventRequestDto.class)), description = "Duration is in minutes")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Created event successfully",
            content = {@Content(mediaType = "application/json",
                schema = @Schema(implementation = EventResponseWithAttendeesDto.class))}),
        @ApiResponse(responseCode = "400", description = "Input validation failed",
            content = @Content)})
    public ResponseEntity<EventResponseWithAttendeesDto> create(
        @Valid @RequestBody EventRequestDto eventRequestDto,
        @AuthenticationPrincipal ProgresoUserDetails userDetails) {
        return new ResponseEntity<>(eventService.create(eventRequestDto, userDetails),
            HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MENTOR','ROLE_INTERN')")
    @Operation(summary = "Get event by id", description = "ADMIN,MENTOR or INTERN role required")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully fetched event",
            content = {@Content(mediaType = "application/json",
                schema = @Schema(implementation = EventResponseWithAttendeesDto.class))}),
        @ApiResponse(responseCode = "404", description = "Event with this id does not exists",
            content = @Content)})
    public ResponseEntity<EventResponseWithAttendeesDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.getById(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MENTOR','ROLE_INTERN')")
    @Operation(summary = "Get all events", description = "ADMIN,MENTOR or INTERN role required")
    @Parameter(name = "searchEventDto", hidden = true)
    @Parameter(name = "user", hidden = true)
    @Parameter(name = "pageable", hidden = true)
    @Parameter(in = ParameterIn.QUERY
        , description = "Zero-based page index (0..N)"
        , name = "page"
        , content = @Content(schema = @Schema(type = "integer", defaultValue = "0")))
    @Parameter(in = ParameterIn.QUERY
        , description = "The size of the page to be returned"
        , name = "size"
        , content = @Content(schema = @Schema(type = "integer", defaultValue = "20")))
    @Parameter(in = ParameterIn.QUERY
        , description = "Sorting criteria in the format: property,(asc|desc). "
        + "Default sort order is ascending. " + "Multiple sort criteria are supported."
        , name = "sort"
        , array = @ArraySchema(schema = @Schema(type = "string")), allowReserved = true)
    @Parameter(in = ParameterIn.QUERY,
        description = "Filter by season.",
        name = "seasonId",
        array = @ArraySchema(schema = @Schema(type = "number")))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully fetched events",
            content = {@Content(mediaType = "application/json",
                schema = @Schema(implementation = EventResponseWithAttendeesDto.class))})})
    public ResponseEntity<CustomPageDto<EventResponseWithAttendeesDto>> getPage(
        SearchEventDto searchEventDto,
        @PageableDefault Pageable pageable, @AuthenticationPrincipal ProgresoUserDetails user) {
        return ResponseEntity.ok(new CustomPageDto<>(eventService.getAll(searchEventDto, pageable, user)));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MENTOR') and @authorizationService.isAdminOrOwner(#id)")
    @Operation(summary = "Update event data by id", description = "ADMIN or MENTOR role required")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully updated event",
            content = {@Content(mediaType = "application/json",
                schema = @Schema(implementation = EventResponseWithAttendeesDto.class))}),
        @ApiResponse(responseCode = "404", description = "Event with this id does not exists",
            content = @Content),
        @ApiResponse(responseCode = "400", description = "Input validation failed",
            content = @Content)})
    public ResponseEntity<EventResponseWithAttendeesDto> update(@PathVariable Long id,
        @Valid @RequestBody EventUpdateDto eventUpdateDto,
        @AuthenticationPrincipal ProgresoUserDetails userDetails) {
        return ResponseEntity.ok(eventService.update(id, eventUpdateDto, userDetails));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MENTOR') and @authorizationService.isAdminOrOwner(#id)")
    @Operation(summary = "Delete event by id", description = "ADMIN or MENTOR role required")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Successfully deleted event"),
        @ApiResponse(responseCode = "404", description = "Event with this id does not exists",
            content = @Content)})
    public void delete(@PathVariable Long id) {
        eventService.delete(id);
    }

    @GetMapping("/calendar-events")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MENTOR','ROLE_INTERN')")
    @Operation(summary = "Get events by date range", description = "ADMIN,MENTOR or INTERN role required")
    @Parameter(name = "userDetails", hidden = true)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully fetched events",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CalendarEventResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Input validation failed",
                    content = @Content)})
    public ResponseEntity<List<CalendarEventResponseDto>> getEventsByDate
            (@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
             @AuthenticationPrincipal ProgresoUserDetails userDetails) {
        return ResponseEntity.ok(eventService.getEventsByDate(startDate, endDate, userDetails));
    }
}
