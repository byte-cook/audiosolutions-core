package de.kobich.audiosolutions.core.service.mp3.id3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import de.kobich.audiosolutions.core.service.AudioDataBuilder;
import de.kobich.audiosolutions.core.service.AudioException;
import de.kobich.audiosolutions.core.service.AudioSolutionsTestSpringConfig;
import de.kobich.audiosolutions.core.service.TestUtils;
import de.kobich.component.file.FileDescriptor;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes=AudioSolutionsTestSpringConfig.class)
public class FileID3TagServiceTest {
	@Autowired
	private IFileID3TagService id3TagService;
	
	private File testFile;
	
	@BeforeEach
	public void init() throws URISyntaxException {
		this.testFile = new File(FileID3TagServiceTest.class.getResource("/mp3/01-mp3-no-tags.mp3").toURI());
	}
	
	@Test
	public void writeSingleID3Tag() throws AudioException {
		Set<FileDescriptor> fileDescriptors = new HashSet<>();
		fileDescriptors.add(new FileDescriptor(this.testFile, testFile));
		
		id3TagService.writeSingleID3Tag(fileDescriptors, MP3ID3TagType.ARTIST, TestUtils.STONES, ID3TagVersion.ALL, null);
		
		ReadID3TagResponse res = id3TagService.readID3Tags(fileDescriptors, null);
		assertTrue(res.getFailedFiles().isEmpty());
		Map<MP3ID3TagType, String> map = res.getSucceededFiles().values().iterator().next();
		assertEquals(TestUtils.STONES, map.get(MP3ID3TagType.ARTIST));
	}

	@Test
	public void writeID3TagByAudioData() throws AudioException {
		Set<FileDescriptor> fileDescriptors = new HashSet<>();
		fileDescriptors.add(TestUtils.createFileDescriptor(this.testFile, AudioDataBuilder.builder().artist(TestUtils.STONES).album(TestUtils.BEGGARS_BANQUET).track(TestUtils.SYMPATHY_DEVIL).trackNo(1)));
		id3TagService.writeID3TagsByAudioData(fileDescriptors, ID3TagVersion.ALL, null);
		
		ReadID3TagResponse res = id3TagService.readID3Tags(fileDescriptors, null);
		assertTrue(res.getFailedFiles().isEmpty());
		Map<MP3ID3TagType, String> map = res.getSucceededFiles().values().iterator().next();
		assertEquals(TestUtils.STONES, map.get(MP3ID3TagType.ARTIST));
		assertEquals(TestUtils.BEGGARS_BANQUET, map.get(MP3ID3TagType.ALBUM));
		assertEquals(TestUtils.SYMPATHY_DEVIL, map.get(MP3ID3TagType.TRACK));
		assertEquals("1", map.get(MP3ID3TagType.TRACK_NO));
	}

}
