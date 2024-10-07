package com.myorg;

import com.fasterxml.jackson.core.Version;
import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.cxapi.CloudArtifact;
import software.amazon.awscdk.cxapi.CloudAssembly;
import software.amazon.awscdk.cxapi.CloudFormationStackArtifact;

public class AwsResourcesProvisionApp {
    public static void main(final String[] args) {
        App app = new App();
        Environment evn = Environment.builder()
                .account(System.getenv("CDK_DEFAULT_ACCOUNT"))
                .region(System.getenv("CDK_DEFAULT_REGION"))
                .build();
        new AwsS3BucketProvisionStack(app, "s3-bucket",
                StackProps.builder().stackName("Git-provision-stack")
                .env(evn)
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
}

