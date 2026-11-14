#!/usr/bin/env bash
#
# ================================================ 说明 start ================================================
# Spring Boot 便捷打包脚本：构建可执行 jar，并可把依赖拆成 app.jar + lib/ 供 docker 脚本使用
# 自动识别构建工具（gradlew > mvnw > gradle > mvn）
# 产物：可执行 jar 拷到脚本所在目录；拆分产物进 build/dist（app.jar + lib/）
# ================================================ 说明 end   ================================================
#
# ================================================ 使用说明 start ================================================
# 注意：本脚本用了 bash 语法，必须用 bash 执行，不能用 sh/dash
#
# 1. 把本脚本放到项目根目录（与 gradlew / pom.xml 同级）
# 2. 默认（打包 + 拆依赖）: bash package.sh
# 3. 只打包:      bash package.sh package
# 4. 只拆依赖:    bash package.sh split
# 5. 需要跑测试:  SKIP_TESTS=0 bash package.sh
#
# 拆分只用 JDK 自带的 jar 命令，不需要 unzip/zip
# docker.sh / docker.ps1 会自动调用本脚本（缺 jar 时打失败会报错，不会静默降级）
# ================================================ 使用说明 end   ================================================
#
# ================================================ 参数 start ================================================
# ------ 脚本所在目录（项目根，与执行时的工作目录无关）
DIR_PATH=$(
  cd "$(dirname "$0")" || exit
  pwd
)
# ------ 拆分的依赖目录与主 jar 落地处（docker 脚本把它当作构建上下文）
DIST_DIR="${DIST_DIR:-${DIR_PATH}/build/dist}"
# ------ 可执行 jar 的落地目录（默认项目根，boot.sh 与 docker 脚本都从这里取"最新的 jar"）
OUTPUT_DIR="${OUTPUT_DIR:-${DIR_PATH}}"
# ------ 是否跳过测试（默认跳过：打包不是 CI，测试留给 CI 跑）
SKIP_TESTS="${SKIP_TESTS:-1}"
# ------ 只打印将要执行的命令，不实际执行
DRY_RUN="${DRY_RUN:-0}"
#
# ================================================ 参数 end   ================================================
#
# ================================================ 方法 start ================================================
#
# ------ 步骤计数：每条步骤都打一行 [n/total]，失败的行里也带当前步骤号，定位到底卡在哪��
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
run() {
  echo -e "\033[40;37m$*\033[0m"
  [ "${DRY_RUN}" = "1" ] || "$@"
}
# 构建工具探测：wrapper 优先，其次 PATH 上的命令
detect_tool() {
  if [ -f "${DIR_PATH}/gradlew" ]; then
    echo "./gradlew"
  elif [ -f "${DIR_PATH}/mvnw" ]; then
    echo "./mvnw"
  elif command -v gradle >/dev/null 2>&1; then
    echo "gradle"
  elif command -v mvn >/dev/null 2>&1; then
    echo "mvn"
  else
    die "没有找到构建工具（gradlew / mvnw / gradle / mvn 都没有）"
  fi
}
# 项目根下最新的可执行 jar（拆分与 docker 打包都以它为准）
latest_jar() {
  find "${OUTPUT_DIR}" -maxdepth 1 -type f -name "*.jar" -printf "%T@\t%f\n" 2>/dev/null |
    sort -n | tail -1 | cut -f2-
}
#
# =======================================
# 构建可执行 jar
# Gradle 用 bootJar（不带 -plain 后缀的包），Maven 用 package；-plain.jar 与 sources/javadoc 都不是可执行包
# =======================================
package() {
  echo "---------- 打包 [start] ----------"
  step "探测构建工具（gradlew > mvnw > gradle > mvn）"
  local tool
  tool=$(detect_tool)
  echo "  → ${tool}"
  local args=()
  case "${tool}" in
  *mvn*)
    args=(clean package)
    [ "${SKIP_TESTS}" = "1" ] && args+=(-DskipTests)
    ;;
  *)
    args=(clean bootJar)
    [ "${SKIP_TESTS}" = "1" ] && args+=(-x test)
    ;;
  esac
  # wrapper 必须在项目根目录执行，脚本可能在别处被调用
  step "执行构建（跳过测试：$([ "${SKIP_TESTS}" = "1" ] && echo 是 || echo 否)）"
  echo -e "\033[40;37m(cd ${DIR_PATH} && ${tool} ${args[*]})\033[0m"
  [ "${DRY_RUN}" = "1" ] || (cd "${DIR_PATH}" && "${tool}" "${args[@]}") || die "构建失败，先看上面的编译错误"
  # DRY_RUN 只打印，不去碰产物
  if [ "${DRY_RUN}" = "1" ]; then
    echo "  → （DRY_RUN）跳过产物查找与拷贝"
    return
  fi
  step "定位可执行 jar（排除 -plain/sources/javadoc/original）"
  local jar
  jar=$(find "${DIR_PATH}/build/libs" "${DIR_PATH}/target" -maxdepth 1 -type f -name "*.jar" 2>/dev/null |
    grep -Ev -- '-(plain|sources|javadoc|tests)\.jar$|\.original$' | head -1)
  [ -z "${jar}" ] && die "构建产物里没有可执行 jar（build/libs 或 target 下只有 -plain/sources/original 之类的包）"
  echo "  → ${jar}"
  step "拷贝 jar 到 ${OUTPUT_DIR}"
  run cp "${jar}" "${OUTPUT_DIR}/"
  echo -e "  → \033[32m${OUTPUT_DIR}/$(basename "${jar}")\033[0m"
  echo "---------- 打包 [end] ----------"
}
#
# =======================================
# 拆分依赖：app.jar（只含应用自身类）+ lib/（BOOT-INF/lib 里的依赖 jar）
# =======================================
split() {
  echo "---------- 拆分 [start] ----------"
  step "检查 JDK 的 jar 命令"
  command -v jar >/dev/null 2>&1 || die "拆分需要 JDK 自带的 jar 命令（JAVA_HOME/bin 不在 PATH 上）"
  echo "  → $(command -v jar)"
  if [ "${DRY_RUN}" = "1" ]; then
    echo "  → （DRY_RUN）跳过拆分"
    echo "---------- 拆分 [end] ----------"
    return
  fi
  step "定位待拆分的 jar"
  local jar_name
  jar_name=$(latest_jar)
  [ -z "${jar_name}" ] && die "${OUTPUT_DIR} 下没有 jar，先执行 bash package.sh package"
  if [[ "${jar_name}" == *" "* ]]; then
    die "jar 文件名含空格（${jar_name}），Dockerfile 的 COPY 无法处理，请改名"
  fi
  echo "  → ${jar_name}"
  local work_dir
  work_dir=$(mktemp -d) || die "创建临时目录失败"
  trap 'rm -rf "${work_dir}"' EXIT
  step "解包到临时目录 ${work_dir}"
  (cd "${work_dir}" && jar --extract --file "${OUTPUT_DIR}/${jar_name}") || die "解包 ${jar_name} 失败"
  [ -d "${work_dir}/BOOT-INF/lib" ] || die "jar 里没有 BOOT-INF/lib，说明它不是 Spring Boot 可执行 jar（或依赖已被排除）"
  # 重新打包后靠 Start-Class 找入口，丢了 PropertiesLauncher 就启不起来，宁可在这里报错
  grep -q '^Start-Class:' "${work_dir}/META-INF/MANIFEST.MF" || die "MANIFEST.MF 里没有 Start-Class，不是 Spring Boot 可执行 jar"
  step "拆出依赖目录 → ${DIST_DIR}/lib"
  rm -rf "${DIST_DIR}/lib" "${DIST_DIR}/app.jar"
  mkdir -p "${DIST_DIR}"
  mv "${work_dir}/BOOT-INF/lib" "${DIST_DIR}/lib"
  rm -rf "${work_dir}/BOOT-INF/lib"
  step "重新打包主 jar（去掉依赖，保留 Start-Class）"
  # 主 jar 里必须去掉依赖，否则同一批类会在 app.jar 与 lib 各存一份
  # 必须显式传 --manifest：jar --create 对输入树里的 MANIFEST.MF 会丢掉 Main-Class/Start-Class
  (cd "${work_dir}" && jar --create --file "${DIST_DIR}/app.jar" --manifest META-INF/MANIFEST.MF .) ||
    die "重新打包 app.jar 失败"
  step "汇总产物"
  echo -e "  → 依赖 \033[32m$(find "${DIST_DIR}/lib" -maxdepth 1 -name "*.jar" | wc -l | tr -d ' ') 个\033[0m → ${DIST_DIR}/lib"
  echo -e "  → 主 jar \033[36m${DIST_DIR}/app.jar\033[0m（$(du -h "${DIST_DIR}/app.jar" | cut -f1)）"
  finish "拆分完成"
  echo "---------- 拆分 [end] ----------"
}
#
# ================================================ 方法 end   ================================================
#
case "$1" in
'package')
  TOTAL=4
  package
  ;;
'split')
  TOTAL=6
  split
  ;;
'all' | '')
  # 4 步打包 + 6 步拆分，编号连续 1..10
  TOTAL=10
  package
  split
  ;;
*)
  echo "必须参数缺失 [package|split|all]（不带参数 = all）"
  echo "  package  构建可执行 jar 并拷到 ${OUTPUT_DIR}"
  echo "  split    把 jar 拆成 ${DIST_DIR}/app.jar + lib/"
  echo "  all      package + split"
  exit 1
  ;;
esac
