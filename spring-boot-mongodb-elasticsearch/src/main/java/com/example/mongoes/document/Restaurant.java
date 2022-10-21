package com.example.mongoes.document;

import com.example.mongoes.utils.AppConstants;
import java.util.ArrayList;
import java.util.List;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.annotation.Version;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@org.springframework.data.elasticsearch.annotations.Document(
        indexName = AppConstants.RESTAURANT_COLLECTION)
@Document(collection = AppConstants.RESTAURANT_COLLECTION)
@NoArgsConstructor
@TypeAlias(AppConstants.RESTAURANT_COLLECTION)
@ToString
@Setting(shards = 3)
public class Restaurant {

    @Id private String id;

    @Indexed
    @Field(value = "restaurant_id", type = FieldType.Long)
    @org.springframework.data.mongodb.core.mapping.Field("restaurant_id")
    private Long restaurantId;

    @NotBlank(message = "Restaurant Name Can't be Blank")
    private String name;

    @Field(type = FieldType.Nested, includeInParent = true)
    private Address address;

    @NotBlank(message = "Borough Can't be Blank")
    @Field(fielddata = true, type = FieldType.Text)
    private String borough;

    @NotBlank(message = "Cuisine Can't be Blank")
    @Field(fielddata = true, type = FieldType.Text)
    private String cuisine;

    private List<Grades> grades = new ArrayList<>();

    @Version private Long version;
}
