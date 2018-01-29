#! /bin/bash
domain="dom4"
dataPath=$1
filename=$2
type=$3
while read -r line
do
  name=( $line )
  class=${name[0]}
  method=${name[1]}
  #pathfile=/Users/elenasherman/git/DFA_SMT2/ExperimentDataConditional/conditions/paths/${class}_${method}.txt
  pathfile=$dataPath/conditions/paths/${class}_${method}.txt
if [ -f $pathfile ]; then
 id=1; 
 fullPath=${class}_${method}_${domain}
 while read -r path
  do
   singlePath=${class}_${method}_${id}_${domain} 
    echo $singlePath 
   ./z3 -smt2 $dataPath/resultsVA/smt2Files/$type/${singlePath}.txt_VS_${fullPath}.txt > $dataPath/resultsVA/satunsat/$type/${singlePath}_satunsat.txt
  id=$(($id+1))  
  done < $pathfile
else 
  echo "File $pathfile does not exists."
fi
done < $filename
