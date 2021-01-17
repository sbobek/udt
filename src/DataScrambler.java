import java.util.*;

/**
 * Created by sbk on 16.09.15.
 */
public class DataScrambler {

    public static Data scrambleData(Data original, Configuration [] conf){
        LinkedList<Attribute> atts = original.getAttributes();
        LinkedList<Instance> inst = new LinkedList<Instance>();
        String name = original.getName()+"_scrambled";

        //create random for different configurations
        HashMap<Configuration,List<Integer>> idxsForScrambling = new HashMap<>();
        for(Configuration c: conf){
            idxsForScrambling.put(c,getIndices((int)(c.toScramble*original.getInstances().size()),original.getInstances().size()));
        }


        int instanceIdx = 0;
        for(Instance i : original.getInstances()){
            Instance newInstance = new Instance();
            LinkedList<Reading> scrambled = new LinkedList<>();
            for(Configuration c: conf){
                if(idxsForScrambling.get(c).contains(new Integer(instanceIdx))){
                    Reading toScramble = i.getReadingForAttribute(c.attName);
                    LinkedList<Value> scrambledReadings = new LinkedList<>();

                    //scramble, add to scrambled
                    Value bestVal = toScramble.getMostProbable();
                    scrambledReadings.add(new Value(bestVal.getName(), bestVal.getConfidence()-c.mistakeEpsilon));
                    LinkedList<Value> toBeSelected = new LinkedList<>();
                    for(Value v : toScramble.getValues()){
                        if(v == bestVal) continue;
                        if(c.uniform){
                            scrambledReadings.add(new Value(v.getName(),v.getConfidence()+c.mistakeEpsilon/(toScramble.getValues().size()-1)));
                        }else{
                            toBeSelected.add(v);
                        }
                    }

                    if(!toBeSelected.isEmpty()){
                        Random rand = new Random();
                        Value winner = toBeSelected.get(rand.nextInt(toBeSelected.size()));
                        scrambledReadings.add(new Value(winner.getName(),winner.getConfidence()+c.mistakeEpsilon));
                        toBeSelected.remove(winner);
                    }

                    scrambledReadings.addAll(toBeSelected);

                    //now, we have complete reading in scrambeled reading, add it to scrambled
                    scrambled.add(new Reading(original.getAttributeOfName(c.attName),scrambledReadings));

                }
            }

            //add scrambled and not scrambled to new instance. Remeber to keep the order of the original data
            for(Reading origReading : i.getReadings()){
                //find in scrambled
                boolean wasScrambled = false;
                for(Reading sR : scrambled){
                    if(sR.getBaseAtt().getName().equals(origReading.getBaseAtt().getName())){
                        newInstance.addReading(sR);
                        wasScrambled = true;
                        break;
                    }
                }
                if(!wasScrambled){
                    newInstance.addReading(origReading);
                }

            }


            //add instance
            inst.add(newInstance);
            instanceIdx++;
        }

        return new Data(name, atts, inst);
    }


    public static List<Integer> getIndices(int number, int range){
        ArrayList<Integer> list = new ArrayList<Integer>();
        for (int i=0; i<range; i++) {
            list.add(new Integer(i));
        }
        Collections.shuffle(list);
        return list.subList(0, number);
    }

    public static class Configuration{

        /**
         * Attribtue name which values hsa to by made unertain
         */
        String attName;

        /**
         * How much data (0-1) has to be scrambled
         */
        double toScramble;

        /**
         * By what factor the data have to be scrambled
         * In other workds, how much certainty has to be subtracted from the real value
         * and assigned to other values
         */
        double mistakeEpsilon;

        /**
         * Does the probability have to be splitted between other values uniformly,
         * or should one of the value be picked  randomly as "favorable mistake"
         */
        boolean uniform;

        public Configuration(String attName, double toScramble, double mistakeEpsilon, boolean uniform) {
            this.attName = attName;
            this.toScramble = toScramble;
            this.mistakeEpsilon = mistakeEpsilon;
            this.uniform = uniform;
        }
    }
}
