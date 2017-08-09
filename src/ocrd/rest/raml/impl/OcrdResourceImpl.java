package ocrd.rest.raml.impl;

import org.raml.jaxrs.example.model.Test;
import org.raml.jaxrs.example.resource.HalloworldResource;

public class OcrdResourceImpl implements HalloworldResource {



	@Override
	public PostHalloworldResponse postHalloworld(Test entity) throws Exception {
		
	    entity.setName("Your name is: "+entity.getName());
	    entity.setInputstring(" you said: "+entity.getInputstring());
		
		return PostHalloworldResponse.withJsonOK(entity);
	}



}
