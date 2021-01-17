import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.lang.management.BufferPoolMXBean;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Created by sbk on 14.09.15.
 */
public class Data {
    private LinkedList<Instance> instances;
    private LinkedList<Attribute> attributes;
    private String name;




    protected Data(String name, LinkedList<Attribute> attributes, LinkedList<Instance> instances){
        this.name = name;
        this.attributes= attributes;
        this.instances = instances;


    }

    public Data filterAttributeValue(Attribute at, String value){
        LinkedList<Instance> newInstances = new LinkedList<>();
        LinkedList<Attribute> newAttributes = ((LinkedList)attributes.clone());
        newAttributes.remove(at);

        for(Instance i : instances){
            Reading reading = i.getReadingForAttribute(at.getName());
            String instanceVal = reading.getMostProbable().getName();
            if(instanceVal.equals(value)){
                LinkedList<Reading> newReadings = (LinkedList)i.getReadings().clone();
                newReadings.remove(reading);
                newInstances.add(new Instance(newReadings));
            }
        }

        return new Data(name,newAttributes,newInstances);
    }


    public Attribute getAttributeOfName(String attName){
        for(Attribute at : attributes){
            if(at.getName().equals(attName)){
                return  at;
            }
        }
        return null;
    }


    public String toArffMostProbable(){
        String result = "@relation "+name + "\n";
        for(Attribute at : attributes){
            result += at.toArff()+"\n";
        }

        result += "@data\n";

        for(Instance i : instances){
            Iterator<Reading> rit = i.getReadings().iterator();
            while(rit.hasNext()){
                Reading r = rit.next();
                result += r.getMostProbable().getName();
                if(rit.hasNext()) result +=",";
            }
            result += "\n";
        }

        return result;
    }

    public String toArffSkipInstance(double epsilon){
        String result = "@relation "+name + "\n";
        for(Attribute at : attributes){
            result += at.toArff()+"\n";
        }

        result += "@data\n";

        for(Instance i : instances){
            Iterator<Reading> rit = i.getReadings().iterator();
            String partial = "";
            while(rit.hasNext()){
                Reading r = rit.next();
                if(r.getMostProbable().getConfidence() > epsilon) {
                    partial += r.getMostProbable().getName();
                }else{
                    break;
                }
                if (rit.hasNext()) partial += ",";
                else result += partial+"\n";
            }
        }
        return result;
    }

    public String toArffSkipValue(double epsilon){
        String result = "@relation "+name + "\n";
        for(Attribute at : attributes){
            result += at.toArff()+"\n";
        }

        result += "@data\n";

        for(Instance i : instances){
            Iterator<Reading> rit = i.getReadings().iterator();
            String partial = "";
            while(rit.hasNext()){
                Reading r = rit.next();
                if(r.getMostProbable().getConfidence() > epsilon) {
                    partial += r.getMostProbable().getName();
                }else{
                    partial += "?";
                }
                if (rit.hasNext()) partial += ",";
                else result += partial+"\n";
            }
        }
        return result;

    }

    public String toUArff() {
        String result = "@relation "+name + "\n";
        for(Attribute at : attributes){
            result += at.toArff()+"\n";
        }

        result += "@data\n";

        for(Instance i : instances){
            result  += i.toArff()+"\n";
        }

        return result;

    }

    public AttStats calculateStatistics(Attribute att){
        return AttStats.getStatistics(att, this);
    }

