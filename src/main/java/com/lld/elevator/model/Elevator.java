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
}