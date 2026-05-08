package com.lld.elevator.service;

import com.lld.elevator.dto.RequestDTO;
import com.lld.elevator.dto.StatusDTO;
import com.lld.elevator.enums.Direction;
import com.lld.elevator.exception.CapacityException;
import com.lld.elevator.model.Elevator;
import com.lld.elevator.repository.ElevatorRepository;
import com.lld.elevator.strategy.DispatchStrategy;
import java.util.List;
import java.util.stream.Collectors;

public class ElevatorService {
    private final ElevatorRepository    repository;
    private final DispatchStrategy      strategy;
    private final NotificationService   notifier;
    private final int                   totalFloors;

    public ElevatorService(ElevatorRepository repository,
                           DispatchStrategy strategy,
                           NotificationService notifier,
                           int totalFloors) {
        this.repository  = repository;
        this.strategy    = strategy;
        this.notifier    = notifier;
        this.totalFloors = totalFloors;
    }

    public void handleRequest(RequestDTO dto) {
        if (dto.isInternal()) {
            handleInternal(dto);
        } else {
            handleExternal(dto);
        }
    }

    private void handleExternal(RequestDTO dto) {
        List<Elevator> all  = repository.findAll();
        Elevator best       = strategy.dispatch(all, dto.getFloor(),
                                                dto.getDirection(), totalFloors);
        if (best.isFull()) throw new CapacityException(best.getId());
        best.addRequest(dto.getFloor(), dto.getDirection());
        notifier.notifyDispatched(best, dto.getFloor());
    }

    private void handleInternal(RequestDTO dto) {
        Elevator e = repository.findById(dto.getElevatorId());
        if (e == null || e.isBroken()) return;
        Direction dir = dto.getFloor() >= e.getCurrentFloor()
                        ? Direction.UP : Direction.DOWN;
        e.addRequest(dto.getFloor(), dir);
    }

    public void step() {
        for (Elevator e : repository.findAll()) {
            if (e.isBroken()) continue;
            if (e.serveCurrentFloor()) notifier.notifyArrival(e);
            e.updateDirection();
            if (!e.isIdle()) {
                e.move(totalFloors);
                System.out.printf("  [Lift %s] moved to F%d [%s]%n",
                    e.getLabel(), e.getCurrentFloor(), e.getDirection());
            }
        }
    }

    public void emergencyStop(int id) {
        Elevator e = repository.findById(id);
        if (e != null) { e.markBroken(); notifier.notifyBroken(e); }
    }

    public boolean allIdle() {
        return repository.findAll().stream().allMatch(Elevator::isIdle);
    }

    public List<StatusDTO> getStatus() {
        return repository.findAll().stream()
                                .map(e -> new StatusDTO(e.getLabel(), e.getCurrentFloor(),
                                   e.getDirection(), e.getState(),
                                   e.totalRequests()))
            .collect(java.util.stream.Collectors.toList());
    }
}