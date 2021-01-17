import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * Created by sbk on 14.09.15.
 */
public class Tree {
    private TreeNode root;

    public Tree(TreeNode root){
        setRoot(root);
    }
    public TreeNode getRoot() {
        return root;
    }

    public void setRoot(TreeNode root){
        this.root = root;
    }

    /**
     * Predicts a value of a class attribute, given the instance.
     * If the instance contains class value it wil be ignored
     * For calculating error, please refer to {@link #error(Instance)}
     * @param i the instance
     * @return the classification probability distribution
     */
    public AttStats predict(Instance i){
        TreeNode testNode = getRoot();
        while(!testNode.isLeaf()){
            //check which path should we go down
            String attToTest = testNode.getAtt();
            Reading r = i.getReadingForAttribute(attToTest);
            Value mostProbable = r.getMostProbable();

            //follow the edge with the mostProbable value
            TreeNode newNode = null;
            for(TreeEdge te : testNode.getEdges()){
                if(te.getValue().getName().equals(mostProbable.getName())){
                    newNode = te.getChild();
                    break;
                }
            }

            if(newNode != null){
                testNode = newNode;
            }else{
                break;
            }

        }

        return testNode.getStats();
    }

    /**
     * Checks whether the prediction of the tree given an instance is correct.
     *
     * @param i the reading with the class value at the last position
     * @return true of false
     */
    public boolean error(Instance i){
        AttStats result = predict(i);

        return result.getMostPorbable().getName().equals(i.getReadings().getLast().getMostProbable().getName());
    }

    public void saveHML(String filename) throws FileNotFoundException, UnsupportedEncodingException {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(filename, "UTF-8");
            writer.println(toHML());

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw e;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw e;
        }

