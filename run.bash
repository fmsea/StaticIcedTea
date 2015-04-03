#!/bin/bash
## Any line that starts with a #PBS is a directive
## Assign a name for your job so that you can track the status of job while running
#PBS -N sootZ3Test
## Specify a queue for your job. There are several different queues with different priorities
#PBS -q batch

## The scheduler uses the “chunk” concept for resources
## Request chunks with select, each with ncpus and ngpus
## If your job uses GPUs, specify ngpus=1. It does not matter how many GPUs there are on the node.
## If your job does NOT use GPUs, specify ngpus=0## Specify number of MPI tasks with mpiprocs
## A CPU job that needs 128 MPI processes would request 4 chunks. Each chunk has 32 CPU threads.
#PBS -l select=1:ncpus=1:ngpus=0:mpiprocs=1

## Request an output file
#PBS -j oe

## Request an error file in case your job does not execute #PBS -j e 

## Load the necessary software environment needed to run your job.

module load pbspro

module load java/gcc/64/1.8.0_31

 

## Jobs should execute on the local scratch disk to improve performance

## Change to the local scratch directory

 

cd $TMPDIR

 

## Set environment variables for your executables and parameters.

## This part is user specific

 

#BINDIR=$HOME/dfa/DFA_SMT/bin
DFA_DIR=$HOME/dfa/DFA_SMT/


EXEC=disjoint.driver.Test

 

## Copy the necessary files and directories to the local scratch $TMPDIR

## pbsdsh utility is needed for this operation

##pbsdsh — cp -rL $BINDIR $TMPDIR

# set dynamic labrary
LD_LIBRARY_PATH=$HOME/tools/z3/build/:$LD_LIBRARY_PATH 

## Execute/run your code


java -cp $HOME/tools/z3/build/com.microsoft.z3.jar:$DFA_DIR:$DFA_DIR/libs/soot-trunk.jar:$DFA_DIR/libs/antlr-4.1-complete.jar:$DFA_DIR/bin/ $EXEC 
#mpirun $your instructions and options

 

## Copy results from local scratch $TMPDIR to your home directory

#pbsdsh -s — cp -r $TMPDIR  $HOME/dfa/DFA_SMT/results
