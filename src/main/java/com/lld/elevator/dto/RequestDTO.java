package com.lld.elevator.dto;

import com.lld.elevator.enums.Direction;
import com.lld.elevator.enums.ElevatorState;
public class RequestDTO{ 
    private final int floor ; 
    private final Direction directions; 
    private final boolean isInternal ; 
    private final int elevatorId; 



    public RequestDTO(int floor, Direction directions, boolean isInternal, int elevatorId){ 
        this.floor = floor ; 
        this.directions = directions ; 
        this.isInternal = isInternal  ;
        this.elevatorId = elevatorId ; 

    }


    // hekper functions 
    public int getFloor(){ return floor ;}
    public Direction getDirection(){ return directions ;}
    public boolean getIsInternal(){ return isInternal ;}
    public boolean isInternal(){ return isInternal ;}
    public int getElevatorId(){ return elevatorId ;}

}