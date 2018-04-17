package de.lmu.cis.ocrd;

import java.util.zip.ZipFile;

public abstract class ArchiveFactory {
	private final String ar;

	public ArchiveFactory(String ar) {
		this.ar = ar;
	}

	public final SimpleDocument create() throws Exception {
		try (ZipFile zip = new ZipFile(this.ar)) {
			return create(zip);
		}
	}

	protected abstract SimpleDocument create(ZipFile zip) throws Exception;
}