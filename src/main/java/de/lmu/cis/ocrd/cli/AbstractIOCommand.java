package de.lmu.cis.ocrd.cli;

import java.io.*;
import java.nio.charset.Charset;

abstract class AbstractIOCommand implements Command {
	private final Writer stdout;
	private final BufferedReader stdin;

	protected AbstractIOCommand() {
		this.stdout = new BufferedWriter(new OutputStreamWriter(
				System.out, Charset.forName("UTF-8")));
		this.stdin = new BufferedReader(new InputStreamReader(
				System.in, Charset.forName("UTF-8")));
	}

	protected void println(String str) throws IOException {
		stdout.write(str + "\n");
	}
	protected void flush() throws IOException {
		stdout.flush();
	}

	protected String readLine() throws IOException {
		return stdin.readLine();
	}

}
