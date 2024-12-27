package com.myorg;

import com.myorg.util.GeneralUtil;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.Tags;
import software.amazon.awscdk.services.ec2.*;
import software.constructs.Construct;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VPCResource extends Stack{
   //todo: please take note that vpc.fromlook up with vpclookupoption is used to return an exsisting vpc


    private final Vpc vpc;
    private final String stackPrefix;

   // private final CfnInternetGateway igw;

    public VPCResource(final Construct scope, final String stackPrefix, final StackProps props){
        super(scope, stackPrefix, props);
        this.stackPrefix = stackPrefix + "-"+GeneralUtil.generateRandomString(25);
        String vpcId = stackPrefix + "-" + stackPrefix+GeneralUtil.generateRandomString(30) +"-vpc";

        vpc = Vpc.Builder.create(this, vpcId)
                .vpcName(stackPrefix + "-vpc")
                .restrictDefaultSecurityGroup(true)
                .enableDnsHostnames(true)
                .enableDnsSupport(true)
                .natGateways(1)
                .maxAzs(2)
                .ipAddresses(IpAddresses.cidr("192.168.0.0/16"))
                .subnetConfiguration(new ArrayList(){{
                    add(buildSubnet(stackPrefix,SubnetType.PUBLIC));
                    add(buildSubnet(stackPrefix,SubnetType.PRIVATE_WITH_EGRESS));
                }})
                .build();

        associateIGW();

        CfnRouteTable publicRouteTable = provisionPublicRouteToIGW();




        CfnSubnet publicSubnet = (CfnSubnet) vpc.getPublicSubnets().get(0).getNode().getDefaultChild();

        CfnSubnetRouteTableAssociation publicAssociation = CfnSubnetRouteTableAssociation.Builder.create(this, generateName("","PublicSubnetRouteTableAssociation"))
                .routeTableId(publicRouteTable.getRef())
                .subnetId(publicSubnet.getRef())

                .build();
        publicAssociation.getNode().addDependency(publicSubnet, publicRouteTable);

        /**note no special rule is needed for public subnet since we are opening it for free flow**/



        Subnet prSubnet = (Subnet) vpc.getPrivateSubnets().get(0);

        String natId = associateNATGateway(prSubnet, this);




        //configure rules for the private subnets
        NetworkAcl privateACL = privateAccessToInternet();

        //associate private subnet with the private network acl

        CfnSubnet privateSubnet = (CfnSubnet) vpc.getPrivateSubnets().get(0).getNode().getDefaultChild();
        CfnSubnetNetworkAclAssociation privateAssociation = CfnSubnetNetworkAclAssociation.Builder.create(this, "PrivateSubnetNetworkAclAssociation")
                .networkAclId(privateACL.getNetworkAclId())
                .subnetId(privateSubnet.getRef())

                .build();

        privateAssociation.getNode().addDependency(privateACL, privateSubnet);


        attachFlowLog();

        Tags.of(vpc).add("Environment", stackPrefix);
        Tags.of(vpc).add("family", this.stackPrefix);


    }

    private CfnInternetGateway associateIGW(){
        CfnInternetGateway internetGateway = CfnInternetGateway.Builder
                .create(this, "InternetGateway").build();
        String id = generateName("","igw");
//        CfnVPCGatewayAttachment.Builder.create(this, id)
//                .vpcId(vpc.getVpcId())
//                .internetGatewayId(internetGateway.getRef())
//                .build();



        return internetGateway;
    }


    private CfnRouteTable provisionPublicRouteToIGW(){
        String routeTableId = generateName("","rtt");

        CfnRouteTable routeTable = CfnRouteTable.Builder.create(this, routeTableId)
                .vpcId(vpc.getVpcId())
                .build();
        CfnRoute route = CfnRoute.Builder.create(this, routeTableId.replace("-rtt", "-rt"))
                .routeTableId(routeTable.getRef())
                .destinationCidrBlock("0.0.0.0/0") // Route all traffic to the internet
                .gatewayId(vpc.getInternetGatewayId())
                .build();
        return routeTable;
    }


    private NetworkAcl privateAccessToInternet(){


        // Private Network ACL for the private subnet
        NetworkAcl privateAcl = NetworkAcl.Builder.create(this, "PrivateNetworkAcl")
                .vpc(vpc)
                .subnetSelection(SubnetSelection
                        .builder()
                       // .subnets(Collections.singletonList(subnet))
                        .subnetType(SubnetType.PRIVATE_WITH_EGRESS)
                        .build())
                .build();

        //NetworkAcl networkAcl = NetworkAcl.Builder.create(this, "MyNetworkAcl")
        //         .vpc(vpc)
        //         // the properties below are optional
        //         .networkAclName("networkAclName")
        //         .subnetSelection(SubnetSelection.builder()
        //                 .availabilityZones(List.of("availabilityZones"))
        //                 .onePerAz(false)
        //                 .subnetFilters(List.of(subnetFilter))
        //                 .subnetGroupName("subnetGroupName")
        //                 .subnets(List.of(subnet))
        //                 .subnetType(SubnetType.PRIVATE_ISOLATED)
        //                 .build())
        //         .build();


        // Allow outbound HTTPS traffic
        privateAcl.addEntry("AllowOutboundHTTPS", NetworkAclEntryProps.builder()
                .networkAcl(privateAcl)
                .ruleNumber(100)
                .cidr(AclCidr.ipv4("0.0.0.0/0"))
                .traffic(AclTraffic.tcpPort(443)) // HTTPS traffic
                .direction(TrafficDirection.EGRESS)
                .ruleAction(Action.ALLOW)
                .build());

// Allow outbound HTTP traffic
        privateAcl.addEntry("AllowOutboundHTTP", NetworkAclEntryProps.builder()
                .networkAcl(privateAcl)
                .ruleNumber(110)
                .cidr(AclCidr.ipv4("0.0.0.0/0"))
                .traffic(AclTraffic.tcpPort(80)) // HTTP traffic
                .direction(TrafficDirection.EGRESS)
                .ruleAction(Action.ALLOW)
                .build());

// Allow outbound DNS traffic (UDP)
        privateAcl.addEntry("AllowOutboundDNS", NetworkAclEntryProps.builder()
                .networkAcl(privateAcl)
                .ruleNumber(120)
                .cidr(AclCidr.ipv4("0.0.0.0/0"))
                .traffic(AclTraffic.udpPort(53)) // DNS traffic
                .direction(TrafficDirection.EGRESS)
                .ruleAction(Action.ALLOW)
                .build());


        // Allow inbound traffic for HTTPS responses
        privateAcl.addEntry("AllowInboundHTTPSResponses", NetworkAclEntryProps.builder()
                .networkAcl(privateAcl)
                .ruleNumber(200)
                .cidr(AclCidr.ipv4("0.0.0.0/0"))
                .traffic(AclTraffic.tcpPortRange(1024, 65535)) // Ephemeral port range
                .direction(TrafficDirection.INGRESS)
                .ruleAction(Action.ALLOW)
                .build());

// Allow inbound traffic for HTTP responses
        privateAcl.addEntry("AllowInboundHTTPResponses", NetworkAclEntryProps.builder()
                .networkAcl(privateAcl)
                .ruleNumber(210)
                .cidr(AclCidr.ipv4("0.0.0.0/0"))
                .traffic(AclTraffic.tcpPortRange(1024, 65535)) // Ephemeral port range
                .direction(TrafficDirection.INGRESS)
                .ruleAction(Action.ALLOW)
                .build());

// Allow inbound DNS responses
        privateAcl.addEntry("AllowInboundDNSResponses", NetworkAclEntryProps.builder()
                .networkAcl(privateAcl)
                .ruleNumber(220)
                .cidr(AclCidr.ipv4("0.0.0.0/0"))
                .traffic(AclTraffic.udpPort(53)) // DNS traffic
                .direction(TrafficDirection.INGRESS)
                .ruleAction(Action.ALLOW)
                .build());



        return privateAcl;
    }

    private SubnetConfiguration buildSubnet(String vpcPrefix, SubnetType type){
        boolean isPublic = SubnetType.PUBLIC.equals(type);
        String subnetName = vpcPrefix + (isPublic ? "-public" : "-private") +"-subnet";

        return SubnetConfiguration.builder()
                .name(subnetName)
                .subnetType(type)
                .cidrMask(isPublic ? 24 : 25)
                .build();
    }


    private String generateName(String prefix, String suffix) {
        StringBuilder nameBuilder = new StringBuilder();

        if (GeneralUtil.validString(prefix)) {
            nameBuilder.append(prefix).append("-");
        }

        nameBuilder.append(stackPrefix).append("-").append(suffix);
        return nameBuilder.toString();
    }


    private void attachFlowLog(){
        String name = generateName("", "flow-log");
        FlowLog.Builder.create(this, name)
                .resourceType(FlowLogResourceType.fromVpc(vpc))
                .trafficType(FlowLogTrafficType.ALL)
                .destination(FlowLogDestination.toCloudWatchLogs())
                .build();
    }

    private String associateNATGateway(Subnet subnet, final Stack stack){

        String id = generateName("nat","ngw");
        CfnEIP eip = CfnEIP.Builder.create(stack, generateName("","eip"))
                .build();
        CfnNatGateway natGateway = CfnNatGateway.Builder.create(stack, id)
                .allocationId(eip.getAttrAllocationId())
                .subnetId(subnet.getSubnetId())
                .build();

        subnet.addRoute("CustomRoute", AddRouteOptions.builder()
                .destinationCidrBlock("0.0.0.0/0")
                .routerId(natGateway.getRef())
                .routerType(RouterType.NAT_GATEWAY)
                .build());

        return natGateway.getRef();

    }

//String routeId = generateName("nat", "route");
//    CfnRoute route = CfnRoute.Builder.create(stack, routeId)
//            .routeTableId(((CfnSubnet) subnet.getNode().getDefaultChild()).getAttrRouteTableId()) // Route table for the subnet
//            .destinationCidrBlock("0.0.0.0/0") // Route all traffic
//            .natGatewayId(natGateway.getRef()) // Reference the NAT Gateway
//            .build();
//
//    // Add dependency to ensure NAT Gateway is created before the route
//    route.addDependsOn(natGateway);
//
//    return natGateway.getRef();

    public Vpc getVpc(){
        return vpc;
    }

}
