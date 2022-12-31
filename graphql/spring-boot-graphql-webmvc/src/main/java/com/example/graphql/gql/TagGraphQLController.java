package com.example.graphql.gql;

import com.example.graphql.entities.TagEntity;
import com.example.graphql.services.TagService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;

@Controller
@Validated
@Slf4j
@RequiredArgsConstructor
public class TagGraphQLController {

    private final TagService tagService;

    @MutationMapping
    public TagEntity createTag(
            @NotBlank @Argument("tagName") String tagName,
            @Argument("tagDescription") String tagDescription) {
        return this.tagService.saveTag(tagName, tagDescription);
    }
}
