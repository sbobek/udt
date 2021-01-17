import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * Created by sbk on 13.09.15.
 */
public class UId3 {

    private static final int NODE_SIZE_LIMIT = 1;
    private static final int TREE_DEPTH_LIMIT = 2;
    private static final double GROW_CONFIDENCE_THRESHOLD = 0;//0.002;

    public static Tree growTree(Data data, EntropyEvaluator entropyEvaluator, int depth){
        //if(data.getInstances().isEmpty()) return null;
        if(data.getInstances().size() < NODE_SIZE_LIMIT) return null;
        if(depth > TREE_DEPTH_LIMIT) return null;
        double entropy = entropyEvaluator.calculateEntropy(data);

        // of the set is heterogeneous or no attributes to split, just class -- return leaf
        if(entropy == 0 || data.getAttributes().size() == 1 ){
            // create the only node and summary for it
            Attribute classAtt = data.getClassAttribute();
            TreeNode root = new TreeNode(classAtt.getName(),data.calculateStatistics(classAtt));
            Tree  result = new Tree(root);
            return result;

        }
        double infoGain = 0;
        Attribute bestSplit = null;
        for(Attribute a : data.getAttributes()){
            if(data.getClassAttribute().equals(a)) continue;
            HashSet<String> values = a.getDomain();
            double tempGain = entropy;
            AttStats stats = data.calculateStatistics(a);
            for(String v : values){
                Data subdata = data.filterAttributeValue(a,v);
                tempGain -= stats.getStatForValue(v)*new UncertainEntropyEvaluator().calculateEntropy(subdata);
            }

            if(tempGain >= infoGain){
                infoGain = tempGain;
                bestSplit = a;
            }

        }

        // if nothing better can happen
        if(bestSplit == null){
            // create the only node and summary for it
            Attribute classAtt = data.getClassAttribute();
            TreeNode root = new TreeNode(classAtt.getName(),data.calculateStatistics(classAtt));
            Tree  result = new Tree(root);
            return result;

        }

        //Create root node, and recursively go deeper into the tree.
        Attribute classAtt = data.getClassAttribute();
        AttStats classStats = data.calculateStatistics(classAtt);
        TreeNode root = new TreeNode(bestSplit.getName(), classStats);

        //attach newly created trees
        for(String val : bestSplit.getDomain()){
            Data newData = data.filterAttributeValue(bestSplit, val);
            Tree subtree = UId3.growTree(newData,entropyEvaluator, depth+1);
            AttStats bestSplitStats = data.calculateStatistics(bestSplit);
            if(subtree != null && bestSplitStats.getMostPorbable().getConfidence() > GROW_CONFIDENCE_THRESHOLD) {
                root.addEdge(new TreeEdge(new Value(val, bestSplitStats.getAvgConfidence()), subtree.getRoot()));
            }

        }
        if(root.getEdges().size() == 0){
            root.setAtt(data.getClassAttribute().getName());
        }

        return new Tree(root);
    }

