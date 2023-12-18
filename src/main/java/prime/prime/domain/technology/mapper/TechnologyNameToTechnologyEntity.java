package prime.prime.domain.technology.mapper;

import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.stereotype.Component;
import prime.prime.domain.technology.entity.Technology;
import prime.prime.domain.technology.service.TechnologyService;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class TechnologyNameToTechnologyEntity {
    private final TechnologyService technologyService;

    public TechnologyNameToTechnologyEntity(TechnologyService technologyService) {
        this.technologyService = technologyService;
    }

    @TechnologyMapping
    public Set<Technology> toTechnologyEntity(Set<String> technologies) {
        return technologies.stream()
                .map(technologyService::findByName)
                .collect(Collectors.toSet());
    }
}
