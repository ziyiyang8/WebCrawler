import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.StringTokenizer;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Main crawler class.
 * The web crawler is initialized with one seed, max pages to crawl, and a domain (optional).
 * The crawler will save all textual content in a folder called repository (created during initialization if it did not exist already).
 * It will also store additional attributes in an html file called report.html.
 * Afterwards, it will continue to crawl all outlinks.  The crawler will not crawl a link if robots.txt disallows it.
 *
 */
public class WebCrawler {
	private Queue<String> seeds; 		// queue of URLs to visit (frontier)
	private Set<String> pagesVisited; 	// set of URLs that have already been visited
	private int maxPages;				// max number of pages to crawl for
	private String domain;				// optional domain to limit our search to
	
	// use an user agent so web browsers do not get confused at our crawler
	private final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/13.0.782.112 Safari/535.1";
	
	/**
	 * Initialize our crawler based on input file.
	 * 
	 * @param input	specifications.csv
	 * @throws IllegalArgumentException bad input file
	 */
	public WebCrawler(FileReader input) throws IllegalArgumentException
	{
		seeds = new LinkedList<String>();	
		pagesVisited = new HashSet<String>();
		
		// Create the repository directory, if it does not exist, to store our crawled data in.
		File dir = new File("repository");
		if (!dir.exists())
			dir.mkdirs();
		
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
	 * Crawl to a given url, add all links on the page to our seeds queue.
	 * 
	 * @param url
	 */
	public void crawl(String url)
	{
		try
		{
			// make HTTP request to url using Jsoup
			Document htmlDoc = Jsoup.connect(url).get();			
			// get all the links on the page
			Elements linksOnPage = htmlDoc.select("a[href]");
	        for(Element link : linksOnPage)
	        {
	        	// add each link url to our seeds queue
	        	seeds.offer(link.absUrl("href"));
	        }
		}
        catch(IOException e)
        {
			e.printStackTrace();
        }
	}

	/**
	 * For politeness policy.
	 * Check if a url is safe to crawl by checking robots.txt
	 * 
	 * @param url
	 * @return
	 */
	public boolean robotSafe(String url)
	{
		URL aURL;			// we use URL object to easily parse for domain part of URL
		String host;		// string to store domain of URL
		String robotsTXT;	// string to store robots.txt
		
		// make sure url has protocol in it so nothing weird happens during parsing for host name, etc.
		if (url.startsWith("http:/"))
		{
	        if (!url.contains("http://")) 
	            url = url.replaceAll("http:/", "http://");
	    } else 
	        url = "http://" + url;
	    // add a '/' to end of url if it does not have it. This is to ensure our matching later is accurate
		if (url.charAt(url.length() -1) != '/')
			url = url + "/";
		
		// parse input URL to access <host>/robots.txt file
		try {
			aURL = new URL(url);
			// get only the domain part of URL
			host = aURL.getHost();	
			// have to use secure communication (https) because some robots.txt are located there
			robotsTXT = "https://" + host + "/robots.txt";
		// if anything bad happens, then assume url not safe to crawl
		} catch (MalformedURLException e) {
			return false;
		}
		
		try {
			// use Jsoup to grab robots.txt into a string. We use an user agent because some web servers
			// will refuse the connection and return a 403 status code.
			Document robot = Jsoup.connect(robotsTXT).userAgent(USER_AGENT).get();
			String robotStr = robot.text();
			
			// get category part of url to later match with disallowed categories.
			// www.<host>.com/<category>
			String category = aURL.getFile();
			
			// parse through robot.txt string, checking only for disallowed categories
			int index = 0;
			while ((index = robotStr.indexOf("Disallow:", index)) != -1) 
			{
			    index += "Disallow:".length();
			    String strPath = robotStr.substring(index);
			    StringTokenizer st = new StringTokenizer(strPath);

			    // no more tokens, we are done
			    if (!st.hasMoreTokens())
			    	break;
			    
			    // get the disallowed category
			    String disallowed = st.nextToken();
			    // if the URL category matches with a disallowed path, we can not crawl the URL
			    if (category.indexOf(disallowed) == 0)
			    	return false;
			}
		// if something bad happened, assume we should not crawl url
		} catch (IOException e) {
			return false;
		}
		// if everything still all good, then url must be safe to crawl
		return true;
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
	 */
	public static void main(String[] args) {
		try {
			FileReader file = new FileReader("specification.csv");
			WebCrawler crawler = new WebCrawler(file);
			System.out.println(crawler.robotSafe("google.com/search/"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

}
