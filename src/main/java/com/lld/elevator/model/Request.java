package com.lld.elevator.model;

import com.lld.elevator.enums.Direction ;
public class Request{ 
    private final int floor ; 
    private final Direction directions; 
    private final boolean isInternal;


    public Request(int floor, Direction directions, boolean isInternal){ 
        this.floor = floor; 
        this.directions = directions;
        this.isInternal = isInternal ; 
    }

    public int getFloor(){ return floor ;}
    public Direction getDirection(){ return directions;}
    public boolean isInternal(){ return isInternal;}


    @Override
    public String toString(){ 
        return String.format("Request[F%d %s %s]", floor, directions, isInternal ? "internal": "external");
    }
}