package com.myorg.util;

import software.amazon.awscdk.services.ec2.Peer;
import software.amazon.awscdk.services.ec2.Port;
import software.amazon.awscdk.services.ec2.SecurityGroup;
import software.amazon.awscdk.services.ec2.Vpc;

public class SecurityGroupUtil {
    public SecurityGroup createSecurityGroup(String groupName, String description, Vpc vpc) {
        SecurityGroup securityGroup = SecurityGroup.Builder.create(this, groupName)
                .vpc(vpc)
                .securityGroupName(groupName)
                .description(description)
                .allowAllOutbound(true) // Allow all outbound traffic by default
                .build();

        // Add inbound rules (example for SSH, HTTP, and HTTPS)
        securityGroup.addIngressRule(Peer.anyIpv4(), Port.tcp(22), "Allow SSH access");
        securityGroup.addIngressRule(Peer.anyIpv4(), Port.tcp(80), "Allow HTTP access");
        securityGroup.addIngressRule(Peer.anyIpv4(), Port.tcp(443), "Allow HTTPS access");

        

        return securityGroup;
    }
}
