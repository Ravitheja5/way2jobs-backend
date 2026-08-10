package com.way2jobs.scraper;

import com.way2jobs.entity.Category;
import com.way2jobs.entity.Department;
import com.way2jobs.entity.State;
import com.way2jobs.repository.CategoryRepository;
import com.way2jobs.repository.DepartmentRepository;
import com.way2jobs.repository.StateRepository;
import com.way2jobs.scraper.config.ScraperProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ScraperLookupService {

    private final StateRepository states;
    private final CategoryRepository categories;
    private final DepartmentRepository departments;
    private final ScraperProperties props;

    private Map<String, State> stateCache = Map.of();

    private Map<String, Category> categoryCache = Map.of();

    private Map<String, Department> departmentCache = Map.of();

    /*
     * =========================================
     * REFRESH DATABASE LOOKUPS
     * =========================================
     */
    public void refreshCaches() {

        /*
         * -----------------------------
         * STATES
         * -----------------------------
         */
        Map<String, State> stateMap =
                new HashMap<>();

        states.findAll().forEach(state -> {

            String name =
                    state.getName();

            if (name == null) {
                return;
            }

            /*
             * Normal key
             *
             * "Andhra Pradesh"
             * -> "andhra pradesh"
             */
            stateMap.put(
                    key(name),
                    state
            );

            /*
             * Compact key
             *
             * "Andhra Pradesh"
             * -> "andhrapradesh"
             *
             * This fixes:
             *
             * "AndhraPradesh"
             */
            stateMap.put(
                    compactKey(name),
                    state
            );
        });

        stateCache = stateMap;


        /*
         * -----------------------------
         * CATEGORIES
         * -----------------------------
         */
        Map<String, Category> categoryMap =
                new HashMap<>();

        categories.findAll().forEach(category -> {

            if (category.getName() == null) {
                return;
            }

            categoryMap.put(
                    key(category.getName()),
                    category
            );
        });

        categoryCache = categoryMap;


        /*
         * -----------------------------
         * DEPARTMENTS
         * -----------------------------
         */
        Map<String, Department> departmentMap =
                new HashMap<>();

        departments.findAll().forEach(department -> {

            /*
             * Full department name
             */
            if (department.getName() != null) {

                departmentMap.put(
                        key(department.getName()),
                        department
                );

                departmentMap.put(
                        compactKey(
                                department.getName()
                        ),
                        department
                );
            }

            /*
             * Short name
             */
            if (department.getShortName() != null
                    && !department.getShortName().isBlank()) {

                departmentMap.put(
                        key(
                                department.getShortName()
                        ),
                        department
                );

                departmentMap.put(
                        compactKey(
                                department.getShortName()
                        ),
                        department
                );
            }
        });

        departmentCache = departmentMap;
    }


    /*
     * =========================================
     * STATE LOOKUP
     * =========================================
     */
    public Optional<State> resolveState(
            String name
    ) {

        if (name == null
                || name.isBlank()) {

            return Optional.empty();
        }

        /*
         * First try normal key.
         */
        State state =
                stateCache.get(
                        key(name)
                );

        if (state != null) {
            return Optional.of(state);
        }

        /*
         * Then try compact key.
         *
         * All India
         * AllIndia
         *
         * Andhra Pradesh
         * AndhraPradesh
         */
        state =
                stateCache.get(
                        compactKey(name)
                );

        return Optional.ofNullable(state);
    }


    /*
     * =========================================
     * CATEGORY LOOKUP
     * =========================================
     */
    public Optional<Category> resolveCategory(
            String name
    ) {

        if (name == null
                || name.isBlank()) {

            return Optional.empty();
        }

        return Optional.ofNullable(
                categoryCache.get(
                        key(name)
                )
        );
    }


    /*
     * =========================================
     * DEPARTMENT LOOKUP
     * =========================================
     */
    public Optional<Department> resolveDepartment(
            String name
    ) {

        if (name == null
                || name.isBlank()) {

            /*
             * Department is optional.
             */
            return Optional.empty();
        }

        /*
         * First check configured aliases.
         */
        String mapped =
                props
                        .getDepartmentAliases()
                        .getOrDefault(
                                name,
                                name
                        );

        /*
         * Normal lookup.
         */
        Department department =
                departmentCache.get(
                        key(mapped)
                );

        if (department != null) {
            return Optional.of(department);
        }

        /*
         * Compact lookup.
         */
        department =
                departmentCache.get(
                        compactKey(mapped)
                );

        if (department != null) {
            return Optional.of(department);
        }

        /*
         * Fallback department.
         *
         * Only use if explicitly configured.
         */
        String fallback =
                props.getFallbackDepartmentName();

        if (fallback != null
                && !fallback.isBlank()) {

            department =
                    departmentCache.get(
                            key(fallback)
                    );

            if (department == null) {

                department =
                        departmentCache.get(
                                compactKey(fallback)
                        );
            }
        }

        return Optional.ofNullable(
                department
        );
    }


    /*
     * =========================================
     * NORMAL KEY
     * =========================================
     *
     * "Andhra Pradesh"
     * -> "andhra pradesh"
     */
    private String key(
            String value
    ) {

        if (value == null) {
            return "";
        }

        return value
                .trim()
                .toLowerCase(
                        Locale.ROOT
                );
    }


    /*
     * =========================================
     * COMPACT KEY
     * =========================================
     *
     * "Andhra Pradesh"
     * -> "andhrapradesh"
     *
     * "AndhraPradesh"
     * -> "andhrapradesh"
     *
     * Both become identical.
     */
    private String compactKey(
            String value
    ) {

        if (value == null) {
            return "";
        }

        return value
                .replaceAll(
                        "[^a-zA-Z0-9]",
                        ""
                )
                .toLowerCase(
                        Locale.ROOT
                );
    }
}