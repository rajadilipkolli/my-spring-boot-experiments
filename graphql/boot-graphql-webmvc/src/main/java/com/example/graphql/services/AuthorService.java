package com.example.graphql.services;

import com.example.graphql.config.logging.Loggable;
import com.example.graphql.entities.AuthorEntity;
import com.example.graphql.mapper.AuthorRequestToEntityMapper;
import com.example.graphql.model.request.AuthorRequest;
import com.example.graphql.model.response.AuthorResponse;
import com.example.graphql.repositories.AuthorRepository;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@Loggable
public class AuthorService {

    private final AuthorRepository authorRepository;
    private final ConversionService appConversionService;
    private final AuthorRequestToEntityMapper authorRequestToEntityMapper;

    public AuthorService(
            AuthorRepository authorRepository,
            ConversionService appConversionService,
            AuthorRequestToEntityMapper authorRequestToEntityMapper) {
        this.authorRepository = authorRepository;
        this.appConversionService = appConversionService;
        this.authorRequestToEntityMapper = authorRequestToEntityMapper;
    }

    @Transactional(readOnly = true)
    public List<AuthorResponse> findAllAuthors() {
        List<CompletableFuture<AuthorResponse>> completableFutureList = authorRepository.findAll().stream()
                .map(authorEntity -> CompletableFuture.supplyAsync(
                        () -> appConversionService.convert(authorEntity, AuthorResponse.class)))
                .toList();
        return completableFutureList.stream().map(CompletableFuture::join).toList();
    }

    @Transactional(readOnly = true)
    public Optional<AuthorResponse> findAuthorById(Long id) {
        return authorRepository
                .findById(id)
                .map(authorEntity -> appConversionService.convert(authorEntity, AuthorResponse.class));
    }

    public AuthorResponse saveAuthor(AuthorRequest authorRequest) {
        AuthorEntity authorEntity = this.appConversionService.convert(authorRequest, AuthorEntity.class);
        return this.appConversionService.convert(
                authorRepository.save(Objects.requireNonNull(authorEntity)), AuthorResponse.class);
    }

    public Optional<AuthorResponse> updateAuthor(AuthorRequest authorRequest, Long id) {

        return authorRepository.findById(id).map(authorEntity -> {
            authorRequestToEntityMapper.updateAuthorEntity(authorRequest, authorEntity);
            return appConversionService.convert(authorRepository.save(authorEntity), AuthorResponse.class);
        });
    }

    public void deleteAuthorById(Long id) {
        authorRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Optional<AuthorResponse> findAuthorByEmailId(String email) {
        return this.authorRepository
                .findByEmailAllIgnoreCase(email)
                .map(authorEntity -> appConversionService.convert(authorEntity, AuthorResponse.class));
    }

    public boolean existsAuthorById(Long id) {
        return this.authorRepository.existsById(id);
    }
}
