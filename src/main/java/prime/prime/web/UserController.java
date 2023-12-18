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
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import prime.prime.domain.account.models.AccountEmailDto;
import prime.prime.domain.account.models.ChangePasswordDto;
import prime.prime.domain.mentorship.models.AssignMentorToUserDto;
import prime.prime.domain.mentorship.models.AssignMentorToUserResponseDto;
import prime.prime.domain.mentorship.service.MentorshipService;
import prime.prime.domain.user.models.SearchUserDto;
import prime.prime.domain.user.models.UserCreateDto;
import prime.prime.domain.user.models.UserReturnDto;
import prime.prime.domain.user.models.UserUpdateDto;
import prime.prime.domain.user.service.UserService;
import prime.prime.infrastructure.image.validator.ValidImage;
import prime.prime.infrastructure.security.ProgresoUserDetails;
import prime.prime.infrastructure.utility.CustomPageDto;

import java.io.IOException;

@RestController
@RequestMapping("/users")
@Tag(description = "Resource for user endpoints",
    name = "User Controller")
@SecurityRequirement(name = "Bearer Authentication")
@Validated
public class UserController {

    private final UserService userService;
    private final MentorshipService mentorshipService;

    public UserController(UserService userService,MentorshipService mentorshipService) {
        this.userService = userService;
        this.mentorshipService = mentorshipService;
    }

