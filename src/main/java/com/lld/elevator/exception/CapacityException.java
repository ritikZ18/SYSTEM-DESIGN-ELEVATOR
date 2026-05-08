public class CapacityException extends ElevatorException { 
    public CapacityException( int elevatorId){ 
        super("Elevator " + elevatorId + " is at full capacity");
    }
}