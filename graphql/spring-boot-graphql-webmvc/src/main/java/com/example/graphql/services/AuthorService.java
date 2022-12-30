package com.example.graphql.services;

import com.example.graphql.entities.AuthorEntity;
import com.example.graphql.repositories.AuthorRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthorService {

    private final AuthorRepository authorRepository;

    public List<AuthorEntity> findAllAuthors() {
        return authorRepository.findAll();
    }

    public Optional<AuthorEntity> findAuthorById(Long id) {
        return authorRepository.findById(id);
    }

    public AuthorEntity saveAuthor(AuthorEntity author) {
        return authorRepository.save(author);
    }

    public void deleteAuthorById(Long id) {
        authorRepository.deleteById(id);
    }

    public Optional<AuthorEntity> findAuthorByEmailId(String email) {
        return this.authorRepository.findByEmailAllIgnoreCase(email);
    }
}
