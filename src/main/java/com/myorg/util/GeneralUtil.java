package com.myorg.util;

import software.amazon.awscdk.*;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.services.ec2.Vpc;
import software.constructs.Construct;

import java.security.SecureRandom;
import java.util.*;

public class GeneralUtil {

    public static String generateRandomString(int length){

        String ALPHABET = "abcdefghijklmnopqrstuvwxyz";
         String ALPHANUMERIC = ALPHABET + "0123456789";
         SecureRandom SECURE_RANDOM = new SecureRandom();
        if (length < 1) {
            throw new IllegalArgumentException("Length must be at least 1");
        }

        StringBuilder sb = new StringBuilder(length);

        // Ensure the first character is alphabetic
        sb.append(ALPHABET.charAt(SECURE_RANDOM.nextInt(ALPHABET.length())));

        // Fill the remaining characters with alphanumeric characters
        for (int i = 1; i < length; i++) {
            sb.append(ALPHANUMERIC.charAt(SECURE_RANDOM.nextInt(ALPHANUMERIC.length())));
        }

        return sb.toString();
    }

    public static boolean validString(String s){
        return null != s && !s.isEmpty();
    }

    public static String getDefaultAZFromStackProps(boolean pubc) {
        int index = pubc ? 0 : 1;
        return getAvailabilityZones().get(index);
    }

    public static List<String> getAvailabilityZones(){
        return new ArrayList<>(){{
            add("us-east-1a");
            add("us-east-1b");
            add("us-east-1c");
            add("us-east-1d");
            add("us-east-1e");
            add("us-east-1f");
        }};
    }



    private static CfnOutput output(Stack scope, String id, String description, Map<String,String> ppts){
        CfnOutput.Builder builder = CfnOutput.Builder.create(scope, id);
        if(validString(description)){
            builder.description(description);
        }
        ppts.forEach(
                (k,v) -> {builder.key(k).value(v);}
        );

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
        return builder.build();
    }
}
