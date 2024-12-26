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
//                .account(properties.getProperty("region"))
//                .region(properties.getProperty("account"))
                .account(System.getenv("CDK_DEFAULT_ACCOUNT"))
                .region(System.getenv("CDK_DEFAULT_REGION"))
                .build();
        new AwsS3BucketProvisionStack(app, "s3-bucket",
                StackProps.builder().stackName("CDK_PROVISIONING-STACK")
                .env(evn)
                .build());

        StackProps stackProps = StackProps.builder()
                .env(Environment.builder()
                        .account(System.getenv("CDK_DEFAULT_ACCOUNT"))
                        .region(System.getenv("CDK_DEFAULT_REGION")) // Replace with your region
                        .build())
                .build();

        VPCResource vpcResource = new VPCResource(app, "test", stackProps);
        Vpc vpc = vpcResource.getVpc();

//        new UbuntuArmStack(app, "UbuntuArmStack", stackProps, vpc);


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


    private void output(App scope, Vpc vpc){
        CfnOutput.Builder.create(scope, "output-vpc")
                .value(vpc.getVpcId())
                .build();

//        CfnOutput.Builder.create(this, "PublicSubnetId")
//                .value(vpc.getPublicSubnets().get(0).getSubnetId())
//                .build();
        /**
         * {
         *   "MyVpcStack": {
         *     "VpcId": "vpc-123456",
         *     "PublicSubnetId": "subnet-123456"
         *   }
         * }
         */
    }
}


//todo: confirm how the bucket id or name used by cloudformation is named
//todo: confirm hiw the CDKToolkit[this becomes the stackname in cf] used by cdk id or name is generated

