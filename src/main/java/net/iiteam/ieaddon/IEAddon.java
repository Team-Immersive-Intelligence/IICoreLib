package net.iiteam.ieaddon;

import lombok.Getter;
import lombok.Setter;
import net.iiteam.ieaddon.common.util.Logger;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLFingerprintViolationEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION,
		dependencies = "required-after:immersiveengineering@[0.12-92,);",
		acceptedMinecraftVersions = "[1.12.2]",
		certificateFingerprint = "84c19709be61a4630ee3812136f5c80086a978db"
)
public class IEAddon
{
	@Mod.Instance(Tags.MOD_ID)
	public static IEAddon INSTANCE;

	@Setter
	@Getter
	boolean testValue;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		Logger.initLogger();
		Logger.info("Starting {} {}", Tags.MOD_NAME, Tags.VERSION);
		setTestValue(true);

		for(EnumFacing value : EnumFacing.values())
			switch(value)
			{
				case NORTH -> Logger.error("NORTH NIE WOLNO");
				case UP -> Logger.warn("UP WOLNO");
				default -> Logger.info("From "+value+" to "+value.getOpposite());
			}
	}

	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		Logger.info("Value is "+testValue);
		Logger.info("Initialising {}", Tags.MOD_NAME);
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
		Logger.info("Finished loading {}", Tags.MOD_NAME);

		setTestValue(false);
		Logger.info("Value is "+isTestValue());
	}

	@Mod.EventHandler
	public void wrongSignature(FMLFingerprintViolationEvent event)
	{
		System.err.println("Incorrect fingerprint for IEAddon.");
		System.err.println("Correct: "+event.getExpectedFingerprint());
		for(String fingerprint : event.getFingerprints())
			System.err.println("Found: "+fingerprint);
	}
}
