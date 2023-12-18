package prime.prime.domain.technology.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import prime.prime.domain.technology.entity.Technology;
import prime.prime.domain.technology.mapper.TechnologyMapper;
import prime.prime.domain.technology.models.TechnologyCreateDto;
import prime.prime.domain.technology.models.TechnologyReturnDto;
import prime.prime.domain.technology.repository.TechnologyRepository;
import prime.prime.infrastructure.exception.DuplicateException;
import prime.prime.infrastructure.exception.NotFoundException;



@Service
public class TechnologyServiceImpl implements TechnologyService{
    private final TechnologyRepository technologyRepository;
    private final TechnologyMapper technologyMapper;

    @Autowired
    public TechnologyServiceImpl(TechnologyRepository technologyRepository,TechnologyMapper technologyMapper) {
        this.technologyRepository = technologyRepository;
        this.technologyMapper = technologyMapper;
    }

    @Override
    public TechnologyReturnDto create(TechnologyCreateDto technologyCreateDto) {
        if(technologyRepository.existsByName(technologyCreateDto.name()))
            throw new DuplicateException(Technology.class.getSimpleName(), "name", technologyCreateDto.name());
        return technologyMapper.technologyToReturnDto(technologyRepository.
                save(technologyMapper.createDtoToTechnology(technologyCreateDto)));
    }

    @Override
    public TechnologyReturnDto getById(Long id) {
        Technology technology=technologyRepository.findById(id)
                .orElseThrow(()-> new NotFoundException(Technology.class.getSimpleName(),"id",id.toString()));
        return technologyMapper.technologyToReturnDto(technology);
    }

    @Override
    public Page<TechnologyReturnDto> getAll(Pageable pageable) {
        return technologyRepository.findAll(pageable).map(technologyMapper::technologyToReturnDto);
    }

    @Override
    public TechnologyReturnDto update(Long id, TechnologyCreateDto technologyCreateDto) {
        Technology technology = technologyRepository.findById(id)
                .orElseThrow(
                        () -> new NotFoundException(Technology.class.getSimpleName(), "id", id.toString())
                );

        if (technologyRepository.existsByName(technologyCreateDto.name())) {
            throw new DuplicateException(Technology.class.getSimpleName(), "name", technologyCreateDto.name());
        }

        technology.setName(technologyCreateDto.name());

        return technologyMapper.technologyToReturnDto(technologyRepository.save(technology));
    }

    @Override
    public void delete(Long id) {
        checkIfTechnologyExists(id);
        technologyRepository.deleteById(id);
    }

    @Override
    public Technology findByName(String name) {
        return technologyRepository.findByName(name).orElseThrow(
                ()->new NotFoundException(Technology.class.getSimpleName(),"name",name));
    }

    private void checkIfTechnologyExists(Long id){
        if(!technologyRepository.existsById(id))
            throw new NotFoundException(Technology.class.getSimpleName(),"id",id.toString());
    }
}
