package com.myorg;

import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.ec2.*;
import software.constructs.Construct;

import java.util.HashMap;
import java.util.Map;

//todo: link up with main for provisioning
public class UbuntuArmStack extends Stack {
    public UbuntuArmStack(final Construct scope, final String id, Vpc vpc) {
        this(scope, id, null, null);
    }

    public UbuntuArmStack(final Construct scope, final String id, final StackProps props,
                          Vpc vpc) {
        super(scope, id, props);


        final ISecurityGroup securityGroup = SecurityGroup.Builder.create(this, id + "-sg")
                .securityGroupName(id)
                .vpc(vpc)
                .build();

        securityGroup.addIngressRule(Peer.anyIpv4(), Port.tcp(22));

        //todo: investigate regioning in ec2 provisioning and ami to use
        final Map<String, String> armUbuntuAMIs = new HashMap<>();
        armUbuntuAMIs.put(props.getEnv().getRegion(), "ami-0866a3c8686eaeeba");
        //ami-0866a3c8686eaeeba (64-bit (x86)) / ami-0325498274077fac5 (64-bit (Arm))
        //ami-005fc0f236362e99f (64-bit (x86)) / ami-07ee04759daf109de (64-bit (Arm))

        final IMachineImage armUbuntuMachineImage = MachineImage.genericLinux(armUbuntuAMIs);

        final Instance engineEC2Instance = Instance.Builder.create(this, id + "-ec2")
                .instanceName(id + "-ec2")
                .machineImage(armUbuntuMachineImage)
                .securityGroup(securityGroup)
                .instanceType(InstanceType.of(
                        InstanceClass.T2,
                        InstanceSize.MICRO
                ))
                .vpcSubnets(
                        SubnetSelection.builder()
                                .subnets(vpc.getPublicSubnets())
                                .subnetType(SubnetType.PUBLIC)
                                .build()
                )
                .vpc(vpc)
                .build();
    }
}
