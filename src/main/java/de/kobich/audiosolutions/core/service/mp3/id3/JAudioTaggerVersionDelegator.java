package de.kobich.audiosolutions.core.service.mp3.id3;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.KeyNotFoundException;
import org.jaudiotagger.tag.TagField;
import org.jaudiotagger.tag.id3.AbstractID3v2Frame;
import org.jaudiotagger.tag.id3.AbstractID3v2Tag;
import org.jaudiotagger.tag.id3.ID3v1Tag;
import org.jaudiotagger.tag.id3.ID3v24Tag;
import org.jaudiotagger.tag.id3.framebody.FrameBodyCOMM;
import org.jaudiotagger.tag.images.Artwork;

/**
 * Delegates read/write actions to all id3 tag versions. 
 * @author ckorn
 */
public class JAudioTaggerVersionDelegator {
	private ID3v1Tag id3v1Tag;
	private AbstractID3v2Tag id3v2Tag;
//	private ID3v24Tag id3v24Tag;
//	private Locale preferedLocale;
	
	/**
	 * @param id3v1tag
	 * @param id3v2Tag
	 * @param id3v24Tag
	 */
	public JAudioTaggerVersionDelegator(ID3v1Tag id3v1tag, AbstractID3v2Tag id3v2Tag, ID3v24Tag id3v24Tag) {
		this.id3v1Tag = id3v1tag;
		this.id3v2Tag = id3v2Tag;
//		this.id3v24Tag = id3v24Tag;
//		this.preferedLocale = Locale.GERMAN;
	}
	
	public String getFirst(FieldKey key) {
//		if (id3v24Tag != null) {
//			if (FieldKey.COMMENT.equals(key)) {
//				String comment = getFirstByLanguage(key);
//				if (StringUtils.hasText(comment)) {
//					return comment;
//				}
//			}
//			if (StringUtils.hasText(id3v24Tag.getFirst(key))) {
//				return id3v24Tag.getFirst(key);
//			}
//		}
		if (id3v2Tag != null) {
			if (FieldKey.COMMENT.equals(key)) {
				String comment = getByLanguage(key);
				if (StringUtils.isNotBlank(comment)) {
					return comment;
				}
			}
			if (StringUtils.isNotBlank(id3v2Tag.getFirst(key))) {
				return id3v2Tag.getFirst(key);
			}
		}
		return id3v1Tag.getFirst(key);
	}
	
	public Optional<Artwork> getArtwork() {
		if (id3v2Tag != null) {
			Artwork artwork = id3v2Tag.getFirstArtwork();
			if (artwork != null) {
				return Optional.of(artwork);
			}
		}
		Artwork artwork = id3v1Tag.getFirstArtwork();
		if (artwork != null) {
			return Optional.of(artwork);
		}
		return Optional.empty();
	}
	
	private String getByLanguage(FieldKey key) {
		if (FieldKey.COMMENT.equals(key)) {
//			String preferedLanguageId = getPreferedLanguageId();
			List<TagField> tagFields = id3v2Tag.getFields(key);
			String comment = "";
			for (TagField tagField : tagFields) {
				AbstractID3v2Frame frame = (AbstractID3v2Frame) tagField; 
				FrameBodyCOMM body = (FrameBodyCOMM) frame.getBody();
//				AbstractDataType language = body.getObject("Language");
//				if (dataType != null && preferedLanguageId.equals(dataType.getValue())) {
				if (StringUtils.isNotBlank(comment)) {
					comment += ";";
				}
				comment += /*language + ": " + */body.getObject("Text");
//				}
			}
			if (StringUtils.isNotBlank(comment)) {
				return comment;
			}
		}
		return null;
	}
	
	public void deleteField(FieldKey key) {
//		if (id3v24Tag != null) {
//			id3v24Tag.deleteField(key);
//		}
		if (id3v2Tag != null) {
			id3v2Tag.deleteField(key);
		}
		if (id3v1Tag != null) {
			id3v1Tag.deleteField(key);
		}
	}
	
	public void setField(FieldKey key, String value) throws KeyNotFoundException, FieldDataInvalidException {
		deleteField(key);
		
//		if (id3v24Tag != null) {
//			id3v24Tag.setField(key, value);
//			if (FieldKey.COMMENT.equals(key)) {
//				// add field for comment in preferred language
//				String preferedLanguageId = getPreferedLanguageId();
//				ID3v24FieldKey id3v24FieldKey = ID3v24Frames.getInstanceOf().getId3KeyFromGenericKey(key);
//				ID3v24Frame frame = new ID3v24Frame(id3v24FieldKey.getFrameId());
//				FrameBodyCOMM body = (FrameBodyCOMM) frame.getBody();
//				body.setLanguage(preferedLanguageId);
//				body.setText(value);
//				id3v24Tag.addField(frame);
//			}
//		}
		if (id3v2Tag != null) {
			id3v2Tag.setField(key, value);
//			if (FieldKey.COMMENT.equals(key)) {
//				// add field for comment in preferred language
//				String preferedLanguageId = getPreferedLanguageId();
//				ID3v22FieldKey id3v22FieldKey = ID3v22Frames.getInstanceOf().getId3KeyFromGenericKey(key);
//				ID3v22Frame frame = new ID3v22Frame(id3v22FieldKey.getFrameId());
//				FrameBodyCOMM body = (FrameBodyCOMM) frame.getBody();
//				body.setLanguage(preferedLanguageId);
//				body.setText(value);
//				id3v2Tag.addField(frame);
//			}
		}
		if (id3v1Tag != null) {
			id3v1Tag.setField(key, value);
		}
	}
	
//	private String getPreferedLanguageId() {
//		return preferedLocale.getISO3Language();
//	}
}
