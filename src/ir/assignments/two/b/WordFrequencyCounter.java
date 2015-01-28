//Jeffrey Fellows 34201703
package ir.assignments.two.b;

import ir.assignments.two.a.Frequency;
import ir.assignments.two.a.Utilities;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * Counts the total number of words and their frequencies in a text file.
 */
public final class WordFrequencyCounter {
	/**
	 * This class should not be instantiated.
	 */
	private WordFrequencyCounter() {}

	/**
	 * Takes the input list of words and processes it, returning a list
	 * of {@link Frequency}s.
	 * 
	 * This method expects a list of lowercase alphanumeric strings.
	 * If the input list is null, an empty list is returned.
	 * 
	 * There is one frequency in the output list for every 
	 * unique word in the original list. The frequency of each word
	 * is equal to the number of times that word occurs in the original list. 
	 * 
	 * The returned list is ordered by decreasing frequency, with tied words sorted
	 * alphabetically.
	 * 
	 * The original list is not modified.
	 * 
	 * Example:
	 * 
	 * Given the input list of strings 
	 * ["this", "sentence", "repeats", "the", "word", "sentence"]
	 * 
	 * The output list of frequencies should be 
	 * ["sentence:2", "the:1", "this:1", "repeats:1",  "word:1"]
	 *  
	 * @param words A list of words.
	 * @return A list of word frequencies, ordered by decreasing frequency.
	 */
	public static List<Frequency> computeWordFrequencies(List<String> words) {
		List<Frequency> wf = new ArrayList<Frequency>();
		HashMap<String, Integer> hm = new HashMap<String, Integer>();

		if(words == null)
			return wf;
		//System.out.println(words.toString());
		//populate hashmap with tokens and values;
		for(String word: words)
		{
			if(hm.containsKey(word))
				hm.put(word, hm.get(word)+1);
			else
				hm.put(word, 1);
		}

		int max = 0;
		//find largest number
		//used as starting location
		for(Integer value: hm.values())
		{
			if (value > max)
				max = value;
		}
				
		//put all of one number in an array
		//sort the array
		//put in to list with frequency
		while(max > 0)
		{
			ArrayList<String> al = new ArrayList<String>();

			for(String key: hm.keySet())
			{
				if(max == hm.get(key))
				{
					al.add(key);
				}
			}
			
			String[] toSort = Arrays.copyOf(al.toArray(), al.size(), String[].class);
			Arrays.sort(toSort);
			
			for(int i = 0; i < toSort.length; i++)
			{
				wf.add(new Frequency(toSort[i], max));
			}
			
			max--;
		}
		return wf;
	}
	
	public static List<Frequency> computeWordFrequencies(List<String> words, int outputNumber) {
		List<Frequency> wf = new ArrayList<Frequency>();
		HashMap<String, Integer> hm = new HashMap<String, Integer>();

		if(words == null)
			return wf;
		//System.out.println(words.toString());
		//populate hashmap with tokens and values;
		for(String word: words)
		{
			if(hm.containsKey(word))
				hm.put(word, hm.get(word)+1);
			else
				hm.put(word, 1);
		}

		int max = 0;
		//find largest number
		//used as starting location
		for(Integer value: hm.values())
		{
			if (value > max)
				max = value;
		}
				
		//put all of one number in an array
		//sort the array
		//put in to list with frequency
		while(max > 0 && outputNumber > 0)
		{
			ArrayList<String> al = new ArrayList<String>();

			for(String key: hm.keySet())
			{
				if(max == hm.get(key))
				{
					al.add(key);
				}
			}
			
			String[] toSort = Arrays.copyOf(al.toArray(), al.size(), String[].class);
			Arrays.sort(toSort);
			
			for(int i = 0; i < toSort.length; i++)
			{
				wf.add(new Frequency(toSort[i], max));
			}
			
			max--;
			outputNumber--;
		}
		return wf;
	}
	
	//comparator for frequency of tokens for priority queue use
	public static Comparator<Frequency> freqComparator = new Comparator<Frequency>(){

		public int compare(Frequency o1, Frequency o2) {
			// if returns + o2 is bigger, if - o1 is bigger
			int difference = o2.getFrequency() - o1.getFrequency();
			if (difference != 0){
				return difference;
			}
			//they start with same character, so alphabetize
			else{
				String string1 = o1.getText();
				String string2 = o2.getText();
				
				int index = 0;
				while(string1.charAt(index) == string2.charAt(index)){
					index++;
					//if we are at the end of a word the smaller word should appear first
					if(index > string1.length() -1 || index > string2.length() -1){
						if(string1.length() >= string2.length()){
							return 1;
						}
						else{
							return -1;
						}
					}
				}
				if(string1.charAt(index) > string2.charAt(index)){
					return 1;
				}
				else{
					return -1;
				}
				
			}
		}
	};

	/**
	 * Runs the word frequency counter. The input should be the path to a text file.
	 * 
	 * @param args The first element should contain the path to a text file.
	 */
	public static void main(String[] args) {
		File file = new File(args[0]);
		List<String> words = Utilities.tokenizeFile(file);
		List<Frequency> frequencies = computeWordFrequencies(words);
		Utilities.printFrequencies(frequencies);
	}
}
