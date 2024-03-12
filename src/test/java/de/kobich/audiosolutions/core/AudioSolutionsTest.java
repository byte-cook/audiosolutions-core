package de.kobich.audiosolutions.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import de.kobich.audiosolutions.core.AudioSolutions.DBConnectionSetting;
import de.kobich.audiosolutions.core.service.AudioAttribute;
import de.kobich.audiosolutions.core.service.AudioDataChange;
import de.kobich.audiosolutions.core.service.AudioException;
import de.kobich.audiosolutions.core.service.TestUtils;
import de.kobich.audiosolutions.core.service.data.AudioDataService;
import de.kobich.audiosolutions.core.service.persist.AudioPersistenceService;
import de.kobich.audiosolutions.core.service.search.AudioTextSearchResult;
import de.kobich.audiosolutions.core.service.search.AudioTextSearchService;
import de.kobich.commons.monitor.progress.IServiceProgressMonitor;
import de.kobich.commons.monitor.progress.SysoutProgressMonitor;
import de.kobich.commons.utils.SQLUtils;
import de.kobich.commons.utils.SQLUtils.DBTable;
import de.kobich.commons.utils.SQLUtils.DBTableColumn;
import de.kobich.commons.utils.SQLUtils.DBTablePK;
import de.kobich.component.file.FileDescriptor;

public class AudioSolutionsTest {
	private static final Logger logger = Logger.getLogger(AudioSolutionsTest.class);
	private static final IServiceProgressMonitor PROGRESS_MONITOR = new SysoutProgressMonitor();
	private static final AudioSolutionsVersion CURRENT_VERSION = AudioSolutionsVersion.V9_0;
	
	@AfterEach
	public void shutdown() {
		AudioSolutions.shutdown();
	}
	
	@Test
	public void testEmptyDataDir() throws Exception {
		System.setProperty(AudioSolutions.DB_DEBUG_PROP, "true");
		File rootDir = createRootDir("empty", null);
		
		startAudioSolutions(rootDir);
	}

	@Test
	public void testLock() throws Exception {
		File rootDir = createRootDir("locked", null);
		
		AudioSolutionsStatus initStatus = startAudioSolutions(rootDir);
		assertEquals(AudioSolutionsStatus.INITIALIZED, initStatus);
		
		AudioSolutionsStatus lockedStatus = startAudioSolutions(rootDir);
		assertEquals(AudioSolutionsStatus.LOCKED, lockedStatus);
	}

	@Test
	public void testSpringContext() throws Exception {
		File rootDir = createRootDir("springcontext", null);
		
		AudioSolutions.init(CURRENT_VERSION, rootDir);
		
		// a view tries to get a service before initialization
		class LoadServiceView implements Runnable {
			public AudioDataService dataService;
			
			@Override
			public void run() {
				logger.info("Getting service...");
				dataService = AudioSolutions.getService(AudioDataService.class);
				logger.info("Getting service finished");
			}
		}
		LoadServiceView view = new LoadServiceView();
		Thread loadServiceThread = new Thread(view);
		loadServiceThread.start();

		Thread.sleep(100);
		AudioSolutions.initSpringContext();
		
		loadServiceThread.join();
		assertNotNull(view.dataService);
	}

	@Test
	public void testCreateTestData() throws Exception {
		File rootDir = createRootDir(CURRENT_VERSION.getLabel(), null);
		startAudioSolutions(rootDir);

		createTestData();
	}

