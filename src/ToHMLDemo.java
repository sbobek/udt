import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

/**
 * Created by sbk on 28.09.15.
 */
public class ToHMLDemo {
    public static void main(String[] argv) {
        try {
            Data odata = Data.parseUArff("./resources/dataset-mobile-usage.arff", 0);
            Data data = DataScrambler.scrambleData(odata, new DataScrambler.Configuration [] {
                    new DataScrambler.Configuration("activity",0.4,0.55,false),
                    new DataScrambler.Configuration("location",0.3,0.3,false)
            });
            Tree result = UId3.growTree(data,new UncertainEntropyEvaluator(),0);


            result.saveHML("/home/sbk/Pulpit/tree.hml");
            result.saveDot("/home/sbk/Pulpit/tree.dot");


        } catch (ParseException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

}
