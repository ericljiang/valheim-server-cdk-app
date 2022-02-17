const EC2 = require('aws-sdk/clients/ec2');
const ec2 = new EC2();
const params = { InstanceIds: [process.env.INSTANCE_ID] };
exports.handler = async _ => ec2.startInstances(params).promise();
