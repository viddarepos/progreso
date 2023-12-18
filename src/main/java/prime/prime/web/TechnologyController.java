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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import prime.prime.domain.technology.models.TechnologyCreateDto;
import prime.prime.domain.technology.models.TechnologyReturnDto;
import prime.prime.domain.technology.service.TechnologyService;
import prime.prime.domain.user.models.UserReturnDto;
import prime.prime.infrastructure.utility.CustomPageDto;

@RestController
@RequestMapping("/technologies")
@Tag(description = "Resource for technology endpoints",
    name = "Technology Controller")
@SecurityRequirement(name = "Bearer Authentication")
public class TechnologyController {

    private final TechnologyService technologyService;

    @Autowired
    public TechnologyController(TechnologyService technologyService) {
        this.technologyService = technologyService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Create new technology", description = "ADMIN role required")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully created technology",
            content = {@Content(mediaType = "application/json",
                schema = @Schema(implementation = TechnologyReturnDto.class))}),
        @ApiResponse(responseCode = "400", description = "Input validation failed",
            content = @Content)})
    public ResponseEntity<TechnologyReturnDto> create(
        @Valid @RequestBody TechnologyCreateDto technologyCreateDto) {
        return ResponseEntity.ok(technologyService.create(technologyCreateDto));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Get technology by id", description = "ADMIN role required")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully fetched technology",
            content = {@Content(mediaType = "application/json",
                schema = @Schema(implementation = TechnologyReturnDto.class))}),
        @ApiResponse(responseCode = "404", description = "Technology with this id doesn't exist",
            content = @Content)})
    public ResponseEntity<TechnologyReturnDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(technologyService.getById(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MENTOR','ROLE_INTERN')")
    @Operation(summary = "Get all technologies", description = "ADMIN,MENTOR or INTERN role required")
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
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully fetched technologies",
            content = {@Content(mediaType = "application/json",
                schema = @Schema(implementation = TechnologyReturnDto.class))})})
    public ResponseEntity<CustomPageDto<TechnologyReturnDto>> getPage(Pageable pageable) {
        return ResponseEntity.ok(new CustomPageDto<>(technologyService.getAll(pageable)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Update technology information by id", description = "ADMIN role required")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully updated technology",
            content = {@Content(mediaType = "application/json",
                schema = @Schema(implementation = UserReturnDto.class))}),
        @ApiResponse(responseCode = "404", description = "Technology with this id doesn't exists",
            content = @Content),
        @ApiResponse(responseCode = "400", description = "Input validation failed",
            content = @Content)})
    public ResponseEntity<TechnologyReturnDto> update(@PathVariable Long id,
        @Valid @RequestBody TechnologyCreateDto technologyCreateDto) {
        return ResponseEntity.ok(technologyService.update(id, technologyCreateDto));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Delete technology by id", description = "ADMIN role required")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Successfully deleted technology"),
        @ApiResponse(responseCode = "404", description = "Technology with this id doesn't exist",
            content = @Content)})
    public void delete(@PathVariable Long id) {
        technologyService.delete(id);
    }
}
