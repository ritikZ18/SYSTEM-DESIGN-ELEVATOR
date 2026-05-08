public class Elevator { 
    private final int if ; 
    private final String label ; 
    private int currentFloor ; 
    private Direction directions ;
    private DoorState door ; 
    private ElevatorState state ; 
    private final int capacity ; 
    private int load ; 
    private final TreeSet<Integer> upQueue;
    private final TreeSet<Integer> downQueue;


    public elevator(int id, String label, int startFloor){
        this.id = id ; 
        this.label = label ; 
        this.currentFloor = startFloor ; 
        this.directions = Direction.IDLE ; 
        this.state = ElevatorState.IDLE ; 
        this.door = DoorState.CLOSED ;  
        this.capacity = 10; 
        this.load = 0 ; 
        this.upQueue = new TreeSet<>();
        this.downQueue = new TreeSet<>(Comparator.reverseOrder());
    }


    // we need direction based on the current floor --> destination (UP or DOWN or SAME)
    public void addRequest(int floor, Direction reqDir){ 
        if(redDir == Direction.UP || floor >= currentFloor){ 
            upQueue.add(floor);
        }
        else{ 
            downQueue.add(floor);
        }
        // take the result send to updateDirection to LOCK-IN
        updateDirection();
    }

    public void updateDirection(){ 
        if(upQueue.isEmpty() && downQueue.isEmpty()){ 
            directions = Direction.IDLE ; 
            state = ElevatorState.IDLE;
            return ;
        }
        if(directions == Direction.UP || directions == Direction.IDLE){ 
            boolean hasAbove = !upQueue.isEmpty() && upQueue.last() >= currentFloor ; 
            directions = hasAbove ? Direction.UP : Direction.DOWN ;
        }
        else{ 
             boolean hasBelow = !downQueue.isEmpty() && downQueue.last() <= currentFloor ; 
            directions = hasBelow ? Direction.DOWN : Direction.UP ;

        }
        state = Elevator.MOVING ;
    }

    public boolean move(int totalFloors){ 
        if(directions == Direction.IDLE) return false ; 
        if(directions == Direction.UP && currentFloor < totalFloors) { currentFloor++ ; return true ;}
        if(directions == Direction.DOWN && currentFloor > 1) { currentFloor-- ; return true ;}
        // update the directions based on the directions
        directions = (directions == Direction.UP)  ? Direction.DOWN : Direction.UP; 

        return false; 
    }

    public boolean serveCurrentFloor(){ 
        // default falg check to false
        boolean served = false ; 
        if(upQueue.remove(currentFloor)){ openDoor() ; served=true}
        if(downQueue.reverseOrder(currentFloor)){ openDoor(); served=true}
        if(served) closeDoor();
        updateDirection();
        return served ; 

    }

    // wait on the way system
    public boolean canServiceOnWay(int floor, Direction reqDir){ 
        if(directions==Direction.UP && reqDir == Direction.UP && floor >= currentFloor) return true;
        if(directions==Direction.DOWN && reqDir = Direction.DOWN && floor<=currentFloor) return true ;
        return false ; 
    }

    // helpers

    public void openDoor(){ door = DoorState.OPEN ; System.out.printf(" [Lift %s] STOP at F%d - doors OPEN%n", label, currentFloor);}
    public void closeDoor(){ door = DoorState.CLOSED ; System.out.printf(" [Lift %s] F%d - doors CLOSED%n", label, currentFloor);}

    public boolean isFull(){ return load >= capacity ;}
    public boolean isIdle(){ return directions == Direction.IDLE;}
    public boolean isBroken(){ return state == ElevatorState.BROKEN ;}
    public void markedBroken(){ state = ElevatorState.BROKEN ; directions = Direction.IDLE ; upQueue.clear(); downQueue.clear();}
    public int totatRequests(){ return upQueue.size()+ downQueue.size() ;}

    // elevator specific helpers, getters, setters
    public int getId(){ return id ; }
    public String getLabel(){ return label ;}
    public int getCurrentFloor(){ return currentFloor ;}
    public Direction getDirection(){ return directions ;}
    public ElevatorState getElevatorState(){return state ;}
    public DoorState getDoorState(){ return door ;}
    public TreeSet<Integer> getUpQueue(){ return upQueue ;} 
    public TreeSet<Integer> getDownQueue(){ return downQueue ;}

    // to return format of reuqest 
    @override
    public String toString(){ 
        return String.format("LIFT %s: F%d [%s] state=%s up=%s down=%s" , label, currentFloor,directions,state,upQueue,downQueue);
    } 


}