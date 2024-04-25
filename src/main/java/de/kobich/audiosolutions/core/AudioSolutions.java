package de.kobich.audiosolutions.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import de.kobich.audiosolutions.core.service.AudioException;
import de.kobich.commons.concurrent.DirectoryLock;
import de.kobich.commons.monitor.progress.IServiceProgressMonitor;
import de.kobich.commons.monitor.progress.ProgressSupport;
import de.kobich.commons.runtime.executor.command.CommandLineTool;
import de.kobich.commons.utils.FileFinderUtils;
import lombok.Getter;

public class AudioSolutions {
	private static final Logger logger = Logger.getLogger(AudioSolutions.class);
	public static final AudioSolutionsVersion CURRENT_VERSION = AudioSolutionsVersion.V10_0;
	public static final record DBConnectionSetting(String url, String user, String password) {}
	public static final String VERSION_PROP = "audiosolutions.version";
	public static final String UI_DEBUG_PROP = "audiosolutions.ui.debug";
	public static final String DB_DEBUG_PROP = "audiosolutions.db.debug";
	public static final String DB_EXISTS_PROP = "audiosolutions.db.exists";
	private static DirectoryLock lock;
	private static Properties properties;
	private static File propertiesFile;
	private static File dataRootDir;
	private static File dbRootDir;
	@Getter
	private static DBConnectionSetting dbConnectionSetting;
	/**
	 * Returns the command definition directory (contains command definition XML files for external tools) 
	 */
	@Getter
	private static File commandDefinitionDir;
	/**
	 * Returns the cover art root dir
	 */
	@Getter
	private static File coverArtRootDir;
	
	private static final AudioSolutionsSpringContext springContext = new AudioSolutionsSpringContext();
	
	/**
	 * Init the application
	 */
	public static AudioSolutionsStatus init(File dataRootDirectory) throws AudioException {
		try {
			Logger.getRootLogger().setLevel(Level.INFO);
		
			logger.info("Data root dir: " + dataRootDirectory.getAbsolutePath());
			if (!dataRootDirectory.exists()) {
				dataRootDirectory.mkdir();
			}
			if (!dataRootDirectory.canWrite()) {
				return AudioSolutionsStatus.NOT_WRITABLE;
			}
			// check for several instances
			File lockFile = new File(dataRootDirectory, "audiosolution.lock");
			AudioSolutions.lock = new DirectoryLock(lockFile);
			boolean status = AudioSolutions.lock.tryLock();
			if (!status) {
				return AudioSolutionsStatus.LOCKED;
			}
			AudioSolutions.lock.registerShutdownHook();
			
			// init directories
			AudioSolutions.dataRootDir = dataRootDirectory;
			
			// -- command definition dir
			AudioSolutions.commandDefinitionDir = dataRootDirectory;
	
			// -- database root dir
			AudioSolutions.dbRootDir = new File(dataRootDirectory, "db");
			logger.info("DB root dir: " + dbRootDir.getAbsolutePath());
			if (!dbRootDir.exists()) {
				logger.info("New audio database will be created");
				System.setProperty(AudioSolutions.DB_EXISTS_PROP, Boolean.FALSE.toString());
				// database will be created implicitly by hibernate
			}
			else {
				logger.info("Existing audio database will be accessed");
				System.setProperty(AudioSolutions.DB_EXISTS_PROP, Boolean.TRUE.toString());
			}
	
			// -- database connection url
			String url = String.format("jdbc:hsqldb:file:%s/audiodb;shutdown=false;ifexists=false;hsqldb.default_table_type=cached", dbRootDir.getAbsolutePath());
			AudioSolutions.dbConnectionSetting = new DBConnectionSetting(url, "sa", "");
			logger.info("DB connection: " + dbConnectionSetting.url());
			
			// -- cover art root url
			AudioSolutions.coverArtRootDir = new File(dataRootDirectory, "coverart");
			AudioSolutions.coverArtRootDir.mkdir();
			logger.info("Cover art dir: " + coverArtRootDir.getAbsolutePath());
			
			// check version
			logger.info("Version: " + CURRENT_VERSION);
			AudioSolutions.propertiesFile = new File(dataRootDirectory, "audiosolutions.properties");
			AudioSolutions.properties = new Properties();
			if (propertiesFile.exists()) {
				try (InputStream in = new FileInputStream(propertiesFile)) {
					properties.load(in);
					String versionFromFile = properties.getProperty(VERSION_PROP);
					if (versionFromFile == null) {
						// create properties file
						properties.put(VERSION_PROP, CURRENT_VERSION.getLabel());
						writeProperties();
					}
					else if (!versionFromFile.equals(CURRENT_VERSION.getLabel())) {
						return AudioSolutionsStatus.VERSION_MISMATCH;
					}
				}
			}
			else {
				// create properties file
				properties.put(VERSION_PROP, CURRENT_VERSION.getLabel());
				writeProperties();
			}
			return AudioSolutionsStatus.INITIALIZED;
		}
		catch (Exception exc) {
			logger.error(exc.getMessage(), exc);
			throw new AudioException(AudioException.INTERNAL);
		}
	}
	
