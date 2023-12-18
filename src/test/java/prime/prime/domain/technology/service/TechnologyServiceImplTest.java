package prime.prime.domain.technology.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import prime.prime.domain.technology.entity.Technology;
import prime.prime.domain.technology.mapper.TechnologyMapper;
import prime.prime.domain.technology.models.TechnologyCreateDto;
import prime.prime.domain.technology.models.TechnologyReturnDto;
import prime.prime.domain.technology.repository.TechnologyRepository;
import prime.prime.infrastructure.exception.DuplicateException;
import prime.prime.infrastructure.exception.NotFoundException;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TechnologyServiceImplTest {

    @InjectMocks
    private TechnologyServiceImpl technologyService;
    @Mock
    private TechnologyRepository technologyRepository;
    @Mock
    private TechnologyMapper technologyMapper;
    private static Technology technology;
    private static TechnologyCreateDto technologyCreateDto;
    private static TechnologyReturnDto technologyReturnDto;

    @BeforeAll
    static void setUp() {
        technology = new Technology(1L, "Java");
        technologyCreateDto = new TechnologyCreateDto("Java");
        technologyReturnDto = new TechnologyReturnDto(1L, "Java");
    }

    @Test
    void createTechnology_ValidName_Successful() {
        when(technologyMapper.technologyToReturnDto(technology)).thenReturn(technologyReturnDto);
        when(technologyMapper.createDtoToTechnology(technologyCreateDto)).thenReturn(technology);
        when(technologyRepository.save(technology)).thenReturn(technology);

        TechnologyReturnDto createdTechnology = technologyService.create(technologyCreateDto);

        assertThat(createdTechnology.id()).isEqualTo(technologyReturnDto.id());
        assertThat(createdTechnology.name()).isEqualTo(technologyReturnDto.name());
    }

    @Test
    void createTechnology_NameAlreadyExists_ThrowsDuplicateException() {
        when(technologyRepository.existsByName(technologyCreateDto.name())).thenReturn(true);

        assertThrows(DuplicateException.class, () -> technologyService.create(technologyCreateDto));
    }

    @Test
    void getById_WithExistingId_Successful() {
        when(technologyRepository.findById(technology.getId())).thenReturn(
            Optional.ofNullable(technology));
        when(technologyMapper.technologyToReturnDto(technology)).thenReturn(technologyReturnDto);

        TechnologyReturnDto savedTechnology = technologyService.getById(technology.getId());

        assertThat(savedTechnology.name()).isEqualTo(technologyReturnDto.name());
        assertThat(savedTechnology.id()).isEqualTo(technologyReturnDto.id());
    }

    @Test
    void getById_InvalidId_ThrowsNotFoundException() {
        assertThrows(NotFoundException.class, () -> technologyService.getById(0L));
    }

    @Test
    void getAll_Valid_Successful() {
        List<Technology> list = List.of(technology);
        Pageable pageable = PageRequest.of(0, 20);
        Page<Technology> technologyReturnPage = new PageImpl<>(list, pageable, list.size());

        when(technologyRepository.findAll(pageable)).thenReturn(technologyReturnPage);
        when(technologyMapper.technologyToReturnDto(technology)).thenReturn(technologyReturnDto);

        Page<TechnologyReturnDto> result = technologyService.getAll(pageable);

        assertEquals(1, result.getTotalElements());
        assertThat(result.getContent().get(0).id()).isEqualTo(
            technologyReturnPage.getContent().get(0).getId());
        assertThat(result.getContent().get(0).name()).isEqualTo(
            technologyReturnPage.getContent().get(0).getName());
    }

    @Test
    void update_ValidId_Successful() {
        when(technologyRepository.findById(technology.getId())).thenReturn(Optional.of(technology));
        TechnologyCreateDto createDto = new TechnologyCreateDto("new name");
        technology.setName(createDto.name());

        when(technologyRepository.save(technology)).thenReturn(technology);
        TechnologyReturnDto expected = new TechnologyReturnDto(technology.getId(),
            technology.getName());
        when(technologyMapper.technologyToReturnDto(technology)).thenReturn(expected);

        TechnologyReturnDto result = technologyService.update(technology.getId(),
            technologyCreateDto);

        assertThat(result).usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    void update_InvalidId_ThrowsNotFoundException() {
        when(technologyRepository.findById(technology.getId())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> technologyService.update(technology.getId(), technologyCreateDto));
    }

    @Test
    void update_NameAlreadyExists_ThrowsDuplicateException() {
        when(technologyRepository.findById(technology.getId()))
            .thenReturn(Optional.of(technology));
        when(technologyRepository.existsByName(technologyCreateDto.name()))
            .thenReturn(true);

        assertThrows(DuplicateException.class,
            () -> technologyService.update(1L, technologyCreateDto));
    }

    @Test
    void delete_ValidId_Successful() {
        when(technologyRepository.existsById(technology.getId())).thenReturn(true);

        technologyService.delete(technology.getId());

        verify(technologyRepository, times(1)).deleteById(1L);
    }

    @Test
    void delete_InvalidId_ThrowsNotFoundException() {
        when(technologyRepository.existsById(technology.getId())).thenReturn(false);

        assertThrows(NotFoundException.class, () -> technologyService.delete(technology.getId()));
    }

    @Test
    void findByName_ExistingName_Successful() {
        when(technologyRepository.findByName("Java")).thenReturn(Optional.ofNullable(technology));

        Technology returnedTechnology = technologyService.findByName("Java");

        assertEquals("Java", returnedTechnology.getName());
    }

    @Test
    void findByName_NonExistingName_ThrowsNotFoundException() {
        assertThrows(NotFoundException.class, () -> technologyService.findByName("Flutter"));
    }
}