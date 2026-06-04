// Source: examples/fibonacci.adb
#include <stdio.h>
#include <stdbool.h>
#include <stdlib.h>

void Fibonacci() {
    int n = 6;
    int a = 0;
    int b = 1;
    int temp = 0;
    for (int i = 1; i <= n; i++) {
        temp = a + b;
        a = b;
        b = temp;
    }
}

int main() {
    Fibonacci();
    return 0;
}
