package prime.prime.domain.season.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import prime.prime.domain.season.entity.Season;
import prime.prime.domain.season.models.SearchSeasonDto;
import prime.prime.domain.season.models.SeasonCreateDto;
import prime.prime.domain.season.models.SeasonResponseDto;
import prime.prime.domain.season.models.SeasonUpdateDto;
import prime.prime.domain.user.entity.User;
import prime.prime.infrastructure.security.ProgresoUserDetails;

import java.util.List;

public interface SeasonService {

    SeasonResponseDto create(SeasonCreateDto seasonCreateDto);

    SeasonResponseDto getById(Long id);

    Page<SeasonResponseDto> getAll(Pageable pageable, ProgresoUserDetails currentUser, SearchSeasonDto searchSeasonDto);

    SeasonResponseDto update(Long id, SeasonUpdateDto seasonUpdateDto);

    void delete(Long id);

    Season findById(Long id);

    Season findActiveSeason(User requester, Long seasonId);

    boolean existsById(Long id);

    List<Long> getAllSeasonIds();

    List<Long> getSeasonIdsForUser(Long userId);
}
