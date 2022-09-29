package com.example.mongoes.document;

import com.example.mongoes.utils.AppConstants;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
@Setter
@org.springframework.data.elasticsearch.annotations.Document(
        indexName = AppConstants.RESTAURANT_COLLECTION)
@Document(collection = AppConstants.RESTAURANT_COLLECTION)
@NoArgsConstructor
@ToString
public class Restaurant {

    @Id private String id;

    @Indexed
    @Field("restaurant_id")
    private Long restaurantId;

    private String name;

    private Address address;

    @NotBlank(message = "Borough Cant be Blank")
    private String borough;

    @NotBlank(message = "Cuisine Cant be Blank")
    private String cuisine;

    private List<Grades> grades = new ArrayList<>();

    @Version private Long version;
}
