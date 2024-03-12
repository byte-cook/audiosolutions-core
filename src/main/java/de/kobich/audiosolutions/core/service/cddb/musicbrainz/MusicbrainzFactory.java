package de.kobich.audiosolutions.core.service.cddb.musicbrainz;

import java.net.ProxySelector;

import org.musicbrainz.controller.Artist;
import org.musicbrainz.controller.Release;
import org.musicbrainz.controller.ReleaseGroup;
import org.musicbrainz.webservice.impl.HttpClientWebServiceWs2;

import de.kobich.audiosolutions.core.service.AudioException;
import de.kobich.commons.net.IProxyProvider;

/*
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.SyncBasicHttpParams;
import org.musicbrainz.controller.Artist;
import org.musicbrainz.controller.Release;
import org.musicbrainz.controller.ReleaseGroup;
import org.musicbrainz.webservice.impl.HttpClientWebServiceWs2;

import de.kobich.commons.net.IProxyProvider;
import de.kobich.commons.net.Proxy;
import de.kobich.component.audio.AudioException;
*/


public class MusicbrainzFactory {
//	private static final String MUSICBRAINZ_URL = "http://www.musicbrainz.org";
//	private static final int CONNECTION_TIMEOUT = 20 * 1000;
//	private static final int SO_TIMEOUT = 30 * 1000;
	private static final long ARTIST_LIMIT = 20;
	private static final long RELEASE_LIMIT = 50;
	private static final long RELEASE_GROUP_LIMIT = 10;
	private static final long MIN_SCORE = 70;
//	private final DefaultHttpClient httpClient;
	
	public static MusicbrainzFactory createInstance(IProxyProvider proxyProvider) throws AudioException {
		ProxySelector.setDefault(new AudioProxySelector(proxyProvider));
		return new MusicbrainzFactory();
	}

/*	public static MusicbrainzFactory createInstance(IProxyProvider proxyProvider) throws AudioException {
		try {
			Proxy proxy = proxyProvider.getProxy(new URL(MUSICBRAINZ_URL));
			HttpParams params = new SyncBasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(params, CONNECTION_TIMEOUT);
			HttpConnectionParams.setSoTimeout(params, SO_TIMEOUT);
			DefaultHttpClient httpClient = new DefaultHttpClient(params);
			if (proxy != null) {
				if (proxy.getServer() != null) {
					HttpHost httpHost = new HttpHost(proxy.getServer(), proxy.getPort());
					httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, httpHost);
				}
				if (proxy.getUsername() != null) {
					AuthScope authScope = new AuthScope(proxy.getServer(), proxy.getPort());
					UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(proxy.getUsername(), proxy.getPassword());
					httpClient.getCredentialsProvider().setCredentials(authScope, credentials);
				}
			}
			
			// check if Internet connection is available
			httpClient.getParams().setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 1000);
			httpClient.getParams().setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 1000);
			HttpHead head = new HttpHead(MUSICBRAINZ_URL);
			HttpResponse response = httpClient.execute(head);
			boolean connectionFailed = response.getStatusLine().getStatusCode() != HttpStatus.SC_OK;
			if (connectionFailed) {
				throw new AudioException(AudioException.CONNECTION_ERROR);
			}
			
			return new MusicbrainzFactory(httpClient);
		}
		catch (AudioException exc) {
			throw exc;
		}
		catch (Exception exc) {
			throw new AudioException(AudioException.CONNECTION_ERROR);
		}
	}*/

	private MusicbrainzFactory(/*DefaultHttpClient httpClient*/) {
//		this.httpClient = httpClient;
	}

	public Artist createArtistController() {
		Artist artistCtr = new Artist();
		artistCtr.setQueryWs(new HttpClientWebServiceWs2());
		artistCtr.getSearchFilter().setLimit(ARTIST_LIMIT);
		artistCtr.getSearchFilter().setMinScore(MIN_SCORE);
		// artistCtr.getIncludes().setAliases(false);
		return artistCtr;
	}

	public ReleaseGroup createReleaseGroupController() {
		ReleaseGroup releaseGroupCtr = new ReleaseGroup();
		releaseGroupCtr.setQueryWs(new HttpClientWebServiceWs2());
		releaseGroupCtr.getSearchFilter().setLimit(RELEASE_GROUP_LIMIT);
		releaseGroupCtr.getSearchFilter().setMinScore(MIN_SCORE);
		return releaseGroupCtr;
	}

	public Release createReleaseController() {
		Release releaseCtr = new Release();
		releaseCtr.setQueryWs(new HttpClientWebServiceWs2());
		releaseCtr.getSearchFilter().setLimit(RELEASE_LIMIT);
		releaseCtr.getSearchFilter().setMinScore(MIN_SCORE);
		return releaseCtr;
	}
}
