import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 *  Goes through all downloaded pages in repository folder and removes noise
 *  such as main navigation bar and advertisements
 *
 */
public class ContentProcessor {

	/**
	 * Initializes the ContentProcessor to loop through all files in repository
	 * directory and remove noise.
	 * 
	 * @throws Exception
	 */
	public ContentProcessor() throws Exception
	{
		File dir = new File("repository");
		File[] repository = dir.listFiles();
		if (repository != null) 
		{
		    for (File page : repository) 
		    {
		    	removeNoise(page);
		    }
		} 
		else 
			throw new Exception ("repository folder not found");
	}
	
	/**
	 * This method will remove noise from a given input html page.
	 * 
	 * @param page
	 */
	public void removeNoise(File page)
	{
		BufferedWriter output = null;
		
		try {
			// use Jsoup to parse a html page in repository folder
			Document doc = Jsoup.parse(page, "UTF-8");
			
			// TODO algorithm to remove noise
			
			// get text content of page after noise removal
			String noiseFreeContent = doc.outerHtml();
			
			// write to page with noise content removed
			output = new BufferedWriter(new FileWriter(page));
			output.write(noiseFreeContent);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		try {
			ContentProcessor cp = new ContentProcessor();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
