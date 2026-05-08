package com.lld.elevator.repository;
import com.lld.elevator.model.Elevator;
import java.util.*;
public class ElevatorRepository{ 

private final Map<Integer, Elevator> store = new LinkedHashMap<>(); 

public void save(Elevator e){ store.put(e.getId(), e) ;}
public Elevator findById(int id){ return store.get(id); }
public List<Elevator> findAll() { return new ArrayList<>(store.values());}
public void remove(int id) { store.remove(id);}
public boolean exists(int id) { return store.containsKey(id) ; }
public int count() { return store.size();}


}