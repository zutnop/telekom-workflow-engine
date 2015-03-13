package ee.telekom.workflow.util;

/**
 * Utility class for conversion between "Y"/"N"/null and true/false/null values
 */
public class YesNoUtil {

	public static final String YES_AS_STRING = "Y";
	public static final String NO_AS_STRING = "N";

	/**
	 * Converts the given flag into a string of either "Y", "N" or null.
	 */
	public static String asString(Boolean flag) {
		return flag == null ? null : (flag ? YES_AS_STRING : NO_AS_STRING);
	}

	/**
	 * Converts the given string into a Boolean flag of either true, false or
	 * null.
	 */
	public static Boolean asBoolean(String flag) {
		return flag == null ? null : flag.equals(YES_AS_STRING);
	}

}
