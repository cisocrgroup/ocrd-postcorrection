package de.lmu.cis.ocrd.ml;

class BooleanValue implements Value{
    private final boolean val;

    public BooleanValue(boolean val) {
        this.val = val;
    }

    @Override
    public boolean isBoolean() {
        return true;
    }

    @Override
    public double getDouble() {
        return val ? 1 : 0;
    }

    @Override
    public boolean getBoolean() {
        return val;
    }
}
