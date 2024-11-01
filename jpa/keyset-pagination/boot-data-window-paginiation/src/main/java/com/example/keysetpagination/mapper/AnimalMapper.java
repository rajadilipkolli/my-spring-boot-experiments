package com.example.keysetpagination.mapper;

import com.example.keysetpagination.entities.Animal;
import com.example.keysetpagination.model.request.AnimalRequest;
import com.example.keysetpagination.model.response.AnimalResponse;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AnimalMapper {

    public Animal toEntity(AnimalRequest animalRequest) {
        Animal animal = new Animal();
        animal.setText(animalRequest.text());
        return animal;
    }

    public void mapAnimalWithRequest(Animal animal, AnimalRequest animalRequest) {
        animal.setText(animalRequest.text());
    }

    public AnimalResponse toResponse(Animal animal) {
        return new AnimalResponse(animal.getId(), animal.getText());
    }

    public List<AnimalResponse> toResponseList(List<Animal> animalList) {
        return animalList.stream().map(this::toResponse).toList();
    }
}
