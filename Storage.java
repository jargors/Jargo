package com.github.jargors;
import java.sql.CallableStatement;   import java.sql.Connection;
import java.sql.DriverManager;       import java.sql.PreparedStatement;
import java.sql.ResultSet;           import java.sql.SQLException;
import java.sql.Statement;           import java.sql.Types;
import org.apache.commons.dbcp2.ConnectionFactory;
import org.apache.commons.dbcp2.DriverManagerConnectionFactory;
import org.apache.commons.dbcp2.PoolableConnection;
import org.apache.commons.dbcp2.PoolableConnectionFactory;
import org.apache.commons.dbcp2.PoolingDriver;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.time.LocalDateTime;
public class Storage {
  private Map<Integer, Boolean> lu_rstatus = new HashMap<>();  //*
  private ConcurrentHashMap<String, String> lu_pstr     = new ConcurrentHashMap<>();
  private ConcurrentHashMap<Integer, int[]> lu_vertices = new ConcurrentHashMap<>();
  private ConcurrentHashMap<Integer,
      ConcurrentHashMap<Integer, int[]>>    lu_edges    = new ConcurrentHashMap<>();
  private ConcurrentHashMap<Integer, int[]> lu_users    = new ConcurrentHashMap<>();
  private       String CONNECTIONS_URL        = "jdbc:derby:memory:jargo;create=true";
  private final String CONNECTIONS_DRIVER_URL = "jdbc:apache:commons:dbcp:";
  private final String CONNECTIONS_POOL_NAME  = "jargo";
  private final String CONNECTIONS_POOL_URL   = (CONNECTIONS_DRIVER_URL + CONNECTIONS_POOL_NAME);
  private final int    STATEMENTS_MAX_COUNT   = 20;
  private final int    DERBY_PAGECACHESIZE    = 8000;
  private final String DERBY_DUMPNAME         = "db-lastgood";
  private final int    REQUEST_TIMEOUT        = 30;
  private ConnectionFactory               connection_factory;
  private PoolableConnectionFactory       poolableconnection_factory;
  private ObjectPool<PoolableConnection>  pool;
  private PoolingDriver                   driver;
  private final String  VERSION = "1.0.0";
  private       boolean DEBUG   = false;
  public Storage() {
    Print("Initialize Storage Interface "+VERSION);
    try {
      setupDriver();
      PSInit();
    } catch (RuntimeException e) {
      e.printStackTrace();
    }
  }
  public int[] DBQuery(String sql, int ncols) throws RuntimeException {
    int[] output = new int[] { };
    try {
      Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL);
      Print("Open connection "+conn.toString());
    Statement stmt = conn.createStatement(
      ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
    ResultSet res = stmt.executeQuery(sql);
    if (res.last()) {
      output = new int[(ncols*res.getRow())];
      res.first();
      do {
        for (int j = 1; j <= ncols; j++) {
          output[((res.getRow() - 1)*ncols + (j - 1))] = res.getInt(j);
        }
      } while (res.next());
    }
    Print("Close connection "+conn.toString());
    conn.close();
      Print("Close connection "+conn.toString());
      conn.close();
    }
    catch (SQLException e1) {
      printSQLException(e1);
      DBSaveBackup(DERBY_DUMPNAME);
      throw new RuntimeException("database failure");
    }
    return output;
  }
  public int[] DBQueryCountVertices() throws RuntimeException {
    int[] output = new int[] { };
    try {
      Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL);
      Print("Open connection "+conn.toString());
    output = DBFetch(conn, "S62", 1);
      Print("Close connection "+conn.toString());
      conn.close();
    }
    catch (SQLException e1) {
      printSQLException(e1);
      DBSaveBackup(DERBY_DUMPNAME);
      throw new RuntimeException("database failure");
    }
    return output;
  }
  public int[] DBQueryCountEdges() throws RuntimeException {
    int[] output = new int[] { };
    try {
      Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL);
      Print("Open connection "+conn.toString());
    output = DBFetch(conn, "S63", 1);
      Print("Close connection "+conn.toString());
      conn.close();
    }
    catch (SQLException e1) {
      printSQLException(e1);
      DBSaveBackup(DERBY_DUMPNAME);
      throw new RuntimeException("database failure");
    }
    return output;
  }
  public final int[] DBQueryVertex(int v) throws RuntimeException {
    return lu_vertices.get(v);
  }
  public int[] DBQueryAllVertices() throws RuntimeException {
    int[] output = new int[] { };
    try {
      Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL);
      Print("Open connection "+conn.toString());
    output = DBFetch(conn, "S136", 3);
      Print("Close connection "+conn.toString());
      conn.close();
    }
    catch (SQLException e1) {
      printSQLException(e1);
      DBSaveBackup(DERBY_DUMPNAME);
      throw new RuntimeException("database failure");
    }
    return output;
  }
  public final int[] DBQueryEdge(int v1, int v2) throws RuntimeException {
    return lu_edges.get(v1).get(v2);
  }
  public int[] DBQueryAllEdges() throws RuntimeException {
    int[] output = new int[] { };
    try {
      Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL);
      Print("Open connection "+conn.toString());
    output = DBFetch(conn, "S137", 4);
      Print("Close connection "+conn.toString());
      conn.close();
    }
    catch (SQLException e1) {
      printSQLException(e1);
      DBSaveBackup(DERBY_DUMPNAME);
      throw new RuntimeException("database failure");
    }
    return output;
  }
  public int[] DBQueryStatisticsEdges() throws RuntimeException {
    int[] output = new int[] { };
    try {
      Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL);
      Print("Open connection "+conn.toString());
    output = DBFetch(conn, "S65", 6);
      Print("Close connection "+conn.toString());
      conn.close();
    }
    catch (SQLException e1) {
      printSQLException(e1);
      DBSaveBackup(DERBY_DUMPNAME);
      throw new RuntimeException("database failure");
    }
    return output;
  }
  public int[] DBQueryMBR() throws RuntimeException {
    int[] output = new int[] { };
    try {
      Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL);
      Print("Open connection "+conn.toString());
    output = DBFetch(conn, "S64", 4);
      Print("Close connection "+conn.toString());
      conn.close();
    }
    catch (SQLException e1) {
      printSQLException(e1);
      DBSaveBackup(DERBY_DUMPNAME);
      throw new RuntimeException("database failure");
    }
    return output;
  }
  public int[] DBQueryCountServers() throws RuntimeException {
    int[] output = new int[] { };
    try {
      Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL);
      Print("Open connection "+conn.toString());
    output = DBFetch(conn, "S66", 1);
      Print("Close connection "+conn.toString());
      conn.close();
    }
    catch (SQLException e1) {
      printSQLException(e1);
      DBSaveBackup(DERBY_DUMPNAME);
      throw new RuntimeException("database failure");
    }
    return output;
  }
  public int[] DBQueryCountRequests() throws RuntimeException {
    int[] output = new int[] { };
    try {
      Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL);
      Print("Open connection "+conn.toString());
    output = DBFetch(conn, "S67", 1);
      Print("Close connection "+conn.toString());
      conn.close();
    }
    catch (SQLException e1) {
      printSQLException(e1);
      DBSaveBackup(DERBY_DUMPNAME);
      throw new RuntimeException("database failure");
    }
    return output;
  }
  public int[] DBQueryAllUsers() throws RuntimeException {
    int[] output = new int[] { };
    try {
      Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL);
      Print("Open connection "+conn.toString());
    output = DBFetch(conn, "S141", 7);
      Print("Close connection "+conn.toString());
      conn.close();
    }
    catch (SQLException e1) {
      printSQLException(e1);
      DBSaveBackup(DERBY_DUMPNAME);
      throw new RuntimeException("database failure");
    }
    return output;
  }
  public final int[] DBQueryUser(int uid) throws RuntimeException {
    return lu_users.get(uid);
  }
  public int[] DBQueryRequestStatus(int rid, int t) throws RuntimeException {
    int[] output = new int[] { };
    try {
      Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL);
      Print("Open connection "+conn.toString());
    output = DBFetch(conn, "S133", 1, rid, t);
      Print("Close connection "+conn.toString());
      conn.close();
    }
    catch (SQLException e1) {
      printSQLException(e1);
      DBSaveBackup(DERBY_DUMPNAME);
      throw new RuntimeException("database failure");
    }
    return output;
  }
  public int[] DBQueryQueuedRequests(int t) throws RuntimeException {
    int[] output = new int[] { };
    int[] temp1 = new int[] { };
    int j = 0;
    try {
      Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL);
      Print("Open connection "+conn.toString());
      output = DBFetch(conn, "S143", 7, t, t, REQUEST_TIMEOUT);
      temp1 = new int[output.length];
      for (int i = 0; i < (output.length - 6); i += 7) {
        if (lu_rstatus.get(output[i]) == false) {
          temp1[(j + 0)] = output[(i + 0)];
          temp1[(j + 1)] = output[(i + 1)];
          temp1[(j + 2)] = output[(i + 2)];
          temp1[(j + 3)] = output[(i + 3)];
          temp1[(j + 4)] = output[(i + 4)];
          temp1[(j + 5)] = output[(i + 5)];
          temp1[(j + 6)] = output[(i + 6)];
          j += 7;
        }
      }
      Print("Close connection "+conn.toString());
      conn.close();
    }
    catch (SQLException e1) {
      printSQLException(e1);
      DBSaveBackup(DERBY_DUMPNAME);
      throw new RuntimeException("database failure");
    }
    int[] temp2 = new int[j];
    for (int i = 0; i < j; i += 7) {
      temp2[(i + 0)] = temp1[(i + 0)];
      temp2[(i + 1)] = temp1[(i + 1)];
      temp2[(i + 2)] = temp1[(i + 2)];
      temp2[(i + 3)] = temp1[(i + 3)];
      temp2[(i + 4)] = temp1[(i + 4)];
      temp2[(i + 5)] = temp1[(i + 5)];
      temp2[(i + 6)] = temp1[(i + 6)];
    }
    return temp2;
  }
  public int[] DBQueryActiveServers(int t) throws RuntimeException {
    int[] output = new int[] { };
    try {
      Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL);
      Print("Open connection "+conn.toString());
    output = DBFetch(conn, "S134", 1, t, t, t);
      Print("Close connection "+conn.toString());
      conn.close();
    }
    catch (SQLException e1) {
      printSQLException(e1);
      DBSaveBackup(DERBY_DUMPNAME);
      throw new RuntimeException("database failure");
    }
    return output;
  }
  public int[] DBQueryServerLocationsAll(int t) throws RuntimeException {
    int[] output = new int[] { };
    try {
      Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL);
      Print("Open connection "+conn.toString());
    output = DBFetch(conn, "S59", 3, t, t, t, t);
      Print("Close connection "+conn.toString());
      conn.close();
    }
    catch (SQLException e1) {
      printSQLException(e1);
      DBSaveBackup(DERBY_DUMPNAME);
      throw new RuntimeException("database failure");
    }
    return output;
  }
  public int[] DBQueryServerLocationsActive(int t) throws RuntimeException {
    int[] output = new int[] { };
    int[] temp1 = new int[] { };
    int[] temp2 = new int[] { };
    int j = 0;
    int sid = 0;
    int te = 0;
    try {
      Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL);
      Print("Open connection "+conn.toString());
      temp1 = DBFetch(conn, "S134", 2, t, t, t);      // <-- 10 ms/call
      output = new int[(3*(temp1.length/2))];
      for (int i = 0; i < temp1.length - 1; i += 2) {
        sid = temp1[(i + 0)];
         te = temp1[(i + 1)];
        temp2 = (t < te
          ? DBFetch(conn, "S135", 2, sid, sid, t, t)  // <-- 0.07-0.15 ms/call
          : DBFetch(conn, "S147", 2, sid, sid));      // <-- 0.04-0.15 ms/call
        output[(j + 0)] = sid;
        output[(j + 1)] = temp2[0];
        output[(j + 2)] = temp2[1];
        j += 3;
      }
      conn.close();
    }
    catch (SQLException e1) {
      printSQLException(e1);
      DBSaveBackup(DERBY_DUMPNAME);
      throw new RuntimeException("database failure");
    }
    return output;
  }
  public int[] DBQueryServerRoute(int sid) throws RuntimeException {
    int[] output = new int[] { };
    try {
      Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL);
      Print("Open connection "+conn.toString());
    output = DBFetch(conn, "S60", 2, sid);
      Print("Close connection "+conn.toString());
      conn.close();
    }
    catch (SQLException e1) {
      printSQLException(e1);
      DBSaveBackup(DERBY_DUMPNAME);
      throw new RuntimeException("database failure");
    }
    return output;
  }
  public int[] DBQueryServerSchedule(int sid) throws RuntimeException {
    int[] output = new int[] { };
    try {
      Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL);
      Print("Open connection "+conn.toString());
    output = DBFetch(conn, "S61", 4, sid);
      Print("Close connection "+conn.toString());
      conn.close();
    }
    catch (SQLException e1) {
      printSQLException(e1);
      DBSaveBackup(DERBY_DUMPNAME);
      throw new RuntimeException("database failure");
    }
    return output;
  }
  public int[] DBQueryServerRemainingRoute(int sid, int t) throws RuntimeException {
    int[] output = new int[] { };
    try {
      Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL);
      Print("Open connection "+conn.toString());
    output = DBFetch(conn, "S129", 2, sid, t);
      Print("Close connection "+conn.toString());
      conn.close();
    }
    catch (SQLException e1) {
      printSQLException(e1);
      DBSaveBackup(DERBY_DUMPNAME);
      throw new RuntimeException("database failure");
    }
    return output;
  }
  public int[] DBQueryServerRemainingSchedule(int sid, int t)
  throws RuntimeException {
    int[] output = new int[] { };
    int[] temp = new int[] { };
    try {
      Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL);
      Print("Open connection "+conn.toString());
      temp = DBFetch(conn, "S144", 3, sid, t);
      output = new int[4*temp.length/3 + 4];
      int j = 0;
      for (int i = 0; i < temp.length - 2; i += 3) {
        output[(j + 0)] = temp[(i + 0)];
        output[(j + 1)] = temp[(i + 1)];
        output[(j + 2)] = 0;
        output[(j + 3)] = temp[(i + 2)];
        j += 4;
      }
      temp = DBFetch(conn, "S145", 2, sid);
      output[(j + 0)] = temp[0];
      output[(j + 1)] = temp[1];
      output[(j + 2)] = sid;
      output[(j + 3)] = 0;
      Print("Close connection "+conn.toString());
      conn.close();
    }
    catch (SQLException e1) {
      printSQLException(e1);
      DBSaveBackup(DERBY_DUMPNAME);
      throw new RuntimeException("database failure");
    }
    return output;
  }
  public int[] DBQueryServerRemainingDistance(int sid, int t)
  throws RuntimeException {
    int[] output = new int[] { };
    try {
      Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL);
      Print("Open connection "+conn.toString());
    output = DBFetch(conn, "S142", 1, sid, t);
      Print("Close connection "+conn.toString());
      conn.close();
    }
    catch (SQLException e1) {
      printSQLException(e1);
      DBSaveBackup(DERBY_DUMPNAME);
      throw new RuntimeException("database failure");
    }
    return output;
  }
  public int[] DBQueryServerRemainingDuration(int sid, int t)
  throws RuntimeException {
    int[] output = new int[] { };
    try {
      Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL);
      Print("Open connection "+conn.toString());
    output = DBFetch(conn, "S127", 1, sid, t);
    output[0] -= t;
      Print("Close connection "+conn.toString());
      conn.close();
    }
    catch (SQLException e1) {
      printSQLException(e1);
      DBSaveBackup(DERBY_DUMPNAME);
      throw new RuntimeException("database failure");
    }
    return output;
  }
  public int[] DBQueryServerMaxLoad(int sid, int t) throws RuntimeException {
    int[] output = new int[] { };
    try {
      Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL);
      Print("Open connection "+conn.toString());
    output = DBFetch(conn, "S73", 1, sid, t);
      Print("Close connection "+conn.toString());
      conn.close();
    }
    catch (SQLException e1) {
      printSQLException(e1);
      DBSaveBackup(DERBY_DUMPNAME);
      throw new RuntimeException("database failure");
    }
    return output;
  }
  public int[] DBQueryServerPendingAssignments(int sid, int t) throws RuntimeException {
    int[] output = new int[] { };
    try {
      Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL);
      Print("Open connection "+conn.toString());
    output = DBFetch(conn, "S100", 1, t, sid);
      Print("Close connection "+conn.toString());
      conn.close();
    }
    catch (SQLException e1) {
      printSQLException(e1);
      DBSaveBackup(DERBY_DUMPNAME);
      throw new RuntimeException("database failure");
    }
    return output;
  }
  public int[] DBQueryServerCompletedAssignments(int sid, int t) throws RuntimeException {
    int[] output = new int[] { };
    try {
      Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL);
      Print("Open connection "+conn.toString());
    output = DBFetch(conn, "S101", 1, t, sid);
      Print("Close connection "+conn.toString());
      conn.close();
    }
    catch (SQLException e1) {
      printSQLException(e1);
      DBSaveBackup(DERBY_DUMPNAME);
      throw new RuntimeException("database failure");
    }
    return output;
  }
  public int[] DBQueryServiceRate() throws RuntimeException {
    int[] output = new int[] { };
    try {
      Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL);
      Print("Open connection "+conn.toString());
    output = DBFetch(conn, "S102", 1);
      Print("Close connection "+conn.toString());
      conn.close();
    }
    catch (SQLException e1) {
      printSQLException(e1);
      DBSaveBackup(DERBY_DUMPNAME);
      throw new RuntimeException("database failure");
    }
    return output;
  }
  public int[] DBQueryBaseDistanceTotal() throws RuntimeException {
    int[] output = new int[] { };
    try {
      Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL);
      Print("Open connection "+conn.toString());
    output = DBFetch(conn, "S103", 1);
      Print("Close connection "+conn.toString());
      conn.close();
    }
    catch (SQLException e1) {
      printSQLException(e1);
      DBSaveBackup(DERBY_DUMPNAME);
      throw new RuntimeException("database failure");
    }
    return output;
  }
  public int[] DBQueryServerBaseDistanceTotal() throws RuntimeException {
    int[] output = new int[] { };
    try {
      Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL);
      Print("Open connection "+conn.toString());
    output = DBFetch(conn, "S110", 1);
      Print("Close connection "+conn.toString());
      conn.close();
    }
    catch (SQLException e1) {
      printSQLException(e1);
      DBSaveBackup(DERBY_DUMPNAME);
      throw new RuntimeException("database failure");
    }
    return output;
  }
  public int[] DBQueryRequestBaseDistanceTotal() throws RuntimeException {
    int[] output = new int[] { };
    try {
      Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL);
      Print("Open connection "+conn.toString());
    output = DBFetch(conn, "S111", 1);
      Print("Close connection "+conn.toString());
      conn.close();
    }
    catch (SQLException e1) {
      printSQLException(e1);
      DBSaveBackup(DERBY_DUMPNAME);
      throw new RuntimeException("database failure");
    }
    return output;
  }
  public int[] DBQueryRequestBaseDistanceUnassigned() throws RuntimeException {
    int[] output = new int[] { };
    try {
      Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL);
      Print("Open connection "+conn.toString());
    output = DBFetch(conn, "S138", 1);
      Print("Close connection "+conn.toString());
      conn.close();
    }
    catch (SQLException e1) {
      printSQLException(e1);
      DBSaveBackup(DERBY_DUMPNAME);
      throw new RuntimeException("database failure");
    }
    return output;
  }
  public int[] DBQueryServerTravelDistance(int sid) throws RuntimeException {
    int[] output = new int[] { };
    try {
      Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL);
      Print("Open connection "+conn.toString());
    output = DBFetch(conn, "S104", 1, sid);
      Print("Close connection "+conn.toString());
      conn.close();
    }
    catch (SQLException e1) {
      printSQLException(e1);
      DBSaveBackup(DERBY_DUMPNAME);
      throw new RuntimeException("database failure");
    }
    return output;
  }
  public int[] DBQueryServerTravelDistanceTotal() throws RuntimeException {
    int[] output = new int[] { };
    try {
      Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL);
      Print("Open connection "+conn.toString());
    output = DBFetch(conn, "S105", 1);
      Print("Close connection "+conn.toString());
      conn.close();
    }
    catch (SQLException e1) {
      printSQLException(e1);
      DBSaveBackup(DERBY_DUMPNAME);
      throw new RuntimeException("database failure");
    }
    return output;
  }
  public int[] DBQueryServerCruisingDistance(int sid) throws RuntimeException {
    int[] output = new int[] { };
    try {
      Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL);
      Print("Open connection "+conn.toString());
    output = DBFetch(conn, "S106", 1, sid);
      Print("Close connection "+conn.toString());
      conn.close();
    }
    catch (SQLException e1) {
      printSQLException(e1);
      DBSaveBackup(DERBY_DUMPNAME);
      throw new RuntimeException("database failure");
    }
    return output;
  }
  public int[] DBQueryServerCruisingDistanceTotal() throws RuntimeException {
    int[] output = new int[] { };
    try {
      Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL);
      Print("Open connection "+conn.toString());
    output = DBFetch(conn, "S107", 1);
      Print("Close connection "+conn.toString());
      conn.close();
    }
    catch (SQLException e1) {
      printSQLException(e1);
      DBSaveBackup(DERBY_DUMPNAME);
      throw new RuntimeException("database failure");
    }
    return output;
  }
  public int[] DBQueryServerServiceDistance(int sid) throws RuntimeException {
    int[] output = new int[] { };
    try {
      Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL);
      Print("Open connection "+conn.toString());
    output = DBFetch(conn, "S108", 1, sid);
      Print("Close connection "+conn.toString());
      conn.close();
    }
    catch (SQLException e1) {
      printSQLException(e1);
      DBSaveBackup(DERBY_DUMPNAME);
      throw new RuntimeException("database failure");
    }
    return output;
  }
  public int[] DBQueryServerServiceDistanceTotal() throws RuntimeException {
    int[] output = new int[] { };
    try {
      Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL);
      Print("Open connection "+conn.toString());
    output = DBFetch(conn, "S109", 1);
      Print("Close connection "+conn.toString());
      conn.close();
    }
    catch (SQLException e1) {
      printSQLException(e1);
      DBSaveBackup(DERBY_DUMPNAME);
      throw new RuntimeException("database failure");
    }
    return output;
  }
  public int[] DBQueryRequestDetourDistance(int rid) throws RuntimeException {
    int[] output = new int[] { };
    try {
      Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL);
      Print("Open connection "+conn.toString());
    output = DBFetch(conn, "S112", 1, rid);
      Print("Close connection "+conn.toString());
      conn.close();
    }
    catch (SQLException e1) {
      printSQLException(e1);
      DBSaveBackup(DERBY_DUMPNAME);
      throw new RuntimeException("database failure");
    }
    return output;
  }
  public int[] DBQueryRequestDetourDistanceTotal() throws RuntimeException {
    int[] output = new int[] { };
    try {
      Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL);
      Print("Open connection "+conn.toString());
    output = DBFetch(conn, "S113", 1);
      Print("Close connection "+conn.toString());
      conn.close();
    }
    catch (SQLException e1) {
      printSQLException(e1);
      DBSaveBackup(DERBY_DUMPNAME);
      throw new RuntimeException("database failure");
    }
    return output;
  }
  public int[] DBQueryRequestTransitDistance(int rid) throws RuntimeException {
    int[] output = new int[] { };
    try {
      Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL);
      Print("Open connection "+conn.toString());
    output = DBFetch(conn, "S114", 1, rid);
      Print("Close connection "+conn.toString());
      conn.close();
    }
    catch (SQLException e1) {
      printSQLException(e1);
      DBSaveBackup(DERBY_DUMPNAME);
      throw new RuntimeException("database failure");
    }
    return output;
  }
  public int[] DBQueryRequestTransitDistanceTotal() throws RuntimeException {
    int[] output = new int[] { };
    try {
      Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL);
      Print("Open connection "+conn.toString());
    output = DBFetch(conn, "S115", 1);
      Print("Close connection "+conn.toString());
      conn.close();
    }
    catch (SQLException e1) {
      printSQLException(e1);
      DBSaveBackup(DERBY_DUMPNAME);
      throw new RuntimeException("database failure");
    }
    return output;
  }
  public int[] DBQueryServerTravelDuration(int sid) throws RuntimeException {
    int[] output = new int[] { };
    try {
      Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL);
      Print("Open connection "+conn.toString());
    output = DBFetch(conn, "S116", 1, sid);
      Print("Close connection "+conn.toString());
      conn.close();
    }
    catch (SQLException e1) {
      printSQLException(e1);
      DBSaveBackup(DERBY_DUMPNAME);
      throw new RuntimeException("database failure");
    }
    return output;
  }
  public int[] DBQueryServerTravelDurationTotal() throws RuntimeException {
    int[] output = new int[] { };
    try {
      Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL);
      Print("Open connection "+conn.toString());
    output = DBFetch(conn, "S117", 1);
      Print("Close connection "+conn.toString());
      conn.close();
    }
    catch (SQLException e1) {
      printSQLException(e1);
      DBSaveBackup(DERBY_DUMPNAME);
      throw new RuntimeException("database failure");
    }
    return output;
  }
  public int[] DBQueryRequestPickupDuration(int rid) throws RuntimeException {
    int[] output = new int[] { };
    try {
      Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL);
      Print("Open connection "+conn.toString());
    output = DBFetch(conn, "S118", 1, rid);
      Print("Close connection "+conn.toString());
      conn.close();
    }
    catch (SQLException e1) {
      printSQLException(e1);
      DBSaveBackup(DERBY_DUMPNAME);
      throw new RuntimeException("database failure");
    }
    return output;
  }
  public int[] DBQueryRequestPickupDurationTotal() throws RuntimeException {
    int[] output = new int[] { };
    try {
      Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL);
      Print("Open connection "+conn.toString());
    output = DBFetch(conn, "S119", 1);
      Print("Close connection "+conn.toString());
      conn.close();
    }
    catch (SQLException e1) {
      printSQLException(e1);
      DBSaveBackup(DERBY_DUMPNAME);
      throw new RuntimeException("database failure");
    }
    return output;
  }
  public int[] DBQueryRequestTransitDuration(int rid) throws RuntimeException {
    int[] output = new int[] { };
    try {
      Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL);
      Print("Open connection "+conn.toString());
    output = DBFetch(conn, "S120", 1, rid);
      Print("Close connection "+conn.toString());
      conn.close();
    }
    catch (SQLException e1) {
      printSQLException(e1);
      DBSaveBackup(DERBY_DUMPNAME);
      throw new RuntimeException("database failure");
    }
    return output;
  }
  public int[] DBQueryRequestTransitDurationTotal() throws RuntimeException {
    int[] output = new int[] { };
    try {
      Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL);
      Print("Open connection "+conn.toString());
    output = DBFetch(conn, "S121", 1);
      Print("Close connection "+conn.toString());
      conn.close();
    }
    catch (SQLException e1) {
      printSQLException(e1);
      DBSaveBackup(DERBY_DUMPNAME);
      throw new RuntimeException("database failure");
    }
    return output;
  }
  public int[] DBQueryRequestTravelDuration(int rid) throws RuntimeException {
    int[] output = new int[] { };
    try {
      Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL);
      Print("Open connection "+conn.toString());
    output = DBFetch(conn, "S122", 1, rid);
      Print("Close connection "+conn.toString());
      conn.close();
    }
    catch (SQLException e1) {
      printSQLException(e1);
      DBSaveBackup(DERBY_DUMPNAME);
      throw new RuntimeException("database failure");
    }
    return output;
  }
  public int[] DBQueryRequestTravelDurationTotal() throws RuntimeException {
    int[] output = new int[] { };
    try {
      Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL);
      Print("Open connection "+conn.toString());
    output = DBFetch(conn, "S123", 1);
      Print("Close connection "+conn.toString());
      conn.close();
    }
    catch (SQLException e1) {
      printSQLException(e1);
      DBSaveBackup(DERBY_DUMPNAME);
      throw new RuntimeException("database failure");
    }
    return output;
  }
  public int[] DBQueryRequestDepartureTime(int rid) throws RuntimeException {
    int[] output = new int[] { };
    try {
      Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL);
      Print("Open connection "+conn.toString());
    output = DBFetch(conn, "S124", 1, rid);
      Print("Close connection "+conn.toString());
      conn.close();
    }
    catch (SQLException e1) {
      printSQLException(e1);
      DBSaveBackup(DERBY_DUMPNAME);
      throw new RuntimeException("database failure");
    }
    return output;
  }
  public int[] DBQueryServerDepartureTime(int sid) throws RuntimeException {
    int[] output = new int[] { };
    try {
      Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL);
      Print("Open connection "+conn.toString());
    output = DBFetch(conn, "S125", 1, sid);
      Print("Close connection "+conn.toString());
      conn.close();
    }
    catch (SQLException e1) {
      printSQLException(e1);
      DBSaveBackup(DERBY_DUMPNAME);
      throw new RuntimeException("database failure");
    }
    return output;
  }
  public int[] DBQueryRequestArrivalTime(int rid) throws RuntimeException {
    int[] output = new int[] { };
    try {
      Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL);
      Print("Open connection "+conn.toString());
    output = DBFetch(conn, "S126", 1, rid);
      Print("Close connection "+conn.toString());
      conn.close();
    }
    catch (SQLException e1) {
      printSQLException(e1);
      DBSaveBackup(DERBY_DUMPNAME);
      throw new RuntimeException("database failure");
    }
    return output;
  }
  public int[] DBQueryServerArrivalTime(int sid) throws RuntimeException {
    int[] output = new int[] { };
    try {
      Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL);
      Print("Open connection "+conn.toString());
    output = DBFetch(conn, "S127", 1, sid);
      Print("Close connection "+conn.toString());
      conn.close();
    }
    catch (SQLException e1) {
      printSQLException(e1);
      DBSaveBackup(DERBY_DUMPNAME);
      throw new RuntimeException("database failure");
    }
    return output;
  }
  public void DBAddNewVertex(int v, int lng, int lat) throws RuntimeException {
    if (!lu_vertices.containsKey(v)) {
      try {
        Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL);
        Print("Open connection "+conn.toString());
        PreparedStatement pS0 = PS(conn, "S0");
        PSAdd(pS0, v, lng, lat);
        PSSubmit(pS0);
        conn.commit();
        Print("Close connection "+conn.toString());
        conn.close();
        lu_vertices.put(v, new int[] { lng, lat });
      }
      catch (SQLException e1) {
        printSQLException(e1);
        DBSaveBackup(DERBY_DUMPNAME);
        throw new RuntimeException("database failure");
      }
    }
  }
  public void DBAddNewEdge(int v1, int v2, int dd, int nu) throws RuntimeException {
    if (!lu_edges.containsKey(v1)) {
      lu_edges.put(v1, new ConcurrentHashMap());
    }
    if (!lu_edges.get(v1).containsKey(v2)) {
      try {
        Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL);
        Print("Open connection "+conn.toString());
        PreparedStatement pS1 = PS(conn, "S1");
        PSAdd(pS1, v1, v2, dd, nu);
        PSSubmit(pS1);
        conn.commit();
        Print("Close connection "+conn.toString());
        conn.close();
        lu_edges.get(v1).put(v2, new int[] { dd, nu });
      }
      catch (SQLException e1) {
        printSQLException(e1);
        DBSaveBackup(DERBY_DUMPNAME);
        throw new RuntimeException("database failure");
      }
    }
  }
  public void DBAddNewRequest(int[] u) throws RuntimeException {
    if (!lu_users.containsKey(u[0])) {
      try {
        Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL);
        Print("Open connection "+conn.toString());
        int uid = u[0];
        PreparedStatement pS2 = PS(conn, "S2");
        PreparedStatement pS3 = PS(conn, "S3");
        PreparedStatement pS4 = PS(conn, "S4");
        PreparedStatement pS5 = PS(conn, "S5");
        PreparedStatement pS6 = PS(conn, "S6");
        PreparedStatement pS7 = PS(conn, "S7");
        PSAdd(pS2, uid, u[1]);
        PSAdd(pS3, uid, u[2]);
        PSAdd(pS4, uid, u[3]);
        PSAdd(pS5, uid, u[4]);
        PSAdd(pS6, uid, u[5]);
        PSAdd(pS7, uid, u[6]);
        PSSubmit(pS2, pS3, pS4, pS5, pS6, pS7);
        PreparedStatement pS9 = PS(conn, "S9");
        PSAdd(pS9, uid, u[1], u[2], u[3], u[4], u[5], u[6]);
        PSSubmit(pS9);
        conn.commit();
        Print("Close connection "+conn.toString());
        conn.close();
        lu_users.put(u[0], u.clone());
        lu_rstatus.put(u[0], false);
      }
      catch (SQLException e1) {
        printSQLException(e1);
        DBSaveBackup(DERBY_DUMPNAME);
        throw new RuntimeException("database failure");
      }
    }
  }
  public void DBAddNewServer(int[] u, int[] route) throws RuntimeException {
    int uid = u[0];
    if (!lu_users.containsKey(uid)) {
      try {
        Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL);
        Print("Open connection "+conn.toString());
        int se = u[2];
        PreparedStatement pS2 = PS(conn, "S2");
        PreparedStatement pS3 = PS(conn, "S3");
        PreparedStatement pS4 = PS(conn, "S4");
        PreparedStatement pS5 = PS(conn, "S5");
        PreparedStatement pS6 = PS(conn, "S6");
        PreparedStatement pS7 = PS(conn, "S7");
        PSAdd(pS2, uid, u[1]);
        PSAdd(pS3, uid, u[2]);
        PSAdd(pS4, uid, u[3]);
        PSAdd(pS5, uid, u[4]);
        PSAdd(pS6, uid, u[5]);
        PSAdd(pS7, uid, u[6]);
        PSSubmit(pS2, pS3, pS4, pS5, pS6, pS7);
        PreparedStatement pS8 = PS(conn, "S8");
        PSAdd(pS8, uid, u[1], u[2], u[3], u[4], u[5], u[6]);
        PSSubmit(pS8);
        int[] output = new int[] { };
        PreparedStatement pS10 = PS(conn, "S10");
        for (int i = 0; i < (route.length - 3); i += 2) {
          int t1 = route[i];
          int v1 = route[(i + 1)];
          int t2 = route[(i + 2)];
          int v2 = route[(i + 3)];
          int dd = lu_edges.get(v1).get(v2)[0];
          int nu = lu_edges.get(v1).get(v2)[1];
          Print("Issue INSERT INTO W VALUES ("+uid+", "+se+", "+t1+", "+v1+", "+t2+", "
            +v2+", "+dd+", "+nu+")");
          PSAdd(pS10, uid, se, t1, v1, t2, v2, dd, nu);
        }
        PSSubmit(pS10);
        pS10 = PS(conn, "S10");
        PSAdd(pS10, uid, se, null, null, route[0], route[1], null, null);
        PSSubmit(pS10);
        PreparedStatement pS11 = PS(conn, "S11");
        int te = route[(route.length - 2)];
        PSAdd(pS11, uid, u[2], u[3], u[4], u[5], u[2], u[4], te, u[5]);
        PSSubmit(pS11);
        PreparedStatement pS14 = PS(conn, "S14");
        PSAdd(pS14, uid, u[1], u[2], null, u[2], u[4], null, u[1],
            null, null, null, null, null, 1);
        PSSubmit(pS14);
        conn.commit();
        Print("Close connection "+conn.toString());
        conn.close();
        lu_users.put(uid, u.clone());
      }
      catch (SQLException e1) {
        printSQLException(e1);
        DBSaveBackup(DERBY_DUMPNAME);
        throw new RuntimeException("database failure");
      }
    }
  }
  public void DBUpdateEdgeSpeed(int v1, int v2, int nu) throws RuntimeException {
    try {
      Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL);
      Print("Open connection "+conn.toString());
      PreparedStatement pS15 = PS(conn, "S15");
      PreparedStatement pS131 = PS(conn, "S131");
      PSAdd(pS15, nu, v1, v2);
      PSAdd(pS131, nu, v1, v2);
      PSSubmit(pS15, pS131);
      conn.commit();
      Print("Close connection "+conn.toString());
      conn.close();
      int dd = lu_edges.get(v1).get(v2)[0];
      lu_edges.get(v1).put(v2, new int[] { dd, nu });
    }
    catch (SQLException e1) {
      printSQLException(e1);
      DBSaveBackup(DERBY_DUMPNAME);
      throw new RuntimeException("database failure");
    }
  }
  public void DBUpdateServerRoute(int sid, int[] route, int[] sched)
  throws RuntimeException {
    int[] output = new int[] { };
    int se, sq;
    try {
      Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL);
      Print("Open connection "+conn.toString());
      sq = lu_users.get(sid)[1];
      se = lu_users.get(sid)[2];
      Print("B1");
      PreparedStatement pS76 = PS(conn, "S76");
      PSAdd(pS76, sid, route[0]);
      Print("Issue DELETE FROM W WHERE sid="+sid+" AND t2>"+route[0]);
      PSSubmit(pS76);
      Print("B2");
      int uid = sid;
      PreparedStatement pS10 = PS(conn, "S10");
      for (int i = 0; i < (route.length - 3); i += 2) {
        int t1 = route[i];
        int v1 = route[(i + 1)];
        int t2 = route[(i + 2)];
        int v2 = route[(i + 3)];
        int dd = lu_edges.get(v1).get(v2)[0];
        int nu = lu_edges.get(v1).get(v2)[1];
        Print("Issue INSERT INTO W VALUES ("+uid+", "+se+", "+t1+", "+v1+", "+t2+", "
          +v2+", "+dd+", "+nu+")");
        PSAdd(pS10, uid, se, t1, v1, t2, v2, dd, nu);
      }
      PSSubmit(pS10);
      Print("B3");
      PreparedStatement pS77 = PS(conn, "S77");
      PreparedStatement pS139 = PS(conn, "S139");
      int te = route[(route.length - 2)];
      int ve = route[(route.length - 1)];
      PSAdd(pS77, te, ve, sid);
      PSAdd(pS139, te, sid);
      Print("Issue UPDATE CW SET te="+te+", ve="+ve+" WHERE sid="+sid);
      Print("Issue UPDATE CPD SET te="+te+" WHERE sid="+sid);
      PSSubmit(pS77, pS139);
      Print("B4");
      if (sched.length > 0) {
        Map<Integer, int[]> cache = new HashMap<>();
        int bound = (sched.length/3);
        PreparedStatement pS82 = PS(conn, "S82");
        PreparedStatement pS83 = PS(conn, "S83");
        PreparedStatement pS84 = PS(conn, "S84");
        for (int j = 0; j < bound; j++) {
          int tj = sched[(3*j + 0)];
          int vj = sched[(3*j + 1)];
          int Lj = sched[(3*j + 2)];
          if (Lj != sid) {
            Print("UPDATE CPD SET tp="+tj+" WHERE vp="+vj+" AND rid="+Lj);
            Print("UPDATE CPD SET td="+tj+" WHERE vd="+vj+" AND rid="+Lj);
            Print("UPDATE PD SET t2="+tj+" WHERE v2="+vj+" AND rid="+Lj);
            PSAdd(pS82, tj, vj, Lj);
            PSAdd(pS83, tj, vj, Lj);
            PSAdd(pS84, tj, vj, Lj);
          }
        }
        PSSubmit(pS82, pS83, pS84);
        PreparedStatement pS140 = PS(conn, "S140");
        for (int j = 0; j < bound; j++) {
          int Lj = sched[(3*j + 2)];
          if (Lj != sid) {
            int rq, tp, td;
            if (!cache.containsKey(Lj)) {
              rq = DBFetch(conn, "S85", 1, Lj)[0];
              output = DBFetch(conn, "S86", 2, Lj);
              tp = output[0];
              td = output[1];
              cache.put(Lj, new int[] { rq, tp, td });
              Print("UPDATE CQ SET tp="+tp+", td="+td+" WHERE rid="+Lj);
              PSAdd(pS140, tp, td, Lj);
            }
          }
        }
        PSSubmit(pS140);
        int t1, q1, o1;
        if (route[0] == 0) {
          t1 = 0;
          q1 = sq;
          o1 = 1;
        } else {
          output = DBFetch(conn, "S87", 3, sid, route[0]);
          t1 = output[0];
          q1 = output[1];
          o1 = output[2];
        }
        PreparedStatement pS80 = PS(conn, "S80");
        PSAdd(pS80, sid, route[0]);
        Print("Issue DELETE FROM CQ WHERE sid="+sid+" AND t2>"+route[0]);
        PSSubmit(pS80);
        PreparedStatement pS14 = PS(conn, "S14");
        for (int j = 0; j < bound; j++) {
          int t2 = sched[(3*j)];
          int v2 = sched[(3*j + 1)];
          int Lj = sched[(3*j + 2)];
          if (Lj != sid) {
            int[] qpd = cache.get(Lj);
            int q2 = (t2 == qpd[1] ? q1 + qpd[0] : q1 - qpd[0]);
            int o2 = o1 + 1;
            Print("Issue INSERT INTO CQ VALUES "+sid+", "+sq+", "+se+", "+t1+", "+t2
                +", "+v2+", "+q1+", "+q2+", "+Lj+", "+qpd[0]+", "+qpd[1]+", "+qpd[2]
                +", "+o1+", "+o2);
            PSAdd(pS14, sid, sq, se, t1, t2, v2, q1, q2, Lj,
                  qpd[0], qpd[1], qpd[2], o1, o2);
            t1 = t2;
            q1 = q2;
            o1 = o2;
          }
        }
        PSSubmit(pS14);
      }
      conn.commit();
      Print("Close connection "+conn.toString());
      conn.close();
    }
    catch (SQLException e1) {
      printSQLException(e1);
      DBSaveBackup(DERBY_DUMPNAME);
      throw new RuntimeException("database failure");
    }
  }
  public void DBUpdateServerAddToSchedule(
      int sid, int[] route, int[] sched, int[] rid)
  throws RuntimeException {
    Print("Call DBUpdateServerAddToSchedule(4)");
    Print("  sid="+sid);
    Print("  route.length="+route.length);
    Print("  schedule.length="+sched.length);
    for (int i = 0; i < route.length; i++)
      Print("route["+i+"]="+route[i]);
    for (int i = 0; i < sched.length; i++)
      Print("sched["+i+"]="+sched[i]);
    for (int i = 0; i < rid.length; i++)
      Print("rid["+i+"]="+rid[i]);

    int[] output = new int[] { };
    int se, sq;
    Map<Integer, int[]> cache = new HashMap<>();
    Map<Integer, int[]> cache2 = new HashMap<>();
    try {
      Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL);
      Print("Open connection "+conn.toString());
      Print("A1");
      sq = lu_users.get(sid)[1];
      se = lu_users.get(sid)[2];
      Print("A2");
      Print("B1");
      PreparedStatement pS76 = PS(conn, "S76");
      PSAdd(pS76, sid, route[0]);
      Print("Issue DELETE FROM W WHERE sid="+sid+" AND t2>"+route[0]);
      PSSubmit(pS76);
      Print("B2");
      int uid = sid;
      PreparedStatement pS10 = PS(conn, "S10");
      for (int i = 0; i < (route.length - 3); i += 2) {
        int t1 = route[i];
        int v1 = route[(i + 1)];
        int t2 = route[(i + 2)];
        int v2 = route[(i + 3)];
        int dd = lu_edges.get(v1).get(v2)[0];
        int nu = lu_edges.get(v1).get(v2)[1];
        Print("Issue INSERT INTO W VALUES ("+uid+", "+se+", "+t1+", "+v1+", "+t2+", "
          +v2+", "+dd+", "+nu+")");
        PSAdd(pS10, uid, se, t1, v1, t2, v2, dd, nu);
      }
      PSSubmit(pS10);
      Print("B3");
      PreparedStatement pS77 = PS(conn, "S77");
      PreparedStatement pS139 = PS(conn, "S139");
      int te = route[(route.length - 2)];
      int ve = route[(route.length - 1)];
      PSAdd(pS77, te, ve, sid);
      PSAdd(pS139, te, sid);
      Print("Issue UPDATE CW SET te="+te+", ve="+ve+" WHERE sid="+sid);
      Print("Issue UPDATE CPD SET te="+te+" WHERE sid="+sid);
      PSSubmit(pS77, pS139);
      Print("B4");
      Print("A3");
      int bound = (sched.length/3);
      Print("C1");
      PreparedStatement pS82 = PS(conn, "S82");
      PreparedStatement pS83 = PS(conn, "S83");
      PreparedStatement pS84 = PS(conn, "S84");
      for (int j = 0; j < bound; j++) {
        int tj = sched[(3*j + 0)];
        int vj = sched[(3*j + 1)];
        int Lj = sched[(3*j + 2)];
        if (Lj != sid) {
          Print("UPDATE CPD SET tp="+tj+" WHERE vp="+vj+" AND rid="+Lj);
          Print("UPDATE CPD SET td="+tj+" WHERE vd="+vj+" AND rid="+Lj);
          Print("UPDATE PD SET t2="+tj+" WHERE v2="+vj+" AND rid="+Lj);
          PSAdd(pS82, tj, vj, Lj);
          PSAdd(pS83, tj, vj, Lj);
          PSAdd(pS84, tj, vj, Lj);
        }
      }
      PSSubmit(pS82, pS83, pS84);
      Print("C2");
      PreparedStatement pS140 = PS(conn, "S140");
      for (int j = 0; j < bound; j++) {
        int Lj = sched[(3*j + 2)];
        if (Lj != sid) {
          int rq, tp, vp;
          int td = -1;
          int vd = -1;
          if (!cache.containsKey(Lj)) {
            // rq = DBFetch(conn, "S85", 1, Lj)[0];
            rq = lu_users.get(Lj)[1];
            boolean flagged = false;
            for (int r : rid) {
              if (Lj == r) {
                Print("set flagged=true");
                flagged = true;
                break;
              }
            }
            if (flagged) {
              Print("get tp, vp, td, vd of new job");
              tp = sched[(3*j)];
              vp = sched[(3*j + 1)];
              Print("set tp="+tp);
              Print("set vd="+vd);
              for (int k = (j + 1); k < bound; k++) {
                Print(": k="+k);
                Print(":   Lj="+Lj);
                Print(":   sched[(3*k+2)]="+sched[3*k+2]);
                if (Lj == sched[(3*k + 2)]) {
                  td = sched[(3*k)];
                  vd = sched[(3*k + 1)];
                  Print(":   set td="+sched[3*k]);
                  Print(":   set vd="+sched[3*k+1]);
                }
              }
              Print("set td="+td);
              Print("set vd="+vd);
              cache2.put(Lj, new int[] { vp, vd });
            } else {
              output = DBFetch(conn, "S86", 2, Lj);
              tp = output[0];
              td = output[1];
              Print("UPDATE CQ SET tp="+tp+", td="+td+" WHERE rid="+Lj);
              PSAdd(pS140, tp, td, Lj);
            }
            Print("cache.put("+Lj+", { "+rq+", "+tp+", "+td+" })");
            cache.put(Lj, new int[] { rq, tp, td });
          }
        }
      }
      PSSubmit(pS140);
      Print("C3");
      int t1, q1, o1;
      if (route[0] == 0) {
        t1 = 0;
        q1 = sq;
        o1 = 1;
      } else {
        output = DBFetch(conn, "S87", 3, sid, route[0]);
        t1 = output[0];
        q1 = output[1];
        o1 = output[2];
      }
      Print("C4");
      PreparedStatement pS80 = PS(conn, "S80");
      PSAdd(pS80, sid, route[0]);
      Print("Issue DELETE FROM CQ WHERE sid="+sid+" AND t2>"+route[0]);
      PSSubmit(pS80);
      Print("C5");
      PreparedStatement pS14 = PS(conn, "S14");
      for (int j = 0; j < bound; j++) {
        int t2 = sched[(3*j)];
        int v2 = sched[(3*j + 1)];
        int Lj = sched[(3*j + 2)];
        if (Lj != sid) {
          int[] qpd = cache.get(Lj);
          int q2 = (t2 == qpd[1] ? q1 + qpd[0] : q1 - qpd[0]);
          int o2 = o1 + 1;
          Print("Issue INSERT INTO CQ VALUES "+sid+", "+sq+", "+se+", "+t1+", "+t2
              +", "+v2+", "+q1+", "+q2+", "+Lj+", "+qpd[0]+", "+qpd[1]+", "+qpd[2]
              +", "+o1+", "+o2);
          PSAdd(pS14, sid, sq, se, t1, t2, v2, q1, q2, Lj,
                qpd[0], qpd[1], qpd[2], o1, o2);
          t1 = t2;
          q1 = q2;
          o1 = o2;
        }
      }
      PSSubmit(pS14);
      Print("C6");
      PreparedStatement pS12 = PS(conn, "S12");
      PreparedStatement pS13 = PS(conn, "S13");
      int rq, re, rl, ro, rd;
      int[] qpd, pd;
      for (int r : rid) {
        output = DBFetch(conn, "S51", 5, r);
        rq = output[0];
        re = output[1];
        rl = output[2];
        ro = output[3];
        rd = output[4];
        qpd = cache.get(r);
        pd = cache2.get(r);
        Print("INSERT INTO PD VALUES ("+sid+", "+qpd[1]+", "+pd[0]+", "+r+")");
        Print("INSERT INTO PD VALUES ("+sid+", "+qpd[2]+", "+pd[1]+", "+r+")");
        PSAdd(pS12, sid, qpd[1], pd[0], r);
        PSAdd(pS12, sid, qpd[2], pd[1], r);
        Print("INSERT INTO CPD VALUES ("+sid+", "+se+", "+route[(route.length - 2)]
          +", "+qpd[1]+", "+pd[0]+", "+qpd[2]+", "+pd[1]+", "+r+", "+re+", "+rl
          +", "+ro+", "+rd+")");
        PSAdd(pS13, sid, se, route[(route.length - 2)], qpd[1], pd[0], qpd[2], pd[1],
              r, re, rl, ro, rd);
      }
      PSSubmit(pS12, pS13);
      Print("C7");
      Print("A4");
      try {
        conn.commit();
        Print("Close connection "+conn.toString());
        conn.close();
        Print("A5");
        for (int r : rid) {
          lu_rstatus.put(r, true);
        }
      }
      // If commit and close fails, we need to rollback the commit
      // and manually close the connection
      catch (SQLException e1) {
        conn.rollback();
        conn.close();
        printSQLException(e1);
        DBSaveBackup(DERBY_DUMPNAME);
        throw new RuntimeException("database failure");
      }
    }
    // If DriverManager.getConnection fails, we throw a 'database failure'
    catch (SQLException e1) {
      printSQLException(e1);
      DBSaveBackup(DERBY_DUMPNAME);
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
      Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL);
      Print("Open connection "+conn.toString());
      sq = lu_users.get(sid)[1];
      se = lu_users.get(sid)[2];
      Print("B1");
      PreparedStatement pS76 = PS(conn, "S76");
      PSAdd(pS76, sid, route[0]);
      Print("Issue DELETE FROM W WHERE sid="+sid+" AND t2>"+route[0]);
      PSSubmit(pS76);
      Print("B2");
      int uid = sid;
      PreparedStatement pS10 = PS(conn, "S10");
      for (int i = 0; i < (route.length - 3); i += 2) {
        int t1 = route[i];
        int v1 = route[(i + 1)];
        int t2 = route[(i + 2)];
        int v2 = route[(i + 3)];
        int dd = lu_edges.get(v1).get(v2)[0];
        int nu = lu_edges.get(v1).get(v2)[1];
        Print("Issue INSERT INTO W VALUES ("+uid+", "+se+", "+t1+", "+v1+", "+t2+", "
          +v2+", "+dd+", "+nu+")");
        PSAdd(pS10, uid, se, t1, v1, t2, v2, dd, nu);
      }
      PSSubmit(pS10);
      Print("B3");
      PreparedStatement pS77 = PS(conn, "S77");
      PreparedStatement pS139 = PS(conn, "S139");
      int te = route[(route.length - 2)];
      int ve = route[(route.length - 1)];
      PSAdd(pS77, te, ve, sid);
      PSAdd(pS139, te, sid);
      Print("Issue UPDATE CW SET te="+te+", ve="+ve+" WHERE sid="+sid);
      Print("Issue UPDATE CPD SET te="+te+" WHERE sid="+sid);
      PSSubmit(pS77, pS139);
      Print("B4");
      int bound = (sched.length/3);
      PreparedStatement pS82 = PS(conn, "S82");
      PreparedStatement pS83 = PS(conn, "S83");
      PreparedStatement pS84 = PS(conn, "S84");
      for (int j = 0; j < bound; j++) {
        int tj = sched[(3*j + 0)];
        int vj = sched[(3*j + 1)];
        int Lj = sched[(3*j + 2)];
        if (Lj != sid) {
          Print("UPDATE CPD SET tp="+tj+" WHERE vp="+vj+" AND rid="+Lj);
          Print("UPDATE CPD SET td="+tj+" WHERE vd="+vj+" AND rid="+Lj);
          Print("UPDATE PD SET t2="+tj+" WHERE v2="+vj+" AND rid="+Lj);
          PSAdd(pS82, tj, vj, Lj);
          PSAdd(pS83, tj, vj, Lj);
          PSAdd(pS84, tj, vj, Lj);
        }
      }
      PSSubmit(pS82, pS83, pS84);
      PreparedStatement pS140 = PS(conn, "S140");
      for (int j = 0; j < bound; j++) {
        int Lj = sched[(3*j + 2)];
        if (Lj != sid) {
          int rq, tp, td;
          if (!cache.containsKey(Lj)) {
            rq = DBFetch(conn, "S85", 1, Lj)[0];
            output = DBFetch(conn, "S86", 2, Lj);
            tp = output[0];
            td = output[1];
            cache.put(Lj, new int[] { rq, tp, td });
            Print("UPDATE CQ SET tp="+tp+", td="+td+" WHERE rid="+Lj);
            PSAdd(pS140, tp, td, Lj);
          }
        }
      }
      PSSubmit(pS140);
      int t1, q1, o1;
      if (route[0] == 0) {
        t1 = 0;
        q1 = sq;
        o1 = 1;
      } else {
        output = DBFetch(conn, "S87", 3, sid, route[0]);
        t1 = output[0];
        q1 = output[1];
        o1 = output[2];
      }
      PreparedStatement pS80 = PS(conn, "S80");
      PSAdd(pS80, sid, route[0]);
      Print("Issue DELETE FROM CQ WHERE sid="+sid+" AND t2>"+route[0]);
      PSSubmit(pS80);
      PreparedStatement pS14 = PS(conn, "S14");
      for (int j = 0; j < bound; j++) {
        int t2 = sched[(3*j)];
        int v2 = sched[(3*j + 1)];
        int Lj = sched[(3*j + 2)];
        if (Lj != sid) {
          int[] qpd = cache.get(Lj);
          int q2 = (t2 == qpd[1] ? q1 + qpd[0] : q1 - qpd[0]);
          int o2 = o1 + 1;
          Print("Issue INSERT INTO CQ VALUES "+sid+", "+sq+", "+se+", "+t1+", "+t2
              +", "+v2+", "+q1+", "+q2+", "+Lj+", "+qpd[0]+", "+qpd[1]+", "+qpd[2]
              +", "+o1+", "+o2);
          PSAdd(pS14, sid, sq, se, t1, t2, v2, q1, q2, Lj,
                qpd[0], qpd[1], qpd[2], o1, o2);
          t1 = t2;
          q1 = q2;
          o1 = o2;
        }
      }
      PSSubmit(pS14);
      PreparedStatement pS42 = PS(conn, "S42");
      PreparedStatement pS43 = PS(conn, "S43");
      for (int r : rid) {
        PSAdd(pS42, r);
        PSAdd(pS43, r);
      }
      PSSubmit(pS42, pS43);
      conn.commit();
      Print("Close connection "+conn.toString());
      conn.close();
    }
    catch (SQLException e1) {
      printSQLException(e1);
      DBSaveBackup(DERBY_DUMPNAME);
      throw new RuntimeException("database failure");
    }
    for (int r : rid) {
      lu_rstatus.remove(r);
    }
  }
  public void setDebug(boolean flag) {
    DEBUG = flag;
  }
  public final ConcurrentHashMap<Integer, int[]> getReferenceVerticesCache() {
    return lu_vertices;
  }
  public final ConcurrentHashMap<Integer,
      ConcurrentHashMap<Integer, int[]>> getReferenceEdgesCache() {
    return lu_edges;
  }
  public final ConcurrentHashMap<Integer, int[]> getReferenceUsersCache() {
    return lu_users;
  }
  public void DBLoadDataModel() throws RuntimeException {
    Print("Load data model");
    try {
      Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL);
      Print("Open connection "+conn.toString());
      Statement stmt = conn.createStatement();
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
                      + "        AND rid IS NULL AND rq IS NULL AND tp IS NULL AND td IS NULL"
                      + "    ELSE q2 <= 0 AND o2 = o1 + 1"
                      + "  END"
                      + ") INITIALLY DEFERRED,"
                      + "CONSTRAINT  C99 CHECK (CASE WHEN t2 = tp THEN q2 = q1 + rq END) INITIALLY DEFERRED,"
                      + "CONSTRAINT C100 CHECK (CASE WHEN t2 = td THEN q2 = q1 - rq END) INITIALLY DEFERRED,"
                      + "CONSTRAINT C101 UNIQUE (t2, v2, rid)"
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
                      + "SELECT CAST(CAST(A.NUM AS FLOAT) / CAST(A.DENOM AS FLOAT) * 10000 as INT)"
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
      stmt.addBatch("CREATE VIEW dist_r_unassigned (val) AS "
                      + "SELECT SUM (rb) FROM R LEFT JOIN assignments_r "
                      + "  ON R.rid = assignments_r.rid "
                      + "WHERE assignments_r.rid IS NULL");
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
      stmt.addBatch("CREATE INDEX W_sid_t1 ON W (sid, t1)");
      stmt.addBatch("CREATE INDEX W_sid_t2 ON W (sid, t2)");
      stmt.addBatch("CREATE INDEX W_sid_v2 ON W (sid, v2)");
      stmt.addBatch("CREATE INDEX W_sid_t1_t2 ON W (sid, t1, t2)");
      stmt.executeBatch();
      conn.commit();
      Print("Close connection "+conn.toString());
      conn.close();
    }
    catch (SQLException e1) {
      printSQLException(e1);
      DBSaveBackup(DERBY_DUMPNAME);
      throw new RuntimeException("database failure");
    }
  }
  public void DBLoadBackup(String p) throws RuntimeException {
    Print("Load database backup ("+p+")");
    try {
      CONNECTIONS_URL = "jdbc:derby:memory:jargobak;createFrom="+p;
      setupDriver();
      Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL);
      Print("Open connection "+conn.toString());
      CallableStatement cs = conn.prepareCall(
        "CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY(?, ?)");
      Print("..set derby.storage.pageCacheSize="+DERBY_PAGECACHESIZE);
      cs.setString(1, "derby.storage.pageCacheSize");
      cs.setInt(2, DERBY_PAGECACHESIZE);
      cs.execute();
      Print("Close connection "+conn.toString());
      conn.close();
      if (lu_vertices.isEmpty()) {
        int[] output = DBQueryAllVertices();
        for (int i = 0; i < (output.length/3); i++) {
          int v = output[(3*i)];
          int lng = output[(3*i + 1)];
          int lat = output[(3*i + 2)];
          lu_vertices.put(v, new int[] { lng, lat });
        }
        output = DBQueryAllEdges();
        for (int i = 0; i < (output.length/4); i++) {
          int v1 = output[(4*i)];
          int v2 = output[(4*i + 1)];
          int dd = output[(4*i + 2)];
          int nu = output[(4*i + 3)];
          if (!lu_edges.containsKey(v1)) {
            lu_edges.put(v1, new ConcurrentHashMap());
          }
          lu_edges.get(v1).put(v2, new int[] { dd, nu });
        }
      }
     if (lu_users.isEmpty()) {
        int[] output = new int[] { };
        try {
          conn = DriverManager.getConnection(CONNECTIONS_POOL_URL);
          Print("Open connection "+conn.toString());
          output = DBFetch(conn, "S141", 7);
          for (int i = 0; i < (output.length/7); i++) {
            int uid = output[(7*i)];
            int uq = output[(7*i + 1)];
            int ue = output[(7*i + 2)];
            int ul = output[(7*i + 3)];
            int uo = output[(7*i + 4)];
            int ud = output[(7*i + 5)];
            int ub = output[(7*i + 6)];
            lu_users.put(uid, new int[] { uid, uq, ue, ul, uo, ud, ub });
            if (uq > 0) {
              lu_rstatus.put(uid, (DBFetch(conn, "S148", 1, uid).length > 0 ? true : false));
            }
          }
          Print("Close connection "+conn.toString());
          conn.close();
        }
        catch (SQLException e1) {
          printSQLException(e1);
          DBSaveBackup(DERBY_DUMPNAME);
          throw new RuntimeException("database failure");
        }
     }
    }
    catch (SQLException e1) {
      printSQLException(e1);
      DBSaveBackup(DERBY_DUMPNAME);
      throw new RuntimeException("database failure");
    }
  }
  public void DBSaveBackup(String p) throws RuntimeException {
    try {
      Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL);
      Print("Open connection "+conn.toString());
      CallableStatement cs = conn.prepareCall(
        "CALL SYSCS_UTIL.SYSCS_BACKUP_DATABASE(?)");
      cs.setString(1, p);
      cs.execute();
      Print("Close connection "+conn.toString());
      conn.close();
    }
    catch (SQLException e1) {
      printSQLException(e1);
      DBSaveBackup(DERBY_DUMPNAME);
      throw new RuntimeException("database failure");
    }
  }
  public void printSQLDriverStatistics() {
    try {
      PoolingDriver d = (PoolingDriver) DriverManager.getDriver(CONNECTIONS_DRIVER_URL);
      ObjectPool<? extends Connection> cp = d.getConnectionPool(CONNECTIONS_POOL_NAME);
      System.out.println("Connections: "+cp.getNumActive()+" active; "+cp.getNumIdle()+" idle");
    }
    catch (SQLException e1) {
      printSQLException(e1);
      DBSaveBackup(DERBY_DUMPNAME);
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
  public Connection _getConnection() throws RuntimeException {
    try {
      return DriverManager.getConnection(CONNECTIONS_POOL_URL);
    }
    catch (SQLException e1) {
      printSQLException(e1);
      DBSaveBackup(DERBY_DUMPNAME);
      throw new RuntimeException("database failure");
    }
  }
    private void Print(String msg) {
      if (DEBUG) {
        System.out.println("[Jargo][Storage]["+LocalDateTime.now()+"] "+msg);
      }
    }
    private void setupDriver() throws RuntimeException {
      Print("Initialize JDBC driver");
      try {
        Print("..initialize connection factory");
        connection_factory = new DriverManagerConnectionFactory(CONNECTIONS_URL);
        Print("..initialize poolable-connection factory");
        poolableconnection_factory = new PoolableConnectionFactory(connection_factory, null);
        poolableconnection_factory.setPoolStatements(true);
        poolableconnection_factory.setDefaultAutoCommit(false);
        poolableconnection_factory.setMaxOpenPreparedStatements(STATEMENTS_MAX_COUNT);
        Print("..initialize connection pool");
        GenericObjectPoolConfig cfg = new GenericObjectPoolConfig();
        cfg.setMinIdle(100000);
        cfg.setMaxIdle(100000);
        cfg.setMaxTotal(100000);
        pool = new GenericObjectPool<>(poolableconnection_factory, cfg);
        Print("..register connection pool to driver");
        poolableconnection_factory.setPool(pool);
        Class.forName("org.apache.commons.dbcp2.PoolingDriver");
        driver = (PoolingDriver) DriverManager.getDriver(CONNECTIONS_DRIVER_URL);
        driver.registerPool(CONNECTIONS_POOL_NAME, pool);
        Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL);
        Print("Open connection "+conn.toString());
        CallableStatement cs = conn.prepareCall(
          "CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY(?, ?)");
        Print("..set derby.storage.pageCacheSize="+DERBY_PAGECACHESIZE);
        cs.setString(1, "derby.storage.pageCacheSize");
        cs.setInt(2, DERBY_PAGECACHESIZE);
        cs.execute();
        Print("Close connection "+conn.toString());
        conn.close();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    private void PSInit() {
      Print("Initialize prepared statement strings");
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
      lu_pstr.put("S0", INS+"V VALUES "+q3);
      lu_pstr.put("S1", INS+"E VALUES "+q4);
      lu_pstr.put("S2", INS+"UQ VALUES "+q2);
      lu_pstr.put("S3", INS+"UE VALUES "+q2);
      lu_pstr.put("S4", INS+"UL VALUES "+q2);
      lu_pstr.put("S5", INS+"UO VALUES "+q2);
      lu_pstr.put("S6", INS+"UD VALUES "+q2);
      lu_pstr.put("S7", INS+"UB VALUES "+q2);
      lu_pstr.put("S8", INS+"S VALUES "+q7);
      lu_pstr.put("S9", INS+"R VALUES "+q7);
      lu_pstr.put("S10", INS+"W VALUES "+q8);
      lu_pstr.put("S11", INS+"CW VALUES "+q9);
      lu_pstr.put("S12", INS+"PD VALUES "+q4);
      lu_pstr.put("S13", INS+"CPD VALUES "+q12);
      lu_pstr.put("S14", INS+"CQ VALUES "+q14);
      lu_pstr.put("S15", UPD+"E SET nu=? WHERE v1=? AND v2=?");
      lu_pstr.put("S131", UPD+"W SET nu=? WHERE v1=? AND v2=?");
      lu_pstr.put("S77", UPD+"CW SET te=?, ve=? WHERE sid=?");
      lu_pstr.put("S84", UPD+"PD SET t2=? WHERE v2=? AND rid=?");
      lu_pstr.put("S82", UPD+"CPD SET tp=? WHERE vp=? AND rid=?");
      lu_pstr.put("S83", UPD+"CPD SET td=? WHERE vd=? AND rid=?");
      lu_pstr.put("S76", DEL+"W WHERE sid=? AND t2>?");
      lu_pstr.put("S42", DEL+"PD WHERE rid=?");
      lu_pstr.put("S43", DEL+"CPD WHERE rid=?");
      lu_pstr.put("S80", DEL+"CQ WHERE sid=? AND t2>?");
      lu_pstr.put("S62", SEL+"COUNT (*) FROM V WHERE v<>0");
      lu_pstr.put("S64", SEL+"MIN (lng), MAX (lng), MIN (lat), MAX (lat) "
            + "FROM V WHERE v<>0");
      lu_pstr.put("S63", SEL+"COUNT (*) FROM E WHERE v1<>0 AND v2<>0");
      lu_pstr.put("S65", SEL+"MIN (dd), MAX (dd), SUM (dd) / COUNT (dd), "
            + "MIN (nu), MAX (nu), SUM (nu) / COUNT (nu) "
            + "FROM E WHERE v1<>0 AND v2<>0");
      lu_pstr.put("S46", SEL+"dd, nu FROM E WHERE v1=? AND v2=?");
      lu_pstr.put("S130", SEL+"lng, lat FROM V WHERE v=?");
      lu_pstr.put("S70", SEL+"sid, sq, se, sl, so, sd, sb FROM S WHERE sid=?");
      lu_pstr.put("S48", SEL+"sq, se FROM S WHERE sid=?");
      lu_pstr.put("S66", SEL+"COUNT (*) FROM S");
      lu_pstr.put("S75", SEL+"rid, rq, re, rl, ro, rd, rb FROM R WHERE rid=?");
      lu_pstr.put("S51", SEL+"rq, re, rl, ro, rd FROM R WHERE rid=?");
      lu_pstr.put("S67", SEL+"COUNT (*) FROM R");
      lu_pstr.put("S59", SEL+"a.sid, a.t2, a.v2 FROM W AS a INNER JOIN ("
            + "SELECT sid, MIN(ABS(t2-?)) as tdiff FROM W WHERE t2<=? AND v2<>0 "
            + "GROUP BY sid"
            + ") as b ON a.sid=b.sid AND ABS(a.t2-?)=b.tdiff AND a.t2<=?");
      lu_pstr.put("S128", SEL+"a.sid, a.t2, a.v2 FROM W AS a INNER JOIN ("
            + "SELECT sid FROM CW WHERE te>? OR (ve=0 AND sl>?)"
            + ") as b ON a.sid=b.sid INNER JOIN ("
            + "SELECT sid, MIN(ABS(t2-?)) as tdiff FROM W WHERE t2<=? AND v2<>0 "
            + "GROUP BY sid"
            + ") as c ON a.sid=c.sid AND ABS(a.t2-?)=c.tdiff AND a.t2<=?");
      lu_pstr.put("S60", SEL+"t, v FROM r_server WHERE sid=? ORDER BY t ASC");
      lu_pstr.put("S129", SEL+"t, v FROM r_server WHERE sid=? AND t>? ORDER BY t ASC");
      lu_pstr.put("S61", SEL+"t, v, Ls, Lr FROM r_server WHERE sid=?"
            + "AND (Ls IS NOT NULL OR Lr IS NOT NULL) ORDER BY t ASC");
      lu_pstr.put("S69", SEL+"t, v, Ls, Lr "
            + "FROM r_server LEFT JOIN CQ ON t=t2 and v=v2 and Lr=rid "
            + "WHERE r_server.sid=?"
            + "   AND (t>? OR v=0)"
            + "   AND (Ls IS NOT NULL OR Lr IS NOT NULL)"
            + "ORDER BY t ASC, o2 ASC");
      // A "timeout" of 30 seconds is hard-coded here
      lu_pstr.put("S68", SEL+"* FROM R WHERE re<=? AND ?<=re+30 AND rid NOT IN  "
            + "(SELECT rid FROM assignments_r)");
      lu_pstr.put("S85", SEL+"uq FROM UQ WHERE uid=?");
      lu_pstr.put("S86", SEL+"tp, td FROM CPD WHERE rid=?");
      lu_pstr.put("S73", SEL+"q2 FROM CQ WHERE sid=? AND t2<=? "
            + "ORDER BY t2 DESC, q2 DESC FETCH FIRST ROW ONLY");
      lu_pstr.put("S87", SEL+"t2, q2, o2 FROM CQ WHERE sid=? AND t2<=? "
            + "ORDER BY t2 DESC, o2 DESC FETCH FIRST ROW ONLY");
      lu_pstr.put("S100", SEL+"rid FROM assignments WHERE t>? AND sid=?");
      lu_pstr.put("S101", SEL+"rid FROM assignments WHERE t<=? AND sid=?");
      lu_pstr.put("S102", SEL+"* FROM service_rate");
      lu_pstr.put("S103", SEL+"* FROM dist_base");
      lu_pstr.put("S104", SEL+"val FROM dist_s_travel WHERE sid=?");
      lu_pstr.put("S105", SEL+"SUM (val) FROM dist_s_travel");
      lu_pstr.put("S106", SEL+"val FROM dist_s_cruising WHERE sid=?");
      lu_pstr.put("S107", SEL+"SUM (val) FROM dist_s_cruising");
      lu_pstr.put("S108", SEL+"val FROM dist_s_service WHERE sid=?");
      lu_pstr.put("S109", SEL+"SUM (val) FROM dist_s_service");
      lu_pstr.put("S110", SEL+"val FROM dist_s_base");
      lu_pstr.put("S111", SEL+"val FROM dist_r_base");
      lu_pstr.put("S112", SEL+"val FROM dist_r_detour WHERE rid=?");
      lu_pstr.put("S113", SEL+"SUM (val) FROM dist_r_detour");
      lu_pstr.put("S114", SEL+"val FROM dist_r_transit WHERE rid=?");
      lu_pstr.put("S115", SEL+"SUM (val) FROM dist_r_transit");
      lu_pstr.put("S116", SEL+"val FROM dur_s_travel WHERE sid=?");
      lu_pstr.put("S117", SEL+"SUM (val) FROM dur_s_travel");
      lu_pstr.put("S118", SEL+"val FROM dur_r_pickup WHERE rid=?");
      lu_pstr.put("S119", SEL+"SUM (val) FROM dur_r_pickup");
      lu_pstr.put("S120", SEL+"val FROM dur_r_transit WHERE rid=?");
      lu_pstr.put("S121", SEL+"SUM (val) FROM dur_r_transit");
      lu_pstr.put("S122", SEL+"val FROM dur_r_travel WHERE rid=?");
      lu_pstr.put("S123", SEL+"SUM (val) FROM dur_r_travel");
      lu_pstr.put("S124", SEL+"val FROM t_r_depart WHERE rid=?");
      lu_pstr.put("S125", SEL+"val FROM t_s_depart WHERE sid=?");
      lu_pstr.put("S126", SEL+"val FROM t_r_arrive WHERE rid=?");
      lu_pstr.put("S127", SEL+"val FROM t_s_arrive WHERE sid=?");
      lu_pstr.put("S133", SEL+"val FROM f_status WHERE rid=? AND t<=? "
          + "ORDER BY t DESC FETCH FIRST ROW ONLY");
      lu_pstr.put("S134", SEL+"sid, te FROM CW WHERE se<=? AND (?<te OR (ve=0 AND sl>?))");
      lu_pstr.put("S135", SEL+"t2, v2 FROM W WHERE sid=? AND t2=("
          + "SELECT t1 FROM W WHERE sid=? AND t1 <= ? AND ? < t2)");
      lu_pstr.put("S136", SEL+"* FROM V");
      lu_pstr.put("S137", SEL+"* FROM E");
      lu_pstr.put("S138", SEL+"val FROM dist_r_unassigned");
      lu_pstr.put("S139", UPD+"CPD SET te=? WHERE sid=?");
      lu_pstr.put("S140", UPD+"CQ SET tp=?, td=? WHERE rid=?");
      lu_pstr.put("S141", SEL+"* FROM r_user");
      lu_pstr.put("S142", SEL+"SUM (dd) FROM W WHERE sid=? AND t2>?");
      lu_pstr.put("S143", SEL+"* FROM R WHERE re<=? AND ?<=re+?");
      lu_pstr.put("S144", SEL+"t2, v2, rid FROM CQ WHERE sid=? AND t2>? ORDER BY o2 ASC");
      lu_pstr.put("S145", SEL+"te, ve FROM CW WHERE sid=?");
      lu_pstr.put("S147", SEL+"t2, v2 FROM W WHERE sid=? AND t2=("
          + "SELECT t1 FROM W WHERE sid=? AND v2=0)");
      lu_pstr.put("S148", SEL+"1 FROM assignments_r WHERE rid=?");
      lu_pstr.put("S149", SEL+"t2, v2 FROM W WHERE sid=? ORDER BY t2 ASC");
    }
    private PreparedStatement PS(Connection conn, String k) throws SQLException {
      Print("Prepare statement "+k);
      PreparedStatement p = null;
      try {
        p = conn.prepareStatement(lu_pstr.get(k),
          ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        p.clearBatch();
        p.clearParameters();
      }
      catch (SQLException e) {
        throw e;
      }
      return p;
    }
    private int[] DBFetch(Connection conn,
        String k, int ncols, Integer... values) throws SQLException {
      PreparedStatement p = PS(conn, k);
      Print("DBFetch("+lu_pstr.get(k)+")");
      Print("DBFetch use conn "+conn.toString());
      Print("DBFetch use thread "+Thread.currentThread().getName());
      int[] output = new int[] { };
      for (int i = 0; i < values.length; i++) {
        if (values[i] == null) {
          p.setNull((i + 1), java.sql.Types.INTEGER);
          Print("Set param"+i+"=NULL");
        } else {
          p.setInt ((i + 1), values[i]);
          Print("Set param"+(i + 1)+"="+values[i]);
        }
      }
      try {
        ResultSet res = p.executeQuery();  // <-- if stuck here, investigate locks
        if (res.last() == true) {
          output = new int[(ncols*res.getRow())];
          res.first();
          do {
            for (int j = 1; j <= ncols; j++) {
              output[((res.getRow() - 1)*ncols + (j - 1))] = res.getInt(j);
            }
          } while (res.next());
        }
        res.close();
      }
      catch (SQLException e) {
        throw e;
      }
      p.close();
      Print("DBFetch close conn "+conn.toString());
      if (output.length == 0) {
        Print("WARNING: DBFetch(3...) returned empty!");
      }
      return output;
    }
    private void PSAdd(PreparedStatement p, Integer... values) throws SQLException {
      p.clearParameters();
      for (int i = 0; i < values.length; i++) {
        if (values[i] == null) {
          p.setNull((i + 1), java.sql.Types.INTEGER);
          Print("Set param"+i+"=NULL");
        } else {
          p.setInt ((i + 1), values[i]);
          Print("Set param"+(i + 1)+"="+values[i]);
        }
      }
      try {
        p.addBatch();
      }
      catch (SQLException e) {
        throw e;
      }
    }
    private void PSSubmit(PreparedStatement... statements) throws SQLException {
      try {
        for (PreparedStatement p : statements) {
          p.executeBatch();
          p.close();
        }
      }
      catch (SQLException e) {
        throw e;
      }
    }
}
