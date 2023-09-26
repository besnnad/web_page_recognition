package org.example.kit;

import java.util.Objects;

public final class ArrayKit {

    public static <T> boolean in(T object, T[] objs){
        if(objs == null || objs.length == 0){
            return false;
        }
        for(T obj : objs){
            if(Objects.equals(object, obj))
                return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public static <T> T getInstance(Class<? super T> clazz, Object[] objs){
        Assert.notNull(clazz);
        for(Object obj : objs){
            if(obj != null && clazz.isAssignableFrom(obj.getClass()))
                return (T)obj;
        }
        return null;
    }

    public static <T> T getFirst(T[] objs){
        if(objs == null || objs.length == 0)
            return null;
        return objs[0];
    }

    public static <T> int indexOf(T[] arr, T target){
        for(int i = 0; i < arr.length; i++){
            if(arr[i] == target)
                return i;
        }
        return -1;
    }

    public static <T> int indexOf(T[] arr, T target, int fromIndex){
        for(int i = fromIndex; i < arr.length; i++){
            if(arr[i] == target)
                return i;
        }
        return -1;
    }
}
