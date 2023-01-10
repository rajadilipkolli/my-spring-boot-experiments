package com.example.graphql.services;

import com.example.graphql.entities.AuthorEntity;
import com.example.graphql.mapper.AuthorRequestToEntityMapper;
import com.example.graphql.mapper.adapter.ConversionServiceAdapter;
import com.example.graphql.model.request.AuthorRequest;
import com.example.graphql.model.response.AuthorResponse;
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
    private final ConversionServiceAdapter conversionServiceAdapter;
    private final AuthorRequestToEntityMapper authorRequestToEntityMapper;

    @Transactional(readOnly = true)
    public List<AuthorResponse> findAllAuthors() {
        return authorRepository.findAll().stream()
                .map(conversionServiceAdapter::mapAuthorEntityToAuthorResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<AuthorResponse> findAuthorById(Long id) {
        return authorRepository
                .findById(id)
                .map(this.conversionServiceAdapter::mapAuthorEntityToAuthorResponse);
    }

    public AuthorResponse saveAuthor(AuthorRequest authorRequest) {
        AuthorEntity authorEntity =
                this.conversionServiceAdapter.mapAuthorRequestToAuthorEntity(authorRequest);
        return this.conversionServiceAdapter.mapAuthorEntityToAuthorResponse(
                authorRepository.save(authorEntity));
    }

    public Optional<AuthorResponse> updateAuthor(AuthorRequest authorRequest, Long id) {

        return authorRepository
                .findById(id)
                .map(
                        authorEntity -> {
                            authorRequestToEntityMapper.upDateAuthorEntity(
                                    authorRequest, authorEntity);
                            return this.conversionServiceAdapter.mapAuthorEntityToAuthorResponse(
                                    authorRepository.save(authorEntity));
                        });
    }

    public void deleteAuthorById(Long id) {
        authorRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Optional<AuthorResponse> findAuthorByEmailId(String email) {
        return this.authorRepository
                .findByEmailAllIgnoreCase(email)
                .map(conversionServiceAdapter::mapAuthorEntityToAuthorResponse);
    }
}
