package com.myorg;

import com.myorg.util.GeneralUtil;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.ec2.CfnVPC;
import software.amazon.awscdk.services.ec2.IpAddresses;
import software.amazon.awscdk.services.ec2.SubnetType;
import software.amazon.awscdk.services.ec2.Vpc;
import software.constructs.Construct;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CustomizedVpc extends Construct {

    private final Vpc vpc;

    private int azs = 2;

    private List<String> availabilityZones;

    public CustomizedVpc(final Construct scope, final String id, final StackProps props){
        super(scope, id);

        vpc = Vpc.Builder.create(this, id)
                .vpcName(GeneralUtil.generateRandomString(15) + "-vpc")
                .restrictDefaultSecurityGroup(true)
                .enableDnsHostnames(true)
                .enableDnsSupport(true)
                .natGateways(0)
                .maxAzs(azs)
                .availabilityZones(fixAZ())
                .ipAddresses(IpAddresses.cidr("192.168.0.0/16"))
                .subnetConfiguration(Collections.emptyList())
                .build();
    }

    private List<String> fixAZ(){
        //may use azs but throw illegalargument exception when azs is less than available azs
        return GeneralUtil.getAvailabilityZones().subList(0,2);
    }

    public Vpc getVpc(){
        return vpc;
    }
}
