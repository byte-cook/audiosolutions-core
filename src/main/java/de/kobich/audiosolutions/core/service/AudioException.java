package de.kobich.audiosolutions.core.service;

import de.kobich.commons.exception.ApplicationException;
import de.kobich.commons.exception.ErrorCode;

public class AudioException extends ApplicationException {
	public static final ErrorCode INTERNAL = new ErrorCode("audio.internalError", "There was an internal problem");
	public static final ErrorCode IO_ERROR = new ErrorCode("audio.ioError", "I/O error");
	public static final ErrorCode TIMEOUT = new ErrorCode("audio.timeoutError", "Timeout exceeded");
	public static final ErrorCode COMMAND_IO_ERROR = new ErrorCode("audio.commandIOError", "I/O error: \nMake sure '{0}' is installed and on your PATH");
	public static final ErrorCode COMMAND_DEFINITION_NOT_FOUND_ERROR = new ErrorCode("audio.commandDefinitionNotFoundError", "No suitable command definition found");
	public static final ErrorCode ILLEGAL_STATE_ERROR = new ErrorCode("audio.illegalStateError", "Illegal state error"); 
	public static final ErrorCode DUPLICATE_FILE_ERROR = new ErrorCode("audio.duplicateFileError", "File already available in the database");
	public static final ErrorCode FILE_ALREADY_EXISTS = new ErrorCode("audio.renameAlreadyExists", "Destination file already exists");
	public static final ErrorCode FILE_MISSING = new ErrorCode("audio.missingFile", "The file does not exist");
	public static final ErrorCode ENCODER_NOT_FOUND_ERROR = new ErrorCode("audio.noEncoderFoundError", "No suitable encoder found");
	public static final ErrorCode CONVERT_ERROR = new ErrorCode("audio.convertFailed", "Audio file cannot be converted");
	public static final ErrorCode NORMALIZER_NOT_FOUND_ERROR = new ErrorCode("audio.normalizerNotFoundError", "No suitable normalizer found");
	public static final ErrorCode NORMALIZE_ERROR = new ErrorCode("audio.normalizeFailed", "Audio file cannot be normalized");
	public static final ErrorCode MP3_ID3_READ_ERROR = new ErrorCode("audio.readID3Failed", "ID3 tags could not be read");
	public static final ErrorCode MP3_ID3_WRITE_ERROR = new ErrorCode("audio.writeID3Failed", "ID3 tags could not be written");
	public static final ErrorCode AUDIO_DECODING_FAILED = new ErrorCode("audio.decodingFailed", "Audio decoding failed");
	public static final ErrorCode AUDIO_UNSUPPORTED_FORMAT = new ErrorCode("audio.unsupportedFormat", "The format is not supported"); 
	public static final ErrorCode CDDB_ERROR = new ErrorCode("audio.cddbError", "Cannot find CDDB data");
	public static final ErrorCode CDDB_ILLEGAL_SEARCH_ERROR = new ErrorCode("audio.cddbIllegalSearchError", "Illegal search pattern (wildcards cannot be used as the first character of a search)");
	public static final ErrorCode CONNECTION_ERROR = new ErrorCode("audio.connectionError", "Internet connection cannot be established");
	public static final ErrorCode CHECKER_NOT_FOUND_ERROR = new ErrorCode("audio.checkerNotFoundError", "No suitable audio checker found");
	public static final ErrorCode DB_MIGRATION_ERROR = new ErrorCode("audio.databaseMigrationError", "Database migration failed");
	public static final ErrorCode PLAYLIST_NOT_FOUND_ERROR = new ErrorCode("audio.playlistNotFoundError", "Playlist cannot be found: {0}");

	private static final long serialVersionUID = -5238782004634985009L;

	public AudioException(ErrorCode errorCode, Object... params) {
		super(errorCode, params);
	}

	public AudioException(ErrorCode errorCode, Throwable cause, Object... params) {
		super(errorCode, cause, params);
	}


}
