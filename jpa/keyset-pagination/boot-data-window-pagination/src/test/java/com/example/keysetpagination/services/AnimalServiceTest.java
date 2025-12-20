package com.example.keysetpagination.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.times;
import static org.mockito.BDDMockito.verify;
import static org.mockito.BDDMockito.willDoNothing;

import com.example.keysetpagination.entities.Animal;
import com.example.keysetpagination.mapper.AnimalMapper;
import com.example.keysetpagination.model.response.AnimalResponse;
import com.example.keysetpagination.repositories.AnimalRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AnimalServiceTest {

    @Mock
    private AnimalRepository animalRepository;

    @Mock
    private AnimalMapper animalMapper;

    @InjectMocks
    private AnimalService animalService;

    @Test
    void findAnimalById() {
        // given
        given(animalRepository.findById(1L)).willReturn(Optional.of(getAnimal()));
        given(animalMapper.toResponse(any(Animal.class))).willReturn(getAnimalResponse());
        // when
        Optional<AnimalResponse> optionalAnimal = animalService.findAnimalById(1L);
        // then
        assertThat(optionalAnimal).isPresent().hasValueSatisfying(animalResponse -> {
            assertThat(animalResponse.id()).isOne();
            assertThat(animalResponse.name()).isEqualTo("junitName");
            assertThat(animalResponse.type()).isEqualTo("junitType");
            assertThat(animalResponse.habitat()).isEqualTo("junitHabitat");
        });
    }

    @Test
    void deleteAnimalById() {
        // given
        willDoNothing().given(animalRepository).deleteById(1L);
        // when
        animalService.deleteAnimalById(1L);
        // then
        verify(animalRepository, times(1)).deleteById(1L);
    }

    private Animal getAnimal() {
        return new Animal().setId(1L).setName("junitName").setType("junitType").setHabitat("junitHabitat");
    }

    private AnimalResponse getAnimalResponse() {
        return new AnimalResponse(1L, "junitName", "junitType", "junitHabitat", LocalDateTime.MAX);
    }
}
