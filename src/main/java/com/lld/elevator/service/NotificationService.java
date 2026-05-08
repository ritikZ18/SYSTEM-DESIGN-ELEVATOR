package com.lld.elevator.service;

import com.lld.elevator.model.Elevator;

public class NotificationService {

    public void notifyArrival(Elevator elevator) {
        System.out.printf("[NOTIFY] Lift %s arrived at floor %d%n",
            elevator.getLabel(), elevator.getCurrentFloor());
    }

    public void notifyBroken(Elevator elevator) {
        System.out.printf("[NOTIFY] ALERT — Lift %s is out of service!%n",
            elevator.getLabel());
    }

    public void notifyDispatched(Elevator elevator, int floor) {
        System.out.printf("[NOTIFY] Lift %s dispatched to floor %d%n",
            elevator.getLabel(), floor);
    }
}