	public static void migrate(IServiceProgressMonitor progressMonitor) throws AudioException {
		checkInitialized();
		try {
			ProgressSupport progressSupport = new ProgressSupport(progressMonitor);
			progressSupport.monitorBeginTask("Migrate to version " + CURRENT_VERSION.getLabel());
			
			progressSupport.monitorSubTask("Creating backup", 1);
			String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_hhmmss"));
			File dbBackupDir = new File(AudioSolutions.dataRootDir, "db_backup_" + timestamp);
			FileUtils.copyDirectory(AudioSolutions.dbRootDir, dbBackupDir);
			logger.info("DB backup created to: " + dbBackupDir.getAbsolutePath());

			progressSupport.monitorSubTask("Start migration", 1);
			final AudioSolutionsVersion sourceVersion = AudioSolutionsVersion.parse(properties.getProperty(VERSION_PROP)).orElseThrow();
			AudioSolutionsVersion tmpVersion = sourceVersion;
			while (!tmpVersion.equals(CURRENT_VERSION)) {
				tmpVersion = AudioSolutionsMigration.migrate(tmpVersion, AudioSolutions.dataRootDir, getDbConnectionSetting(), progressMonitor);
				
				properties.put(VERSION_PROP, tmpVersion.getLabel());
				writeProperties();
			}
			
			progressSupport.monitorEndTask("Migration finished");
		}
		catch (Exception exc) {
			logger.error(exc.getMessage(), exc);
			throw new AudioException(AudioException.DB_MIGRATION_ERROR);
		}
	}
	
	public static void initSpringContext() {
		checkInitialized();
		AudioSolutions.springContext.startup();
	}
	
	public static <T> T getService(Class<T> clazz) {
		return AudioSolutions.springContext.getService(clazz);
	}
	
	public static <T> T getService(String name, Class<T> clazz) {
		return AudioSolutions.springContext.getService(name, clazz);
	}
	
	/**
	 * Shutdowns the plugin
	 */
	public static void shutdown() {
		AudioSolutions.springContext.close();
		
		AudioSolutions.commandDefinitionDir = null;
		AudioSolutions.coverArtRootDir = null;
		AudioSolutions.dataRootDir = null;
		AudioSolutions.dbConnectionSetting = null;
		AudioSolutions.dbRootDir = null;
		AudioSolutions.lock = null;
		AudioSolutions.properties = null;
		AudioSolutions.propertiesFile = null;
	}
	
	/**
	 * Returns a command definition if found, otherwise null
	 * @param tool
	 * @return
	 */
	public static InputStream getCommandDefinitionStream(CommandLineTool tool) {
		checkInitialized();
		
		String fileName = tool.getFileName();
		InputStream is = null;
		List<File> dirs = FileFinderUtils.createDirectories(getCommandDefinitionDir());
		List<String> fileNames = Collections.singletonList(fileName);
		File file = FileFinderUtils.findFile(dirs, fileNames);
		if (file != null) {
			try {
				is = new FileInputStream(file);
			}
			catch (IOException e) {
			}
		}
		return is;
	}
	
	public static Optional<String> getCurrentVersion() {
		return Optional.ofNullable(AudioSolutions.properties.getProperty(VERSION_PROP));
	}
	
	private static void checkInitialized() {
		if (!isInitialized()) {
			throw new IllegalStateException("AudioSolutions is not initialized");
		}
	}
	
	private static boolean isInitialized() {
		return dataRootDir != null && dbRootDir != null && commandDefinitionDir != null && coverArtRootDir != null; 
	}
	
	private static void writeProperties() throws FileNotFoundException, IOException {
		try (OutputStream out = new FileOutputStream(propertiesFile)) {
			properties.store(out, "AudioSolutions");
		}		
	}
}