        finally {
            if(writer != null) {
                writer.close();
            }
        }
    }

    public String toHML(){
        String result = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<hml version=\"2.0\">";


        //types are defined by atts domains
        HashSet<Attribute> atts = getAttributes();
        result += "<types>\n";
        for(Attribute att : atts){
            result += "<type id=\"tpe_"+att.getName()+"\" name=\""+att.getName()+"\" base=\"symbolic\">\n";
            result += "<domain>\n";
            for(String v : att.getDomain()){
                result += "<value is=\""+v+"\"/>\n";
            }
            result += "</domain>\n";
            result += "</type>\n";
        }

        result += "</types>\n";
        //attributes

        result += "<attributes>\n";
        for(Attribute att : atts){
            result += "<attr id=\""+att.getName()+"\" " +
                    "type=\"tpe_"+att.getName()+"\" " +
                    "name=\""+att.getName()+"\" " +
                    "clb=\" \" " +
                    "abbrev=\""+att.getName()+"\" " +
                    "class=\"simple\" comm=\"io\"/>\n";
        }
        result += "</attributes>\n";

        //tables and rules
        result +="<xtt>\n";


        result += "<table id=\"id_"+getClassAttribute().getName()+"\" name=\""+getClassAttribute().getName()+"\">";
        result += "<schm><precondition>";
        for(Attribute att : atts){
            if(!att.equals(getClassAttribute())) result += "<attref ref=\""+att.getName()+"\"/>\n";
        }
        result += "</precondition><conclusion>\n";
        result += "<attref ref=\""+getClassAttribute().getName()+"\"/>\n";
        result += "</conclusion>\n</schm>\n";

        //rules

        LinkedList<ArrayList<Condition>> rules = getRules();


        String decisionAtt = getClassAttribute().getName();
        Attribute decAtt = getClassAttribute();
        Attribute [] condAtts = new Attribute[atts.size()];
        List<Attribute> condAttsList = new LinkedList<Attribute>(Arrays.asList(atts.toArray(condAtts)));
        condAttsList.remove(decAtt);

        for(ArrayList<Condition> rule : rules) {
            result += "<rule id=\"rule_"+rule.hashCode()+"\">\n" +
                    "<condition>\n";
            //conditions
            //for(Condition c : rule){
            for(Attribute att : atts){
                Value value = new Value("any",1.0);
                //for (Attribute att : condAttsList) {
                for(Condition c : rule){
                    if(c.attName.equals(att.getName())){
                        value = c.value;
                    }
                }

                result += "<relation name=\"eq\">\n";
                result +=  "<attref ref=\""+att.getName()+"\"/>\n" +
                        "<set>  <value is=\""+value.getName()+"\"/>\n"+
                        "</set> </relation>";



            }
            result += "</condition>\n";
            result += "<decision>\n";
            //decision


            double confidence = 1;
            for(Condition c : rule){
                confidence *= c.value.getConfidence();
            }


            for(Condition c : rule){
                if(c.attName.equals(decisionAtt)){
                    result += "<trans>\n" +
                            "<attref ref=\""+c.attName+"\"/>\n";
                    result += "<set>";
                    result += "<value is=\""+c.value.getName()+"(#"+(Math.round((confidence*2-1)*100.0)/100.0)+")\"/>\n";
                    result += "</set></trans>\n";
                }
            }
            result += "</decision>\n";
            result += "</rule>\n";
        }

        result += "</table></xtt><callbacks/></hml>\n";



        return result;
    }

    public void saveHMR(String filename){
        //TODO
        return;
    }

    public String toHMR(){
        //TODO
        return null;
    }

    public void saveDot(String filename) throws FileNotFoundException, UnsupportedEncodingException {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(filename, "UTF-8");
            writer.println(toDot());

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

    }

    public Attribute getClassAttribute(){
        TreeNode temp  = root;
        while(!temp.isLeaf()){
            temp = temp.getEdges().getFirst().getChild();
        }
        Attribute result = new Attribute(temp.getAtt(), new HashSet<String>());
        for(Value v : temp.getStats().getStatistics()){
            result.addValue(v.getName());
        }

        return result;
    }

    public LinkedList<ArrayList<Condition>> getRules(){
        LinkedList<ArrayList<Condition>> rules = new LinkedList<>();
        return fillRules(rules,null,getRoot());
    }

    private LinkedList<ArrayList<Condition>> fillRules(
            LinkedList<ArrayList<Condition>> rules,
            ArrayList<Condition> currentRule,
            TreeNode root) {
        if(currentRule == null){
            currentRule = new ArrayList<Condition>(getAttributes().size());
        }
        String attName = root.getAtt();
        if(!root.isLeaf()) {
            for (TreeEdge e : root.getEdges()){
                ArrayList<Condition> newRule = (ArrayList<Condition>)currentRule.clone();
                newRule.add(new Condition(attName,e.getValue(),"eq"));
                fillRules(rules, newRule, e.getChild());
            }
        }else{
            ArrayList<Condition> finalRule = (ArrayList<Condition>)currentRule.clone();
            finalRule.add(new Condition(attName,root.getStats().getMostPorbable(),"set"));
            rules.add(finalRule);

        }


        return rules;
    }

    public HashSet<Attribute> getAttributes(){
        HashSet<Attribute> result = new HashSet<>();
        return fillAttributes(result, root);

    }

    private HashSet<Attribute> fillAttributes(HashSet<Attribute> result, TreeNode root){
        String attName = root.getAtt();
        Attribute att = new Attribute(attName, new HashSet<String>());
        if(result.contains(att)){
            for(Attribute tmp : result){
                if(tmp.equals(att)){
                    att = tmp;
                    break;
                }
            }
        }
        if(!root.isLeaf()) {
            for (TreeEdge e : root.getEdges()){
                att.addValue(e.getValue().getName());
                fillAttributes(result,e.getChild());
            }
            result.add(att);
        }else{
            //this is inefficient, but leave it.
            for(Value v : root.getStats().getStatistics()){
                att.addValue(v.getName());
            }
            result.add(att);

        }
        return result;
    }

    public String toDot(){
        String result = "digraph mediationTree{\n";
        result += toDot(root);
        return result+"\n}";
    }

    public String toDot(TreeNode parent){
        String result = "";

        String label = parent.getAtt()+"\n";
        if(parent.isLeaf()){
            //Add classification info to leaves
            for(Value v: parent.getStats().getStatistics()){
                label += v.toString()+"\n";
            }
        }

        result += parent.hashCode()+"[" +
                "label=\""+label+"\"," +
                "shape=box, " +
                "color=" + (parent.isLeaf() ? "red":"black")+"]";
        for(TreeEdge te : parent.getEdges()){
            result += parent.hashCode() +"->"+te.getChild().hashCode()+"[label=\""+te.getValue().getName()+"\n" +
                    "conf="+Math.round(te.getValue().getConfidence()*100.0)/100.0+"\"]\n";
            result += toDot(te.getChild());
        }

        return result;
    }

    @Override
    public String toString() {
        String result = "if "+root.getAtt()+" (play="+root.getStats()+")\n";
        for(TreeEdge te : root.getEdges()){
            result += toString(0,te.getValue(), te.getChild());
        }
        return result;
    }

    private String toString(int lvl, Value val, TreeNode node){
        String result = "|";
        for(int i =0; i < lvl; i++){
            result += "-------";
        }
        result += "is "+val;
        if(node.isLeaf()){
            result += " then " +node.getAtt()+" = "+node.getStats()+"\n";
        }else{
            result += "then if "+node.getAtt()+" (play="+node.getStats()+")\n";
        }
        lvl++;
        for(TreeEdge te : node.getEdges()) {
            result += toString(lvl, te.getValue(),te.getChild());

        }
        return result;
    }

    private class Condition{
        String attName;
        Value value;
        String op = "eq";

        public Condition(String attName, Value value, String op) {
            this.attName = attName;
            this.value = value;
            this.op = op;
        }
    }
}
