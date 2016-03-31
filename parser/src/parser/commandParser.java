package main.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import main.exception.DateOrderInvalidException;
import main.exception.ManyDateDetectedException;
import main.exception.NoDescriptionException;
import main.logic.Logic;
import main.logic.Task;
import main.logic.command.AddCommand;
import main.logic.command.CheckCommand;
import main.logic.command.ClearCommand;
import main.logic.command.Command;
import main.logic.command.DeleteCommand;
import main.logic.command.DisplayAllCommand;
import main.logic.command.EditCommand;
import main.logic.command.ExitCommand;
import main.logic.command.HelpCommand;
import main.logic.command.InvalidCommand;
import main.logic.command.OpenCommand;
import main.logic.command.RedoCommand;
import main.logic.command.SaveCommand;
import main.logic.command.SearchCommand;
import main.logic.command.ShowCommand;
import main.logic.command.SortCommand;
import main.logic.command.UndoCommand;

/**
 * Parses user input for a Command.
 *
 * 
 */

public class CommandParser {

	private static final String[] ADD_KEYWORDS = { "add", "i want to", "i have to", "i got to", "set" };
	private static final String[] DELETE_KEYWORDS = { "delete", "remove", "get rid of", "del" };
	private static final String[] EDIT_KEYWORDS = { "edit", "change" };
	private static final String[] EXIT_KEYWORDS = { "go away", "exit", "done", "leave" };
	private static final String[] UNDO_KEYWORDS = { "undo" };
	private static final String[] CLEAR_KEYWORDS = { "clear", "remove all" };
	private static final String[] DATE_SENSITIVE_WORDS = { "today", "tomorrow", "tmr", "the day after tomorrow",
			"yesterday" };
	private static final String[] CHECK_KEYWORDS = { "mark", "check", "uncheck", "unmark" };
	private static final String[] SEARCH_KEYWORDS = { "search", "find", "look for" };
	private static final String[] REDO_KEYWORDS = { "redo" };
	private static final String[] EDIT_START_KEYWORDS = { "-sta", "start", "start-time", "from" };
	private static final String[] EDIT_END_KEYWORDS = { "-end", "end", "end-time", "to" };
	private static final String[] EDIT_DESCRIPTION_KEYWORDS = { "-des", "description" };
	private static final String[] EDIT_DEADLINE_KEYWORDS = { "-dea", "deadline" };
	private static final String[] SHOW_KEYWORDS = { "show", "display" };

	private final static int NORMAL_FLAG = 1;
	private final static int SEARCH_FLAG = 2;
	private final static int SHOW_FLAG = 3;
	private final static int SORT_FLAG = 4;
	public static int current_status = NORMAL_FLAG;

	private static Logger addlogger = Logger.getLogger("addCommandLogger");

	/**
	 * Parses the user input and returns the most relevant command.
	 *
	 * @@author
	 * @param userInput
	 *            string of user input
	 * @return command
	 */
	public static Command parse(String userInput) {
		Command command;
		if (userInput.equals("")) {
			return new InvalidCommand("User input cannot be empty");
		}
		// if a command is generated, return it right away
		if ((command = parseAdd(userInput)) != null) {
			current_status = NORMAL_FLAG;
			return command;
		} else if ((command = parseDelete(userInput)) != null) {
			current_status = NORMAL_FLAG;
			return command;
		} else if ((command = parseEdit(userInput)) != null) {
			current_status = NORMAL_FLAG;
			return command;*/
		} else if ((command = parseExit(userInput)) != null) {
			return command;
		} else if ((command = parseSave(userInput)) != null) {
			current_status = NORMAL_FLAG;
			return command;
		} else if ((command = parseOpen(userInput)) != null) {
			current_status = NORMAL_FLAG;
			return command;
		} else if ((command = parseUndo(userInput)) != null) {
			current_status = NORMAL_FLAG;
			return command;
		} else if ((command = parseClear(userInput)) != null) {
			current_status = NORMAL_FLAG;
			return command;
		} else if ((command = parseCheck(userInput)) != null) {
			return command;
		} else if ((command = parseSearch(userInput)) != null) {
			current_status = SEARCH_FLAG;
			return command;
		} else if ((command = parseRedo(userInput)) != null) {
			current_status = NORMAL_FLAG;
			return command;
		} else if ((command = parseHelp(userInput)) != null) {
			return command;
		} else if ((command = parseShow(userInput)) != null) {
			current_status = SHOW_FLAG;
			return command;
		} else if ((command = parseDisplayAll(userInput)) != null) {
			current_status = NORMAL_FLAG;
			return command;
		} else if ((command = parseSort(userInput)) != null) {
			current_status = SORT_FLAG;
			return command;
		}
		return new InvalidCommand("Could you phrase a different way please?");
	}

