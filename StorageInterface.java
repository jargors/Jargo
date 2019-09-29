  package com.github.jargors;
  import java.sql.CallableStatement;   import java.sql.Connection;
  import java.sql.DriverManager;       import java.sql.PreparedStatement;
  import java.sql.ResultSet;           import java.sql.SQLException;
  import java.sql.Statement;           import java.sql.Types;
  import java.util.Scanner;
  import java.io.File;
  import java.io.FileNotFoundException;
  import java.util.Map;
  import java.util.HashMap;
  import java.time.LocalDateTime;
  public class StorageInterface {
    private Connection conn;
    private ResultSet res;
    private Map<Integer, PreparedStatement> pstmt = new HashMap<>();
    private Map<Integer, String> pstr = new HashMap<>();
    private Map<Integer, int[]> lu_nodes = new HashMap<>();
    private final double CSHIFT = 10000000.0;
    private int uid = 0;
    private final String VERSION = "1.0.0";
    public StorageInterface(String f_rnet) {
      Print("Initializing Storage Interface "+VERSION);
      Print("Initializing a new database...");
      try {
        conn = DriverManager.getConnection("jdbc:derby:memory:jargo;create=true");
        Print("Connected to in-memory database");
        conn.setAutoCommit(false);
        try {
          CallableStatement cs = conn.prepareCall(
            "CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY(?, ?)");
          cs.setString(1, "derby.storage.pageCacheSize");
          cs.setInt(2, 8000);  // default=1000
          cs.execute();
          cs.close();
        }
        catch (SQLException e1) {
          printSQLException(e1);
          try {
            conn.rollback();
          } catch (SQLException e2) {
            printSQLException(e2);
          }
          DBSaveBackup("db-lastgood");
          throw new RuntimeException("database failure");
        }
        Statement stmt = conn.createStatement();
        Print("Creating tables and views...");
        stmt.clearBatch();
        stmt.addBatch("CREATE TABLE V ("
                        + "v   int  CONSTRAINT P1 PRIMARY KEY,"
                        + "lng int  CONSTRAINT C1 NOT NULL,"
                        + "lat int  CONSTRAINT C2 NOT NULL,"
                        + "CONSTRAINT C3 CHECK (lng BETWEEN -1800000000 AND 1800000000),"
                        + "CONSTRAINT C4 CHECK (lat BETWEEN  -900000000 AND  900000000)"
                        + ")");
        stmt.addBatch("CREATE TABLE E ("
                        + "v1  int  CONSTRAINT C5 NOT NULL,"
                        + "v2  int  CONSTRAINT C6 NOT NULL,"
                        + "dd  int  CONSTRAINT C7 NOT NULL,"
                        + "nu  int  CONSTRAINT C8 NOT NULL,"
                        + "CONSTRAINT F1 FOREIGN KEY (v1) REFERENCES V (v),"
                        + "CONSTRAINT F2 FOREIGN KEY (v2) REFERENCES V (v),"
                        + "CONSTRAINT P2 PRIMARY KEY (v1, v2, dd, nu),"
                        + "CONSTRAINT C9 CHECK (nu >= 0),"
                        + "CONSTRAINT C10 CHECK (v1 <> v2),"
                        + "CONSTRAINT C11 CHECK ("
                        + "  CASE WHEN v1 = 0 OR v2 = 0"
                        + "    THEN dd = 0"
                        + "    ELSE dd > 0"
                        + "  END"
                        + ")"
                        + ")");
        stmt.addBatch("CREATE TABLE UQ ("
                        + "uid int  CONSTRAINT C12 NOT NULL,"
                        + "uq  int  CONSTRAINT C13 NOT NULL,"
                        + "CONSTRAINT C14 UNIQUE (uid),"
                        + "CONSTRAINT C15 CHECK (uq != 0),"
                        + "CONSTRAINT P3 PRIMARY KEY (uid, uq)"
                        + ")");
        stmt.addBatch("CREATE TABLE UE ("
                        + "uid int  CONSTRAINT C16 NOT NULL,"
                        + "ue  int  CONSTRAINT C17 NOT NULL,"
                        + "CONSTRAINT C18 CHECK (ue BETWEEN 0 AND 86400000),"
                        + "CONSTRAINT C19 UNIQUE (uid),"
                        + "CONSTRAINT P4 PRIMARY KEY (uid, ue)"
                        + ")");
        stmt.addBatch("CREATE TABLE UL ("
                        + "uid int  CONSTRAINT C20 NOT NULL,"
                        + "ul  int  CONSTRAINT C21 NOT NULL,"
                        + "CONSTRAINT C22 UNIQUE (uid),"
                        + "CONSTRAINT C23 CHECK (ul BETWEEN 0 AND 86400000),"
                        + "CONSTRAINT P5 PRIMARY KEY (uid, ul)"
                        + ")");
        stmt.addBatch("CREATE TABLE UO ("
                        + "uid int  CONSTRAINT C24 NOT NULL,"
                        + "uo  int  CONSTRAINT C25 NOT NULL,"
                        + "CONSTRAINT F3 FOREIGN KEY (uo) REFERENCES V (v),"
                        + "CONSTRAINT C26 UNIQUE (uid),"
                        + "CONSTRAINT P6 PRIMARY KEY (uid, uo)"
                        + ")");
        stmt.addBatch("CREATE TABLE UD ("
                        + "uid int  CONSTRAINT C27 NOT NULL,"
                        + "ud  int  CONSTRAINT C28 NOT NULL,"
                        + "CONSTRAINT F4 FOREIGN KEY (ud) REFERENCES V (v),"
                        + "CONSTRAINT C29 UNIQUE (uid),"
                        + "CONSTRAINT P7 PRIMARY KEY (uid, ud)"
                        + ")");
        stmt.addBatch("CREATE TABLE UB ("
                        + "uid int  CONSTRAINT C30 NOT NULL,"
                        + "ub  int  CONSTRAINT C31 NOT NULL,"
                        + "CONSTRAINT C32 CHECK (ub >= 0),"
                        + "CONSTRAINT C33 UNIQUE (uid),"
                        + "CONSTRAINT P8 PRIMARY KEY (uid, ub)"
                        + ")");
        stmt.addBatch("CREATE TABLE S ("
                        + "sid int  CONSTRAINT P9 PRIMARY KEY,"
                        + "sq  int  CONSTRAINT C34 NOT NULL,"
                        + "se  int  CONSTRAINT C35 NOT NULL,"
                        + "sl  int  CONSTRAINT C36 NOT NULL,"
                        + "so  int  CONSTRAINT C37 NOT NULL,"
                        + "sd  int  CONSTRAINT C38 NOT NULL,"
                        + "sb  int  CONSTRAINT C39 NOT NULL,"
                        + "CONSTRAINT C40 CHECK (sq < 0),"
                        + "CONSTRAINT F5 FOREIGN KEY (sid, sq) REFERENCES UQ (uid, uq),"
                        + "CONSTRAINT F6 FOREIGN KEY (sid, se) REFERENCES UE (uid, ue),"
                        + "CONSTRAINT F7 FOREIGN KEY (sid, sl) REFERENCES UL (uid, ul),"
                        + "CONSTRAINT F8 FOREIGN KEY (sid, so) REFERENCES UO (uid, uo),"
                        + "CONSTRAINT F9 FOREIGN KEY (sid, sd) REFERENCES UD (uid, ud),"
                        + "CONSTRAINT F10 FOREIGN KEY (sid, sb) REFERENCES UB (uid, ub),"
                        + "CONSTRAINT C41 CHECK (se < sl)"
                        + ")");
        stmt.addBatch("CREATE TABLE R ("
                        + "rid int  CONSTRAINT P10 PRIMARY KEY,"
                        + "rq  int  CONSTRAINT C42 NOT NULL,"
                        + "re  int  CONSTRAINT C43 NOT NULL,"
                        + "rl  int  CONSTRAINT C44 NOT NULL,"
                        + "ro  int  CONSTRAINT C45 NOT NULL,"
                        + "rd  int  CONSTRAINT C46 NOT NULL,"
                        + "rb  int  CONSTRAINT C47 NOT NULL,"
                        + "CONSTRAINT C48 CHECK (rq > 0),"
                        + "CONSTRAINT F11 FOREIGN KEY (rid, rq) REFERENCES UQ (uid, uq),"
                        + "CONSTRAINT F12 FOREIGN KEY (rid, re) REFERENCES UE (uid, ue),"
                        + "CONSTRAINT F13 FOREIGN KEY (rid, rl) REFERENCES UL (uid, ul),"
                        + "CONSTRAINT F14 FOREIGN KEY (rid, ro) REFERENCES UO (uid, uo),"
                        + "CONSTRAINT F15 FOREIGN KEY (rid, rd) REFERENCES UD (uid, ud),"
                        + "CONSTRAINT F16 FOREIGN KEY (rid, rb) REFERENCES UB (uid, ub),"
                        + "CONSTRAINT C49 CHECK (re < rl)"
                        + ")");
        stmt.addBatch("CREATE TABLE W ("
                        + "sid int  CONSTRAINT C50 NOT NULL,"
                        + "se  int  CONSTRAINT C51 NOT NULL,"
                        + "t1  int  ,"
                        + "v1  int  ,"
                        + "t2  int  CONSTRAINT C52 NOT NULL,"
                        + "v2  int  CONSTRAINT C53 NOT NULL,"
                        + "dd  int ,"
                        + "nu  int ,"
                        + "CONSTRAINT P11 PRIMARY KEY (sid, t2, v2),"
                        + "CONSTRAINT F17 FOREIGN KEY (sid) REFERENCES S,"
                        + "CONSTRAINT F18 FOREIGN KEY (sid, se) REFERENCES UE (uid, ue),"
                        + "CONSTRAINT F19 FOREIGN KEY (v1, v2, dd, nu) REFERENCES E INITIALLY DEFERRED,"
                        + "CONSTRAINT F20 FOREIGN KEY (sid, t1, v1) REFERENCES W (sid, t2, v2) INITIALLY DEFERRED,"
                        + "CONSTRAINT C54 UNIQUE (sid, t1),"
                        + "CONSTRAINT C55 UNIQUE (sid, t2),"
                        + "CONSTRAINT C56 CHECK ("
                        + "  CASE WHEN t1 IS NULL"
                        + "    THEN t2 = se AND v1 IS NULL AND dd IS NULL AND nu IS NULL"
                        + "    ELSE dd/(t2-t1) <= nu AND t1 < t2"
                        + "  END"
                        + ") INITIALLY DEFERRED"
                        + ")");
        stmt.addBatch("CREATE TABLE PD ("
                        + "sid int  CONSTRAINT C57 NOT NULL,"
                        + "t2  int  CONSTRAINT C58 NOT NULL,"
                        + "v2  int  CONSTRAINT C59 NOT NULL,"
                        + "rid int  CONSTRAINT C60 NOT NULL,"
                        + "CONSTRAINT P12 PRIMARY KEY (sid, t2, v2, rid),"
                        + "CONSTRAINT F21 FOREIGN KEY (sid) REFERENCES S,"
                        + "CONSTRAINT F22 FOREIGN KEY (rid) REFERENCES R,"
                        + "CONSTRAINT F23 FOREIGN KEY (sid, t2, v2) REFERENCES W INITIALLY DEFERRED"
                        + ")");
        stmt.addBatch("CREATE TABLE CW ("
                        + "sid int  CONSTRAINT C61 NOT NULL,"
                        + "se  int  CONSTRAINT C62 NOT NULL,"
                        + "sl  int  CONSTRAINT C63 NOT NULL,"
                        + "so  int  CONSTRAINT C64 NOT NULL,"
                        + "sd  int  CONSTRAINT C65 NOT NULL,"
                        + "ts  int  CONSTRAINT C66 NOT NULL,"
                        + "vs  int  CONSTRAINT C67 NOT NULL,"
                        + "te  int  CONSTRAINT C68 NOT NULL,"
                        + "ve  int  CONSTRAINT C69 NOT NULL,"
                        + "CONSTRAINT C70 UNIQUE (sid),"
                        + "CONSTRAINT P13 PRIMARY KEY (sid, ts, te),"
                        + "CONSTRAINT F24 FOREIGN KEY (sid) REFERENCES S,"
                        + "CONSTRAINT F25 FOREIGN KEY (sid, se) REFERENCES UE (uid, ue),"
                        + "CONSTRAINT F26 FOREIGN KEY (sid, sl) REFERENCES UL (uid, ul),"
                        + "CONSTRAINT F27 FOREIGN KEY (sid, so) REFERENCES UO (uid, uo),"
                        + "CONSTRAINT F28 FOREIGN KEY (sid, sd) REFERENCES UD (uid, ud),"
                        + "CONSTRAINT F29 FOREIGN KEY (sid, ts, vs) REFERENCES W (sid, t2, v2) INITIALLY DEFERRED,"
                        + "CONSTRAINT F30 FOREIGN KEY (sid, te, ve) REFERENCES W (sid, t2, v2) INITIALLY DEFERRED,"
                        + "CONSTRAINT C71 CHECK (ts = se AND vs = so),"
                        + "CONSTRAINT C72 CHECK (te <= sl AND ve = sd),"
                        + "CONSTRAINT C73 CHECK (ts < te)"
                        + ")");
        stmt.addBatch("CREATE TABLE CPD ("
                        + "sid int  CONSTRAINT C74 NOT NULL,"
                        + "ts  int  CONSTRAINT C75 NOT NULL,"
                        + "te  int  CONSTRAINT C76 NOT NULL,"
                        + "tp  int  CONSTRAINT C77 NOT NULL,"
                        + "vp  int  CONSTRAINT C78 NOT NULL,"
                        + "td  int  CONSTRAINT C79 NOT NULL,"
                        + "vd  int  CONSTRAINT C80 NOT NULL,"
                        + "rid int  CONSTRAINT C81 NOT NULL,"
                        + "re  int  CONSTRAINT C82 NOT NULL,"
                        + "rl  int  CONSTRAINT C83 NOT NULL,"
                        + "ro  int  CONSTRAINT C84 NOT NULL,"
                        + "rd  int  CONSTRAINT C85 NOT NULL,"
                        + "CONSTRAINT C86 UNIQUE (rid),"
                        + "CONSTRAINT P14 PRIMARY KEY (sid, tp, td, rid),"
                        + "CONSTRAINT F31 FOREIGN KEY (sid) REFERENCES S,"
                        + "CONSTRAINT F32 FOREIGN KEY (rid) REFERENCES R,"
                        + "CONSTRAINT F33 FOREIGN KEY (sid, ts, te) REFERENCES CW (sid, ts, te) "
                        + "  INITIALLY DEFERRED,"
                        + "CONSTRAINT F34 FOREIGN KEY (sid, tp, vp, rid) REFERENCES PD (sid, t2, v2, rid) "
                        + "  INITIALLY DEFERRED,"
                        + "CONSTRAINT F35 FOREIGN KEY (sid, td, vd, rid) REFERENCES PD (sid, t2, v2, rid) "
                        + "  INITIALLY DEFERRED,"
                        + "CONSTRAINT F36 FOREIGN KEY (rid, re) REFERENCES UE (uid, ue),"
                        + "CONSTRAINT F37 FOREIGN KEY (rid, rl) REFERENCES UL (uid, ul),"
                        + "CONSTRAINT F38 FOREIGN KEY (rid, ro) REFERENCES UO (uid, uo),"
                        + "CONSTRAINT F39 FOREIGN KEY (rid, rd) REFERENCES UD (uid, ud),"
                        + "CONSTRAINT C87 CHECK (tp BETWEEN ts AND td) INITIALLY DEFERRED,"
                        + "CONSTRAINT C88 CHECK (td BETWEEN tp AND te) INITIALLY DEFERRED,"
                        + "CONSTRAINT C89 CHECK (tp >= re AND vp = ro) INITIALLY DEFERRED,"
                        + "CONSTRAINT C90 CHECK (td <= rl AND vd = rd) INITIALLY DEFERRED"
                        + ")");
        stmt.addBatch("CREATE TABLE CQ ("
                        + "sid int  CONSTRAINT C91 NOT NULL,"
                        + "sq  int  CONSTRAINT C92 NOT NULL,"
                        + "se  int  CONSTRAINT C93 NOT NULL,"
                        + "t1  int  ,"
                        + "t2  int  CONSTRAINT C94 NOT NULL,"
                        + "v2  int  ,"
                        + "q1  int  ,"
                        + "q2  int  CONSTRAINT C95 NOT NULL,"
                        + "rid int  ,"
                        + "rq  int  ,"
                        + "tp  int  ,"
                        + "td  int  ,"
                        + "o1  int  ,"
                        + "o2  int  CONSTRAINT C96 NOT NULL,"
                        + "CONSTRAINT C97 CHECK (o2 > 0),"
                        + "CONSTRAINT P15 PRIMARY KEY (sid, t2, q2, o2),"
                        + "CONSTRAINT F40 FOREIGN KEY (sid) REFERENCES S,"
                        + "CONSTRAINT F41 FOREIGN KEY (rid) REFERENCES R,"
                        + "CONSTRAINT F42 FOREIGN KEY (sid, t1, q1, o1) REFERENCES CQ (sid, t2, q2, o2)"
                        + "  INITIALLY DEFERRED,"
                        + "CONSTRAINT F43 FOREIGN KEY (sid, se) REFERENCES UE (uid, ue),"
                        + "CONSTRAINT F44 FOREIGN KEY (sid, sq) REFERENCES UQ (uid, uq),"
                        + "CONSTRAINT F45 FOREIGN KEY (rid, rq) REFERENCES UQ (uid, uq),"
                        + "CONSTRAINT F46 FOREIGN KEY (sid, t2, v2, rid) REFERENCES PD INITIALLY DEFERRED,"
                        + "CONSTRAINT F47 FOREIGN KEY (sid, tp, td, rid) REFERENCES CPD INITIALLY DEFERRED,"
                        + "CONSTRAINT C98 CHECK ("
                        + "  CASE WHEN t1 IS NULL"
                        + "    THEN t2 = se AND q1 IS NULL AND q2 = sq AND o1 IS NULL AND o2 = 1"
                        + "      AND rid IS NULL AND rq IS NULL AND tp IS NULL AND td IS NULL"
                        + "    ELSE q2 <= 0 AND o2 = o1 + 1 AND"
                        + "      CASE WHEN t2 = tp"
                        + "        THEN q2 = q1 + rq"
                        + "        ELSE q2 = q1 - rq"
                        + "      END"
                        + "  END"
                        + ") INITIALLY DEFERRED"
                        + ")");
        stmt.addBatch("CREATE VIEW r_user (uid, uq, ue, ul, uo, ud, ub) AS "
                        + "SELECT * from S UNION SELECT * from R");
        stmt.addBatch("CREATE VIEW r_server (sid, t, v, Ls, Lr) AS "
                        + "SELECT W.sid, W.t2, W.v2, CW.sid, PD.rid "
                        + "FROM W LEFT OUTER JOIN CW ON W.sid = CW.sid AND (W.t2 = CW.ts OR W.t2 = CW.te) "
                        + "  LEFT OUTER JOIN PD ON W.sid = PD.sid AND W.t2 = PD.t2");
        stmt.addBatch("CREATE VIEW f_distance_blocks (sid, val, dtype) AS "
                        + "SELECT d.sid, SUM (d.dd), d.dtype FROM ("
                        + "  SELECT c.sid, c.dd, c.q2=c.sq as dtype FROM ("
                        + "    SELECT b.sid, b.dd, b.q2, b.sq FROM ("
                        + "      SELECT W.sid, W.t2, COALESCE (W.dd, 0) as dd, CQ.q2, CQ.sq, CQ.o2 "
                        + "      FROM W LEFT OUTER JOIN CQ ON W.sid = CQ.sid AND W.t2 > CQ.t2"
                        + "    ) AS b JOIN ("
                        + "      SELECT W.sid, W.t2, MAX (CQ.o2) AS oprev "
                        + "      FROM W LEFT OUTER JOIN CQ ON W.sid = CQ.sid AND W.t2 > CQ.t2 "
                        + "      GROUP BY W.sid, W.t2"
                        + "    ) AS a "
                        + "    ON b.sid = a.sid AND b.t2 = a.t2 AND b.o2 = a.oprev"
                        + "  ) AS c"
                        + ") AS d "
                        + "GROUP BY d.sid, d.dtype");
        stmt.addBatch("CREATE VIEW f_status (t, sid, rid, val) AS "
                        + "SELECT a.t2, a.sid, a.rid, COUNT (b.rid) "
                        + "FROM CQ AS a INNER JOIN CQ AS b ON a.t2 >= b.t2 "
                        + "WHERE a.rid IS NOT NULL AND b.rid IS NOT NULL AND a.rid = b.rid "
                        + "GROUP BY a.t2, a.sid, a.rid");
        stmt.addBatch("CREATE VIEW assignments (t, sid, rid) AS "
                        + "SELECT t, sid, rid FROM f_status WHERE val = 2 ORDER BY t ASC");
        stmt.addBatch("CREATE VIEW assignments_r (t, rid) AS "
                        + "SELECT t, rid FROM assignments");
        stmt.addBatch("CREATE VIEW service_rate (val) AS "
                        + "SELECT CAST(CAST(A.NUM AS FLOAT) / CAST(A.DENOM AS FLOAT) * 100 as INT)"
                        + "FROM ( "
                        + "SELECT (SELECT COUNT(*) FROM assignments_r) AS NUM, "
                        + "       (SELECT COUNT(*) FROM R) AS DENOM "
                        + "       FROM assignments_r FETCH FIRST ROW ONLY "
                        + ") A");
        stmt.addBatch("CREATE VIEW dist_base (val) AS "
                        + "SELECT SUM (ub) FROM UB");
        stmt.addBatch("CREATE VIEW dist_s_travel (sid, val) AS "
                        + "SELECT W.sid, SUM (COALESCE (dd, 0)) "
                        + "FROM W JOIN CW ON w.sid = cw.sid AND (t2 BETWEEN ts AND te) "
                        + "GROUP BY W.sid");
        stmt.addBatch("CREATE VIEW dist_s_cruising (sid, val) AS "
                        + "SELECT sid, val FROM f_distance_blocks WHERE dtype = true");
        stmt.addBatch("CREATE VIEW dist_s_service (sid, val) AS "
                        + "SELECT sid, val FROM f_distance_blocks WHERE dtype = false");
        stmt.addBatch("CREATE VIEW dist_s_base (val) AS "
                        + "SELECT SUM (sb) FROM S");
        stmt.addBatch("CREATE VIEW dist_r_base (val) AS "
                        + "SELECT SUM (rb) FROM R");
        stmt.addBatch("CREATE VIEW dist_r_transit (rid, val) AS "
                        + "SELECT rid, SUM (COALESCE (dd, 0)) "
                        + "FROM CPD JOIN W ON CPD.sid = W.sid AND CPD.tp < W.t2 AND W.t2 <= CPD.td "
                        + "GROUP BY rid");
        stmt.addBatch("CREATE VIEW dist_r_detour (rid, val) AS "
                        + "SELECT rid, val-ub FROM UB JOIN dist_r_transit ON uid = rid");
        stmt.addBatch("CREATE VIEW dur_s_travel (sid, val) AS "
                        + "SELECT sid, te - ts FROM CW");
        stmt.addBatch("CREATE VIEW dur_r_pickup (rid, val) AS "
                        + "SELECT rid, tp - re FROM CPD");
        stmt.addBatch("CREATE VIEW dur_r_transit (rid, val) AS "
                        + "SELECT rid, td - tp FROM CPD");
        stmt.addBatch("CREATE VIEW dur_r_travel (rid, val) AS "
                        + "SELECT rid, td - re FROM CPD");
        stmt.addBatch("CREATE VIEW t_r_depart (rid, val) AS "
                        + "SELECT rid, tp FROM CPD");
        stmt.addBatch("CREATE VIEW t_s_depart (sid, val) AS "
                        + "SELECT sid, ts FROM CW");
        stmt.addBatch("CREATE VIEW t_r_arrive (rid, val) AS "
                        + "SELECT rid, td FROM CPD");
        stmt.addBatch("CREATE VIEW t_s_arrive (sid, val) AS "
                        + "SELECT sid, te FROM CW");
        try {
          stmt.executeBatch();
        }
        catch (SQLException e1) {
          printSQLException(e1);
          try {
            conn.rollback();
          } catch (SQLException e2) {
            printSQLException(e2);
          }
          DBSaveBackup("db-lastgood");
          throw new RuntimeException("database failure");
        }
        Print("Finished loading model");
      }
      catch (SQLException e1) {
        printSQLException(e1);
        try {
          conn.rollback();
        } catch (SQLException e2) {
          printSQLException(e2);
        }
        DBSaveBackup("db-lastgood");
        throw new RuntimeException("database failure");
      }
      PSInit();
      Print("Loading road network ("+f_rnet+")");
      try {
        PSClear(0, 1);
        int[] col = new int[7];
        int dist;
        Scanner sc = new Scanner(new File(f_rnet));
        while (sc.hasNext()) {
          col[0] = sc.nextInt();
          col[1] = sc.nextInt();
          col[2] = sc.nextInt();
          col[3] = (int) Math.round(sc.nextDouble()*CSHIFT);
          col[4] = (int) Math.round(sc.nextDouble()*CSHIFT);
          col[5] = (int) Math.round(sc.nextDouble()*CSHIFT);
          col[6] = (int) Math.round(sc.nextDouble()*CSHIFT);
          if (col[1] == 0) {
            col[3] = 0;
            col[4] = 0;
          }
          if (col[2] == 0) {
            col[5] = 0;
            col[6] = 0;
          }
          if (!lu_nodes.containsKey(col[1])) {
            lu_nodes.put(col[1], new int[] { col[3], col[4] });
            PSAdd(0, col[1], col[3], col[4]);
          }
          if (!lu_nodes.containsKey(col[2])) {
            lu_nodes.put(col[2], new int[] { col[3], col[4] });
            PSAdd(0, col[2], col[5], col[6]);
          }
          dist = ((col[1] != 0 && col[2] != 0)
            ? haversine(col[3]/CSHIFT, col[4]/CSHIFT,
                        col[5]/CSHIFT, col[6]/CSHIFT) : 0);
          PSAdd(1, col[1], col[2], dist, 10);
        }
        PSSubmit(0, 1);
        conn.commit();
      } catch (FileNotFoundException e) {
        throw new IllegalArgumentException("bad file");
      }
      catch (SQLException e1) {
        printSQLException(e1);
        try {
          conn.rollback();
        } catch (SQLException e2) {
          printSQLException(e2);
        }
        DBSaveBackup("db-lastgood");
        throw new RuntimeException("database failure");
      }
    }
    public void DBLoadBackup(String filepath) throws RuntimeException {
      Print("Loading database backup ("+filepath+")");
      try {
        if (!conn.isClosed()) {
          conn.close();
        }
        conn = DriverManager.getConnection("jdbc:derby:memory:jargobak;createFrom="+filepath);
        conn.setAutoCommit(false);
        try {
          CallableStatement cs = conn.prepareCall(
            "CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY(?, ?)");
          cs.setString(1, "derby.storage.pageCacheSize");
          cs.setInt(2, 8000);  // default=1000
          cs.execute();
          cs.close();
        }
        catch (SQLException e1) {
          printSQLException(e1);
          try {
            conn.rollback();
          } catch (SQLException e2) {
            printSQLException(e2);
          }
          DBSaveBackup("db-lastgood");
          throw new RuntimeException("database failure");
        }
        PSInit();
        uid = DBFetch(132, 1)[0];
      }
      catch (SQLException e1) {
        printSQLException(e1);
        try {
          conn.rollback();
        } catch (SQLException e2) {
          printSQLException(e2);
        }
        DBSaveBackup("db-lastgood");
        throw new RuntimeException("database failure");
      }
    }
    public void DBSaveBackup(String filepath) throws RuntimeException {
      try {
        CallableStatement cs = conn.prepareCall(
          "CALL SYSCS_UTIL.SYSCS_BACKUP_DATABASE(?)");
        cs.setString(1, filepath);
        cs.execute();
        cs.close();
      }
      catch (SQLException e1) {
        printSQLException(e1);
        try {
          conn.rollback();
        } catch (SQLException e2) {
          printSQLException(e2);
        }
        DBSaveBackup("db-lastgood");
        throw new RuntimeException("database failure");
      }
    }
    public void printSQLException(SQLException e) {
      while (e != null) {
        System.err.println("\n----- SQLException -----");
        System.err.println("  SQL State:  " + e.getSQLState());
        System.err.println("  Error Code: " + e.getErrorCode());
        System.err.println("  Message:    " + e.getMessage());
        e.printStackTrace(System.err);
        e = e.getNextException();
      }
    }
    public int haversine(double lng1, double lat1, double lng2, double lat2) {
      double dlat = Math.toRadians(lat2 - lat1);
      double dlng = Math.toRadians(lng2 - lng1);
      double rlat1 = Math.toRadians(lat1);
      double rlat2 = Math.toRadians(lat2);
      double a = Math.pow(Math.sin(dlat / 2), 2)
        + Math.pow(Math.sin(dlng / 2), 2)
        * Math.cos(rlat1) * Math.cos(rlat2);
      double c = 2 * Math.asin(Math.sqrt(a));
      int d = (int) Math.round(c * 6371000);
      if (d == 0 && (lng1 != lng2 || lat1 != lat2)) {
        d = 1;
      }
      return d;
    }
    public int haversine(int v1, int v2) {
      int[] c1 = lu_nodes.get(v1);
      int[] c2 = lu_nodes.get(v2);
      return haversine(c1[0]/CSHIFT, c1[1]/CSHIFT,
                       c2[0]/CSHIFT, c2[1]/CSHIFT);
    }
    public void printUser(int[] u) {
      System.out.println("User {uid="+u[0]+", q="+u[1]+", e="+u[2]+", l="+u[3]
        +", o="+u[4]+", d="+u[5]+", b="+u[6]+"}");
    }
    public void printPath(int[] path) {
      for (Integer i : path) {
        System.out.print(i+" ");
      }
      System.out.println();
    }
    public void printRoute(int[] route) {
      for (int i = 0; i < (route.length - 1); i += 2) {
        System.out.print("("+route[i]+", "+route[(i + 1)]+") ");
      }
      System.out.println();
    }
    public void printSchedule(int[] sched) {
      for (int i = 0; i < (sched.length - 3); i += 4) {
        System.out.print("("+sched[i]+", "+sched[(i + 1)]
          + ", "+sched[(i + 2)]+", "+sched[(i + 3)]+") ");
      }
      System.out.println();
    }
    public void DBUpdateEdgeSpeed(int v1, int v2, int nu) throws RuntimeException {
      try {
        PSClear(15, 131);
        PSAdd(15, nu, v1, v2);
        PSAdd(131, nu, v1, v2);
        PSSubmit(15, 131);
        conn.commit();
      }
      catch (SQLException e1) {
        printSQLException(e1);
        try {
          conn.rollback();
        } catch (SQLException e2) {
          printSQLException(e2);
        }
        DBSaveBackup("db-lastgood");
        throw new RuntimeException("database failure");
      }
    }
    public int DBAddNewRequest(int[] u) throws RuntimeException {
      try {
        uid += 1;
        PSClear(2, 3, 4, 5, 6, 7);
        PSAdd(2, uid, u[0]);
        PSAdd(3, uid, u[1]);
        PSAdd(4, uid, u[2]);
        PSAdd(5, uid, u[3]);
        PSAdd(6, uid, u[4]);
        PSAdd(7, uid, u[5]);
        PSSubmit(2, 3, 4, 5, 6, 7);
        PSClear(9);
        PSAdd(9, uid, u[0], u[1], u[2], u[3], u[4], u[5]);
        PSSubmit(9);
        conn.commit();
        return uid;
      }
      catch (SQLException e1) {
        printSQLException(e1);
        try {
          conn.rollback();
        } catch (SQLException e2) {
          printSQLException(e2);
        }
        DBSaveBackup("db-lastgood");
        throw new RuntimeException("database failure");
      }
    }
    public int DBAddNewServer(int[] u, int[] route) throws RuntimeException {
      try {
        int[] output = new int[] { };
        int se = u[1];
        uid += 1;
        PSClear(2, 3, 4, 5, 6, 7);
        PSAdd(2, uid, u[0]);
        PSAdd(3, uid, u[1]);
        PSAdd(4, uid, u[2]);
        PSAdd(5, uid, u[3]);
        PSAdd(6, uid, u[4]);
        PSAdd(7, uid, u[5]);
        PSSubmit(2, 3, 4, 5, 6, 7);
        PSClear(8);
        PSAdd(8, uid, u[0], u[1], u[2], u[3], u[4], u[5]);
        PSSubmit(8);
        PSClear(10);
        PSAdd(10, uid, se, null, null, route[0], route[1], null, null);
        PSSubmit(10);
        PSClear(10);
        for (int i = 0; i < (route.length - 3); i += 2) {
          int t1 = route[i];
          int v1 = route[(i + 1)];
          int t2 = route[(i + 2)];
          int v2 = route[(i + 3)];
          output = DBFetch(46, 2, v1, v2);
          int dd = output[0];
          int nu = output[1];
          PSAdd(10, uid, se, t1, v1, t2, v2, dd, nu);
        }
        PSSubmit(10);
        PSClear(11);
        int te = route[(route.length - 2)];
        PSAdd(11, uid, u[1], u[2], u[3], u[4], u[1], u[3], te, u[4]);
        PSSubmit(11);
        PSClear(14);
        PSAdd(14, uid, u[0], u[1], null, u[1], u[3], null, u[0],
            null, null, null, null, null, 1);
        PSSubmit(14);
        conn.commit();
        return uid;
      }
      catch (SQLException e1) {
        printSQLException(e1);
        try {
          conn.rollback();
        } catch (SQLException e2) {
          printSQLException(e2);
        }
        DBSaveBackup("db-lastgood");
        throw new RuntimeException("database failure");
      }
    }
    public void DBUpdateServerRoute(int sid, int[] route, int[] sched)
    throws RuntimeException {
      int[] output = new int[] { };
      int se, sq;
      try {
        output = DBFetch(48, 2, sid);
        sq = output[0];
        se = output[1];
        PSClear(76);
        PSAdd(76, sid, route[0]);
        PSSubmit(76);
        PSClear(10);
        for (int i = 0; i < (route.length - 3); i += 2) {
          int t1 = route[i];
          int v1 = route[(i + 1)];
          int t2 = route[(i + 2)];
          int v2 = route[(i + 3)];
          output = DBFetch(46, 2, v1, v2);
          int dd = output[0];
          int nu = output[1];
          PSAdd(10, uid, se, t1, v1, t2, v2, dd, nu);
        }
        PSSubmit(10);
        PSClear(77);
        int te = route[(route.length - 2)];
        int ve = route[(route.length - 1)];
        PSAdd(77, te, ve, sid);
        PSSubmit(77);
        if (sched.length > 0) {
          Map<Integer, int[]> cache = new HashMap<>();
          int bound = (sched.length/3);
          PSClear(82, 83, 84);
          for (int j = 0; j < bound; j++) {
            int tj = sched[(3*j)];
            int vj = sched[(3*j + 1)];
            int Lj = sched[(3*j + 2)];
            if (Lj != sid) {
              PSAdd(82, tj, vj, Lj);
              PSAdd(83, tj, vj, Lj);
              PSAdd(84, tj, vj, Lj);
            }
          }
          PSSubmit(82, 83, 84);
          for (int j = 0; j < bound; j++) {
            int Lj = sched[(3*j + 2)];
            if (Lj != sid) {
              int rq, tp, td;
              if (!cache.containsKey(Lj)) {
                rq = DBFetch(85, 1, Lj)[0];
                output = DBFetch(86, 2, Lj);
                tp = output[0];
                td = output[1];
                cache.put(Lj, new int[] { rq, tp, td });
              }
            }
          }
          PSClear(80);
          PSAdd(80, sid, route[0]);
          PSSubmit(80);
          int t1, q1, o1;
          output = DBFetch(87, 3, sid, sched[0]);
          t1 = output[0];
          q1 = output[1];
          o1 = output[2];
          PSClear(14);
          for (int j = 0; j < bound; j++) {
            int t2 = sched[(3*j)];
            int v2 = sched[(3*j + 1)];
            int Lj = sched[(3*j + 2)];
            if (Lj != sid) {
              int[] qpd = cache.get(Lj);
              int q2 = (t2 == qpd[1] ? q1 + qpd[0] : q1 - qpd[0]);
              int o2 = o1 + 1;
              PSAdd(14, sid, sq, se, t1, t2, v2, q1, q2, Lj,
                    qpd[0], qpd[1], qpd[2], o1, o2);
              t1 = t2;
              q1 = q2;
              o1 = o2;
            }
          }
          PSSubmit(14);
        }
        conn.commit();
      }
      catch (SQLException e1) {
        printSQLException(e1);
        try {
          conn.rollback();
        } catch (SQLException e2) {
          printSQLException(e2);
        }
        DBSaveBackup("db-lastgood");
        throw new RuntimeException("database failure");
      }
    }
    public void DBUpdateServerAddToSchedule(
        int sid, int[] route, int[] sched, int[] rid)
    throws RuntimeException {
      int[] output = new int[] { };
      int se, sq;
      Map<Integer, int[]> cache = new HashMap<>();
      Map<Integer, int[]> cache2 = new HashMap<>();
      try {
        output = DBFetch(48, 2, sid);
        sq = output[0];
        se = output[1];
        PSClear(76);
        PSAdd(76, sid, route[0]);
        PSSubmit(76);
        PSClear(10);
        for (int i = 0; i < (route.length - 3); i += 2) {
          int t1 = route[i];
          int v1 = route[(i + 1)];
          int t2 = route[(i + 2)];
          int v2 = route[(i + 3)];
          output = DBFetch(46, 2, v1, v2);
          int dd = output[0];
          int nu = output[1];
          PSAdd(10, uid, se, t1, v1, t2, v2, dd, nu);
        }
        PSSubmit(10);
        PSClear(77);
        int te = route[(route.length - 2)];
        int ve = route[(route.length - 1)];
        PSAdd(77, te, ve, sid);
        PSSubmit(77);
        int bound = (sched.length/3);
        PSClear(82, 83, 84);
        for (int j = 0; j < bound; j++) {
          int tj = sched[(3*j)];
          int vj = sched[(3*j + 1)];
          int Lj = sched[(3*j + 2)];
          if (Lj != sid) {
            PSAdd(82, tj, vj, Lj);
            PSAdd(83, tj, vj, Lj);
            PSAdd(84, tj, vj, Lj);
          }
        }
        PSSubmit(82, 83, 84);
        for (int j = 0; j < bound; j++) {
          int Lj = sched[(3*j + 2)];
          if (Lj != sid) {
            int rq, tp, vp;
            int td = -1;
            int vd = -1;
            if (!cache.containsKey(Lj)) {
              rq = DBFetch(85, 1, Lj)[0];
              boolean flagged = false;
              for (int r : rid) {
                if (Lj == r) {
                  flagged = true;
                  break;
                }
              }
              if (flagged) {
                tp = sched[(3*j)];
                vp = sched[(3*j + 1)];
                for (int k = (j + 1); k < bound; k++) {
                  if (Lj == sched[(3*k + 2)]) {
                    td = sched[(3*k)];
                    vd = sched[(3*k + 1)];
                  }
                }
                cache2.put(Lj, new int[] { vp, vd });
              } else {
                output = DBFetch(86, 2, Lj);
                tp = output[0];
                td = output[1];
              }
              cache.put(Lj, new int[] { rq, tp, td });
            }
          }
        }
        PSClear(80);
        PSAdd(80, sid, route[0]);
        PSSubmit(80);
        int t1, q1, o1;
        output = DBFetch(87, 3, sid, sched[0]);
        t1 = output[0];
        q1 = output[1];
        o1 = output[2];
        PSClear(14);
        for (int j = 0; j < bound; j++) {
          int t2 = sched[(3*j)];
          int v2 = sched[(3*j + 1)];
          int Lj = sched[(3*j + 2)];
          if (Lj != sid) {
            int[] qpd = cache.get(Lj);
            int q2 = (t2 == qpd[1] ? q1 + qpd[0] : q1 - qpd[0]);
            int o2 = o1 + 1;
            PSAdd(14, sid, sq, se, t1, t2, v2, q1, q2, Lj,
                  qpd[0], qpd[1], qpd[2], o1, o2);
            t1 = t2;
            q1 = q2;
            o1 = o2;
          }
        }
        PSSubmit(14);
        PSClear(12, 13);
        int rq, re, rl, ro, rd;
        int[] qpd, pd;
        for (int r : rid) {
          output = DBFetch(51, 5, r);
          rq = output[0];
          re = output[1];
          rl = output[2];
          ro = output[3];
          rd = output[4];
          qpd = cache.get(r);
          pd = cache2.get(r);
          PSAdd(12, sid, qpd[1], pd[0], r);
          PSAdd(12, sid, qpd[2], pd[1], r);
          PSAdd(13, sid, se, route[(route.length - 2)], qpd[1], pd[0], qpd[2], pd[1],
                r, re, rl, ro, rd);
        }
        PSSubmit(12, 13);
        conn.commit();
      }
      catch (SQLException e1) {
        printSQLException(e1);
        try {
          conn.rollback();
        } catch (SQLException e2) {
          printSQLException(e2);
        }
        DBSaveBackup("db-lastgood");
        throw new RuntimeException("database failure");
      }
    }
    public void DBUpdateServerRemoveFromSchedule(
        int sid, int[] route, int[] sched, int[] rid)
    throws RuntimeException {
      int[] output = new int[] { };
      int se, sq;
      Map<Integer, int[]> cache = new HashMap<>();
      try {
        output = DBFetch(48, 2, sid);
        sq = output[0];
        se = output[1];
        PSClear(76);
        PSAdd(76, sid, route[0]);
        PSSubmit(76);
        PSClear(10);
        for (int i = 0; i < (route.length - 3); i += 2) {
          int t1 = route[i];
          int v1 = route[(i + 1)];
          int t2 = route[(i + 2)];
          int v2 = route[(i + 3)];
          output = DBFetch(46, 2, v1, v2);
          int dd = output[0];
          int nu = output[1];
          PSAdd(10, uid, se, t1, v1, t2, v2, dd, nu);
        }
        PSSubmit(10);
        PSClear(77);
        int te = route[(route.length - 2)];
        int ve = route[(route.length - 1)];
        PSAdd(77, te, ve, sid);
        PSSubmit(77);
        int bound = (sched.length/3);
        PSClear(82, 83, 84);
        for (int j = 0; j < bound; j++) {
          int tj = sched[(3*j)];
          int vj = sched[(3*j + 1)];
          int Lj = sched[(3*j + 2)];
          if (Lj != sid) {
            PSAdd(82, tj, vj, Lj);
            PSAdd(83, tj, vj, Lj);
            PSAdd(84, tj, vj, Lj);
          }
        }
        PSSubmit(82, 83, 84);
        for (int j = 0; j < bound; j++) {
          int Lj = sched[(3*j + 2)];
          if (Lj != sid) {
            int rq, tp, td;
            if (!cache.containsKey(Lj)) {
              rq = DBFetch(85, 1, Lj)[0];
              output = DBFetch(86, 2, Lj);
              tp = output[0];
              td = output[1];
              cache.put(Lj, new int[] { rq, tp, td });
            }
          }
        }
        PSClear(80);
        PSAdd(80, sid, route[0]);
        PSSubmit(80);
        int t1, q1, o1;
        output = DBFetch(87, 3, sid, sched[0]);
        t1 = output[0];
        q1 = output[1];
        o1 = output[2];
        PSClear(14);
        for (int j = 0; j < bound; j++) {
          int t2 = sched[(3*j)];
          int v2 = sched[(3*j + 1)];
          int Lj = sched[(3*j + 2)];
          if (Lj != sid) {
            int[] qpd = cache.get(Lj);
            int q2 = (t2 == qpd[1] ? q1 + qpd[0] : q1 - qpd[0]);
            int o2 = o1 + 1;
            PSAdd(14, sid, sq, se, t1, t2, v2, q1, q2, Lj,
                  qpd[0], qpd[1], qpd[2], o1, o2);
            t1 = t2;
            q1 = q2;
            o1 = o2;
          }
        }
        PSSubmit(14);
        PSClear(42, 43);
        for (int r : rid) {
          PSAdd(42, r);
          PSAdd(43, r);
        }
        PSSubmit(42, 43);
        conn.commit();
      }
      catch (SQLException e1) {
        printSQLException(e1);
        try {
          conn.rollback();
        } catch (SQLException e2) {
          printSQLException(e2);
        }
        DBSaveBackup("db-lastgood");
        throw new RuntimeException("database failure");
      }
    }
    public int[] DBQueryServer(int sid) throws RuntimeException {
      int[] output = new int[] { };
      try {
        output = DBFetch(70, 7, sid);
      }
      catch (SQLException e1) {
        printSQLException(e1);
        try {
          conn.rollback();
        } catch (SQLException e2) {
          printSQLException(e2);
        }
        DBSaveBackup("db-lastgood");
        throw new RuntimeException("database failure");
      }
      return output;
    }
    public int[] DBQueryRequest(int rid) throws RuntimeException {
      int[] output = new int[] { };
      try {
        output = DBFetch(75, 7, rid);
      }
      catch (SQLException e1) {
        printSQLException(e1);
        try {
          conn.rollback();
        } catch (SQLException e2) {
          printSQLException(e2);
        }
        DBSaveBackup("db-lastgood");
        throw new RuntimeException("database failure");
      }
      return output;
    }
    public int[] DBQueryRequestStatus(int rid, int t) throws RuntimeException {
      int[] output = new int[] { };
      try {
        output = DBFetch(133, 1, rid, t);
      }
      catch (SQLException e1) {
        printSQLException(e1);
        try {
          conn.rollback();
        } catch (SQLException e2) {
          printSQLException(e2);
        }
        DBSaveBackup("db-lastgood");
        throw new RuntimeException("database failure");
      }
      return output;
    }
    public int[] DBQueryQueuedRequests(int t) throws RuntimeException {
      int[] output = new int[] { };
      try {
        output = DBFetch(68, 7, t, t);
      }
      catch (SQLException e1) {
        printSQLException(e1);
        try {
          conn.rollback();
        } catch (SQLException e2) {
          printSQLException(e2);
        }
        DBSaveBackup("db-lastgood");
        throw new RuntimeException("database failure");
      }
      return output;
    }
    public int[] DBQueryServerLocationsAll(int t) throws RuntimeException {
      int[] output = new int[] { };
      try {
        output = DBFetch(59, 3, t, t, t, t);
      }
      catch (SQLException e1) {
        printSQLException(e1);
        try {
          conn.rollback();
        } catch (SQLException e2) {
          printSQLException(e2);
        }
        DBSaveBackup("db-lastgood");
        throw new RuntimeException("database failure");
      }
      return output;
    }
    public int[] DBQueryServerLocationsActive(int t) throws RuntimeException {
      int[] output = new int[] { };
      try {
        output = DBFetch(128, 3, t, t, t, t, t);
      }
      catch (SQLException e1) {
        printSQLException(e1);
        try {
          conn.rollback();
        } catch (SQLException e2) {
          printSQLException(e2);
        }
        DBSaveBackup("db-lastgood");
        throw new RuntimeException("database failure");
      }
      return output;
    }
    public int[] DBQueryRoute(int sid) throws RuntimeException {
      int[] output = new int[] { };
      try {
        output = DBFetch(60, 2, sid);
      }
      catch (SQLException e1) {
        printSQLException(e1);
        try {
          conn.rollback();
        } catch (SQLException e2) {
          printSQLException(e2);
        }
        DBSaveBackup("db-lastgood");
        throw new RuntimeException("database failure");
      }
      return output;
    }
    public int[] DBQueryRouteRemaining(int sid, int t) throws RuntimeException {
      int[] output = new int[] { };
      try {
        output = DBFetch(129, 2, sid, t);
      }
      catch (SQLException e1) {
        printSQLException(e1);
        try {
          conn.rollback();
        } catch (SQLException e2) {
          printSQLException(e2);
        }
        DBSaveBackup("db-lastgood");
        throw new RuntimeException("database failure");
      }
      return output;
    }
    public int[] DBQuerySchedule(int sid) throws RuntimeException {
      int[] output = new int[] { };
      try {
        output = DBFetch(61, 4, sid);
      }
      catch (SQLException e1) {
        printSQLException(e1);
        try {
          conn.rollback();
        } catch (SQLException e2) {
          printSQLException(e2);
        }
        DBSaveBackup("db-lastgood");
        throw new RuntimeException("database failure");
      }
      return output;
    }
    public int[] DBQueryScheduleRemaining(int sid, int t) throws RuntimeException {
      int[] output = new int[] { };
      try {
        output = DBFetch(69, 4, sid, t);
      }
      catch (SQLException e1) {
        printSQLException(e1);
        try {
          conn.rollback();
        } catch (SQLException e2) {
          printSQLException(e2);
        }
        DBSaveBackup("db-lastgood");
        throw new RuntimeException("database failure");
      }
      return output;
    }
    public int[] DBQueryCurrentLoad(int sid, int t) throws RuntimeException {
      int[] output = new int[] { };
      try {
        output = DBFetch(73, 1, sid, t);
      }
      catch (SQLException e1) {
        printSQLException(e1);
        try {
          conn.rollback();
        } catch (SQLException e2) {
          printSQLException(e2);
        }
        DBSaveBackup("db-lastgood");
        throw new RuntimeException("database failure");
      }
      return output;
    }
    public int[] DBQueryCountVertices() throws RuntimeException {
      int[] output = new int[] { };
      try {
        output = DBFetch(62, 1);
      }
      catch (SQLException e1) {
        printSQLException(e1);
        try {
          conn.rollback();
        } catch (SQLException e2) {
          printSQLException(e2);
        }
        DBSaveBackup("db-lastgood");
        throw new RuntimeException("database failure");
      }
      return output;
    }
    public int[] DBQueryCountEdges() throws RuntimeException {
      int[] output = new int[] { };
      try {
        output = DBFetch(63, 1);
      }
      catch (SQLException e1) {
        printSQLException(e1);
        try {
          conn.rollback();
        } catch (SQLException e2) {
          printSQLException(e2);
        }
        DBSaveBackup("db-lastgood");
        throw new RuntimeException("database failure");
      }
      return output;
    }
    public int[] DBQueryVertex(int v) throws RuntimeException {
      int[] output = new int[] { };
      try {
        output = DBFetch(130, 2, v);
      }
      catch (SQLException e1) {
        printSQLException(e1);
        try {
          conn.rollback();
        } catch (SQLException e2) {
          printSQLException(e2);
        }
        DBSaveBackup("db-lastgood");
        throw new RuntimeException("database failure");
      }
      return output;
    }
    public int[] DBQueryEdge(int v1, int v2) throws RuntimeException {
      int[] output = new int[] { };
      try {
        output = DBFetch(46, 2, v1, v2);
      }
      catch (SQLException e1) {
        printSQLException(e1);
        try {
          conn.rollback();
        } catch (SQLException e2) {
          printSQLException(e2);
        }
        DBSaveBackup("db-lastgood");
        throw new RuntimeException("database failure");
      }
      return output;
    }
    public int[] DBQueryStatisticsEdges() throws RuntimeException {
      int[] output = new int[] { };
      try {
        output = DBFetch(65, 6);
      }
      catch (SQLException e1) {
        printSQLException(e1);
        try {
          conn.rollback();
        } catch (SQLException e2) {
          printSQLException(e2);
        }
        DBSaveBackup("db-lastgood");
        throw new RuntimeException("database failure");
      }
      return output;
    }
    public int[] DBQueryMBR() throws RuntimeException {
      int[] output = new int[] { };
      try {
        output = DBFetch(64, 4);
      }
      catch (SQLException e1) {
        printSQLException(e1);
        try {
          conn.rollback();
        } catch (SQLException e2) {
          printSQLException(e2);
        }
        DBSaveBackup("db-lastgood");
        throw new RuntimeException("database failure");
      }
      return output;
    }
    public int[] DBQueryCountServers() throws RuntimeException {
      int[] output = new int[] { };
      try {
        output = DBFetch(66, 1);
      }
      catch (SQLException e1) {
        printSQLException(e1);
        try {
          conn.rollback();
        } catch (SQLException e2) {
          printSQLException(e2);
        }
        DBSaveBackup("db-lastgood");
        throw new RuntimeException("database failure");
      }
      return output;
    }
    public int[] DBQueryCountRequests() throws RuntimeException {
      int[] output = new int[] { };
      try {
        output = DBFetch(67, 1);
      }
      catch (SQLException e1) {
        printSQLException(e1);
        try {
          conn.rollback();
        } catch (SQLException e2) {
          printSQLException(e2);
        }
        DBSaveBackup("db-lastgood");
        throw new RuntimeException("database failure");
      }
      return output;
    }
    public int[] DBQueryServerPendingAssignments(int sid, int t)
    throws RuntimeException {
      int[] output = new int[] { };
      try {
        output = DBFetch(100, 1, t, sid);
      }
      catch (SQLException e1) {
        printSQLException(e1);
        try {
          conn.rollback();
        } catch (SQLException e2) {
          printSQLException(e2);
        }
        DBSaveBackup("db-lastgood");
        throw new RuntimeException("database failure");
      }
      return output;
    }
    public int[] DBQueryServerCompletedAssignments(int sid, int t)
    throws RuntimeException {
      int[] output = new int[] { };
      try {
        output = DBFetch(101, 1, t, sid);
      }
      catch (SQLException e1) {
        printSQLException(e1);
        try {
          conn.rollback();
        } catch (SQLException e2) {
          printSQLException(e2);
        }
        DBSaveBackup("db-lastgood");
        throw new RuntimeException("database failure");
      }
      return output;
    }
    public int[] DBQueryServiceRate() throws RuntimeException {
      int[] output = new int[] { };
      try {
        output = DBFetch(102, 1);
      }
      catch (SQLException e1) {
        printSQLException(e1);
        try {
          conn.rollback();
        } catch (SQLException e2) {
          printSQLException(e2);
        }
        DBSaveBackup("db-lastgood");
        throw new RuntimeException("database failure");
      }
      return output;
    }
    public int[] DBQueryBaseDistanceTotal() throws RuntimeException {
      int[] output = new int[] { };
      try {
        output = DBFetch(103, 1);
      }
      catch (SQLException e1) {
        printSQLException(e1);
        try {
          conn.rollback();
        } catch (SQLException e2) {
          printSQLException(e2);
        }
        DBSaveBackup("db-lastgood");
        throw new RuntimeException("database failure");
      }
      return output;
    }
    public int[] DBQueryServerBaseDistanceTotal() throws RuntimeException {
      int[] output = new int[] { };
      try {
        output = DBFetch(110, 1);
      }
      catch (SQLException e1) {
        printSQLException(e1);
        try {
          conn.rollback();
        } catch (SQLException e2) {
          printSQLException(e2);
        }
        DBSaveBackup("db-lastgood");
        throw new RuntimeException("database failure");
      }
      return output;
    }
    public int[] DBQueryRequestBaseDistanceTotal() throws RuntimeException {
      int[] output = new int[] { };
      try {
        output = DBFetch(111, 1);
      }
      catch (SQLException e1) {
        printSQLException(e1);
        try {
          conn.rollback();
        } catch (SQLException e2) {
          printSQLException(e2);
        }
        DBSaveBackup("db-lastgood");
        throw new RuntimeException("database failure");
      }
      return output;
    }
    public int[] DBQueryServerTravelDistance(int sid) throws RuntimeException {
      int[] output = new int[] { };
      try {
        output = DBFetch(104, 1, sid);
      }
      catch (SQLException e1) {
        printSQLException(e1);
        try {
          conn.rollback();
        } catch (SQLException e2) {
          printSQLException(e2);
        }
        DBSaveBackup("db-lastgood");
        throw new RuntimeException("database failure");
      }
      return output;
    }
    public int[] DBQueryServerTravelDistanceTotal() throws RuntimeException {
      int[] output = new int[] { };
      try {
        output = DBFetch(105, 1);
      }
      catch (SQLException e1) {
        printSQLException(e1);
        try {
          conn.rollback();
        } catch (SQLException e2) {
          printSQLException(e2);
        }
        DBSaveBackup("db-lastgood");
        throw new RuntimeException("database failure");
      }
      return output;
    }
    public int[] DBQueryServerCruisingDistance(int sid) throws RuntimeException {
      int[] output = new int[] { };
      try {
        output = DBFetch(106, 1, sid);
      }
      catch (SQLException e1) {
        printSQLException(e1);
        try {
          conn.rollback();
        } catch (SQLException e2) {
          printSQLException(e2);
        }
        DBSaveBackup("db-lastgood");
        throw new RuntimeException("database failure");
      }
      return output;
    }
    public int[] DBQueryServerCruisingDistanceTotal() throws RuntimeException {
      int[] output = new int[] { };
      try {
        output = DBFetch(107, 1);
      }
      catch (SQLException e1) {
        printSQLException(e1);
        try {
          conn.rollback();
        } catch (SQLException e2) {
          printSQLException(e2);
        }
        DBSaveBackup("db-lastgood");
        throw new RuntimeException("database failure");
      }
      return output;
    }
    public int[] DBQueryServerServiceDistance(int sid) throws RuntimeException {
      int[] output = new int[] { };
      try {
        output = DBFetch(108, 1, sid);
      }
      catch (SQLException e1) {
        printSQLException(e1);
        try {
          conn.rollback();
        } catch (SQLException e2) {
          printSQLException(e2);
        }
        DBSaveBackup("db-lastgood");
        throw new RuntimeException("database failure");
      }
      return output;
    }
    public int[] DBQueryServerServiceDistanceTotal() throws RuntimeException {
      int[] output = new int[] { };
      try {
        output = DBFetch(109, 1);
      }
      catch (SQLException e1) {
        printSQLException(e1);
        try {
          conn.rollback();
        } catch (SQLException e2) {
          printSQLException(e2);
        }
        DBSaveBackup("db-lastgood");
        throw new RuntimeException("database failure");
      }
      return output;
    }
    public int[] DBQueryRequestDetourDistance(int rid) throws RuntimeException {
      int[] output = new int[] { };
      try {
        output = DBFetch(112, 1, rid);
      }
      catch (SQLException e1) {
        printSQLException(e1);
        try {
          conn.rollback();
        } catch (SQLException e2) {
          printSQLException(e2);
        }
        DBSaveBackup("db-lastgood");
        throw new RuntimeException("database failure");
      }
      return output;
    }
    public int[] DBQueryRequestDetourDistanceTotal() throws RuntimeException {
      int[] output = new int[] { };
      try {
        output = DBFetch(113, 1);
      }
      catch (SQLException e1) {
        printSQLException(e1);
        try {
          conn.rollback();
        } catch (SQLException e2) {
          printSQLException(e2);
        }
        DBSaveBackup("db-lastgood");
        throw new RuntimeException("database failure");
      }
      return output;
    }
    public int[] DBQueryRequestTransitDistance(int rid) throws RuntimeException {
      int[] output = new int[] { };
      try {
        output = DBFetch(114, 1, rid);
      }
      catch (SQLException e1) {
        printSQLException(e1);
        try {
          conn.rollback();
        } catch (SQLException e2) {
          printSQLException(e2);
        }
        DBSaveBackup("db-lastgood");
        throw new RuntimeException("database failure");
      }
      return output;
    }
    public int[] DBQueryRequestTransitDistanceTotal() throws RuntimeException {
      int[] output = new int[] { };
      try {
        output = DBFetch(115, 1);
      }
      catch (SQLException e1) {
        printSQLException(e1);
        try {
          conn.rollback();
        } catch (SQLException e2) {
          printSQLException(e2);
        }
        DBSaveBackup("db-lastgood");
        throw new RuntimeException("database failure");
      }
      return output;
    }
    public int[] DBQueryServerTravelDuration(int sid) throws RuntimeException {
      int[] output = new int[] { };
      try {
        output = DBFetch(116, 1, sid);
      }
      catch (SQLException e1) {
        printSQLException(e1);
        try {
          conn.rollback();
        } catch (SQLException e2) {
          printSQLException(e2);
        }
        DBSaveBackup("db-lastgood");
        throw new RuntimeException("database failure");
      }
      return output;
    }
    public int[] DBQueryServerTravelDurationTotal() throws RuntimeException {
      int[] output = new int[] { };
      try {
        output = DBFetch(117, 1);
      }
      catch (SQLException e1) {
        printSQLException(e1);
        try {
          conn.rollback();
        } catch (SQLException e2) {
          printSQLException(e2);
        }
        DBSaveBackup("db-lastgood");
        throw new RuntimeException("database failure");
      }
      return output;
    }
    public int[] DBQueryRequestPickupDuration(int rid) throws RuntimeException {
      int[] output = new int[] { };
      try {
        output = DBFetch(118, 1, rid);
      }
      catch (SQLException e1) {
        printSQLException(e1);
        try {
          conn.rollback();
        } catch (SQLException e2) {
          printSQLException(e2);
        }
        DBSaveBackup("db-lastgood");
        throw new RuntimeException("database failure");
      }
      return output;
    }
    public int[] DBQueryRequestPickupDurationTotal() throws RuntimeException {
      int[] output = new int[] { };
      try {
        output = DBFetch(119, 1);
      }
      catch (SQLException e1) {
        printSQLException(e1);
        try {
          conn.rollback();
        } catch (SQLException e2) {
          printSQLException(e2);
        }
        DBSaveBackup("db-lastgood");
        throw new RuntimeException("database failure");
      }
      return output;
    }
    public int[] DBQueryRequestTransitDuration(int rid) throws RuntimeException {
      int[] output = new int[] { };
      try {
        output = DBFetch(120, 1, rid);
      }
      catch (SQLException e1) {
        printSQLException(e1);
        try {
          conn.rollback();
        } catch (SQLException e2) {
          printSQLException(e2);
        }
        DBSaveBackup("db-lastgood");
        throw new RuntimeException("database failure");
      }
      return output;
    }
    public int[] DBQueryRequestTransitDurationTotal() throws RuntimeException {
      int[] output = new int[] { };
      try {
        output = DBFetch(121, 1);
      }
      catch (SQLException e1) {
        printSQLException(e1);
        try {
          conn.rollback();
        } catch (SQLException e2) {
          printSQLException(e2);
        }
        DBSaveBackup("db-lastgood");
        throw new RuntimeException("database failure");
      }
      return output;
    }
    public int[] DBQueryRequestTravelDuration(int rid) throws RuntimeException {
      int[] output = new int[] { };
      try {
        output = DBFetch(122, 1, rid);
      }
      catch (SQLException e1) {
        printSQLException(e1);
        try {
          conn.rollback();
        } catch (SQLException e2) {
          printSQLException(e2);
        }
        DBSaveBackup("db-lastgood");
        throw new RuntimeException("database failure");
      }
      return output;
    }
    public int[] DBQueryRequestTravelDurationTotal() throws RuntimeException {
      int[] output = new int[] { };
      try {
        output = DBFetch(123, 1);
      }
      catch (SQLException e1) {
        printSQLException(e1);
        try {
          conn.rollback();
        } catch (SQLException e2) {
          printSQLException(e2);
        }
        DBSaveBackup("db-lastgood");
        throw new RuntimeException("database failure");
      }
      return output;
    }
    public int[] DBQueryRequestDepartureTime(int rid) throws RuntimeException {
      int[] output = new int[] { };
      try {
        output = DBFetch(124, 1, rid);
      }
      catch (SQLException e1) {
        printSQLException(e1);
        try {
          conn.rollback();
        } catch (SQLException e2) {
          printSQLException(e2);
        }
        DBSaveBackup("db-lastgood");
        throw new RuntimeException("database failure");
      }
      return output;
    }
    public int[] DBQueryServerDepartureTime(int sid) throws RuntimeException {
      int[] output = new int[] { };
      try {
        output = DBFetch(125, 1, sid);
      }
      catch (SQLException e1) {
        printSQLException(e1);
        try {
          conn.rollback();
        } catch (SQLException e2) {
          printSQLException(e2);
        }
        DBSaveBackup("db-lastgood");
        throw new RuntimeException("database failure");
      }
      return output;
    }
    public int[] DBQueryRequestArrivalTime(int rid) throws RuntimeException {
      int[] output = new int[] { };
      try {
        output = DBFetch(126, 1, rid);
      }
      catch (SQLException e1) {
        printSQLException(e1);
        try {
          conn.rollback();
        } catch (SQLException e2) {
          printSQLException(e2);
        }
        DBSaveBackup("db-lastgood");
        throw new RuntimeException("database failure");
      }
      return output;
    }
    public int[] DBQueryServerArrivalTime(int sid) throws RuntimeException {
      int[] output = new int[] { };
      try {
        output = DBFetch(127, 1, sid);
      }
      catch (SQLException e1) {
        printSQLException(e1);
        try {
          conn.rollback();
        } catch (SQLException e2) {
          printSQLException(e2);
        }
        DBSaveBackup("db-lastgood");
        throw new RuntimeException("database failure");
      }
      return output;
    }
    public int[] DBQuery(String sql, int ncols) throws RuntimeException {
      int[] output = new int[] { };
      try {
        Statement stmt = conn.createStatement(
          ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        res = stmt.executeQuery(sql);
        if (res.last()) {
          output = new int[(ncols*res.getRow())];
          res.first();
          do {
            for (int j = 1; j <= ncols; j++) {
              output[((res.getRow() - 1)*ncols + (j - 1))] = res.getInt(j);
            }
          } while (res.next());
        }
      }
      catch (SQLException e1) {
        printSQLException(e1);
        try {
          conn.rollback();
        } catch (SQLException e2) {
          printSQLException(e2);
        }
        DBSaveBackup("db-lastgood");
        throw new RuntimeException("database failure");
      }
      return output;
    }
      private void Print(String msg) {
        System.out.println("[Jargo][StorageInterface]["+LocalDateTime.now()+"] "+msg);
      }
      private PreparedStatement PS(String sql) throws SQLException {
        PreparedStatement ps = null;
        try {
          ps = conn.prepareStatement(sql,
            ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        }
        catch (SQLException e) {
          throw e;
        }
        return ps;
      }
      private int[] DBFetch(int k, int ncols, Integer... values) throws SQLException {
        int[] output = new int[] { };
        pstmt.get(k).clearParameters();
        for (int i = 0; i < values.length; i++) {
          if (values[i] == null) {
            pstmt.get(k).setNull((i + 1), java.sql.Types.INTEGER);
          } else {
            pstmt.get(k).setInt ((i + 1), values[i]);
          }
        }
        try {
          res = pstmt.get(k).executeQuery();
          if (res.last() == true) {
            output = new int[(ncols*res.getRow())];
            res.first();
            do {
              for (int j = 1; j <= ncols; j++) {
                output[((res.getRow() - 1)*ncols + (j - 1))] = res.getInt(j);
              }
            } while (res.next());
          }
        }
        catch (SQLException e) {
          throw e;
        }
        return output;
      }
      private void PSAdd(int k, Integer... values) throws SQLException {
        pstmt.get(k).clearParameters();
        for (int i = 0; i < values.length; i++) {
          if (values[i] == null) {
            pstmt.get(k).setNull((i + 1), java.sql.Types.INTEGER);
          } else {
            pstmt.get(k).setInt ((i + 1), values[i]);
          }
        }
        try {
          pstmt.get(k).addBatch();
        }
        catch (SQLException e) {
          throw e;
        }
      }
      private void PSClear(Integer... values) throws SQLException {
        try {
          for (Integer k : values) {
            pstmt.get(k).clearBatch();
          }
        }
        catch (SQLException e) {
          throw e;
        }
      }
      private void PSSubmit(Integer... values) throws SQLException {
        try {
          for (Integer k : values) {
            pstmt.get(k).executeBatch();
          }
        }
        catch (SQLException e1) {
          try {
            conn.rollback();
          } catch (SQLException e2) {
            throw e2;
          }
          throw e1;
        }
      }
      private void PSInit() throws RuntimeException {
        pstmt = new HashMap<>();
        pstr = new HashMap<>();
        String INS = "INSERT INTO ";
        String UPD = "UPDATE ";
        String DEL = "DELETE FROM ";
        String SEL = "SELECT ";
        String q2  = "(?,?)";
        String q3  = "(?,?,?)";
        String q4  = "(?,?,?,?)";
        String q7  = "(?,?,?,?,?,?,?)";
        String q8  = "(?,?,?,?,?,?,?,?)";
        String q9  = "(?,?,?,?,?,?,?,?,?)";
        String q12 = "(?,?,?,?,?,?,?,?,?,?,?,?)";
        String q14 = "(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        pstr.put( 0, INS+"V VALUES "+q3);
        pstr.put( 1, INS+"E VALUES "+q4);
        pstr.put( 2, INS+"UQ VALUES "+q2);
        pstr.put( 3, INS+"UE VALUES "+q2);
        pstr.put( 4, INS+"UL VALUES "+q2);
        pstr.put( 5, INS+"UO VALUES "+q2);
        pstr.put( 6, INS+"UD VALUES "+q2);
        pstr.put( 7, INS+"UB VALUES "+q2);
        pstr.put( 8, INS+"S VALUES "+q7);
        pstr.put( 9, INS+"R VALUES "+q7);
        pstr.put(10, INS+"W VALUES "+q8);
        pstr.put(11, INS+"CW VALUES "+q9);
        pstr.put(12, INS+"PD VALUES "+q4);
        pstr.put(13, INS+"CPD VALUES "+q12);
        pstr.put(14, INS+"CQ VALUES "+q14);
        pstr.put(15, UPD+"E SET nu=? WHERE v1=? AND v2=?");
        pstr.put(131, UPD+"W SET nu=? WHERE v1=? AND v2=?");
        pstr.put(77, UPD+"CW SET te=?, ve=? WHERE sid=?");
        pstr.put(84, UPD+"PD SET t2=? WHERE v2=? AND rid=?");
        pstr.put(82, UPD+"CPD SET tp=? WHERE vp=? AND rid=?");
        pstr.put(83, UPD+"CPD SET td=? WHERE vd=? AND rid=?");
        pstr.put(76, DEL+"W WHERE sid=? AND t2>?");
        pstr.put(42, DEL+"PD WHERE rid=?");
        pstr.put(43, DEL+"CPD WHERE rid=?");
        pstr.put(80, DEL+"CQ WHERE sid=? AND t2>?");
        pstr.put(132, SEL+"MAX (uid) FROM UQ");
        pstr.put(62, SEL+"COUNT (*) FROM V WHERE v<>0");
        pstr.put(64, SEL+"MIN (lng), MAX (lng), MIN (lat), MAX (lat) "
            + "FROM V WHERE v<>0");
        pstr.put(63, SEL+"COUNT (*) FROM E WHERE v1<>0 AND v2<>0");
        pstr.put(65, SEL+"MIN (dd), MAX (dd), SUM (dd) / COUNT (dd), "
            + "MIN (nu), MAX (nu), SUM (nu) / COUNT (nu) "
            + "FROM E WHERE v1<>0 AND v2<>0");
        pstr.put(46, SEL+"dd, nu FROM E WHERE v1=? AND v2=?");
        pstr.put(130, SEL+"lng, lat FROM V WHERE v=?");
        pstr.put(70, SEL+"sid, sq, se, sl, so, sd, sb FROM S WHERE sid=?");
        pstr.put(48, SEL+"sq, se FROM S WHERE sid=?");
        pstr.put(66, SEL+"COUNT (*) FROM S");
        pstr.put(75, SEL+"rid, rq, re, rl, ro, rd, rb FROM R WHERE rid=?");
        pstr.put(51, SEL+"rq, re, rl, ro, rd FROM R WHERE rid=?");
        pstr.put(67, SEL+"COUNT (*) FROM R");
        pstr.put(59, SEL+"a.sid, a.t2, a.v2 FROM W AS a INNER JOIN ("
            + "SELECT sid, MIN(ABS(t2-?)) as tdiff FROM W WHERE t2<=? AND v2<>0 "
            + "GROUP BY sid"
            + ") as b ON a.sid=b.sid AND ABS(a.t2-?)=b.tdiff AND a.t2<=?");
        pstr.put(128, SEL+"a.sid, a.t2, a.v2 FROM W AS a INNER JOIN ("
            + "SELECT sid FROM CW WHERE te>?"
            + ") as b ON a.sid=b.sid INNER JOIN ("
            + "SELECT sid, MIN(ABS(t2-?)) as tdiff FROM W WHERE t2<=? AND v2<>0 "
            + "GROUP BY sid"
            + ") as c ON a.sid=c.sid AND ABS(a.t2-?)=c.tdiff AND a.t2<=?");
        pstr.put(60, SEL+"t, v FROM r_server WHERE sid=? ORDER BY t ASC");
        pstr.put(129, SEL+"t, v FROM r_server WHERE sid=? AND t>? ORDER BY t ASC");
        pstr.put(61, SEL+"t, v, Ls, Lr FROM r_server WHERE sid=?"
            + "AND (Ls IS NOT NULL OR Lr IS NOT NULL) ORDER BY t ASC");
        // Always return a dummy destination
        pstr.put(69, SEL+"t, v, Ls, Lr FROM r_server WHERE sid=? "
            + "AND (t>? OR v=0) "
            + "AND (Ls IS NOT NULL OR Lr IS NOT NULL) ORDER BY t ASC");
        // A "timeout" of 30 seconds is hard-coded here
        pstr.put(68, SEL+"* FROM R WHERE re<=? AND ?<=re+30 AND rid NOT IN  "
            + "(SELECT rid FROM assignments_r)");
        pstr.put(85, SEL+"uq FROM UQ WHERE uid=?");
        pstr.put(86, SEL+"tp, td FROM CPD WHERE rid=?");
        pstr.put(73, SEL+"q2 FROM CQ WHERE sid=? AND t2<=? "
            + "ORDER BY t2 DESC, o2 DESC FETCH FIRST ROW ONLY");
        pstr.put(87, SEL+"t2, q2, o2 FROM CQ WHERE sid=? AND t2<? "
            + "ORDER BY t2 DESC, o2 DESC FETCH FIRST ROW ONLY");
        pstr.put(100, SEL+"rid FROM assignments WHERE t>? AND sid=?");
        pstr.put(101, SEL+"rid FROM assignments WHERE t<=? AND sid=?");
        pstr.put(102, SEL+"* FROM service_rate");
        pstr.put(103, SEL+"* FROM dist_base");
        pstr.put(104, SEL+"val FROM dist_s_travel WHERE sid=?");
        pstr.put(105, SEL+"SUM (val) FROM dist_s_travel");
        pstr.put(106, SEL+"val FROM dist_s_cruising WHERE sid=?");
        pstr.put(107, SEL+"SUM (val) FROM dist_s_cruising");
        pstr.put(108, SEL+"val FROM dist_s_service WHERE sid=?");
        pstr.put(109, SEL+"SUM (val) FROM dist_s_service");
        pstr.put(110, SEL+"val FROM dist_s_base");
        pstr.put(111, SEL+"val FROM dist_r_base");
        pstr.put(112, SEL+"val FROM dist_r_detour WHERE rid=?");
        pstr.put(113, SEL+"SUM (val) FROM dist_r_detour");
        pstr.put(114, SEL+"val FROM dist_r_transit WHERE rid=?");
        pstr.put(115, SEL+"SUM (val) FROM dist_r_transit");
        pstr.put(116, SEL+"val FROM dur_s_travel WHERE sid=?");
        pstr.put(117, SEL+"SUM (val) FROM dur_s_travel");
        pstr.put(118, SEL+"val FROM dur_r_pickup WHERE rid=?");
        pstr.put(119, SEL+"SUM (val) FROM dur_r_pickup");
        pstr.put(120, SEL+"val FROM dur_r_transit WHERE rid=?");
        pstr.put(121, SEL+"SUM (val) FROM dur_r_transit");
        pstr.put(122, SEL+"val FROM dur_r_travel WHERE rid=?");
        pstr.put(123, SEL+"SUM (val) FROM dur_r_travel");
        pstr.put(124, SEL+"val FROM t_r_depart WHERE rid=?");
        pstr.put(125, SEL+"val FROM t_s_depart WHERE sid=?");
        pstr.put(126, SEL+"val FROM t_r_arrive WHERE rid=?");
        pstr.put(127, SEL+"val FROM t_s_arrive WHERE sid=?");
        pstr.put(133, SEL+"val FROM f_status WHERE rid=? AND t<=? "
          + "ORDER BY t DESC FETCH FIRST ROW ONLY");
        try {
          for (Map.Entry<Integer, String> kv : pstr.entrySet()) {
            pstmt.put(kv.getKey(), PS(kv.getValue()));
          }
        }
        catch (SQLException e1) {
          printSQLException(e1);
          try {
            conn.rollback();
          } catch (SQLException e2) {
            printSQLException(e2);
          }
          DBSaveBackup("db-lastgood");
          throw new RuntimeException("database failure");
        }
      }
  }
