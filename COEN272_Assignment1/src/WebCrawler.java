import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

/**
 * Main crawler class.
 *
 */
public class WebCrawler {
	private Queue<String> seeds; 		// queue of URLs to visit (frontier)
	private Set<String> pagesVisited; 	// set of URLs that have already been visited
	private int maxPages;				// max number of pages to crawl for
	private String domain;				// optional domain to limit our search to
	
	/**
	 * Initialize our crawler based on input file.
	 * 
	 * @param input	specifications.csv
	 * @throws IllegalArgumentException
	 */
	public WebCrawler(FileReader input) throws IllegalArgumentException
	{
		seeds = new LinkedList<String>();	
		pagesVisited = new HashSet<String>();
		// Create the directory to store our crawled data in.
		File dir = new File("repository");
		if (!dir.exists())
			dir.mkdirs();
		// if directory exists, then delete all files in it.
		else
		{
			for (File f : dir.listFiles())
				f.delete();
		}
		BufferedReader br = new BufferedReader(input);
		try {
			// read line from file and remove any unnecessary whitespaces
			String line = br.readLine().replaceAll("\\s+","");
			// input file will be comma separated
			String [] fields = line.split(",");
			// valid input file
			if (fields.length >= 2 && fields.length <= 3)
			{
				seeds.offer(fields[0]); // put first seed URL on queue
				maxPages = Integer.parseInt(fields[1]); 
				if (fields.length == 3) // a domain is specified
					domain = fields[2];
			}
			// throw exception if bad input file
			else
				throw new IllegalArgumentException("Invalid input file");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
		
	/**
	 * Get the next URL to crawl to, making sure that URL has not been visited yet.
	 * 
	 * @return url
	 */
	private String nextURL()
	{
		String url = seeds.poll();	// pop URL off queue
		// keep popping off queue until we get an URL not yet visited
		while (pagesVisited.contains(url))
			url = seeds.poll();
		return url;
	}

	/**
	 * For testing purposes.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			FileReader file = new FileReader("specification.csv");
			WebCrawler crawler = new WebCrawler(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

}
