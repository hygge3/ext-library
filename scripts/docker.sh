#!/usr/bin/env bash
#
# ================================================ 说明 start ================================================
# Spring Boot 项目 Docker 镜像打包脚本，配合 boot.sh 使用（裸机用它，容器用这个）
# 自动根据所在目录获取最新的可执行 jar，生成 Dockerfile 并构建镜像
# 支持依赖分离（默认）：主 jar 在 /app，第三方依赖 jar 在 /lib（可单独挂载替换，不必重建镜像）
# 打包/拆分交给同目录的 package.sh（缺 jar 时自动调用），本脚本只负责镜像
# 镜像以非 root 运行，JVM 参数在运行时注入，SIGTERM 直接送达 JVM（容器里靠它优雅停机）
# 支持常用功能：拆分依赖、构建镜像、推送镜像、导出离线镜像包、只打印不执行
# ================================================ 说明 end   ================================================
#
# ================================================ 使用说明 start ================================================
# 注意：本脚本用了 bash 语法（[[ ]]、(( ))），必须用 bash 执行，不能用 sh/dash
#
# 1. 把本脚本放到项目根目录（与 jar 同级），package.sh 与它同目录
# 2. 拆分依赖（转调 package.sh）: bash docker.sh split
# 3. 构建镜像（缺 jar 时自动跑 package.sh package）: bash docker.sh build
# 4. 构建并推送: bash docker.sh push
# 5. 导出离线包: bash docker.sh save
# 6. 只看不执行（无 docker 环境也能自检）: DRY_RUN=1 bash docker.sh build
# 7. 交叉构建: PLATFORM=linux/arm64 bash docker.sh build
#
# 依赖默认分离打包（build 前自动跑 package.sh split）
# ================================================ 使用说明 end   ================================================
#
# ================================================ 参数 start ================================================
# ------ 文件路径（脚本所在目录，与执行时的工作目录无关）
DIR_PATH=$(
  cd "$(dirname "$0")" || exit
  pwd
)
# ------ 服务文件名（默认使用目录下最新的 .jar，war 需要外部容器，不处理）
SERVER_FILE_NAME=$(find "${DIR_PATH}" -maxdepth 1 -type f -name "*.jar" -printf "%T@\t%f\n" | sort -n | tail -1 | cut -f2-)
# ------ 镜像名（默认取目录名；脚本应放在 jar 同级的项目根目录）
IMAGE="${IMAGE:-$(basename "${DIR_PATH}")}"
IMAGE=$(echo "${IMAGE}" | tr '[:upper:]' '[:lower:]' | sed 's/[^a-z0-9._-]/-/g')
# ------ 镜像 tag（默认从 jar 名解析版本：app-1.2.3.jar → 1.2.3，解析不出用 latest）
JAR_VERSION=$(echo "${SERVER_FILE_NAME}" | sed -nE 's/^.*-([0-9]+(\.[0-9]+)*(-[A-Za-z0-9]+)?)\.jar$/\1/p')
TAG="${TAG:-${JAR_VERSION:-latest}}"
# ------ 基础镜像（JDK 25 与项目一致，JRE 即可运行 Spring Boot）
BASE_IMAGE="${BASE_IMAGE:-eclipse-temurin:25-jre}"
# ------ 应用端口（仅 EXPOSE 声明，实际端口由 SPRING_OPTS 或编排决定）
APP_PORT="${APP_PORT:-8080}"
# ------ 时区
TIMEZONE="${TIMEZONE:-Asia/Shanghai}"
# ------ 运行期 JVM 参数（构建时不烧进镜像，容器启动时用 -e JAVA_OPTS 覆盖）
JAVA_OPTS="${JAVA_OPTS:--XX:MaxRAMPercentage=75 -XX:+UseZGC -XX:+ExitOnOutOfMemoryError}"
# ------ 运行期 Spring 参数（同上，如 --spring.profiles.active=prod）
SPRING_OPTS="${SPRING_OPTS}"
# ------ 目标平台（留空跟随构建机，交叉构建用 linux/amd64 或 linux/arm64）
PLATFORM="${PLATFORM:-}"
# ------ Dockerfile、构建上下文、拆分产物的落地目录（package.sh split 也写这里）
BUILD_PATH="${DIR_PATH}/build/dist"
# ------ 只打印将要执行的命令与 Dockerfile，不实际调用 docker
DRY_RUN="${DRY_RUN:-0}"
#
# ================================================ 参数 end   ================================================
#
# ================================================ 方法 start ================================================
#
# 统一的错误出口
# ------ 步骤计数：每条步骤都打一行 [n/total]，失败的行里也带当前步骤号，定位到底卡在哪步
STEP=0
TOTAL=0
step() {
  STEP=$((STEP + 1))
  echo -e "\033[36m[${STEP}/${TOTAL}]\033[0m $*"
}
finish() {
  echo -e "\033[32m  ✔ $*\033[0m"
}
die() {
  echo -e "\033[31m[步骤 ${STEP}/${TOTAL}] $*\033[0m"
  exit 1
}
# 打印将要执行或已执行的命令（带颜色区分）
run() {
  echo -e "\033[40;37m$*\033[0m"
  [ "${DRY_RUN}" = "1" ] || "$@"
}
# 检查前置条件：存在可打包的 jar（docker 不在其中，拆分不需要 docker）
require_jar() {
  if [ -z "${SERVER_FILE_NAME}" ]; then
    die "${DIR_PATH} 下没有找到 .jar 文件，请先执行 ./gradlew bootJar（或 build）"
  fi
  # jar 名会写进 Dockerfile 的 COPY 指令，含空格时 Dockerfile 语法会碎
  if [[ "${SERVER_FILE_NAME}" == *" "* ]]; then
    die "jar 文件名含空格（${SERVER_FILE_NAME}），Dockerfile 的 COPY 无法处理，请改名"
  fi
}
# 检查前置条件：docker 可用（DRY_RUN 下不要求）
require_docker() {
  if [ "${DRY_RUN}" != "1" ] && ! command -v docker >/dev/null 2>&1; then
    die "没有找到 docker 命令，容器化构建需要 docker（只看不执行请用 DRY_RUN=1 bash docker.sh build）"
  fi
  # war 需要外部 Servlet 容器，镜像里没有 Tomcat，直接说清楚而不是构建出一个跑不起来的镜像
  if ls "${DIR_PATH}"/*.war >/dev/null 2>&1; then
    echo -e "\033[33m目录下存在 .war，本脚本只打包 .jar（war 需要外部容器，不在镜像内解压运行）\033[0m"
  fi
}
# 没有 jar 就先跑 package.sh 打包，打包后重新解析最新的 jar
ensure_jar() {
  [ -n "${SERVER_FILE_NAME}" ] && return
  [ -f "${DIR_PATH}/package.sh" ] || die "${DIR_PATH} 下没有 .jar，也没有同目录的 package.sh，无法自动打包"
  bash "${DIR_PATH}/package.sh" package
  SERVER_FILE_NAME=$(find "${DIR_PATH}" -maxdepth 1 -type f -name "*.jar" -printf "%T@\t%f\n" | sort -n | tail -1 | cut -f2-)
  [ -z "${SERVER_FILE_NAME}" ] && die "打包完仍然没有找到 .jar，看 package.sh 的输出"
}
# 已拆分的依赖 jar 数量，0 表示按整包（fat jar）打包
lib_count() {
  find "${BUILD_PATH}/lib" -maxdepth 1 -name "*.jar" 2>/dev/null | wc -l | tr -d ' '
}
#
# =======================================
# 拆分依赖：app.jar（只含应用自身类）+ lib/（BOOT-INF/lib 里的依赖 jar）
# =======================================
split() {
  step "确认已有可执行 jar"
  [ -n "$(find "${DIR_PATH}" -maxdepth 1 -type f -name "*.jar" 2>/dev/null)" ] || ensure_jar
  [ -f "${DIR_PATH}/package.sh" ] || die "缺少同目录的 package.sh（拆分逻辑在那里，避免两份实现）"
  step "转调 package.sh 拆分依赖"
  bash "${DIR_PATH}/package.sh" split
  echo -e "  → $(lib_count) 个依赖 → ${BUILD_PATH}/lib"
  finish "拆分完成"
}
#
# =======================================
# 生成 Dockerfile
# JAVA_OPTS / SPRING_OPTS 走运行时环境变量而不是 ENV 烧死值：同一个镜像可以换参数起多份
# =======================================
write_dockerfile() {
  mkdir -p "${BUILD_PATH}"
  # DRY_RUN 下允许还没有拆分产物，目的只是把 Dockerfile 打印出来给人看
  if [ "$(lib_count)" -eq 0 ] && [ "${DRY_RUN}" != "1" ]; then
    die "没有拆分产物（${BUILD_PATH}/lib 为空），先跑 bash package.sh split"
  fi
  {
    echo "# 由 docker.sh 生成，请勿手工修改（改脚本）"
    echo "FROM ${BASE_IMAGE}"
    echo
    echo "# 日志落盘到 /logs（Spring Boot 的 LOGGING_FILE_PATH 环境变量 ≡ logging.file.path）"
    echo "# 时区与编码：容器默认 UTC，日志与数据库时间会差 8 小时"
    echo "ENV TZ=${TIMEZONE} \\"
    echo "    LANG=C.UTF-8 \\"
    echo "    LOGGING_FILE_PATH=/logs"
    echo
    echo "# 目录约定：/app 放主 jar，/lib 放第三方依赖 jar，/logs 落日志，/config 放外部配置"
    echo "RUN mkdir -p /app /lib /logs /config \\"
    echo "    && chown -R 10001:10001 /logs /config"
    echo
    echo "# 上下文是 build/dist，只含主 jar 与依赖目录，.git、源码都不会进镜像"
    echo "COPY app.jar /app/app.jar"
    echo "COPY lib/ /lib/"
    echo
    echo "# 非 root 运行：数字 uid 不依赖基础镜像里的 passwd 条目"
    echo "USER 10001:10001"
    echo
    echo "EXPOSE ${APP_PORT}"
    echo
    echo "WORKDIR /app"
    echo
    echo "# additional-location 是叠加而不是替换，jar 内 application.yml 仍然生效；"
    echo "# exec 让 java 成为 1 号进程，docker stop 的 SIGTERM 直达 JVM，优雅停机才生效"
    echo "# PropertiesLauncher 从 -Dloader.path 装载外部依赖，主 jar 只提供应用自身类"
    echo "ENTRYPOINT [\"sh\", \"-c\", \"exec java \${JAVA_OPTS} -Dloader.path=/lib -cp /app/app.jar org.springframework.boot.loader.launch.PropertiesLauncher --spring.config.additional-location=file:/config/ \${SPRING_OPTS}\"]"
  } >"${BUILD_PATH}/Dockerfile"
  echo -e "生成的 Dockerfile：\033[36m${BUILD_PATH}/Dockerfile\033[0m（依赖 $(lib_count) 个，分离打包）"
}
#
# =======================================
# 构建镜像
# =======================================
build() {
  echo "---------- 构建 [start] ----------"
  step "检查前置条件（docker、war 混放）"
  require_docker
  step "定位可执行 jar（缺则转调 package.sh 打包）"
  ensure_jar
  echo "  → ${SERVER_FILE_NAME}"
  [ -f "${DIR_PATH}/package.sh" ] || die "缺少同目录的 package.sh（拆分逻辑在那里，避免两份实现）"
  # 默认分离打包：每次构建按当前 jar 重拆，避免旧的 lib 配上新的 app.jar
  # 拆分是本机动作而不是 docker 调用，强制真跑（否则 DRY_RUN 打印出的依赖数量是假的）
  step "拆分依赖 → ${BUILD_PATH}/lib"
  DRY_RUN=0 bash "${DIR_PATH}/package.sh" split
  echo "  → $(lib_count) 个依赖"
  step "生成 Dockerfile"
  write_dockerfile
  [ "${DRY_RUN}" = "1" ] && cat "${BUILD_PATH}/Dockerfile" && echo
  local platform_opt=()
  [ -n "${PLATFORM}" ] && platform_opt=(--platform "${PLATFORM}")
  local latest_opt=""
  [ "${TAG}" != "latest" ] && latest_opt="${IMAGE}:latest"
  step "docker build ${IMAGE}:${TAG}（基础镜像 ${BASE_IMAGE}）"
  run docker build "${platform_opt[@]}" \
    --file "${BUILD_PATH}/Dockerfile" \
    --tag "${IMAGE}:${TAG}" \
    ${latest_opt:+--tag ${latest_opt}} \
    "${BUILD_PATH}"
  step "输出结果"
  echo -e "  → \033[32m${IMAGE}:${TAG}\033[0m 构建完成"
  echo -e "  → 运行示例：\033[40;37mdocker run -d --name ${IMAGE} -p ${APP_PORT}:${APP_PORT} -v \$(pwd)/config:/config -v \$(pwd)/logs:/logs -e JAVA_OPTS='${JAVA_OPTS}' ${IMAGE}:${TAG}\033[0m"
  finish "构建完成"
  echo "---------- 构建 [end] ----------"
}
#
# =======================================
# 推送镜像（先构建再推送，避免推了个过期 tag）
# =======================================
push() {
  echo "---------- 推送 [start] ----------"
  build
  local latest_opt=""
  [ "${TAG}" != "latest" ] && latest_opt="${IMAGE}:latest"
  step "docker push ${IMAGE}:${TAG}${latest_opt:+ 与 ${latest_opt}}"
  run docker push "${IMAGE}:${TAG}"
  [ -n "${latest_opt}" ] && run docker push "${latest_opt}"
  finish "已推送 ${IMAGE}:${TAG}${latest_opt:+ 与 ${latest_opt}}"
  echo "---------- 推送 [end] ----------"
}
#
# =======================================
# 导出离线镜像包（内网机器没有 registry 时用 docker load 导入）
# =======================================
save() {
  echo "---------- 导出 [start] ----------"
  build
  local file="${DIR_PATH}/${IMAGE}-${TAG}.tar.gz"
  local latest_opt=""
  [ "${TAG}" != "latest" ] && latest_opt="${IMAGE}:latest"
  step "导出镜像到 ${file}"
  if [ "${DRY_RUN}" = "1" ]; then
    echo -e "\033[40;37mdocker save ${IMAGE}:${TAG} ${latest_opt} | gzip -1 > ${file}\033[0m"
  else
    docker save "${IMAGE}:${TAG}" ${latest_opt} | gzip -1 >"${file}"
    echo -e "  → \033[32m$(du -h "${file}" | cut -f1)\033[0m"
    echo -e "  → 目标机导入：\033[40;37mgunzip -c $(basename "${file}") | docker load\033[0m"
  fi
  finish "导出完成"
  echo "---------- 导出 [end] ----------"
}
#
# ================================================ 方法 end   ================================================
#
case "$1" in
'split')
  TOTAL=2
  split
  ;;
'build')
  TOTAL=6
  build
  ;;
'push')
  # 6 步构建 + 1 步推送，编号连续 1..7
  TOTAL=7
  push
  ;;
'save')
  TOTAL=7
  save
  ;;
*)
  echo "必须参数缺失 [split|build|push|save]"
  echo "  split  拆分主 jar 与依赖（app.jar + lib/），之后 build 自动按分离模式打包"
  echo "  build  生成 Dockerfile 并构建镜像 ${IMAGE}:${TAG}"
  echo "  push   构建后推送 ${IMAGE}:${TAG} 与 latest"
  echo "  save   构建后导出离线包 ${IMAGE}-${TAG}.tar.gz"
  exit 1
  ;;
esac
