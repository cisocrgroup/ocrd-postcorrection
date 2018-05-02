package de.lmu.cis.ocrd.cli;

import org.pmw.tinylog.Logger;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.Logistic;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
class TryoutCommand implements Command{

    @Override
    public void execute(Configuration config) throws Exception {
        DataSource source_train = new DataSource("testdata/iris_train.arff");
        DataSource source_test = new DataSource("testdata/iris_test.arff");
        Instances dataset_train = source_train.getDataSet();
        Instances dataset_test = source_test.getDataSet();
        dataset_train.setClassIndex(dataset_train.numAttributes() - 1);
        dataset_test.setClassIndex(dataset_train.numAttributes() - 1);

        Logger.debug("training");
        Logistic l = new Logistic();
        l.buildClassifier(dataset_train);

        Logger.debug("evaluating");
        Evaluation eval = new Evaluation(dataset_train);
        eval.evaluateModel(l, dataset_test);

        System.out.println(eval.toSummaryString());
    }
}
