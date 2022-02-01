const AWS = require('aws-sdk');
const ec2 = new AWS.EC2();
const route53 = new AWS.Route53();

exports.handler = async (event) => {
    const instances = await ec2.describeInstances({
        InstanceIds: [process.env.INSTANCE_ID]
    }).promise();
    const address = instances.Reservations[0].Instances[0].PublicIpAddress;
    console.log(address);

    const changeParams = {
        ChangeBatch: {
            Changes: [
                {
                    Action: "UPSERT",
                    ResourceRecordSet: {
                        Name: process.env.RECORD_NAME,
                        ResourceRecords: [
                            {
                                Value: address
                            }
                        ],
                        TTL: 30,
                        Type: "A"
                    }
                }
            ],
            Comment: `Route to ${process.env.INSTANCE_ID}`
        },
        HostedZoneId: process.env.HOSTED_ZONE_ID
    };
    return await route53.changeResourceRecordSets(changeParams).promise();
};
