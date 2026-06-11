procedure Main is
    a    : Integer := 15;
    b    : Integer := 27;
    sum  : Integer;
    n    : Integer := 5;
    fact : Integer := 1;
    x    : Float   := 10.5;
    y    : Float   := 20.3;
    max  : Float;
begin
    sum := a + b;

    for i in 1 .. n loop
        fact := fact * i;
    end loop;

    if x > y then
        max := x;
    else
        max := y;
    end if;
end Main;
