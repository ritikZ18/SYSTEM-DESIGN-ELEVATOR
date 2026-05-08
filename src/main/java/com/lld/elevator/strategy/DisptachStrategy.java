// here we have strategy to disptach the lift if the lift is going down and one more request on upco,,ing floor then it should stop to floor and load the passenger
// rather using other resource saving energy 
// using the SCAN method

public interface DispatchStrategy{ 
    Elevator disptach(List<Elevator> elevators, int floor, Direction directions, int totalFloors);
}