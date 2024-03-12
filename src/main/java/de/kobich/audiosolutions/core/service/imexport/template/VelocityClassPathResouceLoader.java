package de.kobich.audiosolutions.core.service.imexport.template;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

public class VelocityClassPathResouceLoader extends ClasspathResourceLoader {

	/* (non-Javadoc)
	 * @see org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader#getResourceStream(java.lang.String)
	 */
//	@Override
	// TODO remove?
	public InputStream getResourceStream(String name) throws ResourceNotFoundException {
		return VelocityClassPathResouceLoader.class.getResourceAsStream(name);
	}
	
	@Override
    public Reader getResourceReader(String name, String encoding)
            throws ResourceNotFoundException {
		InputStream is = VelocityClassPathResouceLoader.class.getResourceAsStream(name);
		return new BufferedReader(new InputStreamReader(is));
		
	}

}
