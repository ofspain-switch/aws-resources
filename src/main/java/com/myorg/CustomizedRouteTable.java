package com.myorg;

import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.Tags;
import software.amazon.awscdk.services.ec2.CfnRouteTable;
import software.amazon.awscdk.services.ec2.CfnRouteTableProps;
import software.constructs.Construct;

public class CustomizedRouteTable extends Construct {

    private CfnRouteTable routeTable;
    private String vpcId;

    public CustomizedRouteTable(final Construct scope, String id){
        super(scope, id);
        CfnRouteTableProps routeTableProps = CfnRouteTableProps
                .builder().vpcId(vpcId)
                .build();
        routeTable = new CfnRouteTable(scope, id, routeTableProps);
        Tags.of(routeTable).add("Environment", "");

    }


}
