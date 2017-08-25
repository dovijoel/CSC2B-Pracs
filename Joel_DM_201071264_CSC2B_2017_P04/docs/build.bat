@ECHO OFF
cls
cd ..
echo *** Setting PATH ***
set path=%path%;"C:\Program Files (x86)\Java\jdk1.8.0_92\bin"
echo *** Setting variables ***
set PRAC_SRC=.\src
set PRAC_BIN=.\bin
set PRAC_DOCS=.\docs
set PRAC_LIBS=.\libs

echo *** Cleanup ***
del /Q %PRAC_BIN%\*.*
del /Q %PRAC_DOCS%\JavaDoc\*.*
echo *** Compiling ***
javac -classpath %PRAC_LIBS%/jmathio.jar;%PRAC_LIBS%/jmathplot.jar;%PRAC_SRC% -d %PRAC_BIN% %PRAC_SRC%\*.java
echo *** Compiling Javadoc ***
javadoc -classpath %PRAC_LIBS%/jmathio.jar;%PRAC_LIBS%/jmathplot.jar;%PRAC_SRC% -subpackages acsse -d %PRAC_DOCS%\JavaDoc %PRAC_SRC%\*.java
echo *** Running ***
start cmd /k java -classpath %PRAC_BIN% Client
java -classpath %PRAC_LIBS%/jmathio.jar;%PRAC_LIBS%/jmathplot.jar;%PRAC_BIN% Server
PAUSE