@echo off
title GvE Classic: Age of Splendor (Game Server)

:start
echo Starting GameServer.
echo.

rem ======== Optimize memory settings =======
rem Minimal size with geodata is 1.5G, w/o geo 1G
rem Make sure -Xmn value is always 1/4 the size of -Xms and -Xmx.
rem -Xms and -Xmx should always be equal.
rem ==========================================
REM одинаковый размер памяти для Xms и Xmx, JVM пытается удержать размер heap'а минимальным, и если его нужно меньше, чем в Xmx - гоняет GC понапрасну
SET java_opts=-Xms10G
SET java_opts=%java_opts% -Xmx10G

REM Альтернативные настройки самой JVM.
REM Циклы заполнения/копирования массивов заменяются на прямые машинные инструкции для ускорения работы.
SET java_opts=%java_opts% -XX:+OptimizeFill
REM Опция, устраняет лишние блокировки путем их объединения.
SET java_opts=%java_opts% -XX:+EliminateLocks
REM Позволяет расширить диапазон кешируемых значений для целых типов при старте виртуальной машины.
SET java_opts=%java_opts% -XX:AutoBoxCacheMax=65536
SET java_opts=%java_opts% -XX:+UseCompressedOops
REM Включает опции которые выше + использует альтернативную библиотеку...
SET java_opts=%java_opts% -XX:+AggressiveOpts
REM Enables the use of large page memory.
SET java_opts=%java_opts% -XX:+UseLargePages

REM -----------------------------------------------------------
REM ###################################################################################
REM Логирование, использовать только для отладки.
md .\gc_log\
REM SET java_opts=%java_opts% -verbose:gc
REM SET java_opts=%java_opts% -XX:+PrintHeapAtGC
REM SET java_opts=%java_opts% -XX:+PrintGCDetails
REM SET java_opts=%java_opts% -XX:+PrintGCDateStamps
REM SET java_opts=%java_opts% -XX:+PrintGCApplicationStoppedTime
REM SET java_opts=%java_opts% -XX:+PrintGC
REM SET java_opts=%java_opts% -Xloggc:.\gc_log\garbage_collector%DATE%-%ctime%.log
REM SET java_opts=%java_opts% -XX:+PrintGCTimeStamps
REM SET java_opts=%java_opts% -XX:+PrintTenuringDistribution

SET java_settings=-Dfile.encoding=UTF-8
SET java_settings=%java_settings% -Djava.net.preferIPv4Stack=true

java -server %java_settings% %java_opts% -cp config;./lib/* l2s.gameserver.GameServer

REM Debug ...
REM java -Dfile.encoding=UTF-8 -cp config;./* -Xmx10G -Xnoclassgc -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=7456 l2s.gameserver.GameServer

if ERRORLEVEL 2 goto restart
if ERRORLEVEL 1 goto error
goto end
:restart
echo.
echo Server restarted ...
echo.
goto start
:error
echo.
echo Server terminated abnormaly ...
echo.
:end
echo.
echo Server terminated ...
echo.

pause
