package com.example.ultimateredis.service;

import com.example.ultimateredis.model.Actor;
import com.example.ultimateredis.repository.ActorRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class ActorService {

    private final ActorRepository actorRepository;

    public ActorService(ActorRepository actorRepository) {
        this.actorRepository = actorRepository;
    }

    public List<Actor> findAll() {
        return (List<Actor>) actorRepository.findAll();
    }

    public Optional<Actor> findActorByName(String name) {
        return actorRepository.findByName(name);
    }

    public Optional<Actor> findActorByNameAndAge(String sampleName, int age) {
        return actorRepository.findByNameAndAge(sampleName, age);
    }

    public Optional<Actor> findActorById(String id) {
        return actorRepository.findById(id);
    }

    public Actor saveActor(Actor actor) {
        return actorRepository.save(actor);
    }

    public List<Actor> saveActors(List<Actor> actors) {
        return (List<Actor>) actorRepository.saveAll(actors);
    }

    public void deleteActorById(String id) {
        actorRepository.deleteById(id);
    }

    public void deleteActorByName(String name) {
        List<Actor> actors = actorRepository.findAllByName(name);
        actors.forEach(actor -> actorRepository.deleteById(actor.getId()));
    }

    public void deleteAll() {
        actorRepository.deleteAll();
    }
}
