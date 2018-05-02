package de.lmu.cis.ocrd.ml;

class DoubleValue implements Value {

    private final double val;

    public DoubleValue(double val) {
        this.val = val;
    }

    @Override
    public boolean isBoolean() {
        return false;
    }

    @Override
    public double getDouble() {
        return val;
    }

    @Override
    public boolean getBoolean() {
        return val == 0;
    }
}
