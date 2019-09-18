# alphabetter
A demonstration of memoization and algorithmic efficiency techniques, to greatly reduce the computational time of an otherwise large input sanitisation workload.

## what is this?
This program takes in a stated set of characters to use as a sampling alphabet, such as```abcdefghijklmnopqrstuvwxyz```as the first line of input, and on following lines, we state any number of "rules", such as the common english rule of "I before E, except after C"; in the format of ```ei c```, in which we are stating first the "bad" string ```ei```, and then the exceptions under which it will be allowed, ```c```
