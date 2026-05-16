// Source: examples/student_database.adb
#include <stdio.h>
#include <stdbool.h>

void Student_Database() {
    int num_students = 3;
    double total_gpa = 0.0;
    for (int i = 1; i <= num_students; i++) {
        total_gpa = total_gpa + 3.5;
    }
    double avg_gpa = total_gpa / num_students;
}

