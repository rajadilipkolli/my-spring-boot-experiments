package com.example.learning.repository;

import com.example.learning.entities.Tag;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Long> {

    /**
     * Finds a tag by its name.
     * @param tagName the name of the tag to find
     * @return the tag if found, empty optional otherwise
     */
    Optional<Tag> findByTagName(String tagName);

    /**
     * Finds all tags whose names are in the given set.
     * @param tagNameSet set of tag names to find
     * @return list of matching tags
     */
    List<Tag> findByTagNameIn(Set<String> tagNameSet);
}
