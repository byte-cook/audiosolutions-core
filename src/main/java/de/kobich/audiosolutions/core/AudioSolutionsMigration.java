package de.kobich.audiosolutions.core;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.hsqldb.jdbc.JDBCDriver;

import de.kobich.audiosolutions.core.AudioSolutions.DBConnectionSetting;
import de.kobich.commons.monitor.progress.IServiceProgressMonitor;
import de.kobich.commons.utils.SQLUtils;
import de.kobich.commons.utils.SQLUtils.DBTable;
import de.kobich.commons.utils.SQLUtils.DBTableColumn;
import de.kobich.commons.utils.SQLUtils.DBTableFK;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Migrates old DB versions to the current one.
 * This class does not use Spring because the entities does not match the underlining database structure. Thus, plain JDBC is used here.
 * @see https://hsqldb.org/doc/guide/index.html
 */
class AudioSolutionsMigration {
	private static final Logger logger = Logger.getLogger(AudioSolutionsMigration.class);
	
	@RequiredArgsConstructor
	private enum AudioTable {
		ARTIST("ARTIST"), ALBUM("ALBUM"), TRACK("TRACK"), MEDIUM("MEDIUM"), GENRE("GENRE");
		
		@Getter
		private final String name;
		
		public static Set<String> getNames() {
			return Arrays.asList(AudioTable.values()).stream().map(AudioTable::getName).collect(Collectors.toSet());
		}
	}
	
	@RequiredArgsConstructor
	private enum PlaylistTable {
		PLAYLIST("PLAYLIST"), PLAYLISTFOLDER("PLAYLISTFOLDER"), PLAYLISTFILE("PLAYLISTFILE");
		
		@Getter
		private final String name;
		
		public static Set<String> getNames() {
			return Arrays.asList(PlaylistTable.values()).stream().map(PlaylistTable::getName).collect(Collectors.toSet());
		}
	}


	public static AudioSolutionsVersion migrate(AudioSolutionsVersion sourceVersion, File dataRootDirectory, DBConnectionSetting dbConnectionSetting, IServiceProgressMonitor progressMonitor) throws Exception {
		DriverManager.registerDriver(new JDBCDriver());
		try (Connection connection = DriverManager.getConnection(dbConnectionSetting.url(), dbConnectionSetting.user(), dbConnectionSetting.password())) {
			// Note: 
			// DDL statements such as CREATE/DROP are transactionally isolated. 
			// In other words, when it is their turn to execute, they commit the current transaction in the connection (if there is one), then do their work and auto-commit the transaction.
			connection.setAutoCommit(false);
			Savepoint savepoint = connection.setSavepoint();
			logger.info("Savepoint created: " + savepoint.getSavepointId());
			try {
				switch (sourceVersion) {
					case V8_0:
						return migrateV8(connection, dataRootDirectory);
					case V9_0:
						return migrateV9(connection, dataRootDirectory);
					case V10_0:
					default:
//						URL sqlScriptUrl = AudioSolutionsMigration.class.getResource("/db/migration/migrate-8.0.sql");
//						EncodedResource sqlScript = new EncodedResource(new PathResource(sqlScriptUrl.toURI()));
//						ScriptUtils.executeSqlScript(connection, sqlScript);
						return AudioSolutionsVersion.V10_0;
				}
			}
			catch (SQLException e) {
//				connection.rollback(savepoint);
				connection.rollback();
				throw e;
			}
			finally {
				shutdown(connection);
			}
		}
	}

