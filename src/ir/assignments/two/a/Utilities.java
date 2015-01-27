//Jeffrey Fellows 34201703
package ir.assignments.two.a;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * A collection of utility methods for text processing.
 */
public class Utilities {
	/**
	 * Reads the input text file and splits it into alphanumeric tokens.
	 * Returns an ArrayList of these tokens, ordered according to their
	 * occurrence in the original text file.
	 * 
	 * Non-alphanumeric characters delineate tokens, and are discarded.
	 *
	 * Words are also normalized to lower case. 
	 * 
	 * Example:
	 * 
	 * Given this input string
	 * "An input string, this is! (or is it?)"
	 * 
	 * The output list of strings should be
	 * ["an", "input", "string", "this", "is", "or", "is", "it"]
	 * 
	 * @param input The file to read in and tokenize.
	 * @return The list of tokens (words) from the input file, ordered by occurrence.
	 */
	public static ArrayList<String> tokenizeFile(File input) {

		Scanner s = null;
		try {
			s = new Scanner(input);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		ArrayList<String> returner = new ArrayList<String>();

		while(s.hasNext())
		{
			//String temp = s.next();
			for(String each: s.next().replaceAll("[^a-zA-Z0-9']", " ").split(" "))
				if(!each.isEmpty())
					returner.add(each.toLowerCase());
		}

		s.close();
		return returner;
	}


	public static ArrayList<String> tokenizeString(String text) {

		Scanner s = null;

		s = new Scanner(text);

		ArrayList<String> returner = new ArrayList<String>();

		while(s.hasNext())
		{
			//String temp = s.next();
			for(String each: s.next().replaceAll("[^a-zA-Z0-9]", " ").split(" "))
				if(!each.isEmpty())
					returner.add(each.toLowerCase());
		}

		s.close();
		return returner;
	}

	/**
	 * Takes a list of {@link Frequency}s and prints it to standard out. It also
	 * prints out the total number of items, and the total number of unique items.
	 * 
	 * Example one:
	 * 
	 * Given the input list of word frequencies
	 * ["sentence:2", "the:1", "this:1", "repeats:1",  "word:1"]
	 * 
	 * The following should be printed to standard out
	 * 
	 * Total item count: 6
	 * Unique item count: 5
	 * 
	 * sentence	2
	 * the		1
	 * this		1
	 * repeats	1
	 * word		1
	 * 
	 * 
	 * Example two:
	 * 
	 * Given the input list of 2-gram frequencies
	 * ["you think:2", "how you:1", "know how:1", "think you:1", "you know:1"]
	 * 
	 * The following should be printed to standard out
	 * 
	 * Total 2-gram count: 6
	 * Unique 2-gram count: 5
	 * 
	 * you think	2
	 * how you		1
	 * know how		1
	 * think you	1
	 * you know		1
	 * 
	 * @param frequencies A list of frequencies.
	 */

	//assumes 
	public static void printFrequencies(List<Frequency> frequencies) {
		int sum = 0;
		StringBuffer sb = new StringBuffer(); 

		if(frequencies == null || frequencies.isEmpty())
		{
			System.out.println("Total item count: 0");
			System.out.println("Unique item count: 0");
			return;
		}

		// if single words
		if(!frequencies.get(0).getText().contains(" "))
		{
			for(Frequency f: frequencies)
			{
				sb.append(f.getText() + " " + f.getFrequency() + "\n");
				sum += f.getFrequency();
			}

			System.out.println("Total item count: " + sum);
			System.out.println("Unique item count: " + frequencies.size() + "\n");
			System.out.println(sb.toString());
		}
		//else two words per token
		else
		{
			for(Frequency f: frequencies)
			{
				sb.append(f.getText() + " " + f.getFrequency() + "\n");
				sum += f.getFrequency();
			}
			System.out.println("Total 2-gram count: " + sum);
			System.out.println("Unique 2-gram count: " + frequencies.size() + "\n");
			System.out.println(sb.toString());
		}

	}

}
