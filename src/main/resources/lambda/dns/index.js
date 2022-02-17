const Lambda = require('aws-sdk/clients/lambda');
const Route53 = require('aws-sdk/clients/route53');

const lambda = new Lambda();
const route53 = new Route53();

async function getIpAddress() {
    const params = { FunctionName: process.env.STATUS_FUNCTION };
    const data = await lambda.invoke(params).promise();
    const response = JSON.parse(data.Payload);
    if (response.statusCode !== 200) {
        throw Error("Unsuccessful status response: " + data.Payload);
    }
    const status = JSON.parse(response.body);
    if (!status.publicIpAddress) {
        throw Error(`IP not available: ${response.body}`);
    }
    return status.publicIpAddress;
}

exports.handler = async _ => {
    const address = await getIpAddress();
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
