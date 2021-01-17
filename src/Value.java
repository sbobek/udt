import java.util.LinkedList;

/**
 * Created by sbk on 14.09.15.
 */
public class Value {
    private String name;
    private double confidence;


    public Value(String name, double confidence) {
        this.confidence = confidence;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public double getConfidence() {
        return confidence;
    }

    @Override
    public String toString() {
        return getName()+"["+Math.round(getConfidence()*100.0)/100.0+"]";
    }

    @Override
    public boolean equals(Object obj) {
        Value other  = (Value)obj;
        return name.equals(other.getName());

    }
}
