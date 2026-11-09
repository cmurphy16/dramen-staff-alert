package com.dramenstaffalert;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class DramenStaffAlertPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(DramenStaffAlertPlugin.class);
		RuneLite.main(args);
	}
}