package com.dramenstaffalert;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import org.junit.Test;
import static org.junit.Assert.*;

public class OverheadMessageTest
{
	@Test
	public void stretchesPatternAcrossMessageInsteadOfRepeatingEveryLetter()
	{
		OverheadMessage message = OverheadMessage.parse("patternq3:abcdefghij");
		assertEquals("abcdefghij", message.text);
		for (int i = 0; i < 10; i++)
		{
			assertEquals(new Color(i < 5 ? 0x2c2c2c : 0xffed00), message.colorAt(0, 0, i));
		}
	}

	@Test
	public void combinesMixedCasePatternKeysWithMotion()
	{
		OverheadMessage message = OverheadMessage.parse("PaTtErN0Az:WaVe:abcdef");
		assertEquals("abcdef", message.text);
		assertEquals("wave", message.motion);
		assertEquals(Color.WHITE, message.colorAt(0, 0, 0));
		assertEquals(new Color(0xa3a3a3), message.colorAt(0, 0, 2));
		assertEquals(new Color(0x3d1a78), message.colorAt(0, 0, 5));
	}

	@Test
	public void handlesShortTextSingleColorAndEightColorPatterns()
	{
		assertEquals(new Color(0xffed00), OverheadMessage.parse("pattern3:Staff missing").colorAt(0, 0, 12));
		OverheadMessage shortText = OverheadMessage.parse("pattern01234567:AB");
		assertEquals(Color.WHITE, shortText.colorAt(0, 0, 0));
		assertEquals(new Color(0x008026), shortText.colorAt(0, 0, 1));
		assertEquals("", OverheadMessage.parse("patternq3:").text);
	}

	@Test
	public void preservesInvalidPatternsAsLiteralYellowText()
	{
		for (String input : new String[]{"pattern:hello", "pattern012345678:hello",
			"patternq!:hello", "patternq3 hello", "pattern\u00e9:hello"})
		{
			OverheadMessage message = OverheadMessage.parse(input);
			assertEquals(input, message.text);
			assertEquals(Color.YELLOW, message.colorAt(0, 0, 0));
		}
	}

	@Test
	public void onlyConsumesOneColorPrefix()
	{
		assertEquals("cyan:Hello", OverheadMessage.parse("patternq3:cyan:Hello").text);
		assertEquals("patternq3:Hello", OverheadMessage.parse("cyan:patternq3:Hello").text);
	}

	@Test
	public void unstyledWarningsAreYellow()
	{
		OverheadMessage message = OverheadMessage.parse("Staff missing");
		assertEquals("Staff missing", message.text);
		assertEquals(Color.YELLOW, message.colorAt(0, 0, 0));
	}

	@Test
	public void combinesColorAndMotionCaseInsensitively()
	{
		OverheadMessage message = OverheadMessage.parse("CyAn:WaVe:Equip your staff");
		assertEquals("Equip your staff", message.text);
		assertEquals("wave", message.motion);
		assertEquals(Color.CYAN, message.colorAt(0, 0, 0));
	}

	@Test
	public void flashingColorsChangeOverTime()
	{
		OverheadMessage message = OverheadMessage.parse("flash1:Staff missing");
		assertEquals("Staff missing", message.text);
		assertEquals(Color.RED, message.colorAt(0, 0, 0));
		assertEquals(Color.YELLOW, message.colorAt(10, 10, 0));
		assertEquals(Color.RED, message.colorAt(20, 20, 0));
	}

	@Test
	public void preservesUnrecognizedPrefixesAndColonsInText()
	{
		assertEquals("unknown:Staff missing", OverheadMessage.parse("unknown:Staff missing").text);
		assertEquals("Reminder: equip staff", OverheadMessage.parse("yellow:Reminder: equip staff").text);
		assertEquals("", OverheadMessage.parse("cyan:wave:").text);
	}

	@Test
	public void supportsMotionWithoutAColor()
	{
		OverheadMessage message = OverheadMessage.parse("scroll:Equip your staff");
		assertEquals("scroll", message.motion);
		assertEquals("Equip your staff", message.text);
		assertEquals(Color.YELLOW, message.colorAt(0, 0, 0));
	}

	@Test
	public void animationRenderingPreservesGraphicsState()
	{
		BufferedImage image = new BufferedImage(300, 100, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = image.createGraphics();
		try
		{
			Rectangle clip = new Rectangle(0, 0, 300, 100);
			graphics.setClip(clip);
			graphics.setColor(Color.PINK);
			for (String motion : new String[]{"wave", "wave2", "shake", "scroll", "slide"})
			{
				OverheadMessage message = OverheadMessage.parse("glow1:" + motion + ":Equip your staff");
				for (int elapsed = 0; elapsed < 150; elapsed++)
				{
					message.render(graphics, 150, 50, elapsed, elapsed);
				}
				assertEquals(clip, graphics.getClipBounds());
				assertEquals(Color.PINK, graphics.getColor());
			}
		}
		finally
		{
			graphics.dispose();
		}
	}
}
