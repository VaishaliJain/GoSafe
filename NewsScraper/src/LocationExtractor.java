import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.util.Triple;
import java.util.List;
import java.util.Map;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author nikijain
 */
public class LocationExtractor 
{
    public static String stanfordClassifierLocation = "/Users/nikijain/NetBeansProjects/NewsScraper/API/stanford-ner-2017-06-09/";
    
    public Boolean getLocationOrganization(String filePath, Map<String, Integer> locations, Map<String, Integer> organizations) {
        
        Boolean result = false;
        String serializedClassifier = "classifiers/english.all.3class.distsim.crf.ser.gz";
        
        try {
            AbstractSequenceClassifier<CoreLabel> classifier = CRFClassifier.getClassifier(stanfordClassifierLocation + serializedClassifier);
            String fileContents = IOUtils.slurpFile(filePath);
            List<Triple<String, Integer, Integer>> list = classifier.classifyToCharacterOffsets(fileContents);

            list.forEach((item) -> {    
                if(item.first().equalsIgnoreCase("LOCATION")) {
                    String key = fileContents.substring(item.second(), item.third());
                    if(!locations.containsKey(key))
                        locations.put(key, 1);
                    else
                        locations.put(key, locations.get(key)+1);
                }   
                else if(item.first().equalsIgnoreCase("ORGANIZATION")) {
                    String key = fileContents.substring(item.second(), item.third());
                    if(!organizations.containsKey(key))
                        organizations.put(key, 1);
                    else
                        organizations.put(key, organizations.get(key)+1);
                }
            });
            
            result = true;
        }
        catch(Exception e) {
            System.out.println("Exceptoin Caught: "+e);
            e.printStackTrace();
        }
        
        return result;
    }
}
