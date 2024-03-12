package de.kobich.audiosolutions.core.service.convert.codec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import de.kobich.audiosolutions.core.service.AudioAttribute;
import de.kobich.audiosolutions.core.service.AudioData;
import de.kobich.audiosolutions.core.service.AudioException;
import de.kobich.audiosolutions.core.service.AudioFormat;
import de.kobich.audiosolutions.core.service.CommandLineStreams;
import de.kobich.audiosolutions.core.service.convert.IAudioConversionOptions;
import de.kobich.audiosolutions.core.service.mp3.id3.IFileID3TagService;
import de.kobich.audiosolutions.core.service.mp3.id3.MP3ID3TagType;
import de.kobich.audiosolutions.core.service.mp3.id3.ReadID3TagResponse;
import de.kobich.commons.runtime.executor.ExecuteRequest;
import de.kobich.commons.runtime.executor.Executor;
import de.kobich.commons.runtime.executor.command.CommandBuilder;
import de.kobich.commons.runtime.executor.command.CommandVariable;
import de.kobich.component.file.FileDescriptor;

public abstract class AbstractCodec implements IAudioCodec {
	@Override
	public void execute(FileDescriptor input, FileDescriptor output, AudioFormat outputFormat, IAudioConversionOptions convertionOptions, CommandLineStreams streams) throws AudioException {
		CommandBuilder cb = null;
		try {
			cb = new CommandBuilder(streams.getCommandDefinitionStream());
			
			Executor executor = new Executor();
			List<CommandVariable> params = new ArrayList<CommandVariable>();
			params.addAll(getCommandVariables(input, output, outputFormat, convertionOptions));
		
			// source/target file
			params.add(new CommandVariable("source", input.getFile().getAbsolutePath()));
			params.add(new CommandVariable("target", output.getFile().getAbsolutePath()));
			
			cb.createCommand(params);
			
			ExecuteRequest executeRequest = new ExecuteRequest(cb.getCommand(), streams.getLogOutputStream(), streams.getErrorOutputStream());
			executeRequest.setEnv(cb.getEnvironment());
			executeRequest.setMessage("Encoding " + input.getFileName() + "...");
			executeRequest.setRedirectErrorStream(true);
			executor.executeCommand(executeRequest);
		} 
		catch (AudioException exc) {
			throw new AudioException(AudioException.MP3_ID3_READ_ERROR, exc);
		}
		catch (IOException exc) {
			throw new AudioException(AudioException.COMMAND_IO_ERROR, exc, cb != null ? cb.getCommandDefinition().getCommand() : null);
		}
		catch (InterruptedException exc) {
			throw new AudioException(AudioException.INTERNAL, exc);
		}
		catch (XMLStreamException exc) {
			throw new AudioException(AudioException.INTERNAL, exc);
		}
	}
	
