package de.kobich.audiosolutions.core.service;

import de.kobich.commons.runtime.executor.command.CommandLineTool;

public class AudioTool {
	public static final CommandLineTool MP3GAIN = new CommandLineTool("MP3Gain", "1.5.1", "mp3gain");
	public static final CommandLineTool LAME_MP3 = new CommandLineTool("LAME", "3.98.2", "lame-mp3");
	public static final CommandLineTool LAME_WAV = new CommandLineTool("LAME", "3.98.2", "lame-wav");
	public static final CommandLineTool FFMPEG = new CommandLineTool("ffmpeg", "3.4.8", "ffmpeg");
	@Deprecated
	public static final CommandLineTool AVCONV = new CommandLineTool("Avconv", "0.8.5", "avconv");
	public static final CommandLineTool MP3CHECK = new CommandLineTool("MP3Check", "0.8.3", "mp3check");
	public static final CommandLineTool UNKNOWN = new CommandLineTool("Unknown", "", "");
}