    public static void main(String [] argv){

        try {
            //Data data = Data.parseUArff("./resources/dataset-mobile-usage.arff",0);
            //Data odata = Data.parseUArff("./resources/dataset-mobile-usage.arff",0);
            //Data data = Data.parseUArff("./resources/soybean.arff");
            //Data data = Data.parseUArff("./resources/S_affective_alluncertain_train.arff");
            //Data test = Data.parseUArff("./resources/S_affective_alluncertain_test.arff");

           // Data data = Data.parseUArff("./resources/weather.nominal.arff");
           // Data test = Data.parseUArff("./resources/weather.nominal.arff");


           Data data = Data.parseUArff("./resources/machine.nominal.uncertain.arff");
            Data test = Data.parseUArff("./resources/machine.nominal.uncertain.arff");

            //Data data = Data.parseUArff("./resources/soybean.arff");
           // Data test = Data.parseUArff("./resources/soybean.arff");

           /* data = DataScrambler.scrambleData(data, new DataScrambler.Configuration [] {
                    new DataScrambler.Configuration("skinTemp",0.02,0.85,true),
                    new DataScrambler.Configuration("BPMtrend",0.01,0.22,false)
                   // new DataScrambler.Configuration("bloodPressure",0.8,0.24,false),
                  //  new DataScrambler.Configuration("emotion",0.3,0.14,false)
            });
*/
            Tree t = growTree(data, new UncertainEntropyEvaluator(),0);
            TreeEvaluator.BenchmarkResult br = TreeEvaluator.trainAndTest(data, test);

            System.out.println("###############################################################");
            System.out.println("Correctly classified instances: "+br.getAccuracy()*100+"%");
            System.out.println("Incorrectly classified instances: "+(1-br.getAccuracy())*100+"%");
            System.out.println(String.format("%-12s %-12s %-12s %-12s %-12s %-12s %-12s ",
                    "TP Rate", "FP Rate", "Precision", "Recall", "F-Measure", "ROC Area", "Class"));

            for(String classLabel : data.getClassAttribute().getDomain()) {
                TreeEvaluator.Stats cs = br.getStatsForLabel(classLabel);
                System.out.println(String.format("%-12f %-12f %-12f %-12f %-12f %-12f %-12s ",
                        cs.getTPRate(),cs.getFPRate(),cs.getPrecision(),cs.getRecall(),cs.getFMeasure(),cs.getROCArea(br),cs.getClassLabel()
                        ));
            }

            t.saveDot("/home/sbk/Pulpit/tree.dot");
            t.saveHML("/home/sbk/Pulpit/tree-n.hml");
            t.saveHMR("/home/sbk/Pulpit/tree.hmr");

            ///For eval train-hoeffding
            for(String val : data.getAttributeOfName("hour").getDomain()){
                String prt = val+",";
                Reading nr =  Reading.parseReading(data.getAttributeOfName("hour"),val);
                LinkedList<Reading> reads = new LinkedList<>();
                reads.add(nr);
                Value vv = t.predict(new Instance(reads)).getMostPorbable();
                prt += vv.getName()+","+vv.getConfidence();
                System.out.println(prt);

            }

            boolean  benchmark= false;
            if(!benchmark) return;
           /**********************************************************************/
           /************************ BENCHMARK ***********************************/
            //All attributes, 5:1:60 percent, mistake (by 0.6), not uniform, most probable
            DataScrambler.Configuration conf [] = new DataScrambler.Configuration[data.getAttributes().size()/2+1];
            //Save all statistics in cvs in order: classic, uncertain
            PrintWriter writer = null;
            try {
                writer = new PrintWriter("stats.csv", "UTF-8");
                writer.println("correctClassic,incorrectClassic, correctUncertain, incorrectUncertain");
                for(int p = 0; p < 99; p++){
                    for(int i  = 0; i < data.getAttributes().size()/2;i++){
                        conf[i]= new DataScrambler.Configuration(data.getAttributes().get(i).getName(), p/100.0, 0.75, false);
                    }
                    conf[conf.length-1]= new DataScrambler.Configuration(data.getClassAttribute().getName(), p/100.0, 0.75, false);
                    System.out.println("Scrambling sample set "+(p+1));
                    Data scrambled = DataScrambler.scrambleData(data,conf);
                    Data classic = Data.parseUArffFromString(scrambled.toArffMostProbable());

                    System.out.println("Training...");
                    Tree uncertainTree = UId3.growTree(scrambled,new UncertainEntropyEvaluator(),0);
                    Tree classicTree  = UId3.growTree(classic,new UncertainEntropyEvaluator(),0);

                    System.out.println(uncertainTree.toString());
                    System.out.println(classicTree.toString());

                    System.out.println("Benchmarking...");
                    TreeEvaluator.BenchmarkResult brClassic = TreeEvaluator.test(classicTree, data);
                    TreeEvaluator.BenchmarkResult brUncertain = TreeEvaluator.test(uncertainTree,data);

                    System.out.println("Saving results...");
                    writer.println(brClassic.getAccuracy()+","+(1-brClassic.getAccuracy())+","+brUncertain.getAccuracy()+","+(1-brUncertain.getAccuracy()));


                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                throw e;
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                throw e;
            }

            finally {
                if(writer != null){
                    writer.close();
                }
            }

            System.out.println("Finished!");



           /**********************************************************************/


        } catch (ParseException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

}
