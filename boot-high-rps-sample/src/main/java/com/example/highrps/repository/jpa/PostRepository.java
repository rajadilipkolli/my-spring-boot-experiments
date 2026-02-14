package com.example.highrps.repository.jpa;

import com.example.highrps.entities.PostEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.example.highrps.shared.ResourceNotFoundException;
import org.springframework.transaction.annotation.Transactional;

public interface PostRepository extends JpaRepository<PostEntity, Long> {
    boolean existsByTitleAndAuthorEntity_EmailIgnoreCase(String title, String email);

    Optional<PostEntity> findByTitleAndAuthorEntity_Email(String title, String email);

    @EntityGraph(attributePaths = {"tags", "details", "authorEntity", "tags.tagEntity"})
    @Override
    Optional<PostEntity> findById(Long aLong);

    @EntityGraph(attributePaths = {"tags", "details", "authorEntity", "tags.tagEntity"})
    List<PostEntity> findByTitleIn(List<String> titles);

    @Transactional
    @Query("delete from PostEntity p where p.title = :title and lower(p.authorEntity.email) = :email")
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    int deleteByTitleAndAuthorEntity_EmailIgnoreCase(@Param("title") String title, @Param("email") String email);

    // Default convenience method
    default PostEntity getByTitleAndEmail(String title, String email) {
        return findByTitleAndAuthorEntity_Email(title, email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Post not found with title: " + title + " and email: " + email));
    }
}
