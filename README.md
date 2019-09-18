# alphabetter
A demonstration of memoization and algorithmic efficiency techniques, to greatly reduce the computational time of an otherwise large input sanitisation workload.

## what is this?
This program takes in a stated set of characters to use as a sampling alphabet, such as ```abcdefghijklmnopqrstuvwxyz``` as the first line of input, and on following lines, we state any number of "rules", such as the common english rule of "I before E, except after C"; in the format of ```ei c```, in which we are stating first the "bad" string ```ei```, and then the exceptions under which it will be allowed, ```c```. One may include multiple exceptions, seperated by spaces. Each new line in will represent another "bad" string to reject, followed by any further exceptions.

Leaving a blank line after the entry of these, will then change the mode of the program to verifying; One can either enter a word, such as ```receive``` and the program will return whether or not this word is considered valid in the defined "language". Alternatively, one can put in a number such as ```10```, and the program will calculate the total number of words of that length or less that are considered valid. This function is the true power of the program, as this is an exceptionally large task for sufficiently complex languages or long values of string length.

## cool features
One can run the program with arguments such as ```-p``` for debugging printouts, ```-s``` for "slow-mode", in which the program will not use any efficiency techniques; Serving as a basis for comparison to brute force time; and finally ```-t```, which will display a visual tree of the searching of strings during computation. In this mode, strings that are being checked will be denoted with a ```|``` at the end. A word that is detected to be invalied is denoted with a ```#```. The end of a search branch is denoted with a ```/```. A ```*``` character denotes that the program has recognised a familiar branch, and will effectively skip over the branch and add the pre-computed value to the total word count.

## how do i run this?
Simply compile the java file, and run it from your favourite terminal application, with ```java IceIceBaby [arguments]```.
Try this out for a demonstration:
```java IceIceBaby -t
qwertyuiopasdfghjklzxcvbnm
ei c
abc def
qwe rty
zxc bnm
g
o m n

receive
recieve
theif
thief
1
2
5
10
20
```
