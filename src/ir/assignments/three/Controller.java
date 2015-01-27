package ir.assignments.three;

import java.util.Calendar;
import java.util.Date;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

public class Controller {
    public static void main(String[] args) throws Exception {

    		Date startTime = Calendar.getInstance().getTime();
            String crawlStorageFolder = "dump";
            int numberOfCrawlers = 7;

            CrawlConfig config = new CrawlConfig();
            config.setUserAgentString("UCI Inf141-CS121 crawler 34201703 22768608");
            config.setPolitenessDelay(300);
            config.setResumableCrawling(false);
            config.setMaxPagesToFetch(13);
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
            System.out.print("The program took a total of : " + (endTime.getTime()-startTime.getTime()) + " seconds");
            mc.printEndResults();
    }
}