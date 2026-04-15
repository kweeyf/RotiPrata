package com.rotiprata.api.category.controller;

import com.rotiprata.api.category.model.Category;
import com.rotiprata.api.category.service.CategoryService;
import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@DisplayName("CategoryController mock integration tests")
class CategoryControllerMockIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoryService categoryService;

    @BeforeEach
    void setUp() {
        RestAssuredMockMvc.mockMvc(mockMvc);
    }

    /** Verifies categories are returned when the service provides category records. */
    @Test
    void listCategories_ShouldReturnCategories_WhenCategoriesExist() {
        //arrange
        Category firstCategory = new Category();
        firstCategory.setId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        firstCategory.setName("Breakfast");

        Category secondCategory = new Category();
        secondCategory.setId(UUID.fromString("00000000-0000-0000-0000-000000000002"));
        secondCategory.setName("Dessert");

        when(categoryService.getAll()).thenReturn(List.of(firstCategory, secondCategory));

        //act
        given()
        .when()
            .get("/api/categories")
        .then()
            //assert
            .status(HttpStatus.OK)
            .contentType(ContentType.JSON)
            .body("$", hasSize(2))
            .body("[0].name", equalTo("Breakfast"))
            .body("[1].name", equalTo("Dessert"));

        //verify
        verify(categoryService, times(1)).getAll();
    }

    /** Verifies an empty JSON array is returned when there are no categories. */
    @Test
    void listCategories_ShouldReturnEmptyList_WhenNoCategoriesExist() {
        //arrange
        when(categoryService.getAll()).thenReturn(List.of());

        //act
        given()
        .when()
            .get("/api/categories")
        .then()
            //assert
            .status(HttpStatus.OK)
            .contentType(ContentType.JSON)
            .body("$", hasSize(0));

        //verify
        verify(categoryService, times(1)).getAll();
    }

    /** Verifies a server error response is returned when category retrieval fails unexpectedly. */
    @Test
    void listCategories_ShouldReturnInternalServerError_WhenServiceThrowsRuntimeException() {
        //arrange
        when(categoryService.getAll()).thenThrow(new RuntimeException("Failed to load categories"));

        //act
        given()
        .when()
            .get("/api/categories")
        .then()
            //assert
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .contentType(ContentType.JSON)
            .body("code", equalTo("error"))
            .body("message", equalTo("Failed to load categories"));

        //verify
        verify(categoryService, times(1)).getAll();
    }
}
