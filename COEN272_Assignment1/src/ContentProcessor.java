import java.io.File;

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
	
	public void removeNoise(File page)
	{
		
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
