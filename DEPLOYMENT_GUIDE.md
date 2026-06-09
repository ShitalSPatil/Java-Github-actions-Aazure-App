# Deployment Guide - Java Application on Azure App Service

Complete step-by-step guide for deploying your Java Spring Boot application to Azure App Service using GitHub Actions.

## 📑 Table of Contents

1. [Prerequisites](#prerequisites)
2. [Azure Setup](#azure-setup)
3. [GitHub Configuration](#github-configuration)
4. [Deployment](#deployment)
5. [Verification](#verification)
6. [Troubleshooting](#troubleshooting)
7. [Performance Optimization](#performance-optimization)

## Prerequisites

### Required Tools
- ✅ Azure CLI installed and configured
- ✅ GitHub account with repository access
- ✅ Azure subscription with appropriate permissions
- ✅ Git installed locally

### Knowledge Requirements
- Basic understanding of Azure concepts
- Familiarity with Git and GitHub workflow
- Basic knowledge of Java/Spring Boot (optional)

---

## Azure Setup

### Step 1: Create Resource Group

```bash
# Set variables
RESOURCE_GROUP="myResourceGroup"
LOCATION="eastus"

# Create resource group
az group create \
  --name $RESOURCE_GROUP \
  --location $LOCATION
```

### Step 2: Create App Service Plan

```bash
# Set variables
APP_SERVICE_PLAN="myAppServicePlan"

# Create App Service Plan (Linux-based for Java)
az appservice plan create \
  --name $APP_SERVICE_PLAN \
  --resource-group $RESOURCE_GROUP \
  --sku B1 \
  --is-linux

# For production, use higher SKU:
# --sku P1V2 (Premium, recommended)
# --sku S1 (Standard)
```

### Step 3: Create Web App

```bash
# Set variables
APP_NAME="myJavaApp"

# Create the web app
az webapp create \
  --resource-group $RESOURCE_GROUP \
  --plan $APP_SERVICE_PLAN \
  --name $APP_NAME \
  --runtime "JAVA|17-java17"

# Enable continuous deployment from repository
az webapp config appsettings set \
  --resource-group $RESOURCE_GROUP \
  --name $APP_NAME \
  --settings WEBSITES_PORT=8080
```

### Step 4: Create Service Principal

The service principal allows GitHub Actions to authenticate with Azure.

```bash
# Get your subscription ID
SUBSCRIPTION_ID=$(az account show --query id -o tsv)

# Create service principal
az ad sp create-for-rbac \
  --name github-actions-sp-$(date +%s) \
  --role contributor \
  --scopes /subscriptions/$SUBSCRIPTION_ID/resourceGroups/$RESOURCE_GROUP
```

**⚠️ Important:** Save the entire JSON output. You'll need it for GitHub Secrets:

```json
{
  "clientId": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
  "clientSecret": "xxxxxxxx~xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx",
  "subscriptionId": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
  "tenantId": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"
}
```

### Step 5: Configure App Service Settings

```bash
# Configure startup command for JAR deployment
az webapp config set \
  --resource-group $RESOURCE_GROUP \
  --name $APP_NAME \
  --startup-file "java -jar /home/site/wwwroot/java-azure-app-1.0.0.jar"

# Set JVM options (optional)
az webapp config appsettings set \
  --resource-group $RESOURCE_GROUP \
  --name $APP_NAME \
  --settings "JAVA_OPTS=-Xmx512m -Xms256m"
```

---

## GitHub Configuration

### Step 1: Add GitHub Secrets

Navigate to your GitHub repository:

1. Go to **Settings** → **Secrets and variables** → **Actions**
2. Click **New repository secret**

#### Secret 1: AZURE_CREDENTIALS

- **Name:** `AZURE_CREDENTIALS`
- **Value:** Paste the entire JSON from service principal creation (from Step 4)

#### Secret 2: AZURE_APP_SERVICE_NAME

- **Name:** `AZURE_APP_SERVICE_NAME`
- **Value:** Your App Service name (e.g., `myJavaApp`)

### Step 2: Verify Workflow File

Ensure `.github/workflows/deploy-to-azure.yml` exists in your repository with the correct configuration.

---

## Deployment

### Automatic Deployment

Once GitHub Secrets are configured, any push to the `main` branch triggers automatic deployment:

```bash
# Make changes to your code
git add .
git commit -m "Your commit message"
git push origin main
```

### Manual Workflow Dispatch

To enable manual deployment, add this to your workflow file:

```yaml
on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]
  workflow_dispatch:  # Add this line
```

Then trigger from the Actions tab manually.

### Monitoring Workflow

1. Go to your GitHub repository
2. Click **Actions** tab
3. Select the latest workflow run
4. Watch build and deployment progress
5. Check logs for any errors

---

## Verification

### Step 1: Check Deployment Status

```bash
# Check app status
az webapp show \
  --resource-group $RESOURCE_GROUP \
  --name $APP_NAME

# Check deployment history
az webapp deployment list \
  --resource-group $RESOURCE_GROUP \
  --name $APP_NAME
```

### Step 2: Test Application

```bash
# Get the app URL
APP_URL=$(az webapp show \
  --resource-group $RESOURCE_GROUP \
  --name $APP_NAME \
  --query defaultHostName \
  -o tsv)

echo "Your app is available at: https://$APP_URL"

# Test endpoints
curl https://$APP_URL/api/hello
curl https://$APP_URL/api/status
curl https://$APP_URL/actuator/health
```

### Step 3: View Logs

```bash
# Stream live logs
az webapp log tail \
  --resource-group $RESOURCE_GROUP \
  --name $APP_NAME

# View deployment logs
az webapp deployment log show \
  --resource-group $RESOURCE_GROUP \
  --name $APP_NAME
```

---

## Troubleshooting

### ❌ Issue: "Package not found" in deployment

**Solution:**
- Verify JAR file is created in workflow logs
- Check `pom.xml` artifact name matches workflow path
- Ensure Maven build completes successfully

### ❌ Issue: Authentication fails in GitHub Actions

**Solution:**
```bash
# Verify service principal credentials
az login --service-principal \
  -u $CLIENT_ID \
  -p $CLIENT_SECRET \
  --tenant $TENANT_ID

# Re-create service principal if needed
az ad sp create-for-rbac ...
```

### ❌ Issue: Application fails to start on Azure

**Steps:**
1. Check logs: `az webapp log tail ...`
2. Verify Java version: `az webapp config show --query linuxFxVersion`
3. Test startup command locally
4. Check for missing environment variables

### ❌ Issue: Port 8080 not accessible

**Solution:**
```bash
# Verify WEBSITES_PORT setting
az webapp config appsettings set \
  --resource-group $RESOURCE_GROUP \
  --name $APP_NAME \
  --settings WEBSITES_PORT=8080
```

### ❌ Issue: Deployment timeout

**Solution:**
- Check app size (larger JAR files take longer)
- Review Azure App Service plan (might need upgrade)
- Check network connectivity
- Review workflow timeout settings

---

## Performance Optimization

### Recommended Settings for Production

```bash
# Use Premium plan
az appservice plan create \
  --name prodPlan \
  --resource-group $RESOURCE_GROUP \
  --sku P1V2 \
  --is-linux

# Enable always-on
az webapp config set \
  --resource-group $RESOURCE_GROUP \
  --name $APP_NAME \
  --always-on true

# Set JVM memory allocation
az webapp config appsettings set \
  --resource-group $RESOURCE_GROUP \
  --name $APP_NAME \
  --settings "JAVA_OPTS=-Xmx1024m -Xms512m"

# Enable Application Insights
az monitor app-insights component create \
  --app insights-$APP_NAME \
  --location $LOCATION \
  --resource-group $RESOURCE_GROUP \
  --application-type web
```

---

## 📊 Next Steps

1. **Set up monitoring** with Azure Application Insights
2. **Configure autoscale** for production environments
3. **Enable HTTPS** and custom domain
4. **Set up alerts** for failures
5. **Implement logging** and diagnostics
6. **Review security** - Check Azure Key Vault integration

---

## 💬 Support

For issues, check:
- GitHub Actions workflow logs
- Azure App Service logs in Azure Portal
- Application Insights telemetry
- Azure Activity Log
