package com.example.graphql.gql;

import com.example.graphql.entities.Author;
import com.example.graphql.services.AuthorService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class AuthorGraphQlController {

    private final AuthorService authorService;

    @QueryMapping
    public List<Author> allAuthors() {
        return this.authorService.findAllAuthors();
    }
}
