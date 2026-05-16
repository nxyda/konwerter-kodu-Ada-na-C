procedure Complex_Procedure is
begin
    max_iterations := 10;
    counter := 0;
    score := 0.0;

    while counter < max_iterations loop
        if counter = 0 then
            score := score + 1.5;
        else
            score := score - 0.5;
        end if;

        counter := counter + 1;
    end loop;
end Complex_Procedure;
