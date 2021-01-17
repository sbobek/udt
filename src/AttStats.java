import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Created by sbk on 15.09.15.
 */
public class AttStats {
    private ArrayList<Value> statistics;
    private double avgConfidence;

    private AttStats(ArrayList<Value> statistics, double avgConfidence){
        this.statistics = statistics;
        this.avgConfidence = avgConfidence;
    }

    public static AttStats getStatistics(Attribute att, Data data){
        ArrayList<Value> sum = new ArrayList<>();
        for(String valName : att.getDomain()){
            sum.add(new Value(valName, 0));
        }
        double avgConf = 0;

        if(data.getInstances().isEmpty()){
            return new AttStats(sum, avgConf);
        }

        Iterator<Instance> it  = data.getInstances().iterator();
        while(it.hasNext()){
            Reading r = it.next().getReadingForAttribute(att.getName());
            for(Value v : r.getValues()){
                int idx  = sum.indexOf(v);
                Value old  = sum.get(idx);
                sum.remove(old);

                sum.add(new Value(v.getName(), old.getConfidence()+v.getConfidence()));
            }
            avgConf += r.getMostProbable().getConfidence();
        }

        int size = data.getInstances().size();
        avgConf /= size;

        ArrayList<Value> stats = new ArrayList<>();
        for(Value statV : sum){
            stats.add(new Value(statV.getName(), statV.getConfidence()/size));
        }
        return new AttStats(stats, avgConf);
    }


    public double getAvgConfidence() {
        return avgConfidence;
    }


    public ArrayList<Value> getStatistics() {
        return statistics;
    }

    public double getStatForValue(String valueName){
        for(Value v : statistics){
            if(v.getName().equals(valueName)){
                return v.getConfidence();
            }
        }
        return 0;
    }

    public Value getMostPorbable(){
        Iterator<Value> it = statistics.iterator();
        Value result = it.next();
        while(it.hasNext()){
            Value temp = it.next();
            if(result.getConfidence() < temp.getConfidence()){
                result = temp;
            }
        }
        return result;
    }

    @Override
    public String toString() {
        String result = "{";
        Iterator<Value> it = statistics.iterator();
        while(it.hasNext()){
            result += it.next().toString();
            if(it.hasNext()) result +=",";
        }
        result += "}";
        return result;
    }
}
