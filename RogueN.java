import java.io.*;
import java.util.*;


public class RogueN
{

    private int numComUni;
    private int numUniSys;
    private int numUniRef;

    private int numComBi;
    private int numBiSys;
    private int numBiRef;

    public RogueN(String sysSummary, String refSummary)
    {
        // Remove punctuation and lowercase everything
        String[] tSys = sysSummary.replaceAll("\\p{P}", "").toLowerCase().trim().split("\\s+");
        String[] tRef = refSummary.replaceAll("\\p{P}", "").toLowerCase().trim().split("\\s+");

        // Number of unigrams and bigrams
        numUniSys   = tSys.length;
        numBiSys    = tSys.length - 1;
        numUniRef   = tRef.length;
        numBiRef    = tRef.length - 1;

        Set<String> sysUnigrams = new HashSet<String>(Arrays.asList(tSys));
        Set<String> refUnigrams = new HashSet<String>(Arrays.asList(tRef));

        // Number of common unigram between referene summary and generated summary
        Set<String> comUnigrams = new HashSet<String>();
        for(String word: sysUnigrams)
        {
            if(refUnigrams.contains(word) && !comUnigrams.contains(word))
            {
                numComUni++;
                comUnigrams.add(word);
            }
        }
        numComUni   = comUnigrams.size();


        Set<String> sysBigrams = new HashSet<String>();
        for(int i=1; i < tSys.length; i++)
            sysBigrams.add(tSys[i-1]+tSys[i]);

        Set<String> refBigrams = new HashSet<String>();
        for(int i=1; i < tRef.length; i++)
            refBigrams.add(tRef[i-1]+tRef[i]);

        // Number of common bigrams
        Set<String> comBigrams = new HashSet<String>();
        for(String word: sysBigrams)
        {
            if(refBigrams.contains(word) && !comBigrams.contains(word))
            {
                numComBi++;
                comBigrams.add(word);
            }
        }
        numComBi = comBigrams.size();
    }

    public double rogue1Recall()
    {
        return (double)numComUni/numUniRef;
    }

    public double rogue1Precision()
    {
        return (double)numComUni/numUniSys;
    }

    public double rogue1F()
    {
        double rec = (double)numComUni/numUniRef;
        double pre = (double)numComUni/numUniSys;
        return 2 * rec * pre / (rec + pre);
    }

    public double rogue2Recall()
    {
        return (double)numComBi/numBiRef;
    }

    public double rogue2Precison()
    {
        return (double)numComBi/numBiSys;
    }

    public double rogue2F()
    {
        double rec = (double)numComBi/numBiRef;
        double pre = (double)numComBi/numBiSys;
        return 2 * rec * pre / (rec + pre);
    }

    public static void main(String[] args)
    {
        RogueN RN = new RogueN(
                "France 's Teddy Tamgho became third man leap over 18m in jump. France 's Teddy Tamgho exceeding mark by four centimeters. France 's Teddy Tamgho third man leap over 18m in triple jump. France     's Teddy Tamgho became In other final action on last day of championships. Germany 's Christina Obergfoll finally took gold. Germany 's Christina Obergfoll finally took gold after five previous silvers. Germ    any 's Christina Obergfoll finally took gold at global level in women 's javelin. Germany 's Christina Obergfoll clarify comments.",
                " France    dsdsfsdf France 123    Teddy  Teddy  tamgho became third 212 "
                );
        System.out.println(RN.rogue1Recall());
    }
}
