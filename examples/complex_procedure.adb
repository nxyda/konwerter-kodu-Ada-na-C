procedure Complex_Procedure is
    max_iterations : Integer := 10;
    counter        : Integer := 0;
    score          : Float   := 0.0;
begin
    while counter < max_iterations loop
        if counter = 0 then
            score := score + 1.5;
        else
            score := score - 0.5;
        end if;
        counter := counter + 1;
    end loop;
end Complex_Procedure;
