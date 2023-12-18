package prime.prime.domain.technology.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import prime.prime.domain.technology.entity.Technology;
import prime.prime.domain.technology.models.TechnologyCreateDto;
import prime.prime.domain.technology.models.TechnologyReturnDto;

import java.util.Optional;

public interface TechnologyService {
    TechnologyReturnDto create(TechnologyCreateDto technologyCreateDto);

    TechnologyReturnDto getById(Long id);

    Page<TechnologyReturnDto> getAll(Pageable pageable);

    TechnologyReturnDto update(Long id,TechnologyCreateDto technologyCreateDto);

    void delete(Long id);

    Technology findByName(String name);

}
