function MaxValue return Integer is
    a      : Integer := 15;
    b      : Integer := 42;
    result : Integer;
begin
    if a > b then
        result := a;
    else
        result := b;
    end if;
    return result;
end MaxValue;
