package de.lmu.cis.ocrd.train.step;

import java.nio.file.Paths;

public class Main {

	public static void main(String[] args) throws Exception {
		if (args.length < 1 ){
			throw new Exception("usage: [train|eval|recognize] args...")
		}
		switch (args[0]) {
		case "train":
			train(args);
		default:
			throw new Exception("invalid command: " + args[0]);
		}
	}

	public static void train(String[] args) throws Exception {
		if (args.length != 5) {
			throw new Exception("usage: train [DEBUG|INFO] mdir tdir config");
		}
		TrainDLE tdle = new TrainDLE(args[1], new ModelDir(Paths.get(args[2])),
				new TmpDir(Paths.get(args[3])), Config.fromJSON(args[4]));
		tdle.run();
	}
}