	/**
	 * Parses for a save command.
	 *
	 * 
	 * @param userInput
	 * @return null or a SaveCommand
	 */
	private static Command parseSave(String userInput) {
		String userInputLowerCase = userInput.toLowerCase();
		int keywordIndex = userInputLowerCase.indexOf("save");
		if (keywordIndex == 0) { // save should be the first word
			int contentBeginIndex = keywordIndex + "save".length();
			String content = userInput.substring(contentBeginIndex).trim();
			return new SaveCommand(content);
		}
		return null;
	}

	/**
	 * Parses for an open command.
	 *
	 * @param userInput
	 * @return null or a OpenCommand
	 */
	private static Command parseOpen(String userInput) {
		String userInputLowerCase = userInput.toLowerCase();
		int keywordIndex = userInputLowerCase.indexOf("open");
		if (keywordIndex == 0) { // save should be the first word
			int contentBeginIndex = keywordIndex + "save".length();
			String content = userInput.substring(contentBeginIndex).trim();
			return new OpenCommand(content);
		}
		return null;
	}

	/**
	 * Parses for an add command.
	 *
	 * 
	 * @param userInput
	 *            string of user input
	 * @return the relevant add command or null
	 */

	private static Command parseAdd(String userInput) {
		String userInputLowerCase = userInput.toLowerCase();
		int keywordIndex;
		for (String keyword : ADD_KEYWORDS) {
			keywordIndex = userInputLowerCase.indexOf(keyword);
			if (keywordIndex == 0) {
				String content = userInput.replace(keyword, "").trim();
				addlogger.log(Level.INFO, "start to parser add command");
				try {
					return parseDate(content);
				} catch (NoDescriptionException e1) {
					addlogger.log(Level.WARNING, "error: No Description", e1);
					return new InvalidCommand(e1.getMessage());
				} catch (ManyDateDetectedException e2) {
					addlogger.log(Level.WARNING, "error: 3 or more date detected", e2);
					return new InvalidCommand(e2.getMessage());
				} catch (DateOrderInvalidException e3) {
					addlogger.log(Level.WARNING, "errow: start date is late than end date", e3);
					return new InvalidCommand(e3.getMessage());
				}
			}
		}
		return null;
	}

	/**
	 * Parses for a date within an add command.
	 *
	 *
	 * @param content
	 *            the description of the task the user wants to add
	 * @return the relevant add command
	 */
	private static Command parseDate(String content) {
		ArrayList<String> sensitiveWord = new ArrayList<>(Arrays.asList(DATE_SENSITIVE_WORDS));
		String description = "";
		// check if the description for add command have "" to indicate
		// task description manually
		String patternString = "\"(.*)\"";
		Pattern pattern = Pattern.compile(patternString);
		Matcher matcher = pattern.matcher(content);
		boolean find = matcher.find();
		String strPattern = "";

		if (find) {
			// if there is description indicated by user,
			// remove it and extract the time from the rest
			strPattern = matcher.group(0).replaceAll("\"", "");
			content = content.replace(strPattern, "");
		}

		ArrayList<Date> dateList = DateParser.getDateList(content);
		// 3 dates found will be an exception
		if (dateList.size() >= 3) {
			throw new ManyDateDetectedException(dateList.size());
		}
		if (!dateList.isEmpty()) {
			if (find) {
				description = strPattern;
			} else {
				String matchPattern = DateParser.getMatchingValue(content);
				String[] noPatternList = content.split(matchPattern);
				String[] leftDesList = noPatternList[0].split(" ");
				ArrayList<String> wordList = new ArrayList<>(Arrays.asList(leftDesList));
				if (!sensitiveWord.contains(matchPattern)) {
					wordList.remove(wordList.size() - 1);
				}
				for (String str : wordList) {
					description += str + " ";
				}
				for (int i = 1; i < noPatternList.length; i++) {
					description = description + noPatternList[i] + " ";
				}
				description = description.trim();
			}
			// 2 dates found: treat them as start time and end time
			if (dateList.size() == 2) {
				Date startDate = dateList.get(0);
				Date endDate = dateList.get(1);
				if (startDate.compareTo(endDate) < 0) {
					return new AddCommand(description, dateList.get(0), dateList.get(1));
				} else {
					throw new DateOrderInvalidException();
				}
				// 1 date found: treat it as deadline
			} else if (dateList.size() == 1) {
				return new AddCommand(description, dateList.get(0));
			}
		}
		if (find) {
			description = strPattern;
		} else {
			description = content;
		}
		if (description.trim().equals("")) {
			throw new NoDescriptionException();
		}
		addlogger.log(Level.INFO, "successful add command");
		return new AddCommand(description);
	}

