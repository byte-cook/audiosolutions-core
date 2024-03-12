package de.kobich.audiosolutions.core;

import java.util.Properties;

import javax.sql.DataSource;

import org.hibernate.cfg.AvailableSettings;
import org.hibernate.dialect.HSQLDialect;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.hibernate.tool.schema.Action;
import org.hsqldb.jdbc.JDBCDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
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
import de.kobich.audiosolutions.core.AudioSolutions.DBConnectionSetting;
import jakarta.persistence.EntityManagerFactory;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackageClasses = SpringComponentScan.class)
@ComponentScan(basePackageClasses = SpringComponentScan.class)
@Lazy
public class AudioSolutionsSpringConfig {
	@Autowired
	private Environment env;
	
	@Bean
	public DataSource dataSource() {
		AtomikosNonXADataSourceBean dataSource = new AtomikosNonXADataSourceBean();
    	dataSource.setDriverClassName(JDBCDriver.class.getName());
//    	dataSource.setUrl("jdbc:hsqldb:mem:testdb;DB_CLOSE_DELAY=-1");
		DBConnectionSetting setting = AudioSolutions.getDbConnectionSetting();
    	dataSource.setUrl(setting.url());
    	dataSource.setUser(setting.user());
    	dataSource.setPassword(setting.password());
    	dataSource.setMinPoolSize(5);
    	dataSource.setMaxPoolSize(20);
    	dataSource.setUniqueResourceName("AudioSolutionsDB");
    	dataSource.setLocalTransactionMode(true);
    	return dataSource;
		
	}

	@Bean
	public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
		LocalContainerEntityManagerFactoryBean b = new LocalContainerEntityManagerFactoryBean();
		b.setPersistenceUnitName("AudioSolutionsPersistenceUnit");
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
        hibernateProperties.put(AvailableSettings.SHOW_SQL, env.getProperty(AudioSolutions.DB_DEBUG_PROP, Boolean.class, false));
        hibernateProperties.put(AvailableSettings.FORMAT_SQL, env.getProperty(AudioSolutions.DB_DEBUG_PROP, Boolean.class, false));
        hibernateProperties.put(AvailableSettings.USE_SQL_COMMENTS, false);
        hibernateProperties.put(AvailableSettings.GENERATE_STATISTICS, true);
//        if (env.getProperty(AudioSolutions.DB_INMEMORY_PROP, Boolean.class, false)) {
//        	hibernateProperties.put(AvailableSettings.DIALECT, HSQLInMemoryDialect.class.getName());
//        }
//        else {
        	hibernateProperties.put(AvailableSettings.DIALECT, HSQLDialect.class.getName());
//        }
        	
        if (!env.getRequiredProperty(AudioSolutions.DB_EXISTS_PROP, Boolean.class)) {
        	hibernateProperties.put(AvailableSettings.HBM2DDL_AUTO, Action.CREATE.getExternalHbm2ddlName());
        }
        else {
        	hibernateProperties.put(AvailableSettings.HBM2DDL_AUTO, Action.NONE.getExternalHbm2ddlName());
        }
        return hibernateProperties;
    }

}
