package de.lmu.cis.ocrd.archive;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

public interface Archive extends Closeable, Iterable<Entry> {
	public InputStream open(Entry entry) throws IOException;
}