	/**
	 * Parses for a delete command.
	 *
	 * 
	 * @param userInput
	 *            string of user input
	 * @return a delete command or an invalid command
	 */
	private static Command parseDelete(String userInput) {
		String userInputLowerCase = userInput.toLowerCase();
		int keywordIndex;
		for (String keyword : DELETE_KEYWORDS) {
			keywordIndex = userInputLowerCase.indexOf(keyword);
			if (keywordIndex == 0) {
				try {
					int indexBeginIndex = keywordIndex + keyword.length();
					String taskIndexStr = userInputLowerCase.substring(indexBeginIndex).trim();
					int taskIndex = Integer.parseInt(taskIndexStr) - 1;
					if (current_status == SEARCH_FLAG) {
						if (taskIndex >= Logic.foundTasks.size() || taskIndex < 0) {
							return new InvalidCommand("Please specify an index that's within range!");
						}
						Task tempT = Logic.foundTasks.get(taskIndex);
						taskIndex = Logic.tasks.indexOf(tempT);
					} else if (current_status == SHOW_FLAG) {
						if (taskIndex >= Logic.showTasks.size() || taskIndex < 0) {
							return new InvalidCommand("Please specify an index that's within range!");
						}
						Task tempT = Logic.showTasks.get(taskIndex);
						taskIndex = Logic.tasks.indexOf(tempT);
					} else if (current_status == SORT_FLAG) {
						if (taskIndex >= Logic.sortedTasks.size() || taskIndex < 0) {
							return new InvalidCommand("Please specify an index that's within range!");
						}
						Task tempT = Logic.sortedTasks.get(taskIndex);
						taskIndex = Logic.tasks.indexOf(tempT);
					} else {
						if (taskIndex >= Logic.tasks.size() || taskIndex < 0) {
							return new InvalidCommand("Please specify an index that's within range!");
						}
					}
					return new DeleteCommand(taskIndex);
				} catch (NumberFormatException numberFormatException) {
					return new InvalidCommand("Please specify your index correctly!");
				}
			}
		}
		return null;
	}

	/**
	 * Parses for an edit command.
	 *
	 * 
	 * @param userInput
	 *            string of user input
	 * @return an edit command or null
	 */
	
	private static Command parseEdit(String userInput) {

		// check if the command is long enough i.e. enough length for the syntax
		// <edit> <index> <details>
		String[] inputWords = userInput.split(" ");

		if (inputWords.length < 3) {
			return null;
		}

		// check if the command contains the edit keyword
		boolean containsEdit = false;
		for (String keyword : EDIT_KEYWORDS) {
			if (inputWords[0].toLowerCase().equals(keyword)) {
				containsEdit = true;
				break;
			}
		}

		if (!containsEdit) {
			return null;
		}

		// check if the index is valid
		int taskIndex;
		try {
			taskIndex = Integer.parseInt(inputWords[1]) - 1;
			if (current_status == SEARCH_FLAG) {
				if (taskIndex >= Logic.foundTasks.size() || taskIndex < 0) {
					return new InvalidCommand("Please specify an index that's within range!");
				}
				Task tempT = Logic.foundTasks.get(taskIndex);
				taskIndex = Logic.tasks.indexOf(tempT);
			} else if (current_status == SHOW_FLAG) {
				if (taskIndex >= Logic.showTasks.size() || taskIndex < 0) {
					return new InvalidCommand("Please specify an index that's within range!");
				}
				Task tempT = Logic.showTasks.get(taskIndex);
				taskIndex = Logic.tasks.indexOf(tempT);
			} else if (current_status == SORT_FLAG) {
				if (taskIndex >= Logic.sortedTasks.size() || taskIndex < 0) {
					return new InvalidCommand("Please specify an index that's within range!");
				}
				Task tempT = Logic.sortedTasks.get(taskIndex);
				taskIndex = Logic.tasks.indexOf(tempT);
			} else {
				if (taskIndex >= Logic.tasks.size() || taskIndex < 0) {
					return new InvalidCommand("Please specify an index that's within range!");
				}
			}
		} catch (NumberFormatException numberFormatException) {
			return new InvalidCommand("Please specify your index correctly!");
		}

		String details = "";
		for (int i = 2; i < inputWords.length; i++) {
			details += inputWords[i] + " ";
		}
		return parseEditDetails(details, taskIndex);
	}

