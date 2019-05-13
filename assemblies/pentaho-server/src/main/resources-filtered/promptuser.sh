#!/bin/sh

# *******************************************************************************************
# This program is free software; you can redistribute it and/or modify it under the
# terms of the GNU General Public License, version 2 as published by the Free Software
# Foundation.
#
# You should have received a copy of the GNU General Public License along with this
# program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
# or from the Free Software Foundation, Inc.,
# 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
#
# This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
# without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
# See the GNU General Public License for more details.
#
#
# Copyright 2011 - ${copyright.year} Hitachi Vantara. All rights reserved.
# *******************************************************************************************

echo --------------------------------------------------------------------------------------------
echo The Pentaho BI Platform now contains a version checker that will notify you
echo when newer versions of the software are available. The version checker is enabled by default.
echo For information on what the version checker does, why it is beneficial, and how it works see:
echo http://wiki.pentaho.com/display/ServerDoc2x/Version+Checker
echo Press Enter to continue, or type "cancel" or Ctrl-C to prevent the server from starting.
echo You will only be prompted once with this question.
echo --------------------------------------------------------------------------------------------

echo [OK]:
read choice
choicelower=`echo $choice| tr A-Z a-z`
if [ "$choicelower" = "cancel" ]; then
  exit 1
else
  echo -e "#!/bin/sh\nexit 0" > promptuser.sh
  exit 0
fi
