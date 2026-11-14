#
# ================================================ 说明 start ================================================
# Spring Boot 项目 Docker 镜像打包脚本（PowerShell 版，与 docker.sh 行为一致）
# 默认分离打包：主 jar 在 /app，第三方依赖 jar 在 /lib（可单独挂载替换，不必重建镜像）
# 打包/拆分交给同目录的 package.ps1（缺 jar 时自动调用），本脚本只负责镜像
# 镜像以非 root 运行，JVM 参数在运行时注入，SIGTERM 直接送达 JVM（容器里靠它优雅停机）
# 本文件必须保存为 UTF-8 with BOM：Windows PowerShell 5.1 会按 GBK 读无 BOM 的脚本，中文乱码并报解析错误
# ================================================ 说明 end   ================================================
#
# ================================================ 使用说明 start ================================================
# 1. 把本脚本放到项目根目录（与 jar 同级），package.ps1 与它同目录
# 2. 拆分依赖（转调 package.ps1）: powershell -File docker.ps1 split
# 3. 构建镜像（默认先拆分；缺 jar 时自动打包）: powershell -File docker.ps1 build
# 4. 构建并推送: powershell -File docker.ps1 push
# 5. 导出离线包: powershell -File docker.ps1 save
# 6. 只看不执行（无 docker 环境也能自检）: $env:DRY_RUN=1; powershell -File docker.ps1 build
# 7. 交叉构建: $env:PLATFORM='linux/arm64'; powershell -File docker.ps1 build
#
# 若提示"未对文件进行数字签名"，用 powershell -ExecutionPolicy Bypass -File docker.ps1 build
# ================================================ 使用说明 end   ================================================
#
param(
    [Parameter(Position = 0)]
    [ValidateSet('split', 'build', 'push', 'save', '')]
    [string]$Command = ''
)

$ErrorActionPreference = 'Stop'
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

# ------ 脚本所在目录（项目根，与执行时的工作目录无关）
$DirPath = $PSScriptRoot
# ------ Dockerfile 与构建上下文（package.ps1 split 也写这里）
$BuildPath = Join-Path $DirPath 'build/dist'
# ------ 镜像名（默认取目录名，Docker 只接受小写）
$Image = if ($env:IMAGE) { $env:IMAGE } else { (Split-Path $DirPath -Leaf).ToLower() }
$Image = $Image -replace '[^a-z0-9._-]', '-'
# ------ 镜像 tag（默认从 jar 名解析版本：app-1.2.3.jar → 1.2.3，解析不出用 latest）
$ServerJar = Get-ChildItem -Path $DirPath -File -Filter '*.jar' -ErrorAction SilentlyContinue |
    Where-Object { $_.Name -notmatch '-(sources|javadoc|plain)\.jar$' } |
    Sort-Object LastWriteTime | Select-Object -Last 1
$ServerJarName = if ($ServerJar) { $ServerJar.Name } else { '' }
$JarVersion = if ($ServerJarName -match '^.*-([0-9]+(\.[0-9]+)*(-[A-Za-z0-9]+)?)\.jar$') { $Matches[1] } else { '' }
$Tag = if ($env:TAG) { $env:TAG } elseif ($JarVersion) { $JarVersion } else { 'latest' }
# ------ 基础镜像（JDK 25 与项目一致，JRE 即可运行 Spring Boot）
$BaseImage = if ($env:BASE_IMAGE) { $env:BASE_IMAGE } else { 'eclipse-temurin:25-jre' }
# ------ 应用端口（仅 EXPOSE 声明，实际端口由 SPRING_OPTS 或编排决定）
$AppPort = if ($env:APP_PORT) { $env:APP_PORT } else { '8080' }
# ------ 时区
$Timezone = if ($env:TIMEZONE) { $env:TIMEZONE } else { 'Asia/Shanghai' }
# ------ 运行期 JVM 参数（构建时不烧进镜像，容器启动时用 -e JAVA_OPTS 覆盖）
$JavaOpts = if ($env:JAVA_OPTS) { $env:JAVA_OPTS } else { '-XX:MaxRAMPercentage=75 -XX:+UseZGC -XX:+ExitOnOutOfMemoryError' }
# ------ 运行期 Spring 参数（同上，如 --spring.profiles.active=prod）
$SpringOpts = if ($env:SPRING_OPTS) { $env:SPRING_OPTS } else { '' }
# ------ 目标平台（留空跟随构建机，交叉构建用 linux/amd64 或 linux/arm64）
$Platform = if ($env:PLATFORM) { $env:PLATFORM } else { '' }
# ------ 只打印将要执行的命令与 Dockerfile，不实际调用 docker
$DryRun = if ($env:DRY_RUN) { $env:DRY_RUN } else { '0' }

