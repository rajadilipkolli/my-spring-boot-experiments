package com.example.highrps.postcomment.rest;

import com.example.highrps.postcomment.domain.vo.PostCommentId;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class LongToPostCommentIdConverter implements Converter<Long, PostCommentId> {

    @Override
    public PostCommentId convert(Long source) {
        return PostCommentId.of(source);
    }
}
