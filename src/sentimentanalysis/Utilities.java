/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sentimentanalysis;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;


/**
 *
 * @author Chucho
 */
public class Utilities {
    //Sentiwordnet data
    public static Map<Integer, String> partOfSpeech;
    public static Map<Integer, Float> posScore;
    public static Map<Integer, Float> negScore;
    //xrisimopoiw bimaps gia na mporw na xrisimopoihsw thn inverse methodo(grammi 139)
    //h opoia antistrefei ton pinaka termDictionary(id,synset) se invTermDictionary(synset,id)
    public static BiMap<Integer, List<String>> termDictionary;
    public static BiMap<List<String>,Integer> invTermDictionary;
    
    //Twitter4j data
    public static ConfigurationBuilder cb;
    public static TwitterFactory tf;
    public static twitter4j.Twitter twitter;
    public static Paging paging;
    public static int tweetsCount;
    public static List<Status> status;
    
    //Files
    public static File sentiwordnet;
    
    //Output
    static List<List<Integer>> termIds;
    static float tweetScore;
    static String classification;
    static float accuracy;
    
    public static void ConnectToTwitter(String conKey,String conSec,String accTok,String accTokSec){
        cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey(conKey)
                .setOAuthConsumerSecret(conSec)
                .setOAuthAccessToken(accTok)
                .setOAuthAccessTokenSecret(accTokSec);
               
        tf = new TwitterFactory(cb.build());
        twitter = tf.getInstance();
        paging=new Paging();
    }
    
    public static void ScanWordNet(File filename) throws FileNotFoundException{
        // Ta dedomena diaxorizontai se 3 hashmaps tis morfis
        //posScore ->  (ID,posScore)
        //negScore ->  (ID,negScore)
        //termDictionary ->  (ID,synSet)
        //synSet(synonimset)->  (term#num,term#num...)
        partOfSpeech = new HashMap();
        posScore = new HashMap();
        negScore = new HashMap();
        termDictionary = HashBiMap.create();
        invTermDictionary = HashBiMap.create();
        List<String> synSet;
        
        //Xrisimopoiw scanner gia to diavasma twn dedomenwn apo to sentiwordnet
        Scanner sc = new Scanner(filename);
        String current="----";
        String previous;
        
        //Regex gia tous orous mesa sto sentiwordnet
        String termRegX=".+#[0-9]+";
        //Regex gia ta id mesa sto sentiwordnet
        String idRegX="[0-9]{8}";
        //Regex gia ta meri toy logou(Parts of speech) : a=adjective v=verb n=noun r=adverb
        String posRegX="[avnr]{1}";
        
        
        Integer id;
        Float temp;
        while(sc.hasNext()){
            
            synSet=new ArrayList<>();
            previous=current;
            current=sc.next();
            
            //System.out.println(current);
            
            if(current.matches(idRegX)){
                id=Integer.parseInt(current);
                //System.out.println(id);
                if(previous.matches(posRegX)){
                    partOfSpeech.put(Integer.parseInt(current), previous);
                }
                current=sc.next();
                temp=Float.parseFloat(current);
                posScore.put(id,temp);
                //System.out.println(current);
                current=sc.next();
                temp=Float.parseFloat(current);
                negScore.put(id,temp);
                //System.out.println(current);
                
                //SYNSET HANDLER
                current=sc.next();
                while(current.matches(termRegX)){
                    current=current.replaceAll("[-_]", " ");
                    synSet.add(current);
                    
                    //System.out.println(current);
                    current=sc.next();
                }
                
                if(!termDictionary.containsValue(synSet)){
                    termDictionary.put(id, synSet);
                    //System.out.println(id+" "+synSet);
                }
                
            }
        }
        
        
        invTermDictionary=termDictionary.inverse();
        
        /*
        System.out.println(partOfSpeech);
        System.out.println(termDictionary);
        System.out.println(invTermDictionary);
        System.out.println(posScore);
        System.out.println(negScore);
        */
        
        
    }
    
