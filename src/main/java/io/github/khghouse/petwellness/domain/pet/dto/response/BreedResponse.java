package io.github.khghouse.petwellness.domain.pet.dto.response;

import io.github.khghouse.petwellness.domain.pet.entity.Breed;

public record BreedResponse(Long id, String name) {

    public static BreedResponse from(Breed breed) {
        return new BreedResponse(breed.getId(), breed.getName());
    }
}
