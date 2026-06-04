procedure Factorial is
begin
    n := 5;
    fact := 1;

    for i in 1 .. n loop
        fact := fact * i;
    end loop;
end Factorial;