    public static List<List<Integer>> AnalyzeText(String text){
        List<String> tokens;
        String[] tknPos;
        
        List<List<Integer>> id = new ArrayList<>();
        List<Integer> currentId;
        tokens=TagPartsOfSpeech(text);
        //System.out.println(tokens);
        for(int i=0;i<tokens.size();i++){
            currentId=new LinkedList<>();
            
            tknPos=tokens.get(i).split("_");
            tknPos[0]=tknPos[0].toLowerCase();
            if(tknPos[0].startsWith("#")){
                tknPos[0]=tknPos[0].split("#")[1];
            }
            //System.out.println(i+" : "+tokens.get(i)+" - "+tknPos[0]);
            if(tknPos[0].matches("^(http|https|ftp)://.*$|^@[A-Za-z0-9]++")||tknPos[1].matches("CC|CD|DT|EX|FW|IN|LS|MD|PDT|POS|PRP|PRP$|RP|SYM|TO|UH|UH|WDT|WP|WP$|WRB")){
                //System.out.println("Link,@ mention or other non-useful part of speech found!");
                continue;
            }
            for (Map.Entry<List<String>,Integer> entry : invTermDictionary.entrySet())
            {
                for(String term : entry.getKey()){
                    term=term.split("#")[0];
                    if(term.equals(tknPos[0])&&partOfSpeech.get(entry.getValue()).equals(tknPos[1])){
                        currentId.add(entry.getValue());
                        //System.out.println(entry.getValue());
                    }
                }
                
            }
            id.add(currentId);
        }
        return id;
        
    }
    public static List<String> TagPartsOfSpeech(String text){
        List<String> res=new ArrayList<>();
        MaxentTagger tagger = new MaxentTagger("english-left3words-distsim.tagger");
        String tagged = tagger.tagString(text);
        //System.out.println(tagged);
        
        for(String str:tagged.split(" ")){
            
            //System.out.println(str);
            if(str.endsWith("JJ")){
                str=str.replaceAll("_JJ", "_a");
            }else if(str.endsWith("JJR")){
                str=str.replaceAll("_JJR", "_a");
            }else if(str.endsWith("JJS")){
                str=str.replaceAll("_JJS", "_a");
            }else if(str.endsWith("NN")){
                str=str.replaceAll("_NN", "_n");
            }else if(str.endsWith("NNS")){
                str=str.replaceAll("s_NNS", "_n");
            }else if(str.endsWith("NNP")){
                str=str.replaceAll("_NNP", "_n");
            }else if(str.endsWith("NNPS")){
                str=str.replaceAll("s_NNPS", "_n");
            }else if(str.endsWith("RB")){
                str=str.replaceAll("_RB", "_r");
                if(str.startsWith("n't")){
                    str=str.replaceAll("n't", "not");
                }
            }else if(str.endsWith("RBR")){
                str=str.replaceAll("_RBR", "_r");
            }else if(str.endsWith("RBS")){
                str=str.replaceAll("_RBS", "_r");
            }else if(str.endsWith("VB")){
                str=str.replaceAll("_VB", "_v");
            }else if(str.endsWith("VBD")){
                str=str.replaceAll("ed_VBD", "_v");
            }else if(str.endsWith("VBG")){
                str=str.replaceAll("ing_VBG", "_v");
            }else if(str.endsWith("VBN")){
                str=str.replaceAll("_VBN", "_a");
            }else if(str.endsWith("VBP")){
                str=str.replaceAll("_VBP", "_v");
            }else if(str.endsWith("ies_VBZ")){
                str=str.replaceAll("ies_VBZ", "y_v");
            }else if(str.endsWith("s_VBZ")){
                str=str.replaceAll("s_VBZ", "_v");
            }
            
            res.add(str);
            //System.out.println(str);
        }
        
        return res;
    }
    public static float CalculateScore(List<List<Integer>> termIds){
        
        float[] swnScore=new float[termIds.size()];
        //List<Float> swnScore=new ArrayList<>();
        //System.out.println(termIds);
        for(List<Integer> list : termIds){
            for(Integer id : list){
                //System.out.println(termDictionary.get(id));
            }
        }
        //System.out.println("Total number of terms : "+termIds.size());
        int i=0;
        float sum;
        float temp;
        accuracy=0;
        //System.out.println("Calculating individual term score table...");
        for(List<Integer> token : termIds){
            sum=0;
            if(!token.isEmpty()){
                //System.out.println("Token Not empty : "+token);
                accuracy=accuracy+1;
                for(Integer id : token){
                    
                    swnScore[i]=1+posScore.get(id)-negScore.get(id);
                    //swnScore.add(1+posScore.get(id)-negScore.get(id));
                    
                    temp=1+posScore.get(id)-negScore.get(id);
                    //swnScore[i]=swnScore[i]*token.size();
                    //System.out.println(token.size());
                    sum=sum+temp;
                    
                   //System.out.println("--"+swnScore[i]);
                    //System.out.println(id);
                }
                
                //temp=sum/(float)token.size();
                swnScore[i]=sum/token.size();
                //swnScore.set(i,temp);
                
                //System.out.println(swnScore[i]);
                i++;
            }
            
            else{
                //System.out.println("Token empty :"+token);
                swnScore[i]=-1f;
                //swnScore.set(i, -1f);
                i++;
            }
            
        }
        //System.out.println("Done");
        accuracy=accuracy/termIds.size();
        //Classification testing
        /*
        System.out.println("Printing results...");
        for(i=0;i<swnScore.length;i++){
            if(swnScore[i]>1.2f){
                System.out.println(i+" - "+swnScore[i]+" : positive");
            }else if(swnScore[i]<=1.2f && swnScore[i]>0.95f){
                System.out.println(i+" - "+swnScore[i]+" : somewhat positive");
            }else if(swnScore[i]<=0.95f && swnScore[i]>0.5f){
                System.out.println(i+" - "+swnScore[i]+" : neutral");
            }else if(swnScore[i]<=0.95f && swnScore[i]>0.2f){
                System.out.println(i+" - "+swnScore[i]+" : somewhat negative");
            }else if(swnScore[i]<0.2f){
                System.out.println(i+" - "+swnScore[i]+" : negative");
            }else if(swnScore[i]==-1){
                System.out.println("Null");
            }
            
        }
        
        */
        
        //System.out.println("Calculating total tweet score");
        //float tweetScore=swnScore.get(0);
        float tweetScore=swnScore[0];
        //System.out.println("First term score : "+swnScore[0]);
        //System.out.println("Total tweet score : "+tweetScore+"\n");
        //System.out.println("Accuracy : "+accuracy*100+"%");
        //----------------------------
        for(int j=1;j<swnScore.length;j++){
            if(tweetScore!=-1&&swnScore[j]!=-1){
                tweetScore=tweetScore+swnScore[j];
                
                //System.out.println("Current term score - "+j+ " : "+swnScore[j]);
                //System.out.println("Total tweet score : "+tweetScore+"\n");
            }else if(j+1!=swnScore.length&&tweetScore==-1) {
                if(j+1!=swnScore.length){
                    //System.out.println("Check");
                    tweetScore=swnScore[j+1];
                }
            }
        }
        //System.out.println(swnScore.length);
        
        tweetScore=tweetScore/swnScore.length;
        return tweetScore;
    }
    
    public static String ClassifyScore(float tweetScore){
        String result;
            if(tweetScore>1.2f){
                result="positive";
                //System.out.println("Final Tweet Score : "+tweetScore+".Classified as : positive");
            }else if(tweetScore<=1.2f && tweetScore>0.95f){
                result="somewhat positive";
                //System.out.println("Final Tweet Score : "+tweetScore+".Classified as : somewhat positive");
            }else if(tweetScore<=0.95f && tweetScore>0.5f){
                result="neutral";
                //System.out.println("Final Tweet Score : "+tweetScore+".Classified as : neutral");
            }else if(tweetScore<=0.5f && tweetScore>0.2f){
                result="somewhat negative";
                //System.out.println("Final Tweet Score : "+tweetScore+".Classified as : somewhat negative");
            }else if(tweetScore<=0.2f&&tweetScore>=0){
                result="negative";
                //System.out.println("Final Tweet Score : "+tweetScore+".Classified as : negative");
            }else{
                result=null;
            }
            
        return result;
    }
    
    
    
    
    
    
    
    
}
