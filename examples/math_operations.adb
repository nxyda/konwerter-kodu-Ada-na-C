procedure Main is
begin
    a := 15;
    b := 27;
    sum := a + b;

    n := 5;
    fact := 1;
    for i in 1 .. n loop
        fact := fact * i;
    end loop;

    x := 10.5;
    y := 20.3;
    if x > y then
        max := x;
    else
        max := y;
    end if;
end Main;