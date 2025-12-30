package com.example.graphql.gql;

import com.example.graphql.exception.TagNotFoundException;
import com.example.graphql.model.response.TagResponse;
import com.example.graphql.services.TagService;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;

@Controller
@Validated
public class TagGraphQLController {

    private static final Logger log = LoggerFactory.getLogger(TagGraphQLController.class);

    private final TagService tagService;

    public TagGraphQLController(TagService tagService) {
        this.tagService = tagService;
    }

    @QueryMapping
    public List<TagResponse> allTags() {
        return this.tagService.findAllTags();
    }

    @QueryMapping
    public TagResponse findTagByName(@Argument("tagName") String tagName) {
        return this.tagService.findTagByName(tagName).orElseThrow(() -> new TagNotFoundException(tagName));
    }

    @MutationMapping
    public TagResponse createTag(
            @NotBlank @Argument("tagName") String tagName, @Argument("tagDescription") String tagDescription) {
        return this.tagService.saveTag(tagName, tagDescription);
    }

    @MutationMapping
    public TagResponse updateTagDescription(
            @NotBlank @Argument("tagName") String tagName,
            @NotBlank @Argument("tagDescription") String tagDescription) {
        return this.tagService.updateTag(tagName, tagDescription);
    }

    @MutationMapping
    public boolean deleteTag(@NotBlank @Argument("tagName") String tagName) {
        this.tagService.deleteTagByName(tagName);
        log.info("Deleted tag with Name :{}", tagName);
        return true;
    }
}
