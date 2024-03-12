package de.kobich.audiosolutions.core.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;

import de.kobich.commons.utils.StreamUtils;

/**
 * Contains all streams for CLI tools.
 */
public class CommandLineStreams implements Closeable {
	private final boolean nested;
	private final OutputStream logOutputStream;
	private final OutputStream errorOutputStream;
	private InputStream commandDefinitionStream;
	private byte[] commandDefinitionBytes;
	
	public CommandLineStreams(OutputStream logOutputStream, OutputStream errorOutputStream) {
		this(logOutputStream, errorOutputStream, false);
	}
	
	private CommandLineStreams(OutputStream logOutputStream, OutputStream errorOutputStream, boolean nested) {
		this.logOutputStream = logOutputStream;
		this.errorOutputStream = errorOutputStream;
		this.nested = nested;
	}
	
	/**
	 * Returns a nested instance that contains a copy of the command definition stream
	 * @return
	 * @throws IOException
	 */
	public CommandLineStreams createNestedStreams() throws IOException {
		CommandLineStreams sub = new CommandLineStreams(this.getLogOutputStream(), this.getErrorOutputStream(), true);
		if (this.hasCommandDefinitionStream()) {
			if (commandDefinitionBytes == null) {
				// cache bytes of command definition stream once
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				try {
					IOUtils.copy(commandDefinitionStream, baos);
					this.commandDefinitionBytes = baos.toByteArray();
				}
				finally {
					baos.close();
				}
			}
			InputStream nestedCommandDefinitionStream = new ByteArrayInputStream(this.commandDefinitionBytes);
			sub.setCommandDefinitionStream(nestedCommandDefinitionStream);
		}
		return sub;
	}

	public boolean isNested() {
		return nested;
	}

	/**
	 * @return the logOutputStream
	 */
	public OutputStream getLogOutputStream() {
		return logOutputStream;
	}

	/**
	 * @return the errorOutputStream
	 */
	public OutputStream getErrorOutputStream() {
		return errorOutputStream;
	}
	
	public boolean hasCommandDefinitionStream() {
		return getCommandDefinitionStream() != null;
	}

	/**
	 * @return the commandDefinitionStream
	 */
	public InputStream getCommandDefinitionStream() {
		return commandDefinitionStream;
	}

	/**
	 * @param commandDefinitionStream the commandDefinitionStream to set
	 * @throws IOException 
	 */
	public void setCommandDefinitionStream(InputStream commandDefinitionStream) {
		this.commandDefinitionStream = commandDefinitionStream;
	}
	
	@Override
	public void close() {
		if (!isNested()) {
			StreamUtils.forceClose(logOutputStream);
			StreamUtils.forceClose(errorOutputStream);
		}
		StreamUtils.forceClose(commandDefinitionStream);
	}

}
