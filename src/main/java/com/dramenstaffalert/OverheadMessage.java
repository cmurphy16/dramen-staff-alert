package com.dramenstaffalert;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Parses public-chat style prefixes and draws their effects without sending chat. */
final class OverheadMessage
{
	private static final String[] COLORS = {
		"yellow", "red", "green", "cyan", "purple", "white",
		"flash1", "flash2", "flash3", "glow1", "glow2", "glow3", "rainbow"
	};
	private static final String[] MOTIONS = {"wave", "wave2", "shake", "scroll", "slide"};
	private static final int[] SOLID_COLORS = {0xffff00, 0xff0000, 0x00ff00, 0x00ffff, 0xff00ff, 0xffffff};
	private static final Pattern PATTERN_PREFIX = Pattern.compile("^pattern([0-9a-z]{1,8}):", Pattern.CASE_INSENSITIVE);
	// Public-chat pattern palette, indexed by keys 0-9 followed by a-z.
	private static final int[] PATTERN_COLORS = {
		0xffffff, 0xe40303, 0xff8c00, 0xffed00, 0x008026, 0x24408e,
		0x732982, 0xff218c, 0xb55690, 0x5049cc, 0xa3a3a3, 0xd52d00,
		0xef7627, 0xfcf434, 0x078d70, 0x21b1ff, 0x9b4f96, 0xffafc7,
		0xd162a4, 0x7bade3, 0xff9a56, 0x26ceaa, 0x73d7ee, 0x9c59d1,
		0x98e8c1, 0xb57edc, 0x2c2c2c, 0x940202, 0x613915, 0xd0c100,
		0x4a8123, 0x0038a8, 0x800080, 0xd60270, 0xa30262, 0x3d1a78
	};

	final String text;
	final int color;
	final String motion;
	private final int[] patternColors;

	private OverheadMessage(String text, int color, String motion, int[] patternColors)
	{
		this.text = text;
		this.color = color;
		this.motion = motion;
		this.patternColors = patternColors;
	}

	static OverheadMessage parse(String input)
	{
		String text = input;
		int color = 0;
		String motion = "";
		int[] patternColors = null;
		Matcher pattern = PATTERN_PREFIX.matcher(text);
		if (pattern.find())
		{
			String keys = pattern.group(1).toLowerCase(Locale.ROOT);
			patternColors = new int[keys.length()];
			for (int i = 0; i < keys.length(); i++)
			{
				patternColors[i] = PATTERN_COLORS[Character.digit(keys.charAt(i), 36)];
			}
			text = text.substring(pattern.end());
		}
		for (int i = 0; patternColors == null && i < COLORS.length; i++)
		{
			String prefix = COLORS[i] + ":";
			if (text.toLowerCase(Locale.ROOT).startsWith(prefix))
			{
				color = i;
				text = text.substring(prefix.length());
				break;
			}
		}
		for (String effect : MOTIONS)
		{
			String prefix = effect + ":";
			if (text.toLowerCase(Locale.ROOT).startsWith(prefix))
			{
				motion = effect;
				text = text.substring(prefix.length());
				break;
			}
		}
		return new OverheadMessage(text, color, motion, patternColors);
	}

	Color colorAt(int elapsed, int cycle, int character)
	{
		if (patternColors != null)
		{
			// Stretch the pattern into color bands across the entire text, rather than repeating per letter.
			int index = character * patternColors.length / Math.max(1, text.length());
			return new Color(patternColors[Math.min(index, patternColors.length - 1)]);
		}
		int rgb;
		if (color < SOLID_COLORS.length)
		{
			rgb = SOLID_COLORS[color];
		}
		else
		{
			boolean flash = cycle % 20 < 10;
			switch (color)
			{
				case 6: rgb = flash ? 0xff0000 : 0xffff00; break;
				case 7: rgb = flash ? 0x0000ff : 0x00ffff; break;
				case 8: rgb = flash ? 0x00b000 : 0x80ff80; break;
				case 9:
					rgb = elapsed < 50 ? 0xff0000 + elapsed * 1280
						: elapsed < 100 ? 0xffff00 - (elapsed - 50) * 327680
						: 0x00ff00 + (elapsed - 100) * 5;
					break;
				case 10:
					rgb = elapsed < 50 ? 0xff0000 + elapsed * 5
						: elapsed < 100 ? 0xff00ff - (elapsed - 50) * 327680
						: 0x0000ff + (elapsed - 100) * 327680 - (elapsed - 100) * 5;
					break;
				case 11:
					rgb = elapsed < 50 ? 0xffffff - elapsed * 327685
						: elapsed < 100 ? 0x00ff00 + (elapsed - 50) * 327685
						: 0xffffff - (elapsed - 100) * 327680;
					break;
				default:
					return Color.getHSBColor((float) character / Math.max(1, text.length()), 1, 1);
			}
		}
		return new Color(rgb);
	}

	void render(Graphics2D graphics, int centerX, int baseline, int elapsed, int cycle)
	{
		Graphics2D g = (Graphics2D) graphics.create();
		try
		{
			FontMetrics metrics = g.getFontMetrics();
			int width = metrics.stringWidth(text);
			int x = centerX - width / 2;
			if (motion.equals("scroll"))
			{
				g.clipRect(centerX - 50, baseline - metrics.getHeight(), 100, metrics.getHeight() + 5);
				x = centerX + 50 - elapsed * (width + 100) / 150;
			}
			else if (motion.equals("slide"))
			{
				g.clipRect(centerX - width / 2 - 1, baseline - metrics.getHeight() - 1, width + 3, metrics.getHeight() + 6);
				baseline += elapsed < 25 ? elapsed - 25 : elapsed > 125 ? elapsed - 125 : 0;
			}

			for (int i = 0; i < text.length(); i++)
			{
				String glyph = text.substring(i, i + 1);
				int dx = 0;
				int dy = 0;
				switch (motion)
				{
					case "wave":
						dy = (int) (Math.sin(i / 2.0 + cycle / 5.0) * 5);
						break;
					case "wave2":
						dx = (int) (Math.sin(i / 5.0 + cycle / 5.0) * 5);
						dy = (int) (Math.sin(i / 3.0 + cycle / 5.0) * 5);
						break;
					case "shake":
						dy = (int) (Math.sin(i / 1.5 + cycle) * Math.max(0, 7 - elapsed / 8.0));
						break;
					default:
						break;
				}
				g.setColor(Color.BLACK);
				g.drawString(glyph, x + dx + 1, baseline + dy + 1);
				g.setColor(colorAt(elapsed, cycle, i));
				g.drawString(glyph, x + dx, baseline + dy);
				x += metrics.stringWidth(glyph);
			}
		}
		finally
		{
			g.dispose();
		}
	}
}