function Die([string]$Message) {
    Write-Host "[步骤 $script:Step/$script:Total] $Message" -ForegroundColor Red
    exit 1
}

# 步骤计数：每条步骤都打一行 [n/total]，失败的行里也带当前步骤号
# TOTAL 由外层命令设定，嵌套阶段（push/save → build）继续累加编号
function Step([string]$Message) {
    $script:Step++
    Write-Host "[$script:Step/$script:Total] " -ForegroundColor Cyan -NoNewline
    Write-Host $Message
}

function Finish([string]$Message) {
    Write-Host "  OK $Message" -ForegroundColor Green
}

# 打印将要执行或已执行的命令（带颜色区分）
function Run([string[]]$CmdArgs) {
    Write-Host ($CmdArgs -join ' ') -ForegroundColor White
    if ($DryRun -ne '1') {
        & $CmdArgs[0] $CmdArgs[1..($CmdArgs.Length - 1)]
        if ($LASTEXITCODE -ne 0) { Die "命令执行失败：$($CmdArgs -join ' ')" }
    }
}

function Get-LibCount {
    $dir = Join-Path $BuildPath 'lib'
    if (-not (Test-Path $dir)) { return 0 }
    @(Get-ChildItem -Path $dir -File -Filter '*.jar').Count
}

# 检查前置条件：docker 可用（DRY_RUN 下不要求）
function Assert-Docker {
    if ($DryRun -ne '1' -and -not (Get-Command docker -ErrorAction SilentlyContinue)) {
        Die '没有找到 docker 命令，容器化构建需要 docker（只看不执行请用 $env:DRY_RUN=1）'
    }
    if (Get-ChildItem -Path $DirPath -File -Filter '*.war' -ErrorAction SilentlyContinue) {
        Write-Host '目录下存在 .war，本脚本只打包 .jar（war 需要外部容器，不在镜像内解压运行）' -ForegroundColor Yellow
    }
}

# 没有 jar 就先跑 package.ps1 打包，打包后重新解析最新的 jar
function Assert-Jar {
    if ($ServerJarName) { return }
    $pkg = Join-Path $DirPath 'package.ps1'
    if (-not (Test-Path $pkg)) { Die "$DirPath 下没有 .jar，也没有同目录的 package.ps1，无法自动打包" }
    & $pkg package
    $latest = Get-ChildItem -Path $DirPath -File -Filter '*.jar' -ErrorAction SilentlyContinue |
        Sort-Object LastWriteTime | Select-Object -Last 1
    if (-not $latest) { Die '打包完仍然没有找到 .jar，看 package.ps1 的输出' }
    $script:ServerJarName = $latest.Name
}

