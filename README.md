# Java Application with GitHub Actions & Azure App Service

This project demonstrates a complete CI/CD pipeline for deploying a Java Spring Boot application to Azure App Service using GitHub Actions.

## 📋 Project Structure

```
.
├── src/
│   ├── main/
│   │   ├── java/com/example/
│   │   │   ├── Application.java          # Spring Boot entry point
│   │   │   └── controller/
│   │   │       └── HelloController.java  # REST API endpoints
│   │   └── resources/
│   │       └── application.properties    # Application configuration
│   └── test/
│       └── java/com/example/
│           └── HelloControllerTest.java  # Unit tests
├── .github/
│   └── workflows/
│       └── deploy-to-azure.yml           # GitHub Actions workflow
├── pom.xml                                # Maven configuration
├── .gitignore                             # Git ignore file
├── README.md                              # This file
└── DEPLOYMENT_GUIDE.md                    # Detailed deployment steps
```

## ✨ Features

- **Spring Boot 3.1.5** - Modern Java framework for building applications
- **Maven** - Build automation tool
- **GitHub Actions** - Automated CI/CD pipeline
- **Azure App Service** - Cloud hosting on Microsoft Azure
- **Unit Tests** - Using JUnit 5 and Spring Test
- **Actuator** - Health checks and monitoring endpoints
- **Java 17** - Latest LTS Java version

## 📋 Prerequisites

1. **Local Development**
   - Java 17 or higher
   - Maven 3.6+
   - Git

2. **Azure Deployment**
   - Azure subscription
   - Azure App Service instance
   - Service Principal credentials

## 🔌 API Endpoints

- `GET /api/hello` - Returns a greeting message
- `GET /api/status` - Returns application status
- `GET /api/version` - Returns application version
- `GET /actuator/health` - Health check endpoint
- `GET /actuator/metrics` - Application metrics

## 🚀 Building Locally

### Clone the repository
```bash
git clone https://github.com/ShitalSPatil/Java-Github-actions-Aazure-App.git
cd Java-Github-actions-Aazure-App
```

### Build the project
```bash
mvn clean install
```

### Run tests
```bash
mvn test
```

### Package JAR file
```bash
mvn clean package
```

### Run the application locally
```bash
java -jar target/java-azure-app-1.0.0.jar
```

The application will start on `http://localhost:8080`

## 🔄 GitHub Actions Workflow

The workflow (`deploy-to-azure.yml`) performs the following steps:

1. **Checkout** - Clones the repository
2. **Setup JDK 17** - Configures Java environment
3. **Run Tests** - Executes Maven test suite
4. **Build JAR** - Creates executable JAR file
5. **Upload Artifact** - Stores JAR for deployment
6. **Login to Azure** - Authenticates with Azure using credentials
7. **Deploy** - Deploys JAR to Azure App Service
8. **Logout** - Cleans up Azure session

## 🎯 Setting Up Azure Deployment

### 1. Create Azure App Service

```bash
# Create resource group
az group create --name myResourceGroup --location eastus

# Create App Service Plan
az appservice plan create \
  --name myAppServicePlan \
  --resource-group myResourceGroup \
  --sku B1 \
  --is-linux

# Create Web App
az webapp create \
  --resource-group myResourceGroup \
  --plan myAppServicePlan \
  --name myJavaApp \
  --runtime "JAVA|17-java17"
```

### 2. Create Service Principal

```bash
az ad sp create-for-rbac \
  --name github-actions-sp \
  --role contributor \
  --scopes /subscriptions/{subscription-id}/resourceGroups/{resource-group}
```

### 3. Configure GitHub Secrets

Add the following secrets to your GitHub repository:

1. **AZURE_CREDENTIALS** - Paste the JSON output from service principal creation
2. **AZURE_APP_SERVICE_NAME** - Name of your Azure App Service

See **DEPLOYMENT_GUIDE.md** for detailed instructions.

### 4. Configure Azure App Service

```bash
# Configure Java runtime settings
az webapp config appsettings set \
  --resource-group myResourceGroup \
  --name myJavaApp \
  --settings WEBSITES_PORT=8080

# Set the startup command
az webapp config set \
  --resource-group myResourceGroup \
  --name myJavaApp \
  --startup-file "java -jar /home/site/wwwroot/java-azure-app-1.0.0.jar"
```

## 📤 Deployment

### Automatic Deployment

Push code to the `main` branch to trigger automatic deployment:

```bash
git add .
git commit -m "Your commit message"
git push origin main
```

### Monitor Deployment

1. Go to your GitHub repository
2. Click on the "Actions" tab
3. Select the workflow run to view logs
4. Check Azure Portal for deployment status

## 🐛 Troubleshooting

### JAR file not found during deployment
- Ensure `pom.xml` correctly specifies the artifact name
- Verify Maven build completes successfully in logs

### Authentication failures
- Verify `AZURE_CREDENTIALS` secret contains valid JSON
- Confirm service principal has correct permissions
- Check Azure subscription is active

### Application fails to start
- Check Azure App Service logs
- Verify Java version matches runtime configuration
- Ensure port 8080 is not blocked

### Health check fails
- Verify actuator endpoints are enabled in `application.properties`
- Test endpoints locally first
- Check Application Insights for errors

## ⚡ Performance Tips

- Use **B2 or higher** App Service plan for production
- Enable **Application Insights** for monitoring
- Configure **Autoscale** rules for traffic spikes
- Use **Azure CDN** for static content

## 🔒 Security Considerations

1. **Secrets Management**
   - Never commit credentials to repository
   - Rotate service principal credentials regularly
   - Use GitHub encrypted secrets

2. **Network Security**
   - Enable HTTPS only in App Service
   - Use Azure Key Vault for sensitive data
   - Configure network access restrictions

3. **Code Security**
   - Run security scans in CI/CD pipeline
   - Keep dependencies updated
   - Use OWASP best practices

## 📚 Additional Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Azure App Service Documentation](https://docs.microsoft.com/azure/app-service/)
- [GitHub Actions Documentation](https://docs.github.com/actions)
- [Azure CLI Reference](https://docs.microsoft.com/cli/azure/)
- [DEPLOYMENT_GUIDE.md](DEPLOYMENT_GUIDE.md) - Step-by-step deployment instructions

## 🤝 Contributing

Feel free to fork this repository and contribute improvements.

## 📄 License

This project is open source and available under the MIT License.

## 💬 Support

For issues or questions, please open a GitHub issue in the repository.
