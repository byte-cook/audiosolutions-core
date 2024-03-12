package de.kobich.audiosolutions.core.service.play.player;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.kobich.audiosolutions.core.service.AudioException;
import de.kobich.audiosolutions.core.service.play.AudioPlayerClient;
import de.kobich.audiosolutions.core.service.play.AudioPlayerClientDispatcher;
import de.kobich.audiosolutions.core.service.play.AudioPlayingThreadManager;
import de.kobich.audiosolutions.core.service.play.player.AudioPlayerResponse.PlayListFlowType;
import de.kobich.commons.utils.StreamUtils;
import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.decoder.Header;

/**
 * Javazoom/Jlayer audio player.
 * The Jlayer player supports MP3 files, but do not support rewinding. Therefore, we use the standard Java API to implement the player. 
 * Java does however not support the MP3 encoding by default. Thus, the mp3plugin.jar must be installed - either in the class path of the application or in the lib/ext/ directory of the JDK.  
 * @see https://github.com/umjammer/jlayer
 * @see https://www.oracle.com/java/technologies/javase/jmf-mp3-plugin.html
 */
@Service
public class JavaZoomAudioPlayer extends AbstractAudioPlayer {
	@Autowired
	private AudioPlayerState state;
	@Autowired
	private AudioPlayingThreadManager threadManager;
	@Autowired
	private AudioPlayerClientDispatcher dispatcher;
	
	@Override
	public AudioPlayerResponse playFile(File file, long beginMillis, AudioPlayerClient client) throws AudioException {
		AudioInputStream in = null;
		AudioInputStream inputStream = null;
		SourceDataLine line = null;
		try {
			in = AudioSystem.getAudioInputStream(file);
			AudioFormat targetFormat = getTargetAudioFormat(in);
			inputStream = AudioSystem.getAudioInputStream(targetFormat, in);
	
			byte[] data = new byte[4096];
			byte[] skip = new byte[261568];
			line = getSourceDataLine(targetFormat);
			if (line != null) {
				state.setPlaying(file, getTotalMillis(file));
	
//				line.open(targetFormat);
				line.start();
				
				boolean play = false;
				long bytesRead = 0;
				long playedMillis = 0;
				while (bytesRead != -1) {
					// check player state
					Optional<AudioPlayerResponse> responseOpt = super.checkState(state, threadManager);
					if (responseOpt.isPresent()) {
						return responseOpt.get();
					}
					
					// set played milliseconds
					playedMillis += getCurrentTimeMillis(bytesRead, targetFormat);
					if (play) {
						dispatcher.firePlayedMillis(client, playedMillis);
					}
					
					play = playedMillis >= beginMillis;
					if (play) {
						// play audio file: read audio stream + write to audio device
						bytesRead = inputStream.read(data, 0, data.length);
						if (bytesRead != -1 && play) {
							line.write(data, 0, (int) bytesRead);
						}
						// sleep
						doSleep(1);
					}
					else {
						// skip
						bytesRead = inputStream.read(skip, 0, skip.length);
						
						// does not work for MP3
//						long bytes = getBytes(beginMillis, targetFormat);
//						bytesRead = inputStream.skip(bytes);
//						playedMillis += getCurrentTimeMillis(bytesRead, targetFormat);
//						beginMillis = playedMillis;
					}
	
				}
			}
			return new AudioPlayerResponse(PlayListFlowType.TRACK_FINISHED);
		}
		catch (IOException exc) {
			throw new AudioException(AudioException.IO_ERROR, exc);
		}
		catch (LineUnavailableException exc) {
			throw new AudioException(AudioException.IO_ERROR, exc);
		}
		catch (UnsupportedAudioFileException exc) {
			throw new AudioException(AudioException.AUDIO_UNSUPPORTED_FORMAT, exc);
		}
		catch (Exception exc) {
			throw new AudioException(AudioException.INTERNAL, exc);
		}
		finally {
			StreamUtils.forceClose(in);
			StreamUtils.forceClose(inputStream);
			if (line != null) {
				line.drain();
				line.stop();
				line.close();
			}
		}
	}
	
	/**
	 * Returns the current time millis
	 * @param bytesRead
	 * @param format
	 * @return
	 */
	private long getCurrentTimeMillis(long bytesRead, AudioFormat format) {
		float sampleRate = format.getSampleRate() / 1000;
		int frameSize = format.getFrameSize();
		return (long) (bytesRead / (sampleRate * frameSize));
	}
	
	/**
	 * Returns the bytes to skip
	 * @param millis
	 * @param format
	 * @return
	 */
//	private long getBytes(long millis, AudioFormat format) {
//		float sampleRate = format.getSampleRate() / 1000;
//		int frameSize = format.getFrameSize();
//		return (long) ((sampleRate * frameSize) * millis);
//	}

	/**
	 * Returns the audio playing format
	 * @param in
	 * @return
	 */
	private AudioFormat getTargetAudioFormat(AudioInputStream in) {
		AudioFormat baseFormat = in.getFormat();

		// define desired decoding format
		AudioFormat decodingFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, baseFormat.getSampleRate(), 16, baseFormat.getChannels(),
				baseFormat.getChannels() * 2, baseFormat.getSampleRate(), false);
		return decodingFormat;
	}

	/**
	 * Returns the line to write
	 * @param audioFormat
	 * @return
	 * @throws LineUnavailableException
	 */
	private SourceDataLine getSourceDataLine(AudioFormat audioFormat) throws LineUnavailableException {
		SourceDataLine res = null;
		DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
		res = (SourceDataLine) AudioSystem.getLine(info);
		res.open(audioFormat);
		return res;
	}

	/**
	 * Returns the milliseconds of given audio file or AUDIO_STREAM_MILLIS_UNDEFINED
	 * @param file
	 * @return milliseconds 
	 * @throws AudioException
	 */
	private long getTotalMillis(File file) throws AudioException {
		Bitstream bitStream = null;
		InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(file);
			bitStream = new Bitstream(inputStream);
			Header header = bitStream.readFrame();
	
			long totalMillis = IAudioPlayerListener.TOTAL_MILLIS_UNDEFINED;
			if (header != null) {
				totalMillis = Math.round(header.total_ms(inputStream.available()));
				if (totalMillis == 0) {
					AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(file);
					AudioFormat format = audioInputStream.getFormat();
					long frames = audioInputStream.getFrameLength();
					long durationInSeconds = (long) ((frames+0.0) / format.getFrameRate());
					totalMillis = durationInSeconds * 1000;
					if (totalMillis == 0) {
						totalMillis = IAudioPlayerListener.TOTAL_MILLIS_UNDEFINED;
					}
				}
			}
			
			return totalMillis;
		}
		catch (UnsupportedAudioFileException exc) {
			throw new AudioException(AudioException.IO_ERROR, exc);
		}
		catch (FileNotFoundException exc) {
			throw new AudioException(AudioException.IO_ERROR, exc);
		}
		catch (BitstreamException exc) {
			throw new AudioException(AudioException.IO_ERROR, exc);
		}
		catch (IOException exc) {
			throw new AudioException(AudioException.IO_ERROR, exc);
		}
		finally {
			StreamUtils.forceClose(inputStream);
			try {
				if (bitStream != null) {
					bitStream.close();
				}
			}
			catch (Exception exc) {
			}
		}
	}
}
