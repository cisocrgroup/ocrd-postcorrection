package de.lmu.cis.ocrd.test;

import org.pmw.tinylog.Configurator;
import org.pmw.tinylog.Level;

public class Test {
	protected void enableDebugging() {
		Configurator.currentConfig().level(Level.DEBUG).activate();
		Configurator.currentConfig().formatPattern("{message}").activate();
	}
}
