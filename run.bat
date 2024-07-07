@echo off

set "app_name=Framework-java"
set "jar_name=framework-java"
set "root=E:\S4\Web_dynamique\Framework\%app_name%"

set "sourceFolder=%root%\src\java"
set "destinationFolder=%root%\bin"
set "lib=%root%\lib"
set "src=%root%"

set "lib_test=E:\S4\Web_dynamique\Test3\web\WEB-INF\lib" 

for /r "%sourceFolder%" %%f in (*.java) do (
    xcopy "%%f" "%root%\temp"
)

cd "%root%\temp"
javac -d "%destinationFolder%" -cp "%lib%\*" *.java

cd "%destinationFolder%"
jar -cvfm "%lib_test%\%jar_name%.jar" "%src%\manifest.txt" *
cd "%src%"

pause