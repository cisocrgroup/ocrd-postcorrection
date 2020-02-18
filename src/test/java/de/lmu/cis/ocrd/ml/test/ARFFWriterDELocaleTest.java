package de.lmu.cis.ocrd.ml.test;

import java.util.Locale;

public class ARFFWriterDELocaleTest extends ARFFWriterTest {
	@Override
	public void init() throws Exception {
		Locale old = Locale.getDefault();
		try {
			Locale.setDefault(new Locale("de", "DE"));
			super.init();
		} catch (Exception e) {
			throw e;
		} finally {
			Locale.setDefault(old);
		}
	}
}

