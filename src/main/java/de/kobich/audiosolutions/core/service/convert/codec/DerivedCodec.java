package de.kobich.audiosolutions.core.service.convert.codec;


//not used
@Deprecated
public class DerivedCodec { //implements IAudioCodec {
//	private static final Logger logger = Logger.getLogger(DerivedCodec.class);
//	private AudioCodecs audioCodecs;
//
//	@Override
//	public void execute(FileDescriptor fileDescriptor, FileDescriptor output, AudioFormat outputFormat, IAudioConversionOptions convertionOptions, InputStream definitionStream, OutputStream logOutputStream, OutputStream errorOutputStream) throws AudioException {
//		try {
//			AudioFormat inputFormat = AudioFormatUtil.getAudioFormat(fileDescriptor);
//			File input = fileDescriptor.getFile();
//			
//			List<AudioFormat> formats = findIntermediateFormats(inputFormat, outputFormat);
//			if (formats != null) {
//				// temporarily input/output files
//				File tmpInput = File.createTempFile(input.getName(), "input");
//				FileUtils.copyFile(input, tmpInput);
//				FileDescriptor tmpInputFD = new FileDescriptor(tmpInput, tmpInput.getName(), null);
//				File tmpOutput = File.createTempFile(input.getName(), "output");
//				FileDescriptor tmpOutputFD = new FileDescriptor(tmpOutput, tmpOutput.getName(), null);
//				
//				AudioFormat lastFormat = inputFormat;
//				for (AudioFormat format : formats) {
//					if (lastFormat.equals(format)) {
//						continue;
//					}
//					
//					IAudioCodec codec = audioCodecs.findCodec(lastFormat, format);
//					if (codec != null) {
//						codec.execute(tmpInputFD, tmpOutputFD, outputFormat, convertionOptions, definitionStream, logOutputStream, errorOutputStream);
//						lastFormat = format;
//						
//						// swap input/output files
//						FileUtils.deleteQuietly(tmpInput);
//						FileUtils.copyFile(tmpOutput, tmpInput);
//					}
//				}
//				
//				FileUtils.copyFile(tmpOutput, output.getFile());
//			}
//		}
//		catch (IOException exc) {
//			throw new AudioException(AudioException.IO_ERROR, exc);
//		}
//	}
//
//	@Override
//	public boolean supports(AudioFormat inputFormat, AudioFormat outputFormat) {
//		return !findIntermediateFormats(inputFormat, outputFormat).isEmpty();
//	}
//
//	@Override
//	public CommandLineTool getCommandLineTool(AudioFormat inputFormat,
//			AudioFormat outputFormat) {
//		return AudioTool.UNKNOWN;
//	}
//
//	private List<AudioFormat> findIntermediateFormats(AudioFormat inputFormat, AudioFormat outputFormat) {
//		Stack<AudioFormat> formats = new Stack<AudioFormat>();
//		formats.push(inputFormat);
//		boolean status = findIntermediateFormats(formats, inputFormat, outputFormat);
//		if (status) {
//			return formats;
//		}
//		return null;
//	}
//
//	private boolean findIntermediateFormats(Stack<AudioFormat> formats, AudioFormat inputFormat, AudioFormat outputFormat) {
//		if (formats.size() > 2) {
//			return false;
//		}
//		
//		logger.debug("findIntermediateFormats");
//		for (AudioFormat format : AudioFormat.values()) {
//			if (inputFormat.equals(format) || formats.contains(format)) {
//				// skip format
//				continue;
//			}
//			logger.debug(inputFormat + " -> " + format);
//			for (IAudioCodec codec : audioCodecs.getCodecs()) {
//				if (codec.supports(inputFormat, format)) {
//					formats.push(format);
//					if (outputFormat.equals(format)) {
//						return true;
//					}
//					else {
//						boolean status = findIntermediateFormats(formats, format, outputFormat);
//						if (status) {
//							return true;
//						}
//						else {
//							formats.pop();
//						}
//					}
//				}
//			}
//		}
//		return false;
//	}
//
}
