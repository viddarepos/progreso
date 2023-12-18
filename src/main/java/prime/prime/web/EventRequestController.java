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
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import prime.prime.domain.eventrequest.models.*;
import prime.prime.domain.eventrequest.service.EventRequestService;
import prime.prime.infrastructure.security.ProgresoUserDetails;
import prime.prime.infrastructure.utility.CustomPageDto;


@RestController
@RequestMapping("/events/request")
@Tag(description = "Resource for event request endpoints", name = "EventRequest Controller")
@SecurityRequirement(name = "Bearer Authentication")
public class EventRequestController {

    private final EventRequestService eventRequestService;

    public EventRequestController(EventRequestService eventRequestService) {
        this.eventRequestService = eventRequestService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_INTERN') || (hasRole('ROLE_MENTOR') && "
        + "@authorizationService.isOwnerOfSeason(#eventRequestDto.seasonId()))")
    @Operation(summary = "Create new event request", description = "ADMIN, INTERN role required "
        + "or MENTOR who is owner of the specified season")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        content = @Content(schema = @Schema(implementation = EventRequestCreateDto.class)))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Event request created successfully",
            content = {@Content(mediaType = "application/json",
                schema = @Schema(implementation = EventRequestReturnDto.class))}),
        @ApiResponse(responseCode = "400", description = "Input validation failed",
            content = @Content)})
    public ResponseEntity<EventRequestReturnDto> create(
        @Valid @RequestBody EventRequestCreateDto eventRequestDto,
        @AuthenticationPrincipal ProgresoUserDetails user) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(eventRequestService.create(eventRequestDto, user));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MENTOR','ROLE_INTERN')")
    @Operation(summary = "Get all requested events", description = "ADMIN,MENTOR or INTERN role required. " +
            "MENTORS and INTERNS can only access event requests from the season they are a part of.")
    @Parameter(name = "user", hidden = true)
    @Parameter(name = "searchEventRequestDto", hidden = true)
    @Parameter(name = "pageable", hidden = true)
    @Parameter(in = ParameterIn.QUERY,
        description = "Zero-based page index (0..N)",
        name = "page",
        content = @Content(schema = @Schema(type = "integer", defaultValue = "0")))
    @Parameter(in = ParameterIn.QUERY,
        description = "The size of the page to be returned",
        name = "size",
        content = @Content(schema = @Schema(type = "integer", defaultValue = "20")))
    @Parameter(in = ParameterIn.QUERY,
        description = "Sorting criteria in the format: property,(asc|desc). "
            + "Default sort order is ascending. " + "Multiple sort criteria are supported.",
        name = "sort",
        array = @ArraySchema(schema = @Schema(type = "string")), allowReserved = true)
    @Parameter(in = ParameterIn.QUERY,
        description = "Filter by status. Accepted values REQUESTED, APPROVED or REJECTED.",
        name = "status",
        array = @ArraySchema(schema = @Schema(type = "string")))
    @Parameter(in = ParameterIn.QUERY,
        description = "Filter by season.",
        name = "seasonId",
        array = @ArraySchema(schema = @Schema(type = "number")))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully fetched requested events",
            content = {@Content(mediaType = "application/json",
                schema = @Schema(implementation = EventRequestReturnDto.class))})})
    public ResponseEntity<CustomPageDto<EventRequestReturnDto>> getAll(
        SearchEventRequestDto searchEventRequestDto, Pageable pageable,
        @AuthenticationPrincipal ProgresoUserDetails user) {
        return ResponseEntity.ok(new CustomPageDto<>(eventRequestService.getAll(searchEventRequestDto, pageable, user)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or ((hasRole('ROLE_MENTOR') or hasRole('ROLE_INTERN')) " +
            "and @authorizationService.canAccessEventRequest(#id))")
    @Operation(summary = "Get an event request by id", description = "ADMIN,MENTOR or INTERN role required. " +
            "MENTORS and INTERNS can only access event requests from the season they are a part of.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully fetched requested event",
            content = {@Content(mediaType = "application/json",
                schema = @Schema(implementation = EventRequestReturnDto.class))}),
        @ApiResponse(responseCode = "404", description = "A event request with this id does not exist",
            content = @Content)})
    public ResponseEntity<EventRequestReturnDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(eventRequestService.getById(id));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or (hasRole('ROLE_INTERN') and @authorizationService.isOwnerOfRequest(#id)) "
        + "or (hasRole('ROLE_MENTOR') and  @authorizationService.isAllowedToChangeEventRequest(#id))" )
    @Operation(summary = "Update an event request by id", description =
        "ADMIN, INTERN role required or MENTOR who is owner of the specified season" +
            " INTERN can update only his/her request")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        content = @Content(schema = @Schema(implementation = EventRequestUpdateDto.class)))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully updated event request",
            content = {@Content(mediaType = "application/json",
                schema = @Schema(implementation = EventRequestReturnDto.class))}),
        @ApiResponse(responseCode = "400", description = "Input validation failed",
            content = @Content),
        @ApiResponse(responseCode = "404", description = "Event request with this id does not exist",
            content = @Content)})
    public ResponseEntity<EventRequestReturnDto> update(@PathVariable Long id,
        @Valid @RequestBody EventRequestUpdateDto eventRequestUpdateDto) {
        return ResponseEntity.ok(eventRequestService.update(id, eventRequestUpdateDto));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ROLE_ADMIN') or (hasRole('ROLE_INTERN') and @authorizationService.isOwnerOfRequest(#id))"
        + "or (hasRole('ROLE_MENTOR') and @authorizationService.isAllowedToChangeEventRequest(#id))")
    @Operation(summary = "Delete event request by id", description =
        "ADMIN, INTERN role required or MENTOR who is owner of the specified season" +
            " INTERN can delete only his/her request")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Successfully deleted event request"),
        @ApiResponse(responseCode = "404", description = "Event request with this id does not exist",
            content = @Content)})
    public void deleteEventRequest(@PathVariable Long id) {
        eventRequestService.deleteEventRequest(id);
    }

    @PostMapping("/{id}/status")
    @PreAuthorize("hasRole('ROLE_ADMIN') or (hasRole('ROLE_MENTOR') and"
        + " @authorizationService.isAssignedToRequest(#id, #eventRequestStatusDto))")
    @Operation(summary = "Change status of event request by id", description =
        "Only the ADMIN has permission to change the " +
            "event request status and MENTOR on an event request he is assigned to")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully changed status of event request",
            content = {@Content(mediaType = "application/json",
                schema = @Schema(implementation = EventRequestReturnDto.class))}),
        @ApiResponse(responseCode = "404", description = "Event request with this id does not exist",
            content = @Content),
        @ApiResponse(responseCode = "409", description = "Event request with this id doesn't have status REQUESTED",
            content = @Content)
    })
    public ResponseEntity<EventRequestReturnDto> changeStatus(@PathVariable Long id,
        @Valid @RequestBody EventRequestStatusDto eventRequestStatusDto) {
        return ResponseEntity.ok(eventRequestService.changeStatus(id, eventRequestStatusDto));
    }
}
