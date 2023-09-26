package org.example.kit;

/**
 * Created by bonult on 2017/8/2.
 */
public class Assert {
    private Assert(){
    }

    /**
     * check if an object is null, if not, throw NullPointerException.
     *
     * @param t the object to check
     */
    public static void notNull(Object t){
        if(t == null)
            throw new NullPointerException();
    }

    public static void isNull(Object object){
        if(object != null)
            throw new IllegalArgumentException();
    }

    public static void notNull(Object t, String msg){
        if(t == null)
            throw new NullPointerException(msg);
    }

    public static void isNull(Object object, String msg){
        if(object != null)
            throw new IllegalArgumentException(msg);
    }

    public static void isTrue(boolean expression){
        if(!expression)
            throw new IllegalArgumentException();
    }

    public static void isTrue(boolean expression, String msg){
        if(!expression)
            throw new IllegalArgumentException(msg);
    }

    public static void isFalse(boolean expression){
        if(expression)
            throw new IllegalArgumentException();
    }

    public static void isFalse(boolean expression, String msg){
        if(expression)
            throw new IllegalArgumentException(msg);
    }

}
