package de.lmu.cis.ocrd.ml.features;

public interface ArgumentFactory {
    <A> A create(Class<A> clazz);
}
