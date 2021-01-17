import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

/**
 * Created by sbk on 16.09.15.
 */
public class TreeEvaluator {

    public static BenchmarkResult trainAndTest(Data trainingData, Data testData){
        Tree trainedTree = UId3.growTree(trainingData,new UncertainEntropyEvaluator(),0);
        return test(trainedTree,testData);

    }

    public static BenchmarkResult test(Tree trainedTree, Data testData){
        BenchmarkResult result = new BenchmarkResult(testData.getClassAttribute());

        for(Instance i : testData.getInstances()){
            AttStats prediction = trainedTree.predict(i);
            boolean error = !prediction.getMostPorbable().getName().equals(i.getReadings().getLast().getMostProbable().getName());
            result.addPrediction(new Prediction(prediction,i.getReadings().getLast().getMostProbable().getName()));
            if(error){
                //give false positive to predicted class, false negative to real class, and true negatives to other
                String predictedName = prediction.getMostPorbable().getName();
                String realName = i.getReadings().getLast().getMostProbable().getName();
                result.addFP(predictedName);
                result.addFN(realName);
                for(Stats s:result.stats){
                    if(!s.classLabel.equals(predictedName) && !s.classLabel.equals(realName)){
                        result.addTN(s.getClassLabel());
                    }
                }
                result.incorrect++;
            }else{
                //add true positive for predicted class, and true negatives for other
                String predictedName = prediction.getMostPorbable().getName();
                result.addTP(predictedName);
                for(Stats s:result.stats){
                    if(!s.classLabel.equals(predictedName)){
                        result.addTN(s.getClassLabel());
                    }
                }
                result.correct++;
            }
        }

        return result;
    }

    public static class BenchmarkResult{
        private double correct;
        private double incorrect;
        private LinkedList<Stats> stats;
        private LinkedList<Prediction> predictions;

        BenchmarkResult(Attribute classAttribute){
            stats = new LinkedList<Stats>();
            predictions  = new LinkedList<>();
            for(String classLabel : classAttribute.getDomain()){
                stats.add(new Stats(classLabel));
            }
        }

        public LinkedList<Prediction> getPredictions() {
            return predictions;
        }


        public double getAccuracy(){
            return correct/(correct+incorrect);
        }


        public Stats getStatsForLabel(String classLabel){
            for(Stats s : stats){
                if(s.getClassLabel().equals(classLabel)){
                    return s;
                }
            }
            return null;
        }

        void addTP(String value){
            for(Stats s : stats){
                if(s.getClassLabel().equals(value)){
                    s.setTP(s.getTP() + 1);
                    break;
                }
            }
        }

        void addFP(String value){
            for(Stats s : stats){
                if(s.getClassLabel().equals(value)){
                    s.setFP(s.getFP() + 1);
                    break;
                }
            }

        }

        void addTN(String value){
            for(Stats s : stats){
                if(s.getClassLabel().equals(value)){
                    s.setTN(s.getTN() + 1);
                    break;
                }
            }

        }

        void addFN(String value){
            for(Stats s : stats){
                if(s.getClassLabel().equals(value)){
                    s.setFN(s.getFN() + 1);
                    break;
                }
            }
        }

        public void addPrediction(Prediction prediction) {
            predictions.add(prediction);
        }
    }


    private static class Prediction{
        AttStats prediction;
        String correctLabel;

        public Prediction(AttStats prediction, String correctLabel){
            this.prediction = prediction;
            this.correctLabel = correctLabel;
        }

    }

    public static class Stats{
        private String classLabel;
        private double TP;
        private double FP;
        private double TN;
        private double FN;

        public double getTPRate(){
            return getTP()/(getTP()+getFN());
        }

        public double getFPRate(){
            return getFP()/(getFP()+getTN());
        }

        public double getPrecision(){
            return getTP()/(getTP()+getFP());
        }
        public double getRecall(){
            return getTP()/(getTP()+getFN());
        }
        public double getFMeasure(){
            return 2* (getPrecision()*getRecall())/(getPrecision()+getRecall());
        }
        public double getROCArea(BenchmarkResult bench){
            double  result = 0;
            //first sort the predictions according to probability of
            LinkedList<Prediction> preds = bench.getPredictions();
            Collections.sort(preds, new Comparator() {
                @Override
                public int compare(Object o, Object t1) {
                    //sort according to class label treated as U_1
                    Prediction p1 = (Prediction) o;
                    Prediction p2 = (Prediction) t1;

                    double prob1 = p1.prediction.getStatForValue(classLabel);
                    double prob2 = p2.prediction.getStatForValue(classLabel);
                    ;

                    if (prob1 > prob2) {
                        return 1;
                    } else if (prob1 < prob2) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            });
            //calculate times the class label was beaten by other class
            //in other words cout number of other classes, and add the number of them to list
            // any time the class label appears

            int nClassCount = 0;
            int yClassCount = 0;
            double Uy  = 0;
            double Un = 0;
            for(Prediction p : preds){
               if(p.correctLabel.equals(classLabel)){
                   Uy += nClassCount;
                   yClassCount++;
               }else{
                   Un += yClassCount;
                   nClassCount++;
               }
            }

            result = Uy/(Uy+Un);

            return result;

        }

        public Stats(String classLabel){
            this.classLabel = classLabel;
        }

        public String getClassLabel() {
            return classLabel;
        }

        public void setClassLabel(String classLabel) {
            this.classLabel = classLabel;
        }

        public double getTP() {
            return TP;
        }

        public void setTP(double TP) {
            this.TP = TP;
        }

        public double getFP() {
            return FP;
        }

        public void setFP(double FP) {
            this.FP = FP;
        }

        public double getTN() {
            return TN;
        }

        public void setTN(double TN) {
            this.TN = TN;
        }

        public double getFN() {
            return FN;
        }

        public void setFN(double FN) {
            this.FN = FN;
        }
    }
}
