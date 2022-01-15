# Valheim Server CDK App

## TODO
- [x] Self-mutating CDK pipeline
- [x] Beta and Prod stages
- [x] Game server on EC2/Fargate
  - [x] Logs in CloudWatch
  - [x] Player count metric in CloudWatch
    - [ ] Add dimension to differentiate Beta and Prod
  - [ ] Server lifecycle hooks to SNS
  - [ ] Auto shutdown when idle
  - [ ] Persistent save file in S3?
- [ ] Web client
  - [ ] API
    - [ ] Start server
    - [ ] Get status
  - [ ] UI
- [ ] Discord notifications

## Useful commands

 * `cdk ls`          list all stacks in the app
 * `cdk synth`       emits the synthesized CloudFormation template
 * `cdk deploy`      deploy this stack to your default AWS account/region
 * `cdk diff`        compare deployed stack with current state
 * `cdk docs`        open CDK documentation
