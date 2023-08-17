package com.example.graphql.repositories;

import com.example.graphql.entities.PostDetailsEntity;
import com.example.graphql.projections.PostDetailsInfo;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PostDetailsRepository extends JpaRepository<PostDetailsEntity, Long> {
    @Query("select p from PostDetailsEntity p where p.id = ?1")
    Optional<PostDetailsInfo> findByDetailsId(Long id);

    @Query("select p from PostDetailsEntity p")
    List<PostDetailsInfo> findAllDetails();
}
