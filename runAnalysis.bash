#! /bin/bash
export DYLD_LIBRARY_PATH=~/Documents/z3Java/z3/build/:.
echo $DYLD_LIBRARY_PATH
domain="dom5"
filename="$1"
writeToFile="y"
while read -r line
do 
   name=( $line )
   echo ${name[0]} "->" ${name[1]}
   class=${name[0]}
   method=${name[1]}
   pathfile=./ScratchData/conditions/paths/${class}_${method}_L.txt
   echo ${pathfile}
  if [ -f $pathfile ]; then
     while read -r path
     do
       echo $class $method $path
       java -cp .:./bin/:./libs/soot-trunk.jar:/Users/elenasherman/Documents/z3Java/z3/build/com.microsoft.z3.jar:./libs/antlr-4.1-complete.jar  driver.StartAnalysisScript $class $method $domain $path $writeToFile
     done < $pathfile
  else 
     echo "File $pathfile does not exists."
  fi
 # run regular analysis with all paths - idex 0 means all paths
 echo $class $method "full"
 java -cp .:./bin/:./libs/soot-trunk.jar:/Users/elenasherman/Documents/z3Java/z3/build/com.microsoft.z3.jar:./libs/antlr-4.1-complete.jar  driver.StartAnalysisScript $class $method $domain 0 $writeToFile
done < $filename
