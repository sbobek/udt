import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Created by sbk on 14.09.15.
 */
public class Reading {

    private Attribute baseAtt;
    private LinkedList<Value> values;

    public Attribute getBaseAtt() {
        return baseAtt;
    }

    public LinkedList<Value> getValues() {
        return values;
    }


    protected Reading (Attribute baseAtt, LinkedList<Value> values){
        this.baseAtt = baseAtt;
        this.values = values;
    }

    public Value getMostProbable(){
        Value max = null;
        for(Value v : values){
            if(max  == null || max.getConfidence() < v.getConfidence()){
                max = v;
            }
        }

        return max;
    }


    @Override
    public String toString() {
        String result = "";
        Iterator<Value> it = values.iterator();
        while(it.hasNext()){
            Value v = it.next();
            result += v.getName()+"["+v.getConfidence()+"]";
            if(it.hasNext()) result +=";";
        }
        return result;
    }

    /**
     * The method parse the reading which has to be formated int the following way:
     * v1[probability];v2[probability];...;vn[probability]
     * The number of values has to correspond to the soze of the domain of baseAtt.
     * In case the reading does not cover all the values, remaining values are assigned probability
     * accoriding to uniform distribution.
     *
     *
     * @param baseAtt the attribute for which the reading is made
     * @param readingDef the reading definition
     * @return the reading
     */
    public static Reading parseReading(Attribute baseAtt, String readingDef) throws ParseException {
        String [] vals = readingDef.replaceAll(" ","").split(";");
        LinkedList<Value> values = new LinkedList<>();
        double totalProb = 0;

        for(String v : vals){
            String []valProb = v.split("[\\[\\]]");
            String name = valProb[0].trim();
            double confidence = 1;
            if(name.equals("?")) break;
            if(valProb.length > 1) {
                confidence = Double.parseDouble(valProb[1].trim());
            }
            values.add(new Value(name, confidence));
            totalProb += confidence;
        }

        if(totalProb > 1) throw new ParseException("Probability greater than 1 in "+readingDef);

        //check if there are some missing values to assign them uniform distribution
        LinkedList<String> valNames = new LinkedList<>();
        for(Value v : values){
             valNames.add(v.getName());
        }

        HashSet<String> remaining  = ((HashSet)baseAtt.getDomain().clone());
        remaining.removeAll(valNames);

        //find out if there is any probability left for missing values, if any
        if(!remaining.isEmpty()){
            double uniformProb = (1-totalProb)/remaining.size();
            for(String rv : remaining){
                values.add(new Value(rv,uniformProb));
            }
        }


        return new Reading(baseAtt,values);
    }
}
