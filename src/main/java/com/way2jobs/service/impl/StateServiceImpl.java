package com.way2jobs.service.impl;

import com.way2jobs.service.StateService;
import com.way2jobs.entity.State;
import com.way2jobs.repository.StateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StateServiceImpl implements StateService {

    private final StateRepository stateRepository;

    @Override
    public State saveState(State state) {
        return stateRepository.save(state);
    }

    @Override
    public List<State> getAllStates() {
        return stateRepository.findAllByOrderByNameAsc();
    }

    @Override
    public Optional<State> getStateById(Long id) {
        return stateRepository.findById(id);
    }

    @Override
    public Optional<State> getStateByName(String name) {
        return stateRepository.findByName(name);
    }

    @Override
    public State updateState(Long id, State state) {

        State existingState = stateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("State not found with id: " + id));

        existingState.setName(state.getName());

        return stateRepository.save(existingState);
    }

    @Override
    public void deleteState(Long id) {

        if (!stateRepository.existsById(id)) {
            throw new RuntimeException("State not found with id: " + id);
        }

        stateRepository.deleteById(id);
    }







}