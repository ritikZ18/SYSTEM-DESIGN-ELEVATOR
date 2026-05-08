package com.lld.elevator.controller;

import com.lld.elevator.dto.RequestDTO;
import com.lld.elevator.dto.StatusDTO;
import com.lld.elevator.enums.Direction;
import com.lld.elevator.service.ElevatorService;
import java.util.List;

public class ElevatorController {
    private final ElevatorService service;

    public ElevatorController(ElevatorService service) {
        this.service = service;
    }

    public void callElevator(int floor, Direction direction) {
        System.out.printf("%nCALL: Floor %d [%s]%n", floor, direction);
        service.handleRequest(new RequestDTO(floor, direction, false, -1));
    }

    public void pressFloorInside(int elevatorId, int floor) {
        service.handleRequest(new RequestDTO(floor, Direction.IDLE, true, elevatorId));
    }

    public void emergencyStop(int elevatorId) {
        System.out.println("EMERGENCY STOP: Lift " + elevatorId);
        service.emergencyStop(elevatorId);
    }

    public void step() { service.step(); }

    public List<StatusDTO> getStatus() {
        return service.getStatus();
    }

    public void run() {
        int max = 50, steps = 0;
        while (!service.allIdle() && steps++ < max) {
            step();
            printStatus();
        }
        System.out.println("\nAll requests served.");
    }

    public void printStatus() {
        List<StatusDTO> statuses = service.getStatus();
        statuses.forEach(s -> System.out.println("  " + s));
    }
}