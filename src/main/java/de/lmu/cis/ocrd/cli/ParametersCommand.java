package de.lmu.cis.ocrd.cli;

import de.lmu.cis.ocrd.config.Parameters;

abstract class ParametersCommand implements Command {
    private final String name;
    protected Parameters parameters;

    ParametersCommand(String name) {
        this.name = name;
    }

    protected void init(CommandLineArguments config) throws Exception {
        this.parameters = config.mustGetParameter(Parameters.class);
    }

    public Parameters getParameters() {
        return parameters;
    }

    @Override
    public String getName() {
        return name;
    }
}
