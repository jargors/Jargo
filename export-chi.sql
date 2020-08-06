CALL SYSCS_UTIL.SYSCS_EXPORT_QUERY
('select sid, t, v.v, cast(v.lng as float)/10000000, cast(v.lat as float)/10000000,
    ls, lr, ro, rd from r_server join v on r_server.v=v.v left join r on rid=lr',
 'r_server.csv',null,null,null);

