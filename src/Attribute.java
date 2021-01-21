import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Created by sbk on 14.09.15.
 */
public class Attribute {
    public static final int TYPE_NOMINAL = 1;
    public static final int TYPE_NUMERICAL = 2;


    private String name;
    private HashSet<String> domain;
    private int type = TYPE_NOMINAL;
    private String valueToSplitOn;
    private double infoGain;

    public Attribute(String name, HashSet<String> domain) {
        this.domain  = domain;
        this.name = name;
    }

    public Attribute(String name, HashSet<String> domain, int type) {
        this.domain  = domain;
        this.name = name;
        this.type = type;
    }



    public void addValue(String value){
        domain.add(value);
    }

    public String getName() {
        return name;
    }

    void setDomain(HashSet<String> domain){
        this.domain = domain;
    }

    public int getType() {
        return type;
    }


    public HashSet<String> getDomain() {
        return domain;
    }

    public HashSet<String> getSpittableDomain() {
        if(getType() == Attribute.TYPE_NOMINAL) {
            return domain;
        }else if(getType() == Attribute.TYPE_NUMERICAL){
            HashSet<String> splittingDomain = new HashSet<>();
            splittingDomain.add(getValueToSplitOn());
            return splittingDomain;
        }
        return new HashSet<>();
    }

    private String getValueToSplitOn() {
        return valueToSplitOn;
    }


    public String toArff() {
        String result = "@attribute "+name+" {";
        Iterator<String> it = domain.iterator();

        while(it.hasNext()){
            result += it.next();
            if(it.hasNext()) result += ",";
        }

        result += "}";

        return result;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public boolean equals(Object obj) {
        String name = null;
        if(obj instanceof  Attribute)
            name = ((Attribute) obj).getName();
        else if(obj instanceof String)
            name = (String) obj;

        return this.getName().equals(name);
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    public void setValueToSplitOn(String v) {
        this.valueToSplitOn = v;
    }

    public void setImportanceGain(double infoGain) {
        this.infoGain = infoGain;
    }

    public double getImportanceGain() {
        return infoGain;
    }
}