#
# =======================================
# 生成 Dockerfile
# JAVA_OPTS / SPRING_OPTS 走运行时环境变量而不是 ENV 烧死值：同一个镜像可以换参数起多份
# =======================================
function Write-Dockerfile {
    New-Item -ItemType Directory $BuildPath -Force | Out-Null
    # DRY_RUN 下允许还没有拆分产物，目的只是把 Dockerfile 打印出来给人看
    if ((Get-LibCount) -eq 0 -and $DryRun -ne '1') {
        Die "没有拆分产物（$(Join-Path $BuildPath 'lib') 为空），先跑 package.ps1 split"
    }
    $lines = @(
        '# 由 docker.ps1 生成，请勿手工修改（改脚本）'
        "FROM $BaseImage"
        ''
        '# 日志落盘到 /logs（Spring Boot 的 LOGGING_FILE_PATH 环境变量 ≡ logging.file.path）'
        '# 时区与编码：容器默认 UTC，日志与数据库时间会差 8 小时'
        "ENV TZ=$Timezone \"
        '    LANG=C.UTF-8 \'
        '    LOGGING_FILE_PATH=/logs'
        ''
        '# 目录约定：/app 放主 jar，/lib 放第三方依赖 jar，/logs 落日志，/config 放外部配置'
        'RUN mkdir -p /app /lib /logs /config \'
        '    && chown -R 10001:10001 /logs /config'
        ''
        '# 上下文是 build/dist，只含主 jar 与依赖目录，.git、源码都不会进镜像'
        'COPY app.jar /app/app.jar'
        'COPY lib/ /lib/'
        ''
        '# 非 root 运行：数字 uid 不依赖基础镜像里的 passwd 条目'
        'USER 10001:10001'
        ''
        "EXPOSE $AppPort"
        ''
        'WORKDIR /app'
        ''
        '# additional-location 是叠加而不是替换，jar 内 application.yml 仍然生效；'
        '# exec 让 java 成为 1 号进程，docker stop 的 SIGTERM 直达 JVM，优雅停机才生效'
        '# PropertiesLauncher 从 -Dloader.path 装载外部依赖，主 jar 只提供应用自身类'
        'ENTRYPOINT ["sh", "-c", "exec java ${JAVA_OPTS} -Dloader.path=/lib -cp /app/app.jar org.springframework.boot.loader.launch.PropertiesLauncher --spring.config.additional-location=file:/config/ ${SPRING_OPTS}"]'
    )
    $dockerfile = Join-Path $BuildPath 'Dockerfile'
    # UTF-8 无 BOM：中文注释不能写成 ascii（会变 ???），带 BOM 又会干扰 Dockerfile 首行
    $utf8NoBom = New-Object System.Text.UTF8Encoding($false)
    [System.IO.File]::WriteAllText($dockerfile, ($lines -join "`n"), $utf8NoBom)
    Write-Host "生成的 Dockerfile：$dockerfile（依赖 $(Get-LibCount) 个，分离打包）" -ForegroundColor Cyan
}

#
# =======================================
# 拆分依赖（转调 package.ps1，逻辑只留一份）
# =======================================
function Invoke-Split {
    if (-not $ServerJarName) { Assert-Jar }
    $pkg = Join-Path $DirPath 'package.ps1'
    if (-not (Test-Path $pkg)) { Die '缺少同目录的 package.ps1（拆分逻辑在那里，避免两份实现）' }
    Step '转调 package.ps1 拆分依赖'
    # 拆分是本机动作而不是 docker 调用，强制真跑（否则 DRY_RUN 下依赖数量是假的）
    $dryRun = $env:DRY_RUN
    $env:DRY_RUN = '0'
    try { & $pkg split }
    finally { $env:DRY_RUN = $dryRun }
    if (-not (Test-Path (Join-Path $BuildPath 'app.jar'))) { Die '拆分失败，看上面的 package.ps1 输出' }
}