    public static Data parseUArff(String filename) throws ParseException {
        String name = null;
        LinkedList<Attribute> atts = new LinkedList<Attribute>();
        LinkedList<Instance> insts = new LinkedList<Instance>();
        //Open file
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            name = br.readLine().split("@relation")[1].trim();
            String line = "";
            while ((line = br.readLine()) != null) {
                if(line.length() == 0) continue;
                String [] attSplit = line.trim().split("@attribute");
                if(attSplit.length > 1){
                    Attribute att = parseAttribute(attSplit[1].trim());
                    atts.add(att);
                }else if(line.trim().equals("@data")){
                    break;
                }

            }

            //Read instances
            while ((line = br.readLine()) != null) {
                Instance inst = parseInstances(atts, line.trim());
                insts.add(inst);
            }

            return new Data(name, atts, insts);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Data parseUArffFromString(String uarff) throws ParseException {
        String name = null;
        LinkedList<Attribute> atts = new LinkedList<Attribute>();
        LinkedList<Instance> insts = new LinkedList<Instance>();
        //Open file
        try (BufferedReader br = new BufferedReader(new StringReader(uarff))) {
            name = br.readLine().split("@relation")[1].trim();
            String line = "";
            while ((line = br.readLine()) != null) {
                if(line.length() == 0) continue;
                String [] attSplit = line.trim().split("@attribute");
                if(attSplit.length > 1){
                    Attribute att = parseAttribute(attSplit[1].trim());
                    atts.add(att);
                }else if(line.trim().equals("@data")){
                    break;
                }

            }

            //Read instances
            while ((line = br.readLine()) != null) {
                Instance inst = parseInstances(atts, line.trim());
                insts.add(inst);
            }

            return new Data(name, atts, insts);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Data parseUArffFromString(String string, String className) throws ParseException {
        Data tempData = parseUArffFromString(string);
        Attribute classAtt = tempData.getAttributeOfName(className);
        int classIndex = tempData.attributes.indexOf(classAtt);
        tempData.attributes.remove(classIndex);
        tempData.attributes.addLast(classAtt);
        //change order of reading for the att.
        for(Instance i : tempData.instances){
            Reading classLabel = i.getReadingForAttribute(classAtt.getName());
            LinkedList<Reading> readings = i.getReadings();
            readings.remove(classIndex);
            readings.addLast(classLabel);
            i.setReadings(readings);
        }
        return tempData;
    }

    public static Data parseUArffFromString(String string, int classIndex) throws ParseException {
        Data tempData = parseUArffFromString(string);
        //change order of the attribtues
        Attribute classAtt = tempData.attributes.get(classIndex);
        tempData.attributes.remove(classIndex);
        tempData.attributes.addLast(classAtt);
        //change order of reading for the att.
        for(Instance i : tempData.instances){
            Reading classLabel = i.getReadingForAttribute(classAtt.getName());
            LinkedList<Reading> readings = i.getReadings();
            readings.remove(classIndex);
            readings.addLast(classLabel);
            i.setReadings(readings);
        }
        return tempData;
    }


    public static Data parseUArff(String filename, String className) throws ParseException {
        Data tempData = parseUArff(filename);
        Attribute classAtt = tempData.getAttributeOfName(className);
        int classIndex = tempData.attributes.indexOf(classAtt);
        tempData.attributes.remove(classIndex);
        tempData.attributes.addLast(classAtt);
        //change order of reading for the att.
        for(Instance i : tempData.instances){
            Reading classLabel = i.getReadingForAttribute(classAtt.getName());
            LinkedList<Reading> readings = i.getReadings();
            readings.remove(classIndex);
            readings.addLast(classLabel);
            i.setReadings(readings);
        }
        return tempData;
    }

    public static Data parseUArff(String filename, int classIndex) throws ParseException {
        Data tempData = parseUArff(filename);
        //change order of the attribtues
        Attribute classAtt = tempData.attributes.get(classIndex);
        tempData.attributes.remove(classIndex);
        tempData.attributes.addLast(classAtt);
        //change order of reading for the att.
        for(Instance i : tempData.instances){
            Reading classLabel = i.getReadingForAttribute(classAtt.getName());
            LinkedList<Reading> readings = i.getReadings();
            readings.remove(classIndex);
            readings.addLast(classLabel);
            i.setReadings(readings);
        }
        return tempData;
    }

    public static Instance parseInstances(LinkedList<Attribute> baseAtts, String instDef) throws ParseException {
        String [] readingsDefs = instDef.split(",");
        Instance i = new Instance();
        if(readingsDefs.length != baseAtts.size()) throw new ParseException("Missing attribute definition, or value in line "+instDef);

        Iterator<Attribute> it = baseAtts.iterator();
        for(String reading : readingsDefs){
            Attribute att = it.next();
            Reading r = Reading.parseReading(att, reading);
            i.addReading(r);
        }

        return i;
    }

    public static Attribute parseAttribute(String attDef){
        int nameBoundary = attDef.indexOf(' ');
        String name = attDef.substring(0,nameBoundary);
        HashSet<String> domain  = new HashSet<String>();


        String [] untrimmedDomain = attDef.substring(nameBoundary).replaceAll("[{}]","").split(",");
        for(String value : untrimmedDomain){
            domain.add(value.trim());
        }

        return new Attribute(name, domain);
    }

    public LinkedList<Instance> getInstances() {
        return (LinkedList)instances.clone();
    }

    public LinkedList<Attribute> getAttributes() {

        return (LinkedList)attributes.clone();
    }

    public String getName() {
        return name;
    }

    public Attribute getClassAttribute() {
        return attributes.getLast();
    }
}
