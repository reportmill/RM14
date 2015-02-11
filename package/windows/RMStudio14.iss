;This file will be executed next to the application bundle image
;I.e. current directory will contain folder RMStudio14 with application files
[Setup]
AppId={{RMStudio14}}
AppName=RMStudio14
AppVersion=1.0
AppVerName=RMStudio14 1.0
AppPublisher=RMStudio14
AppComments=
AppCopyright=
;AppPublisherURL=http://reportmill.com/
;AppSupportURL=http://reportmill.com/
;AppUpdatesURL=http://reportmill.com/
DefaultDirName={localappdata}\RMStudio14
DisableStartupPrompt=Yes
DisableDirPage=Auto
DisableProgramGroupPage=Yes
DisableReadyPage=Yes
DisableFinishedPage=Yes
DisableWelcomePage=Yes
DefaultGroupName=ReportMill
;Optional License
LicenseFile=
;WinXP or above
MinVersion=0,5.1 
OutputBaseFilename=RMStudio14
Compression=lzma
SolidCompression=yes
PrivilegesRequired=lowest
SetupIconFile=RMStudio14\RMStudio14.ico
UninstallDisplayIcon={app}\RMStudio14.ico
UninstallDisplayName=RMStudio14
WizardImageStretch=No
WizardSmallImageFile=RMStudio14-setup-icon.bmp   

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Files]
Source: "RMStudio14\RMStudio14.exe"; DestDir: "{app}"; Flags: ignoreversion
Source: "RMStudio14\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs

[Icons]
Name: "{userstartmenu}\RMStudio14"; Filename: "{app}\RMStudio14.exe"; IconFilename: "{app}\RMStudio14.ico"; Check: returnTrue()
Name: "{commondesktop}\RMStudio14"; Filename: "{app}\RMStudio14.exe"; IconFilename: "{app}\RMStudio14.ico"; Check: returnTrue()

[Run]
Filename: "{app}\RMStudio14.exe"; Description: "{cm:LaunchProgram,RMStudio14}"; Flags: nowait postinstall skipifsilent

[Code]
function returnTrue(): Boolean;
begin
  Result := True;
end;

function returnFalse(): Boolean;
begin
  Result := False;
end;

function InitializeSetup(): Boolean;
begin
// Possible future improvements:
//   if version less or same => just launch app
//   if upgrade => check if same app is running and wait for it to exit
//   Add pack200/unpack200 support? 
  Result := True;
end;  
