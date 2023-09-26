package org.example.kit;

import java.util.Collection;

public class CollectionKit {

    public static long[] toLongs(Collection<Long> collection){
        if(collection == null)
            return null;
        long[] arr = new long[collection.size()];
        int i = 0;
        for(Long c : collection){
            arr[i++] = c;
        }
        return arr;
    }

    public static int[] toInts(Collection<Integer> collection){
        if(collection == null)
            return null;
        int[] arr = new int[collection.size()];
        int i = 0;
        for(Integer c : collection){
            arr[i++] = c;
        }
        return arr;
    }

}
