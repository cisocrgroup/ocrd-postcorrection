package de.lmu.cis.ocrd.archive;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.stream.Stream;

public class DirectoryArchive implements Archive {

	private final Path dir;
	private ArrayList<Entry> files;

	public DirectoryArchive(String dir) throws IOException {
		this.dir = Paths.get(dir);
		this.files = readFiles(this.dir);
	}

	private static ArrayList<Entry> readFiles(Path dir) throws IOException {
		ArrayList<Entry> entries = new ArrayList<Entry>();
		try (Stream<Path> paths = Files.walk(dir)) {
			paths.filter(Files::isRegularFile).forEach((p) -> {
				entries.add(new DirectoryEntry(p));
			});
		}
		return entries;
	}

	@Override
	public void close() throws IOException {
		// does nothing
	}

	@Override
	public Path getName() {
		return this.dir;
	}

	@Override
	public Iterator<Entry> iterator() {
		return this.files.iterator();
	}

	@Override
	public InputStream open(Entry entry) throws IOException {
		if (!(entry instanceof DirectoryEntry)) {
			throw new InvalidEntryException(entry);
		}
		return ((DirectoryEntry) entry).open();
	}

}
