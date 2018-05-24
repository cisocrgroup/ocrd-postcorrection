package de.lmu.cis.ocrd.train.test;

import de.lmu.cis.ocrd.train.Environment;

import java.io.IOException;

class TestBase {
	private final String tmpDirProperty = "java.io.tmpdir";
	private final String path = System.getProperty(tmpDirProperty);
	private final String name = "environment-test";

	String getName() {
		return name;
	}

	String getPath() {
		return path;
	}

	Environment newEnvironment() throws IOException {
		return new Environment(path, name);
	}
}
