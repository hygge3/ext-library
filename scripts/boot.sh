#!/usr/bin/env bash
#
# ================================================ 说明 start ================================================
# 自动根据所在目录获取最新的可执行 jar、war
# 自动使用环境变量 JAVA_HOME 进行执行命令（可配置）
# 支持优雅下线（默认等待60秒，可配置，如果超过60秒则强制下线）
# 支持监听 SpringBoot 上线状态
# 支持添加自定义运行参数（如：JVM 参数、SpringBoot 参数）
# 支持常用功能：启动、停止、重启、查看状态、查看日志
# SpringBoot 项目支持查看应用端口信息
# ================================================ 说明 end   ================================================
#
# ================================================ 使用说明 start ================================================
# 注意：本脚本用了 bash 语法（[[ ]]、(( ))），必须用 bash 执行，不能用 sh/dash
#
# 1. 赋予执行权限: chmod +x boot.sh
# 2. 启动应用程序: bash boot.sh start
# 3. 停止应用程序: bash boot.sh stop
# 4. 查看应用程序: bash boot.sh status
# 5. 重启应用程序: bash boot.sh restart c # 加 c 删除历史日志文件
# 6. 查看应用日志: bash boot.sh logs
# ================================================ 使用说明 end   ================================================
#
# ================================================ 参数 start ================================================
# ------ Java 路径, 可以是 JAVA_HOME 也可以 Java 可执行文件路径, 默认使用环境变量的 JAVA_HOME
#JAVA="/usr/java/latest/bin/java"
#JAVA="/usr/java/latest/"
JAVA="${JAVA_HOME}"
# ------ 文件路径（脚本所在目录，与执行时的工作目录无关）
DIR_PATH=$(
  cd "$(dirname "$0")" || exit
  pwd
)
# ------ 服务文件名（默认使用目录下最新的.jar或者.war文件）
# 制表符分隔时间戳与文件名，%f 直接取 basename，避免文件名含空格时被 xargs/cut 拆坏
SERVER_FILE_NAME=$(find "${DIR_PATH}" -maxdepth 1 -type f \( -name "*.jar" -o -name "*.war" \) -printf "%T@\t%f\n" | sort -n | tail -1 | cut -f2-)
# ------ 堆大小（可用环境变量覆盖，如 XMS=1g XMX=1g bash boot.sh start）
XMS="${XMS:-4g}"
XMX="${XMX:-4g}"
# ------ Java 参数
# 日志、dump 一律用绝对路径：脚本不 cd 到 DIR_PATH，相对路径会在调用者的工作目录下找不到
JAVA_OPT="${JAVA_OPT} -server"
# 调整堆大小：通过 -Xms 和 -Xmx 参数来调整堆大小。合理的堆大小设置可以减少垃圾回收的频率，从而提高性能。
JAVA_OPT="${JAVA_OPT} -Xms${XMS} -Xmx${XMX}"
# 异步日志 + GC 日志按 5 × 20M 轮转，避免无限增长
JAVA_OPT="${JAVA_OPT} -Xlog:gc*:file=${DIR_PATH}/logs/gc.log:time,uptime,level,tags:filecount=5,filesize=20M -Xlog:async"
# 启用编译优化：启用分层编译，这将在运行时进行更多的 JIT 编译，提高应用程序的性能
JAVA_OPT="${JAVA_OPT} -XX:+TieredCompilation"
# 启用多线程处理器：来启用非一致性内存访问 (NUMA) 支持，以提高多核系统上的性能。
JAVA_OPT="${JAVA_OPT} -XX:+UseNUMA"
# 启用动态GC线程数，根据系统负载自动调整
JAVA_OPT="${JAVA_OPT} -XX:+UseDynamicNumberOfGCThreads"
# 启用字符串去重，减少内存占用
JAVA_OPT="${JAVA_OPT} -XX:+UseStringDeduplication"
# 当 JVM 发生 OOM 时，自动生成 DUMP 文件
JAVA_OPT="${JAVA_OPT} -XX:+HeapDumpOnOutOfMemoryError"
# 生成DUMP文件的路径（目录必须在启动前存在，否则 dump 写不出来）
JAVA_OPT="${JAVA_OPT} -XX:HeapDumpPath=${DIR_PATH}/logs/dump/"
# 开启分代ZGC
JAVA_OPT="${JAVA_OPT} -XX:+UseZGC"
# 关闭主动GC周期，在主动回收模式下，ZGC 会在系统空闲时自动执行垃圾回收，以减少垃圾回收在应用程序忙碌时所造成的影响。如果未指定此参数（默认情况），ZGC 会在需要时（即堆内存不足以满足分配请求时）执行垃圾回收。
JAVA_OPT="${JAVA_OPT} -XX:-ZProactive"
# 设置 GC 线程数为 CPU 核心数的一半，平衡性能和资源消耗（单核机器兜底为 1，0 会被 JVM 拒绝）
JAVA_OPT="${JAVA_OPT} -XX:ParallelGCThreads=$(($(nproc) / 2 > 1 ? $(nproc) / 2 : 1))"
# ------ Spring 参数
SPRING_OPT="${SPRING_OPT}"
# ------ 控制台文件
#CONSOLE_FILE="/dev/null"
CONSOLE_FILE="${DIR_PATH}/logs/console.log"
# ------ 停止时等待超时的秒数（可配置）
STOP_TIMEOUT="${STOP_TIMEOUT:-60}"
# ------ 启动时等待端口绑定的最长秒数（可配置）
PORT_TIMEOUT="${PORT_TIMEOUT:-300}"
#
# ================================================ 参数 end   ================================================
#
# ================================================ 方法 start ================================================
#
# 服务PID
_SERVER_PID=0
# 服务端口
_SERVER_PORT=0
# 日志目录
_LOG_PATH="${DIR_PATH}/logs"
# 输入的第二个参数
_P2=$2
#
# =======================================
# 判断给定 pid 是否为本脚本启动的应用
# 只看 pid 是否存在不够：机器重启后 pidfile 里的号可能已被别的进程复用，
# 按号码直接 kill 会误杀无关进程
# =======================================
is_our_pid() {
  local pid=$1
  # 目录里没 jar 时名字为空，grep -qF "" 会匹配任何进程，必须提前返回
  [ -n "${pid}" ] && [ -n "${SERVER_FILE_NAME}" ] || return 1
  if [ -r "/proc/${pid}/cmdline" ]; then
    # cat 包一层：进程可能在检查与读取之间退出，直接重定向会报错刷屏
    cat "/proc/${pid}/cmdline" 2>/dev/null | tr '\0' ' ' | grep -qF "${SERVER_FILE_NAME}"
  else
    # 非 Linux 没有 /proc，退化为 ps
    ps -p "${pid}" -o args= 2>/dev/null | grep -qF "${SERVER_FILE_NAME}"
  fi
}
#
# =======================================
# 解析服务的 pid
# =======================================
resolve_server_pid() {
  _SERVER_PID=0
  if [ -f "${_LOG_PATH}/server.pid" ]; then
    # pid 文件存在则读取文件
    local pid
    pid=$(tr -dc '0-9' <"${_LOG_PATH}/server.pid")
    if [ -n "${pid}" ] && is_our_pid "${pid}"; then
      _SERVER_PID=${pid}
    else
      # 内容非法或进程已不存在（或号已被别的进程复用），删除这个文件
      rm -f "${_LOG_PATH}/server.pid"
      sleep 0.2
    fi
  fi
}
#
# =======================================
# 列出本进程正在监听的端口
# ss/netstat 的 PID 列只有 root 可见，非 root 下返回空，调用方需按「未知」处理
# =======================================
list_server_ports() {
  if command -v ss >/dev/null 2>&1; then
    ss -ltnp 2>/dev/null | grep "pid=${_SERVER_PID}," | awk '{print $4}'
  elif command -v netstat >/dev/null 2>&1; then
    netstat -tulnp 2>/dev/null | grep "${_SERVER_PID}/" | awk '{print $4}'
  fi
}
#
# =======================================
# 解析服务的端口
# 返回 0 已绑定端口 / 1 进程已退出或启动失败 / 2 超时仍未拿到端口信息
# =======================================
retry_resolve_server_port() {
  _SERVER_PORT=0
  local i
  for ((i = 0; i <= PORT_TIMEOUT; i++)); do
    # 进程没了，没必要再等
    if ! is_our_pid "${_SERVER_PID}"; then
      return 1
    fi
    # 启动失败时 JVM 不会退出，只会在控制台打印栈，命中关键字就直接放弃
    if [ -f "${CONSOLE_FILE}" ] &&
      grep -qE "APPLICATION FAILED TO START|Error starting ApplicationContext|Web server failed to start|Exception encountered during context initialization" "${CONSOLE_FILE}"; then
      return 1
    fi
    local ports
    ports=$(list_server_ports)
    if [ -n "${ports}" ]; then
      _SERVER_PORT_TEXT=$(echo "${ports}" | awk 'BEGIN{ORS=" "}NF{print}')
      return 0
    fi
    [[ $((i % 5)) -eq 0 ]] && echo "观察服务器端口使用情况 ${i}s"
    sleep 1
  done
  return 2
}
#
# =======================================
# 启动
# =======================================
start() {
  echo "---------- 启动 [start] ----------"
  # 解析服务的 pid
  resolve_server_pid
  if [ "${_SERVER_PID}" -ne 0 ]; then
    # 如果 _SERVER_PID != 0, 那就说明应用程序在运行,不进行启动
    echo "请不要尝试启动, ${SERVER_FILE_NAME} 已经在运行了! [ SERVER_PID = ${_SERVER_PID} ]"
    echo "---------- 启动 [end] ----------"
    return
  fi
  # 目录里没有可执行包时早报错，否则用户只会看到一句模糊的「启动失败」
  if [ -z "${SERVER_FILE_NAME}" ]; then
    echo -e "\033[31m${DIR_PATH} 下没有找到 .jar 或 .war 文件\033[0m"
    exit 1
  fi
  #
  echo "启动 ${SERVER_FILE_NAME} 中 ..."
  # 如果 JAVA 是一个目录,则加上/bin/java
  [ -d "${JAVA}" ] && JAVA="${JAVA}/bin/java"
  # 如果 JAVA 不是一个存在的文件, 则尝试使用默认的java
  [ ! -e "$JAVA" ] && JAVA=$(which java)
  # 最终 Java 不是一个存在的文件, 则清空
  [ ! -e "$JAVA" ] && unset JAVA
  if [ -z "${JAVA}" ]; then
    echo -e "\033[31m请在您的环境中设置JAVA_HOME变量，我们需要java（x64）！\033[0m"
    exit 1
  fi
  # 判断是否需要删除日志文件
  if [[ "$_P2" == "c" ]]; then
    # 如果第二个参数是c, 删除日志文件
    echo -e "\033[33m删除 ${SERVER_FILE_NAME} 的日志文件(${DIR_PATH}/logs/*)\033[0m"
    rm -rf "${DIR_PATH}"/logs/*
  fi
  # 检查日志目录（含 dump 目录，OOM 时目录不存在则 dump 静默失败）
  mkdir -p "${_LOG_PATH}/dump"
  # 上一次的控制台日志留一份，重启后还能查上一次的异常
  [ -f "${CONSOLE_FILE}" ] && mv -f "${CONSOLE_FILE}" "${CONSOLE_FILE}.1"
  # 执行脚本
  # nohup java -Xmx128m -jar /home/app/server.jar --spring.profiles.active=prod >> /home/app/logs/console.log 2>&1 & echo $! > /home/app/logs/server.pid
  #
  # 运行的参数
  # 用数组传参，jar 路径含空格时不会被拆成多个参数
  read -r -a RUN_OPT <<<"${JAVA_OPT} ${SPRING_OPT}"
  RUN_OPT_TEXT="${JAVA} ${JAVA_OPT} ${SPRING_OPT} -jar ${DIR_PATH}/${SERVER_FILE_NAME}"
  #
  echo "${RUN_OPT_TEXT}"
  echo "${RUN_OPT_TEXT}" >"${CONSOLE_FILE}"
  nohup "$JAVA" "${RUN_OPT[@]}" -jar "${DIR_PATH}/${SERVER_FILE_NAME}" >>"${CONSOLE_FILE}" 2>&1 &
  echo $! >"${_LOG_PATH}/server.pid"
  #
  echo "${SERVER_FILE_NAME} 正在运行"
  echo -e "你可以查看日志文件 \033[36m${CONSOLE_FILE}\033[0m 或者执行下面的命令"
  echo -e "\033[40;37mtail -f -n 300 ${CONSOLE_FILE}\033[0m"
  # 睡眠1秒
  sleep 1
  # 解析服务的 pid
  resolve_server_pid
  # 判断应用程序是否成功跑起来
  if [ "${_SERVER_PID}" -eq 0 ]; then
    echo -e "\033[31m${SERVER_FILE_NAME} 启动失败，请查看 ${CONSOLE_FILE}\033[0m"
  else
    retry_resolve_server_port
    case $? in
    0)
      echo -e "\033[32m${SERVER_FILE_NAME} 启动成功 [ SERVER_PID = ${_SERVER_PID}, SERVER_PORT = ${_SERVER_PORT_TEXT}]\033[0m"
      ;;
    1)
      echo -e "\033[31m${SERVER_FILE_NAME} 启动失败，请查看 ${CONSOLE_FILE}\033[0m"
      ;;
    *)
      # 非 root 看不到 ss 的 PID 列，进程活着就算成功，端口只是显示不出来
      echo -e "\033[32m${SERVER_FILE_NAME} 启动成功 [ SERVER_PID = ${_SERVER_PID}, \033[0m\033[33mSERVER_PORT = unknown（非 root 无法读取端口，或非 Spring 项目）\033[0m\033[32m]\033[0m"
      ;;
    esac
  fi
  echo "---------- 启动 [end] ----------"
}
#
# =======================================
# 停止
# =======================================
stop() {
  echo "---------- 停止 [start] ----------"
  # 解析服务的 pid
  resolve_server_pid
  if [ "${_SERVER_PID}" -eq 0 ]; then
    # _SERVER_PID = 0, 那就说明应用程序没有在运行
    echo "${SERVER_FILE_NAME} 已停止"
    echo "---------- 停止 [end] ----------"
    return
  fi
  echo "停止 ${SERVER_FILE_NAME} 中..."
  # 进行优雅停机
  kill -15 "${_SERVER_PID}"
  # 当前等待秒数
  _wait_seconds=0
  #
  while true; do
    if ! is_our_pid "${_SERVER_PID}"; then
      echo "${SERVER_FILE_NAME} 已停止"
      rm -f "${_LOG_PATH}/server.pid"
      break
    fi
    if [ "${_wait_seconds}" -ge "${STOP_TIMEOUT}" ]; then
      # 强制终止进程
      echo -e "\033[31m等待超时(${_wait_seconds}s), 强制关机 [kill -9 ${_SERVER_PID}].\033[0m"
      kill -9 "${_SERVER_PID}" 2>/dev/null || echo -e "\033[31m强制关机失败，请手动处理 [kill -9 ${_SERVER_PID}]\033[0m"
      rm -f "${_LOG_PATH}/server.pid"
      break
    fi
    sleep 1
    ((_wait_seconds++))
    echo "请稍等 ${_wait_seconds}s"
  done
  echo "---------- 停止  [end] ----------"
  sleep 1
}
#
# =======================================
# 状态
# =======================================
status() {
  echo "---------- 状态 [start] ----------"
  # 解析服务的 pid
  resolve_server_pid
  if [ "${_SERVER_PID}" -eq 0 ]; then
    echo -e "\033[33m${SERVER_FILE_NAME} 已停止\033[0m"
  else
    _SERVER_PORT_TEXT=$(list_server_ports | awk 'BEGIN{ORS=" "}NF{print}')
    [ -z "$_SERVER_PORT_TEXT" ] && _SERVER_PORT_TEXT="unknown（非 root 无法读取端口）"
    echo -e "\033[32m${SERVER_FILE_NAME} 运行中 [ SERVER_PID = ${_SERVER_PID}, SERVER_PORT = ${_SERVER_PORT_TEXT}]\033[0m"
    echo -e "你可以查看日志文件 \033[36m${CONSOLE_FILE}\033[0m 或者执行下面的命令"
    echo -e "\033[40;37mtail -f -n 300 ${CONSOLE_FILE}\033[0m"
  fi
  echo "---------- 状态 [end] ----------"
}
#
# =======================================
# 日志
# =======================================
logs() {
  if [ ! -f "${CONSOLE_FILE}" ]; then
    echo -e "\033[33m还没有日志文件 ${CONSOLE_FILE}，服务是否启动过？\033[0m"
    exit 1
  fi
  tail -f -n 200 "${CONSOLE_FILE}"
}
#
# ================================================ 方法 end   ================================================
#
case "$1" in
'start')
  start
  ;;
'stop')
  stop
  ;;
'restart')
  stop
  start
  ;;
'status')
  status
  ;;
'logs')
  logs
  ;;
*)
  echo "必须参数缺失 [start|stop|restart|status|logs] [c]?"
  exit 1
  ;;
esac