	/**
	 * Parses for the details of an edit command.
	 *
	 * The edit command clears a field if the keyword is called but no argument
	 * is provided. Descriptions cannot be cleared. If a deadline is added to a
	 * task containing a duration, the duration is cleared, and vice versa. If
	 * both a deadline and a duration is provided, the deadline takes
	 * precedence.
	 *
	 *
	 * @param details
	 *            string of details
	 * @return an edit command
	 */
	
	private static Command parseEditDetails(String details, int taskIndex) {

		String[] words = details.split(" ");
		ArrayList<String> wordsList = new ArrayList<>(Arrays.asList(words));

		int deadlineBeginIndex = -1;
		int startBeginIndex = -1;
		int endBeginIndex = -1;
		int descriptionBeginIndex = -1;
		String tempStr = "";
		String newDescription = null;
		Date newDeadline = null;
		Date newStartDate = null;
		Date newEndDate = null;
		boolean clearDeadline = false;
		boolean clearDuration = false;

		// parse for the deadline
		for (String deadline_keyword : EDIT_DEADLINE_KEYWORDS) {
			if (wordsList.contains(deadline_keyword)) {
				deadlineBeginIndex = wordsList.indexOf(deadline_keyword);

				for (int i = deadlineBeginIndex + 1; i < wordsList.size(); i++) {
					if (i < wordsList.size()) {
						tempStr += wordsList.get(i) + " ";
					}
				}
				tempStr.trim();
				if (!tempStr.equals("")) {
					newDeadline = DateParser.getDateList(tempStr).get(0);
				}
				tempStr = "";
				if (newDeadline == null) {
					clearDeadline = true;
				}
				break;
			}
		}

		if (!clearDeadline) {
			// parse for the starting time
			for (String start_keyword : EDIT_START_KEYWORDS) {// go through all
																// the keywords
				if (wordsList.contains(start_keyword)) {// if any of the
														// keywords are present
					startBeginIndex = wordsList.indexOf(start_keyword);
					for (int i = startBeginIndex + 1; i < wordsList.size(); i++) {
						tempStr += wordsList.get(i) + " ";
					} // get the word directly after
					tempStr.trim();
					if (!tempStr.equals("")) {
						newStartDate = DateParser.getDateList(tempStr).get(0);
					}
					if (newStartDate == null) {
						clearDuration = true;
					}
					tempStr = "";
					break;
				}
			}

			for (String end_keyword : EDIT_END_KEYWORDS) {
				if (wordsList.contains(end_keyword)) {
					endBeginIndex = wordsList.indexOf(end_keyword);
					for (int i = endBeginIndex + 1; i < wordsList.size(); i++) {
						tempStr += wordsList.get(i) + " ";
					}
					tempStr.trim();
					if (!tempStr.equals("")) {
						newEndDate = DateParser.getDateList(tempStr).get(0);
					}
					if (newEndDate == null) {
						clearDuration = true;
					}
					tempStr = "";
					break;
				}
			}
		}

		for (String description_keyword : EDIT_DESCRIPTION_KEYWORDS) {
			if (wordsList.contains(description_keyword)) {
				descriptionBeginIndex = wordsList.indexOf(description_keyword);
				for (int i = descriptionBeginIndex + 1; i < wordsList.size(); i++) {
					if (Arrays.asList(EDIT_START_KEYWORDS).contains(wordsList.get(i))
							|| Arrays.asList(EDIT_END_KEYWORDS).contains(wordsList.get(i))
							|| Arrays.asList(EDIT_DEADLINE_KEYWORDS).contains(wordsList.get(i))) {
						break;
					}
					tempStr += wordsList.get(i) + " ";
				}
				tempStr.trim();
				newDescription = tempStr;
			}
		}

		if (newDescription == null && newDeadline == null && newStartDate == null && newEndDate == null
				&& !clearDeadline && !clearDuration) {
			return new InvalidCommand("Please phrase your edit command differently!");
		}

		// make sure start dates never come after end dates
		if (startBeginIndex != -1 && endBeginIndex != -1) {// both will be
															// changed
			if (newStartDate.compareTo(newEndDate) > 0) {
				return new InvalidCommand("Make sure your start date precedes your end date!");
			}
		} else if (startBeginIndex != -1) {
			if (newStartDate != null) {
				if (Logic.tasks.get(taskIndex).getEndDate() != null) {
					if (newStartDate.compareTo(Logic.tasks.get(taskIndex).getEndDate()) > 0) {
						return new InvalidCommand("Make sure your start date precedes your end date!");
					}
				} else {
					return new InvalidCommand("You can't edit just the start date for this task!");
				}
			}

		} else if (endBeginIndex != -1) {
			if (newEndDate != null) {
				if (Logic.tasks.get(taskIndex).getStartDate() != null) {
					if (Logic.tasks.get(taskIndex).getStartDate().compareTo(newEndDate) > 0) {
						return new InvalidCommand("Make sure your start date precedes your end date!");
					}
				} else {
					return new InvalidCommand("You can't edit just the end date for this task!");
				}
			}
		}

		return new EditCommand(taskIndex, newDescription, newDeadline, newStartDate, newEndDate, clearDeadline,
				clearDuration);
	}

