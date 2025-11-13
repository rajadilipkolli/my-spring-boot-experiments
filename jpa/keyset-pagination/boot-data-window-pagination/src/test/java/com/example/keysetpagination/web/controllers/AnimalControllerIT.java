package com.example.keysetpagination.web.controllers;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.keysetpagination.common.AbstractIntegrationTest;
import com.example.keysetpagination.entities.Animal;
import com.example.keysetpagination.model.request.AnimalRequest;
import com.example.keysetpagination.repositories.AnimalRepository;
import com.example.keysetpagination.repositories.CustomWindow;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

class AnimalControllerIT extends AbstractIntegrationTest {

    @Autowired
    private AnimalRepository animalRepository;

    private List<Animal> animalList = null;

    @BeforeEach
    void setUp() {
        animalRepository.deleteAllInBatch();

        animalList = new ArrayList<>();
        animalList.add(new Animal().setName("Lion").setType("Mammal").setHabitat("Savannah"));
        animalList.add(new Animal().setName("Elephant").setType("Mammal").setHabitat("Forest"));
        animalList.add(new Animal().setName("Shark").setType("Fish").setHabitat("Ocean"));
        animalList.add(new Animal().setName("Parrot").setType("Bird").setHabitat("Rainforest"));
        animalList.add(new Animal().setName("Penguin").setType("Bird").setHabitat("Antarctic"));
        animalList.add(new Animal().setName("Crocodile").setType("Reptile").setHabitat("Swamp"));
        animalList.add(new Animal().setName("Frog").setType("Amphibian").setHabitat("Wetlands"));
        animalList.add(new Animal().setName("Eagle").setType("Bird").setHabitat("Mountains"));
        animalList.add(new Animal().setName("Whale").setType("Mammal").setHabitat("Ocean"));
        animalList.add(new Animal().setName("Snake").setType("Reptile").setHabitat("Desert"));
        animalList = animalRepository.saveAll(animalList);
    }

