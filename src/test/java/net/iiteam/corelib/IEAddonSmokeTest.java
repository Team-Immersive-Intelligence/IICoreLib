package net.iiteam.corelib;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class IEAddonSmokeTest
{
	@Test
	void modIdIsStable()
	{
		assertEquals("ieaddon", Tags.MOD_ID);
	}
}
