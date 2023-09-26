package org.example.kit.entity;

/**
 * @author bonult
 */
public class BiSupplier<F, S> {
    private F first;
    private S second;
    public BiSupplier(F first, S second){
        this.first = first;
        this.second = second;
    }
    public F first(){
        return first;
    }
    public S second(){
        return second;
    }
}
