create procedure CR.cr3user.period_end_year (in txt varchar) {
	declare start_year integer;
	declare end_year integer;
	declare period integer;
	declare pidx integer;
	declare yidx integer;
	declare ylength integer;
	
	pidx := strcasestr(txt, '/P');
	yidx := strcasestr(txt, 'Y');
	if (pidx > 0 and yidx > 0) {
		ylength := yidx - pidx - 2;
		start_year := cast(substring(txt, 1 , 4) as integer);
		period := cast(substring(txt, pidx + 3, ylength) as integer);
		end_year := start_year + period;
	}
	return end_year;
};
GRANT EXECUTE ON CR.cr3user.period_end_year TO cr3user;