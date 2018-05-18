package de.lmu.cis.ocrd.train;

import de.lmu.cis.ocrd.ml.FeatureSet;
import weka.classifiers.functions.Logistic;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import java.io.Serializable;
import java.util.Arrays;

public class Classifier extends Logistic implements Serializable {
    private final Instances structure;

    public Classifier(Instances structure) {
        this.structure = structure;
    }

    void setClassIndex(int n) {
        structure.setClassIndex(n);
    }

    double predict(FeatureSet.Vector features) throws Exception {
        final int n = features.size(); // last feature is GT
        final Instance instance = new DenseInstance(n);
        instance.setDataset(structure);
        for (int i = 0; i < n; i++) {
            instance.setValue(i, features.get(i).toString());
        }
        System.out.println("classifying instance: " + instance);
        System.out.println("class attribute: " + instance.classAttribute());
        final double res = classifyInstance(instance);
        for (int i = 0; i < n; i++) {
            System.out.println(Arrays.toString(structure.attributeToDoubleArray(i)));
        }
        return res;
    }
}
