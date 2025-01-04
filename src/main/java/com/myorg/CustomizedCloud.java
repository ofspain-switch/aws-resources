package com.myorg;

import software.amazon.awscdk.CfnResource;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.ec2.*;
import software.constructs.Construct;

import java.util.ArrayList;
import java.util.List;

public class CustomizedCloud extends Stack {

    private Vpc customizedVpc;

    private List<CfnRoute> privateRoutes = new ArrayList<>();
    private List<CfnRoute> publicRoutes = new ArrayList<>();

    private final CfnRouteTable publicRouteTable;
    private final CfnRouteTable privateRouteTable;

    private CfnEIP elasticIp;
    private CfnInternetGateway internetGateway;

    private List<CfnSubnet> privateSubnets;
    private List<CfnSubnet> publicSubnets;

    private CfnNatGateway natGateway;



    public CustomizedCloud(final Construct scope, final String stackPrefix, final StackProps props){

        customizedVpc = new CustomizedVpc(scope, stackPrefix+"-vpc", props).getVpc();
        elasticIp = CfnEIP.Builder.create(scope, stackPrefix+"-eip").build();
        associateIGW(scope,stackPrefix);
        publicRouteTable = createRouteTable(scope,stackPrefix+"-public");
        privateRouteTable = createRouteTable(scope,stackPrefix+"-private");

        CustomizedSubnet publicSubnet = new CustomizedSubnet(scope, stackPrefix+"-public_subnet", props, CustomizedSubnet.SubnetType.PUBLIC);
        publicSubnets.add(publicSubnet.getCfnSubnet());
        CustomizedSubnet privateSubnet = new CustomizedSubnet(scope, stackPrefix+"-private_subnet", props, CustomizedSubnet.SubnetType.PRIVATE_WITH_INGRESS_AND_EGRESS);
        privateSubnets.add(privateSubnet.getCfnSubnet());

        provisionNatGateway(scope, stackPrefix);


        associateRouteToSubnet(scope, stackPrefix+"-public-", publicSubnets.get(0), publicRouteTable);
        associateRouteToSubnet(scope, stackPrefix+"-private-", privateSubnets.get(0), privateRouteTable);

        createPrivateRoutes(scope, stackPrefix, 1);
        createPublicRoutes(scope, stackPrefix, 1);
    }

    private void associateIGW(Construct scope, String prefix){

        internetGateway = CfnInternetGateway.Builder
                .create(scope, prefix+"-igw")
                .build();

          CfnVPCGatewayAttachment.Builder.create(this, prefix+"-igw_attachment")
                        .vpcId(customizedVpc.getVpcId())
                        .internetGatewayId(internetGateway.getRef())
                        .build();
    }

    private CfnRouteTable createRouteTable(Construct scope, String prefix){

        CfnRouteTable routeTable = CfnRouteTable.Builder.create(scope, prefix+"-route_table")
                .vpcId(customizedVpc.getVpcId())
                .build();

        return routeTable;
    }

    private void createPublicRoutes(Construct scope, String prefix, int count){

        for(int i=1; i<=count; i++){
            CfnRoute route = CfnRoute.Builder.create(scope, prefix+"-public_route-"+i)
                    .destinationCidrBlock("0.0.0.0/0")
                    .gatewayId(internetGateway.getRef())
                    .routeTableId(publicRouteTable.getRef())
                    .build();

            publicRoutes.add(route);
        }
    }

    public void provisionNatGateway(Construct scope, String prefix){


        natGateway = CfnNatGateway.Builder
                .create(scope,"nat-"+prefix+"_natgateway")
                .subnetId(publicSubnets.get(0).getRef())
                .allocationId(elasticIp.getAttrAllocationId())
                .build();

        natGateway.addDependency(elasticIp);
    }

    private void createPrivateRoutes(Construct scope, String prefix, int count){

        for(int i=1; i<=count; i++){
            CfnRoute route = CfnRoute.Builder.create(scope, prefix+"-private_route-"+i)
                    .destinationCidrBlock("0.0.0.0/0")
                    .natGatewayId(natGateway.getRef())
                    .routeTableId(privateRouteTable.getRef())
                    .build();

            publicRoutes.add(route);
        }
    }

    private void associateRouteToSubnet(Construct scope, String prefix, CfnSubnet subnet, CfnRouteTable routeTable){
        CfnSubnetRouteTableAssociation association = CfnSubnetRouteTableAssociation.Builder
                .create(scope,prefix+"-subnet_route-table_association")
                .routeTableId(routeTable.getRef())
                .subnetId(subnet.getRef())
                .build();
    }

}
