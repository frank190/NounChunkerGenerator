import java.io.*;
import java.util.*;

/*Author: Leizhen Shi
Update Date: 08/16/2018
Information: Generating .pos tag and .tchunk files by reading outputs from Brandeis Chinese System
The format read by the program will be consistent to .out.txt file generated from Brandeis System

Rule:
Find a "NN" or "NP"
Look for if there is a "JJ", "JJS" or "JJR" before the noun phrase
If there is, the the adjective is "B"
Then keep looking for the noun phrases after the noun we found

Instrustions:
Arg1: input the file directory
Arg2: input "background" or "foreground"
 */

public class NounChunkerGenerator {
    public static void main (String[] args){

        String fileDir = args[0];
        String outputDir = args[1];

        File outputDirectory = new File (outputDir);
        outputDirectory.mkdirs();

        try {
            File dir = new File(fileDir);
            File[] listofFiles = dir.listFiles();

            //Create list containing information for background/foreground files
            File listforInput = new File(outputDir+"_tchunk_list");
            listforInput.createNewFile();
            FileWriter listForInputWriter = new FileWriter(listforInput);




            for (int num = 0; num < listofFiles.length; num++) {


                BufferedReader br = new BufferedReader(new FileReader(listofFiles[num]));

                //Create posfile and tchunkfiles
                File posfile = new File(outputDirectory, listofFiles[num].getName() + ".pos.txt");
                File tchunkfile = new File( outputDirectory, listofFiles[num].getName() + ".tchunk.txt");

                posfile.createNewFile();
                tchunkfile.createNewFile();

                //Write the directories for background/foreground files
                listForInputWriter.write(outputDir + "/" + listofFiles[num].getName() + ".tchunk.txt\n" );


                FileWriter posfileWriter = new FileWriter(posfile);
                FileWriter tchunkfileWriter = new FileWriter(tchunkfile);

                String CurrentLine;

                long startCounter = 1;
                long endCounter;

                long positionCounter = 0;

                ArrayList<Chunk> chunklist = new ArrayList<Chunk>();

                while ((CurrentLine = br.readLine()) != null) {
                    String[] lineInString = CurrentLine.split(" ");
                    for (int i = 0; i < lineInString.length; i++) {
                        //Generating POS Tag files
                        String[] wordProperty = lineInString[i].split("_");
                        endCounter = startCounter + wordProperty[0].length();
                        posfileWriter.write(wordProperty[0] + "  |||  " + "S: " + startCounter + " " + "E: " + endCounter + "  " + "|||  " + wordProperty[1] + "\n");
                        startCounter = endCounter;


                        //Store information to a list
                        Chunk chunk = new Chunk(wordProperty[0], wordProperty[1], positionCounter);
                        chunklist.add(chunk);
                        positionCounter++;
                    }
                }

                long BICounter = 0;

                for (int i = 0; i < positionCounter; i++) {
                    //For B and I Tags
                    if (chunklist.get(i).getTag().equals("NN") || chunklist.get(i).getTag().equals("NP")) {
                        //The case that the found NN is at the start of the text
                        if (i == 0) {
                            chunklist.get(i).modifyBIOTag("B-NP");
                            BICounter++;
                        } else {
                            if (BICounter == 0) {
                                if (chunklist.get(i - 1).getTag().equals("JJ") || chunklist.get(i - 1).getTag().equals("JJR") || chunklist.get(i - 1).getTag().equals("JJS")) {
                                    chunklist.get(i - 1).modifyBIOTag("B-NP");
                                    chunklist.get(i).modifyBIOTag("I-NP");
                                    BICounter += 2;
                                } else {
                                    chunklist.get(i).modifyBIOTag("B-NP");
                                    BICounter++;
                                }
                            } else {
                                chunklist.get(i).modifyBIOTag("I-NP");
                            }
                        }
                    } else {
                        BICounter = 0;
                    }
                }

                for (int j = 0; j < positionCounter; j++) {
                    tchunkfileWriter.write(chunklist.get(j).getWord() + "  " + chunklist.get(j).getWord() + "  " + chunklist.get(j).getTag() + "  " + chunklist.get(j).getBIOTag() + "\n");
                }

                posfileWriter.close();
                tchunkfileWriter.close();

            }

            listForInputWriter.close();


        } catch (IOException  e){
            e.printStackTrace();
        }
    }
}

class Chunk{
    private String word;
    private String tag;
    private long position;
    private String BIOTag = "0";

    public Chunk(String word, String tag, long position){
        this.word = word;
        this.tag = tag;
        this.position = position;
    }

    public String getWord(){
        return word;
    }

    public String getTag(){
        return tag;
    }

    public long getPosition(){
        return position;
    }

    public String getBIOTag(){
        return BIOTag;
    }

    public void modifyWord (String word){
        this.word = word;
    }

    public void modifytag (String tag){
        this.tag = tag;
    }

    public void modifyPosition(long position){
        this.position = position;
    }

    public void modifyBIOTag(String BIOTag){
        this.BIOTag = BIOTag;
    }
}