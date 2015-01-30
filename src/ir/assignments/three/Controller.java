package ir.assignments.three;

import ir.assignments.two.a.Frequency;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
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
		//		System.out.println(stopWords.toString());

		Date startTime = Calendar.getInstance().getTime();
		String crawlStorageFolder = "dump";
		int numberOfCrawlers = 10;

		CrawlConfig config = new CrawlConfig();
		config.setUserAgentString("UCI Inf141-CS121 crawler 34201703 22768608");
		config.setPolitenessDelay(300);
		config.setResumableCrawling(false);
		config.setMaxPagesToFetch(50);
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
		
		
//		controller.addSeed("http://www.ics.uci.edu/");
		try {
			s = new Scanner(new File("queue.txt"));
			while(s.hasNext())
				controller.addSeed(s.next().trim());
			s.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		/*
		 * Start the crawl. This is a blocking operation, meaning that your code
		 * will reach the line after this only when crawling is finished.
		 */
		Crawler c = new Crawler();
		c.loadData();
		controller.start(c.getClass(), numberOfCrawlers);   
		c.printToFile();
		
		Date endTime = Calendar.getInstance().getTime();
		PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter("metadata.txt", true)));
		pw.write(endTime.getTime() + " " + startTime.getTime() + " ");
		pw.close();
		System.out.println("The program took a total of : " + (endTime.getTime()-startTime.getTime()) + " seconds");
		updateIndex();
		//c.printEndResults(stopWords);
	}

	public static void updateIndex()
	{
		try {
			Scanner s = new Scanner(new File("PreIndex.txt"));
			Scanner s2 = new Scanner(new File("Index.txt"));
			HashMap<String, ArrayList<Frequency>> preIndex = new HashMap<String, ArrayList<Frequency>>();
			HashMap<String, ArrayList<Frequency>> index = new HashMap<String, ArrayList<Frequency>>();

			//import index into memory
			while(s2.hasNextLine())
			{	
				Scanner line = new Scanner(s2.nextLine());
				String key = line.next();
				while(line.hasNext())
				{
					String url = line.next();
					int value = line.nextInt();
					if(index.containsKey(key))
					{
						ArrayList<Frequency> al = index.get(key);
						al.add(new Frequency(url, value));
					}
					else
					{
						ArrayList<Frequency> al = new ArrayList<Frequency>();
						al.add(new Frequency(url, value));
						index.put(key, al);
					}

					//index.put(key, valu)
				}
				line.close();
			}

			//import preindex and place into index
			while(s.hasNextLine())
			{	
				Scanner line = new Scanner(s.nextLine());
				String url = line.next();
				while(line.hasNext())
				{
					String key = line.next();
					int value = line.nextInt();
					if(index.containsKey(key))
					{
						ArrayList<Frequency> al = index.get(key);
						al.add(new Frequency(url, value));
					}
					else
					{
						ArrayList<Frequency> al = new ArrayList<Frequency>();
						al.add(new Frequency(url, value));
						index.put(key, al);
					}
					//index.put(key, valu)

				}
				line.close();
			}
			s.close();
			s2.close();
			PrintWriter pw = new PrintWriter("Index.txt");
			for(String key: index.keySet())
			{
				pw.write(key);
				ArrayList<Frequency> al = index.get(key);
				for(Frequency f : al)
				{
					pw.write(" " + f.getText() + " " + f.getFrequency());
				}
				pw.write("\n");
			}
			pw.close();
			

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		System.out.println("Index updated");
	}
}