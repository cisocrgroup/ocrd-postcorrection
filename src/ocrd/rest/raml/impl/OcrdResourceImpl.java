package ocrd.rest.raml.impl;

import org.json.JSONArray;
import org.json.JSONObject;
import org.raml.jaxrs.example.model.EvaluationResult;
import org.raml.jaxrs.example.model.TrainData;
import org.raml.jaxrs.example.resource.TrainclassifierResource;

import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.SMO;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import org.apache.commons.lang.StringEscapeUtils;

public class OcrdResourceImpl implements TrainclassifierResource {

	

	@Override
	public PostTrainclassifierResponse postTrainclassifier(TrainData entity) throws Exception {
		
		EvaluationResult result = new EvaluationResult();
		
		JSONArray results_json = new JSONArray();
		
		DataSource source_train = new DataSource("testdata/iris_train.arff");
		Instances dataset_train = source_train.getDataSet();		
		dataset_train.setClassIndex(dataset_train.numAttributes()-1);
		
		
		DataSource source_test = new DataSource("testdata/iris_test.arff");
		Instances dataset_test = source_test.getDataSet();
		dataset_test.setClassIndex(dataset_test.numAttributes()-1);

		
		String struct_jsonstring = entity.getMethods().toString();
		JSONArray methods_array = new JSONArray(struct_jsonstring);
		
		for(int i=0;i<methods_array.length();i++) {
		
	    JSONObject method = methods_array.getJSONObject(i);
	    String type = method.getString("type");
	    
	    System.out.println(type);
	    
	    if(type.equals("svm")) {
	    	
	    	SMO svm = new SMO();
	    	svm.buildClassifier(dataset_train);
	    	
	    	Evaluation eval = new Evaluation(dataset_train);
	    	eval.evaluateModel(svm, dataset_test);
	    	
	    	
	    	JSONObject evalresult = new JSONObject();
	    	evalresult.append("evalstring", StringEscapeUtils.escapeHtml(eval.toSummaryString()));
	    	evalresult.append("name","Support Vector Machine");
	    	evalresult.append("type","svm");

	    	results_json.put(evalresult);
	    }
	    
	    if(type.equals("naivebayes")) {
	    	
	    	NaiveBayes bayes = new NaiveBayes();
	    	bayes.buildClassifier(dataset_train);
	    	
	    	Evaluation eval = new Evaluation(dataset_train);
	    	eval.evaluateModel(bayes, dataset_test);
	    	
	    	
	    	JSONObject evalresult = new JSONObject();
	    	evalresult.append("evalstring", StringEscapeUtils.escapeHtml(eval.toSummaryString()));
	    	evalresult.append("name","Naive Bayes");
	    	evalresult.append("type","naivebayes");

	    	results_json.put(evalresult);

	    }
	    
	    if(type.equals("decisiontree")) {
	    	
	    	String [] options = new String[2];
	    	options[0] = "-C"; 
	    	options[1] = "0.11";
	    	
	    	J48 decision_tree = new J48();
	    	
	    	decision_tree.setOptions(options);
	    	decision_tree.buildClassifier(dataset_train);
	    	
	    	Evaluation eval = new Evaluation(dataset_train);
	    	eval.evaluateModel(decision_tree, dataset_test);
	    
	    	JSONObject evalresult = new JSONObject();
	    	evalresult.append("evalstring", StringEscapeUtils.escapeHtml(eval.toSummaryString()));
	    	evalresult.append("name","Decision Tree");
	    	evalresult.append("type","decisiontree");

	    	results_json.put(evalresult);
	    	
	    	
	    }
	    
		}
	   
		result.setResult(results_json.toString());
		
		return PostTrainclassifierResponse.withJsonOK(result);
	}



}
