package com.example.highrps.postcomment.domain.vo;

import com.example.highrps.shared.AssertUtil;
import com.example.highrps.shared.IdGenerator;

public record PostCommentId(Long id) {
    public PostCommentId {
        AssertUtil.requireNotNull(id, "PostComment ID cannot be null");
    }

    public static PostCommentId of(Long id) {
        return new PostCommentId(id);
    }

    public static PostCommentId generate() {
        return new PostCommentId(IdGenerator.generateLong());
    }
}
