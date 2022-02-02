const EC2 = require('aws-sdk/clients/ec2');
const http = require('http');
const ec2 = new EC2();
const params = { InstanceIds: [process.env.INSTANCE_ID] };

function getGameStatus(ipAddress) {
    return new Promise((resolve, reject) => {
        try {
            const options = { timeout: 2000 };
            http.get(`http://${ipAddress}/status.json`, options, (response) => {
                if (response.statusCode !== 200) {
                    resolve(new Error('statusCode=' + response.statusCode));
                }
                let str = '';
                response.on('data', (chunk) => str += chunk);
                response.on('end', () => resolve(JSON.parse(str)));
            }).on('error', resolve).end();
        } catch (error) {
            reject(error);
        }
    });
}

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

        const serverStatus = await getGameStatus(instanceStatus.publicIpAddress);

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
