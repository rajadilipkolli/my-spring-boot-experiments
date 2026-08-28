package com.example.graphql.services;

import com.example.graphql.config.logging.Loggable;
import com.example.graphql.entities.AuthorEntity;
import com.example.graphql.mapper.AuthorRequestToEntityMapper;
import com.example.graphql.model.query.FindQuery;
import com.example.graphql.model.request.AuthorRequest;
import com.example.graphql.model.response.AuthorResponse;
import com.example.graphql.model.response.PagedResult;
import com.example.graphql.repositories.AuthorRepository;
import com.example.graphql.utils.PageUtils;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.*;
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

    public PagedResult<AuthorResponse> findAllAuthors(FindQuery findQuery) {
        // create Pageable instance
        Pageable pageable = PageUtils.createPageable(findQuery);

        Page<AuthorEntity> authorsPage = authorRepository.findAll(pageable);

        List<AuthorResponse> authorResponseList = authorsPage.getContent().stream()
                .map(author -> appConversionService.convert(author, AuthorResponse.class))
                .toList();

        return new PagedResult<>(authorsPage, authorResponseList);
    }

    public Optional<AuthorResponse> findAuthorById(Long id) {
        return authorRepository
                .findById(id)
                .map(authorEntity -> appConversionService.convert(authorEntity, AuthorResponse.class));
    }

    @Transactional
    public AuthorResponse saveAuthor(AuthorRequest authorRequest) {
        AuthorEntity authorEntity = this.appConversionService.convert(authorRequest, AuthorEntity.class);
        return this.appConversionService.convert(
                authorRepository.save(Objects.requireNonNull(authorEntity)), AuthorResponse.class);
    }

    @Transactional
    public Optional<AuthorResponse> updateAuthor(AuthorRequest authorRequest, Long id) {

        return authorRepository.findById(id).map(authorEntity -> {
            authorRequestToEntityMapper.updateAuthorEntity(authorRequest, authorEntity);
            return appConversionService.convert(authorRepository.save(authorEntity), AuthorResponse.class);
        });
    }

    @Transactional
    public void deleteAuthorById(Long id) {
        authorRepository.deleteById(id);
    }

    public Optional<AuthorResponse> findAuthorByEmailId(String email) {
        return this.authorRepository
                .findByEmailAllIgnoreCase(email)
                .map(authorEntity -> appConversionService.convert(authorEntity, AuthorResponse.class));
    }

    public boolean existsAuthorById(Long id) {
        return this.authorRepository.existsById(id);
    }

    public Window<AuthorResponse> findAllAuthors(ScrollPosition scrollPosition, int limit) {
        var authorWindow = authorRepository.findAllBy(
                scrollPosition == null ? ScrollPosition.offset() : scrollPosition, Limit.of(limit), Sort.by("id"));
        return authorWindow.map(author -> appConversionService.convert(author, AuthorResponse.class));
    }
}
