package com.lld.elevator.dto;
import com.lld.elevator.enums.Direction;
import com.lld.elevator.enums.ElevatorState;
public class StatusDTO{ 
    private String label ; 
    private int currentFloor ; 
    private final Direction directions ; 
    private ElevatorState state ;
    // private final DoorState door ; 
    private final int pendingRequests; 

    public StatusDTO(String label, int currentFloor, Direction directions, ElevatorState state, int pendingRequests){ 
        this.label = label ; 
        this.currentFloor = currentFloor; 
        this.directions = directions ; 
        this.state = state ; 
        this.pendingRequests = pendingRequests ;
    }


    @Override
    public String toString(){ 
        return String.format("LIFT %s: F%d [%s] state=%s pending=%d", label, currentFloor, directions, state, pendingRequests);
    }


}