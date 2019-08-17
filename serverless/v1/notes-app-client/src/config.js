export default {
    MAX_ATTACHMENT_SIZE: 5000000,
    s3: {
        BUCKET: "notes-app-uploads-bruno"
    },
    apiGateway: {
        URL: "https://ro55q71363.execute-api.us-east-1.amazonaws.com/prod",
        REGION: "us-east-1"
    },
    cognito: {
        USER_POOL_ID: "us-east-1_r9c93Nxod",
        APP_CLIENT_ID: "2m58trggpcetef9qobquknkk45",
        REGION: "us-east-1",
        IDENTITY_POOL_ID: "us-east-1:07d2cad3-baa1-4484-9594-4fdf53eee1dc"
    }
};