#!/bin/bash

# Author: Tomas Verescak
# Created: 7.12.2011
# Modified: 8.12.2011

JAR_FILENAME=jxcoll.jar
CONFIG_FILENAME=jxcoll_config.xml
IPFIX_FILENAME=ipfixFields.xml
CONTROL_FILE=DEBIAN/control
MD5SUMS_FILE=DEBIAN/md5sums

# changelog file
CHANGELOG_FILE_DECOMPRESSED=debian/usr/share/doc/jxcoll/changelog
CHANGELOG_FILE=${CONFIG_MANFILE_DECOMPRESSED}".gz"

# config file manpage
CONFIG_MANFILE_DECOMPRESSED=debian/usr/share/man/man5/jxcoll_config.5
CONFIG_MANFILE=$CONFIG_MANFILE_DECOMPRESSED".gz"

# config file manpage
CONFIG_MANFILE_DECOMPRESSED=debian/usr/share/man/man5/jxcoll_config.5
CONFIG_MANFILE=$CONFIG_MANFILE_DECOMPRESSED".gz"
# jxcoll manpage
JXCOLL_MANFILE_DECOMPRESSED=debian/usr/share/man/man1/jxcoll.1
JXCOLL_MANFILE=$JXCOLL_MANFILE_DECOMPRESSED".gz"

#cesta cieloveho JAR-ka v DEB baliku
DST_BIN_FILE=../bin/$JAR_FILENAME

#cesta zdrojoveho JAR-ka v projekte
SRC_JAR_FILE=../dist/$JAR_FILENAME
#cesta cieloveho JAR-ka v DEB baliku
DST_JAR_FILE=debian/usr/lib/jxcoll/$JAR_FILENAME

#cesta zdrojoveho konfiguraku v projekte
SRC_CONFIG_FILE=../$CONFIG_FILENAME
#cesta cieloveho konfiguraku v DEB baliku
DST_CONFIG_FILE=debian/etc/jxcoll/$CONFIG_FILENAME

#cesta zdrojoveho IPFIX suboru v projekte
SRC_IPFIX_FILE=../$IPFIX_FILENAME
#cesta cieloveho IPFIX suboru v DEB baliku
DST_IPFIX_FILE=debian/etc/jxcoll/$IPFIX_FILENAME


########################  TU ZACINA PROGRAM ##########################

if [ ! -d debian ]; then
    echo "Directory debian/ is not present, unpacking debian.tar.gz ..."
    tar xfz debian.tar.gz     
else
    echo "Directory debian/ already exists, just updating its data..."
fi


# skopiruj aktualne JAR-ko do /bin priecinka
echo -n "\nCopying file $SRC_JAR_FILE to $DST_BIN_FILE"
cp $SRC_JAR_FILE $DST_BIN_FILE && echo "\t\t\t\t[DONE]"

#skopiruj aktualne JAR-ko, konfigurak a IPFIX subor do noveho balicka
echo -n "Copying file $SRC_JAR_FILE to $DST_JAR_FILE"
cp $SRC_JAR_FILE $DST_JAR_FILE && echo "\t\t[DONE]"

#zabezpecime, aby bolo jarko spustitelne
chmod 755 $DST_JAR_FILE

echo -n "Copying file $SRC_CONFIG_FILE to $DST_CONFIG_FILE"
cp $SRC_CONFIG_FILE $DST_CONFIG_FILE && echo "\t[DONE]"

echo -n "Copying file $SRC_IPFIX_FILE to $DST_IPFIX_FILE"
cp $SRC_IPFIX_FILE $DST_IPFIX_FILE && echo "\t\t[DONE]"

gunzip $CONFIG_MANFILE
gzip --best $CONFIG_MANFILE_DECOMPRESSED

gunzip $JXCOLL_MANFILE
gzip --best $JXCOLL_MANFILE_DECOMPRESSED

gunzip debian/usr/share/doc/jxcoll/changelog.gz
gzip --best debian/usr/share/doc/jxcoll/changelog

gunzip debian/usr/share/doc/jxcoll/changelog.Debian.gz
gzip --best debian/usr/share/doc/jxcoll/changelog.Debian

# ------------------ All changes to files made ----------------------

# vymaz vsetky docasne subory
echo -n "Removing any temporary files in DEB package"
rm -R -f debian/*~ && echo "\t\t\t\t\t[DONE]"

# vojdi do root path v adresarovej strukture DEB balicka
cd debian

# spocitaj MD5 pre vsetky subory a zapis ich do suboru DEBIAN/md5sums
echo -n "Generating MD5 checksums and saving them in $MD5SUMS_FILE"
find . -type f -exec md5sum {} \; | grep -v DEBIAN > $MD5SUMS_FILE && echo "\t\t\t[DONE]\n"

# navrat do adresara jxcoll/deb/
cd ..

#echo -n "Building DEB package"
fakeroot dpkg --build debian/

echo "\nVerifying DEB package... \n"
lintian debian.deb

echo "\nCreating backup of current debian directory tree in debian.tar.gz file..."
echo "If everything is OK, you can remove directory debian/ \n"
tar cfz debian.tar.gz debian/





