import java.util.Iterator;
import java.util.LinkedList;

/**
 * Created by sbk on 14.09.15.
 */
public class Instance{
private LinkedList<Reading> readings;

    public Instance(){
        readings = new LinkedList<>();
    }

    public Instance(LinkedList<Reading> readings){
        this.readings = readings;
    }

    public LinkedList<Reading> getReadings() {
        return readings;
    }

    public Reading getReadingForAttribute(String attName){
        for(Reading r : readings){
            if(r.getBaseAtt().getName().equals(attName)){
                return r;
            }
        }
        return null;
    }

    public void setReadings(LinkedList<Reading> readings) {
        this.readings = readings;
    }

    public void addReading(Reading r){
        readings.add(r);
    }


    public String toArff() {
        String result = "";
        Iterator<Reading> it = readings.iterator();
        while(it.hasNext()){
            result += it.next().toString();
            if(it.hasNext()) result += ",";
        }
        return result+"\n";
    }
}
