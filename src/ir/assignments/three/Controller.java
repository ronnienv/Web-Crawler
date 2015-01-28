package ir.assignments.three;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

public class Controller {
	public static void main(String[] args) throws Exception {

		//gets stop words from file for later use
		HashMap<String, Integer> stopWords = new HashMap<String, Integer>();
		Scanner s = new Scanner(new File("StopWords.txt"));
		while(s.hasNext())
		{
			stopWords.put(s.nextLine(), 1);
		}
		s.close();
		System.out.println(stopWords.toString());
		
		Date startTime = Calendar.getInstance().getTime();
		String crawlStorageFolder = "dump";
		int numberOfCrawlers = 10;

		CrawlConfig config = new CrawlConfig();
		config.setUserAgentString("UCI Inf141-CS121 crawler 34201703 22768608");
		config.setPolitenessDelay(300);
		config.setResumableCrawling(false);
		config.setMaxPagesToFetch(200);
		config.setCrawlStorageFolder(crawlStorageFolder);

		/*
		 * Instantiate the controller for this crawl.
		 */
		PageFetcher pageFetcher = new PageFetcher(config);
		RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
		RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
		CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);

		/*
		 * For each crawl, you need to add some seed urls. These are the first
		 * URLs that are fetched and then the crawler starts following links
		 * which are found in these pages
		 */
		//            controller.addSeed("http://www.ics.uci.edu/~welling/");
		//            controller.addSeed("http://www.ics.uci.edu/~lopes/");
		controller.addSeed("http://www.ics.uci.edu/");

		/*
		 * Start the crawl. This is a blocking operation, meaning that your code
		 * will reach the line after this only when crawling is finished.
		 */
		MyCrawler mc = new MyCrawler();
		controller.start(mc.getClass(), numberOfCrawlers);    

		Date endTime = Calendar.getInstance().getTime();
		System.out.println("The program took a total of : " + (endTime.getTime()-startTime.getTime()) + " seconds");
		mc.printEndResults(stopWords);
	}
}