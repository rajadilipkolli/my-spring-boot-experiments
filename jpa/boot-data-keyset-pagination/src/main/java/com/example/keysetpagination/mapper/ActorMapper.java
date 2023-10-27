package com.example.keysetpagination.mapper;

import com.example.keysetpagination.entities.Actor;
import com.example.keysetpagination.model.request.ActorRequest;
import com.example.keysetpagination.model.response.ActorResponse;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ActorMapper {

    public Actor toEntity(ActorRequest actorRequest) {
        Actor actor = new Actor();
        actor.setText(actorRequest.text());
        return actor;
    }

    public void mapActorWithRequest(Actor actor, ActorRequest actorRequest) {
        actor.setText(actorRequest.text());
    }

    public ActorResponse toResponse(Actor actor) {
        return new ActorResponse(actor.getId(), actor.getText());
    }

    public List<ActorResponse> toResponseList(List<Actor> actorList) {
        return actorList.stream().map(this::toResponse).toList();
    }
}
