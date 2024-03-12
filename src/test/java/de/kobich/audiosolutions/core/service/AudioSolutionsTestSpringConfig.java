package de.kobich.audiosolutions.core.service;

import java.util.Properties;

import javax.sql.DataSource;

import org.hibernate.cfg.AvailableSettings;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaDialect;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.atomikos.jdbc.AtomikosNonXADataSourceBean;

import de.kobich.SpringComponentScan;
import jakarta.persistence.EntityManagerFactory;

@TestConfiguration
@EnableTransactionManagement
@EnableJpaRepositories
@ComponentScan(basePackageClasses = SpringComponentScan.class)
public class AudioSolutionsTestSpringConfig {
	
	@Bean
	public DataSource dataSource() {
		AtomikosNonXADataSourceBean ds = new AtomikosNonXADataSourceBean();
		ds.setUrl("jdbc:hsqldb:mem:testdb;DB_CLOSE_DELAY=-1");
		ds.setUser("sa");
		ds.setPassword("");
		ds.setDriverClassName("org.hsqldb.jdbc.JDBCDriver");
		ds.setMinPoolSize(5);
		ds.setMaxPoolSize(20);
		ds.setUniqueResourceName("AudioSolutionsTest");
		ds.setLocalTransactionMode(true);
		return ds;
	}

	@Bean
	public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
		LocalContainerEntityManagerFactoryBean b = new LocalContainerEntityManagerFactoryBean();
		b.setPersistenceUnitName("AudioSolutionsTest");
		b.setPersistenceProviderClass(HibernatePersistenceProvider.class);
		b.setJtaDataSource(dataSource());
		b.setPackagesToScan(SpringComponentScan.class.getPackageName());
		HibernateJpaVendorAdapter va = new HibernateJpaVendorAdapter();
		va.setDatabase(Database.HSQL);
		va.setGenerateDdl(true);
		b.setJpaVendorAdapter(va);
		b.setJpaDialect(new HibernateJpaDialect());
		b.setJpaProperties(hibernateProperties());
		b.afterPropertiesSet();
		return b;
	}
	
	@Bean
	public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
		JpaTransactionManager t = new JpaTransactionManager(entityManagerFactory);
		t.afterPropertiesSet();
		return t;
	}
//	@Bean
//	public UserTransactionManager userTransactionManager() {
//		UserTransactionManager userTransactionManager = new UserTransactionManager();
////		userTransactionManager.setTransactionTimeout(60);
//		userTransactionManager.setForceShutdown(true);
//		return userTransactionManager;
//	}
//	@Bean
//	public JtaTransactionManager transactionManager() {
//		JtaTransactionManager m = new JtaTransactionManager();
//		m.setAutodetectTransactionSynchronizationRegistry(false);
//		m.setAutodetectUserTransaction(false);
//		m.setTransactionManager(userTransactionManager());
//		m.setUserTransaction(userTransactionManager());
//		m.setAllowCustomIsolationLevels(true);
//		return m;
//	}
	
	@Bean
	public PersistenceExceptionTranslationPostProcessor exceptionTranslation(){
		return new PersistenceExceptionTranslationPostProcessor();
	}
	
	private final Properties hibernateProperties() {
        Properties hibernateProperties = new Properties();
//        hibernateProperties.setProperty(AvailableSettings.CURRENT_SESSION_CONTEXT_CLASS, "jta");
        hibernateProperties.put(AvailableSettings.HBM2DDL_AUTO, "create-drop");
        hibernateProperties.put(AvailableSettings.DIALECT, "org.hibernate.dialect.HSQLDialect");
        hibernateProperties.put(AvailableSettings.USE_SQL_COMMENTS, true);
        hibernateProperties.put(AvailableSettings.FORMAT_SQL, true);
        hibernateProperties.put(AvailableSettings.SHOW_SQL, true);
        hibernateProperties.put(AvailableSettings.GENERATE_STATISTICS, true);
        hibernateProperties.put(AvailableSettings.LOG_SLOW_QUERY, "500");
        return hibernateProperties;
    }

}
