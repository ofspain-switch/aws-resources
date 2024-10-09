package com.myorg;

import com.fasterxml.jackson.core.Version;
import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.cxapi.CloudArtifact;
import software.amazon.awscdk.cxapi.CloudAssembly;
import software.amazon.awscdk.cxapi.CloudFormationStackArtifact;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AwsResourcesProvisionApp {
    public static void main(final String[] args) {
        App app = new App();
        Properties properties = loadVariables();
        Environment evn = Environment.builder()
                .account(properties.getProperty("region"))
                .region(properties.getProperty("account"))
//                .account(System.getenv("CDK_DEFAULT_ACCOUNT"))
//                .region(System.getenv("CDK_DEFAULT_REGION"))
                .build();
        new AwsS3BucketProvisionStack(app, "s3-bucket",
                StackProps.builder().stackName("Git-provision-stack")
                .env(evn)
                .build());

        new UbuntuArmStack(app, "UbuntuArmStack", StackProps.builder()
                .env(Environment.builder()
                        .account(System.getenv("CDK_DEFAULT_ACCOUNT"))
                .region(System.getenv("CDK_DEFAULT_REGION")) // Replace with your region
                        .build())
                .build());


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

