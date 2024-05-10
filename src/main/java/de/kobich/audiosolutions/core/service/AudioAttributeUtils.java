package de.kobich.audiosolutions.core.service;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import de.kobich.audiosolutions.core.service.AudioDataChange.AudioDataChangeBuilder;


/**
 * Utility class to convert audio attributes
 * @author ckorn
 */
public class AudioAttributeUtils {
	private static final Logger logger = Logger.getLogger(AudioAttributeUtils.class);
	
	private AudioAttributeUtils() {}
	
	/**
	 * Returns a value object
	 * @param value
	 * @param clazz
	 * @return
	 * @see AudioAttribute#getType()
	 */
	public static Object getObjectValue(String value, Class<?> clazz) {
		Object obj = null;
		if (String.class.equals(clazz)) {
			obj = value;
		}
		else if (Integer.class.equals(clazz)) {
			obj = convert2Integer(value);
		}
		else if (Date.class.equals(clazz)) {
			obj = convert2Date(value);
		}
		else if (RatingType.class.equals(clazz)) {
			obj = convert2RatingType(value);
		}
		else {
			logger.warn("Type " + clazz + " is not supported");
		}
		return obj;
	}
	
	/**
	 * Converts from text to RatingType
	 * @param ratingType
	 * @return
	 */
	public static RatingType convert2RatingType(String ratingType) {
		return RatingType.getByName(ratingType);
	}
	
	/**
	 * Converts from RatingType to text
	 * @param ratingType
	 * @return
	 */
	public static String convert2String(RatingType ratingType) {
		return ratingType.name();
	}
	
	/**
	 * Converts from text to date, e.g. for publication
	 * @return
	 */
	public static Date convert2Date(String dateString) {
		Date date = null;
		if (dateString != null) {
			String sep = ".";
			if (dateString != null) {
				if (dateString.contains("-")) {
					sep = "-";
				}
				else if (dateString.contains(" ")) {
					sep = " ";
				}
				else if (dateString.contains("/")) {
					sep = "/";
				}
				String[] datePatterns = { "yyyy" + sep + "MM" + sep + "dd", "yyyy" + sep + "MM", "yyyy" };
				for (String datePattern : datePatterns) {
					if (datePattern.length() != dateString.length()) {
						continue;
					}
					
					SimpleDateFormat dateFormat = new SimpleDateFormat(datePattern);
					try {
						date = dateFormat.parse(dateString);
						logger.debug("Date pattern: " + datePattern);
						break;
					}
					catch (ParseException exc) {
						logger.debug("Album publication cannot be casted to date", exc);
					}
				}
			}
			
		}
		return date;
	}

	/**
	 * Converts from date to string, e.g. for publication
	 * @return
	 */
	public static String convert2String(@Nullable Date date) {
		if (date != null) {
			DateFormat parser = new SimpleDateFormat("yyyy-MM-dd");
			return parser.format(date);
		}
		return null;
	}
	
	public static Integer convert2Integer(String number) {
		try {
			return Integer.parseInt(number);
		} 
		catch (NumberFormatException exc) {
			return null;
		}
	}
	
	public static String convert2String(@Nullable Integer number) {
		if (number != null) {
			return String.valueOf(number);
		}
		return null;
	}
	
	public static AudioDataChangeBuilder setValueInBuilder(AudioDataChangeBuilder builder, AudioAttribute attribute, String value) {
		if (StringUtils.isBlank(value)) {
			return builder;
		}
		
		switch (attribute) {
			case ALBUM:
				builder.album(value);
				break;
			case ALBUM_PUBLICATION:
				Date publication = AudioAttributeUtils.convert2Date(value);
				if (publication != null) {
					builder.albumPublication(publication);
				}
				break;
			case ARTIST:
				builder.artist(value);
				break;
			case DISK:
				builder.disk(value);
				break;
			case GENRE:
				builder.genre(value);
				break;
			case MEDIUM:
				builder.medium(value);
				break;
			case RATING:
				RatingType rating = AudioAttributeUtils.convert2RatingType(value);
				if (rating != null) {
					builder.rating(rating);
				}
				break;
			case TRACK:
				builder.track(value);
				break;
			case TRACK_FORMAT:
				builder.trackFormat(value);
				break;
			case TRACK_NO:
				Integer no = AudioAttributeUtils.convert2Integer(value);
				if (no != null) {
					builder.trackNo(no);
				}
				break;
		}
		return builder;
	}
}
