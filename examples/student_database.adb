procedure Student_Database is
begin
    num_students := 3;
    total_gpa := 0.0;

    for i in 1 .. num_students loop
        total_gpa := total_gpa + 3.5;
    end loop;

    avg_gpa := total_gpa / num_students;
end Student_Database;
