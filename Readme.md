This is the project related to the elevator system 
1. we need to fix the direction of the lift 
    a. UP
    b. DOWN 
    c. IDLE 

2. Door State : 
    a. OPEN
    b. CLOSE 

3. To AVOID Resorue burn : 
    Before assigning, check: is any lift already going DOWN and will pass this floor?
    If yes → add to THAT lift's stops instead of dispatching a new one

4. SCAN algorithm elevator movesin one-direction serves all request on the way, reverses at the end 

5. we can use the TreeSet Queues (UP & DOWN) for next stop lookup

6. strategy pattern dispatch pattern, to inject the Lift is dispatched algorithkm 

7. using SOLID patterns 
