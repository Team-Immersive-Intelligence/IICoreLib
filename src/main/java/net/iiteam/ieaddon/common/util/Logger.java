package net.iiteam.ieaddon.common.util;

import net.iiteam.ieaddon.Tags;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;

/**
 * @author Pabilo8
 * @since 03.08.2023
 */
public class Logger
{
	private static org.apache.logging.log4j.Logger LOGGER;

	public static void initLogger()
	{
		LOGGER = LogManager.getLogger(Tags.MOD_NAME);
	}

	public static void log(Level logLevel, Object object)
	{
		LOGGER.log(logLevel, String.valueOf(object));
	}

	public static void error(Object object)
	{
		log(Level.ERROR, object);
	}

	public static void info(Object object)
	{
		log(Level.INFO, object);
	}

	public static void warn(Object object)
	{
		log(Level.WARN, object);
	}

	public static void error(String message, Object... params)
	{
		LOGGER.log(Level.ERROR, message, params);
	}

	public static void info(String message, Object... params)
	{
		LOGGER.log(Level.INFO, message, params);
	}

	public static void warn(String message, Object... params)
	{
		LOGGER.log(Level.WARN, message, params);
	}
}
