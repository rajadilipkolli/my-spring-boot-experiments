package com.example.keysetpagination.model.query;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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

    @NotNull(message = "Operator cannot be null") private QueryOperator queryOperator;

    @NotBlank(message = "Field name cannot be null or blank")
    private String field;

    @NotNull(message = "Values list cannot be null") @Size(min = 1, message = "Values list cannot be empty")
    @Valid
    private List<String> values;
}
