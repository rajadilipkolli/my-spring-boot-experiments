package com.example.graphql.services;

import com.example.graphql.config.logging.Loggable;
import com.example.graphql.entities.PostDetailsEntity;
import com.example.graphql.model.request.PostDetailsRequest;
import com.example.graphql.model.response.PostDetailsResponse;
import com.example.graphql.projections.PostDetailsInfo;
import com.example.graphql.repositories.PostDetailsRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@Loggable
public class PostDetailsService {

    private final PostDetailsRepository postDetailsRepository;

    public PostDetailsService(PostDetailsRepository postDetailsRepository) {
        this.postDetailsRepository = postDetailsRepository;
    }

    public List<PostDetailsResponse> findAllPostDetails() {
        return postDetailsRepository.findAllDetails().stream()
                .map(this::toResponse)
                .toList();
    }

    public Optional<PostDetailsResponse> findPostDetailsById(Long id) {
        return postDetailsRepository.findByDetailsId(id).map(this::toResponse);
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

    private PostDetailsResponse toResponse(PostDetailsInfo info) {
        // PostDetailsInfo projection may not expose createdAt/createdBy exactly; adapt as needed
        return new PostDetailsResponse(info.getDetailsKey(), info.getCreatedAt(), info.getCreatedBy());
    }
}
