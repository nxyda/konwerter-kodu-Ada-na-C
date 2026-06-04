procedure Fibonacci is
begin
    n := 6;

    a := 0;
    b := 1;
    temp := 0;

    for i in 1 .. n loop
        temp := a + b;
        a := b;
        b := temp;
    end loop;
end Fibonacci;