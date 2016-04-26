package com.abc.codegen;

import io.swagger.codegen.*;
import io.swagger.models.Operation;
import io.swagger.codegen.languages.*;

import java.util.*;
import java.io.File;

public class AbcCodegenGenerator extends AbstractJavaJAXRSServerCodegen {
	public AbcCodegenGenerator(){
		super();
		
		// source folder where to write the files
		sourceFolder = "src/main/java";
        invokerPackage = "io.swagger.api";
        artifactId = "com.abc";
        outputFolder = "generated-code/ABCCodegen";

        modelTemplateFiles.put("model.mustache", ".java"); 
        
        //Classes for the API
        apiTemplateFiles.put("api.mustache", ".java");
        apiTemplateFiles.put("apiService.mustache", ".java");
        apiTemplateFiles.put("apiServiceImpl.mustache", ".java");
        apiTemplateFiles.put("apiServiceFactory.mustache", ".java");
        apiPackage = "io.swagger.api";

        additionalProperties.put("title", title);
        
        //The location templates will be read from
        templateDir = "src/main/resources/ABCCodegen";
	}
    
	/**
	   * Configures a friendly name for the generator.  This will be used by the generator
	   * to select the library with the -l flag.
	   *
	   * @return the friendly name for the generator
	   */
	@Override
    public String getName()
    {
        return "abc"; 
    }
	
	/**
	   * Returns human-friendly help for the generator.  Provide the consumer with help
	   * tips, parameters here
	   *
	   * @return A string value for the help message
	   */
    @Override
    public String getHelp()
    {
        return "Generates an ABC Server application based on Jersey framework.";
    }

    @Override
    public void postProcessModelProperty(CodegenModel model, CodegenProperty property) {
        super.postProcessModelProperty(model, property);
        if("null".equals(property.example)) {
            property.example = null;
        }
    }

    @Override
    public void processOpts() {
        super.processOpts();

        if ( additionalProperties.containsKey(CodegenConstants.IMPL_FOLDER) ) {
            implFolder = (String) additionalProperties.get(CodegenConstants.IMPL_FOLDER);
        }

        supportingFiles.clear();
        writeOptional(outputFolder, new SupportingFile("pom.mustache", "", "pom.xml"));
        writeOptional(outputFolder, new SupportingFile("README.mustache", "", "README.md"));
        supportingFiles.add(new SupportingFile("ApiException.mustache", (sourceFolder + '/' + apiPackage).replace(".", "/"), "ApiException.java"));
        supportingFiles.add(new SupportingFile("ApiOriginFilter.mustache", (sourceFolder + '/' + apiPackage).replace(".", "/"), "ApiOriginFilter.java"));
        supportingFiles.add(new SupportingFile("ApiResponseMessage.mustache", (sourceFolder + '/' + apiPackage).replace(".", "/"), "ApiResponseMessage.java"));
        supportingFiles.add(new SupportingFile("NotFoundException.mustache", (sourceFolder + '/' + apiPackage).replace(".", "/"), "NotFoundException.java"));
        supportingFiles.add(new SupportingFile("jacksonJsonProvider.mustache", (sourceFolder + '/' + apiPackage).replace(".", "/"), "JacksonJsonProvider.java"));
		supportingFiles.add(new SupportingFile("BadRequestException.mustache", (sourceFolder + '/' + apiPackage).replace(".", "/"), "BadRequestException.java"));
        supportingFiles.add(new SupportingFile("JavaRestResourceUtil.mustache", (sourceFolder + '/' + apiPackage).replace(".", "/"), "JavaRestResourceUtil.java"));

        writeOptional(outputFolder, new SupportingFile("bootstrap.mustache", (implFolder + '/' + apiPackage).replace(".", "/"), "Bootstrap.java"));
        writeOptional(outputFolder, new SupportingFile("web.mustache", ("src/main/webapp/WEB-INF"), "web.xml"));
        writeOptional(outputFolder, new SupportingFile("index.mustache", ("src/main/webapp"), "index.html"));
        writeOptional(outputFolder, new SupportingFile("log4j.mustache", ("conf"), "log4j.properties"));      
        writeOptional(outputFolder, new SupportingFile("logback.mustache", ("src/main/resources"), "logback.xml"));

        if ( additionalProperties.containsKey("dateLibrary") ) {
            setDateLibrary(additionalProperties.get("dateLibrary").toString());
            additionalProperties.put(dateLibrary, "true");
        }

        if ( "joda".equals(dateLibrary) ) {
            supportingFiles.add(new SupportingFile("JodaDateTimeProvider.mustache", (sourceFolder + '/' + apiPackage).replace(".", "/"), "JodaDateTimeProvider.java"));
            supportingFiles.add(new SupportingFile("JodaLocalDateProvider.mustache", (sourceFolder + '/' + apiPackage).replace(".", "/"), "JodaLocalDateProvider.java"));
        } else if ( "java8".equals(dateLibrary) ) {
            supportingFiles.add(new SupportingFile("LocalDateTimeProvider.mustache", (sourceFolder + '/' + apiPackage).replace(".", "/"), "LocalDateTimeProvider.java"));
            supportingFiles.add(new SupportingFile("LocalDateProvider.mustache", (sourceFolder + '/' + apiPackage).replace(".", "/"), "LocalDateProvider.java"));
        }
    }

    @Override
    public void addOperationToGroup(String tag, String resourcePath, Operation operation, CodegenOperation co, Map<String, List<CodegenOperation>> operations) {
        String basePath = resourcePath;
        if (basePath.startsWith("/")) {
            basePath = basePath.substring(1);
        }
        int pos = basePath.indexOf("/");
        if (pos > 0) {
            basePath = basePath.substring(0, pos);
        }

        if (basePath == "") {
            basePath = "default";
        } else {
            if (co.path.startsWith("/" + basePath)) {
                co.path = co.path.substring(("/" + basePath).length());
            }
            co.subresourceOperation = !co.path.isEmpty();
        }
        List<CodegenOperation> opList = operations.get(basePath);
        if (opList == null) {
            opList = new ArrayList<CodegenOperation>();
            operations.put(basePath, opList);
        }
        opList.add(co);
        co.baseName = basePath;
    }

    public void hideGenerationTimestamp(boolean hideGenerationTimestamp) {
        this.hideGenerationTimestamp = hideGenerationTimestamp;
    }
  
}