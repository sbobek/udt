/**
 * Created by sbk on 15.09.15.
 */
public class UncertainEntropyEvaluator implements EntropyEvaluator {

    /**
     * It calculates classical entropy without taking into consideration uncertain values
     * The last element of the instance is always considered to be a class label
     *
     * @param data instances among which the entropy is calculated
     * @return
     */
    @Override
    public double calculateEntropy(Data data) {
        Attribute classAtt = data.getAttributes().getLast();
        AttStats probs = data.calculateStatistics(classAtt);
        double entropy = 0;
        for(Value v : probs.getStatistics()){
            if(v.getConfidence() == 0) continue;
            entropy -= v.getConfidence()*Math.log(v.getConfidence())/Math.log(2.0);

        }

        return entropy;
    }



}
