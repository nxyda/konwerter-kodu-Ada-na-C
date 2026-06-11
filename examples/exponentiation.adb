procedure Exponentiation is
    base     : Integer := 2;
    exponent : Integer := 5;
    result   : Integer := 1;
begin
    for i in 1 .. exponent loop
        result := result * base;
    end loop;
end Exponentiation;
