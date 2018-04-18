package de.lmu.cis.ocrd.archive;

import java.nio.file.Path;
import java.nio.file.Paths;

class ZipEntry implements Entry {

	private final java.util.zip.ZipEntry entry;

	public ZipEntry(java.util.zip.ZipEntry entry) {
		this.entry = entry;
	}

	@Override
	public Path getName() {
		return Paths.get(this.entry.getName());
	}

	public java.util.zip.ZipEntry getUnderlying() {
		return this.entry;
	}
}