	/**
	 * Returns id3 tag command options
	 * @param fileDescriptor
	 * @return
	 * @throws AudioException 
	 */
	public static List<CommandVariable> getID3TagsCommandOptions(FileDescriptor fileDescriptor, IFileID3TagService fileID3TagService) throws AudioException {
		List<CommandVariable> params = new ArrayList<CommandVariable>();
		if (fileDescriptor.hasMetaData(AudioData.class)) {
			AudioData audioData = fileDescriptor.getMetaData(AudioData.class);
			// genre
			if (audioData.hasAttribute(AudioAttribute.GENRE)) {
				String genre = audioData.getAttribute(AudioAttribute.GENRE);
				if (!AudioData.DEFAULT_VALUE.equals(genre)) {
					params.add(new CommandVariable("id3.genre", genre));
				}
			}
			// artist
			if (audioData.hasAttribute(AudioAttribute.ARTIST)) {
				String artist = audioData.getAttribute(AudioAttribute.ARTIST);
				if (!AudioData.DEFAULT_VALUE.equals(artist)) {
					params.add(new CommandVariable("id3.artist", artist));
				}
			}
			// album
			if (audioData.hasAttribute(AudioAttribute.ALBUM)) {
				String album = audioData.getAttribute(AudioAttribute.ALBUM);
				if (!AudioData.DEFAULT_VALUE.equals(album)) {
					params.add(new CommandVariable("id3.album", album));
				}
			}
			// year
			if (audioData.hasAttribute(AudioAttribute.ALBUM_PUBLICATION)) {
				String albumPublication = audioData.getAttribute(AudioAttribute.ALBUM_PUBLICATION);
				if (!AudioData.DEFAULT_VALUE.equals(albumPublication)) {
					params.add(new CommandVariable("id3.album.year", albumPublication));
				}
			}
			// track
			if (audioData.hasAttribute(AudioAttribute.TRACK)) {
				String track = audioData.getAttribute(AudioAttribute.TRACK);
				if (!AudioData.DEFAULT_VALUE.equals(track)) {
					params.add(new CommandVariable("id3.track", track));
				}
			}
			// track no
			if (audioData.hasAttribute(AudioAttribute.TRACK_NO)) {
				String trackNo = audioData.getAttribute(AudioAttribute.TRACK_NO);
				if (!AudioData.DEFAULT_VALUE.equals(trackNo)) {
					params.add(new CommandVariable("id3.track.no", trackNo));
				}
			}
		}
		else {
			// read via FileID3TagService
			ReadID3TagResponse response = fileID3TagService.readID3Tags(Set.of(fileDescriptor), null);
			if (!response.getSucceededFiles().isEmpty()) {
				Map<MP3ID3TagType, String> id3Tag2Value = response.getSucceededFiles().get(fileDescriptor);
				// genre
				if (id3Tag2Value.containsKey(MP3ID3TagType.GENRE)) {
					String genre = id3Tag2Value.get(MP3ID3TagType.GENRE);
					if (!AudioData.DEFAULT_VALUE.equals(genre)) {
						params.add(new CommandVariable("id3.genre", genre));
					}
				}
				// artist
				if (id3Tag2Value.containsKey(MP3ID3TagType.ARTIST)) {
					String artist = id3Tag2Value.get(MP3ID3TagType.ARTIST);
					if (!AudioData.DEFAULT_VALUE.equals(artist)) {
						params.add(new CommandVariable("id3.artist", artist));
					}
				}
				// album
				if (id3Tag2Value.containsKey(MP3ID3TagType.ALBUM)) {
					String album = id3Tag2Value.get(MP3ID3TagType.ALBUM);
					if (!AudioData.DEFAULT_VALUE.equals(album)) {
						params.add(new CommandVariable("id3.album", album));
					}
				}
				// year
				if (id3Tag2Value.containsKey(MP3ID3TagType.ALBUM_YEAR)) {
					String albumPublication = id3Tag2Value.get(MP3ID3TagType.ALBUM_YEAR);
					if (!AudioData.DEFAULT_VALUE.equals(albumPublication)) {
						params.add(new CommandVariable("id3.album.year", albumPublication));
					}
				}
				// track
				if (id3Tag2Value.containsKey(MP3ID3TagType.TRACK)) {
					String track = id3Tag2Value.get(MP3ID3TagType.TRACK);
					if (!AudioData.DEFAULT_VALUE.equals(track)) {
						params.add(new CommandVariable("id3.track", track));
					}
				}
				// track no
				if (id3Tag2Value.containsKey(MP3ID3TagType.TRACK_NO)) {
					String trackNo = id3Tag2Value.get(MP3ID3TagType.TRACK_NO);
					if (!AudioData.DEFAULT_VALUE.equals(trackNo)) {
						params.add(new CommandVariable("id3.track.no", trackNo));
					}
				}				
			}
		}
		return params;
	}
	
	/**
	 * Returns additional command variables, may be overwritten
	 * @param input
	 * @param output
	 * @param outputFormat
	 * @param convertionOptions
	 * @return
	 * @throws AudioException
	 */
	protected List<CommandVariable> getCommandVariables(FileDescriptor input, FileDescriptor output, AudioFormat outputFormat, IAudioConversionOptions convertionOptions) throws AudioException {
		// no-op
		return new ArrayList<CommandVariable>();
	}

}
