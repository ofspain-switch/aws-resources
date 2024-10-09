package com.myorg;

import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.ec2.*;
import software.constructs.Construct;

import java.util.HashMap;
import java.util.Map;

//todo: link up with main for provisioning
public class UbuntuArmStack extends Stack {
    public UbuntuArmStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public UbuntuArmStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        Vpc vpc = Vpc.Builder.create(this, id + "-vpc")
                .vpcName(id + "-vpc")
                .build();

        final ISecurityGroup securityGroup = SecurityGroup.Builder.create(this, id + "-sg")
                .securityGroupName(id)
                .vpc(vpc)
                .build();

        securityGroup.addIngressRule(Peer.anyIpv4(), Port.tcp(22));

        //todo: investigate regioning in ec2 provisioning and ami to use
        final Map<String, String> armUbuntuAMIs = new HashMap<>();
       // armUbuntuAMIs.put("eu-west-1", "ami-0d7409d480c699f24");

        final IMachineImage armUbuntuMachineImage = MachineImage.genericLinux(armUbuntuAMIs);

        final Instance engineEC2Instance = Instance.Builder.create(this, id + "-ec2")
                .instanceName(id + "-ec2")
                .machineImage(armUbuntuMachineImage)
                .securityGroup(securityGroup)
                .instanceType(InstanceType.of(
                        InstanceClass.BURSTABLE4_GRAVITON,
                        InstanceSize.SMALL
                ))
                .vpcSubnets(
                        SubnetSelection.builder()
                                .subnetType(SubnetType.PUBLIC)
                                .build()
                )
                .vpc(vpc)
                .build();
    }
}