#
# =======================================
# 构建镜像
# =======================================
function Invoke-Build {
    Write-Host '---------- 构建 [start] ----------'
    Step '检查前置条件（docker、war 混放）'
    Assert-Docker
    Step '定位可执行 jar（缺则转调 package.ps1 打包）'
    Assert-Jar
    Write-Host "  -> $ServerJarName"
    # 默认分离打包：每次构建按当前 jar 重拆，避免旧的 lib 配上新的 app.jar
    Step "拆分依赖 -> $BuildPath\lib"
    Invoke-Split
    Write-Host "  -> $(Get-LibCount) 个依赖"
    Step '生成 Dockerfile'
    Write-Dockerfile
    if ($DryRun -eq '1') { Get-Content (Join-Path $BuildPath 'Dockerfile') | Write-Host }
    Step "docker build ${Image}:${Tag}（基础镜像 $BaseImage）"
    $buildArgs = @('docker', 'build')
    if ($Platform) { $buildArgs += @('--platform', $Platform) }
    $buildArgs += @('--file', (Join-Path $BuildPath 'Dockerfile'), '--tag', "${Image}:${Tag}")
    if ($Tag -ne 'latest') { $buildArgs += @('--tag', "${Image}:latest") }
    $buildArgs += $BuildPath
    Run $buildArgs
    Step '输出结果'
    Write-Host "  -> ${Image}:${Tag}" -ForegroundColor Green
    Write-Host "  -> 运行示例：docker run -d --name $Image -p ${AppPort}:${AppPort} -v `$(pwd)/config:/config -v `$(pwd)/logs:/logs -e JAVA_OPTS='$JavaOpts' ${Image}:${Tag}" -ForegroundColor White
    Finish '构建完成'
    Write-Host '---------- 构建 [end] ----------'
}

#
# =======================================
# 推送镜像（先构建再推送，避免推了个过期 tag）
# =======================================
function Invoke-Push {
    Write-Host '---------- 推送 [start] ----------'
    Invoke-Build
    Step "docker push ${Image}:${Tag}$(if ($Tag -ne 'latest') { " 与 ${Image}:latest" })"
    Run @('docker', 'push', "${Image}:${Tag}")
    if ($Tag -ne 'latest') { Run @('docker', 'push', "${Image}:latest") }
    Finish "已推送 ${Image}:${Tag}"
    Write-Host '---------- 推送 [end] ----------'
}

#
# =======================================
# 导出离线镜像包（内网机器没有 registry 时用 docker load 导入）
# =======================================
function Invoke-Save {
    Write-Host '---------- 导出 [start] ----------'
    Invoke-Build
    $tarFile = Join-Path $DirPath "${Image}-${Tag}.tar"
    $gzFile = "${tarFile}.gz"
    Step "docker save -> $tarFile"
    $saveArgs = @('docker', 'save', '--output', $tarFile, "${Image}:${Tag}")
    if ($Tag -ne 'latest') { $saveArgs += "${Image}:latest" }
    Run $saveArgs
    Step 'gzip 压缩（无 tar 命令则保留 .tar）'
    # Windows 自带 bsdtar，支持 -z；没有就留着 tar 让人自己压
    if ($DryRun -ne '1' -and (Get-Command tar -ErrorAction SilentlyContinue)) {
        & tar -czf $gzFile -C (Split-Path $tarFile -Parent) (Split-Path $tarFile -Leaf)
        if ($LASTEXITCODE -eq 0) { Remove-Item $tarFile -Force; $tarFile = $gzFile }
    }
    Finish "导出完成：$tarFile"
    Write-Host '  -> 目标机导入：docker load --input <文件>' -ForegroundColor White
    Write-Host '---------- 导出 [end] ----------'
}

switch ($Command) {
    'split' { $script:Total = 2; $script:Step = 0; Step '确认已有可执行 jar'; Invoke-Split; Finish '拆分完成' }
    'build' { $script:Total = 6; $script:Step = 0; Invoke-Build }
    # 6 步构建 + 1 步推送，编号连续 1..7
    'push' { $script:Total = 7; $script:Step = 0; Invoke-Push }
    # 6 步构建 + save，save 后可能再压一步
    'save' { $script:Total = 8; $script:Step = 0; Invoke-Save }
    default {
        Write-Host '必须参数缺失 [split|build|push|save]'
        Write-Host "  split  拆分主 jar 与依赖（app.jar + lib/），之后 build 自动按分离模式打包"
        Write-Host "  build  生成 Dockerfile 并构建镜像 ${Image}:${Tag}"
        Write-Host "  push   构建后推送 ${Image}:${Tag} 与 latest"
        Write-Host "  save   构建后导出离线包 ${Image}-${Tag}.tar.gz"
        exit 1
    }
}