	@Test
	public void testMigrateCorruptedDatabase() throws Exception {
		System.setProperty(AudioSolutions.DB_DEBUG_PROP, "true");
		File rootDir = createRootDir("v8_corrupted", "/data/8.0/audiosolutions/");
		
		final AudioSolutionsStatus status = AudioSolutions.init(CURRENT_VERSION, rootDir);
		assertEquals(AudioSolutionsStatus.VERSION_MISMATCH, status);
		
		// make DB corrupted
		DBConnectionSetting dbConnectionSetting = AudioSolutions.getDbConnectionSetting();
		try (Connection connection = DriverManager.getConnection(dbConnectionSetting.url(), dbConnectionSetting.user(), dbConnectionSetting.password())) {
			connection.setAutoCommit(false);
			try (Statement stmt = connection.createStatement()) {
				// delete ID column of TRACK table
				stmt.execute("ALTER TABLE track DROP COLUMN ID");
				connection.commit();
			}
		}
		
		assertThrows(AudioException.class, () -> AudioSolutions.migrate(CURRENT_VERSION, PROGRESS_MONITOR));
		try (Connection connection = DriverManager.getConnection(dbConnectionSetting.url(), dbConnectionSetting.user(), dbConnectionSetting.password())) {
			connection.setAutoCommit(false);
			List<DBTable> tables = SQLUtils.getTables(connection, Set.of("ALBUM"));
			assertFalse(tables.isEmpty());
			List<DBTableColumn> columns = SQLUtils.getTableColumns(connection, tables.get(0));
			DBTableColumn idColumn = columns.stream().filter(c -> "ID".equals(c.columnName())).findFirst().orElse(null);
			assertNotNull(idColumn);
			// DDL statements are committed automatically
			assertEquals("BIGINT", idColumn.columnTypeName());
		}
	}

	@Test
	public void testMigrateV8() throws Exception {
		System.setProperty(AudioSolutions.DB_DEBUG_PROP, "true");
		File rootDir = createRootDir("v8", "/data/8.0/audiosolutions/");

		final AudioSolutionsStatus status = AudioSolutions.init(CURRENT_VERSION, rootDir);
		assertEquals(AudioSolutionsStatus.VERSION_MISMATCH, status);
		assertEquals(AudioSolutionsVersion.V8_0.getLabel(), AudioSolutions.getCurrentVersion().orElse("unknown"));
		AudioSolutions.migrate(CURRENT_VERSION, PROGRESS_MONITOR);
		assertEquals(CURRENT_VERSION.getLabel(), AudioSolutions.getCurrentVersion().orElse("unknown"));
		
		AudioSolutions.initSpringContext();

		validateDatabase();
		createTestData();
	}
	
	@Test
	public void testPerformanceSearch() throws Exception {
		URL resource = AudioSolutionsTest.class.getResource("/data/8.0/audiosolutions_all/");
		if (resource == null) {
			System.out.println("Skipping testPerformanceSearch");
			return;
		}
		
		System.setProperty(AudioSolutions.DB_DEBUG_PROP, "true");
		File rootDir = createRootDir("v8_performance", "/data/8.0/audiosolutions_all/");

		final AudioSolutionsStatus status = AudioSolutions.init(CURRENT_VERSION, rootDir);
		if (AudioSolutionsStatus.VERSION_MISMATCH.equals(status)) {
			AudioSolutions.migrate(CURRENT_VERSION, PROGRESS_MONITOR);
		}
		
		AudioSolutions.initSpringContext();
		validateDatabase();
		
		AudioTextSearchService searchService = AudioSolutions.getService(AudioTextSearchService.class);
		
		System.out.println("############## Start search...");
//		searchService.searchArtists("roll sympa", 20, null);
//		searchService.searchAlbums("roll sympa", 20, null);
//		searchService.searchTracks("roll sympa", 20, null);
		AudioTextSearchResult res = searchService.searchSimultaneously("album:grammy", 20, null);
//		AudioTextSearchResult res = searchService.searchSimultaneously("stones track:sym", 20, null);
		System.out.println("############## Search finished.");
		System.out.println(res.getArtists().size() + " artists: ");
		res.getArtists().forEach(a -> System.out.println("  " + a));
		System.out.println(res.getAlbums().size() + " albums: ");
		res.getAlbums().forEach(a -> System.out.println("  " + a));
		System.out.println(res.getTracks().size() + " tracks: ");
		res.getTracks().forEach(t -> System.out.println("  " + t));
		//
	}
	
	private File createRootDir(String name, @Nullable final String resourcePath) throws IOException, URISyntaxException {
		File dataDir = new File(AudioSolutionsTest.class.getResource("/data/").toURI());
		File rootDir = new File(dataDir, name);
		FileUtils.deleteDirectory(rootDir);
		logger.info("Root dir: " + rootDir);
		
		if (resourcePath == null) {
			return rootDir;
		}
		else {
			File resourceDir = new File(AudioSolutionsTest.class.getResource(resourcePath).toURI());
			FileUtils.copyDirectory(resourceDir, rootDir);
			return rootDir;
		}		
	}
	
