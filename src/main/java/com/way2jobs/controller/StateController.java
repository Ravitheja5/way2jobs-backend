package com.way2jobs.controller;

import com.way2jobs.entity.State;
import com.way2jobs.service.StateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/states")
@RequiredArgsConstructor
public class StateController {

    private final StateService stateService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public State createState(@RequestBody State state) {
        return stateService.saveState(state);
    }

    @GetMapping
    public List<State> getAllStates() {
        return stateService.getAllStates();
    }

    @GetMapping("/{id}")
    public State getStateById(@PathVariable Long id) {
        return stateService.getStateById(id)
                .orElseThrow(() -> new RuntimeException("State not found with id: " + id));
    }

    @PutMapping("/{id}")
    public State updateState(@PathVariable Long id,
                             @RequestBody State state) {
        return stateService.updateState(id, state);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteState(@PathVariable Long id) {
        stateService.deleteState(id);
    }
}