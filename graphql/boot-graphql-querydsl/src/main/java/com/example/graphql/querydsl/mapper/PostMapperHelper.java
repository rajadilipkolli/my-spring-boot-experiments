package com.example.graphql.querydsl.mapper;

import com.example.graphql.querydsl.entities.Post;
import com.example.graphql.querydsl.entities.PostComment;
import com.example.graphql.querydsl.entities.PostDetails;
import com.example.graphql.querydsl.entities.PostTag;
import com.example.graphql.querydsl.entities.PostTagId;
import com.example.graphql.querydsl.entities.QTag;
import com.example.graphql.querydsl.entities.Tag;
import com.example.graphql.querydsl.model.request.CreatePostRequest;
import com.example.graphql.querydsl.model.request.PostCommentRequest;
import com.example.graphql.querydsl.model.request.TagRequest;
import com.example.graphql.querydsl.repositories.TagRepository;
import com.querydsl.core.types.Predicate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.mapstruct.AfterMapping;
import org.mapstruct.MappingTarget;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
class PostMapperHelper {

    private final TagRepository tagRepository;

    PostMapperHelper(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    @AfterMapping
    void setAfterMappingToPost(CreatePostRequest createPostRequest, @MappingTarget Post post) {
        post.addDetails(
                new PostDetails().setCreatedBy(createPostRequest.createdBy()).setCreatedOn(LocalDateTime.now()));

        List<PostCommentRequest> postCommentRequests = createPostRequest.comments();
        if (!CollectionUtils.isEmpty(postCommentRequests)) {
            postCommentRequests.forEach(postCommentRequest -> post.addComment(
                    new PostComment().setReview(postCommentRequest.review()).setCreatedOn(LocalDateTime.now())));
        }

        updateTagsToPost(createPostRequest.tags(), post);
    }

    @AfterMapping
    void updateTagsToPost(List<TagRequest> tagRequests, @MappingTarget Post post) {
        if (!CollectionUtils.isEmpty(tagRequests)) {
            tagRequests.forEach(tagRequest -> {
                Predicate predicate = QTag.tag.name.equalsIgnoreCase(tagRequest.name());
                Optional<Tag> tag = this.tagRepository.findOne(predicate);
                if (tag.isPresent()) {
                    PostTag postTag = new PostTag()
                            .setPost(post)
                            .setTag(tag.get())
                            .setId(new PostTagId(post.getId(), tag.get().getId()));
                    post.getTags().add(postTag);
                } else {
                    post.addTag(tagRepository.save(
                            new Tag().setName(tagRequest.name().toLowerCase(Locale.ENGLISH))));
                }
            });
        }
    }
}
