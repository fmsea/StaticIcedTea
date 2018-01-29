#! /bin/bash
#output="./ConditionalTACAS/conditions/"
output="$1"
filename="$2"
while read -r line
do 
   name=( $line )
   echo ${name[0]} "->" ${name[1]}
   class=${name[0]}
   method=${name[1]}
   perc=${name[2]}
   diff=${name[3]}
 java -cp .:./bin/:./libs/soot-trunk.jar conditional.driver.StartPartitionInner $output $class $method $perc $diff
done < $filename
