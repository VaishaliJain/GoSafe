
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author nikijain
 */

class Story
{
    String link;
    String place;
    String typeOfCrime;
    Date date;
    Map<String, Integer> locations; 
    Map<String, Integer> organizations; 

    Story (String url) {
        this.link  = url;
        locations = new HashMap<>(); 
        organizations = new HashMap<>(); 
    }
}

enum CrimeType {Robbery, Murder, Rape, Attack};
enum CrimePlace {Delhi, Bangalore};

public class TheHinduScraper {
    public static Date lastRead = new Date();
    public static final String tempLocation = System.getProperty("user.home")+"/NetBeansProjects/NewsScraper/tempData/";
    
    //gets all story link in a page
    public static Queue<Story> getStoriesLink(CrimeType crimeSearch, CrimePlace locationForCrime)
    {
        String searchQuery = crimeSearch+ " in " +locationForCrime;
        //System.out.println("searchQuery: "+searchQuery);
        
        // get all story in page 1..iterate for other pages too
        Queue<Story> stories = new LinkedList();
        try {  
            searchQuery = searchQuery.replaceAll(" ", "+").toLowerCase();
            URL url = new URL("http://www.thehindu.com/search/?q="+searchQuery+"&order=DESC&sort=publishdate");
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
            String line = null;
            StringBuilder tmp = new StringBuilder();
            BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
            while ((line = in.readLine()) != null) {
                tmp.append(line);
            }
            
            Document doc = Jsoup.parse(tmp.toString());

            Elements links = doc.getElementsByClass("story-card-33-img focuspoint");
            
            Map<String, Integer> unique_links =new HashMap<String,Integer>(); 
            for (Element link : links)
            {
                if(unique_links.containsKey(link.attr("href")))
                    continue;
                else
                    unique_links.put(link.attr("href"), 1);
                
                Story s = new Story(link.attr("href"));
                s.place = locationForCrime+"";
                s.typeOfCrime = crimeSearch+"";

                stories.add(s);
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        return stories;
        
    }
    
    //gets crime location and organization
    public static void getCrimeRegionData(Queue<Story> stories)
    {
        try 
        {  
            for(Story s : stories)
            {
                URL url = new URL(s.link);
                HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
                String line = null;
                StringBuilder tmp = new StringBuilder();
                BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
                while ((line = in.readLine()) != null) {
                    tmp.append(line);
                }

                Document doc = Jsoup.parse(tmp.toString());
                Elements links = doc.getElementsByClass("article");
                
                String filePath = tempLocation+"/article";
                try(BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
                    for (Element link : links)
                        bw.write(link.getElementsByTag("p").text()+"\n");
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
                
                LocationExtractor e = new LocationExtractor();
                e.getLocationOrganization(filePath, s.locations, s.organizations);
                
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        
        Queue<Story> stories =new LinkedList();
        
        //Get All the stories
        CrimeType crimes[] = CrimeType.values();
        for (CrimeType crimeSearch : crimes) {
            //stories.addAll(getStoriesLink(crimeSearch, CrimePlace.Delhi));
            stories.addAll(getStoriesLink(crimeSearch, CrimePlace.Bangalore));
            
            break;
        }        
        
        //Parse crime data from the stories
        getCrimeRegionData(stories);
        
        //Output
        for(Story s : stories)
        {
            System.out.println("Crime Link: "+s.link);
            System.out.println("Crime Type: "+s.typeOfCrime);
            System.out.println("Crime Place: "+s.place);
            System.out.println("Crime Locations: ");
            s.locations.entrySet().stream()
                                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                                .forEach(System.out::println);

            System.out.println("Crime Organizations: ");
            s.organizations.entrySet().stream()
                                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                                .forEach(System.out::println);
        }
    }
}
