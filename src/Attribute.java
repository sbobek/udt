import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Created by sbk on 14.09.15.
 */
public class Attribute {
    private String name;
    private HashSet<String> domain;

    public Attribute(String name, HashSet<String> domain) {
        this.domain  = domain;
        this.name = name;
    }



    public void addValue(String value){
        domain.add(value);
    }

    public String getName() {
        return name;
    }

    public HashSet<String> getDomain() {
        return domain;
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
}
