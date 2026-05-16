// Source: examples/complex_procedure.adb
#include <stdio.h>
#include <stdbool.h>

void Complex_Procedure() {
    int max_iterations = 10;
    int counter = 0;
    double score = 0.0;
    while (counter < max_iterations) {
        if (counter == 0) {
            score = score + 1.5;
        } else {
            score = score - 0.5;
        }
        counter = counter + 1;
    }
}

mvn -DincludeScope=runtime dependency:build-classpath -Dmdep.outputFile=target/classpath.txt
CP="$(cat target/classpath.txt):target/classes"