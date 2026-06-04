procedure Exponentiation is
begin
    base := 2;
    exponent := 5;
    result := 1;

    for i in 1 .. exponent loop
        result := result * base;
    end loop;
end Exponentiation;