	private AudioSolutionsStatus startAudioSolutions(File rootDir) throws AudioException, FileNotFoundException, IOException {
		final AudioSolutionsStatus status = AudioSolutions.init(CURRENT_VERSION, rootDir);
		logger.info("DB Url: " + AudioSolutions.getDbConnectionSetting());
		switch (status) {
			case LOCKED:
			case NOT_WRITABLE:
			case VERSION_MISMATCH:
				return status;
			case INITIALIZED:
				// continue
				break;
		}
		assertNotNull(AudioSolutions.getCommandDefinitionDir());
		AudioSolutions.initSpringContext();
		
		AudioDataService dataService = AudioSolutions.getService(AudioDataService.class);
		assertNotNull(dataService);
		
		File propertiesFile = new File(rootDir, "audiosolutions.properties");
		assertTrue(propertiesFile.exists());
		Properties properties = new Properties();
		try (InputStream in = new FileInputStream(propertiesFile)) {
			properties.load(in);
			assertEquals(CURRENT_VERSION.getLabel(), properties.get(AudioSolutions.VERSION_PROP));
		}
		return status;
	}
	
	private void validateDatabase() throws SQLException {
		DBConnectionSetting dbConnectionSetting = AudioSolutions.getDbConnectionSetting();
		Set<String> AUDIO_TABLENAMES = Set.of("ARTIST", "ALBUM", "TRACK", "MEDIUM", "GENRE");
		try (Connection connection = DriverManager.getConnection(dbConnectionSetting.url(), dbConnectionSetting.user(), dbConnectionSetting.password())) {
			connection.setAutoCommit(false);
			List<DBTable> tables = SQLUtils.getTables(connection, AUDIO_TABLENAMES);
			for (DBTable table : tables) {
				List<DBTableColumn> columns = SQLUtils.getTableColumns(connection, table);
				// check columns
				DBTableColumn idColumn = columns.stream().filter(c -> c.columnName().equals("ID")).findFirst().orElse(null);
				assertNotNull(idColumn);
				assertEquals("BIGINT", idColumn.columnTypeName());
				assertTrue(idColumn.autoIncrement());
				
				// check PKs
				List<DBTablePK> pks = SQLUtils.getPrimaryKeys(connection, table);
				assertFalse(pks.isEmpty());
				assertEquals("ID", pks.get(0).columnName());
			}
		}
	}

	private void createTestData() throws AudioException {
		AudioPersistenceService persistenceService = AudioSolutions.getService(AudioPersistenceService.class);
		persistenceService.removeAll();
		
		FileDescriptor file1 = TestUtils.createFileDescriptor("sympathy for the devil.mp3", "sympathy for the devil.mp3");
		AudioDataChange adc1 = AudioDataChange.builder().fileDescriptor(file1).artist(TestUtils.STONES).album(TestUtils.BEGGARS_BANQUET).track(TestUtils.SYMPATHY_DEVIL).genre("Rock").build();
		FileDescriptor file2 = TestUtils.createFileDescriptor("memory motel.mp3", "memory motel.mp3");
		AudioDataChange adc2 = AudioDataChange.builder().fileDescriptor(file2).artist(TestUtils.STONES).album(TestUtils.BEGGARS_BANQUET).track("memory motel").genre("Rock").build();
		FileDescriptor file3 = TestUtils.createFileDescriptor("boris the spider.mp3", "boris the spider.mp3");
		AudioDataChange adc3 = AudioDataChange.builder().fileDescriptor(file3).artist("The Who").album("Best of").track("boris the spider").build();
		
		AudioDataService dataService = AudioSolutions.getService(AudioDataService.class);
		dataService.applyChanges(Set.of(adc1, adc2, adc3), PROGRESS_MONITOR);
		
		persistenceService.persist(Set.of(file1, file2, file3), PROGRESS_MONITOR);
		
		assertEquals(3, persistenceService.getCount(AudioAttribute.TRACK));
		assertEquals(2, persistenceService.getCount(AudioAttribute.ARTIST));
		assertEquals(2, persistenceService.getCount(AudioAttribute.ALBUM));
	}

}
