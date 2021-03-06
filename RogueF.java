import java.io.*;
import java.util.*;
import java.util.stream.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;


public class RogueF
{

    private int numComUni;
    private int numUniSys;
    private int numUniRef;

    private int numComBi;
    private int numBiSys;
    private int numBiRef;

    private static double avgRogue1F;
    private static double avgRogue2F;
    private static double avgRoguelF;
    private static double avgRogueSF;
    private static int count;

    public static List<String> generateSkipGrams(String str) {
        String[] retstr = str.replaceAll("[^a-zA-Z0-9 ]", "").split("\\s+");
        return generateSkipGrams2(retstr);
    }
    
    private static List<String> generateSkipGrams2(String[] s) {
        List<String> skipgrams = new ArrayList<>();
        for (int i = 0; i < s.length; i++) {
            for (int j = i + 1; j < s.length; j++) {
            	skipgrams.add(s[i]+" "+s[j]);
            }
        }
        return skipgrams;
    }
    
    public static int lcs(String referenceSum, String systemSum) 
    { 	
	 	String[] X = referenceSum.split(" ");
	 	String[] Y = systemSum.split(" ");

	 	int m = X.length;
	 	int n = Y.length;
	 
        int[][] L = new int[m+1][n+1]; 
   
        // Following steps build L[m+1][n+1] in bottom up fashion. Note 
        // that L[i][j] contains length of LCS of X[0..i-1] and Y[0..j-1]  
        for (int i=0; i<=m; i++) 
        { 
            for (int j=0; j<=n; j++) 
            { 
                if (i == 0 || j == 0) 
                    L[i][j] = 0; 
                else if (X[i-1].equals(Y[j-1])) 
                    L[i][j] = L[i-1][j-1] + 1; 
                else
                    L[i][j] = Math.max(L[i-1][j], L[i][j-1]); 
            } 
        } 
        return L[m][n];
    }
    
    private double computeRogue1F(String sysSummary, String refSummary)
    {
    	// Remove punctuation and lowercase everything
        String[] tSys = sysSummary.replaceAll("\\p{P}", "").toLowerCase().trim().split("\\s+");
        String[] tRef = refSummary.replaceAll("\\p{P}", "").toLowerCase().trim().split("\\s+");

        // Number of bigrams
        numUniSys   = tSys.length;
        numUniRef   = tRef.length;

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
        
        double r1rec = (double)numComUni/numUniRef;
        double r1pre = (double)numComUni/numUniSys;
        double r1F = 2 * r1rec * r1pre / (r1rec + r1pre);
        return r1F;
    }
    
    private double computeRogue2F(String sysSummary, String refSummary)
    {
    	// Remove punctuation and lowercase everything
        String[] tSys = sysSummary.replaceAll("\\p{P}", "").toLowerCase().trim().split("\\s+");
        String[] tRef = refSummary.replaceAll("\\p{P}", "").toLowerCase().trim().split("\\s+");

        // Number of bigrams
        numBiSys    = tSys.length - 1;
        numBiRef    = tRef.length - 1;
        
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
        double r2rec = (double)numComBi/numBiRef;
        double r2pre = (double)numComBi/numBiSys;
        double r2F = 2 * r2rec * r2pre / (r2rec + r2pre);
        return r2F;
    }
    
    private double computeRogueLF(String sysSummary, String refSummary)
    {
    	int len = lcs(refSummary,sysSummary);
        double rLrec = (double)len/numUniRef;
        double rLpre = (double)len/numUniSys;
        double rLF = 2 * rLrec * rLpre / (rLrec + rLpre);
        return rLF;
    }
    
    private double computeRogueSkipF(String sysSummary, String refSummary)
    {
    	int numComSkip = 0;
        List<String> ref_Skip = generateSkipGrams(refSummary);
        List<String> sys_Skip = generateSkipGrams(sysSummary);
        
        int numSkipSys = sys_Skip.size();
        int numSkipRef = ref_Skip.size();
        Set<String> comSkipGrams = new HashSet<String>();
        for(String word: sys_Skip)
        {
            if(ref_Skip.contains(word) && !comSkipGrams.contains(word))
            {
                numComSkip++;
                comSkipGrams.add(word);
            }
        }
        numComSkip = comSkipGrams.size();
        
        double rSrec = (double)numComSkip/numSkipRef;
        double rSpre = (double)numComSkip/numSkipSys;
        double rSF = 2 * rSrec * rSpre / (rSrec + rSpre);
        return rSF;
    }
    
    private List<Double> computeRogueF(String sysSummary, String refSummary)
    {
        
        double r1F = computeRogue1F(sysSummary,refSummary);

        double r2F = computeRogue2F(sysSummary,refSummary);

        double rlF = computeRogueLF(sysSummary,refSummary);

        double rSF = computeRogueSkipF(sysSummary,refSummary);		
        
        List<Double> r = new ArrayList<Double>();
        r.add(r1F);
        r.add(r2F);
        r.add(rlF);
        r.add(rSF);
        return r;
    }

     
    private void Compute(Path sys_path)
    {
    	String data = System.getenv("PROJECT_HOME") + File.separator + "data";
        String ref_dir = data + File.separator + "parsed" + File.separator + "summary";

        String ref_summary = ref_dir + File.separator + sys_path.getFileName().toString();

        try
        {
            File ref_file = new File(ref_summary);
            if(ref_file.exists())
            {
                BufferedReader ref_br = new BufferedReader(new FileReader(ref_summary));
                BufferedReader sys_br = new BufferedReader(new FileReader( sys_path.toString() ));

                String ref = ref_br.readLine();
                String sys = sys_br.readLine();

                List<Double> F = computeRogueF(sys, ref);
                
                if(!F.contains(Double.NaN))
                {
                	count++;
                	avgRogue1F += F.get(0);
                	avgRogue2F += F.get(1);
                	avgRoguelF += F.get(2);
                	avgRogueSF += F.get(3);
                    for(double f: F)
                        System.out.print(f+" ");
                    System.out.println();
                }
                else
                {
//                    System.out.println("Failed for " + ref_file + ", " + sys_path.toString());
                }
                ref_br.close();
                sys_br.close();
            }
            else
            {
                System.out.println(ref_file + " doesn't exist.");
            }
        }
        catch(Exception e)
        {
            System.out.println(e);
        }
    }

    public void Process()
    {
        String data = System.getenv("PROJECT_HOME") + File.separator + "data";
        String sys_dir = data + File.separator + "output";

        try(Stream<Path> paths = Files.walk( Paths.get(sys_dir) ))
        {
            System.out.println("Computing RogueN measures...");
            System.out.println("Outputting in order : R1F R2F RLF");
                paths
                    .filter(Files::isRegularFile)
                    .forEach( path -> Compute(path) );
            System.out.println("Finished RogueN measures...");
        }
        catch(Exception e)
        {
            System.out.println(e);
        }
    }

    public static void main(String[] args)
    {
    	avgRogue1F=0;
    	avgRogue2F=0;
    	avgRoguelF=0;
    	avgRogueSF=0;
    	count=0;
        RogueF RF = new RogueF();
        RF.Process();
        avgRogue1F=avgRogue1F/count;
    	avgRogue2F=avgRogue2F/count;
    	avgRoguelF=avgRoguelF/count;
    	avgRogueSF=avgRogueSF/count;
        System.out.print("Averages for " + count + " files : R1F : "+avgRogue1F+" R2F : "+avgRogue2F+ " RLF : "+avgRoguelF + " RSF : "+avgRogueSF);
    }
}
