package de.lmu.cis.ocrd.cli.test;

import de.lmu.cis.ocrd.cli.Command;
import de.lmu.cis.ocrd.cli.CommandFactory;
import de.lmu.cis.ocrd.cli.CommandLineArguments;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
public class CommandFactoryTest {
	private CommandFactory commandFactory;

	@Before
	public void init() throws Exception {
		commandFactory = new CommandFactory().register(TestCommand.class);
	}

	@Test
	public void testCreateNewCommand() throws Exception {
		final Command command = commandFactory.get("test-command");
		assertThat(command.getName(), is(new TestCommand().getName()));
		assertThat(command instanceof TestCommand, is(true));
	}

	@Test(expected = Exception.class)
	public void testInvalidCommand() throws Exception {
		commandFactory.get("invalid");
	}

	@Test(expected = Exception.class)
	public void testNullCommand() throws Exception {
		commandFactory.get(null);
	}

	public static class TestCommand implements Command {
		@Override
		public String getName() {
			return "test-command";
		}

		@Override
		public void execute(CommandLineArguments commandLineArguments) {
		}
	}
}
