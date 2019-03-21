#! /bin/bash

usage() {
    echo "usage: $PROGNAME folder destname" >&2
    exit 1
}

PROGNAME=$(basename $0)

if [ "$#" -ne 2 ]
then
    usage
fi

FOLDER="$1"
DESTPATH="$2"
if [ -d $FOLDER ]
then
cd $FOLDER
echo creating zip $DESTPATH/webroot.zip with *
rm $DESTPATH/webroot.zip
zip -9D $DESTPATH/webroot.zip *
for i in *
do
if [ -d $i ] 
then
ilow=`echo $i | tr [A-Z] [a-z]`
echo creating zip $DESTPATH/$ilow.zip with $i/*
rm $DESTPATH/$ilow.zip
cd $i
zip -9Dr $DESTPATH/$ilow.zip *
cd -
fi
done
fi
