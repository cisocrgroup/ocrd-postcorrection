package de.lmu.cis.ocrd.archive;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.zip.ZipFile;

public class ZipArchive implements Archive {

	private final ZipFile zip;
	private ArrayList<Entry> entries;

	public ZipArchive(String zip) throws IOException {
		this(new ZipFile(zip));
	}

	public ZipArchive(ZipFile zip) {
		this.zip = zip;
		this.entries = null;
	}

	@Override
	public void close() throws IOException {
		this.zip.close();
	}

	@Override
	public Path getName() {
		return Paths.get(this.zip.getName());
	}

	@Override
	public Iterator<Entry> iterator() {
		if (this.entries == null) {
			readEntries();
		}
		return this.entries.iterator();
	}

	@Override
	public InputStream open(Entry entry) throws IOException {
		if (!(entry instanceof ZipEntry)) {
			throw new IOException("invalid entry");
		}
		return this.zip.getInputStream(((ZipEntry) entry).getUnderlying());
	}

	private void readEntries() {
		this.entries = new ArrayList<Entry>();
		for (Enumeration<? extends java.util.zip.ZipEntry> entries = this.zip.entries(); entries.hasMoreElements();) {
			java.util.zip.ZipEntry entry = entries.nextElement();
			if (entry.isDirectory()) {
				continue;
			}
			this.entries.add(new ZipEntry(entry));
		}
	}
}
