package com.myorg;

import software.amazon.awscdk.*;
import software.amazon.awscdk.cxapi.CloudAssembly;
import software.amazon.awscdk.cxapi.CloudFormationStackArtifact;
import software.amazon.awscdk.services.ec2.Vpc;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AwsResourcesProvisionApp {
    public static void main(final String[] args) {
        App app = new App();
//        Properties properties = loadVariables();
        Environment evn = Environment.builder()
                .account(System.getenv("CDK_DEFAULT_ACCOUNT"))
                .region(System.getenv("CDK_DEFAULT_REGION"))
                .build();


        StackProps stackProps = StackProps.builder()
                .env(Environment.builder()
                        .account(System.getenv("CDK_DEFAULT_ACCOUNT"))
                        .region(System.getenv("CDK_DEFAULT_REGION")) // Replace with your region
                        .build())
                .build();

        CustomizedCloud customizedCloud = new CustomizedCloud(app, "test", stackProps);


        app.synth();

    }

    private static  void printCloudTemplate(App app){
        CloudAssembly assembly = app.synth();

        for(CloudFormationStackArtifact artifact : assembly.getStacks()){
            String template = artifact.getTemplate().toString();
            System.out.println(template);
        }

    }

    private static Properties loadVariables(){
        Properties properties = new Properties();

        // Load the properties file from the classpath (resources folder)
        try (InputStream input = App.class.getClassLoader().getResourceAsStream("env.properties")) {
            if (input == null) {
                System.out.println("Sorry, unable to find config.properties");
            }else{

                properties.load(input);
            }


        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return properties;
    }


}


//todo: confirm how the bucket id or name used by cloudformation is named
//todo: confirm hiw the CDKToolkit[this becomes the stackname in cf] used by cdk id or name is generated

