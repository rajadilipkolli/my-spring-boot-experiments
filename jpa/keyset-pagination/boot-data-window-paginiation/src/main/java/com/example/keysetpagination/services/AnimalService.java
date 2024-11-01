package com.example.keysetpagination.services;

import com.example.keysetpagination.entities.Animal;
import com.example.keysetpagination.exception.AnimalNotFoundException;
import com.example.keysetpagination.mapper.AnimalMapper;
import com.example.keysetpagination.model.query.FindAnimalsQuery;
import com.example.keysetpagination.model.request.AnimalRequest;
import com.example.keysetpagination.model.response.AnimalResponse;
import com.example.keysetpagination.model.response.PagedResult;
import com.example.keysetpagination.repositories.AnimalRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AnimalService {

    private final AnimalRepository animalRepository;
    private final AnimalMapper animalMapper;

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
