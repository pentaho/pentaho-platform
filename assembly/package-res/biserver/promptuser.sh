#!/bin/sh
echo --------------------------------------------------------------------------------------------
echo The Pentaho BI Platform now contains a version checker that will notify you
echo when newer versions of the software are available. The version checker is enabled by default.
echo For information on what the version checker does, why it is beneficial, and how it works see:
echo http://wiki.pentaho.com/display/ServerDoc2x/Version+Checker
echo Press Enter to continue, or type "cancel" or Ctrl-C to prevent the server from starting.
echo You will only be prompted once with this question.
echo --------------------------------------------------------------------------------------------

read -p"[OK]:" choice
choicelower=`echo $choice| tr A-Z a-z`
if [ "$choice" = "cancel" ]; then
  exit 1
else
  echo -e "#!/bin/sh\nexit 0" > promptuser.sh
  exit 0
fi
