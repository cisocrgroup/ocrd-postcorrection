package de.lmu.cis.ocrd.archive;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

class DirectoryEntry implements Entry {

	private final Path path;

	public DirectoryEntry(Path path) {
		this.path = path;
	}

	@Override
	public Path getName() {
		return this.path;
	}

	public InputStream open() throws IOException {
		return new FileInputStream(this.path.toFile());
	}
}
