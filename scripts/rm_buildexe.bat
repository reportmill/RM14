
echo "Creating RMStudio14.exe"
pushd Z:\Temp\RM14
"C:\Program Files\Java\jdk1.8.0_20\bin\javapackager" -deploy -native exe ^
-outdir "C:\Users\Jeff\RMApp" -outfile RMStudio14 -name RMStudio14 ^
-appclass com.reportmill.AppLoader -v -srcdir "Z:\Temp\RM14\bin" ^
-srcfiles AppLoader.jar;RMStudio14.jar;spell.jar;BuildInfo.txt

echo "Signing RMStudio14.exe"
Z:\Temp\Signtool\signtool sign /f Z:\Temp\Signtool\RMVeriCert.pfx /p rmverisign ^
/t http://timestamp.verisign.com/scripts/timstamp.dll C:\Users\Jeff\RMApp\bundles\RMStudio14.exe

echo "Verify Signing RMStudio14.exe"
Z:\Temp\Signtool\signtool verify /v /pa C:\Users\Jeff\RMApp\bundles\RMStudio14.exe
