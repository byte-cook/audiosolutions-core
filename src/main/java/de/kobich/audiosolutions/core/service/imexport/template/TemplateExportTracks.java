package de.kobich.audiosolutions.core.service.imexport.template;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;


public class TemplateExportTracks {
	private static final Comparator<Object> objectComparator = new ObjectComparator();
	private static final Comparator<TemplateExportTrack> trackComparator = new TrackComparator();
	private List<TemplateExportTrack> tracks;
	
	public TemplateExportTracks(List<TemplateExportTrack> tracks) {
		Collections.sort(tracks, trackComparator);
		this.tracks = Collections.unmodifiableList(tracks);
	}

	/**
	 * Lists all distinct items of the given key
	 * @param key
	 * @return
	 */
	public List<Object> list(TemplateExportKey key) {
		Set<Object> values = new HashSet<Object>();
		
		for (TemplateExportTrack track : list()) {
			Object value = track.get(key);
			if (value != null) {
				values.add(value);
			}
		}
		
		List<Object> valueList = new ArrayList<Object>(values);
		Collections.sort(valueList, objectComparator);
		return Collections.unmodifiableList(valueList);
	}

	/**
	 * Filters this tracks 
	 * @param filter the filter, alternate between key and value: TrackKey.MEDIUM, "medium", TrackKey.ALBUM, "album", ...
	 * @return
	 */
	public TemplateExportTracks filter(Object... filter) {
		List<TemplateExportTrack> tracks = new ArrayList<TemplateExportTrack>();
		TrackPredicate predicate = new TrackPredicate(filter);
		CollectionUtils.select(list(), predicate, tracks);
		
		return new TemplateExportTracks(tracks);
		
	}

	/**
	 * @return the tracks
	 */
	public List<TemplateExportTrack> list() {
		return tracks;
	}
	
	private class TrackPredicate implements Predicate<TemplateExportTrack> {
		private final Map<TemplateExportKey, Object> filterMap;
		
		public TrackPredicate(Object... filter) {
			this.filterMap = new HashMap<TemplateExportKey, Object>();
			
			for (int i = 0; i < filter.length; i += 2) {
				int nextIndex = i + 1;
				if (nextIndex < filter.length) {
					TemplateExportKey key = TemplateExportKey.toKey(filter[i]);
					Object value = filter[nextIndex];
					if (key != null) {
						filterMap.put(key, value);
					}
				}
			}
		}
		
		@Override
		public boolean evaluate(TemplateExportTrack t) {
			for (TemplateExportKey key : filterMap.keySet()) {
				Object value = filterMap.get(key);
				if (value != null && !value.toString().equalsIgnoreCase(t.getAsString(key))) {
					return false;
				}
				
			}
			return true;
		}
	}
	
	private static class ObjectComparator implements Comparator<Object> {
		@Override
		public int compare(Object o1, Object o2) {
			return o1.toString().compareTo(o2.toString());
		}
	}
	private static class TrackComparator implements Comparator<TemplateExportTrack> {
		@Override
		public int compare(TemplateExportTrack o1, TemplateExportTrack o2) {
			int result = 0;
//			result = o1.getAsString(TemplateExportKey.MEDIUM).compareTo(o2.getAsString(TemplateExportKey.MEDIUM));
//			if (result != 0) {
//				return result;
//			}
			result = o1.getAsString(TemplateExportKey.ARTIST).compareTo(o2.getAsString(TemplateExportKey.ARTIST));
			if (result != 0) {
				return result;
			}
			result = o1.getAsString(TemplateExportKey.ALBUM).compareTo(o2.getAsString(TemplateExportKey.ALBUM));
			if (result != 0) {
				return result;
			}
			result = o1.getAsString(TemplateExportKey.DISK).compareTo(o2.getAsString(TemplateExportKey.DISK));
			if (result != 0) {
				return result;
			}
			result = o1.getAsInteger(TemplateExportKey.TRACK_NO).compareTo(o2.getAsInteger(TemplateExportKey.TRACK_NO));
			if (result != 0) {
				return result;
			}
			result = o1.getAsString(TemplateExportKey.TRACK).compareTo(o2.getAsString(TemplateExportKey.TRACK));
			if (result != 0) {
				return result;
			}
			return 0;
		}
	}
}
