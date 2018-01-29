#! /bin/bash
dataPath="$1"
domain="dom4"
filename="$2"
type="$3"
while read -r line
do 
   name=( $line )
   echo ${name[0]} "->" ${name[1]}
   class=${name[0]}
   method=${name[1]}
 java -cp .:./bin/ processing.CountSatUnsatPartial $dataPath $class $method $domain $type
done < $filename
