package com.example.keysetpagination.services;

import com.example.keysetpagination.entities.Animal;
import com.example.keysetpagination.exception.AnimalNotFoundException;
import com.example.keysetpagination.mapper.AnimalMapper;
import com.example.keysetpagination.model.query.FindAnimalsQuery;
import com.example.keysetpagination.model.request.AnimalRequest;
import com.example.keysetpagination.model.response.AnimalResponse;
import com.example.keysetpagination.model.response.PagedResult;
import com.example.keysetpagination.repositories.AnimalRepository;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Window;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AnimalService {

    private final AnimalRepository animalRepository;
    private final AnimalMapper animalMapper;

    public AnimalService(AnimalRepository animalRepository, AnimalMapper animalMapper) {
        this.animalRepository = animalRepository;
        this.animalMapper = animalMapper;
    }

    public PagedResult<AnimalResponse> findAllAnimals(FindAnimalsQuery findAnimalsQuery) {

        // create Pageable instance
        Pageable pageable = createPageable(findAnimalsQuery);

        Page<Animal> animalsPage = animalRepository.findAll(pageable);

        List<AnimalResponse> animalResponseList = animalMapper.toResponseList(animalsPage.getContent());

        return new PagedResult<>(animalsPage, animalResponseList);
    }

    private Pageable createPageable(FindAnimalsQuery findAnimalsQuery) {
        int pageNo = Math.max(findAnimalsQuery.pageNo() - 1, 0);
        Sort sort = Sort.by(
                findAnimalsQuery.sortDir().equalsIgnoreCase(Sort.Direction.ASC.name())
                        ? Sort.Order.asc(findAnimalsQuery.sortBy())
                        : Sort.Order.desc(findAnimalsQuery.sortBy()));
        return PageRequest.of(pageNo, findAnimalsQuery.pageSize(), sort);
    }

    public Window<AnimalResponse> searchAnimals(String name, String type, int pageSize, Long scrollId) {
        Specification<Animal> specification = Specification.where(null);

        if (name != null && !name.isEmpty()) {
            specification = specification.and(AnimalSpecifications.hasName(name));
        }

        if (type != null && !type.isEmpty()) {
            specification = specification.and(AnimalSpecifications.hasType(type));
        }

        // Create initial ScrollPosition or continue from the given scrollId
        ScrollPosition position = scrollId == null
                ? ScrollPosition.keyset()
                : ScrollPosition.of(Collections.singletonMap("id", scrollId), ScrollPosition.Direction.FORWARD);

        // Create WindowIterator
        Specification<Animal> finalSpecification = specification;

        return animalRepository
                .findAll(finalSpecification, PageRequest.of(0, pageSize, Sort.by(Sort.Order.asc("id"))), position)
                .map(animalMapper::toResponse);
    }

    public Optional<AnimalResponse> findAnimalById(Long id) {
        return animalRepository.findById(id).map(animalMapper::toResponse);
    }

    @Transactional
    public AnimalResponse saveAnimal(AnimalRequest animalRequest) {
        Animal animal = animalMapper.toEntity(animalRequest);
        Animal savedAnimal = animalRepository.save(animal);
        return animalMapper.toResponse(savedAnimal);
    }

    @Transactional
    public AnimalResponse updateAnimal(Long id, AnimalRequest animalRequest) {
        Animal animal = animalRepository.findById(id).orElseThrow(() -> new AnimalNotFoundException(id));

        // Update the animal object with data from animalRequest
        animalMapper.mapAnimalWithRequest(animal, animalRequest);

        // Save the updated animal object
        Animal updatedAnimal = animalRepository.save(animal);

        return animalMapper.toResponse(updatedAnimal);
    }

    @Transactional
    public void deleteAnimalById(Long id) {
        animalRepository.deleteById(id);
    }
}
