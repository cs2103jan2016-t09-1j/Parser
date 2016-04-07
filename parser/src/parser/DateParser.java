package main.parser;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.joestelmach.natty.DateGroup;
import com.joestelmach.natty.Parser;

/**
 * This class is used to extract date form string and get to know which part of
 * string provides date
 * 
 * 
 */

public class DateParser {
	private static ArrayList<Date> dateList;
	private static String matchingValue = null;
	private static Parser dateParser = new Parser();

	/**
	 * extract a list of date from string
	 * 
	 * 
	 * @param description
	 * @return ArrayList<String>
	 */
	public static ArrayList<Date> getDateList(String description) {
		dateList = new ArrayList<>();
		List<DateGroup> groups = dateParser.parse(description);
		for (DateGroup group : groups) {
			dateList.addAll(group.getDates());
		}
		return dateList;
	}

	/**
	 * get the part of string which can extract a list of date
	 * 
	 * 
	 * @param description
	 * @return matchingValue
	 */
	public static String getMatchingValue(String description) {
		dateList = new ArrayList<>();
		List<DateGroup> groups = dateParser.parse(description);

		for (DateGroup group : groups) {
			matchingValue = group.getText();
		}
		return matchingValue;
	}
}
