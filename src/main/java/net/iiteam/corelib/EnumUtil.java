package net.iiteam.corelib;

import net.iiteam.corelib.enums.ISerializableEnum;

import javax.annotation.Nonnull;

/**
 * Class containing enum-related utilities.
 *
 * @author Gabriel (gabriel@iiteam.net)
 * @since 19/08/2026
 */
public class EnumUtil
{
	/**
	 * @param en   enum class
	 * @param name name of the enum value
	 * @param <T>  enum type
	 * @return enum value with name, case insensitive
	 */
	@Nonnull
	public static <T extends Enum<T> & ISerializableEnum> T enumValue(Class<T> en, String name)
	{
		try
		{
			return Enum.valueOf(en, name.toUpperCase());
		} catch(IllegalArgumentException ignored)
		{
			return en.getEnumConstants()[0];
		}
	}

	public static <E extends Enum<E>> E cycleEnum(boolean forward, Class<E> enumType, E current)
	{
		return enumType.getEnumConstants()[Utils.cycleInt(forward, current.ordinal(), 0, enumType.getEnumConstants().length-1)];
	}
}
