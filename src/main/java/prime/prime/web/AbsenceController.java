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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import prime.prime.domain.absence.models.*;
import prime.prime.domain.absence.service.AbsenceTrackingService;
import prime.prime.infrastructure.security.ProgresoUserDetails;
import prime.prime.infrastructure.utility.CustomPageDto;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/absences")
@Tag(description = "Resource for absence tracking endpoints", name = "AbsenceTracking Controller")
@SecurityRequirement(name = "Bearer Authentication")
public class AbsenceController {

    private final AbsenceTrackingService absenceTrackingService;

    public AbsenceController(AbsenceTrackingService absenceTrackingService) {
        this.absenceTrackingService = absenceTrackingService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MENTOR','ROLE_INTERN')")
    @Operation(summary = "Create new absence request", description = "ADMIN, MENTOR or INTERN role required")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(schema = @Schema(implementation =
            AbsenceRequestDto.class)))
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Absence request created successfully",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation =
                    AbsenceResponseDto.class))}), @ApiResponse(responseCode = "409", description = "Absence conflict "
            ,content = @Content)})
    public ResponseEntity<AbsenceResponseDto> create(@Valid @RequestBody AbsenceRequestDto absenceRequestDto,
                                                     @AuthenticationPrincipal ProgresoUserDetails user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(absenceTrackingService.createAbsenceRequest(absenceRequestDto, user));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ROLE_ADMIN') or (hasRole('ROLE_MENTOR') and @authorizationService.isAllowedToChangeAbsenceRequest(#id))")
    @Operation(summary = "Change status of absence request by id", description =
            "Admin has permission to change the absence request status or mentor who is the owner of the season")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(schema = @Schema(implementation =
            AbsenceRequestStatusDto.class)))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully changed status of absence request",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = AbsenceResponseDto.class))}),
            @ApiResponse(responseCode = "404", description = "Absence request with this id does not exist",
                    content = @Content),
            @ApiResponse(responseCode = "409", description = "Absence conflict",
                    content = @Content)
    })
    public ResponseEntity<AbsenceResponseDto> changeStatus(@PathVariable Long id,
                                                              @Valid @RequestBody AbsenceRequestStatusDto absenceRequestStatusDto) {
        return ResponseEntity.ok(absenceTrackingService.changeStatus(id, absenceRequestStatusDto));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or ((hasRole('ROLE_INTERN') or hasRole('ROLE_MENTOR')) and " +
            "@authorizationService.canAccessAbsenceRequest(#id))" )
    @Operation(summary = "Get an absence request by id", description = "ADMIN, MENTOR or INTERN role required")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully fetched absence request",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = AbsenceResponseDto.class))}),
            @ApiResponse(responseCode = "404", description = "Absence request with this id does not exist",
                    content = @Content)})
    public ResponseEntity<AbsenceResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(absenceTrackingService.getById(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MENTOR','ROLE_INTERN')")
    @Operation(summary = "Get all absence requests", description = "ADMIN, MENTOR or INTERN role required")
    @Parameter(name = "searchAbsenceRequestDto", hidden = true)
    @Parameter(name = "pageable", hidden = true)
    @Parameter(name = "user", hidden = true)
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
    @Parameter(in = ParameterIn.QUERY,
            description = "Filter by absenceType",
            name = "absenceType",
            array = @ArraySchema(schema = @Schema(type = "string")))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully fetched absence requests",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = AbsenceResponseDto.class))})})
    public ResponseEntity<CustomPageDto<AbsenceResponseDto>> getAll(SearchAbsenceRequestDto searchAbsenceRequestDto,
                                                           Pageable pageable, @AuthenticationPrincipal ProgresoUserDetails user) {
        return ResponseEntity.ok(new CustomPageDto<>(absenceTrackingService.getAll(searchAbsenceRequestDto,pageable, user)));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or " +
            "(hasRole('ROLE_INTERN') and @authorizationService.isOwnerOfAbsenceRequest(#id)) or " +
            "(hasRole('ROLE_MENTOR') and " +
            "(@authorizationService.isAllowedToChangeAbsenceRequest(#id) or @authorizationService.isOwnerOfAbsenceRequest(#id)))")
    @Operation(summary = "Update an absence request by id", description =
            "ADMIN, INTERN role required or MENTOR who is owner of the specified season, INTERN and MENTOR can update only his/hers request")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(schema = @Schema(implementation = AbsenceRequestUpdateDto.class)))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully updated absence request",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = AbsenceResponseDto.class))}),
            @ApiResponse(responseCode = "400", description = "Input validation failed",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Absence request with this id does not exist",
                    content = @Content)})
    public ResponseEntity<AbsenceResponseDto> update(@PathVariable Long id,
                                                     @Valid @RequestBody AbsenceRequestUpdateDto absenceRequestUpdateDto,
                                                     @AuthenticationPrincipal ProgresoUserDetails user) {
        return ResponseEntity.ok(absenceTrackingService.update(id, absenceRequestUpdateDto,user));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ROLE_ADMIN') or " +
            "(hasRole('ROLE_INTERN') and @authorizationService.isOwnerOfAbsenceRequest(#id)) or " +
            "(hasRole('ROLE_MENTOR') and " +
            "(@authorizationService.isAllowedToChangeAbsenceRequest(#id) or @authorizationService.isOwnerOfAbsenceRequest(#id)))")
    @Operation(summary = "Delete absence request by id", description =
            "ADMIN, INTERN role required or MENTOR who is owner of the specified season, INTERN and MENTOR can delete only his/hers request")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Successfully deleted absence request"),
            @ApiResponse(responseCode = "404", description = "Absence request with this id does not exist",
                    content = @Content)})
    public void delete(@PathVariable Long id) {
        absenceTrackingService.delete(id);
    }

    @GetMapping("/calendar-absences")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MENTOR', 'ROLE_INTERN')")
    @Operation(summary = "Show all absence requests by date range, except REJECTED ones - " +
            "displayName holds {requester name} - {absence type}", description =
            "ADMIN, MENTOR or INTERN role required")
    @Parameter(name = "calendarAbsenceRequestDto", hidden = true)
    @Parameter(name = "user", hidden = true)
    @Parameter(in = ParameterIn.QUERY,
            description = "Filter by status. Accepted values REQUESTED or APPROVED.",
            name = "status",
            array = @ArraySchema(schema = @Schema(type = "string")))
    @Parameter(in = ParameterIn.QUERY,
            description = "Filter by absenceType",
            name = "absenceType",
            array = @ArraySchema(schema = @Schema(type = "string")))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully fetched calendar-absence requests",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = CalendarAbsenceResponseDto.class))}),
            @ApiResponse(responseCode = "400", description = "Input validation failed",
                    content = @Content)})
    public ResponseEntity<List<CalendarAbsenceResponseDto>> getAbsencesByDate(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                                                              SearchCalendarAbsenceRequestDto calendarAbsenceRequestDto,
                                                                              @AuthenticationPrincipal ProgresoUserDetails user) {
        return ResponseEntity.ok(absenceTrackingService.getAbsencesByDate(startDate, endDate, calendarAbsenceRequestDto, user));
    }
}
