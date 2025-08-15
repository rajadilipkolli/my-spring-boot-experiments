package com.example.graphql.gql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.example.graphql.config.graphql.GraphQlConfiguration;
import com.example.graphql.entities.TagEntity;
import com.example.graphql.services.TagService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.GraphQlTest;
import org.springframework.context.annotation.Import;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@GraphQlTest(TagGraphQLController.class)
@Import(GraphQlConfiguration.class)
class TagGraphQLControllerTest {

    @Autowired
    private GraphQlTester graphQlTester;

    @MockitoBean
    TagService tagService;

    @Test
    void allTags() {
        TagEntity tag1 = new TagEntity();
        tag1.setTagName("tag1");
        TagEntity tag2 = new TagEntity();
        tag2.setTagName("tag2");
        given(tagService.findAllTags()).willReturn(List.of(tag1, tag2));

        var query =
                """
            query {
                allTags {
                    tagName
                }
            }
        """;
        graphQlTester
                .document(query)
                .execute()
                .path("allTags[*].tagName")
                .entityList(String.class)
                .satisfies(names -> assertThat(names).containsExactlyInAnyOrder("tag1", "tag2"));

        verify(tagService, times(1)).findAllTags();
        verifyNoMoreInteractions(tagService);
    }

    @Test
    void findTagByName_found() {
        TagEntity tag = new TagEntity();
        tag.setTagName("tag1");
        given(tagService.findTagByName("tag1")).willReturn(Optional.of(tag));

        var query =
                """
            query findTagByName($tagName: String!) {
                findTagByName(tagName: $tagName) {
                    tagName
                }
            }
        """;
        graphQlTester
                .document(query)
                .variable("tagName", "tag1")
                .execute()
                .path("findTagByName.tagName")
                .entity(String.class)
                .isEqualTo("tag1");

        verify(tagService, times(1)).findTagByName("tag1");
        verifyNoMoreInteractions(tagService);
    }

    @Test
    void findTagByName_notFound() {
        given(tagService.findTagByName("notfound")).willReturn(Optional.empty());

        var query =
                """
            query findTagByName($tagName: String!) {
                findTagByName(tagName: $tagName) {
                    tagName
                }
            }
        """;
        graphQlTester
                .document(query)
                .variable("tagName", "notfound")
                .execute()
                .errors()
                .satisfy(errors -> assertThat(errors).isNotEmpty());

        verify(tagService, times(1)).findTagByName("notfound");
        verifyNoMoreInteractions(tagService);
    }

    @Test
    void createTag() {
        TagEntity tag = new TagEntity();
        tag.setTagName("tag3");
        tag.setTagDescription("desc3");
        given(tagService.saveTag("tag3", "desc3")).willReturn(tag);

        var mutation =
                """
            mutation {
                createTag(tagName: \"tag3\", tagDescription: \"desc3\") {
                    tagName
                    tagDescription
                }
            }
        """;
        graphQlTester
                .document(mutation)
                .execute()
                .path("createTag")
                .entity(TagEntity.class)
                .satisfies(created -> {
                    assertThat(created.getTagName()).isEqualTo("tag3");
                    assertThat(created.getTagDescription()).isEqualTo("desc3");
                });

        verify(tagService, times(1)).saveTag("tag3", "desc3");
        verifyNoMoreInteractions(tagService);
    }

    @Test
    void updateTagDescription() {
        TagEntity tag = new TagEntity();
        tag.setTagName("tag4");
        tag.setTagDescription("newdesc");
        given(tagService.updateTag("tag4", "newdesc")).willReturn(Optional.of(tag));

        var mutation =
                """
            mutation {
                updateTagDescription(tagName: \"tag4\", tagDescription: \"newdesc\") {
                    tagName
                    tagDescription
                }
            }
        """;
        graphQlTester
                .document(mutation)
                .execute()
                .path("updateTagDescription")
                .entity(TagEntity.class)
                .satisfies(updated -> {
                    assertThat(updated.getTagName()).isEqualTo("tag4");
                    assertThat(updated.getTagDescription()).isEqualTo("newdesc");
                });

        verify(tagService, times(1)).updateTag("tag4", "newdesc");
        verifyNoMoreInteractions(tagService);
    }

    @Test
    void deleteTag() {
        // deleteTag returns boolean true, just verify service call
        var mutation =
                """
            mutation {
                deleteTag(tagName: \"tag5\")
            }
        """;

        // No need to stub, as deleteTagByName is void
        graphQlTester
                .document(mutation)
                .execute()
                .path("deleteTag")
                .entity(Boolean.class)
                .isEqualTo(true);

        verify(tagService, times(1)).deleteTagByName("tag5");
        verifyNoMoreInteractions(tagService);
    }
}
