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
        super(scope, stackPrefix, props);

        customizedVpc = new CustomizedVpc(this, stackPrefix+"-vpc", props).getVpc();
        elasticIp = CfnEIP.Builder.create(this, stackPrefix+"-eip").build();
        associateIGW(this,stackPrefix);
        publicRouteTable = createRouteTable(this,stackPrefix+"-public");
        privateRouteTable = createRouteTable(this,stackPrefix+"-private");

        CustomizedSubnet publicSubnet = new CustomizedSubnet(this, stackPrefix+"-public_subnet", props, CustomizedSubnet.SubnetType.PUBLIC, customizedVpc.getVpcId());
        publicSubnets.add(publicSubnet.getCfnSubnet());
        CustomizedSubnet privateSubnet = new CustomizedSubnet(this, stackPrefix+"-private_subnet", props, CustomizedSubnet.SubnetType.PRIVATE_WITH_INGRESS_AND_EGRESS, customizedVpc.getVpcId());
        privateSubnets.add(privateSubnet.getCfnSubnet());

        provisionNatGateway(this, stackPrefix);


        associateRouteToSubnet(this, stackPrefix+"-public-", publicSubnets.get(0), publicRouteTable);
        associateRouteToSubnet(this, stackPrefix+"-private-", privateSubnets.get(0), privateRouteTable);

        createPrivateRoutes(this, stackPrefix, 1);
        createPublicRoutes(this, stackPrefix, 1);
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
