package com.way2jobs.service;

import com.way2jobs.entity.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryService {

    Category saveCategory(Category category);

    List<Category> getAllCategories();

    Optional<Category> getCategoryById(Long id);

    Optional<Category> getCategoryByName(String name);

    Category updateCategory(Long id, Category category);

    void deleteCategory(Long id);

}