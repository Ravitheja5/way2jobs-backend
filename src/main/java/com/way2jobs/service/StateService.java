package com.way2jobs.service;

import com.way2jobs.entity.State;

import java.util.List;
import java.util.Optional;

public interface StateService {

    State saveState(State state);

    List<State> getAllStates();

    Optional<State> getStateById(Long id);

    Optional<State> getStateByName(String name);

    State updateState(Long id, State state);

    void deleteState(Long id);
}