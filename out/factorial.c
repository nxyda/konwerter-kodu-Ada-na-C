// Source: examples/factorial.adb
#include <stdio.h>
#include <stdbool.h>
#include <stdlib.h>

void Factorial() {
    int n = 5;
    int fact = 1;
    for (int i = 1; i <= n; i++) {
        fact = fact * i;
    }
}

int main() {
    Factorial();
    return 0;
}
