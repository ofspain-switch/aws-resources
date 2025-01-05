package com.myorg;

import com.myorg.util.GeneralUtil;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.Tags;
import software.amazon.awscdk.services.ec2.CfnSubnet;
import software.amazon.awscdk.services.ec2.CfnSubnetProps;
import software.amazon.awscdk.services.ec2.Subnet;
import software.amazon.awscdk.services.ec2.SubnetProps;
import software.constructs.Construct;

public class CustomizedSubnet extends Construct {

//    private Subnet subnet;
    private final CfnSubnet cfnSubnet;

    private String cidrBlock;
    private String availabilityZone;
    private String vpcId;

    private SubnetType subnetType;
    private StackProps props;


    public CustomizedSubnet(final Construct scope, String id, final StackProps props, SubnetType subnetType, String vpcId,
            String cidrBlock){
        super(scope, id);
        this.props = props;
        this.subnetType = subnetType;
        this.vpcId = vpcId;
        this.cidrBlock = cidrBlock;

        cfnSubnet = initializeSubnet(scope, id);
        Tags.of(cfnSubnet).add("Environment", "");

    }

    private String fixAZ(){
        String chc =  GeneralUtil.getDefaultAZFromStackProps(subnetType.equals(SubnetType.PUBLIC));
        return GeneralUtil.validString(availabilityZone) ? availabilityZone : chc;
    }

    private CfnSubnet initializeSubnet(Construct scope, String id){
        CfnSubnetProps cfnSubnetProps = CfnSubnetProps.builder()
                .availabilityZone(fixAZ())
                .vpcId(vpcId)
                .cidrBlock(cidrBlock)
                .enableDns64(fixPublicAccess())
                .mapPublicIpOnLaunch(fixPublicAccess())
                .mapPublicIpOnLaunch(fixPublicAccess())
                .build();


        return new CfnSubnet(scope, id+System.currentTimeMillis(), cfnSubnetProps);


//        SubnetProps subnetProps = SubnetProps.builder()
//                .availabilityZone(fixAZ())
//                .vpcId(vpcId)
//                .mapPublicIpOnLaunch(fixPublicAccess())
//                .cidrBlock(cidrBlock)
//                .build();
//
//        subnet = new Subnet(scope, id, subnetProps);


    }

    public CfnSubnet getCfnSubnet() {
        return cfnSubnet;
    }

    public Boolean fixPublicAccess(){
        return  subnetType.equals(SubnetType.PUBLIC);
    }


    public enum SubnetType{
        PUBLIC, LOCKED_PRIVATE,  PRIVATE_WITH_INGRESS_AND_EGRESS
    }
}
