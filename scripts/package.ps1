#
# ================================================ 说明 start ================================================
# Spring Boot 便捷打包脚本（PowerShell 版，与 package.sh 行为一致）
# 构建可执行 jar，并可把依赖拆成 app.jar + lib/ 供 docker.ps1 / docker.sh 使用
# 自动识别构建工具（gradlew > mvnw > gradle > mvn）
# 产物：可执行 jar 拷到脚本所在目录；拆分产物进 build/dist（app.jar + lib/）
# 本文件必须保存为 UTF-8 with BOM：Windows PowerShell 5.1 会按 GBK 读无 BOM 的脚本，中文乱码并报解析错误
# ================================================ 说明 end   ================================================
#
# ================================================ 使用说明 start ================================================
# 1. 把本脚本放到项目根目录（与 gradlew.bat / pom.xml 同级）
# 2. 默认（打包 + 拆依赖）: powershell -File package.ps1
# 3. 只打包:      powershell -File package.ps1 package
# 4. 只拆依赖:    powershell -File package.ps1 split
# 5. 需要跑测试:  $env:SKIP_TESTS=0; powershell -File package.ps1
#
# 拆分只用 JDK 自带的 jar 命令，不需要 unzip/zip
# 若提示"无法加载文件 ... 未对文件进行数字签名"，用 powershell -ExecutionPolicy Bypass -File package.ps1
# ================================================ 使用说明 end   ================================================
#
param(
    [Parameter(Position = 0)]
    [ValidateSet('package', 'split', 'all', '')]
    [string]$Command = ''
)

$ErrorActionPreference = 'Stop'
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

# ------ 脚本所在目录（项目根，与执行时的工作目录无关）
$DirPath = $PSScriptRoot
# ------ 拆分的依赖目录与主 jar 落地处（docker 脚本把它当作构建上下文）
$DistDir = if ($env:DIST_DIR) { $env:DIST_DIR } else { Join-Path $DirPath 'build/dist' }
# ------ 可执行 jar 的落地目录（默认项目根，boot.sh 与 docker 脚本都从这里取"最新的 jar"）
$OutputDir = if ($env:OUTPUT_DIR) { $env:OUTPUT_DIR } else { $DirPath }
# ------ 是否跳过测试（默认跳过：打包不是 CI，测试留给 CI 跑）
$SkipTests = if ($env:SKIP_TESTS) { $env:SKIP_TESTS } else { '1' }
# ------ 只打印将要执行的命令，不实际执行
$DryRun = if ($env:DRY_RUN) { $env:DRY_RUN } else { '0' }

function Die([string]$Message) {
    Write-Host "[步骤 $script:Step/$script:Total] $Message" -ForegroundColor Red
    exit 1
}

# 步骤计数：每条步骤都打一行 [n/total]，失败的行里也带当前步骤号
function Step([string]$Message) {
    $script:Step++
    Write-Host "[$script:Step/$script:Total] " -ForegroundColor Cyan -NoNewline
    Write-Host $Message
}

function Finish([string]$Message) {
    Write-Host "  OK $Message" -ForegroundColor Green
}

function Show([string]$Message) {
    Write-Host $Message -ForegroundColor White
}

# 构建工具探测：wrapper 优先，其次 PATH 上的命令
function Get-BuildTool {
    if (Test-Path (Join-Path $DirPath 'gradlew.bat')) { return (Join-Path $DirPath 'gradlew.bat') }
    if (Test-Path (Join-Path $DirPath 'gradlew')) { return (Join-Path $DirPath 'gradlew') }
    if (Test-Path (Join-Path $DirPath 'mvnw.cmd')) { return (Join-Path $DirPath 'mvnw.cmd') }
    if (Test-Path (Join-Path $DirPath 'mvnw')) { return (Join-Path $DirPath 'mvnw') }
    if (Get-Command gradle -ErrorAction SilentlyContinue) { return 'gradle' }
    if (Get-Command mvn -ErrorAction SilentlyContinue) { return 'mvn' }
    Die '没有找到构建工具（gradlew / mvnw / gradle / mvn 都没有）'
}

# 项目根下最新的可执行 jar（拆分与 docker 打包都以它为准）
function Get-LatestJar {
    $jar = Get-ChildItem -Path $OutputDir -File -Filter '*.jar' -ErrorAction SilentlyContinue |
        Sort-Object LastWriteTime | Select-Object -Last 1
    if ($jar) { return $jar.Name }
    return ''
}