	/**
	 * Parses for an exit command.
	 *
	 *
	 * @param userInput
	 *            string of user input
	 * @return exit command or null
	 */
	public static Command parseExit(String userInput) {
		String userInputLowerCase = userInput.toLowerCase();
		int keywordIndex;
		for (String keyword : EXIT_KEYWORDS) {
			keywordIndex = userInputLowerCase.indexOf(keyword);
			if (keywordIndex != -1) {
				return new ExitCommand();
			}
		}
		return null;
	}

	/**
	 * Parses for an undo command.
	 *
	 * 
	 * @param userInput
	 *            string of user input
	 * @return exit command or null
	 */
	public static Command parseUndo(String userInput) {
		String userInputLowerCase = userInput.toLowerCase();
		int keywordIndex;
		for (String keyword : UNDO_KEYWORDS) {
			keywordIndex = userInputLowerCase.indexOf(keyword);
			if (keywordIndex != -1) {
				return new UndoCommand();
			}
		}
		return null;

	}

	/**
	 * Parses for a redo command.
	 *
	 
	 * @param userInput
	 *            string of user input
	 * @return exit command or null
	 */
	public static Command parseRedo(String userInput) {
		String userInputLowerCase = userInput.toLowerCase();
		int keywordIndex;
		for (String keyword : REDO_KEYWORDS) {
			keywordIndex = userInputLowerCase.indexOf(keyword);
			if (keywordIndex != -1) {
				return new RedoCommand();
			}
		}
		return null;
	}

	/**
	 * parses for clear all tasks
	 *
	 * 
	 * @param userInput
	 *            string of user input
	 * @return clear command which clear the tasks
	 */
	private static Command parseClear(String userInput) {
		String userInputLowerCase = userInput.toLowerCase();
		int keywordIndex;
		for (String keyword : CLEAR_KEYWORDS) {
			keywordIndex = userInputLowerCase.indexOf(keyword);
			if (keywordIndex != -1) {
				return new ClearCommand();
			}
		}
		return null;
	}

	/**
	 * parses for help
	 *
	 *
	 * @param userInput
	 *            string of user input
	 * @return clear command which clear the tasks
	 */
	private static Command parseHelp(String userInput) {
		if (userInput.equalsIgnoreCase("help")) {
			return new HelpCommand();
		}
		return null;
	}

	/**
	 * parses for show all the tasks
	 *
	 *
	 * @param userInput
	 *            string of user input
	 * @return displayAllCommand command
	 */
	private static Command parseDisplayAll(String userInput) {
		if (userInput.equalsIgnoreCase("displayAll")) {
			return new DisplayAllCommand();
		}
		return null;
	}

