package com.example.keysetpagination.model.query;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SearchCriteria {

    @NotBlank(message = "Operator cannot be null")
    private QueryOperator queryOperator;

    @NotBlank(message = "Field name cannot be null or blank")
    private String field;

    private List<String> values;
}
