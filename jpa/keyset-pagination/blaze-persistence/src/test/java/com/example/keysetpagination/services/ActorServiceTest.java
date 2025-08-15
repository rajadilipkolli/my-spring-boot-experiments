package com.example.keysetpagination.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.times;
import static org.mockito.BDDMockito.verify;
import static org.mockito.BDDMockito.willDoNothing;

import com.example.keysetpagination.entities.Actor;
import com.example.keysetpagination.mapper.ActorMapper;
import com.example.keysetpagination.model.response.ActorResponse;
import com.example.keysetpagination.repositories.ActorRepository;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ActorServiceTest {

    @Mock
    private ActorRepository actorRepository;

    @Mock
    private ActorMapper actorMapper;

    @InjectMocks
    private ActorService actorService;

    @Test
    void findActorById() {
        // given
        given(actorRepository.findById(1L)).willReturn(Optional.of(getActor()));
        given(actorMapper.toResponse(any(Actor.class))).willReturn(getActorResponse());
        // when
        Optional<ActorResponse> optionalActor = actorService.findActorById(1L);
        // then
        assertThat(optionalActor).isPresent();
        ActorResponse actor = optionalActor.get();
        assertThat(actor.id()).isEqualTo(1L);
        assertThat(actor.name()).isEqualTo("junitTest");
    }

    @Test
    void deleteActorById() {
        // given
        willDoNothing().given(actorRepository).deleteById(1L);
        // when
        actorService.deleteActorById(1L);
        // then
        verify(actorRepository, times(1)).deleteById(1L);
    }

    private Actor getActor() {
        Actor actor = new Actor();
        actor.setId(1L);
        actor.setName("junitTest");
        actor.setCreatedOn(LocalDate.now());
        return actor;
    }

    private ActorResponse getActorResponse() {
        return new ActorResponse(1L, "junitTest", LocalDate.now());
    }
}