    @PostMapping
    @Operation(summary = "Create new user", description = "ADMIN role required")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Created user successfully",
            content = {@Content(mediaType = "application/json",
                schema = @Schema(implementation = UserCreateDto.class))}),
        @ApiResponse(responseCode = "409", description = "User with this email already exists",
            content = @Content),
        @ApiResponse(responseCode = "400", description = "Input validation failed",
            content = @Content)})

    @PreAuthorize("hasRole('ROLE_ADMIN') and @authorizationService.isAllowedToCreate(#userCreateDto)")
    public ResponseEntity<UserReturnDto> create(@Valid @RequestBody UserCreateDto userCreateDto) {
        return ResponseEntity.ok(userService.create(userCreateDto));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by id", description = "ADMIN,MENTOR or INTERN role required")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully fetched user",
            content = {@Content(mediaType = "application/json",
                schema = @Schema(implementation = UserReturnDto.class))}),
        @ApiResponse(responseCode = "404", description = "User with this id doesn't exists",
            content = @Content)})
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MENTOR','ROLE_INTERN')")
    public ResponseEntity<UserReturnDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MENTOR','ROLE_INTERN')")
    @Operation(summary = "Get all users", description = "ADMIN,MENTOR or INTERN role required. Only admin can group by season, technology or location. Group fields can accept multiple values.")
    @Parameter(name = "searchUserDto", hidden = true)
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
    @Parameter(in = ParameterIn.QUERY
        , description = "Filter by fullName."
        , name = "fullName"
        , array = @ArraySchema(schema = @Schema(type = "string")))
    @Parameter(in = ParameterIn.QUERY
        , description = "Filter by location."
        , name = "location"
        , array = @ArraySchema(schema = @Schema(type = "string")))
    @Parameter(in = ParameterIn.QUERY
        , description = "Filter by role.Accepted values INTERN,MENTOR or ADMIN."
        , name = "role"
        , array = @ArraySchema(schema = @Schema(type = "string")))
    @Parameter(in = ParameterIn.QUERY
        , description = "Filter by status.Accepted values INVITED, ACTIVE or ARCHIVED."
        , name = "status"
        , array = @ArraySchema(schema = @Schema(type = "string")))
    @Parameter(in = ParameterIn.QUERY
            , description = "Filter by season."
            , name = "season"
            , array = @ArraySchema(schema = @Schema(type = "string")))
    @Parameter(in = ParameterIn.QUERY
            , description = "Filter by technology."
            , name = "technology"
            , array = @ArraySchema(schema = @Schema(type = "string")))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully fetched users",
            content = {@Content(mediaType = "application/json",
                schema = @Schema(implementation = UserReturnDto.class))}),
        @ApiResponse(responseCode = "400", description = "Input validation failed",
            content = @Content)})
    public ResponseEntity<CustomPageDto<UserReturnDto>> getPage(@Valid SearchUserDto searchUserDto,
                                                                Pageable pageable,@AuthenticationPrincipal ProgresoUserDetails userDetails) {
        return ResponseEntity.ok(new CustomPageDto<>(userService.getAll(searchUserDto, pageable,userDetails)));
    }

    @PatchMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MENTOR','ROLE_INTERN') and " +
        "@authorizationService.isAllowedToUpdate(#id, #updateDto)")
    @Operation(summary = "Update user information by id", description = "User can update only his/her info. Only ADMIN can change roles!")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully updated user",
            content = {@Content(mediaType = "application/json",
                schema = @Schema(implementation = UserReturnDto.class))}),
        @ApiResponse(responseCode = "404", description = "User with this id doesn't exists",
            content = @Content),
        @ApiResponse(responseCode = "409", description = "User with this email already exists",
            content = @Content),
        @ApiResponse(responseCode = "400", description = "Input validation failed",
            content = @Content)})
    public ResponseEntity<UserReturnDto> update(@PathVariable Long id,
        @Valid @RequestPart("user") UserUpdateDto updateDto, @ValidImage MultipartFile file)
        throws IOException {
        return ResponseEntity.ok(userService.update(id, updateDto, file));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("(hasRole('ROLE_ADMIN') and @authorizationService.isAllowedToDelete(#id))"
        + "or (hasRole('ROLE_MENTOR') and @authorizationService.areFromSameSeason(#id))")
    @Operation(summary = "Delete user by id", description = "ADMIN role required")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Successfully deleted user", content = @Content),
        @ApiResponse(responseCode = "404", description = "User with this id doesn't exist",
            content = @Content)})
    public void delete(@PathVariable Long id) throws IOException {
        userService.delete(id);
    }

    @PatchMapping("/{id}/password")
    @Operation(summary = "Change password on currently logged in user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Successfully changed password", content = @Content),
        @ApiResponse(responseCode = "400", description = "New password validation failed",
            content = @Content),
        @ApiResponse(responseCode = "409", description = "Incorrect old password",
            content = @Content)})
    public ResponseEntity<AccountEmailDto> changePassword(@PathVariable Long id,
        @Valid @RequestBody ChangePasswordDto changePasswordDto,
        HttpServletRequest httpServletRequest) {
        return ResponseEntity.ok(
            userService.changePassword(id, changePasswordDto, httpServletRequest));

    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}/image")
    @PreAuthorize("@authorizationService.isAllowedToDeleteUserImage(#id)")
    public void deleteUserImage(@PathVariable Long id) throws IOException {
        userService.deleteUserImage(id);
    }

    @PatchMapping("/{id}/archive")
    @PreAuthorize("hasRole('ROLE_ADMIN') or (hasRole('ROLE_MENTOR') && @authorizationService.areFromSameSeason(#id))")
    @Operation(summary = "Archive user by id", description = "ADMIN role required")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Successfully archived user", content = @Content),
        @ApiResponse(responseCode = "404", description = "User with the given id does not exist or is already archived",
            content = @Content)})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<AccountEmailDto> archive(@PathVariable Long id) {
        return ResponseEntity.ok(userService.archive(id));
    }

    @PostMapping("/{id}/assign-mentor")
    @Operation(summary = "Assign mentor to an intern", description = "Mentor can be assigned to user for a part of a " +
            "season. A mentor can be assigned to multiple interns at the same time for the same and for different " +
            "internships. An intern can have multiple mentors.Only admin and the owner of the season for which a " +
            "mentor is being assigned are allowed to access this endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Mentor assigned successfully",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = AssignMentorToUserResponseDto.class))}),
            @ApiResponse(responseCode = "404", description = "Entity not found",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Input validation failed",
                    content = @Content)})
    @PreAuthorize("hasRole('ROLE_ADMIN') or (hasRole('ROLE_MENTOR') && @authorizationService.isOwnerOfSeason(#assignMentorToUserDto.seasonId()))")
    public ResponseEntity<AssignMentorToUserResponseDto> assignMentor(@PathVariable Long id, @Valid @RequestBody AssignMentorToUserDto assignMentorToUserDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mentorshipService.assignMentor(id,assignMentorToUserDto));
    }
}
