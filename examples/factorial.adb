procedure Factorial is
    n    : Integer := 5;
    fact : Integer := 1;
begin
    for i in 1 .. n loop
        fact := fact * i;
    end loop;
end Factorial;
