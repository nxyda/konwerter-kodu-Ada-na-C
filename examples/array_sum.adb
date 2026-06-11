procedure ArraySum is
    data : array (1..5) of Integer;
    sum  : Integer := 0;
begin
    data(1) := 10;
    data(2) := 20;
    data(3) := 30;
    data(4) := 40;
    data(5) := 50;

    for i in 1 .. 5 loop
        sum := sum + data(i);
    end loop;
end ArraySum;
