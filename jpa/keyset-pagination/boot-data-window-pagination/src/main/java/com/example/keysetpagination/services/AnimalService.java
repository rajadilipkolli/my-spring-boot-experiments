package com.example.keysetpagination.services;

import com.example.keysetpagination.entities.Animal;
import com.example.keysetpagination.exception.AnimalNotFoundException;
import com.example.keysetpagination.mapper.AnimalMapper;
import com.example.keysetpagination.model.query.FindAnimalsQuery;
import com.example.keysetpagination.model.query.SearchRequest;
import com.example.keysetpagination.model.query.SortRequest;
import com.example.keysetpagination.model.request.AnimalRequest;
import com.example.keysetpagination.model.response.AnimalResponse;
import com.example.keysetpagination.model.response.PagedResult;
import com.example.keysetpagination.repositories.AnimalRepository;
import java.util.ArrayList;
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
import org.springframework.util.CollectionUtils;

@Service
@Transactional(readOnly = true)
public class AnimalService {

    private final AnimalRepository animalRepository;
    private final AnimalMapper animalMapper;
    private final EntitySpecification<Animal> animalEntitySpecification;

    public AnimalService(
            AnimalRepository animalRepository,
            AnimalMapper animalMapper,
            EntitySpecification<Animal> animalEntitySpecification) {
        this.animalRepository = animalRepository;
        this.animalMapper = animalMapper;
        this.animalEntitySpecification = animalEntitySpecification;
    }

    public PagedResult<AnimalResponse> findAllAnimals(FindAnimalsQuery findAnimalsQuery) {

        // create Pageable instance
        Pageable pageable = createPageable(findAnimalsQuery);

        Page<Animal> animalsPage = animalRepository.findAll(pageable);

        List<AnimalResponse> animalResponseList = animalMapper.toResponseList(animalsPage.getContent());

        return new PagedResult<>(animalsPage, animalResponseList);
    }

    /**
 * Creates a Pageable object for pagination and sorting based on the query parameters.
 *
 * @param findAnimalsQuery the query object containing pagination and sorting parameters:
 *                        pageNo (1-based page number),
 *                        pageSize (number of items per page),
 *                        sortBy (field name to sort by),
 *                        sortDir (sort direction: "ASC" or "DESC")
 * @return a Pageable object configured with zero-based page number, page size, and sort criteria
 * @see org.springframework.data.domain.Pageable
 * @see org.springframework.data.domain.PageRequest
 * @see org.springframework.data.domain.Sort
 */
private Pageable createPageable(FindAnimalsQuery findAnimalsQuery) {
    int pageNo = Math.max(findAnimalsQuery.pageNo() - 1, 0);
    Sort sort = Sort.by(
            findAnimalsQuery.sortDir().equalsIgnoreCase(Sort.Direction.ASC.name())
                    ? Sort.Order.asc(findAnimalsQuery.sortBy())
                    : Sort.Order.desc(findAnimalsQuery.sortBy()));
    return PageRequest.of(pageNo, findAnimalsQuery.pageSize(), sort);
}

    /**
 * Searches for animals based on specified criteria with pagination and scrolling support.
 *
 * @param searchRequest the search criteria and filters to apply
 * @param pageSize the maximum number of results to return per page
 * @param scrollId the ID to continue scrolling from a previous search result, null for first page
 * @return a Window containing AnimalResponse objects matching the search criteria
 * @throws IllegalArgumentException if pageSize is less than 1
 * @see SearchRequest
 * @see AnimalResponse
 * @see Window
 */
public Window<AnimalResponse> searchAnimals(SearchRequest searchRequest, int pageSize, Long scrollId) {

        Specification<Animal> specification =
                animalEntitySpecification.specificationBuilder(searchRequest.getSearchCriteriaList(), Animal.class);

        // Create initial ScrollPosition or continue from the given scrollId
        ScrollPosition position = scrollId == null
                ? ScrollPosition.keyset()
                : ScrollPosition.of(Collections.singletonMap("id", scrollId), ScrollPosition.Direction.FORWARD);

        // Parse and create sort orders
        List<Sort.Order> orders = new ArrayList<>();

        if (!CollectionUtils.isEmpty(searchRequest.getSortDtos())) {
            for (SortRequest sortRequest : searchRequest.getSortDtos()) {
                Sort.Direction direction = "desc"
                                .equalsIgnoreCase(Optional.ofNullable(sortRequest.getDirection())
                                        .orElse("asc"))
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC;
                orders.add(new Sort.Order(direction, sortRequest.getField()));
            }
        } else {
            orders.add(new Sort.Order(Sort.Direction.ASC, "id"));
        }

        return animalRepository
                .findAll(specification, PageRequest.of(0, pageSize, Sort.by(orders)), position, Animal.class)
                .map(animalMapper::toResponse);
    }

    /**
 * Retrieves an animal by its unique identifier.
 *
 * @param id the unique identifier of the animal to find
 * @return an Optional containing the mapped AnimalResponse if found, or empty Optional if not found
 * @throws IllegalArgumentException if id is null
 */
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
