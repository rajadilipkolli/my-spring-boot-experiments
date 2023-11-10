package com.example.graphql.querydsl.mapper;

import com.example.graphql.querydsl.entities.Tag;
import com.example.graphql.querydsl.model.request.TagRequest;
import com.example.graphql.querydsl.model.response.TagResponse;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class TagMapper {

    public Tag toEntity(TagRequest tagRequest) {
        Tag tag = new Tag();
        tag.setText(tagRequest.text());
        return tag;
    }

    public void mapTagWithRequest(Tag tag, TagRequest tagRequest) {
        tag.setText(tagRequest.text());
    }

    public TagResponse toResponse(Tag tag) {
        return new TagResponse(tag.getId(), tag.getText());
    }

    public List<TagResponse> toResponseList(List<Tag> tagList) {
        return tagList.stream().map(this::toResponse).toList();
    }
}
