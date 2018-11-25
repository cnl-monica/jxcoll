#!/bin/bash

path="/usr/lib/jxcoll/bmdbScripts/"
while :
do
echo -n "Enter the administrative postgres user name (default postgres): "
read pgadmin
#echo -n "Enter administrative postgres user password (You can change it with - ALTER USER postgres WITH ENCRYPTED PASSWORD 'new_password' - command): "
#read pgpass
echo -n "Enter database server ip address/host: "
read pghost


#echo "$pghost:5432:postgres:$pgadmin:$pgpass" > ~/.pgpass
#chmod 0600 ~/.pgpass

#RolesAndDB
psql -h $pghost -U $pgadmin -f $path/RolesAndDB.sql
if [ $? -eq 0 ] 
then
 #rm ~/.pgpass
 break;
else 
echo "Type mismatch or wrong input data, please try again!"
echo "Connection error, check pg_hba.conf for correct configuration (line matching: host bm,bmdwh bm,bmro collector_ip,analyzer_ip md5)"
fi
done
echo "Roles and databases creation finished successfully."

#echo "$pghost:5432:bmdb:bm:bm" > ~/.pgpass
#chmod 0600 ~/.pgpass

#SchemPrivTab
while :
do
psql -h $pghost -U bm -d bmdb -f $path/SchemPrivTab.sql
if [ $? -eq 0 ] 
then
 #rm ~/.pgpass
 break;
else 
echo "Connection error, check pg_hba.conf for correct configuration (line matching: host bm,bmdwh bm,bmro collector_ip,analyzer_ip md5)"
echo "Press any key, when the pg_hba.conf file is fixed..."
read
fi
done
echo "Schema, privileges and tables creation finished successfully."

#echo "$pghost:5432:bmdb:$pgadmin:$pgpass" > ~/.pgpass
#chmod 0600 ~/.pgpass

#dbLink
while :
do
psql -h $pghost -U $pgadmin -d bmdb -f $path/dbLink.sql
if [ $? -eq 0 ] 
then
 #rm ~/.pgpass
 break;
else 
echo "Connection error, check pg_hba.conf for correct configuration (line matching: host bm,bmdwh bm,bmro collector_ip,analyzer_ip md5)"
echo "Press any key, when the pg_hba.conf file is fixed..."
read
fi
done
echo "Database link functions creation finished successfully."

#echo "$pghost:5432:bmdb:bm:bm" > ~/.pgpass
#chmod 0600 ~/.pgpass

#dbFunctTrig
while :
do
psql -h $pghost -U bm -d bmdb -f $path/dbFunctTrig.sql
if [ $? -eq 0 ] 
then
 #rm ~/.pgpass
 break;
else 
echo "Connection error, check pg_hba.conf for correct configuration (line matching: host bm,bmdwh bm,bmro collector_ip,analyzer_ip md5)"
echo "Press any key, when the pg_hba.conf file is fixed..."
read
fi
done
echo "Database Functions and Triggres creation finished successfully."

#echo "$pghost:5432:bmdwh:bm:bm" > ~/.pgpass
#chmod 0600 ~/.pgpass

#dwh
while :
do
psql -h $pghost -U bm -d bmdwh -f $path/dwh.sql
if [ $? -eq 0 ] 
then
 #rm ~/.pgpass
 break;
else 
echo "Connection error, check pg_hba.conf for correct configuration (line matching: host bm,bmdwh bm,bmro collector_ip,analyzer_ip md5)"
echo "Press any key, when the pg_hba.conf file is fixed..."
read
fi
done

echo "Data Warehouse tables creation finished successfully."
exit 0

