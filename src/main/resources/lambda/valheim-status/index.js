const EC2 = require('aws-sdk/clients/ec2');
const http = require('http');
const ec2 = new EC2();
const params = { InstanceIds: [process.env.INSTANCE_ID] };

exports.handler = async (event) => {
    try {
        const instanceDescriptions = await ec2.describeInstances(params).promise();
        const instance = instanceDescriptions.Reservations[0].Instances[0];
        const instanceStatus = {
            instanceId: instance.InstanceId,
            instanceType: instance.InstanceType,
            state: instance.State.Code,
            publicDnsName: instance.PublicDnsName,
            publicIpAddress: instance.PublicIpAddress
        };

        if (instanceStatus.state !== 16) {
            console.log("Instance is not running, exiting early.");
            return {
                statusCode: 200,
                body: JSON.stringify({ ...instanceStatus })
            };
        }

        let serverStatus;
        http.get(`http://${instanceStatus.publicIpAddress}/status.json`, (response) => {
            let str = '';
            response.on('data', (chunk) => str += chunk);
            response.on('end', () => serverStatus = JSON.parse(str));
        });

        return {
            statusCode: 200,
            body: JSON.stringify({ ...instanceStatus, serverStatus: serverStatus })
        };
    } catch (error) {
        console.error(error);
        return {
            statusCode: 500,
            body: JSON.stringify(error)
        };
    }
};
