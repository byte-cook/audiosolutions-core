package de.kobich.audiosolutions.core.service.cddb.musicbrainz;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import de.kobich.commons.net.IProxyProvider;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AudioProxySelector extends ProxySelector {
	private static final Logger logger = Logger.getLogger(AudioProxySelector.class);
	private final IProxyProvider proxyProvider;

	@Override
	public List<Proxy> select(URI uri) {
		List<Proxy> proxyList = new ArrayList<>();
		try {
			de.kobich.commons.net.Proxy proxy = proxyProvider.getProxy(uri.toURL());
			if (proxy != null) {
				proxyList.add(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxy.getServer(), proxy.getPort())));
			}
		}
		catch (MalformedURLException e) {
			logger.warn(e);
		}
		
		if (proxyList.isEmpty()) {
			proxyList.add(Proxy.NO_PROXY);
		}
		return proxyList;
	}

	@Override
	public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
		
	}

}
