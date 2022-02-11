const AWS = require('aws-sdk');
const route53 = new AWS.Route53();

exports.handler = async (event) => {
    const address = ""; // TODO
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
            ]
        },
        HostedZoneId: process.env.HOSTED_ZONE_ID
    };
    return await route53.changeResourceRecordSets(changeParams).promise();
};