    @Test
    void shouldFetchAllAnimals() throws Exception {
        this.mockMvc
                .perform(get("/api/animals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()", is(animalList.size())))
                .andExpect(jsonPath("$.totalElements", is(10)))
                .andExpect(jsonPath("$.pageNumber", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.isFirst", is(true)))
                .andExpect(jsonPath("$.isLast", is(true)))
                .andExpect(jsonPath("$.hasNext", is(false)))
                .andExpect(jsonPath("$.hasPrevious", is(false)));
    }

    @Test
    void shouldFetchAnimalsWithCustomPageSize() throws Exception {
        this.mockMvc
                .perform(get("/api/animals").param("pageSize", "2").param("pageNo", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()", is(2)))
                .andExpect(jsonPath("$.hasNext", is(true)));
    }

    @Test
    void shouldSearchAnimals() throws Exception {
        String contentAsString = this.mockMvc
                .perform(post("/api/animals/search")
                        .param("pageSize", "2")
                        .content(
                                """
                                {
                                    "searchCriteriaList": [
                                        {
                                            "queryOperator": "EQ",
                                            "field": "type",
                                            "values": [
                                                "Bird"
                                            ]
                                        }
                                    ]
                                }
                                """)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()", is(2)))
                .andExpect(jsonPath("$.content[0].type", is("Bird")))
                .andExpect(jsonPath("$.content[1].type", is("Bird")))
                .andExpect(jsonPath("$.last", is(false)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        CustomWindow<Animal> window = jsonMapper.readValue(
                contentAsString, jsonMapper.getTypeFactory().constructParametricType(CustomWindow.class, Animal.class));
        List<Animal> animalResponses = window.getContent();
        this.mockMvc
                .perform(post("/api/animals/search")
                        .param("pageSize", "2")
                        .param(
                                "scrollId",
                                String.valueOf(animalResponses.getLast().getId()))
                        .content(
                                """
                                {
                                    "searchCriteriaList": [
                                        {
                                            "queryOperator": "EQ",
                                            "field": "type",
                                            "values": [
                                                "Bird"
                                            ]
                                        }
                                    ],
                                    "sortRequests": [
                                        {
                                            "field": "type"
                                        }
                                    ]
                                }
                                """)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()", is(1)))
                .andExpect(jsonPath("$.content[0].type", is("Bird")))
                .andExpect(jsonPath("$.last", is(true)));
    }

    @Test
    void shouldReturnResultForNotEqualType() throws Exception {
        this.mockMvc
                .perform(
                        post("/api/animals/search")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {
                                        	"searchCriteriaList": [
                                        		{
                                        			"queryOperator": "NE",
                                        			"field": "type",
                                        			"values": [
                                        				"Mammal",
                                        				"Bird"
                                        			]
                                        		}
                                        	],
                                        	"sortRequests": []
                                        }
                                        """))
                .andExpect(status().isOk())
                // Total animals (10) - Mammals (3) - Birds (3) = 4 animals
                .andExpect(jsonPath("$.content", hasSize(4)))
                .andExpect(jsonPath("$.last", is(true)));
    }

    @Test
    void shouldReturnEmptyResultForNonExistentType() throws Exception {
        this.mockMvc
                .perform(
                        post("/api/animals/search")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                         {
                                        	"searchCriteriaList": [
                                        		{
                                        			"queryOperator": "EQ",
                                        			"field": "name",
                                        			"values": [
                                        				"NonExistent"
                                        			]
                                        		}
                                        	]
                                        }
                                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.last", is(true)));
    }

    @Test
    void shouldFindAnimalById() throws Exception {
        Animal animal = animalList.getFirst();
        Long animalId = animal.getId();

        this.mockMvc
                .perform(get("/api/animals/{id}", animalId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(animal.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(animal.getName())));
    }

    @Test
    void shouldCreateNewAnimal() throws Exception {
        AnimalRequest animalRequest = new AnimalRequest("Snake", "Reptile", "Desert");
        this.mockMvc
                .perform(post("/api/animals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(animalRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is(animalRequest.name())))
                .andExpect(jsonPath("$.type", is(animalRequest.type())))
                .andExpect(jsonPath("$.habitat", is(animalRequest.habitat())));
    }

    @Test
    void shouldReturn400WhenCreateNewAnimalWithoutNameAndType() throws Exception {
        AnimalRequest animalRequest = new AnimalRequest(null, null, null);

        this.mockMvc
                .perform(post("/api/animals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(animalRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("https://api.boot-data-window-pagination.com/errors/validation")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.detail", is("Invalid request content.")))
                .andExpect(jsonPath("$.instance", is("/api/animals")))
                .andExpect(jsonPath("$.violations", hasSize(2)))
                .andExpect(jsonPath("$.violations[0].field", is("name")))
                .andExpect(jsonPath("$.violations[0].message", is("Name cannot be blank")))
                .andExpect(jsonPath("$.violations[1].field", is("type")))
                .andExpect(jsonPath("$.violations[1].message", is("Type cannot be blank")))
                .andReturn();
    }

    @Test
    void shouldUpdateAnimal() throws Exception {
        Animal animal = animalList.getFirst();
        AnimalRequest animalRequest = new AnimalRequest("Updated Animal", animal.getType(), animal.getHabitat());

        Long animalId = animal.getId();
        this.mockMvc
                .perform(put("/api/animals/{id}", animalId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(animalRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(animalId), Long.class))
                .andExpect(jsonPath("$.name", is(animalRequest.name())))
                .andExpect(jsonPath("$.type", is(animalRequest.type())))
                .andExpect(jsonPath("$.habitat", is(animalRequest.habitat())));
    }

    @Test
    void shouldBeIdempotentWhenUpdatingAnimalWithSameData() throws Exception {
        Long animalId = animalList.getFirst().getId();
        AnimalRequest animalRequest = new AnimalRequest("Elephant", "Mammal", "Forest");

        // Perform update twice with same data
        this.mockMvc
                .perform(put("/api/animals/{id}", animalId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(animalRequest)))
                .andExpect(status().isOk());

        this.mockMvc
                .perform(put("/api/animals/{id}", animalId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(animalRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingAnimal() throws Exception {
        AnimalRequest animalRequest = new AnimalRequest("Updated Animal", "Updated Type", "Forest");

        this.mockMvc
                .perform(put("/api/animals/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(animalRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title", is("Not Found")))
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    void shouldDeleteAnimal() throws Exception {
        Animal animal = animalList.getFirst();
        Long animalId = animal.getId();

        this.mockMvc
                .perform(delete("/api/animals/{id}", animalId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(animal.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(animal.getName())));

        // Verify animal is deleted
        this.mockMvc.perform(get("/api/animals/{id}", animalId)).andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnResultForEqualOperator() throws Exception {
        // Test for EQ operator
        this.mockMvc
                .perform(
                        post("/api/animals/search")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {
                                        	"searchCriteriaList": [
                                        		{
                                        			"queryOperator": "EQ",
                                        			"field": "name",
                                        			"values": [
                                        				"Lion"
                                        			]
                                        		}
                                        	]
                                        }
                                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()", is(1)))
                .andExpect(jsonPath("$.content[0].name", is("Lion")))
                .andExpect(jsonPath("$.last", is(true)));
    }

    @Test
    void shouldReturnResultForNotEqualOperator() throws Exception {
        // Test for NE operator
        this.mockMvc
                .perform(
                        post("/api/animals/search")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {
                                        	"searchCriteriaList": [
                                        		{
                                        			"queryOperator": "NE",
                                        			"field": "type",
                                        			"values": [
                                        				"Mammal"
                                        			]
                                        		}
                                        	]
                                        }
                                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()", is(7))) // Total 10 animals - 3 mammals
                .andExpect(jsonPath("$.last", is(true)));
    }

    @Test
    void shouldReturnResultForInOperator() throws Exception {
        // Test for IN operator
        this.mockMvc
                .perform(
                        post("/api/animals/search")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {
                                        	"searchCriteriaList": [
                                        		{
                                        			"queryOperator": "IN",
                                        			"field": "type",
                                        			"values": [
                                        				"Bird",
                                        				"Fish"
                                        			]
                                        		}
                                        	]
                                        }
                                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()", is(4))) // "Parrot", "Penguin", "Shark", "Eagle"
                .andExpect(jsonPath("$.last", is(true)));
    }

    @Test
    void shouldReturnResultForNotInOperator() throws Exception {
        // Test for NOTIN operator
        this.mockMvc
                .perform(
                        post("/api/animals/search")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {
                                        	"searchCriteriaList": [
                                        		{
                                        			"queryOperator": "NOT_IN",
                                        			"field": "type",
                                        			"values": [
                                        				"Bird",
                                        				"Fish"
                                        			]
                                        		}
                                        	]
                                        }
                                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()", is(6)))
                .andExpect(jsonPath("$.last", is(true)));
    }

    @Test
    void shouldReturnResultForLikeOperator() throws Exception {
        // Test for LIKE operator
        this.mockMvc
                .perform(
                        post("/api/animals/search")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {
                                        	"searchCriteriaList": [
                                              {
                                                "queryOperator": "LIKE",
                                                "field": "name",
                                                "values": ["%e%"]
                                              }
                                            ]
                                        }
                                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath(
                        "$.content.size()", is(6))) // "Elephant", "Penguin", "Crocodile", "Eagle", "Whale", "Snake"
                .andExpect(jsonPath("$.last", is(true)));
    }

    @Test
    void shouldReturnResultForContainsOperator() throws Exception {
        // Test for CONTAINS operator
        this.mockMvc
                .perform(
                        post("/api/animals/search")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {
                                           "searchCriteriaList": [
                                              {
                                                "queryOperator": "CONTAINS",
                                                "field": "name",
                                                "values": ["ar"]
                                              }
                                            ]
                                        }
                                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()", is(2))) // "Parrot", "Shark"
                .andExpect(jsonPath("$.last", is(true)));
    }

    @Test
    void shouldReturnResultForStartsWithOperator() throws Exception {
        // Test for STARTS_WITH operator
        this.mockMvc
                .perform(
                        post("/api/animals/search")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {
                                          "searchCriteriaList": [
                                              {
                                                "type": "criteria",
                                                "queryOperator": "STARTS_WITH",
                                                "field": "name",
                                                "values": ["P"]
                                              }
                                            ],
                                            "sortRequests" : [
                                               {
                                                   "field": "name",
                                                   "direction" : "desc"
                                               }
                                            ]
                                        }
                                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()", is(2))) // "Parrot", "Penguin"
                .andExpect(jsonPath("$.last", is(true)));
    }

    @Test
    void shouldReturnResultForEndsWithOperator() throws Exception {
        // Test for ENDS_WITH operator
        this.mockMvc
                .perform(
                        post("/api/animals/search")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {
                                        	"searchCriteriaList": [
                                              {
                                                "queryOperator": "ENDS_WITH",
                                                "field": "name",
                                                "values": ["g"]
                                              }
                                            ]
                                        }
                                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()", is(1))) // "Frog"
                .andExpect(jsonPath("$.content[0].name", is("Frog")))
                .andExpect(jsonPath("$.last", is(true)));
    }

    @Test
    void shouldReturnResultForBetweenOperator() throws Exception {
        // Since 'Animal' doesn't have a numeric field, we'll use 'id' for BETWEEN operator
        Long minId = animalList.getFirst().getId();
        Long maxId = animalList.get(4).getId();

        this.mockMvc
                .perform(post("/api/animals/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                {
                                    "searchCriteriaList": [
                                        {
                                            "queryOperator": "BETWEEN",
                                            "field": "id",
                                            "values": [
                                                %d,
                                                %d
                                            ]
                                        }
                                    ]
                                }
                                """
                                        .formatted(minId, maxId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()", is(5))) // Animals with IDs between minId and maxId
                .andExpect(jsonPath("$.last", is(true)));
    }

    @Test
    void shouldReturnResultForAndOperator() throws Exception {
        // Test for AND operator
        this.mockMvc
                .perform(
                        post("/api/animals/search")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {
                                        	"searchCriteriaList": [
                                        		{
                                        			"queryOperator": "EQ",
                                        			"field": "type",
                                        			"values": [
                                        				"Bird"
                                        			]
                                        		},
                                        		{
                                        			"queryOperator": "EQ",
                                        			"field": "habitat",
                                        			"values": [
                                        				"Rainforest"
                                        			]
                                        		}
                                        	]
                                        }
                                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()", is(1))) // "Parrot"
                .andExpect(jsonPath("$.content[0].name", is("Parrot")))
                .andExpect(jsonPath("$.last", is(true)));
    }

    @Test
    void shouldReturnResultForAndOperatorCriteriaGroup() throws Exception {
        // Test for AND operator
        this.mockMvc
                .perform(
                        post("/api/animals/search")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {
                                          "searchCriteriaList": [
                                            {
                                              "type": "group",
                                              "operator": "AND",
                                              "criteriaList": [
                                                {
                                                  "type": "criteria",
                                                  "queryOperator": "EQ",
                                                  "field": "type",
                                                  "values": ["Bird"]
                                                },
                                                {
                                                  "type": "group",
                                                  "operator": "OR",
                                                  "criteriaList": [
                                                    {
                                                      "type": "criteria",
                                                      "queryOperator": "EQ",
                                                      "field": "habitat",
                                                      "values": ["Rainforest"]
                                                    },
                                                    {
                                                      "type": "criteria",
                                                      "queryOperator": "EQ",
                                                      "field": "habitat",
                                                      "values": ["Mountains"]
                                                    }
                                                  ]
                                                }
                                              ]
                                            }
                                          ]
                                        }
                                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()", is(2))) // "Parrot", "Eagle"
                .andExpect(jsonPath("$.content[0].name", is("Parrot")))
                .andExpect(jsonPath("$.last", is(true)));
    }

    @Test
    void shouldReturnResultForOrOperator() throws Exception {
        // Test for OR operator
        this.mockMvc
                .perform(
                        post("/api/animals/search")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {
                                           "searchCriteriaList": [
                                              {
                                                "queryOperator": "OR",
                                                "field": "name",
                                                "values": ["Shark", "Eagle"]
                                              }
                                            ]
                                        }
                                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()", is(2))) // "Shark" and "Eagle"
                .andExpect(jsonPath("$.last", is(true)));
    }

    @Test
    void shouldFetchAnimalsWithBackwardDirectionUsingSearch() throws Exception {
        // Fetch the first page of animals
        String contentAsString = this.mockMvc
                .perform(
                        post("/api/animals/search")
                                .param("pageSize", "2")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                {
                                    "searchCriteriaList": [
                                      {
                                        "queryOperator": "LIKE",
                                        "field": "name",
                                        "values": ["%e%"]
                                      }
                                    ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()", is(2)))
                .andExpect(jsonPath("$.last", is(false)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        CustomWindow<Animal> window = jsonMapper.readValue(
                contentAsString, jsonMapper.getTypeFactory().constructParametricType(CustomWindow.class, Animal.class));
        List<Animal> animalResponses = window.getContent();

        // Fetch the previous page using backward direction
        this.mockMvc
                .perform(
                        post("/api/animals/search")
                                .param("pageSize", "2")
                                .param(
                                        "scrollId",
                                        String.valueOf(
                                                animalResponses.getFirst().getId()))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                {
                                    "searchCriteriaList": [
                                      {
                                        "queryOperator": "LIKE",
                                        "field": "name",
                                        "values": ["%e%"]
                                      }
                                    ],
                                    "sortDirection": "BACKWARD"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()", is(2)))
                .andExpect(jsonPath("$.last", is(false)));
    }
}
