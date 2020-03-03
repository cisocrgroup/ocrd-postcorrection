package de.lmu.cis.ocrd.cli.test;

import de.lmu.cis.ocrd.cli.CommandLineArguments;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class CommandLineArgumentsTest {
    @Test
    public void TestCommand() throws Exception {
        CommandLineArguments args = CommandLineArguments.fromArgs("-c", "some-command");
        assertThat(args.getCommand(), is("some-command"));
    }

    @Test
    public void TestIterate() throws Exception {
        CommandLineArguments args = CommandLineArguments.fromArgs("-c", "some-command", "--iterate");
        assertThat(args.isIterate(), is(true));
    }

    @Test
    public void TestNOCR() throws Exception {
        CommandLineArguments args = CommandLineArguments.fromArgs("-c", "some-command", "--nOCR", "101010");
        assertThat(args.maybeGetNOCR().orElse(-1), is(101010));
    }

    @Test
    public void TestTrainingTypeLE() throws Exception {
        CommandLineArguments args = CommandLineArguments.fromArgs("-c", "some-command", "--training", "le");
        assertThat(args.getTrainingType(), is(CommandLineArguments.TrainingType.LE));
    }

    @Test
    public void TestTrainingTypeRR() throws Exception {
        CommandLineArguments args = CommandLineArguments.fromArgs("-c", "some-command", "--training", "Rr");
        assertThat(args.getTrainingType(), is(CommandLineArguments.TrainingType.RR));
    }

    @Test
    public void TestTrainingTypeDM() throws Exception {
        CommandLineArguments args = CommandLineArguments.fromArgs("-c", "some-command", "--training", "DM");
        assertThat(args.getTrainingType(), is(CommandLineArguments.TrainingType.DM));
    }
}
