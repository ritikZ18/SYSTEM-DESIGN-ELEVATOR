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

    public void pressUp(){ return this.upButtonPressed= true ;}
    public void pressDown(){ return this.downButtonPressed = true ;}

    public void clearUp(){ return this.upButtonPressed = false;}
    public void clearDown(){ return this.downButtonPressed = false ;}




}