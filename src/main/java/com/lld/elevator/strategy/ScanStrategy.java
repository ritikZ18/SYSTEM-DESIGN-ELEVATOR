// this method implements he DispatchStrategy, with made consolidations cases 

public class ScanStrategy implements DispatchStrategy { 


    @override 
    public Elevator disptach(List<Elevator> elevators, int floor, Direction directions, int totalFloors){ 
        // step 1 : any lift heading same way passing floors?
        Elevator onWay = elevators.stream()
                                  .filter(e -> !e.isFull() && !e.isBroken())
                                  .filter(e -> e.canServiceOnWay(floor,directions))
                                  .min(Comparator.comparingInt(e -> Math.abs(e.getCurrentFloor()-floor)))\
                                  .orElse(null);

        if( onWay != null){ 
            System.out.printf(" [SCAN] consolidating with Lift %s%n", onWay.getLabel());
            return onWay ;
        }

        // STEP 2 :score all lifts, pick lowest
        return elevators.stream()
                        .filter(e -> !e.isFull() && !e.isBroken())
                        .min(Comparator.comparingInt(e-> score(e,floor,directions,totalFloors)))
                        .orElseThrow(()-> new RuntimeException("No Available Elevator"));


    } 

    //  this logic to place miniun score on the lift for floors 
    private int score(Elevator e, int floor, Direction dir, int totalFloors){ 

        int distance = Math.abs(e.getCurrentFloor()- floor);
        if(e.isIdle()) return distance ; 
        if(e.canServiceOnWay(floor,dir)) return distance;
        return distanec + totalFloors; //  CoW style penalty for wrong directions to avoid miscalcualtions
    }
}