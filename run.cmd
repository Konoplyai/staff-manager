@echo off
cd /d "%~dp0"
if not exist target\lib call mvn -q compile
java -cp "target\classes;target\lib\*" ua.edu.sumdu.j2se.staff.Launcher
