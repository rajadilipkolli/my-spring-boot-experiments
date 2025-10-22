package com.example.graphql.services;

import com.example.graphql.config.logging.Loggable;
import com.example.graphql.entities.PostDetailsEntity;
import com.example.graphql.model.request.PostDetailsRequest;
import com.example.graphql.model.response.PostDetailsResponse;
import com.example.graphql.repositories.PostDetailsRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@Loggable
public class PostDetailsService {

    private final PostDetailsRepository postDetailsRepository;
    private final ConversionService myConversionService;

    public PostDetailsService(PostDetailsRepository postDetailsRepository, ConversionService myConversionService) {
        this.postDetailsRepository = postDetailsRepository;
        this.myConversionService = myConversionService;
    }

    public List<PostDetailsResponse> findAllPostDetails() {
        return postDetailsRepository.findAllDetails().stream()
                .map(postDetailsInfo -> myConversionService.convert(postDetailsInfo, PostDetailsResponse.class))
                .toList();
    }

    public Optional<PostDetailsResponse> findPostDetailsById(Long id) {
        return postDetailsRepository
                .findByDetailsId(id)
                .map(postDetailsInfo -> myConversionService.convert(postDetailsInfo, PostDetailsResponse.class));
    }

    public Optional<PostDetailsEntity> findDetailsById(Long id) {
        return postDetailsRepository.findById(id);
    }

    @Transactional
    public Optional<PostDetailsResponse> updatePostDetails(
            PostDetailsEntity postDetailsObj, PostDetailsRequest postDetailsRequest) {
        postDetailsObj.setDetailsKey(postDetailsRequest.detailsKey());
        PostDetailsEntity persistedDetails = postDetailsRepository.save(postDetailsObj);
        return findPostDetailsById(persistedDetails.getId());
    }
}
