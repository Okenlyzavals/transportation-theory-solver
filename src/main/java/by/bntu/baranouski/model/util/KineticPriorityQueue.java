package by.bntu.baranouski.model.util;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

public class KineticPriorityQueue<T> {
    private final List<T> base;
    private final Function<T, Integer> weightFunction;

    public KineticPriorityQueue(Function<T, Integer> weightFunction){
        this.base = new LinkedList<>();
        this.weightFunction = weightFunction;
    }

    public KineticPriorityQueue(Function<T, Integer> weightFunction, Collection<T> from){
        this.base = new LinkedList<>(from);
        this.weightFunction = weightFunction;
    }

    public T popNext(Integer immediatePollWeight){
        T result = null;
        int currentWeight = Integer.MIN_VALUE;
        for (T value : base){
            Integer valueWeight = weightFunction.apply(value);
            if (valueWeight >= immediatePollWeight){
                result = value;
                break;
            } else if (valueWeight > currentWeight){
                currentWeight = valueWeight;
                result = value;
            }
        }
        base.remove(result);
        return result;
    }

    public int size(){
        return base.size();
    }
}
