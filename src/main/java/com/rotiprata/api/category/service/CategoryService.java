package com.rotiprata.api.category.service;

import com.rotiprata.api.category.model.Category;
import java.util.List;

public interface CategoryService {
    /**
     * Retrieves all categories from the database.
     *
     * @return list of categories
     */
    List<Category> getAll();
}
