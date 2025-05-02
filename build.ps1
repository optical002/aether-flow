# PowerShell script to build a fat JAR using SBT assembly

$baseDir = $PSScriptRoot
$buildArtifactsDir = Join-Path -Path $baseDir -ChildPath "build_artifacts"
$gitBashPath = Join-Path -Path $baseDir -ChildPath "build_tools\git\git-bash.exe"

# Function to get the next build version number
function Get-NextBuildVersion {
    # Check if build_artifacts directory exists
    if (Test-Path -Path $buildArtifactsDir) {
        # Get all numeric subdirectories
        $existingDirs = Get-ChildItem -Path $buildArtifactsDir -Directory | 
                        Where-Object { $_.Name -match '^\d+$' } |
                        Select-Object -ExpandProperty Name |
                        ForEach-Object { [int]$_ }
        
        if ($existingDirs.Count -gt 0) {
            # Return the next version number (max + 1)
            return ($existingDirs | Measure-Object -Maximum).Maximum + 1
        }
    } else {
        # Create the build_artifacts directory if it doesn't exist
        New-Item -Path $buildArtifactsDir -ItemType Directory -Force | Out-Null
    }
    
    # Return 1 if no build directories exist yet
    return 1
}

# Get the next build version
$buildVersion = Get-NextBuildVersion
$newBuildDir = Join-Path -Path $buildArtifactsDir -ChildPath $buildVersion
Write-Host "Building version: $buildVersion" -ForegroundColor Cyan

# Create the new build directory
if (-not (Test-Path -Path $newBuildDir)) {
    New-Item -Path $newBuildDir -ItemType Directory -Force | Out-Null
    Write-Host "Created build directory: $newBuildDir"
}

# Run SBT clean assembly
Write-Host "Running 'sbt clean assembly'..." -ForegroundColor Yellow
try {
    if (Test-Path -Path $gitBashPath) {
        # Use git-bash to run sbt if available
        Write-Host "Using git-bash to run SBT..."
        $sbtProcess = Start-Process -FilePath $gitBashPath -ArgumentList "sbt clean assembly" -NoNewWindow -PassThru -Wait
    } else {
        # Otherwise run sbt directly
        Write-Host "Using direct SBT command..."
        $sbtProcess = Start-Process -FilePath "sbt" -ArgumentList "clean assembly" -NoNewWindow -PassThru -Wait
    }
    
    $exitCode = $sbtProcess.ExitCode
    if ($exitCode -ne 0) {
        Write-Host "SBT failed with exit code: $exitCode" -ForegroundColor Red
        exit $exitCode
    }
    
    Write-Host "SBT completed successfully." -ForegroundColor Green
} catch {
    Write-Host "Error running SBT: $_" -ForegroundColor Red
    exit 1
}

# Find and copy the JAR file to the new build directory
Write-Host "Finding and copying the assembled JAR file..."
$jarFiles = Get-ChildItem -Path "$baseDir\target" -Filter "*assembly*.jar" -Recurse
if ($jarFiles.Count -gt 0) {
    $jarFile = $jarFiles[0]
    $destJarPath = Join-Path -Path $newBuildDir -ChildPath $jarFile.Name
    $jarFileName = $jarFile.Name
    
    Copy-Item -Path $jarFile.FullName -Destination $destJarPath -Force
    Write-Host "Successfully copied JAR file to: $destJarPath" -ForegroundColor Green
    
    # Optional: Return the build version and JAR path for use by other scripts
    Write-Host "BUILD_VERSION=$buildVersion"
    Write-Host "JAR_PATH=$destJarPath"
    
    # Use existing Launch4j configuration
    Write-Host "Using existing Launch4j configuration..." -ForegroundColor Yellow
    $templateConfigPath = Join-Path -Path $baseDir -ChildPath "launch4j_config.xml"
    $exeOutputPath = Join-Path -Path $newBuildDir -ChildPath "game_launch4j.exe"
    
    if (Test-Path -Path $templateConfigPath) {
        # Copy the configuration file to the build directory
        Copy-Item -Path $templateConfigPath -Destination $newBuildDir -Force
        $configXmlPath = Join-Path -Path $newBuildDir -ChildPath "launch4j_config.xml"
        
        # Update the configuration with the correct JAR file and output path
        [xml]$configXml = Get-Content -Path $configXmlPath
        Write-Host "jar=$jarFileName"
        $configXml.launch4jConfig.jar = $jarFileName
        Write-Host "outfile=$exeOutputPath"
        $configXml.launch4jConfig.outfile = "$exeOutputPath"
        $configXml.Save($configXmlPath)
        
        Write-Host "Launch4j configuration updated with current build information." -ForegroundColor Green
    } else {
        Write-Host "Launch4j configuration not found at: $templateConfigPath" -ForegroundColor Red
        Write-Host "Skipping executable creation." -ForegroundColor Yellow
        return
    }
    
    # Run Launch4j to build the executable
    $launch4jDir = Join-Path -Path $baseDir -ChildPath "build_tools\launch4j"
    $launch4jExe = Join-Path -Path $launch4jDir -ChildPath "launch4jc.exe"
    
    if (Test-Path -Path $launch4jExe) {
        Write-Host "Running Launch4j to create executable..." -ForegroundColor Yellow
        $launch4jProcess = Start-Process -FilePath $launch4jExe -ArgumentList $configXmlPath -NoNewWindow -PassThru -Wait
        
        if ($launch4jProcess.ExitCode -eq 0) {
            if (Test-Path -Path $exeOutputPath) {
                Write-Host "Successfully created executable: $exeOutputPath" -ForegroundColor Green
            } else {
                Write-Host "Launch4j reported success but executable not found at: $exeOutputPath" -ForegroundColor Yellow
            }
        } else {
            Write-Host "Launch4j failed with exit code: $($launch4jProcess.ExitCode)" -ForegroundColor Red
        }
    } else {
        Write-Host "Launch4j executable not found at: $launch4jExe" -ForegroundColor Yellow
        Write-Host "Skipping executable creation." -ForegroundColor Yellow
    }
} else {
    Write-Host "No assembly JAR file found in target directory." -ForegroundColor Red
    exit 1
}

Write-Host "Build completed successfully!" -ForegroundColor Green
Write-Host "Build artifacts location: $newBuildDir" -ForegroundColor Cyan
