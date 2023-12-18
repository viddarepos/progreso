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
import org.springframework.web.bind.annotation.*;
import prime.prime.domain.season.models.SearchSeasonDto;
import prime.prime.domain.season.models.SeasonCreateDto;
import prime.prime.domain.season.models.SeasonResponseDto;
import prime.prime.domain.season.models.SeasonUpdateDto;
import prime.prime.domain.season.service.SeasonService;
import prime.prime.infrastructure.security.AuthorizationService;
import prime.prime.infrastructure.security.ProgresoUserDetails;
import prime.prime.infrastructure.utility.CustomPageDto;

@RestController
@RequestMapping("/seasons")
@PreAuthorize("hasRole('ROLE_ADMIN')")
@Tag(description = "Resource for season endpoints",
    name = "Season Controller")
@SecurityRequirement(name = "Bearer Authentication")
public class SeasonController {

    private final SeasonService seasonService;
    private final AuthorizationService authorizationService;

    public SeasonController(SeasonService seasonService,
        AuthorizationService authorizationService) {
        this.seasonService = seasonService;
        this.authorizationService = authorizationService;
    }

    @PostMapping
    @Operation(summary = "Create new season", description = "ADMIN role required")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        content = @Content(schema = @Schema(implementation = SeasonCreateDto.class)))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Season created successfully",
            content = {@Content(mediaType = "application/json",
                schema = @Schema(implementation = SeasonResponseDto.class))}),
        @ApiResponse(responseCode = "400", description = "Input validation failed",
            content = @Content)})
    public ResponseEntity<SeasonResponseDto> create(
        @RequestBody @Valid SeasonCreateDto seasonCreateDto) {
        return ResponseEntity.ok(seasonService.create(seasonCreateDto));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MENTOR', 'ROLE_INTERN')")
    @Operation(summary = "Get all seasons", description = "ADMIN/MENTOR/INTERN role required." +
            " Only admin can filter and group by mentor")
    @Parameter(name = "pageable", hidden = true)
    @Parameter(name = "searchDto", hidden = true)
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
            description = "Filter and group by mentor.",
            name = "mentorId",
            array = @ArraySchema(schema = @Schema(type = "number")))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully fetched seasons",
            content = {@Content(mediaType = "application/json",
                schema = @Schema(implementation = SeasonResponseDto.class))})})
    public ResponseEntity<CustomPageDto<SeasonResponseDto>> getAll(SearchSeasonDto searchDto, Pageable pageable) {
        ProgresoUserDetails currentUser = authorizationService.getCurrentUser();

        return ResponseEntity.ok(new CustomPageDto<>(seasonService.getAll(pageable,currentUser, searchDto)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') || (hasAnyRole('ROLE_MENTOR', 'ROLE_INTERN') && @authorizationService.isAssignedToSeason(#id))")
    @Operation(summary = "Get a season by id", description = "ADMIN role required or MENTOR/INTERN assigned to the specified season")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully fetched the season",
            content = {@Content(mediaType = "application/json",
                schema = @Schema(implementation = SeasonResponseDto.class))}),
        @ApiResponse(responseCode = "404", description = "A season with this id does not exist",
            content = @Content)})
    public ResponseEntity<SeasonResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(seasonService.getById(id));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') || (hasAnyRole('ROLE_MENTOR') && @authorizationService.isOwnerOfSeason(#id))")
    @Operation(summary = "Update a season by id", description = "ADMIN role required or MENTOR who is owner of the specified season")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        content = @Content(schema = @Schema(implementation = SeasonUpdateDto.class)))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully updated the season",
            content = {@Content(mediaType = "application/json",
                schema = @Schema(implementation = SeasonResponseDto.class))}),
        @ApiResponse(responseCode = "400", description = "Input validation failed",
            content = @Content),
        @ApiResponse(responseCode = "404", description = "A season with this id does not exist",
            content = @Content)})
    public ResponseEntity<SeasonResponseDto> update(@PathVariable Long id,
        @RequestBody @Valid SeasonUpdateDto seasonUpdateDto) {
        return ResponseEntity.ok(seasonService.update(id, seasonUpdateDto));
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ROLE_ADMIN') || (hasAnyRole('ROLE_MENTOR') && @authorizationService.isOwnerOfSeason(#id))")
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a season by id", description = "ADMIN role required or MENTOR who is owner of the specified season")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Successfully deleted the season"),
        @ApiResponse(responseCode = "404", description = "A season with this id does not exist",
            content = @Content)})
    public void delete(@PathVariable Long id) {
        seasonService.delete(id);
    }
}
