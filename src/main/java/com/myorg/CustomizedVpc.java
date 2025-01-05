package com.myorg;

import com.myorg.util.GeneralUtil;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.ec2.*;
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
                .createInternetGateway(false)
               // .maxAzs(azs)
                .availabilityZones(fixAZ())
                .ipAddresses(IpAddresses.cidr("192.168.0.0/16"))
//                .subnetConfiguration(new ArrayList<>(){{
//                    add(buildSubnet(id,SubnetType.PUBLIC));
//                    add(buildSubnet(id,SubnetType.PRIVATE_WITH_EGRESS));
//                }})
                .build();
    }

    private List<String> fixAZ(){
        //may use azs but throw illegalargument exception when azs is less than available azs
        return GeneralUtil.getAvailabilityZones().subList(0,2);
    }

    private SubnetConfiguration buildSubnet(String prefix, SubnetType type){
        boolean isPublic = SubnetType.PUBLIC.equals(type);
        String subnetName = prefix + (isPublic ? "-public" : "-private") +"-subnet";

        SubnetConfiguration.Builder configBuilder = SubnetConfiguration.builder()
                .name(subnetName)
                .subnetType(type)
                .cidrMask(isPublic ? 24 : 25);


        if(isPublic){
            configBuilder.mapPublicIpOnLaunch(true);
        }
        return configBuilder.build();

    }

    public Vpc getVpc(){
        return vpc;
    }
}
