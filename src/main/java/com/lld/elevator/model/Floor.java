package com.lld.elevator.model;

public class Floor { 
    private final int floorNumber; 
    private boolean upButtonPressed ; 
    private boolean downButtonPressed ; 


    public Floor(int floorNumber){ 
        this.floorNumber = floorNumber ; 
    }

    public int getFloorNumber(){ return floorNumber ;}
    public boolean isUpButtonPressed(){ return upButtonPressed ;}
    public boolean isDownButtonPressed(){ return downButtonPressed ;}

    public void pressUp(){  this.upButtonPressed= true ;}
    public void pressDown(){  this.downButtonPressed = true ;}

    public void clearUp(){ this.upButtonPressed = false;}
    public void clearDown(){ this.downButtonPressed = false ;}




}