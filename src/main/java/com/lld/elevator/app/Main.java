package com.lld.elevator.app;

import com.lld.elevator.controller.ElevatorController;
import com.lld.elevator.enums.Direction;
import com.lld.elevator.model.Elevator;
import com.lld.elevator.repository.ElevatorRepository;
import com.lld.elevator.service.ElevatorService;
import com.lld.elevator.service.NotificationService;
import com.lld.elevator.strategy.ScanStrategy;

public class Main {
    public static void main(String[] args) {

        int totalFloors    = 8;
        int numElevators   = 3;
        int[] startFloors  = {1, 4, 8};
        String[] labels    = {"A", "B", "C"};

        // ── Wire up dependencies ─────────────────────────
        ElevatorRepository  repo     = new ElevatorRepository();
        NotificationService notifier = new NotificationService();
        ScanStrategy        strategy = new ScanStrategy();
        ElevatorService     service  = new ElevatorService(repo, strategy,
                                                           notifier, totalFloors);
        ElevatorController  ctrl     = new ElevatorController(service);

        for (int i = 0; i < numElevators; i++) {
            Elevator e = new Elevator(i, labels[i], startFloors[i]);
            repo.save(e);
        }
        // Elevator e = new Elevator();
        // e.setId(i);
        // e.setLabel(labels[i]); 
        // e.setCurrentFloor(startFloors[i]);
        // repo.save(e);

        System.out.println("=== Elevator LLD — Consolidation Demo ===");

        // External requests — should consolidate DOWN requests onto Lift C
        ctrl.callElevator(7, Direction.DOWN);
        ctrl.callElevator(6, Direction.DOWN);
        ctrl.callElevator(5, Direction.DOWN);
        ctrl.callElevator(3, Direction.UP);

        // Internal requests
        ctrl.pressFloorInside(2, 1);   // Lift C → go to floor 1
        ctrl.pressFloorInside(0, 8);   // Lift A → go to floor 8

        System.out.println("\n=== Running Simulation ===");
        ctrl.run();
    }
}