#
# =======================================
# 构建可执行 jar
# Gradle 用 bootJar（不带 -plain 后缀的包），Maven 用 package；-plain.jar 与 sources/javadoc 都不是可执行包
# =======================================
function Invoke-Package {
    Write-Host '---------- 打包 [start] ----------'
    $script:Total = 4
    $script:Step = 0
    Step '探测构建工具（gradlew > mvnw > gradle > mvn）'
    $tool = Get-BuildTool
    Write-Host "  -> $tool"
    $isMaven = $tool -match 'mvn'
    $args = if ($isMaven) { @('clean', 'package') } else { @('clean', 'bootJar') }
    if ($SkipTests -eq '1') {
        $args += if ($isMaven) { '-DskipTests' } else { '-x', 'test' }
    }
    Step "执行构建（跳过测试：$(if ($SkipTests -eq '1') { '是' } else { '否' })）"
    Show "(cd $DirPath; $tool $($args -join ' '))"
    if ($DryRun -ne '1') {
        Push-Location $DirPath
        try {
            & $tool @args
            if ($LASTEXITCODE -ne 0) { Die '构建失败，先看上面的编译错误' }
        }
        finally { Pop-Location }
    }
    # DRY_RUN 只打印，不去碰产物
    if ($DryRun -eq '1') {
        Write-Host "  -> （DRY_RUN）跳过产物查找与拷贝 -> $OutputDir"
        return
    }
    Step '定位可执行 jar（排除 -plain/sources/javadoc/original）'
    $exclude = '-(plain|sources|javadoc|tests)\.jar$|\.original$'
    $jar = @(
        Get-ChildItem -Path (Join-Path $DirPath 'build/libs'), (Join-Path $DirPath 'target') `
            -File -Filter '*.jar' -ErrorAction SilentlyContinue
    ) | Where-Object { $_.Name -notmatch $exclude } | Select-Object -First 1
    if (-not $jar) {
        Die '构建产物里没有可执行 jar（build/libs 或 target 下只有 -plain/sources/original 之类的包）'
    }
    Write-Host "  -> $($jar.FullName)"
    Step "拷贝 jar 到 $OutputDir"
    Copy-Item $jar.FullName (Join-Path $OutputDir $jar.Name) -Force
    Write-Host "  -> $(Join-Path $OutputDir $jar.Name)" -ForegroundColor Green
    Finish '打包完成'
    Write-Host '---------- 打包 [end] ----------'
}

#
# =======================================
# 拆分依赖：app.jar（只含应用自身类）+ lib/（BOOT-INF/lib 里的依赖 jar）
# =======================================
function Invoke-Split {
    Write-Host '---------- 拆分 [start] ----------'
    $script:Total = 6
    $script:Step = 0
    Step '检查 JDK 的 jar 命令'
    if (-not (Get-Command jar -ErrorAction SilentlyContinue)) {
        Die '拆分需要 JDK 自带的 jar 命令（JAVA_HOME\bin 不在 PATH 上）'
    }
    if ($DryRun -eq '1') {
        Write-Host "  -> （DRY_RUN）跳过拆分 -> $DistDir\{app.jar,lib}"
        Write-Host '---------- 拆分 [end] ----------'
        return
    }
    Step '定位待拆分的 jar'
    $jarName = Get-LatestJar
    if (-not $jarName) { Die "$OutputDir 下没有 jar，先执行 package.ps1 package" }
    if ($jarName -match ' ') { Die "jar 文件名含空格（$jarName），Dockerfile 的 COPY 无法处理，请改名" }
    Write-Host "  -> $jarName"
    $work = Join-Path ([System.IO.Path]::GetTempPath()) ("pkg-" + [guid]::NewGuid().ToString('N'))
    New-Item -ItemType Directory $work | Out-Null
    try {
        Step "解包到临时目录 $work"
        Push-Location $work
        try {
            & jar --extract --file (Join-Path $OutputDir $jarName)
            if ($LASTEXITCODE -ne 0) { Die "解包 $jarName 失败" }
        }
        finally { Pop-Location }

        $libDir = Join-Path $work 'BOOT-INF/lib'
        if (-not (Test-Path $libDir)) {
            Die 'jar 里没有 BOOT-INF/lib，说明它不是 Spring Boot 可执行 jar（或依赖已被排除）'
        }
        # 重新打包后靠 Start-Class 找入口，丢了 PropertiesLauncher 就启不起来，宁可在这里报错
        $manifest = Join-Path $work 'META-INF/MANIFEST.MF'
        if (-not (Select-String -Path $manifest -Pattern '^Start-Class:' -Quiet)) {
            Die 'MANIFEST.MF 里没有 Start-Class，不是 Spring Boot 可执行 jar'
        }
        Step "拆出依赖目录 -> $DistDir\lib"
        Remove-Item -Recurse -Force (Join-Path $DistDir 'lib'), (Join-Path $DistDir 'app.jar') -ErrorAction SilentlyContinue
        New-Item -ItemType Directory $DistDir -Force | Out-Null
        Move-Item $libDir (Join-Path $DistDir 'lib')
        Step '重新打包主 jar（去掉依赖，保留 Start-Class）'
        # 主 jar 里去掉依赖，否则同一批类会在 app.jar 与 lib 各存一份
        # 必须显式传 --manifest：jar --create 对输入树里的 MANIFEST.MF 会丢掉 Main-Class/Start-Class
        Push-Location $work
        try {
            & jar --create --file (Join-Path $DistDir 'app.jar') --manifest 'META-INF/MANIFEST.MF' '.'
            if ($LASTEXITCODE -ne 0) { Die '重新打包 app.jar 失败' }
        }
        finally { Pop-Location }

        Step '汇总产物'
        $libCount = (Get-ChildItem -Path (Join-Path $DistDir 'lib') -File -Filter '*.jar').Count
        $appSize = [math]::Round((Get-Item (Join-Path $DistDir 'app.jar')).Length / 1KB, 1)
        Write-Host "  -> 依赖 $libCount 个 -> $(Join-Path $DistDir 'lib')" -ForegroundColor Green
        Write-Host "  -> 主 jar -> $(Join-Path $DistDir 'app.jar')（${appSize}K）" -ForegroundColor Cyan
        Finish '拆分完成'
    }
    finally {
        Remove-Item -Recurse -Force $work -ErrorAction SilentlyContinue
    }
    Write-Host '---------- 拆分 [end] ----------'
}

switch ($Command) {
    'package' { Invoke-Package }
    'split' { Invoke-Split }
    default {
        Invoke-Package
        Invoke-Split
    }
}
