# Step 0: Verify Docker is installed and running
Write-Host "Checking Docker installation at $(Get-Date)..."
if (!(Get-Command docker -ErrorAction SilentlyContinue)) {
    Write-Host "Docker is not installed. Please install Docker Desktop first."
    exit 1
}

# Check if Docker daemon is running
try {
    Write-Host "Checking Docker daemon status at $(Get-Date)..."
    docker ps | Out-Null
    Write-Host "Docker daemon is running at $(Get-Date)"
} catch {
    Write-Host "Docker daemon is not running. Please start Docker Desktop."
    exit 1
}

# Clean up any existing container with the same name
Write-Host "Cleaning up any existing MySQL container at $(Get-Date)..."
docker stop fulkoping-mysql 2> $null
docker rm fulkoping-mysql 2> $null
Write-Host "Container cleanup completed at $(Get-Date)"

# Step 1: Navigate to the 'sql' folder
Write-Host "Navigating to the 'sql' folder..."
cd ./sql/

# Step 2: Pull MySQL Docker image
Write-Host "Pulling MySQL Docker image..."
docker pull mysql:8.0

# Step 3: Run a MySQL container with mounted 'sql' folder
Write-Host "Running MySQL container with mounted 'sql' folder..."
$absolutePath = Resolve-Path "C:\Users\Desp\Documents\GitHub\FulkopingLibrary\sql"
docker run --name fulkoping-mysql -p 3306:3306 -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=fulkoping_library -v ${absolutePath}:/sql -d mysql:8.0
Write-Host "MySQL container started at $(Get-Date)"

# Step 4: Verify the container is running
Write-Host "Verifying the MySQL container is running..."
$containerRunning = docker ps | Select-String -Pattern "fulkoping-mysql"
if ($containerRunning) {
    Write-Host "MySQL container is running at $(Get-Date)"
} else {
    Write-Host "MySQL container failed to start. Exiting..."
    exit 1
}

# Wait for MySQL to initialize
Write-Host "Waiting for 60 seconds to give MySQL time to initialize..."
Start-Sleep -Seconds 60

# Step 7: Create database and populate tables with demo data
Write-Host "Creating database and populating tables with demo data..."
$containerName = "fulkoping-mysql"

# Create database
Write-Host "Executing create_database.sql..."
docker exec -i $containerName mysql -h localhost -uroot -proot fulkoping_library -e "source /sql/create_database.sql"

# Populate with demo data
Write-Host "Executing demo_data.sql..."
docker exec -i $containerName mysql -h localhost -uroot -proot fulkoping_library -e "source /sql/demo_data.sql"

# Step 8: Go back to the root directory to build the project
Write-Host "Navigating back to the root folder..."
cd ..

# Build the project using Maven
Write-Host "Building the project using Maven..."
$buildCommand = ".\mvnw clean package"  # Maven Wrapper command
if (-not (Test-Path .\mvnw)) {
    Write-Host "Maven Wrapper (.mvnw) not found. Please ensure it's available or install Maven manually."
    exit 1
}

Invoke-Expression $buildCommand

# Check if the JAR file was created
$jarPath = "target/FulkopingLibrary-1.0-SNAPSHOT-jar-with-dependencies.jar"
if (Test-Path $jarPath) {
    # Step 9: Run the JAR file
    Write-Host "Running the application..."
    Start-Process -NoNewWindow -Wait -FilePath "java" -ArgumentList "-jar", $jarPath
    Write-Host "Application started successfully!"
} else {
    Write-Host "JAR file not found. Please run 'mvn clean package' first."
}
