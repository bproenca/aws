import AWS from 'aws-sdk';

// const credentials = new AWS.SharedIniFileCredentials({profile: 'bcp_aws_icloud'});
// AWS.config.credentials = credentials;
AWS.config.update({region:'us-east-1'});

export function call(action, params) {
  const dynamoDb = new AWS.DynamoDB.DocumentClient();

  return dynamoDb[action](params).promise();
}
