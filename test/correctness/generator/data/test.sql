--------------------------------------------------------------------------------
-- This file contains the SQL used to manually prepare the test database.
--------------------------------------------------------------------------------
-- Connect to a DB preloaded with Jargo data model and test road network
connect 'jdbc:derby:db;createFrom=temp/jargo';
autocommit off;
-- Clear
delete from cq;
delete from cpd;
delete from pd;
delete from cw;
delete from w;
delete from r;
delete from s;
delete from uq;
delete from ue;
delete from ul;
delete from uo;
delete from ud;
delete from ub;

-- Insert Request 10
insert into uq values (10, 1);
insert into ue values (10, 0);
insert into ul values (10, 500);
insert into uo values (10, 4);
insert into ud values (10, 30);
insert into ub values (10, 172);

--                   rid rq re   rl ro  rd   rb
insert into r values (10, 1, 0, 500, 4, 30, 172);

-- Insert Request 11
insert into uq values (11, 1);
insert into ue values (11, 5);
insert into ul values (11, 500);
insert into uo values (11, 1);
insert into ud values (11, 32);
insert into ub values (11, 194);

--                   rid rq re   rl ro  rd   rb
insert into r values (11, 1, 5, 500, 1, 32, 194);

-- Insert Server 1
insert into uq values (1, -10);
insert into ue values (1, 1);
insert into ul values (1, 500);
insert into uo values (1, 22);
insert into ud values (1, 0);
insert into ub values (1, 0);

--                   sid  sq se   sl  so sd sb
insert into s values (1, -10, 1, 500, 22, 0, 0);

-- Assign Request 10 to Server 1
--                   sid  se    t1    v1  t2  v2    dd    nu
insert into w values (1,   1, null, null,  1, 22, null, null);
insert into w values (1,   1,    1,   22,  9,  1,   71,   10);
insert into w values (1,   1,    9,    1, 17,  2,   71,   10);
insert into w values (1,   1,   17,    2, 25,  4,   71,   10);
insert into w values (1,   1,   25,    4, 32, 31,   70,   10);
insert into w values (1,   1,   32,   31, 38, 39,   52,   10);
insert into w values (1,   1,   38,   39, 44, 30,   52,   10);
insert into w values (1,   1,   44,   30, 45,  0,    0,   10);

--                    sid  se   sl  so sd ts  vs  te ve
insert into cw values (1,   1, 500, 22, 0, 1, 22, 45, 0);

--                   sid  t2  v2  rid
insert into pd values (1, 25,  4, 10);
insert into pd values (1, 44, 30, 10);

--                    sid ts  te  tp vp  td  vd  rid  re   rl ro  rd
insert into cpd values (1, 1, 45, 25, 4, 44, 30,  10,  0, 500, 4, 30);

--                    sid  sq se    t1  t2  v2    q1   q2   rid    rq    tp    td    o1 o2
insert into cq values (1, -10, 1, null,  1, 22, null, -10, null, null, null, null, null, 1);
insert into cq values (1, -10, 1,    1, 25,  4,  -10,  -9,   10,    1,   25,   44,    1, 2);
insert into cq values (1, -10, 1,   25, 44, 30,   -9, -10,   10,    1,   25,   44,    2, 3);

-- Insert Server 2
insert into uq values (2, -10);
insert into ue values (2, 10);
insert into ul values (2, 500);
insert into uo values (2, 45);
insert into ud values (2, 5);
insert into ub values (2, 166);

--                   sid  sq  se   sl  so sd   sb
insert into s values (2, -10, 10, 500, 45, 5, 166);

--                   sid  se    t1    v1  t2  v2    dd    nu
insert into w values (2,  10, null, null, 10, 45, null, null);
insert into w values (2,  10,   10,   45, 19, 40,   83,   10);
insert into w values (2,  10,   19,   40, 28,  5,   83,   10);

--                    sid  se   sl  so sd  ts  vs  te ve
insert into cw values (2,  10, 500, 45, 5, 10, 45, 28, 5);

--                    sid  sq  se    t1  t2  v2    q1   q2   rid    rq    tp    td    o1 o2
insert into cq values (2, -10, 10, null, 10, 45, null, -10, null, null, null, null, null, 1);
-- Commit and Quit
commit;
disconnect;
exit;