	/**
	 * Migrates the DB from v8 to v9: 
	 */
	private static AudioSolutionsVersion migrateV8(Connection connection, File dataRootDirectory) throws Exception {
		// ================================================================
		// 1. change type of column ID to BIGINT
		// ================================================================
		final String TYPE_BIGINT = "BIGINT";
			
		final List<DBTable> tables = SQLUtils.getTables(connection, AudioTable.getNames());
		final AudioTables audioTables = new AudioTables(tables);
		List<DBTableFK> albumFKs = SQLUtils.getForeignKeys(connection, audioTables.getAlbumTable(), tables);
		List<DBTableFK> trackFKs = SQLUtils.getForeignKeys(connection, audioTables.getTrackTable(), tables);
		
		try (Statement stmt = connection.createStatement()) {
			// album has FKs to medium + artist
			for (DBTableFK fk : albumFKs) {
				changeFKColumnType(connection, stmt, fk, TYPE_BIGINT);
			}
			// track has FKs to album + genre 
			for (DBTableFK fk : trackFKs) {
				changeFKColumnType(connection, stmt, fk, TYPE_BIGINT);
			}
			// track ID (no FK reference)
			DBTable trackTable = audioTables.getTrackTable();
			DBTableColumn idColumn = getColumn(connection, trackTable, "ID").orElseThrow();
			if (!idColumn.columnTypeName().equals(TYPE_BIGINT)) {
				logger.info(String.format("Change type of %s.%s to type %s", trackTable.name(), "ID", TYPE_BIGINT));
				logAndExecute(stmt, String.format("ALTER TABLE %s ALTER COLUMN %s %s GENERATED BY DEFAULT AS IDENTITY (START WITH 1)", trackTable.name(), "ID", TYPE_BIGINT));
			}
			
			// has probably no effect: DDL statements are committed automatically
			connection.commit();
		}
		
		// ================================================================
		// 2. add column artist_id to table track
		// ================================================================
		try (Statement stmt = connection.createStatement()) {
			DBTableColumn trackArtistId = getColumn(connection, audioTables.getTrackTable(), "artist_id").orElse(null);
			if (trackArtistId == null) {
				// add column: track.artist_id
				logAndExecute(stmt, String.format("ALTER TABLE track ADD COLUMN artist_id BIGINT"));
				logAndExecute(stmt, "ALTER TABLE track ADD CONSTRAINT FK_track_artist FOREIGN KEY (artist_id) references artist(id)");
				
				List<DBTableUniqueConstraints> trackConstraints = getHsqldbUniqueConstraints(stmt, audioTables.getTrackTable());
				for (DBTableUniqueConstraints constraint : trackConstraints) {
					// delete all unique constraints on table TRACK except the one for file path
					if (!constraint.columnNames().contains("file_path")) {
						logAndExecute(stmt, String.format("ALTER TABLE track DROP CONSTRAINT %s", constraint.name()));
					}
				}
				
				// migrate existing data
				// see: https://hsqldb.org/doc/guide/deployment-chapt.html#dec_bulk_operations
				logAndExecute(stmt, "UPDATE track SET artist_id=( SELECT artist_id FROM album WHERE track.album_id=album.id )");
			
				// check if data migration was successful
				ResultSet checkRS = stmt.executeQuery("SELECT * FROM track WHERE artist_id is null");
				if (checkRS.next()) {
					throw new IllegalStateException("Tracks with missing artist_id's found");
				}
				
				// delete all unique constraints on table ALBUM
				List<DBTableUniqueConstraints> albumConstraints = getHsqldbUniqueConstraints(stmt, audioTables.getAlbumTable());
				for (DBTableUniqueConstraints constraint : albumConstraints) {
					logAndExecute(stmt, String.format("ALTER TABLE album DROP CONSTRAINT %s", constraint.name()));
				}
			}
		}
		
		// ================================================================
		// 3. set NOT NULL for foreign keys
		// ================================================================
		try (Statement stmt = connection.createStatement()) {
			logAndExecute(stmt, "ALTER TABLE album ALTER COLUMN medium_id SET NOT NULL");
			logAndExecute(stmt, "ALTER TABLE track ALTER COLUMN artist_id SET NOT NULL");
			logAndExecute(stmt, "ALTER TABLE track ALTER COLUMN album_id SET NOT NULL");
			logAndExecute(stmt, "ALTER TABLE track ALTER COLUMN genre_id SET NOT NULL");
		}
		
		return AudioSolutionsVersion.V9_0;
	}
	
	/**
	 * Migrates the DB from v9 to v10: 
	 */
	private static AudioSolutionsVersion migrateV9(Connection connection, File dataRootDirectory) throws Exception {
		// ================================================================
		// 1. create tables + constraints for playlist
		// ================================================================
		final List<DBTable> tables = SQLUtils.getTables(connection, PlaylistTable.getNames());
		if (tables.isEmpty()) {
			try (Statement stmt = connection.createStatement()) {
				// create tables
				logAndExecute(stmt, """
						create table playlist (
							id bigint generated by default as identity (start with 1),
							name varchar(255) not null,
							system boolean,
							primary key (id)
						)
						""");
				logAndExecute(stmt, """
						create table playlistfolder (
							id bigint generated by default as identity (start with 1),
							path varchar(255) not null,
							playlist_id bigint not null,
							primary key (id)
						)
						""");
				logAndExecute(stmt, """
						create table playlistfile (
							id bigint generated by default as identity (start with 1),
							file_path varchar(255) not null,
							name varchar(255) not null,
							sort_order bigint not null,
							playlistfolder_id bigint not null,
							primary key (id)
						)
						""");
				// create constraints
				logAndExecute(stmt, """
						alter table playlist add constraint UK_playlist unique (name, system)
						""");
				logAndExecute(stmt, """
						alter table playlistfolder add constraint UK_playlistfolder unique (path, playlist_id)
						""");
				logAndExecute(stmt, """
						alter table playlistfile add constraint UK_playlistfile unique (name, file_path, playlistfolder_id)
						""");
				logAndExecute(stmt, """
						alter table playlistfile 
						add constraint FK_playlistfile_playlistfolder
						foreign key (playlistfolder_id) 
						references playlistfolder
						""");
				logAndExecute(stmt, """
						alter table playlistfolder 
						add constraint FK_playlistfolder_playlist 
						foreign key (playlist_id) 
						references playlist
						""");
			}
		}
		return AudioSolutionsVersion.V10_0;
	}
	
	@Getter
	private static class AudioTables {
		private final DBTable artistTable;
		private final DBTable albumTable;
		private final DBTable trackTable;
		private final DBTable mediumTable;
		private final DBTable genreTable;
		
