package de.kobich.audiosolutions.core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Calendar;
import java.util.Date;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes=AudioSolutionsTestSpringConfig.class)
public class AudioAttributeUtilsTest {
	@Test
	public void testDate() throws Exception {
		Date _1981 = createDateAndAssert("1981", 1981, 0, 1);
		Date _1981_01 = createDateAndAssert("1981/01", 1981, 0, 1);
		Date _1981_03 = createDateAndAssert("1981-03", 1981, 2, 1);
		Date _1981_03_12 = createDateAndAssert("1981 03 12", 1981, 2, 12);
		assertNull(AudioAttributeUtils.convert2Date("1981/03 12"), "Mixing separator is not allowed");
		
		assertEquals("1981-01-01", AudioAttributeUtils.convert2String(_1981));
		assertEquals("1981-01-01", AudioAttributeUtils.convert2String(_1981_01));
		assertEquals("1981-03-01", AudioAttributeUtils.convert2String(_1981_03));
		assertEquals("1981-03-12", AudioAttributeUtils.convert2String(_1981_03_12));
	}
	
	private Date createDateAndAssert(String value, int year, int month, int dayOfMonth) {
		Calendar calendar = Calendar.getInstance();
		Date date = AudioAttributeUtils.convert2Date(value);
		assertNotNull(date, "date is null for: " + value);
		calendar.setTime(date);
		assertEquals(year, calendar.get(Calendar.YEAR), "year wrong for: " + value);
		assertEquals(month, calendar.get(Calendar.MONTH), "month wrong for: " + value);
		assertEquals(dayOfMonth, calendar.get(Calendar.DAY_OF_MONTH), "day wrong for: " + value);
		return date;
	}
	
}
