// Source: examples/exponentiation.adb
#include <stdio.h>
#include <stdbool.h>
#include <stdlib.h>

void Exponentiation() {
    int base = 2;
    int exponent = 5;
    int result = 1;
    for (int i = 1; i <= exponent; i++) {
        result = result * base;
    }
}

int main() {
    Exponentiation();
    return 0;
}