		public AudioTables(List<DBTable> tables) {
			artistTable = tables.stream().filter(t -> AudioTable.ARTIST.getName().equals(t.name())).findFirst().orElseThrow();
			albumTable = tables.stream().filter(t -> AudioTable.ALBUM.getName().equals(t.name())).findFirst().orElseThrow();
			trackTable = tables.stream().filter(t -> AudioTable.TRACK.getName().equals(t.name())).findFirst().orElseThrow();
			mediumTable = tables.stream().filter(t -> AudioTable.MEDIUM.getName().equals(t.name())).findFirst().orElseThrow();
			genreTable = tables.stream().filter(t -> AudioTable.GENRE.getName().equals(t.name())).findFirst().orElseThrow();
		}
	}
	
	private static void changeFKColumnType(Connection connection, Statement stmt, DBTableFK fk, String newType) throws SQLException {
//		ALTER TABLE ALBUM DROP CONSTRAINT FK5897E6FCECEA062
//		ALTER TABLE ALBUM ALTER COLUMN ARTIST_ID BIGINT
//		ALTER TABLE ARTIST ALTER COLUMN ID BIGINT GENERATED BY DEFAULT AS IDENTITY (START WITH 1)
//		ALTER TABLE ALBUM ADD CONSTRAINT FK5897E6FCECEA062 FOREIGN KEY (ARTIST_ID) REFERENCES ARTIST(ID)
		
		DBTableColumn idColumn = getColumn(connection, fk.pkTable(), fk.pkColumnName()).orElseThrow();
		if (idColumn.columnTypeName().equals(newType)) {
			// nothing to do
			return;
		}
		
		logger.info(String.format("Change type of %s.%s to type %s", fk.pkTable().name(), fk.pkColumnName(), newType));
		logAndExecute(stmt, String.format("ALTER TABLE %s DROP CONSTRAINT %s", fk.fkTable().name(), fk.name()));
		logAndExecute(stmt, String.format("ALTER TABLE %s ALTER COLUMN %s %s", fk.fkTable().name(), fk.fkColumnName(), newType));
		logAndExecute(stmt, String.format("ALTER TABLE %s ALTER COLUMN %s %s GENERATED BY DEFAULT AS IDENTITY (START WITH 1)", fk.pkTable().name(), fk.pkColumnName(), newType));
		logAndExecute(stmt, String.format("ALTER TABLE %s ADD CONSTRAINT %s FOREIGN KEY (%s) REFERENCES %s(%s)", fk.fkTable().name(), fk.name(), fk.fkColumnName(), fk.pkTable().name(), fk.pkColumnName()));
	}
	
	private static Optional<DBTableColumn> getColumn(Connection connection, DBTable table, String columnName) throws SQLException {
		List<DBTableColumn> columns = SQLUtils.getTableColumns(connection, table);
		return columns.stream().filter(c -> columnName.equalsIgnoreCase(c.columnName())).findFirst();
	}
	
	public static record DBTableUniqueConstraints(String name, List<String> columnNames) {};
	/**
	 * Returns unique constraints by using HSQLDB's system tables.
	 */
	private static List<DBTableUniqueConstraints> getHsqldbUniqueConstraints(Statement stmt, DBTable table) throws SQLException {
		List<DBTableUniqueConstraints> constraints = new ArrayList<>();
		
		ResultSet trackConstraintRS = stmt.executeQuery(String.format("SELECT * FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS where table_name = '%s' and constraint_type = 'UNIQUE'", table.name()));
		while (trackConstraintRS.next()) {
			final String uniqueConstraintName = trackConstraintRS.getString("CONSTRAINT_NAME");
			
			ResultSet trackConstraintColumnRS = stmt.executeQuery(String.format("SELECT * FROM INFORMATION_SCHEMA.CONSTRAINT_COLUMN_USAGE where table_name = '%s' and constraint_name = '%s'", table.name(), uniqueConstraintName));
			List<String> columnsInConstraint = new ArrayList<>();
			while (trackConstraintColumnRS.next()) {
				columnsInConstraint.add(trackConstraintColumnRS.getString("COLUMN_NAME").toLowerCase());
			}
			constraints.add(new DBTableUniqueConstraints(uniqueConstraintName, columnsInConstraint));
		}
		return constraints;
	}
	
	private static void logAndExecute(Statement stmt, String sqlCommand) throws SQLException {
		logger.info(sqlCommand);
		stmt.execute(sqlCommand);
	}

	private static void shutdown(Connection connection) throws SQLException {
		try (Statement stmt = connection.createStatement()) {
			// SHUTDOWN COMPACT: 
			// This command rewrites the .data file that contains the information stored in CACHED tables and compacts it to its minimum size. 
			// This command should be issued periodically, especially when lots of inserts, updates, or deletes have been performed on the cached tables. 
			// Changes to the structure of the database, such as dropping or modifying populated CACHED tables or indexes also create large amounts of unused file space that can be reclaimed using this command.
			stmt.execute("SHUTDOWN COMPACT");
		}
	}
}
