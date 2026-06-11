procedure Fibonacci is
    n    : Integer := 6;
    a    : Integer := 0;
    b    : Integer := 1;
    temp : Integer := 0;
begin
    for i in 1 .. n loop
        Put_Line(a);
        temp := a + b;
        a := b;
        b := temp;
    end loop;
end Fibonacci;
