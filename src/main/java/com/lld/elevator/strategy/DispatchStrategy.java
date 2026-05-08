// here we have strategy to disptach the lift if the lift is going down and one more request on upco,,ing floor then it should stop to floor and load the passenger
// rather using other resource saving energy 
// using the SCAN method
package com.lld.elevator.strategy;

import com.lld.elevator.enums.Direction;
import com.lld.elevator.model.Elevator;
import java.util.List;

public interface DispatchStrategy{ 
    Elevator dispatch(List<Elevator> elevators, int floor, Direction directions, int totalFloors);
}