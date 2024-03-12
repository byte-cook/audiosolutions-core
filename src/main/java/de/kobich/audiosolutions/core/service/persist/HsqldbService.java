package de.kobich.audiosolutions.core.service.persist;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PreDestroy;

@Service
public class HsqldbService {
	private static final Logger logger = Logger.getLogger(HsqldbService.class);
	
	@Autowired
	protected SessionFactory sessionFactory;

	@PreDestroy
	@Transactional
	public void shutdown() {
		logger.info("Shutdown DB");
		sessionFactory.getCurrentSession().createNativeQuery("shutdown", Object.class).executeUpdate();
	}

}
