import java.io.*;
import java.util.stream.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;

public class Parser
{
    private String data;

    // Number of files to parse, if not specified parse all
    private int num = -1;

    // Number of flies parsed in a single call to Parse()
    private int num_parsed = 0;

    public Parser(String[] args)
    {

        data = System.getenv("PROJECT_HOME") + File.separator + "data";

        if(args.length > 1)
            throw new IllegalArgumentException("There can be maximum of one command line argument");
        else if(args.length == 1)
        {
            num = Integer.parseInt(args[0]);
        } 
    }

    private boolean Extract(Path path)
    {
        try(BufferedReader br = new BufferedReader(new FileReader(path.toString())))
        {
            String dir_parsed = data + File.separator + "parsed";
            Files.createDirectories(Paths.get(dir_parsed));

            String parsed_summary = dir_parsed + File.separator + "summary" + File.separator  + path.getFileName().toString();
            String parsed_text = dir_parsed + File.separator + "text" + File.separator + path.getFileName().toString();

            try
            {
                File out_summary = new File(parsed_summary);
                out_summary.getParentFile().mkdirs();
                if(out_summary.exists())
                    return false;

                out_summary.createNewFile();
                BufferedWriter bw_summary = new BufferedWriter(new FileWriter(out_summary));

                // Assuming there are at least two lines in each file
                String line = br.readLine();

                // Headline (which is typically first line) starts with
                // "(CNN) --", remove it
                String token = "(CNN)";
                int idx = line.indexOf(token);
                if(idx != -1)
                    line = line.substring(idx + token.length());
                token = "-- ";
                idx = line.indexOf(token);
                if(idx != -1)
                    line = line.substring(idx + token.length());

                bw_summary.write(line);
                bw_summary.flush();
                bw_summary.close();
                
    	    	File out_text = new File(parsed_text);
    	    	out_text.getParentFile().mkdirs();
                if(out_text.exists())
                    return false;
                out_text.createNewFile();
                BufferedWriter bw_text = new BufferedWriter(new FileWriter(out_text));
                
                line = br.readLine();
                while(line != null && !line.equals("@highlight"))
                {
                    if(line.length()>0)
                    {
                    	bw_text.write(line + " ");
                    }
                    line = br.readLine();
                }
                bw_text.flush();
                bw_text.close();
            }
            catch(Exception e)
            {
                throw e;
            }
            
        }
        catch(Exception e)
        {
            System.out.println("Error during data extraction: " + e.getMessage());
        }

        num_parsed++;
        if(num_parsed % 1000 == 0)
        {
            int x = num_parsed/1000;
            System.out.println("Parsed " + Integer.toString(x) + "k files...");
        }

        return true;
    }

    public void Parse() throws Exception
    {
        num_parsed = 0;
        String dir = data + File.separator + "stories";
        
        try(Stream<Path> paths = Files.walk( Paths.get(dir) ))
        {
            System.out.println("Parsing started...");
            if(num == -1)
            {
                paths
                    .filter(Files::isRegularFile)
                    .forEach( path -> Extract(path) );
            }
            else
            {
                paths
                    .filter(Files::isRegularFile)
                    .filter( path -> Extract(path) )
                    .limit(num)
                    .forEach( path -> {} );
            }
            System.out.println(Integer.toString(num_parsed) + " files parsed.");
        }
        catch(Exception e)
        {
            throw e;
        }
    }

   public static void main(String[] args) throws Exception
   {
       try
       {
           Parser P = new Parser(args);
           P.Parse();
       }
       catch(Exception e)
       {
           System.out.println("Error during parsing: " + e.getMessage());
       }
   }
}

