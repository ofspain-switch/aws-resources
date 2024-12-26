package com.myorg;

import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.s3.Bucket;
import software.amazon.awscdk.services.s3.BucketEncryption;
import software.constructs.Construct;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;

public class AwsS3BucketProvisionStack extends Stack {
    public AwsS3BucketProvisionStack(final Construct scope, final String id) {
        this(scope, id, null);

    }

    public AwsS3BucketProvisionStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);
        Bucket bucket = Bucket.Builder.create(this, "amzn-s3-demo-bucket")
                .versioned(true)
                .encryption(BucketEncryption.KMS_MANAGED)
                .bucketName("demo-basket")
                .build();
    }
}