	/**
	 * Parses for the CheckCommand.
	 *
	 *
	 * @param details
	 *            string of details
	 * @return a CheckCommand
	 */
	private static Command parseCheck(String userInput) {
		String userInputLowerCase = userInput.toLowerCase();
		int keywordIndex;
		String[] split = userInputLowerCase.split("\\s+");
		for (String keyword : CHECK_KEYWORDS) {
			keywordIndex = userInputLowerCase.indexOf(keyword);
			if (keywordIndex != -1) {
				try {
					int indexBeginIndex = keywordIndex + keyword.length();
					String taskIndexStr = userInputLowerCase.substring(indexBeginIndex).trim();
					int taskIndex = Integer.parseInt(taskIndexStr) - 1;

					if (current_status == SEARCH_FLAG) {
						if (taskIndex >= Logic.foundTasks.size() || taskIndex < 0) {
							return new InvalidCommand("Please specify an index that's within range!");
						}
						Task tempT = Logic.foundTasks.get(taskIndex);
						taskIndex = Logic.tasks.indexOf(tempT);
					} else if (current_status == SHOW_FLAG) {
						if (taskIndex >= Logic.showTasks.size() || taskIndex < 0) {
							return new InvalidCommand("Please specify an index that's within range!");
						}
						Task tempT = Logic.showTasks.get(taskIndex);
						taskIndex = Logic.tasks.indexOf(tempT);
					} else if (current_status == SORT_FLAG) {
						if (taskIndex >= Logic.sortedTasks.size() || taskIndex < 0) {
							return new InvalidCommand("Please specify an index that's within range!");
						}
						Task tempT = Logic.sortedTasks.get(taskIndex);
						taskIndex = Logic.tasks.indexOf(tempT);
					} else {
						if (taskIndex >= Logic.tasks.size() || taskIndex < 0) {
							return new InvalidCommand("Please specify an index that's within range");
						}
					}

					if (split[0].equals("unmark") || split[0].equals("uncheck")) {
						return new CheckCommand(false, taskIndex);
					} else {
						return new CheckCommand(true, taskIndex);
					}

				} catch (NumberFormatException numberFormatException) {
					return new InvalidCommand("Please specify your index correctly!");
				}
			}
		}
		return null;
	}

	/**
	 * Parses for the SearchCommand.
	 *
	 *
	 * @param details
	 *            string of details
	 * @return a SearchCommand
	 */
	
	private static Command parseSearch(String userInput) {
		String userInputLowerCase = userInput.toLowerCase();
		String[] split = userInputLowerCase.split("\\s+");
		ArrayList<String> list = new ArrayList<String>();
		for (String keyword : SEARCH_KEYWORDS) {
			if (keyword.equals(split[0])) {
				for (int i = 1; i < split.length; i++) {
					list.add(split[i]);
				}
				return new SearchCommand(list);
			}
		}
		return null;
	}

	/**
	 * Parses for the ShowCommand.
	 *
	
	 * @param details
	 *            string of details
	 * @return a ShowCommand
	 */

	private static Command parseShow(String userInput) {
		String userInputLowerCase = userInput.toLowerCase();
		int keywordIndex;
		ArrayList<String> list = new ArrayList<String>();
		String description = "";
		String[] split = userInputLowerCase.split("\\s+");
		for (String keyword : SHOW_KEYWORDS) {
			keywordIndex = userInputLowerCase.indexOf(keyword);
			if (keywordIndex != -1) {
				for (int i = 1; i < split.length; i++) {
					list.add(split[i]);
				}
				break;
			}
		}

		if (list.size() == 0) { // escape the show command
			return null;
		}

		for (int i = 0; i < list.size(); i++) {
			description += list.get(i);
		}

		if (description.equals("floatingtask") || description.equals("floatingtasks")
				|| description.equals("floating")) {
			description = "floating";
		} else if (description.equals("deadlinetask") || description.equals("deadlinetasks")
				|| description.equals("deadline")) {
			description = "deadline";
		} else if (description.equals("durationtask") || description.equals("durationtasks")
				|| description.equals("duration")) {
			description = "duration";
		} else if (description.equals("completed") || description.equals("finished")) {
			description = "completed";
		} else if (description.equals("uncompleted") || description.equals("incompleted")) {
			description = "uncompleted";
		} else if (description.equals("today")) {
			description = "today";
		} else if (description.equals("tomorrow") || description.equals("tmr")) {
			description = "tomorrow";
		} else {
			return new InvalidCommand("No such show command");
		}
		if (!(description.equals(null))) {
			return new ShowCommand(description);
		}

		return null;
	}

	/**
	 * Parses for a sort command.
	 *
	 * 
	 * @param userInput
	 *            string of user input
	 * @return sort command or null
	 */
	public static Command parseSort(String userInput) {
		String userInputLowerCase = userInput.toLowerCase();

		String[] str_arr = userInputLowerCase.split(" ");
		String sortKey;

		if (userInput.equals("sort") || userInput.equals("arrange")) {
			return new SortCommand("No sortKey");
		}

		if (str_arr.length != 2) {
			return null;
		}
		sortKey = str_arr[1];

		if (sortKey.equals("des")) {
			return new SortCommand("description");
		} else if (sortKey.equals("dl")) {
			return new SortCommand("deadline");
		} else {
			return null;
		}

	}
}
